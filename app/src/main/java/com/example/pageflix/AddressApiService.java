package com.example.pageflix;

import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AddressApiService {
    private static final String API_BASE_URL = "https://data.gov.il/api/3/action/datastore_search";
    private static final String RESOURCE_ID = "9ad3862c-8391-4b2f-84a4-2d4c68625f4b";

    public static void fetchCityNames(AutoCompleteTextView cityAutoComplete, ArrayAdapter<String> cityAdapter) {
        new FetchCityNamesTask(cityAutoComplete, cityAdapter).execute();
    }

    /*
     this function is foing into Gov api in order to fetch the names of the cities ,
     inside this function we are using the "parseCityNames" method in order to get only the name of the city from the current response
 */

    public static List<String> fetchCityNames() {
        String apiUrl = API_BASE_URL + "?resource_id=" + RESOURCE_ID;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");

            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                String response = scanner.next();
                return parseCityNames(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
/*
    This method used in the city fetch above in order to take the right data from the api response
 */
    private static List<String> parseCityNames(String response) {
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
        return cityNames;
    }

    public static class FetchCityNamesTask extends AsyncTask<Void, Void, List<String>> {
        private final AutoCompleteTextView cityAutoComplete;
        private final ArrayAdapter<String> cityAdapter;

        public FetchCityNamesTask(AutoCompleteTextView cityAutoComplete, ArrayAdapter<String> cityAdapter) {
            this.cityAutoComplete = cityAutoComplete;
            this.cityAdapter = cityAdapter;
        }


        @Override
        protected List<String> doInBackground(Void... voids) {
            return fetchCityNames();
        }

        @Override
        protected void onPostExecute(List<String> cities) {
            if (cities != null) {
                // Update the cityAdapter with the fetched city names
                if (cityAdapter != null) {
                    cityAdapter.clear();
                    cityAdapter.addAll(cities);
                    cityAdapter.notifyDataSetChanged();
                }

                // Show the dropdown when the AutoCompleteTextView gains focus
                if (cityAutoComplete != null) {
                    cityAutoComplete.setOnFocusChangeListener((v, hasFocus) -> {
                        if (hasFocus) {
                            cityAutoComplete.showDropDown();
                        }
                    });
                }
            } else {
                // Handle error
                if (cityAutoComplete != null) {
                    Toast.makeText(cityAutoComplete.getContext(), "Failed to fetch city names", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
