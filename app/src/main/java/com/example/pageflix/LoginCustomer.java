package com.example.pageflix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LoginCustomer extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_customer);
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