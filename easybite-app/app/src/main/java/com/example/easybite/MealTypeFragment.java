package com.example.easybite;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.easybite.recipes.RecipesAdapter;
import com.example.easybite.recipes.RecipesData;
import com.example.easybite.recipes.RecipesMealTypeAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MealTypeFragment extends Fragment {
    private RequestQueue requestQueue;
    private String recipeId, recipeMealType;
    private RecipesMealTypeAdapter adapter;
    private List<RecipesData> RecipesDataList;
    private RecyclerView recyclerView;
    private TextView recipeName, recipeExplanation, titleTextView, descriptionTextView ;
    private ImageView recipeImage, heartImage, addRecipeImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meal_type, container, false);
        requestQueue = Volley.newRequestQueue(getContext());
        titleTextView = view.findViewById(R.id.meal_type);
        descriptionTextView  = view.findViewById(R.id.subtitle);
        RecipesDataList = new ArrayList<>();
        adapter = new RecipesMealTypeAdapter(RecipesDataList, (Activity) getContext());
        recyclerView = view.findViewById(R.id.recycler_view_include);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        Bundle bundle = getArguments();
        this.recipeId = bundle.getString("recipe_id");
        this.recipeMealType = bundle.getString("meal_type");
        String receivedTitle  = bundle.getString("title");
        String receivedDescription  = bundle.getString("description");

        switch (recipeMealType) {
            case "Desayuno":
                titleTextView.setText("Desayunos");
                descriptionTextView.setText("Empieza tu día con energía con estas recetas de desayuno.");
                break;
            case "Comida":
                titleTextView.setText("Comidas");
                descriptionTextView.setText("Deliciosas comidas para mantenerte activo durante el día.");
                break;
            case "Snack":
                titleTextView.setText("Snacks");
                descriptionTextView.setText("Pequeños bocados para cualquier momento del día.");
                break;
            case "Postre":
                titleTextView.setText("Postres");
                descriptionTextView.setText("Encuentra inspiración en los postres más deliciosos!");
                break;
            default:
                titleTextView.setText(receivedTitle != null ? receivedTitle : "Recetas");
                descriptionTextView.setText(receivedDescription != null ? receivedDescription : "Disfruta de nuestras recetas.");
                break;
        }

        if (bundle != null) {
            this.requestQueue = Volley.newRequestQueue(getContext());
            JsonArrayRequest request = new JsonArrayRequest(
                    Request.Method.GET,
                    Server.name + "/recipes?food_type=" + this.recipeMealType,
                    null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            for (int i = 0; i < response.length(); i++) {
                                System.out.println(response);
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
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse == null) {
                                Toast.makeText(getContext(), "La conexión no se ha establecido", Toast.LENGTH_LONG).show();
                            } else {
                                int serverCode = error.networkResponse.statusCode;
                                Toast.makeText(getContext(), "Estado de respuesta " + serverCode, Toast.LENGTH_LONG).show();
                            }
                            error.printStackTrace();
                        }
                    }
            );
            this.requestQueue.add(request);
        }
        return view;
    }
}