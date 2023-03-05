package com.example.opengates;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.location.LocationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.app.AlertDialog;
import android.os.IBinder;
import android.os.Looper;
import android.widget.TextView;
import java.time.LocalDateTime;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import android.app.Notification.Builder;
import static com.example.opengates.MainActivity.CHANNEL_ID;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;
import java.time.format.DateTimeFormatter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import android.app.NotificationManager;
import android.widget.Toast;
import java.text.ParseException;
import java.util.ArrayList;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.DialogInterface;
import android.view.View.OnClickListener;
public class Service extends android.app.Service {
    SharedPreferences sharedPreferences;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    ArrayList<Object> locationFrmSP, isInTheCircleFrmSP, phoneFrmSP, mainWindowState;
    String[] datesFrmSP;
    boolean goingInToTheCircle = false;
    boolean alreadyCall;
    String className;
    int i = 0,radius;
    ArrayList<String> sendToMain;
    long timeLeftFrmStrt;
    Gson gson;
    String[] jsons;
    LocationCallback locationCallback;
    NotificationManager mNotificationManager;
    NotificationCompat.Builder notification;

    String days[] = {"sunday", "monday", "tuesday", "wednesday",
            "thursday", "friday", "saturday", "sunday"};

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        className = this.getClass().getSimpleName();

        sharedPreferences = getApplicationContext().getSharedPreferences(Globals.sharedPrefName, Context.MODE_PRIVATE);//Read from shared preferences file.
        sendToMain = new ArrayList<String>();
        int length = 3;
        gson = new Gson();
        jsons = new String[length];
        timeLeftFrmStrt = System.currentTimeMillis();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        if (action == "stop" && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;

        }
        if (action == "start") {

            UpdatesCurrentLocation();
            // Get the layouts to use in the custom notification
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0, notificationIntent, 0);
            notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Searching for the gate...\n")
                    .setSmallIcon(R.drawable.icon_barrier)
                    .setContentIntent(pendingIntent);

            startForeground(1, notification.build());//id shouldnt equal to 0!
            jsons[1] = sharedPreferences.getString(Globals.MainClassesInProj.Service.toString(), "");
            if (jsons[1] != null && !jsons[1].equals("")) {
                isInTheCircleFrmSP = gson.fromJson(jsons[1], ArrayList.class);
                if (isInTheCircleFrmSP != null && isInTheCircleFrmSP.size() != 2)
                    alreadyCall = Boolean.valueOf(isInTheCircleFrmSP.get(0).toString());
            }

        }
        return START_NOT_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void UpdatesCurrentLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
//Instantiating the Location request and setting the priority and the interval I need to update the location.
        locationRequest = locationRequest.create();
        locationRequest.setInterval(0);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//instantiating the LocationCallBack
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                if (locationResult != null) {
                    if (locationResult == null) {
                        return;
                    }
                    //Showing the latitude, longitude and accuracy on the home screen.
                    for (Location location : locationResult.getLocations()) {
                        if (location != null)
                        if (CheckIfLiesInTwoTimes()) {
                                CheckIfInSaveLocation(location.getLatitude(), location.getLongitude());
                            }
                      /*  else {
                            notification.setContentTitle("It will be possible to call during the determined time");
                            mNotificationManager.notify(1, notification.build());
                        }*/
                    }
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    public void CheckIfInSaveLocation(double latitude, double longitude) {
        float[] results = new float[1];
        Location locationSaved = new Location("service");
        jsons[0] = sharedPreferences.getString(Globals.MainClassesInProj.LocationSettings.toString(), "");
        if (jsons[0] == null || jsons[0] == "")
            return;
        locationFrmSP = gson.fromJson(jsons[0], ArrayList.class);
        locationSaved.setLatitude((Double) locationFrmSP.get(0));
        locationSaved.setLongitude((Double) locationFrmSP.get(1));
        radius = (int)(Double.parseDouble(locationFrmSP.get(2).toString()));
    Location.distanceBetween(locationSaved.getLatitude(), locationSaved.getLongitude(),
                latitude, longitude, results);

        sendToMain.clear();
        sendToMain.add(Globals.difgMessage);
        sendToMain.add(String.valueOf(results[0]));
        sendToMainAvtivity(sendToMain);

        if (results[0] < radius && alreadyCall) {
            notification.setContentTitle("Can be call when going far from the gate");
            mNotificationManager.notify(1, notification.build());
        }
        if (results[0] < radius && !alreadyCall) {
            callPhone();
            alreadyCall = true;
            sendToMain.add(String.valueOf(alreadyCall));
            sendToMainAvtivity(sendToMain);
            try {
                notification.setContentTitle("Can be call when going far from the gate");
                mNotificationManager.notify(1, notification.build());
                /*Thread.sleep(Globals.waitInSeconds * 1000);*/

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (results[0] > 1.7 * radius /*&& timeLeftFrmStrt >= (Globals.waitInSeconds * 1000)*/) // if go Outside (*2) from the circle and some time left when starting the application,pay attention the alreadycall get its value first when the app closed.
        {
            alreadyCall = false;
            sendToMain.add(String.valueOf(alreadyCall));
            sendToMainAvtivity(sendToMain);
            notification.setContentTitle("Searching for the gate...\n");
            mNotificationManager.notify(1, notification.build());
        }
    }

    public boolean CheckIfLiesInTwoTimes() {//Read data from sp as string(no json);
        String timeFrmJson = sharedPreferences.getString(Globals.MainClassesInProj.Days.toString(), "");
        if (timeFrmJson == null || timeFrmJson == "")
            return true;

        timeFrmJson = timeFrmJson.replace("[", "");
        timeFrmJson = timeFrmJson.replace("]", "");
        datesFrmSP = timeFrmJson.split(",");

        if (datesFrmSP != null && timeFrmJson != "") {
            String from = datesFrmSP[0].substring(12, 19);
            String to = datesFrmSP[1].substring(11, 19);
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

                Date date_from = formatter.parse(from);
                Date date_to = formatter.parse(to);
                LocalDateTime localDateTime = LocalDateTime.now();
                Date nowDate = new Date();
                Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
                nowDate = Date.from(instant);
                for (int i = 2; i < datesFrmSP.length; i++) {
                    if (days[nowDate.getDay()].equals(datesFrmSP[i])) {
                        return true;
                    }
                }
                String sDate1 = formatter.format(nowDate);
                nowDate = formatter.parse(sDate1);

                if (date_from.before(nowDate) && date_to.after(nowDate)) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void callPhone() {
        jsons[2] = sharedPreferences.getString(Globals.MainClassesInProj.PhoneCall.toString(), "");
        if (jsons[2] == "")
            return;
        phoneFrmSP = gson.fromJson(jsons[2], ArrayList.class);
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneFrmSP.get(0).toString()));//example phone number
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PackageManager packageManager = getApplicationContext().getPackageManager();
        List activities = packageManager.queryIntentActivities(callIntent, PackageManager.MATCH_DEFAULT_ONLY);

        for(int j = 0 ; j < activities.size() ; j++)
        {
            if(activities.get(j).toString().toLowerCase().contains("com.android.phone"))
            {
                callIntent.setPackage("com.android.phone");
            }
            else if(activities.get(j).toString().toLowerCase().contains("call"))
            {
                String pack = (activities.get(j).toString().split("[ ]")[1].split("[/]")[0]);
                callIntent.setPackage(pack);
            }
        }
        try {
            startActivity(callIntent);
            sleep(150);

        } catch (Exception e) {
        }
    }

    public void sendToMainAvtivity(ArrayList<String> msg) {
        Intent intent2 = new Intent(Globals.serviceMessages);
        intent2.putStringArrayListExtra(Globals.gitcMessage, msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent2);
    }

    private void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
}




