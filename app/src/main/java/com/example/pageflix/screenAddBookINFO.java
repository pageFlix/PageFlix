package com.example.pageflix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class screenAddBookINFO extends AppCompatActivity {
    private EditText edDescription, edCategory;
    private String title, author, year, libID;
    private String USER_KEY = "Books"; // DataBase name for Librarians
    DatabaseReference bookDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_add_book_info);
        init();
    }
    private void init(){
        edDescription = findViewById(R.id.edDescription);
        edCategory = findViewById(R.id.edCategory);
        title = getIntent().getStringExtra("title");
        author = getIntent().getStringExtra("author");
        year = getIntent().getStringExtra("year");
        libID = getIntent().getStringExtra("libID");
        bookDB = FirebaseDatabase.getInstance().getReference(USER_KEY);
    }
    //    add in Books -> author, title, year, description , publication year
    public void addBook(View v) {
        String description = edDescription.getText().toString();
        String category = edCategory.getText().toString();
        Book newBook = new Book( title,author,  year, 1, category, description);
        if (!TextUtils.isEmpty(description) &&  !TextUtils.isEmpty(category)) {
            DatabaseReference newBookRef = bookDB.push(); // Pushing userMap to a new child node under bookDB
            newBookRef.setValue(newBook); // Setting userMap to the new child node
            newBookRef.child("LibraryID").push().setValue(libID); // Setting LibraryID under the new child node
            Intent intent = new Intent(this, mainLibrarian.class);
            startActivity(intent);
        }
        else {
            Toast.makeText(this, "Write Category and Description", Toast.LENGTH_SHORT).show();
        }
    }

}