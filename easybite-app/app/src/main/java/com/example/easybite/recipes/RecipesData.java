package com.example.easybite.recipes;

import org.json.JSONException;
import org.json.JSONObject;

public class RecipesData {
    private String recipeId;
    private String imageLink;
    private String recipeName;
    private String recipe;
    private boolean isLiked;

    public RecipesData(JSONObject jsonObject) throws JSONException {
        this.recipeId = jsonObject.getString("id");
        this.imageLink = jsonObject.getString("image_link");
        this.recipeName = jsonObject.getString("name");
        this.recipe = jsonObject.getString("recipe");
        this.isLiked = jsonObject.getBoolean("is_liked");
    }

    public String getRecipeId(){
        return recipeId;
    }
    public String getImageLink() {
        return imageLink;
    }
    public String getRecipeName() { return recipeName; }
    public String getRecipe() {
        return recipe;
    }
    public boolean getIsLiked() {
        return isLiked;
    }
    public void setIsLiked(boolean is_liked){
        this.isLiked = is_liked;
    }

}
