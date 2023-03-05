package com.example.opengates;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements LocationListener {
    public static final String CHANNEL_ID = "exampleServiceChannel";
    Button changeServiceStateBtn;
    List<String> dataFromService = null;
    TextView saveDataTextView;
    String pNumber = "";
    String userLocation = "";
    String dateData = "";
    Button setLocBtn;
    Button setPhnNumBtn;
    Button setTimBtn;
    private boolean gpsEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<String> al = new ArrayList<String>();
        al.add("onCreate");

        writeToSpFile(this.getClass().getSimpleName(), al);
        setContentView(R.layout.activity_main);
        setPhnNumBtn = findViewById(R.id.setPhnNumBtn);
        setTimBtn = findViewById(R.id.setTimBtn);
        setLocBtn = findViewById(R.id.setLocBtn);
        changeServiceStateBtn = findViewById(R.id.changeServiceStateBtn);
        saveDataTextView = findViewById(R.id.saveDataTextView);
        if (!isGooglePlayServicesAvailable(this)) {
            runOnUiThread(() -> Toast.makeText(getBaseContext(), "You should have google play installation in your device",
                    Toast.LENGTH_LONG).show());
            return;
        }
        //Not the best practices to get runtime permissions, but still here I ask permissions.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
        gpsEnable = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        createNotificationChannel();
        // Storing data into SharedPreferences
        Intent intent = getIntent();
        List<String> dataRecieved;
        for (Globals.MainClassesInProj mcip : Globals.MainClassesInProj.values()) {
            dataRecieved = intent.getStringArrayListExtra(mcip.toString());
            if (dataRecieved != null && !dataRecieved.isEmpty()) {
                writeToSpFile(mcip.toString(), dataRecieved);
            }
        }

        // Check if Android M or higher
        // Show alert dialog to the user saying a separate permission is needed
        // Launch the settings activity if the user prefers
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getApplicationContext()))
            ShowAlertDialog();
        setPhnNumBtn.setOnClickListener(view -> {
            if (pNumber != "")
                setPhnNumBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.phone_on, 0, 0);
            Intent PhoneCallIntent = new Intent(getApplicationContext(), PhoneCall.class);
            startActivity(PhoneCallIntent);
        });
        setTimBtn.setOnClickListener(view -> {
            if (dateData != "")
                setTimBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.date_on, 0, 0);
            Intent intent12 = new Intent(getApplicationContext(), Hours.class);
            startActivity(intent12);
        });

        setLocBtn.setOnClickListener(view -> {
            ReadFromSpFile();
            if (!Objects.equals(userLocation, ""))
                setLocBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.location_on, 0, 0);
            Intent intent13 = new Intent(getApplicationContext(), LocationSettings.class);
            startActivity(intent13);
        });

        changeServiceStateBtn.setOnClickListener(view -> {
            if (!gpsEnable){
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "You should turn on GPS",
                        Toast.LENGTH_SHORT).show());
                return;
            }
            ReadFromSpFile();
            if (!isMyServiceRunning(Service.class)) {
                if (pNumber.equals("") || userLocation.equals("")) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "You Should set Phone Number and set Location at least!!  (Set Time is Optional)",
                            Toast.LENGTH_LONG).show());
                    return;
                }
                runOnUiThread(() -> {
                    changeServiceStateBtn.setBackgroundResource(R.drawable.switch_on);
                });

                startService();
            } else {//Stop Server.
                runOnUiThread(() -> {
                    changeServiceStateBtn.setBackgroundResource(R.drawable.switch_off);
                    saveDataTextView.setText("");
                });
                stopService();
            }
        });
        ButtonState();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Example Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ArrayList<String> al = new ArrayList<>();
        al.add("onResume");
        writeToSpFile(this.getClass().getSimpleName(), al);
        ReadFromSpFile();
        ButtonState();
        if (isMyServiceRunning(Service.class)) {
            runOnUiThread(() -> changeServiceStateBtn.setBackgroundResource(R.drawable.switch_on));
        } else {
            runOnUiThread(() -> changeServiceStateBtn.setBackgroundResource(R.drawable.switch_off));

        }
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(messageReceiver, new IntentFilter("my-message"));
    }

    // Handling the received Intents.
    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent.
            dataFromService = intent.getStringArrayListExtra(Globals.gitcMessage); // -1 is going to be used as the default value.
            if (dataFromService != null && !dataFromService.isEmpty()) {

                if (dataFromService.get(0).equals(Globals.difgMessage)) {
                    runOnUiThread(() -> {
                        double dis = Double.parseDouble(dataFromService.get(1));
                        if (dis > 1000)//meters.
                            saveDataTextView.setText("Air distance to the gate: " + String.format("%.2f", dis / 1000) + " Km");
                        else
                            saveDataTextView.setText("Air distance to the gate: " + String.format("%.2f", dis) + " Meters");
                    });
                }
            }
        }

    };

    @Override
    protected void onDestroy() {
        stopService();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(messageReceiver);    //unregisterReceiver            return;
        finish();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        ArrayList<String> al = new ArrayList<String>();
        if (dataFromService != null && (dataFromService.get(dataFromService.size() - 1) == "true" || dataFromService.get(dataFromService.size() - 1) == "false")) {
            al.add(dataFromService.get(2));
            writeToSpFile(Globals.MainClassesInProj.Service.toString(), al);// example [Distance from the gate, 0.15779087, true, Call Phone]
        }
        al.clear();
        al.add("onPause");
        writeToSpFile(this.getClass().getSimpleName(), al);
    }

    public void startService() {
        Intent serviceIntent = new Intent(getApplicationContext(), Service.class);
        serviceIntent.setAction("start");
        serviceIntent.putExtra("inputExtra", "lattitude");
        ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, Service.class);
        serviceIntent.setAction("stop");
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void writeToSpFile(String cameFrom, List<String> data) {

// Creating an Editor object to edit(write to the file).
        SharedPreferences.Editor myEdit = getSharedPreferences(Globals.sharedPrefName, MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = null;
        try {
            json = gson.toJson(data); // convert to json
        } catch (Exception ignored) {

        }
        assert json != null;
        String replace = json.replace("\"", "");
        myEdit.putString(cameFrom, replace);
        myEdit.apply();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    public void ReadFromSpFile() {
        Object obj = new Object();
        String[] json = new String[1];
        int i = 0;
        for (Globals.MainClassesInProj mcip : Globals.MainClassesInProj.values()) {
            json[i] = this.getSharedPreferences(Globals.sharedPrefName, MODE_PRIVATE).getString(mcip.toString(), "");

            if (json[i] == "" && !mcip.equals(Globals.MainClassesInProj.Service) && obj != null)
                continue;
            json[i] = json[i].replace("[", "");
            json[i] = json[i].replace("]", "");
            if (mcip.equals(Globals.MainClassesInProj.LocationSettings)) {
                userLocation = json[i];
                continue;
            }
            if (mcip.equals(Globals.MainClassesInProj.PhoneCall)) {
                pNumber = json[i];
                continue;
            }
            if (mcip.equals(Globals.MainClassesInProj.Days)) {
                dateData = json[i];
            }
        }
    }

    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void ButtonState() {
        if (!Objects.equals(userLocation, ""))
            setLocBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.location_on, 0, 0);
        if (Objects.equals(userLocation, ""))
            setLocBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.location, 0, 0);
        if (!Objects.equals(pNumber, ""))
            setPhnNumBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.phone_on, 0, 0);
        if (Objects.equals(pNumber, ""))
            setPhnNumBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.phone, 0, 0);
        if (!Objects.equals(dateData, "")) {
            setTimBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.date_on, 0, 0);
        }
        if (Objects.equals(dateData, ""))
            setTimBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.date, 0, 0);
    }

    public void ShowAlertDialog() {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setMessage("You should allow the app to display above other apps").setTitle("Alert")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Show alert dialog to the user saying a separate permission is needed
                    // Launch the settings activity if the user prefers
                    Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    startActivity(myIntent);
                });
        ab.show();
    }

    @Override
    public void onProviderEnabled(String s) {
        if (LocationManager.GPS_PROVIDER.equals(s)) {
            runOnUiThread(() -> {
                runOnUiThread(() -> {
                    saveDataTextView.setText("");
                });
                gpsEnable = true;
            });
        }
    }

    @Override
    public void onProviderDisabled(String s) {
        if (LocationManager.GPS_PROVIDER.equals(s)) {
            if (isMyServiceRunning(Service.class))
                stopService();
            runOnUiThread(() -> {
                changeServiceStateBtn.setBackgroundResource(R.drawable.switch_off);
                saveDataTextView.setText("Turn on GPS");
            });
            gpsEnable = false;
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }
}