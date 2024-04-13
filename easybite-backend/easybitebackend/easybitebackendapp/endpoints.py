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
    # Extract the token from the request headers
    if session_token is None:
        raise PermissionDenied('Unauthorized')
    # Authenticate user based on the provided token
    try:
        user_session = UserSession.objects.get(token=session_token)
    except UserSession.DoesNotExist:
        raise PermissionDenied('Unauthorized')
    return user_session

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
            return JsonResponse({"response": "User not in database"}, status=404)  # No existe el usuario
        if bcrypt.checkpw(json_password.encode('utf8'), db_user.password.encode('utf8')):
            # json_password y db_user.encrypted_password coinciden
            random_token = secrets.token_hex(10)
            session = UserSession(user=db_user, token=random_token)
            session.save()
            return JsonResponse({"response": "ok", "SessionToken": random_token}, status=201)
        else:
            # No coinciden
            return JsonResponse({"response": "Unauthorized"}, status=401)
    else:
        return JsonResponse({"response": "HTTP method unsupported"}, status=405)
