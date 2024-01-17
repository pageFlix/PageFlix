package com.example.pageflix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class mainCustomer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_customer);
    }
    public void backToFirstScreen(View v){
        Intent intent = new Intent(this, FirstScreen.class);// from Login Customer screen to First screen
        startActivity(intent);
    }

    public static class user {
    }
}