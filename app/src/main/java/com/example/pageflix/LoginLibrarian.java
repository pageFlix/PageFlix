package com.example.pageflix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LoginLibrarian extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_librarian);
    }
    public void registerLibrarian(View v){
        Intent intent = new Intent(this, registerLibrarian.class);// from Login Customer screen to registerLibrarian screen
        startActivity(intent);
    }
    public void backToFirstScreen(View v){
        Intent intent = new Intent(this, FirstScreen.class);// from Login com.example.pageflix.LoginLibrarian.Librarian screen to First screen
        startActivity(intent);
    }

}