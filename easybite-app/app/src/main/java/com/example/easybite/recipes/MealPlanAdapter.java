package com.example.easybite.recipes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.easybite.DetailActivity;
import com.example.easybite.JsonObjectRequestWithAuthentication;
import com.example.easybite.R;

import org.json.JSONObject;

import java.util.List;

public class MealPlanAdapter extends RecyclerView.Adapter<MealPlanViewHolder> {
    private List<MealPlanData> allTheData;
    private Activity activity;
    String weekDay;

    public MealPlanAdapter(List<MealPlanData> mealPlanDataList, Activity activity) {
        this.allTheData = mealPlanDataList;
        this.activity = activity;
    }

    public void setWeekDay(String weekDay) {
        this.weekDay = weekDay;
    }

    @NonNull
    @Override
    public MealPlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View cellView = inflater.inflate(R.layout.recycler_mealplan_item, parent, false);
        return new MealPlanViewHolder(cellView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull MealPlanViewHolder holder, int position) {
        MealPlanData dataForThisCell = this.allTheData.get(position);
        holder.showData(dataForThisCell);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("recipe_id", dataForThisCell.getMealPlanId());
                context.startActivity(intent);
            }
        });

        holder.getDeleteImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                showDeleteConfirmationDialog(holder.getAdapterPosition(), dataForThisCell.getMealPlanId(), v.getContext());
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.allTheData.size();
    }

    private void showDeleteConfirmationDialog(int position, String mealPlanId, Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar receta")
                .setMessage("¿Estás seguro de que quieres eliminar esta receta de tu planificación semanal?")
                .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        RequestQueue queue = Volley.newRequestQueue(activity);

                        JsonObjectRequestWithAuthentication deleteRequest = new JsonObjectRequestWithAuthentication(
                                Request.Method.DELETE,
                                "/user/mealplan/" + weekDay + "/" + mealPlanId,
                                null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        allTheData.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, allTheData.size());
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        error.printStackTrace();
                                    }
                                },context
                        );
                        queue.add(deleteRequest);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
