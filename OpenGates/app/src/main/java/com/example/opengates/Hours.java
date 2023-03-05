package com.example.opengates;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class Hours extends AppCompatActivity {

    // Initialize variables.
    EditText hourBfrEditTxt;
    EditText hourAftrEditTxt;
    int hourBfr, hourAftr;
    int minBfr, minAftr;
    String className;
    ArrayList<String> chosenHours;
    SharedPreferences sharedPreferences;
    String[] datesFrmSP;
    String timeFrmJson;
    String[] fromSp;
    String[] toSp;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_hours);
        className = this.getClass().getSimpleName();
        chosenHours = new ArrayList<>();
        sharedPreferences = getApplicationContext().getSharedPreferences(Globals.sharedPrefName, Context.MODE_PRIVATE);//Read from shared preferences file.
        timeFrmJson = sharedPreferences.getString(Globals.MainClassesInProj.Days.toString(), "");

        Button chooseDaysBtn = findViewById(R.id.chooseDaysBtn);
        Button infoHoursBtn = findViewById(R.id.infoHoursBtn);
        infoHoursBtn.setOnClickListener(view -> runOnUiThread(() -> Toast.makeText(getApplicationContext(), "You can choose time that the app will call (Optional)",
                Toast.LENGTH_LONG).show()));
        hourBfrEditTxt = findViewById(R.id.bfrEditTextTime);
        hourAftrEditTxt = findViewById(R.id.aftrEditTextTime);

        hourBfrEditTxt.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(Hours.this,
                    (view, hourOfDay, minute) -> {
                            hourBfr = hourOfDay;
                            minBfr = minute;
                        hourBfrEditTxt.setText(ShowTime(hourBfr,minBfr));
                    }, hourBfr, 00, false);
                timePickerDialog.show();

        });
        hourAftrEditTxt.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(Hours.this,
                    (view, hourOfDay, minute) -> {
                        hourAftr = hourOfDay;
                        minAftr = minute;
                        hourAftrEditTxt.setText(ShowTime(hourAftr,minAftr));

                    }, hourBfr, minBfr + 5, false);
            timePickerDialog.show();
        });

        chooseDaysBtn.setOnClickListener(view -> {

            if (hourBfr == 0 && minBfr == 0 && hourAftr == 0 && minAftr == 0) {

                runOnUiThread(() -> Toast.makeText(Hours.this, "",
                        Toast.LENGTH_LONG).show());
                return;
            }
            String dateStart = null;
            String dateStop = null;

            dateStart = hourBfr + ":" + minBfr + ":00";
            dateStop = hourAftr + ":" + minAftr + ":00";
            if (hourBfr == 0) {
                dateStart = "00" + ":" + minBfr + ":00";
            }
            if (minBfr == 0) {
                dateStart = hourBfr + ":" + "00:00";
            }
            if (hourAftr == 0) {
                dateStop = "00" + ":" + minAftr + ":00";
            }
            if (minAftr == 0) {
                dateStop = hourAftr + ":" + "00:00";
            }
            if (hourBfr == 0 && minBfr == 0) {
                dateStart = "00" + ":" + "00" + ":00";
                //dateStop - neef to change it
            }
            if (hourAftr == 0 && minAftr == 0) {
                dateStop = "00" + ":" + "00" + ":00";
            }
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

            Date dStrt = null;
            Date dFnsh = null;
            try {
                dStrt = format.parse(dateStart);
                dFnsh = format.parse(dateStop);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (!CheckIfDiffBtwnTime(dateStart, dateStop)) {
                ShowAlertDialog();
                return;
            }
            assert dStrt != null;
            chosenHours.add(dStrt.toString());
            assert dFnsh != null;
            chosenHours.add(dFnsh.toString());
            ArrayList<String> sendDaysSp = new ArrayList(Arrays.asList(datesFrmSP));
            Intent intent = new Intent(getBaseContext(), Days.class);
            intent.putExtra(className, chosenHours);

            if(sendDaysSp.size()>1) {
                sendDaysSp.remove(0);//remove hour before.
                sendDaysSp.remove(0);//remove hour after.
                intent.putExtra("Saved days", sendDaysSp);
            }
            startActivity(intent);

        });
        if (timeFrmJson != null || timeFrmJson != "") {
            System.out.println("131");
            timeFrmJson = timeFrmJson.replace("[", "");
            timeFrmJson = timeFrmJson.replace("]", "");
            datesFrmSP = timeFrmJson.split(",");

            if (datesFrmSP != null && timeFrmJson != "") {
                System.out.println("137");
                fromSp = datesFrmSP[0].substring(12, 19).split(":");
                toSp = datesFrmSP[1].substring(11, 19).split(":");
                hourBfr= Integer.parseInt(fromSp[0]);
                minBfr= Integer.parseInt(fromSp[1]);
                hourAftr= Integer.parseInt(toSp[0]);
                minAftr= Integer.parseInt(toSp[1]);

                hourBfrEditTxt.setText(ShowTime(hourBfr,minBfr));
                hourAftrEditTxt.setText(ShowTime(hourAftr,minAftr));


            }

        }

    }
private String ShowTime(int hour,int min){
    String hourToShow = String.valueOf(hour), minToShow = String.valueOf(min);
    if (hour == 0)
        hourToShow = hour + "0";
    if (min < 10)
        minToShow = "0" + min;
    return hourToShow+":"+minToShow;
}
    public boolean CheckIfDiffBtwnTime(String dateStart, String dateStop) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

        Date dStrt = null;
        Date dFnsh = null;
        try {
            dStrt = format.parse(dateStart);
            dFnsh = format.parse(dateStop);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        assert dFnsh != null;
        assert dStrt != null;
        long diff = dFnsh.getTime() - dStrt.getTime();

        return diff >= 0;
    }

    public void ShowAlertDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(Hours.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("The hour before should be smaller from the hour after!");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }
}


