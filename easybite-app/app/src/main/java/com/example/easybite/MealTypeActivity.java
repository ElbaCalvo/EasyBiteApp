package com.example.easybite;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class MealTypeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_type);
        MealTypeFragment fragment = new MealTypeFragment();
        Intent intent = getIntent();
        String mealType = intent.getStringExtra("meal_type");
        Bundle bundle = new Bundle();
        bundle.putString("meal_type", mealType);
        fragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.activity_mealplan_fragment_container, fragment).commit();
    }
}