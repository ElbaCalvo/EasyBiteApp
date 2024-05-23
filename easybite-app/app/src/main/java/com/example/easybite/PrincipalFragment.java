package com.example.easybite;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_principal, container, false);
        queue = Volley.newRequestQueue(getContext());

        RecipesDataList = new ArrayList<>();
        adapter = new RecipesAdapter(RecipesDataList, (Activity) getContext());
        View includedLayout = view.findViewById(R.id.recycler_view_include);
        recyclerView = includedLayout.findViewById(R.id.recycler_view_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        return view;    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        RequestQueue queue = Volley.newRequestQueue(getContext());

        JsonArrayRequest request = new JsonArrayRequest
                (Request.Method.GET,
                        Server.name + "/recipes",
                        null,
                        new Response.Listener<JSONArray>(){
                            @Override
                            public void onResponse(JSONArray response) {
                                List<RecipesData> allRecipesPlaces = new ArrayList<>();

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
                                        System.out.print(response);
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

}