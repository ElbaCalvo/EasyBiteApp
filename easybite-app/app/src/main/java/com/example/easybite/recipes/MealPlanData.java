package com.example.easybite.recipes;

import org.json.JSONException;
import org.json.JSONObject;

public class MealPlanData {
    private String mealplan_id;
    private String recipe_id;
    private String image_link;
    private String recipe_name;

    public MealPlanData(JSONObject jsonObject) throws JSONException {
        this.mealplan_id = jsonObject.getString("mealplan_id");
        this.recipe_id = jsonObject.getString("id");
        this.image_link = jsonObject.getString("image_link");
        this.recipe_name = jsonObject.getString("name");
    }

    public String getMealPlanId(){
        return mealplan_id;
    }
    public String getRecipeId(){
        return recipe_id;
    }
    public String getImage_link() {
        return image_link;
    }
    public String getRecipeName() { return recipe_name; }
}
