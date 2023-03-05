package com.example.opengates;


import static java.lang.Thread.*;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class LocationSettings extends FragmentActivity implements OnMapReadyCallback {
    GoogleMap mMap;
    SearchView searchView;
    LatLng latLng;
    ArrayList<String> sendToMain;
    String className;
    Thread thread;
    LinearLayout radiusLayout;
    String location;
    SharedPreferences sharedPreferences;
    Gson gson;
    String[] jsons;
    int radius = 80;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_location);
        searchView = findViewById(R.id.idSearchView);
        sendToMain = new ArrayList<>();

        Button savePointBtn = findViewById(R.id.savePointBtn);
        Button uncheckPointBtn = findViewById(R.id.uncheckPointBtn);
        TextView seekBarTxtView = findViewById(R.id.seekBarTxtView);
        Button infoLoctionBtn = findViewById(R.id.infoLocationBtn);
        SeekBar radiusSeekBr = findViewById(R.id.radiusSeekBr);
        radiusLayout = findViewById(R.id.radiusLayout);
        className = this.getClass().getSimpleName();
        sharedPreferences = getApplicationContext().getSharedPreferences(Globals.sharedPrefName, Context.MODE_PRIVATE);//Read from shared preferences file.
        int length = 3;
        jsons = new String[length];
        gson = new Gson();
        if (!isNetworkAvailable()) {
            runOnUiThread(() -> Toast.makeText(LocationSettings.this, "You must open network connection",
                    Toast.LENGTH_SHORT).show());
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        infoLoctionBtn.setOnClickListener(view -> runOnUiThread(() -> Toast.makeText(getApplicationContext(), "You can choose a circular area.If you get closer to this area,the app will automatically call to saved phone number that you set.",
                Toast.LENGTH_LONG).show()));


        radiusSeekBr.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser) {
                        if (mMap != null && latLng != null) {
                            try {
                                radius = progress;
                            } catch (NumberFormatException nfe) {
                                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Cannot get location address",
                                        Toast.LENGTH_SHORT).show());
                                return;
                            }
                            if (radius < 1 || radius > 200) {
                                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "The radius should be between 1 to 200 meters!",
                                        Toast.LENGTH_SHORT).show());
                                return;
                            }
                            seekBarTxtView.setText(String.valueOf(radius));
                            mMap.clear();
                            drawCircle(latLng, radius);
                            mMap.addMarker(new MarkerOptions().position(latLng));
                        }

                    }
                }
        );

        uncheckPointBtn.setOnClickListener(view -> {
            if (mMap != null) {
                mMap.clear();
                sendToMain.clear();
                latLng = null;
                searchView.setQuery("", false);
            }
        });
        savePointBtn.setOnClickListener(view -> {
            if (latLng.toString()=="") {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "please choose location ",
                        Toast.LENGTH_SHORT).show());
                return;
            }
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Location saved",
                    Toast.LENGTH_SHORT).show());
            sendToMain.clear();
            sendToMain.add(String.valueOf(latLng.latitude));
            sendToMain.add(String.valueOf(latLng.longitude));
            sendToMain.add(String.valueOf(radius));
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            intent.putStringArrayListExtra(className, sendToMain);
            startActivity(intent);
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        // adding on query listener for our search view.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {//When click on search button.
                // on below line we are getting the
                // location name from search view.
                location = searchView.getQuery().toString();
                latLng = null;
                // below line is to create a list of address
                // where we will store the list of all address.
                if (mMap != null) {
                    mMap.clear();
                    sendToMain.clear();
                }

                // checking if the entered location is null or not.
                if (location != null || location.equals("")) {

                    // on below line we are getting location from the
                    // location name and adding that location to address list.
                    thread = new Thread(() -> {
                        try {
                            latLng = determineLatLngFromAddress(getApplicationContext(), location);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    thread.start();
                    final boolean[] find = {false};
                    try {
                        sleep(100);
                        runOnUiThread(() -> {
                            while (!find[0])
                                if (latLng != null) {
                                    find[0] = true;
                                    // on below line we are adding marker to that position.
                                    drawCircle(latLng, radius);
                                    mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                                    radiusLayout.setVisibility(View.VISIBLE);

                                }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        // at last we calling our map fragment to update.
        mapFragment.getMapAsync(this);
        jsons[0] = sharedPreferences.getString(Globals.MainClassesInProj.LocationSettings.toString(), "");
        if (jsons[0] != null && !jsons[0].equals("")) {
            latLng = new LatLng(Double.parseDouble(gson.fromJson(jsons[0], ArrayList.class).get(0).toString()), Double.parseDouble(gson.fromJson(jsons[0], ArrayList.class).get(1).toString()));
            radius = (int)Double.parseDouble(gson.fromJson(jsons[0], ArrayList.class).get(2).toString());
            radiusSeekBr.setProgress(radius);
            seekBarTxtView.setText(String.valueOf(radius));
        }
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = Objects.requireNonNull(googleMap);

        // Setting a click event handler for the map
        mMap.setOnMapClickListener(latLngVal -> {
            radiusLayout.setVisibility(View.VISIBLE);
            latLng = latLngVal;
            // Creating a marker
            MarkerOptions markerOptions = new MarkerOptions();

            // Setting the position for the marker
            markerOptions.position(latLngVal);

            // Clears the previously touched position
            mMap.clear();

            // Animating to the touched position
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLngVal));

            drawCircle(latLng, radius);

            // Placing a marker on the touched position
            mMap.addMarker(markerOptions);

        });

        if (latLng != null) {
            mMap.addMarker(new MarkerOptions().position(latLng));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
            searchView.setQuery(getCompleteAddressString(latLng.latitude, latLng.longitude), false);
            radiusLayout.setVisibility(View.VISIBLE);
            drawCircle(latLng, radius);
            return;
        }
    }


    private void drawCircle(LatLng point, int rad) {
        // Instantiating CircleOptions to draw a circle around the marker
        CircleOptions circleOptions = new CircleOptions();

        // Specifying the center of the circle
        circleOptions.center(point);

        // Radius of the circle
        circleOptions.radius(rad);//radius is in meters.

        // Border color of the circle
        circleOptions.strokeColor(Color.BLACK);

        // Fill color of the circle
        circleOptions.fillColor(0x30ff0000);//Red color.

        // Border width of the circle
        circleOptions.strokeWidth(2);

        // Adding the circle to the GoogleMap
        mMap.addCircle(circleOptions);

    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strAdd;
    }

    public LatLng determineLatLngFromAddress(Context appContext, String strAddress) {
        LatLng latLng = null;
        Geocoder geocoder = new Geocoder(appContext, Locale.getDefault());
        List<Address> geoResults = null;

        try {
            geoResults = geocoder.getFromLocationName(strAddress, 1);
            while (geoResults.size() == 0) {
                geoResults = geocoder.getFromLocationName(strAddress, 1);

            }
            if (geoResults.size() > 0) {
                Address addr = geoResults.get(0);
                latLng = new LatLng(addr.getLatitude(), addr.getLongitude());
            }
        } catch (Exception ignored) {
        }
        return latLng; //LatLng value of address.
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}





