import json
import secrets
import bcrypt
import datetime
from django.core.exceptions import PermissionDenied
from django.db.models import F, ExpressionWrapper, FloatField
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.utils import timezone
from .models import User, UserSession, Ingredients, Recipes, UserFavorites, UserMealPlan

def authenticate_user(request): # Autenticar al usuario
    session_token = request.headers.get('SessionToken', None)
    if session_token is None:
        raise PermissionDenied({"response": "unauthorized"}, status=401)
    try:
        user_session = UserSession.objects.get(token=session_token)
    except UserSession.DoesNotExist:
        raise PermissionDenied({"response": "unauthorized"}, status=401)
    return user_session

#Funciones de validación de formatos
def check_email_format(email):
    if email is None or '@' not in email or len(email) < 8:
        return False
    return True

def check_password_format(password):
    if len(password) < 6:
        return False
    return True

def check_birthdate_format(birthdate):
    try:
        birthdate_dt = datetime.datetime.strptime(birthdate, '%Y-%m-%d')
        current_year = datetime.datetime.now().year
        if birthdate_dt.year == current_year:
            return False
    except ValueError:
        return False
    return True

@csrf_exempt
def sessions(request): # Manejo de sesiones
    if request.method == 'POST':
        try: # Se obtienen los datos del cliente
            client_json = json.loads(request.body)
            json_email = client_json['email']
            json_password = client_json['password']
        except KeyError:
            return JsonResponse({"response": "not_ok"}, status=400)
        try: # Se verifica si el usuario existe en la base de datos
            db_user = User.objects.get(email=json_email)
        except User.DoesNotExist:
            return JsonResponse({"response": "not_ok"}, status=404)
        # Se verifica si las contraseñas coinciden y se maneja la sesión
        if bcrypt.checkpw(json_password.encode('utf8'), db_user.password.encode('utf8')):
            if 'SessionToken' in request.headers and request.headers['SessionToken'] is not None:
                try:
                    user_session = authenticate_user(request)
                    return JsonResponse({"response": "ok", "SessionToken": user_session.token}, status=200)
                except PermissionDenied:
                    return JsonResponse({"response": "unauthorized"}, status=401)
            else:
                random_token = secrets.token_hex(10)
                session = UserSession(user=db_user, token=random_token)
                session.save()
            return JsonResponse({"response": "ok", "SessionToken": random_token}, status=201)
        else:
            # Las contraseñas no coinciden
            return JsonResponse({"response": "unauthorized"}, status=401)
    else:
        return JsonResponse({"response": "http_method_unsupported"}, status=405)

@csrf_exempt
def user(request): # Manejo de usuarios
    if request.method == 'POST':
        # Se intenta obtener la sesión del usuario
        try:
            client_json = json.loads(request.body)
            client_email = client_json['email']
            client_username = client_json['username']
            client_password = client_json['password']
            client_birthdate = client_json['birthdate']
        except KeyError:
            return JsonResponse({"response": "not_ok"}, status=400)

        # Se verifican los datos y su formato
        if not client_username or not client_email or not client_password or not client_birthdate:
                return JsonResponse({"response": "not_ok"}, status=400)
        
        if not (check_email_format(client_email) and check_password_format(client_password) and check_birthdate_format(client_birthdate)):
            return JsonResponse({"response": "not_ok"}, status=400)
        try: # Se verifica si el usuario ya existe en la base de datos
            existing_user = User.objects.get(email=client_email)
            if existing_user.email == client_email:
                return JsonResponse({"response": "already_exist"}, status=409)
        except User.DoesNotExist:
            pass

        # Se crea el nuevo usuario
        salted_and_hashed_pass = bcrypt.hashpw(client_password.encode('utf8'), bcrypt.gensalt()).decode('utf8')
        new_user = User(email=client_email, username=client_username, password=salted_and_hashed_pass, birthdate=client_birthdate)
        new_user.save()

        # Se genera un token de sesión para el nuevo usuario
        random_token = secrets.token_hex(16)
        session = UserSession(user=new_user, token=random_token)
        session.save()

        return JsonResponse({"response": "ok", "SessionToken": random_token}, status=201)

    elif request.method == 'GET':
        try: # Se obtiene la información del usuario autenticado
            user_session = authenticate_user(request)
            user_id = user_session.user.id
            full_user = User.objects.get(id=user_id)
            json_response = full_user.to_json()
            return JsonResponse(json_response, safe=False, status=200)
        except PermissionDenied:
            return JsonResponse({'error': 'unauthorized'}, status=401)
    
    elif request.method == 'PUT':
        # Se actualizan los datos del usuario
        try:
            user_session = authenticate_user(request)
        except PermissionDenied:
            return JsonResponse({'error': 'unauthorized'}, status=401)
        try:
            body_json = json.loads(request.body)
            email = body_json.get('email', None)
            username = body_json.get('username', None)
            password = body_json.get('password', None)
            birthdate = body_json.get('birthdate', None)
            if not (email and username and password and birthdate):
                return JsonResponse({"response": "not_ok"}, status=400)
        except KeyError:
            return JsonResponse({"response": "not_ok"}, status=400)
        if not (check_email_format(email) and check_password_format(password) and check_birthdate_format(birthdate)):
            return JsonResponse({"response": "not_ok"}, status=400)
        try:
            existing_user = User.objects.get(email=email)
            if existing_user.email == email:
                return JsonResponse({"response": "already_exist"}, status=409)
        except User.DoesNotExist:
            pass

        # Se actualizan los datos del usuario en la base de datos
        salted_and_hashed_pass = bcrypt.hashpw(password.encode('utf8'), bcrypt.gensalt()).decode('utf8')
        user = User.objects.get(id=user_session.user.id)
        user.email = email
        user.username = username
        user.password = salted_and_hashed_pass
        user.birthdate = birthdate
        user.save()
        return JsonResponse({"response": "ok"}, status=200)

    elif request.method == 'DELETE':
        try:
            try:
                user_session = authenticate_user(request)
            except PermissionDenied:
                return JsonResponse({'response': 'unauthorized'}, status=401)
            user = User.objects.get(id=user_session.user.id)
            user.delete() # Se elimina el usuario
            return JsonResponse({'response': 'ok'}, status=200)
        except User.DoesNotExist:
            return JsonResponse({'response': 'not_ok'}, status=404)
    else:
        return JsonResponse({"response": "not_ok"}, status=405)

@csrf_exempt
def user_favorites(request): # Manejo de los favoritos del usuario
    if request.method == 'GET':
        try: # Se intenta obtener la sesión del usuario
            user_session = authenticate_user(request)
            user = User.objects.get(id=user_session.user.id)
            favorites = UserFavorites.objects.filter(user=user)
            favorites_json = [favorite.recipe.to_json() for favorite in favorites]
            return JsonResponse({'favorites': favorites_json}, status=200)
        except PermissionDenied:
            return JsonResponse({'response': 'unauthorized'}, status=401)
        except User.DoesNotExist:
            return JsonResponse({'response': 'not_ok'}, status=404)

    elif request.method == 'POST':
        try: # Se crea un nuevo favorito para el usuario
            user_session = authenticate_user(request)
            user = User.objects.get(id=user_session.user.id)
            data = json.loads(request.body)
            recipe_id = data['recipe_id']
            # Se verifica si el usuario ya tiene la receta como favorita
            if UserFavorites.objects.filter(user=user, recipe_id=recipe_id).exists():
                return JsonResponse({'response': 'already_exists'}, status=400)
            recipe = Recipes.objects.get(id=data['recipe_id'])
            new_favorite = UserFavorites.objects.create(user=user, recipe=recipe)
            return JsonResponse({'response': 'ok'}, status=201)
        except PermissionDenied:
            return JsonResponse({'response': 'unauthorized'}, status=401)
        except User.DoesNotExist:
            return JsonResponse({'response': 'not_ok'}, status=404)
        except Recipes.DoesNotExist:
            return JsonResponse({'response': 'not_ok'}, status=404)
    else:
        return JsonResponse({"response": "not_ok"}, status=405)

@csrf_exempt
def delete_user_favorite(request, recipe_id):
    if request.method == 'DELETE':
        try: # Se intenta eliminar el favorito del usuario
            user_session = authenticate_user(request)
            user = User.objects.get(id=user_session.user.id)
            favorite = UserFavorites.objects.get(user=user, recipe_id=recipe_id)
            favorite.delete() # Se elimina el favorito
            return JsonResponse({'response': 'ok'}, status=200)
        except PermissionDenied:
            return JsonResponse({'response': 'unauthorized'}, status=401)
        except UserFavorites.DoesNotExist:
            return JsonResponse({'response': 'not_ok'}, status=404)
    else:
        return JsonResponse({"response": "not_ok"}, status=405)

@csrf_exempt
def get_mealplan(request, day):
    if request.method == 'GET':
        try:  # Se intenta obtener los planes de comida para el día especificado
            user_session = authenticate_user(request)
            user = User.objects.get(id=user_session.user.id)
            meal_plans = UserMealPlan.objects.filter(user=user, week_day=day)
            meal_plans_data = {}
            for meal_plan in meal_plans: # Se recorre cada plan de comida obtenido para el día especificado
                day_name = dict(UserMealPlan.WEEKDAYS)[meal_plan.week_day] # Se obtiene el nombre del día correspondiente al plan de comida
                # Si el nombre del día no está en los datos de los planes de comida, se agrega como una nueva entrada
                if day_name not in meal_plans_data:
                    meal_plans_data[day_name] = []
                meal_plans_data[day_name].append(meal_plan.to_json()) # Se agrega el plan de comida al día correspondiente
            return JsonResponse({'meal_plans': meal_plans_data}, status=200)
        except PermissionDenied:
            return JsonResponse({'response': 'unauthorized'}, status=401)
        except User.DoesNotExist:
            return JsonResponse({'response': 'not_ok'}, status=404)

@csrf_exempt
def user_mealplan(request, day, recipe_id):
    if request.method == 'POST':
        try:  # Se intenta agregar un plan de comida para el día especificado
            user_session = authenticate_user(request)
            user = User.objects.get(id=user_session.user.id)
            recipe = Recipes.objects.get(id=recipe_id)
            valid_days = [choice[0] for choice in UserMealPlan.WEEKDAYS]
            if day not in valid_days:
                return JsonResponse({'response': 'not_ok'}, status=400)

            user_mealplans = UserMealPlan.objects.filter(user=user, week_day=day, recipe_id=recipe_id)

            if user_mealplans.exists(): # Se verifica si el plan de comida ya contiene la receta especificada
                return JsonResponse({'response': 'not_ok'}, status=400)

            # Se crea un nuevo plan de comida
            new_mealplan = UserMealPlan.objects.create(user=user, recipe=recipe, week_day=day)
            return JsonResponse({'response': 'ok'}, status=201)
        except PermissionDenied:
            return JsonResponse({'response': 'unauthorized'}, status=401)
        except User.DoesNotExist:
            return JsonResponse({'response': 'not_ok'}, status=404)
        except Recipes.DoesNotExist:
            return JsonResponse({'response': 'not_ok'}, status=404)

    elif request.method == 'DELETE':
        try: # Se intenta eliminar un plan de comida para el día especificado
            user_session = authenticate_user(request)
            user = User.objects.get(id=user_session.user.id)
            mealplan = UserMealPlan.objects.get(user=user, recipe_id=recipe_id, week_day=day)
            mealplan.delete() # Se elimina el plan de comida
            return JsonResponse({'response': 'ok'}, status=200)
        except PermissionDenied:
            return JsonResponse({'response': 'unauthorized'}, status=401)
        except UserMealPlan.DoesNotExist:
            return JsonResponse({'response': 'not_ok'}, status=404)
    else:
        return JsonResponse({"response": "not_ok"}, status=405)

@csrf_exempt
def recipes(request):
    if request.method == 'POST':
        data = json.loads(request.body) # Se obtienen los datos de la receta del cuerpo de la solicitud

        # Se extraen los campos de la receta
        image_link = data.get('image_link')
        name = data.get('name')
        recipe = data.get('recipe')
        food_type = data.get('food_type')
        ingredients_str = data.get('ingredients')

        # Se dividen los ingredientes y se eliminan los espacios en blanco
        ingredient_names = [ingredient.strip() for ingredient in ingredients_str.split(',')]
        # Se obtienen los objetos Ingredient correspondientes a los nombres de los ingredientes
        ingredients = Ingredients.objects.filter(name__in=ingredient_names)

        # Se crea una nueva receta con los datos proporcionados
        new_recipe = Recipes.objects.create(image_link=image_link, name=name, recipe=recipe, food_type=food_type)
        new_recipe.ingredients.set(ingredients)
        response_data = new_recipe.to_json()
        return JsonResponse(response_data)

    elif request.method == 'GET':
        # Se obtienen los query params food_type e ingredient
        food_type = request.GET.get('food_type')
        ingredient = request.GET.get('ingredient')

        recipes_queryset = Recipes.objects.all()

        if food_type: # Se filtran las recetas por tipo de comida si se proporciona food_type
            recipes_queryset = recipes_queryset.filter(food_type=food_type)
        if ingredient: # Se filtran las recetas por ingredientes si se proporciona ingredient
            recipes_queryset = recipes_queryset.filter(ingredients__name=ingredient)

        recipes_data = [recipe.to_json() for recipe in recipes_queryset]

        return JsonResponse(recipes_data, safe=False)
    
    else:
        return JsonResponse({"response": "not_ok"}, status=405)

@csrf_exempt
def recipe_detail(request, recipe_id):
    if request.method == 'GET':
        try:
            recipe = Recipes.objects.get(id=recipe_id) # Se obtiene la receta por su ID
            return JsonResponse(recipe.to_json(), status=200)
        except Recipes.DoesNotExist:
            return JsonResponse({'response': 'not_ok'}, status=404)
    
    elif request.method == 'DELETE':
        try:
            recipe = Recipes.objects.get(id=recipe_id)
            recipe.delete() # Se elimina la receta
            return JsonResponse({'response': 'ok'}, status=204)
        except Recipes.DoesNotExist:
            return JsonResponse({'response': 'not_ok'}, status=404)

    else:
        return JsonResponse({'response': 'not_ok'}, status=405)
    

def ingredients(request):
    if request.method == 'GET':
        ingredients = Ingredients.objects.all().values('name')
        ingredients_list = list(ingredients)
        return JsonResponse(ingredients_list, safe=False)
    else:
        return JsonResponse({'response': 'not_ok'}, status=405)

def recipes_by_ingredient(request, ingredient):
    if request.method == 'GET':
        recipes_queryset = Recipes.objects.filter(ingredients__name=ingredient)
        recipes_data = [recipe.to_json() for recipe in recipes_queryset]
        return JsonResponse(recipes_data, safe=False)
    else:
        return JsonResponse({"response": "not_ok"}, status=405)