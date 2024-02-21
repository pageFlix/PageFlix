package com.example.pageflix.activities.addBook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pageflix.entities.Book;
import com.example.pageflix.R;
import com.example.pageflix.activities.main.mainLibrarian;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class screenAddBookINFO extends AppCompatActivity {
    private EditText edDescription, edCategory;
    private String title, author, year, libID;
    private String BOOKS = "Books"; // DataBase name for Librarians
    DatabaseReference bookDB, libDB;
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
        bookDB = FirebaseDatabase.getInstance().getReference(BOOKS);
        libDB = FirebaseDatabase.getInstance().getReference("Librarian").child(libID);
    }
    //    add in Books -> author, title, year, description , publication year + LibraryID
    public void addBook(View v) {
        String description = edDescription.getText().toString();
        String category = edCategory.getText().toString();
        Book newBook = new Book( title,author,  year, 1, category, description);
        if (!TextUtils.isEmpty(description) &&  !TextUtils.isEmpty(category)) {
            DatabaseReference newBookRef = bookDB.push();// create newBookRef for add data after adding book info
            newBookRef.setValue(newBook); // add data
            String bookID = newBookRef.getKey(); // get relevant Book key
            HashMap<String, Integer> bookCount = new HashMap<>();// create count : 1
            bookCount.put("count", 1);

            Map<String, Object> BookUpdates = new HashMap<>();// to avoid unique key creation
            Map<String, Object> LibraryIDupdates = new HashMap<>();// to avoid unique key creation
            BookUpdates.put("LibraryID/"  + libID,bookCount);
            LibraryIDupdates.put("BooksID/" + bookID, bookCount);

            newBookRef.updateChildren(BookUpdates);
            if (bookID != null){libDB.updateChildren(LibraryIDupdates);}
            Intent intent = new Intent(this, mainLibrarian.class);
            startActivity(intent);
        }
        else {
            Toast.makeText(this, "Write Category and Description", Toast.LENGTH_SHORT).show();
        }
    }

}