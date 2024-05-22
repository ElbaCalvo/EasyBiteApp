package com.example.easybite.recipes;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easybite.R;

import java.util.List;

public class RecipesAdapter extends RecyclerView.Adapter<RecipesViewHolder> {
    private List<RecipesData> allTheData;

    public RecipesAdapter(List<RecipesData> recipesDataList, Activity activity) {
        this.allTheData = recipesDataList;
    }

    @NonNull
    @Override
    public RecipesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View cellView = inflater.inflate(R.layout.fragment_recyclerview_item, parent, false);
        return new RecipesViewHolder(cellView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipesViewHolder holder, int position) {
        RecipesData dataForThisCell = this.allTheData.get(position);
        holder.showData(dataForThisCell);
    }

    @Override
    public int getItemCount() {
        return this.allTheData.size();
    }
}