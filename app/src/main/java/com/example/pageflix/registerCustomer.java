package com.example.pageflix;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class registerCustomer extends AppCompatActivity {
    private EditText edEmail ,edPassword;
    private DatabaseReference dbRef;
    private FirebaseAuth fbAuth;
    private String USER_KEY = "Customer";//DataBase name for Customers
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_customer);
        init();
    }
    public void init(){// find all text's in the screen
        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassword);
        fbAuth = FirebaseAuth.getInstance();
    }
    public void signupButton(View v){
        String email = edEmail.getText().toString();
        String password = edPassword.getText().toString();
       //check if .user fill all the lines
        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
            //create Authentication
            fbAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        createUserInDatabase(email);
                        //open the user main screen
                        Intent intent = new Intent(getApplicationContext(), mainCustomer.class);
                        startActivity(intent);
                        Toast.makeText(getApplicationContext(), "User Sign Up Successful!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "User Sign Up failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else {
            Toast.makeText(this, "Write name and Email", Toast.LENGTH_SHORT).show();
        }
    }
    //Split type of user Customer / Librarian
    private void createUserInDatabase(String email) {
        // save type user and email in realtime database
        dbRef = FirebaseDatabase.getInstance().getReference().child(USER_KEY);
        String userId = fbAuth.getCurrentUser().getUid();
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("email", email);
        dbRef.child(userId).setValue(userMap);
    }
    public void backToPreviousScreen(View v){
        Intent intent = new Intent(this, LoginCustomer.class);// from Login Customer screen PreviousScreen screen
        startActivity(intent);
    }
}