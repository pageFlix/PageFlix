package com.example.pageflix;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class registerCustomer extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;

    private EditText edEmail, edPassword, edCellphoneNumber, edNumber, edFirstname , edLastname, edBirthday ;

    private AutoCompleteTextView cityAutoComplete, streetAutoComplete;
    private ArrayAdapter<String> cityAdapter, streetAdapter;

    private DatabaseReference dbRef;
    private FirebaseAuth fbAuth;
    private String USER_KEY = "Customer"; // DataBase name for Librarians
    public registerCustomer() {

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_customer);
        init();
        fbAuth = FirebaseAuth.getInstance();



        edBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign-In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign-In failed
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        // Get user's email from Google account
        String email = acct.getEmail();

        // Fill in email field
        edEmail.setText(email);

        // Password field can be left empty or disabled
        // You can also hide the password field if you don't need it for Google Sign-In

        // Additional actions if needed...

        // Proceed with the sign-up process...
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                registerCustomer.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Update the EditText with the selected date
                        String birthday = dayOfMonth + "/" + (month + 1) + "/" + year;
                        edBirthday.setText(birthday);
                    }
                },
                year, month, dayOfMonth);

        datePickerDialog.show();
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
        edBirthday = findViewById(R.id.edBirthday);
        edCellphoneNumber = findViewById(R.id.edCellphoneNumber);
        edNumber = findViewById(R.id.edNumber);
        cityAutoComplete = findViewById(R.id.cityAutoComplete);
        streetAutoComplete = findViewById(R.id.streetAutoComplete);



    }

    public void signupButton(View v) {
        Log.d("SignUpButton", "Button clicked");
        String email = this.edEmail.getText().toString();
        String password = this.edPassword.getText().toString();
        String LastName = this.edLastname.getText().toString();
        String FirstName = this.edFirstname.getText().toString();
        String BirthDay = this.edBirthday.getText().toString();
        String CellNumber = this.edCellphoneNumber.getText().toString();
        String City = this.cityAutoComplete.getText().toString();
        String Street = this.streetAutoComplete.getText().toString();
        String Number = this.edNumber.getText().toString();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(LastName) && !TextUtils.isEmpty(FirstName) &&!TextUtils.isEmpty(BirthDay) &&
        !TextUtils.isEmpty(CellNumber) && !TextUtils.isEmpty(City)&& !TextUtils.isEmpty(Street) && !TextUtils.isEmpty(Number) ) {
            fbAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        User user = new User(email,password, FirstName, LastName ,BirthDay, CellNumber, City, Street, Number,null);
                        createUserInDatabase(user.getEmail(),user.getPassword(), user.getFirstName(), user.getLastName(), user.getBirthDay(), user.getCellNumber(), user.getCity(),user.getStreet(),user.getNumber());
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

    private void createUserInDatabase(String email,String password, String FisrtName, String LastName,String BirthDay, String CellNumber, String City, String street, String Number) {
        dbRef = FirebaseDatabase.getInstance().getReference().child(USER_KEY);
        String userId = fbAuth.getCurrentUser().getUid();
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("email", email);
        userMap.put("FistName", FisrtName);
        userMap.put("LastName", LastName);
        userMap.put("BirthDay", BirthDay);
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
