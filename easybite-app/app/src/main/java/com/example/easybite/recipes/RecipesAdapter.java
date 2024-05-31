package com.example.easybite.recipes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.easybite.DetailActivity;
import com.example.easybite.JsonObjectRequestWithAuthentication;
import com.example.easybite.R;
import com.example.easybite.Server;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipesAdapter extends RecyclerView.Adapter<RecipesViewHolder> {
    private List<RecipesData> allTheData;
    public Button addButton;

    public RecipesAdapter(List<RecipesData> recipesDataList, Activity activity) {
        this.allTheData = recipesDataList;
    }

    @NonNull
    @Override
    public RecipesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View cellView = inflater.inflate(R.layout.recycler_item, parent, false);
        return new RecipesViewHolder(cellView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipesViewHolder holder, int position) {
        RecipesData dataForThisCell = this.allTheData.get(position);
        holder.showData(dataForThisCell);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("recipe_id", dataForThisCell.getRecipeId());
                context.startActivity(intent);
            }
        });

        holder.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] weekDay = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sabado", "Domingo"};
                final Map<String, String> dayCodeMap = new HashMap<>();
                dayCodeMap.put("Lunes", "MON");
                dayCodeMap.put("Martes", "TUE");
                dayCodeMap.put("Miércoles", "WED");
                dayCodeMap.put("Jueves", "THU");
                dayCodeMap.put("Viernes", "FRI");
                dayCodeMap.put("Sabado", "SAT");
                dayCodeMap.put("Domingo", "SUN");

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Comeré este plato el...")
                        .setItems(weekDay, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String selectedDay = weekDay[which];
                                String dayCode = dayCodeMap.get(selectedDay);
                                String recipeId = dataForThisCell.getRecipeId();

                                RequestQueue queue = Volley.newRequestQueue(v.getContext());

                                JsonObjectRequestWithAuthentication request = new JsonObjectRequestWithAuthentication(
                                        Request.Method.POST,
                                        "/user/mealplan/" + dayCode + "/" + recipeId,
                                        null,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                try {
                                                    String serverResponse = response.getString("response");
                                                    if (serverResponse.equals("ok")) {
                                                        Toast.makeText(v.getContext(), "Plan de comida agregado con éxito", Toast.LENGTH_SHORT).show();
                                                    } else if (serverResponse.equals("not_ok")) {
                                                        Toast.makeText(v.getContext(), "Hubo un problema al agregar el plan de comida", Toast.LENGTH_SHORT).show();
                                                    } else if (serverResponse.equals("unauthorized")) {
                                                        Toast.makeText(v.getContext(), "No estás autorizado para realizar esta acción", Toast.LENGTH_SHORT).show();
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                if (error.networkResponse == null) {
                                                    Toast.makeText(v.getContext(), "No se pudo establecer la conexión", Toast.LENGTH_LONG).show();
                                                } else {
                                                    int serverCode = error.networkResponse.statusCode;
                                                    Toast.makeText(v.getContext(), "Estado de respuesta: " + serverCode, Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        },v.getContext()
                                );
                                queue.add(request);
                            }
                        });
                builder.create().show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.allTheData.size();
    }
}