"""
URL configuration for easybitebackend project.

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/4.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from django.urls import path
from easybitebackendapp import endpoints

urlpatterns = [
    path('admin/', admin.site.urls),
    path('user/sessions', endpoints.sessions),
    path('user', endpoints.user),
    path('user/favorites', endpoints.get_user_favorites),
    path('user/favorites/<int:recipe_id>', endpoints.user_favorite),
    path('user/mealplan/<day>', endpoints.get_mealplan),
    path('user/mealplan/<day>/<int:recipe_id>', endpoints.user_mealplan),
    path('recipes', endpoints.recipes),
    path('recipes/<int:recipe_id>', endpoints.recipe_detail),
    path('ingredients', endpoints.ingredients),
    path('recipes/ingredient/<str:ingredient>', endpoints.recipes_by_ingredient),
]
