from django.db import models

class User(models.Model):
    email = models.EmailField(unique=True, max_length=75)
    username = models.CharField(max_length=100)
    password = models.CharField(max_length=100)
    birthdate = models.DateField()
    mealplan = models.CharField(max_length=100)

    def __str__(self):
        return self.name

    def to_json(self):
        return {
            "email": self.email,
            "name": self.name,
            "password": self.password,
            "birthdate": self.birthdate,
            "mealplan": self.mealplan
        }

class Ingredients(models.Model):
    name = models.CharField(max_length=30)
    kcal = models.IntegerField()

    def __str__(self):
        return self.name

    def to_json(self):
        return {
            "name": self.name,
            "kcal": self.kcal
        }
        
class Recipes(models.Model):
    image_link = models.CharField(max_length=200)
    name = models.CharField(max_length=30)
    recipe = models.CharField(max_length=150)
    ingredients = models.ManyToManyField(Ingredients)

    def __str__(self):
        return self.name

    def to_json(self):
        return {
            "image_link": self.image_link,
            "name": self.name,
            "recipe": self.recipe,
            "ingredients": self.ingredients
        }
        
class UserSession(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    token = models.CharField(unique=True, max_length=45)

class UserFavorites(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    recipe = models.ForeignKey(Recipes, on_delete=models.CASCADE)

    def __str__(self):
        return self.user

    def to_json(self):
        return {
            "user": self.user,
            "recipe": self.recipe
        }