package com.example.pageflix;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
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

public class registerLibrarian extends AppCompatActivity {
    private static final String API_BASE_URL = "https://data.gov.il/api/3/action/datastore_search";
    private static final String RESOURCE_ID = "9ad3862c-8391-4b2f-84a4-2d4c68625f4b";

    private EditText edEmail, edPassword, edLibraryname, edCellphoneNumber;
    private AutoCompleteTextView cityAutoComplete, streetAutoComplete, numberAutoComplete;
    private ArrayAdapter<String> cityAdapter, streetAdapter, numberAdapter;

    private DatabaseReference dbRef;
    private FirebaseAuth fbAuth;
    private String USER_KEY = "Librarian";//DataBase name for Librarians

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_librarian);
        init();
        fetchCityNamesFromApi();
      //  new AddressApiService.FetchCityNamesTask(cityAutoComplete,cityAdapter).execute();      ////   for now its not working when calling from the api so the class is in the end of the file
        new registerLibrarian.FetchCityNamesTask().execute();

        Log.d("ActivityLifecycle", "registerLibrarian activity created");

        // Set up the adapter
        cityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        streetAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        numberAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);

        // Set up the initial adapters for the first level (city)
        cityAutoComplete.setAdapter(cityAdapter);
        streetAutoComplete.setAdapter(streetAdapter);
        numberAutoComplete.setAdapter(numberAdapter);

        // Set up the text change listeners for search functionality
        setupSearchFunctionality(cityAutoComplete, cityAdapter);
        setupSearchFunctionality(streetAutoComplete, streetAdapter);
        setupSearchFunctionality(numberAutoComplete, numberAdapter);


        // Show the dropdown when the AutoCompleteTextView gains focus
        setAutoCompleteFocusChangeListener(cityAutoComplete);
        setAutoCompleteFocusChangeListener(streetAutoComplete);
        setAutoCompleteFocusChangeListener(numberAutoComplete);
    }

    private void fetchCityNamesFromApi() {
        AddressApiService.fetchCityNames(cityAutoComplete, cityAdapter);
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
        cityAutoComplete = findViewById(R.id.cityAutoComplete);
        streetAutoComplete = findViewById(R.id.streetAutoComplete);
        numberAutoComplete = findViewById(R.id.numberAutoComplete);
    }

    public void signupButton(View v) {
        Log.d("SignUpButton", "Button clicked");
        String email = edEmail.getText().toString();
        String password = edPassword.getText().toString();
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            fbAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        createUserInDatabase(email);
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

    private void createUserInDatabase(String email) {
        dbRef = FirebaseDatabase.getInstance().getReference().child(USER_KEY);
        String userId = fbAuth.getCurrentUser().getUid();
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("email", email);
        dbRef.child(userId).setValue(userMap);
    }

    public void backToPreviousScreen(View v) {
        Intent intent = new Intent(this, LoginLibrarian.class);
        startActivity(intent);
    }

    private class FetchCityNamesTask extends AsyncTask<Void, Void, List<String>> {
        @Override
        protected List<String> doInBackground(Void... voids) {
            return AddressApiService.fetchCityNames();
        }

        @Override
        protected void onPostExecute(List<String> cities) {
            if (cities != null) {
                cityAdapter.clear();
                cityAdapter.addAll(cities);
                cityAdapter.notifyDataSetChanged();

                // Clear the street and number adapters
                if (streetAdapter != null) {
                    streetAdapter.clear();
                }
                if (numberAdapter != null) {
                    numberAdapter.clear();
                }
            } else {
                Toast.makeText(registerLibrarian.this, "Failed to fetch city names", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
