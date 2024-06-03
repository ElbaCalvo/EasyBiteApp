package com.example.easybite;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

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

public class SearchFragment extends Fragment {
    private Context context;
    private RequestQueue queue;
    private RecyclerView recyclerView;
    private RecipesAdapter adapter;
    private List<RecipesData> recipesList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        setHasOptionsMenu(true);
        context = getContext();

        recyclerView = view.findViewById(R.id.recycler_view_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recipesList = new ArrayList<>();
        adapter = new RecipesAdapter(recipesList, getActivity());
        recyclerView.setAdapter(adapter);

        queue = Volley.newRequestQueue(getContext());

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("ingredient")) {
            String ingredient = bundle.getString("ingredient");
            getRecipesByIngredient(ingredient);
        }
        return view;
    }

    private void getRecipesByIngredient(String ingredient) {
        JsonArrayRequestWithAuthentication request = new JsonArrayRequestWithAuthentication(
                Request.Method.GET,
                "/recipes/ingredient/" + ingredient,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for(int i=0; i<response.length(); i++) {
                            try {
                                JSONObject recipes = response.getJSONObject(i);
                                String imageLink = recipes.getString("image_link");
                                String name = recipes.getString("name");
                                String recipe = recipes.getString("recipe");
                                Boolean isLiked = recipes.getBoolean("is_liked");
                                recipes.put("image_link", imageLink);
                                recipes.put("name", name);
                                recipes.put("recipe", recipe);
                                recipes.put("is_liked", isLiked);
                                RecipesData data = new RecipesData(recipes);
                                recipesList.add(data);
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
                        error.printStackTrace();
                    }
                }, context
        );
        queue.add(request);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}