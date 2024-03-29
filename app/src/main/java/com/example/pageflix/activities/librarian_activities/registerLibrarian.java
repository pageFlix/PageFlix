package com.example.pageflix.activities.librarian_activities;

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

import com.example.pageflix.activities.main.mainLibrarian;
import com.example.pageflix.services.AddressApiService;
import com.example.pageflix.R;
import com.example.pageflix.entities.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

public class registerLibrarian extends AppCompatActivity {


    private EditText edEmail, edPassword, edLibraryname, edCellphoneNumber, edNumber;
    private AutoCompleteTextView cityAutoComplete, streetAutoComplete;
    private ArrayAdapter<String> cityAdapter, streetAdapter;

    private DatabaseReference dbRef;
    private FirebaseAuth fbAuth;
    private String USER_KEY = "Librarian"; // DataBase name for Librarians

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_librarian);
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
        edLibraryname = findViewById(R.id.edLibraryname);
        edCellphoneNumber = findViewById(R.id.Cellphone_number);
        edNumber = findViewById(R.id.number);
        cityAutoComplete = findViewById(R.id.cityAutoComplete);
        streetAutoComplete = findViewById(R.id.streetAutoComplete);
    }

    public void signupButton(View v) {
        Log.d("SignUpButton", "Button clicked");
        String email = this.edEmail.getText().toString();
        String password = this.edPassword.getText().toString();
        String LibraryName = this.edLibraryname.getText().toString();
        String CellNumber = this.edCellphoneNumber.getText().toString();
        String City = this.cityAutoComplete.getText().toString();
        String Street = this.streetAutoComplete.getText().toString();
        String Number = this.edNumber.getText().toString();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(LibraryName) &&
                !TextUtils.isEmpty(CellNumber) && !TextUtils.isEmpty(City)&& !TextUtils.isEmpty(Street)  && !TextUtils.isEmpty(Number)) {
            fbAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        User user = new User(email, null, null ,null, CellNumber, City, Street, Number,LibraryName);
                        createUserInDatabase(user.getEmail(),user.getLibraryName(), user.getCellNumber(), user.getCity(),user.getStreet(),user.getNumber());
                        Intent intent = new Intent(getApplicationContext(), mainLibrarian.class);
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

    private void createUserInDatabase(String email, String LibraryName, String CellNumber, String City, String street, String Number) {
        dbRef = FirebaseDatabase.getInstance().getReference().child(USER_KEY);
        String userId = fbAuth.getCurrentUser().getUid();
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("email", email);
        userMap.put("libraryName", LibraryName);
        userMap.put("city", City);
        userMap.put("street", street);
        userMap.put("number", Number);
        userMap.put("cellNumber", CellNumber);


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
                Toast.makeText(registerLibrarian.this, "Failed to fetch city names", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(registerLibrarian.this, "Failed to fetch streets in the selected city", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void backToPreviousScreen(View v){
        Intent intent = new Intent(this, LoginLibrarian.class);// from Login com.example.pageflix.activities.librarian_activities.LoginLibrarian.Librarian screen to First screen
        startActivity(intent);
    }
}
