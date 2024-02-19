package com.example.pageflix.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchBooks extends AppCompatActivity {

    private static final String TAG = "SearchBooksActivity";

    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private List<Book> allBooksList = new ArrayList<>(), filteredBooks = new ArrayList<>();
    private DatabaseReference booksRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_books);
        Log.d(TAG, "onCreate: Activity created");

        // Initialize RecyclerView
        setupRecyclerView();

        // Setup Firebase Database
        setupFirebaseDatabase();

        // Setup SearchView
        setupSearchView();
        getBooksList();
    }

    // Initialize RecyclerView
    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        adapter = new BookAdapter(filteredBooks);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    // Setup Firebase Database
    private void setupFirebaseDatabase() {
        Log.d(TAG, "setupFirebaseDatabase: Setting up Firebase Database");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        booksRef = database.getReference("Books");
    }

    // Setup SearchView
    private void setupSearchView() {
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    showAllBooks();
                } else if (newText.length() >= 1) {
                    // If the search query has at least one character, filter books
                    filterBooks(newText);
                }
                return true;
            }
        });
    }


    private void getBooksList(){
        Log.d(TAG, "showAllBooks: Showing all books");
        Query allBooksRef = booksRef.limitToFirst(1000);
        allBooksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: Data changed");
                // Iterate through each book in the dataSnapshot
                for (DataSnapshot bookSnapshot : dataSnapshot.getChildren()) {
                    // Convert each book to a Book object and add it to the list
                    Book book = bookSnapshot.getValue(Book.class);
                    if (book != null) {
                        book.ID = bookSnapshot.getKey();
                        allBooksList.add(book);
                    }
                }
                showAllBooks();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database errors
                handleDatabaseError(databaseError);
            }
        });
    }
    // Show all books
    private void showAllBooks() {
        adapter.setBookList(allBooksList);
    }

    // Filter books based on search text
    private void filterBooks(String searchText) {
        filteredBooks.clear();
        for(Book b: allBooksList){
            if(b.getTitle().toLowerCase().contains(searchText.toLowerCase())){
                filteredBooks.add(b) ;
            }
        }
        adapter.setBookList(filteredBooks);
    }

    // Handle Firebase database errors
    private void handleDatabaseError(DatabaseError databaseError) {
        // Log error message
        Log.e(TAG, "handleDatabaseError: Failed to read value.", databaseError.toException());
        // Display a toast message to the user
        Toast.makeText(SearchBooks.this, "Failed to retrieve data.", Toast.LENGTH_SHORT).show();
    }
}
