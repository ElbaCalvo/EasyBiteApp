package com.example.easybite;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DetailFragment extends Fragment {

    private RequestQueue requestQueue;
    private String recipeId;
    private TextView recipeName, recipeCalories, recipeIngredients, recipeExplanation;
    private ImageView recipeImage, heartImage, addRecipeImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        setHasOptionsMenu(true);

        recipeName = view.findViewById(R.id.recipe_name);
        recipeIngredients = view.findViewById(R.id.ingredients);
        recipeExplanation = view.findViewById(R.id.recipe);
        recipeImage = view.findViewById(R.id.image);

        Bundle bundle = getArguments();
        this.recipeId = bundle.getString("recipe_id");

        if (bundle != null) {
            this.requestQueue = Volley.newRequestQueue(getContext());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.GET,
                    Server.name + "/recipes/" + this.recipeId,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String image_link = response.getString("image_link");
                                String name = response.getString("name");
                                System.out.println(name);
                                String recipe = response.getString("recipe");
                                JSONArray ingredientsArray = response.getJSONArray("ingredients");

                                StringBuilder ingredientsBuilder = new StringBuilder();
                                for (int i = 0; i < ingredientsArray.length(); i++) {
                                    JSONObject ingredient = ingredientsArray.getJSONObject(i);
                                    String ingredientName = ingredient.getString("name");
                                    int kcal = ingredient.getInt("kcal");
                                    ingredientsBuilder.append(ingredientName).append(" - ").append(kcal).append(" kcal\n");
                                }

                                recipeName.setText(name);
                                recipeIngredients.setText(ingredientsBuilder.toString());
                                recipeExplanation.setText(recipe);

                                try {
                                    Util.downloadBitmapToImageView(image_link, recipeImage);
                                } catch (Exception e) {
                                    recipeImage.setImageResource(R.drawable.ic_launcher_background);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}