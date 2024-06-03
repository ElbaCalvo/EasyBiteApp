package com.example.easybite.recipes;

import org.json.JSONException;
import org.json.JSONObject;

public class RecipesData {
    private String recipe_id;
    private String image_link;
    private String recipe_name;
    private String recipe;
    private boolean is_liked;

    public RecipesData(JSONObject jsonObject) throws JSONException {
        this.recipe_id = jsonObject.getString("id");
        this.image_link = jsonObject.getString("image_link");
        this.recipe_name = jsonObject.getString("name");
        this.recipe = jsonObject.getString("recipe");
        this.is_liked = jsonObject.getBoolean("is_liked");
    }

    public String getRecipeId(){
        return recipe_id;
    }
    public String getImage_link() {
        return image_link;
    }
    public String getRecipeName() { return recipe_name; }
    public String getRecipe() {
        return recipe;
    }
    public boolean getIsLiked() {
        return is_liked;
    }
    public void setIsLiked(boolean is_liked){
        this.is_liked = is_liked;
    }

}
