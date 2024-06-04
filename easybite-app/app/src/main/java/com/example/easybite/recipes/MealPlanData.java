package com.example.easybite.recipes;

import org.json.JSONException;
import org.json.JSONObject;

public class MealPlanData {
    private String mealplanId;
    private String recipeId;
    private String imageLink;
    private String recipeName;

    public MealPlanData(JSONObject jsonObject) throws JSONException {
        this.mealplanId = jsonObject.getString("mealplan_id");
        this.recipeId = jsonObject.getString("id");
        this.imageLink = jsonObject.getString("image_link");
        this.recipeName = jsonObject.getString("name");
    }

    public String getMealPlanId(){
        return mealplanId;
    }
    public String getRecipeId(){
        return recipeId;
    }
    public String getImageLink() {
        return imageLink;
    }
    public String getRecipeName() { return recipeName; }
}
