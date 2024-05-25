package com.example.easybite.recipes;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easybite.R;
import com.example.easybite.Util;

class RecipesViewHolder extends RecyclerView.ViewHolder {
    private ImageView imageViewFood;
    private TextView textViewRecipeName;
    private TextView textViewRecipe;

    public RecipesViewHolder(@NonNull View itemView) {
        super(itemView);
        this.imageViewFood = itemView.findViewById(R.id.image_view);
        this.textViewRecipeName = itemView.findViewById(R.id.recipe_name);
        this.textViewRecipe = itemView.findViewById(R.id.description);
    }

    public void showData(RecipesData data) {
        Util.downloadBitmapToImageView(data.getImage_link(), imageViewFood);
        textViewRecipeName.setText(data.getRecipeName());
        textViewRecipe.setText(data.getRecipe());
    }
}
