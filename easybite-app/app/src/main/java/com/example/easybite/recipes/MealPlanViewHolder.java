package com.example.easybite.recipes;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easybite.R;
import com.example.easybite.Util;

import java.util.List;

class MealPlanViewHolder extends RecyclerView.ViewHolder {
    private ImageView imageViewFood;
    private TextView textViewRecipeName;
    private ImageView deleteImageView;
    private List<MealPlanData> dataset;

    public MealPlanViewHolder(@NonNull View itemView, MealPlanAdapter adapter) {
        super(itemView);
        this.imageViewFood = itemView.findViewById(R.id.image_view);
        this.textViewRecipeName = itemView.findViewById(R.id.recipe_name);
        deleteImageView = itemView.findViewById(R.id.delete);
    }

    public void showData(MealPlanData data) {
        Util.downloadBitmapToImageView(data.getImageLink(), imageViewFood);
        textViewRecipeName.setText(data.getRecipeName());
    }

    public ImageView getDeleteImageView() {
        return deleteImageView;
    }
}

