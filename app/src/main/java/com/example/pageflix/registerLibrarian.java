package com.example.pageflix;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class registerLibrarian extends AppCompatActivity {
    private EditText edEmail, edName;
    private DatabaseReference dbRef;
    private String USER_KEY = "Librarian";//DataBase name for Librarians
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_librarian);
        init();
    }
    public void init(){// find all text's in the screen
        edEmail = findViewById(R.id.edEmail);
        edName = findViewById(R.id.edName);
        dbRef = FirebaseDatabase.getInstance().getReference(USER_KEY);
    }
    public void signupButton(View v){
        String id = dbRef.getKey();
        String email = edEmail.getText().toString();
        String name = edName.getText().toString();
        Librarian newLibrarian = new Librarian(email, name);
        //check if user fill all the lines
        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(name)){
            dbRef.push().setValue(newLibrarian);//add data to database
            Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Write name and email", Toast.LENGTH_SHORT).show();
        }
    }
    public void backToPreviousScreen(View v){
        Intent intent = new Intent(this, LoginLibrarian.class);// from Login Customer screen to PreviousScreen screen
        startActivity(intent);
    }
}