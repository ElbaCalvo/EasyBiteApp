package com.example.easybite;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class WeekDaysFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week_days, container, false);

        Button mondayButton = view.findViewById(R.id.monday_button);
        Button tuesdayButton = view.findViewById(R.id.tuesday_button);
        Button wednesdayButton = view.findViewById(R.id.wednesday_button);
        Button thursdayButton = view.findViewById(R.id.thursday_button);
        Button fridayButton = view.findViewById(R.id.friday_button);
        Button saturdayButton = view.findViewById(R.id.saturday_button);
        Button sundayButton = view.findViewById(R.id.sunday_button);

        mondayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMealPlanActivity("MON");
            }
        });
        tuesdayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMealPlanActivity("TUE");
            }
        });
        wednesdayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMealPlanActivity("WED");
            }
        });
        thursdayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMealPlanActivity("THU");
            }
        });
        fridayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMealPlanActivity("FRI");
            }
        });
        saturdayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMealPlanActivity("SAT");
            }
        });
        sundayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMealPlanActivity("SUN");
            }
        });

        return view;
    }

    private void startMealPlanActivity(String day) {
        Intent intent = new Intent(getActivity(), MealPlanActivity.class);
        intent.putExtra("day", day);
        startActivity(intent);
    }
}