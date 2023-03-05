package com.example.opengates;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;

public class Days extends FragmentActivity {
    final ArrayList<String> chosenDays = new ArrayList<>();
    final String className = this.getClass().getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_days);
        Intent intent = getIntent();

        ArrayList<String> hours = (ArrayList<String>) intent.getExtras().get(Globals.MainClassesInProj.Hours.toString());
        ArrayList<String> days = (ArrayList<String>) intent.getExtras().get("Saved days");
        chosenDays.addAll(hours);

        CheckBox sundayCheckBox = findViewById(R.id.sundayCheckBox);
        CheckBox mondayCheckBox = findViewById(R.id.mondayCheckBox);
        CheckBox tuesdayCheckBox = findViewById(R.id.tuesdayCheckBox);
        CheckBox wednesdayCheckBox = findViewById(R.id.wednesdayCheckBox);
        CheckBox thursdayCheckBox = findViewById(R.id.thursdayCheckBox);
        CheckBox fridayCheckBox = findViewById(R.id.fridayCheckBox);
        CheckBox saturdayCheckBox = findViewById(R.id.saturdayCheckBox);
        Button saveDaysBtn = findViewById(R.id.saveDaysBtn);

        ///Listeners on Check Boxes Buttons.
        sundayCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                chosenDays.add(Globals.DaysInWeek.sunday.toString());
            } else
                chosenDays.remove(Globals.DaysInWeek.sunday.toString());
        });
        mondayCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                chosenDays.add(Globals.DaysInWeek.monday.toString());
            } else
                chosenDays.remove(Globals.DaysInWeek.monday.toString());
        });
        tuesdayCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                chosenDays.add(Globals.DaysInWeek.tuesday.toString());
            } else
                chosenDays.remove(Globals.DaysInWeek.tuesday.toString());
        });
        wednesdayCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                chosenDays.add(Globals.DaysInWeek.wednesday.toString());
            } else
                chosenDays.remove(Globals.DaysInWeek.wednesday.toString());
        });
        thursdayCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                chosenDays.add(Globals.DaysInWeek.thursday.toString());
            } else
                chosenDays.remove(Globals.DaysInWeek.thursday.toString());
        });
        fridayCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                chosenDays.add(Globals.DaysInWeek.friday.toString());
            } else
                chosenDays.remove(Globals.DaysInWeek.friday.toString());
        });
        saturdayCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                chosenDays.add(Globals.DaysInWeek.saturday.toString());
            } else
                chosenDays.remove(Globals.DaysInWeek.saturday.toString());
        });
        if (days!=null)
            if (!days.isEmpty()) {//Set Checked saved days(From Hours Class).
                for (int i = 0; i < days.size(); i++) {
                    switch (days.get(i)) {
                        case "sunday":
                            sundayCheckBox.setChecked(true);
                            break;
                        case "monday":
                            mondayCheckBox.setChecked(true);
                            break;
                        case "tuesday":
                            tuesdayCheckBox.setChecked(true);
                            break;
                        case "wednesday":
                            wednesdayCheckBox.setChecked(true);
                            break;
                        case "thursday":
                            thursdayCheckBox.setChecked(true);
                            break;
                        case "friday":
                            fridayCheckBox.setChecked(true);
                            break;
                        case "saturday":
                            saturdayCheckBox.setChecked(true);
                            break;
                    }
                }
            }
        saveDaysBtn.setOnClickListener(view -> {
            Intent intent1 = new Intent(getBaseContext(), MainActivity.class);
            if (chosenDays.toArray().length == 2) {//Just choose hours.
                runOnUiThread(() -> Toast.makeText(Days.this, "You must Choose at least one day that the app will work",
                        Toast.LENGTH_LONG).show());

                return;
            }
            runOnUiThread(() -> Toast.makeText(Days.this, "Time saved",
                    Toast.LENGTH_LONG).show());
            intent1.putStringArrayListExtra(className, chosenDays);
            startActivity(intent1);
            finish();
        });
    }
}