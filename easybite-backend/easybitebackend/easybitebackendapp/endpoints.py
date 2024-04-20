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
                return JsonResponse({"response": "not_ok", "message": "Campos incompletos"}, status=400)
        
        if client_email is None or '@' not in client_email or len(client_email) < 8:
            return JsonResponse({"response": "not_ok", "message": "Formato de correo electrónico inválido"}, status=400)
        try:
            User.objects.get(email=client_email)
            return JsonResponse({"response": "already_exists"}, status=409)
        except:
            pass

        salted_and_hashed_pass = bcrypt.hashpw(client_password.encode('utf8'), bcrypt.gensalt()).decode('utf8')
        new_user = User(email=client_email, username=client_username, password=salted_and_hashed_pass, birthdate=client_birthdate)
        new_user.save()

        random_token = secrets.token_hex(16)
        session = UserSession(user=new_user, token=random_token)
        session.save()

        return JsonResponse({"response": "ok", "SessionToken": random_token}, status=201)
