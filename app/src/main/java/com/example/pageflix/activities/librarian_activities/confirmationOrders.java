package com.example.pageflix.activities.librarian_activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.pageflix.R;
import com.example.pageflix.activities.main.mainLibrarian;

public class confirmationOrders extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation_orders);
    }
    public void backToPreviousScreen(View v){
        finish();
    }
}