package com.example.pageflix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

public class mainCustomer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_customer);
    }
    public void backToFirstScreen(View v) {
        Intent intent = new Intent(this, FirstScreen.class);// from Login Customer screen to First screen
        startActivity(intent);
    }
    public void signOut(View v){
        Intent intent = new Intent(this, FirstScreen.class);// from Login Customer screen to First screen
        startActivity(intent);
    }
    public void Update_Profile(View v) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        Intent intent = new Intent(getApplicationContext(), Update_Customer_Profile.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }
    public void Search_books(View v) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        Intent intent = new Intent(getApplicationContext(), SearchBooks.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }




}