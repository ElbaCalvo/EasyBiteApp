package com.example.easybite;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.easybite.recipes.RecipesAdapter;
import com.example.easybite.recipes.RecipesData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PrincipalFragment extends Fragment {
    private RequestQueue queue;
    private RecyclerView recyclerView;
    private RecipesAdapter adapter;
    private List<RecipesData> RecipesDataList;
    private TextView textViewTitle, textViewDescription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_principal, container, false);
        queue = Volley.newRequestQueue(getContext());

        textViewTitle = view.findViewById(R.id.meal_type);
        textViewDescription = view.findViewById(R.id.subtitle);

        RecipesDataList = new ArrayList<>();
        adapter = new RecipesAdapter(RecipesDataList, (Activity) getContext());
        View includedLayout = view.findViewById(R.id.recycler_view_include);
        recyclerView = includedLayout.findViewById(R.id.recycler_view_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        return view;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        ImageView desayunoImageView = view.findViewById(R.id.image_view_desayunos);
        ImageView comidaImageView = view.findViewById(R.id.image_view_comidas);
        ImageView snacksImageView = view.findViewById(R.id.image_view_snacks);
        ImageView postresImageView = view.findViewById(R.id.image_view_postres);

        desayunoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMealTypeActivity("Desayuno");
            }
        });

        comidaImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMealTypeActivity("Comida");
            }
        });

        snacksImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMealTypeActivity("Snack");
            }
        });

        postresImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMealTypeActivity("Postre");
            }
        });


        RequestQueue queue = Volley.newRequestQueue(getContext());

        JsonArrayRequest request = new JsonArrayRequest
                (Request.Method.GET,
                        Server.name + "/recipes",
                        null,
                        new Response.Listener<JSONArray>(){
                            @Override
                            public void onResponse(JSONArray response) {
                                for(int i=0; i<response.length(); i++) {
                                    try {
                                        JSONObject recipes = response.getJSONObject(i);
                                        String image_link = recipes.getString("image_link");
                                        String name = recipes.getString("name");
                                        String recipe = recipes.getString("recipe");
                                        recipes.put("image_link", image_link);
                                        recipes.put("name", name);
                                        recipes.put("recipe", recipe);
                                        RecipesData data = new RecipesData(recipes);
                                        RecipesDataList.add(data);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        error.printStackTrace();
                    }
                });
        queue.add(request);
    }

    private void openMealTypeActivity(String mealType) {
        Intent intent = new Intent(getContext(), MealTypeActivity.class);
        intent.putExtra("meal_type", mealType);
        startActivity(intent);
    }

}