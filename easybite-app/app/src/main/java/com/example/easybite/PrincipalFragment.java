package com.example.easybite;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AutoCompleteTextView;
import android.widget.SimpleCursorAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private Context context;
    private RequestQueue queue;
    private RecyclerView recyclerView;
    private RecipesAdapter adapter;
    private List<RecipesData> RecipesDataList;
    private TextView textViewTitle, textViewDescription;
    private AutoCompleteTextView searchAutoComplete;
    private List<String> ingredientsList;
    private SimpleCursorAdapter suggestionAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_principal, container, false);
        queue = Volley.newRequestQueue(getContext());

        textViewTitle = view.findViewById(R.id.meal_type);
        textViewDescription = view.findViewById(R.id.subtitle);

        RecipesDataList = new ArrayList<>();
        ingredientsList = new ArrayList<>();
        adapter = new RecipesAdapter(RecipesDataList, (Activity) getContext());
        View includedLayout = view.findViewById(R.id.recycler_view_include);
        recyclerView = includedLayout.findViewById(R.id.recycler_view_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        searchAutoComplete = view.findViewById(R.id.search_view);

        String[] from = new String[]{"ingredient"};
        int[] to = new int[]{android.R.id.text1};
        suggestionAdapter = new SimpleCursorAdapter(getContext(),
                android.R.layout.simple_dropdown_item_1line,
                null, from, to, 0);

        searchAutoComplete.setAdapter(suggestionAdapter);
        searchAutoComplete.setThreshold(1);

        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedIngredient = (String) parent.getItemAtPosition(position);
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                intent.putExtra("ingredient", selectedIngredient);
                startActivity(intent);
            }
        });

        loadIngredients();

        return view;
    }

    private void showSuggestions(String query) {
        String[] columns = new String[]{BaseColumns._ID, "ingredient"};
        Object[] temp = new Object[]{0, "default"};

        MatrixCursor cursor = new MatrixCursor(columns);
        for (int i = 0; i < ingredientsList.size(); i++) {
            if (ingredientsList.get(i).toLowerCase().contains(query.toLowerCase())) {
                temp[0] = i;
                temp[1] = ingredientsList.get(i);
                cursor.addRow(temp);
            }
        }
        suggestionAdapter.changeCursor(cursor);
    }

    private void loadIngredients() {
        JsonArrayRequest request = new JsonArrayRequest
                (Request.Method.GET,
                Server.name + "/ingredients",
                null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                for (int i = 0; i < response.length(); i++) {
                                    try {
                                        JSONObject ingredient = response.getJSONObject(i);
                                        String name = ingredient.getString("name");
                                        ingredientsList.add(name);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                                        android.R.layout.simple_dropdown_item_1line, ingredientsList);
                                searchAutoComplete.setAdapter(adapter);
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        queue.add(request);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        context = getContext();
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

        reloadRecipes();
    }

    private void reloadRecipes() {
        RequestQueue queue = Volley.newRequestQueue(getContext());

        JsonArrayRequestWithAuthentication request = new JsonArrayRequestWithAuthentication
                (Request.Method.GET,
                        "/recipes",
                        null,
                        new Response.Listener<JSONArray>(){
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    for(int i=0; i<response.length(); i++) {
                                        JSONObject recipes = response.getJSONObject(i);
                                        String imageLink = recipes.getString("image_link");
                                        String name = recipes.getString("name");
                                        String recipe = recipes.getString("recipe");
                                        String isLiked = recipes.getString("is_liked");
                                        recipes.put("image_link", imageLink);
                                        recipes.put("name", name);
                                        recipes.put("recipe", recipe);
                                        recipes.put("is_liked", isLiked);
                                        RecipesData data = new RecipesData(recipes);
                                        RecipesDataList.add(data);
                                    }
                                    adapter.notifyDataSetChanged();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                ;adapter.notifyDataSetChanged();
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                },context
                );
        queue.add(request);
    }

    private void openMealTypeActivity(String mealType) {
        Intent intent = new Intent(getContext(), MealTypeActivity.class);
        intent.putExtra("meal_type", mealType);
        startActivity(intent);
    }

}