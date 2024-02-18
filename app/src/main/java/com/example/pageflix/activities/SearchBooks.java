package com.example.pageflix.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pageflix.R;
import com.example.pageflix.adapters.BookAdapter;
import com.example.pageflix.entities.Book;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchBooks extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private List<Book> bookList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_books);
        // Initialize RecyclerView
        setupRecyclerView();

        // Setup Firebase Database
        setupFirebaseDatabase();
    }

    // Initialize RecyclerView
    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        adapter = new BookAdapter(bookList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    // Setup Firebase Database
    private void setupFirebaseDatabase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference booksRef = database.getReference("Books");

        // Limit to 1000 books
        booksRef.limitToFirst(1000).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Handle data changes
                handleDataChange(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database errors
                handleDatabaseError(databaseError);
            }
        });
    }

    // Handle data changes from Firebase database
    private void handleDataChange(DataSnapshot dataSnapshot) {
        // Clear the list before adding new data
        bookList.clear();

        // Iterate through each book in the dataSnapshot
        for (DataSnapshot bookSnapshot : dataSnapshot.getChildren()) {
            // Convert each book to a Book object and add it to the list
            Book book = bookSnapshot.getValue(Book.class);
            Integer count = bookSnapshot.child("count").getValue(Integer.class) ;
            if (book != null && count != null) {
                if(count > 0) {
                    book.ID = bookSnapshot.getKey();
                    bookList.add(book);
                }
            }
        }

        // Notify adapter about data changes
        adapter.notifyDataSetChanged();
    }

    // Handle Firebase database errors
    private void handleDatabaseError(DatabaseError databaseError) {
        // Log error message
        Log.e("Error", "Failed to read value.", databaseError.toException());
    }

}
