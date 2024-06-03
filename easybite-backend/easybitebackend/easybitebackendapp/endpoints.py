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

def authenticate_user(request):
    session_token = request.headers.get('SessionToken', None)
    if session_token is None:
        raise PermissionDenied({"response": "unauthorized"}, status=401)
    try:
        user_session = UserSession.objects.get(token=session_token)
    except UserSession.DoesNotExist:
        raise PermissionDenied({"response": "unauthorized"}, status=401)
    return user_session


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
def sessions(request):
    if request.method == 'POST':
        try:
            client_json = json.loads(request.body)
            json_email = client_json['email']
            json_password = client_json['password']
        except KeyError:
            return JsonResponse({"response": "not_ok"}, status=400)
        try:
            db_user = User.objects.get(email=json_email)
        except User.DoesNotExist:
            return JsonResponse({"response": "not_ok"}, status=404)
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
            return JsonResponse({"response": "unauthorized"}, status=401)
    else:
        return JsonResponse({"response": "http_method_unsupported"}, status=405)

@csrf_exempt
def user(request):
    if request.method == 'POST':
        try:
            client_json = json.loads(request.body)
            client_email = client_json['email']
            client_username = client_json['username']
            client_password = client_json['password']
            client_birthdate = client_json['birthdate']
        except KeyError:
            return JsonResponse({"response": "not_ok"}, status=400)

        if not client_username or not client_email or not client_password or not client_birthdate:
                return JsonResponse({"response": "not_ok"}, status=400)
        
        if not (check_email_format(client_email) and check_password_format(client_password) and check_birthdate_format(client_birthdate)):
            return JsonResponse({"response": "not_ok"}, status=400)
        try:
            existing_user = User.objects.get(email=client_email)
            if existing_user.email == client_email:
                return JsonResponse({"response": "already_exist"}, status=409)
        except User.DoesNotExist:
            pass

        salted_and_hashed_pass = bcrypt.hashpw(client_password.encode('utf8'), bcrypt.gensalt()).decode('utf8')
        new_user = User(email=client_email, username=client_username, password=salted_and_hashed_pass, birthdate=client_birthdate)
        new_user.save()

        random_token = secrets.token_hex(16)
        session = UserSession(user=new_user, token=random_token)
        session.save()

        return JsonResponse({"response": "ok", "SessionToken": random_token}, status=201)

    elif request.method == 'GET':
        try:
            user_session = authenticate_user(request)
            user_id = user_session.user.id
            full_user = User.objects.get(id=user_id)
            json_response = full_user.to_json()
            return JsonResponse(json_response, safe=False, status=200)
        except PermissionDenied:
            return JsonResponse({'error': 'unauthorized'}, status=401)
    
    elif request.method == 'PUT':
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
            user.delete()
            return JsonResponse({'response': 'ok'}, status=200)
        except User.DoesNotExist:
            return JsonResponse({'response': 'not_ok'}, status=404)
    else:
        return JsonResponse({"response": "not_ok"}, status=405)

@csrf_exempt
def get_user_favorites(request):
    if request.method == 'GET':
        try:
            user_session = authenticate_user(request)
            user = User.objects.get(id=user_session.user.id)
            favorites = UserFavorites.objects.filter(user=user)

            ingredients = Recipes.objects.filter()

            json_response = []

            for recipe in favorites:
                recipe_info = Recipes.objects.get(id=recipe.recipe.id)
                user_liked = True

                ingredients_info = []
                for ingredient in recipe_info.ingredients.all():
                    ingredients_info.append({ 
                        "name": ingredient.name,
                        "kcal": ingredient.kcal
                    })

                json_response.append({
                    "id": recipe_info.id,
                    "image_link": recipe_info.image_link,
                    "name": recipe_info.name,
                    "recipe": recipe_info.recipe,
                    "food_type": recipe_info.food_type,
                    "is_liked": user_liked,
                    "ingredients": ingredients_info,

                })
            return JsonResponse({'favorites': json_response}, safe=False, status=200)
        except PermissionDenied:
            return JsonResponse({'response': 'unauthorized'}, status=401)
        except User.DoesNotExist:
            return JsonResponse({'response': 'not_ok'}, status=404)
    else:
        return JsonResponse({"response": "not_ok"}, status=405)

@csrf_exempt
def user_favorite(request, recipe_id):
    if request.method == 'POST':
        try:
            user_session = authenticate_user(request)
            user = User.objects.get(id=user_session.user.id)
            if UserFavorites.objects.filter(user=user, recipe_id=recipe_id).exists():
                return JsonResponse({'response': 'already_exists'}, status=400)
            recipe = Recipes.objects.get(id=recipe_id)
            new_favorite = UserFavorites.objects.create(user=user, recipe=recipe)
            
            return JsonResponse({'response': 'ok'}, status=201)
        except PermissionDenied:
            return JsonResponse({'response': 'unauthorized'}, status=401)
        except User.DoesNotExist:
            return JsonResponse({'response': 'not_ok'}, status=404)
        except Recipes.DoesNotExist:
            return JsonResponse({'response': 'not_ok'}, status=404)

    elif request.method == 'DELETE':
        try:
            user_session = authenticate_user(request)
            user = User.objects.get(id=user_session.user.id)
            favorite = UserFavorites.objects.get(user=user, recipe_id=recipe_id)
            favorite.delete()
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
        try:
            user_session = authenticate_user(request)
            user = User.objects.get(id=user_session.user.id)
            meal_plans = UserMealPlan.objects.filter(user=user, week_day=day)
            meal_plans_data = {}
            for meal_plan in meal_plans:
                day_name = dict(UserMealPlan.WEEKDAYS)[meal_plan.week_day]
                if day_name not in meal_plans_data:
                    meal_plans_data[day_name] = []
                meal_plans_data[day_name].append(meal_plan.to_json())
            return JsonResponse({'meal_plans': meal_plans_data}, status=200)
        except PermissionDenied:
            return JsonResponse({'response': 'unauthorized'}, status=401)
        except User.DoesNotExist:
            return JsonResponse({'response': 'not_ok'}, status=404)

@csrf_exempt
def user_mealplan(request, day, recipe_id):
    if request.method == 'POST':
        try:
            user_session = authenticate_user(request)
            user = User.objects.get(id=user_session.user.id)
            recipe = Recipes.objects.get(id=recipe_id)
            valid_days = [choice[0] for choice in UserMealPlan.WEEKDAYS]
            if day not in valid_days:
                return JsonResponse({'response': 'not_ok'}, status=400)

            user_mealplans = UserMealPlan.objects.filter(user=user, week_day=day, recipe_id=recipe_id)

            if user_mealplans.exists():
                return JsonResponse({'response': 'not_ok'}, status=400)

            new_mealplan = UserMealPlan.objects.create(user=user, recipe=recipe, week_day=day)
            return JsonResponse({'response': 'ok'}, status=201)
        except PermissionDenied:
            return JsonResponse({'response': 'unauthorized'}, status=401)
        except User.DoesNotExist:
            return JsonResponse({'response': 'not_ok'}, status=404)
        except Recipes.DoesNotExist:
            return JsonResponse({'response': 'not_ok'}, status=404)

    elif request.method == 'DELETE':
        try:
            user_session = authenticate_user(request)
            user = User.objects.get(id=user_session.user.id)
            mealplan = UserMealPlan.objects.get(user=user, recipe_id=recipe_id, week_day=day)
            mealplan.delete()
            return JsonResponse({'response': 'ok'}, status=200)
        except PermissionDenied:
            return JsonResponse({'response': 'unauthorized'}, status=401)
        except UserMealPlan.DoesNotExist:
            return JsonResponse({'response': 'not_ok'}, status=404)
    else:
        return JsonResponse({"response": "not_ok"}, status=405)

@csrf_exempt
def recipes(request):
    if request.method == 'GET':
        try:
            user_session = authenticate_user(request)
        except PermissionDenied:
            return JsonResponse({'error': 'unauthorized'}, status=401)

        food_type = request.GET.get('food_type', None)
        ingredient = request.GET.get('ingredient', None)

        recipes_queryset = Recipes.objects.all()
        ingredients = Recipes.objects.filter()

        if food_type is not None:
            recipes_queryset = Recipes.objects.filter(food_type=food_type)
            print(recipes_queryset)
        if ingredient is not None:
            recipes_queryset = recipes_queryset.filter(ingredients__name=ingredient)

        json_response = []

        for recipe in recipes_queryset:
            try:
                recipe_liked = UserFavorites.objects.get(user=user_session.user, recipe=recipe)
                user_liked = True
            except UserFavorites.DoesNotExist:
                user_liked = False

            ingredients_info = []
            for ingredient in recipe.ingredients.all():
                ingredients_info.append({ 
                    "name": ingredient.name,
                    "kcal": ingredient.kcal
                })

            json_response.append({
                "id": recipe.id,
                "image_link": recipe.image_link,
                "name": recipe.name,
                "recipe": recipe.recipe,
                "food_type": recipe.food_type,
                "is_liked": user_liked,
                "ingredients": ingredients_info,

            })
        return JsonResponse(json_response, safe=False, status=200)
    else:
        return JsonResponse({"response": "not_ok"}, status=405)

@csrf_exempt
def recipe_detail(request, recipe_id):
    if request.method == 'GET':
        try:
            recipe = Recipes.objects.get(id=recipe_id)
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
        try:
            user_session = authenticate_user(request)
        except PermissionDenied:
            return JsonResponse({'error': 'unauthorized'}, status=401)
        
        recipes_queryset = Recipes.objects.filter(ingredients__name=ingredient)
        ingredients = Recipes.objects.filter()

        if ingredient:
            recipes_queryset = recipes_queryset.filter(ingredients__name=ingredient)

        json_response = []

        for recipe in recipes_queryset:
            try:
                recipe_liked = UserFavorites.objects.get(user=user_session.user, recipe=recipe)
                user_liked = True
            except UserFavorites.DoesNotExist:
                user_liked = False

            ingredients_info = []
            for ingredient in recipe.ingredients.all():
                ingredients_info.append({ 
                    "name": ingredient.name,
                    "kcal": ingredient.kcal
                })

            json_response.append({
                "id": recipe.id,
                "image_link": recipe.image_link,
                "name": recipe.name,
                "recipe": recipe.recipe,
                "food_type": recipe.food_type,
                "is_liked": user_liked,
                "ingredients": ingredients_info,

            })
        return JsonResponse(json_response, safe=False, status=200)
    else:
        return JsonResponse({"response": "not_ok"}, status=405)