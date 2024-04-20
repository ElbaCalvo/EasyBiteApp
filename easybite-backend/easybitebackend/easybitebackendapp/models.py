from django.db import models

class User(models.Model):
    email = models.EmailField(unique=True, max_length=75, null=False, blank=False)
    username = models.CharField(max_length=100, unique=True, null=False, blank=False)
    password = models.CharField(max_length=100, null=False, blank=False)
    birthdate = models.DateField(null=False, blank=False)

    def __str__(self):
        return self.name

    def to_json(self):
        return {
            "email": self.email,
            "username": self.username,
            "password": self.password,
            "birthdate": self.birthdate,
        }

class Ingredients(models.Model):
    name = models.CharField(max_length=35, null=True, blank=True)
    kcal = models.IntegerField(null=True, blank=True)

    def __str__(self):
        return self.name

    def to_json(self):
        return {
            "name": self.name,
            "kcal": self.kcal
        }

class Recipes(models.Model):
    image_link = models.CharField(max_length=400, null=False, blank=False)
    name = models.CharField(max_length=50, null=False, blank=False)
    recipe = models.CharField(max_length=300, null=False, blank=False)
    ingredients = models.ManyToManyField(Ingredients) # Una receta puede tener múltiples ingredientes y un ingrediente puede estar presente en múltiples recetas.

def to_json(self):
        # Se obtiene una lista de los nombres y kcal de los ingredientes
        ingredients_info = [{"name": ingredient.name, "kcal": ingredient.kcal} for ingredient in self.ingredients.all()]

        return {
            "image_link": self.image_link,
            "name": self.name,
            "recipe": self.recipe,
            "ingredients": ingredients_info # Se usa la lista de nombres y kcal en lugar de los objetos Ingredient
        }
        
class UserSession(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    token = models.CharField(unique=True, max_length=45)

class UserFavorites(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    recipe = models.ForeignKey(Recipes, on_delete=models.CASCADE)

class UserMealPlan(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE)
    recipe = models.ForeignKey(Recipes, on_delete=models.CASCADE)
    WEEKDAYS = [ # Se definen las opciones para los días de la semana.
        ('MON', 'Lunes'),
        ('TUE', 'Martes'),
        ('WED', 'Miércoles'),
        ('THU', 'Jueves'),
        ('FRI', 'Viernes'),
        ('SAT', 'Sabado'),
        ('SUN', 'Domingo'),
    ]
    # El campo 'week_day' permite que el usuario seleccione el día de la semana al que añadirá la receta.
    week_day = models.CharField(choices=WEEKDAYS, null=False, blank=False, max_length=3)