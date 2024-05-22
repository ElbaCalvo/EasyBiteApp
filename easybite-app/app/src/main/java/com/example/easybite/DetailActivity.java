package com.example.easybite;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        String foodType = getIntent().getStringExtra("FOOD_TYPE");
        TextView textView = findViewById(R.id.food_type_text);
        textView.setText(foodType);
    }
}
