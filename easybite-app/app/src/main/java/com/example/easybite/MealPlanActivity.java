package com.example.easybite;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.example.easybite.recipes.MealPlanAdapter;
import com.example.easybite.recipes.MealPlanData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MealPlanActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MealPlanAdapter adapter;
    private List<MealPlanData> mealPlanList;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_plan);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        String weekDay = getIntent().getStringExtra("day");
        if (weekDay == null || weekDay.isEmpty()) {
            Toast.makeText(this, "Día de la semana no especificado", Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, String> dayMapping = new HashMap<>();
        dayMapping.put("MON", "Lunes");
        dayMapping.put("TUE", "Martes");
        dayMapping.put("WED", "Miércoles");
        dayMapping.put("THU", "Jueves");
        dayMapping.put("FRI", "Viernes");
        dayMapping.put("SAT", "Sabado");
        dayMapping.put("SUN", "Domingo");

        final String mapWeekDay = dayMapping.get(weekDay);

        mealPlanList = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view_item);
        adapter = new MealPlanAdapter(mealPlanList, this);
        adapter.setWeekDay(weekDay);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        this.requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequestWithAuthentication request = new JsonObjectRequestWithAuthentication
                (Request.Method.GET,
                        "/user/mealplan/" + weekDay,
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    mealPlanList.clear();
                                    if (response.has("meal_plans")) {
                                        JSONObject mealPlansObject = response.getJSONObject("meal_plans");
                                        if (mealPlansObject.has("Lunes") || mealPlansObject.has("Martes")
                                                || mealPlansObject.has("Miércoles") || mealPlansObject.has("Jueves")
                                                || mealPlansObject.has("Viernes") || mealPlansObject.has("Sabado")
                                                || mealPlansObject.has("Domingo")) {
                                            JSONArray recipesArray = mealPlansObject.getJSONArray(mapWeekDay);
                                            for (int i = 0; i < recipesArray.length(); i++) {
                                                JSONObject recipeObject = recipesArray.getJSONObject(i);
                                                MealPlanData preference = new MealPlanData(recipeObject);
                                                mealPlanList.add(preference);
                                            }
                                        }
                                    }
                                    adapter.notifyDataSetChanged();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse == null) {
                            Toast.makeText(MealPlanActivity.this, "La conexión no se ha establecido", Toast.LENGTH_LONG).show();
                        } else {
                            int serverCode = error.networkResponse.statusCode;
                            Toast.makeText(MealPlanActivity.this, "Estado de respuesta " + serverCode, Toast.LENGTH_LONG).show();
                        }
                        error.printStackTrace();
                    }
                }, this);
        this.requestQueue.add(request);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
