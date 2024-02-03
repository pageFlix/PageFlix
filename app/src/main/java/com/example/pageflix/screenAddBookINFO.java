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

public class screenAddBookINFO extends AppCompatActivity {
    private EditText edDescription, edCategory;
    private String title, author, year;
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
        bookDB = FirebaseDatabase.getInstance().getReference(USER_KEY);
    }
    //    add in Books -> author, title, year, description , publication year
    public void addBook(View v) {
        String description = edDescription.getText().toString();
        String category = edCategory.getText().toString();
        Book newBook = new Book( author,  category,  year,  description,  title, 1);
        if (!TextUtils.isEmpty(description) &&  !TextUtils.isEmpty(category)) {
            bookDB.push().setValue(newBook);
            Intent intent = new Intent(this, mainLibrarian.class);
            startActivity(intent);
        }
        else {
            Toast.makeText(this, "Write Category and Description", Toast.LENGTH_SHORT).show();
        }
    }

}