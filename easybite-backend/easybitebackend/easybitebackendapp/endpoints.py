import json
import secrets
import bcrypt
import datetime
from django.core.exceptions import PermissionDenied
from django.db.models import F, ExpressionWrapper, FloatField
from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.utils import timezone
from .models import User, UserSession, Ingredients, Recipes, UserFavorites

def authenticate_user(request):
    session_token = request.headers.get('SessionToken', None)
    if session_token is None:
        raise PermissionDenied('Unauthorized')
    try:
        user_session = UserSession.objects.get(token=session_token)
    except UserSession.DoesNotExist:
        raise PermissionDenied('Unauthorized')
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
            return JsonResponse({"response": "User not in database"}, status=404)
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
            # No coinciden
            return JsonResponse({"response": "unauthorized"}, status=401)
    else:
        return JsonResponse({"response": "HTTP method unsupported"}, status=405)

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
            return JsonResponse({"response": "invalid_request_format"}, status=400)

        if not client_username or not client_email or not client_password or not client_birthdate:
                return JsonResponse({"response": "missing_required_fields"}, status=400)
        
        if not (check_email_format(client_email) and check_password_format(client_password) and check_birthdate_format(client_birthdate)):
            return JsonResponse({"response": "invalid_email_password_or_birthdate_format"}, status=400)
        try:
            existing_user = User.objects.get(email=client_email)
            if existing_user.email == client_email:
                return JsonResponse({"response": "email_already_exist"}, status=409)
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
                return JsonResponse({"response": "missing_required_fields"}, status=400)
        except KeyError:
            return JsonResponse({"response": "invalid_request_format"}, status=400)
        if not (check_email_format(email) and check_password_format(password) and check_birthdate_format(birthdate)):
            return JsonResponse({"response": "invalid_email_password_or_birthdate_format"}, status=400)
        try:
            existing_user = User.objects.get(email=email)
            if existing_user.email == email:
                return JsonResponse({"response": "email_already_exist"}, status=409)
        except User.DoesNotExist:
            pass

        salted_and_hashed_pass = bcrypt.hashpw(password.encode('utf8'), bcrypt.gensalt()).decode('utf8')
        user = User.objects.get(id=user_session.user.id)
        user.email = email
        user.username = username
        user.password = salted_and_hashed_pass
        user.birthdate = birthdate
        user.save()
        return JsonResponse({"response": "user_updated_successfully"}, status=200)

    elif request.method == 'DELETE':
        try:
            try:
                user_session = authenticate_user(request)
            except PermissionDenied:
                return JsonResponse({'response': 'unauthorized'}, status=401)
            user = User.objects.get(id=user_session.user.id)
            user.delete()
            return JsonResponse({'response': 'user_deleted_successfully'}, status=200)
        except User.DoesNotExist:
            return JsonResponse({'response': 'user_not_found'}, status=404)
    else:
        return JsonResponse({"response": "method_not_allowed"}, status=405)
