package com.example.easybite;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class DetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_detail, findViewById(R.id.nav_host_fragment_activity_main));

        setContentView(R.layout.activity_detail);
        DetailFragment fragment = new DetailFragment();
        Intent intent = getIntent();
        String itemId = intent.getStringExtra("recipe_id");
        System.out.print("recipe_id");
        Bundle bundle = new Bundle();
        bundle.putString("recipe_id", itemId);
        fragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }
}
