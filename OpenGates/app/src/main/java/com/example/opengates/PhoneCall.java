package com.example.opengates;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.gson.Gson;
import java.util.ArrayList;

public class PhoneCall extends AppCompatActivity {

    EditText phoneNo;
    String className;
    ArrayList<String> sendToMain, savedPhoneNumber;
    SharedPreferences sharedPreferences;
    String[] jsons;
    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_phone);
        phoneNo = findViewById(R.id.editTextPhone);
        Button savePhoneNo = findViewById(R.id.savePhonebtn);
        className = this.getClass().getSimpleName();
        sendToMain = new ArrayList<>();
        sharedPreferences = getApplicationContext().getSharedPreferences(Globals.sharedPrefName, Context.MODE_PRIVATE);//Read from shared preferences file.
        Intent intent2 = new Intent();
        intent2.getExtras();
        System.out.println("intent.getExtras() "+intent2.getExtras());
        int length = 3;
        jsons = new String[length];
        gson = new Gson();


        if (ContextCompat.checkSelfPermission(PhoneCall.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            int PERMISSION_CODE = 100;
            ActivityCompat.requestPermissions(PhoneCall.this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CODE);

        }
        savePhoneNo.setOnClickListener(v -> {
            if (phoneNo.getText().toString().isEmpty() || !isValidPhoneNo(phoneNo.getText().toString())) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "You must choose valid phone number!",
                        Toast.LENGTH_LONG).show());
                return;
            }
            sendToMain.add(phoneNo.getText().toString());
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Phone Number " + phoneNo.getText() + " saved",
                    Toast.LENGTH_LONG).show());
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            intent.putStringArrayListExtra(className, sendToMain);
            startActivity(intent);
            finish();
        });

        jsons[0] = sharedPreferences.getString(Globals.MainClassesInProj.PhoneCall.toString(), "");
        if (jsons[0] != null && !jsons[0].equals("")) {
            savedPhoneNumber = gson.fromJson(jsons[0], ArrayList.class);
            phoneNo.setText(savedPhoneNumber.get(0));
        }
    }

    public static boolean isValidPhoneNo(String phoneNo) {
        return !TextUtils.isEmpty(phoneNo) &&
                Patterns.PHONE.matcher(phoneNo).matches() &&( phoneNo.length() <12&&phoneNo.length() >9);
    }
}
