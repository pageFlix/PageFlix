package com.example.pageflix.services;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AddressApiService {
    private static final String API_BASE_URL = "https://data.gov.il/api/3/action/datastore_search";
    private static final String StreetRESOURCE_ID = "9ad3862c-8391-4b2f-84a4-2d4c68625f4b";
    private static final String CityRESOURCE_ID = "5c78e9fa-c2e2-4771-93ff-7f400a12f7ba";


    /*
    fetching cities names of all the cities in israel, this function is using gov api for cities in israel.
     */
    public static List<String> fetchCityNames() {
        try {
            String apiUrl = API_BASE_URL + "?resource_id=" + CityRESOURCE_ID;
            URL url = new URL(apiUrl);
            Log.d("fetchCityNames", apiUrl);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");

            InputStream in = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
                Log.d("fetchCityNames", "City name fetched: " + line);
            }

            return parseCityNames(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /*
    The parseCityNames function is used to extract the cities names from the json that returned from the api
     */
    private static List<String> parseCityNames(String response) {
        Log.d("API Response", response); // Log the full JSON response
        List<String> cityNames = new ArrayList<>();
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray records = jsonResponse.getJSONObject("result").getJSONArray("records");

            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.getJSONObject(i);
                String cityName = record.getString("שם_ישוב");

                if (!cityNames.contains(cityName)) {
                    cityNames.add(cityName);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sortCityNames(cityNames);
    }
/*
fetching streets name of the chosen city, this function is using gov api for streets in israel.
 */
    public static List<String> fetchStreetsInCity(String cityName) {
        try {
            Log.d("fetchStreetsInCity", "city name: " + cityName);

            // Construct the API URL with resource_id and JSON-formatted filters
            String filterJson = "{\"שם_ישוב\":\"" + cityName + "\"}";
            String apiUrl = API_BASE_URL + "?resource_id=" + StreetRESOURCE_ID
                    + "&filters=" + URLEncoder.encode(filterJson, "UTF-8");

            URL url = new URL(apiUrl);
            Log.d("fetchStreetsInCity", "URL url: " + apiUrl);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
                Log.d("fetchStreetsInCity", "Street fetched: " + line);
            }

            return parseStreets(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /*
       The parseCityNames function is used to extract the Streets name from the json that returned from the api
        */
    private static List<String> parseStreets(String response) {
        Log.d("API Response (Streets)", response); // Log the full JSON response
        List<String> streets = new ArrayList<>();
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray records = jsonResponse.getJSONObject("result").getJSONArray("records");

            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.getJSONObject(i);
                String streetName = record.getString("שם_רחוב");

                if (!streets.contains(streetName)) {
                    streets.add(streetName);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sortStreetNames(streets);
    }


    private static List<String> sortStreetNames(List<String> streetNames) {
        Collections.sort(streetNames, new HebrewComparator());
        return streetNames;
    }

    private static List<String> sortCityNames(List<String> cityNames) {
        Collections.sort(cityNames, new HebrewComparator());
        return cityNames;
    }

    private static class HebrewComparator implements Comparator<String> {
        private Collator collator;

        public HebrewComparator() {
            this.collator = Collator.getInstance(new java.util.Locale("he", "IL"));
        }

        @Override
        public int compare(String s1, String s2) {
            return collator.compare(s1, s2);
        }
    }
}
