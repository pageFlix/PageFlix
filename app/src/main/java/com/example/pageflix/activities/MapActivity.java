package com.example.pageflix.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.pageflix.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

// MapActivity class is used in order to start Google maps from the
// customer location to the library he choose to rent from.
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize MapView
        MapView mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Receive address data from intent extras
        String city = getIntent().getStringExtra("city");
        String street = getIntent().getStringExtra("street");
        String number = getIntent().getStringExtra("number");
        // Start navigation using the received address data
        if (city != null && street != null && number != null) {

            startNavigation(city, street, number);

        }

    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        // Additional map setup code...
    }

    private void startNavigation(String city, String street, String number) {
        // Construct the address string
        String address = city + ", " + street + ", " + number;

        // Encode the address for URI
        try {
            address = URLEncoder.encode(address, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error encoding address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a URI for Google Maps with the destination address
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + address);

        // Create an intent to launch Google Maps
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps"); // Specify package to ensure it opens in Google Maps
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
            finish(); // Close the current MapActivity
        } else {
            Toast.makeText(getApplicationContext(), "Google Maps app not found", Toast.LENGTH_SHORT).show();
        }
    }
}
