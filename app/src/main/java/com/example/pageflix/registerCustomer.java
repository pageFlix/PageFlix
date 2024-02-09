package com.example.pageflix;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

public class registerCustomer extends AppCompatActivity {
    private static final String CITY_RESOURCE_ID = "5c78e9fa-c2e2-4771-93ff-7f400a12f7ba";

    private EditText edEmail, edPassword, edCellphoneNumber, edNumber, edFirstname , edLastname;
    private AutoCompleteTextView cityAutoComplete, streetAutoComplete;
    private ArrayAdapter<String> cityAdapter, streetAdapter;

    private DatabaseReference dbRef;
    private FirebaseAuth fbAuth;
    private String USER_KEY = "Customer"; // DataBase name for Librarians

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_customer);
        init();

        // Set up the adapter
        cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        streetAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);

        // Set up the initial adapters for the first level (city)
        cityAutoComplete.setAdapter(cityAdapter);
        streetAutoComplete.setAdapter(streetAdapter);

        // Set up the text change listeners for search functionality
        setupSearchFunctionality(cityAutoComplete, cityAdapter);
        setupSearchFunctionality(streetAutoComplete, streetAdapter);

        // Show the dropdown when the AutoCompleteTextView gains focus
        setAutoCompleteFocusChangeListener(cityAutoComplete);
        // Fetch city names from the API
        new FetchCityNamesTask().execute();

        Log.d("ActivityLifecycle", "registerLibrarian activity created");
    }



    private void setAutoCompleteFocusChangeListener(AutoCompleteTextView autoCompleteTextView) {
        autoCompleteTextView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                autoCompleteTextView.showDropDown();
            }
        });
    }

    private void setupSearchFunctionality(final AutoCompleteTextView autoCompleteTextView, final ArrayAdapter<String> adapter) {
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Filter the adapter based on the entered text
                if (adapter != null) {
                    adapter.getFilter().filter(editable.toString());
                }
            }
        });
    }

    private void init() {
        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassword);
        fbAuth = FirebaseAuth.getInstance();
        edFirstname = findViewById(R.id.edFirstname);
        edLastname = findViewById(R.id.edLastname);
        edCellphoneNumber = findViewById(R.id.edCellphoneNumber);
        edNumber = findViewById(R.id.edNumber);
        cityAutoComplete = findViewById(R.id.cityAutoComplete);
        streetAutoComplete = findViewById(R.id.streetAutoComplete);
    }

    public void signupButton(View v) {
        Log.d("SignUpButton", "Button clicked");
        String email = edEmail.getText().toString();
        String password = edPassword.getText().toString();
        String LastName = edLastname.getText().toString();
        String FirstName = edFirstname.getText().toString();
        String CellNumber = edCellphoneNumber.getText().toString();
        String City = cityAutoComplete.getText().toString();
        String Street = streetAutoComplete.getText().toString();
        String Number = edNumber.getText().toString();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(LastName) && !TextUtils.isEmpty(FirstName) &&
        !TextUtils.isEmpty(CellNumber) && !TextUtils.isEmpty(City)&& !TextUtils.isEmpty(Street) && !TextUtils.isEmpty(Number) ) {
            fbAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        createUserInDatabase(email, FirstName, LastName , CellNumber, City, Street, Number);
                        Intent intent = new Intent(getApplicationContext(), mainCustomer.class);
                        startActivity(intent);
                        Toast.makeText(getApplicationContext(), "User Sign Up Successful!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "User Sign Up failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Write name and Email", Toast.LENGTH_SHORT).show();
        }
    }

    private void createUserInDatabase(String email, String FisrtName, String LastName, String CellNumber, String City, String street, String Number) {
        dbRef = FirebaseDatabase.getInstance().getReference().child(USER_KEY);
        String userId = fbAuth.getCurrentUser().getUid();
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("email", email);
        userMap.put("FistName", FisrtName);
        userMap.put("LastName", LastName);
        userMap.put("City", City);
        userMap.put("Street", street);
        userMap.put("Number", Number);
        userMap.put("CellNumber", CellNumber);

        dbRef.child(userId).setValue(userMap);
    }

    private class FetchCityNamesTask extends AsyncTask<Void, Void, List<String>> {
        @Override
        protected List<String> doInBackground(Void... voids) {

            Log.d("FetchCityNamesTask", "Fetching city names from API...");
            return AddressApiService.fetchCityNames();
        }

        @Override
        protected void onPostExecute(List<String> cities) {
            if (cities != null) {
                cityAdapter.clear();
                cityAdapter.addAll(cities);
                cityAdapter.notifyDataSetChanged();

                // Clear the street adapter
                if (streetAdapter != null) {
                    streetAdapter.clear();
                }

                // Set up item selection listener for cityAutoComplete
                cityAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String selectedCity = (String) parent.getItemAtPosition(position);
                        setAutoCompleteFocusChangeListener(streetAutoComplete);
                        new FetchStreetsInCityTask().execute(selectedCity);
                    }
                });
            } else {
                Toast.makeText(registerCustomer.this, "Failed to fetch city names", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private class FetchStreetsInCityTask extends AsyncTask<String, Void, List<String>> {
        @Override
        protected List<String> doInBackground(String... cities) {
            if (cities.length > 0) {
                Log.d("FetchStreetsInCityTask", "Fetching streets in city from API...");
                return AddressApiService.fetchStreetsInCity(cities[0]);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<String> streets) {
            if (streets != null) {
                streetAdapter.clear();
                streetAdapter.addAll(streets);
                streetAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(registerCustomer.this, "Failed to fetch streets in the selected city", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
