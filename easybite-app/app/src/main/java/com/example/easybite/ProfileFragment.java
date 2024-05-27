package com.example.easybite;

import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;

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


public class ProfileFragment extends Fragment {
    private TextView usernameLabel, emailLabel, birthdateLabel;
    private RequestQueue queue;
    private List<RecipesData> recipesDataList;
    private RecipesAdapter adapter;
    private RecyclerView recyclerView;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Inicializar vistas
        usernameLabel = view.findViewById(R.id.username_label);
        emailLabel = view.findViewById(R.id.email_label);
        birthdateLabel = view.findViewById(R.id.birthdate_label);

        recyclerView = view.findViewById(R.id.recycler_view_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recipesDataList = new ArrayList<>();
        adapter = new RecipesAdapter(recipesDataList, getActivity());
        recyclerView.setAdapter(adapter);

        context = getContext();

        view.findViewById(R.id.edit_profile).setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetailActivity.class);
            context.startActivity(intent);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        context = getContext();
        sendProfileRequest();
        sentRecipesRequest();
    }

    private void sendProfileRequest() {
        JsonObjectRequestWithAuthentication request = new JsonObjectRequestWithAuthentication(
                Request.Method.GET,
                "/user",
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            System.out.println(response);
                            usernameLabel.setText(response.getString("username"));
                            emailLabel.setText(response.getString("email"));
                            birthdateLabel.setText(response.getString("birthdate"));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse == null) {
                            Toast.makeText(context, "No se pudo establecer la conexión", Toast.LENGTH_LONG).show();
                        } else {
                            int serverCode = error.networkResponse.statusCode;
                            Toast.makeText(context, "Estado de respuesta: " + serverCode, Toast.LENGTH_LONG).show();
                        }
                    }
                },
                context
        );
        queue.add(request);
    }

    private void sentRecipesRequest(){
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
                                        recipesDataList.add(data);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        queue.add(request);
    }
}