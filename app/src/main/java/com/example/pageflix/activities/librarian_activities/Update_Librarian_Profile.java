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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class Update_Librarian_Profile extends AppCompatActivity {
    private EditText edEmail, edLibraryname, edCellphoneNumber, edNumber;
    private AutoCompleteTextView cityAutoComplete, streetAutoComplete;
    private ArrayAdapter<String> cityAdapter, streetAdapter;
    private FirebaseAuth fbAuth;
    private DatabaseReference dbRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_librarian_profile);


        // Initialize FirebaseAuth and DatabaseReference
        fbAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference().child("Librarian");

        // Retrieve references to EditText fields
        edLibraryname = findViewById(R.id.edLibraryname);
        edCellphoneNumber = findViewById(R.id.edCellphoneNumber);
        edNumber = findViewById(R.id.edNumber);
        cityAutoComplete = findViewById(R.id.cityAutoComplete);
        streetAutoComplete = findViewById(R.id.streetAutoComplete);

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
        setAutoCompleteFocusChangeListener(streetAutoComplete);

        // Fetch city names from the API
        new FetchCityNamesTask().execute();

        // Retrieve user ID
        userId = fbAuth.getCurrentUser().getUid();

        // Fetch user data from Firebase and populate EditText fields
        fetchUserData();
    }

    private void fetchUserData() {
        // Retrieve user data from Firebase Realtime Database using userId
        dbRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Populate EditText fields with user data
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        edLibraryname.setText(user.getLibraryName());
                        edCellphoneNumber.setText(user.getCellNumber());
                        cityAutoComplete.setText(user.getCity());
                        streetAutoComplete.setText(user.getStreet());
                        edNumber.setText(user.getNumber());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
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


    public void Update_button(View v) {
        Log.d("UpdateButton", "Button clicked");
        String librariName = this.edLibraryname.getText().toString();
        String city = this.cityAutoComplete.getText().toString();
        String street = this.streetAutoComplete.getText().toString();
        String number = this.edNumber.getText().toString();
        String cellNumber = this.edCellphoneNumber.getText().toString();


        if (!TextUtils.isEmpty(librariName) &&
                !TextUtils.isEmpty(cellNumber) && !TextUtils.isEmpty(city) && !TextUtils.isEmpty(street) && !TextUtils.isEmpty(number)) {

            // Get the current user
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            updateUserProfile(currentUser.getUid(), currentUser.getEmail(),librariName, cellNumber, city, street, number);

        } else {
            Toast.makeText(this, "Please fill in all the required fields", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUserProfile(String userId, String email, String libraryName, String cellNumber, String city, String street, String number) {
        // Create a User object with the updated information
        User updatedUser = new User(email, null, null, null, cellNumber, city, street, number,libraryName);
        Intent intent = new Intent(this, mainLibrarian.class);// from Login com.example.pageflix.activities.LoginLibrarian.Librarian screen to First screen

        // Update the user's profile in the database
        dbRef.child(userId).setValue(updatedUser)
                .addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Update successful
                            Toast.makeText(Update_Librarian_Profile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            startActivity(intent);
                        } else {
                            // Update failed
                            Toast.makeText(Update_Librarian_Profile.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    // Fetch city names from the API asynchronously
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
                Toast.makeText(Update_Librarian_Profile.this, "Failed to fetch city names", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(Update_Librarian_Profile.this, "Failed to fetch streets in the selected city", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void backToPreviousScreen(View v){
        Intent intent = new Intent(this, mainLibrarian.class);// from Login com.example.pageflix.activities.librarian_activities.LoginLibrarian.Librarian screen to First screen
        startActivity(intent);
    }
}
