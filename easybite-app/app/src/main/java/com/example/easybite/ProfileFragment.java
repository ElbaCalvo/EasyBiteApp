package com.example.easybite;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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
    private Button closeSession, deleteAccount;
    private RequestQueue queue;
    private List<RecipesData> recipesDataList;
    private RecipesAdapter adapter;
    private RecyclerView recyclerView;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        usernameLabel = view.findViewById(R.id.username_label);
        emailLabel = view.findViewById(R.id.email_label);
        birthdateLabel = view.findViewById(R.id.birthdate_label);
        closeSession = view.findViewById(R.id.close_button);
        deleteAccount = view.findViewById(R.id.delete_button);

        recyclerView = view.findViewById(R.id.recycler_view_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recipesDataList = new ArrayList<>();
        adapter = new RecipesAdapter(recipesDataList, getActivity());
        recyclerView.setAdapter(adapter);

        context = getContext();

        view.findViewById(R.id.edit_profile).setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EditProfileActivity.class);
            context.startActivity(intent);
        });

        closeSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = context.getSharedPreferences("EASYBITE_APP_PREFS", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.remove("user_id");
                editor.remove("session_id");
                editor.apply();

                Intent intent = new Intent(context, LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("¿Estás seguro de que deseas eliminar tu cuenta?");
                builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        JsonObjectRequestWithAuthentication request = new JsonObjectRequestWithAuthentication(
                                Request.Method.DELETE,
                                "/user",
                                null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Intent intent = new Intent(context, LoginActivity.class);
                                        startActivity(intent);
                                        getActivity().finish();
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(getContext(), "Error al eliminar la cuenta", Toast.LENGTH_SHORT).show();                                    }
                                },context
                        );
                        queue.add(request);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
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
        JsonObjectRequestWithAuthentication request = new JsonObjectRequestWithAuthentication
                (Request.Method.GET,
                        "/user/favorites",
                        null,
                        new Response.Listener<JSONObject>(){
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONArray favorites = response.getJSONArray("favorites");
                                    for(int i=0; i<favorites.length(); i++) {
                                        JSONObject recipes = favorites.getJSONObject(i);
                                        String imageLink = recipes.getString("image_link");
                                        String name = recipes.getString("name");
                                        String recipe = recipes.getString("recipe");
                                        recipes.put("image_link", imageLink);
                                        recipes.put("name", name);
                                        recipes.put("recipe", recipe);
                                        RecipesData data = new RecipesData(recipes);
                                        recipesDataList.add(data);
                                    }
                                    adapter.notifyDataSetChanged();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                adapter.notifyDataSetChanged();
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
}