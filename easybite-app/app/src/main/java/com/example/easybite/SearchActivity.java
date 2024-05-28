package com.example.easybite;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SearchActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (savedInstanceState == null) {
            String ingredient = getIntent().getStringExtra("ingredient");
            SearchFragment searchFragment = new SearchFragment();
            Bundle bundle = new Bundle();
            bundle.putString("ingredient", ingredient);
            searchFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.search_fragment_container, searchFragment)
                    .commit();
        }
    }
}
