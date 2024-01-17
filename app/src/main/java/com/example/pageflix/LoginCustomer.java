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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class LoginCustomer extends AppCompatActivity {
    private EditText edEmail ,edPassword;
    private FirebaseAuth fbAuth; // for email+password connection
    private String USER_KEY = "Customer";//DataBase name for Customers
    private DatabaseReference dbRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_customer);
        init();
    }
    public void init(){
        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassword);
        fbAuth = FirebaseAuth.getInstance();
    }
    public void loginButton(View v){
        String email = edEmail.getText().toString();
        String password = edPassword.getText().toString();
        //check if .user fill all the lines
        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            // check email+password
            fbAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        //find all Customers
                        dbRef = FirebaseDatabase.getInstance().getReference(USER_KEY);
                        checkUserTypeAndRedirect(email);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Email or Password is incorrect", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else {
            Toast.makeText(this, "Write name and Email", Toast.LENGTH_SHORT).show();
        }
    }
    // check type user email
    private void checkUserTypeAndRedirect(String email) {
        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean flag = true;
                for(DataSnapshot ds : snapshot.getChildren()){
                    String emailFromDatabase = ds.child("email").getValue(String.class);
                    // if finds that user with 'x' email is customer open his main screen
                    if (email.equals(emailFromDatabase)) {
                        flag = false;
                        Intent intent = new Intent(getApplicationContext(), mainCustomer.class);
                        startActivity(intent);
                        Toast.makeText(getApplicationContext(), "User Sign In Successful", Toast.LENGTH_SHORT).show();
                    }
                }
                if (flag){
                    Toast.makeText(getApplicationContext(), "This email not related to Customer account ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        dbRef.addValueEventListener(vListener);
    }

    public void registerCustomer(View v){
        Intent intent = new Intent(this, registerCustomer.class);// from Login Customer screen to registerCustomer screen
        startActivity(intent);
    }
    public void backToFirstScreen(View v){
        Intent intent = new Intent(this, FirstScreen.class);// from Login Customer screen to First screen
        startActivity(intent);
    }
}