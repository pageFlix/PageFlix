package com.example.pageflix.activities.customer_activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
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
import java.util.Collections;
import java.util.List;

public class SearchBooks extends AppCompatActivity {

    private static final String TAG = "SearchBooksActivity";

    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private final List<Book> allBooksList = new ArrayList<>();
    private List<Book> filteredBooks = new ArrayList<>();
    private DatabaseReference booksRef;
    private boolean isArrowUp ;

    private enum ResultsSort { BY_TITLE, BY_YEAR}
    private enum ResultsOrder{ INCREASING, DECREASING }

    private ResultsOrder currentOrder ;
    private ResultsSort currentSort ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_books);
        Log.d(TAG, "onCreate: Activity created");
        setOrderSpinnerView();
        // Initialize RecyclerView
        setupRecyclerView();

        // Setup Firebase Database
        setupFirebaseDatabase();

        // Setup SearchView
        setupSearchView();
        getBooksList();

    }

    private void setOrderSpinnerView(){
        this.currentOrder = ResultsOrder.INCREASING ;
        this.currentSort = ResultsSort.BY_TITLE ;
        ImageButton imageButton = findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle the state
                if (currentOrder == ResultsOrder.INCREASING) {
                    imageButton.setImageResource(R.drawable.ic_arrow_up);
                    currentOrder = ResultsOrder.DECREASING ;
                } else {
                    imageButton.setImageResource(R.drawable.ic_arrow_down);
                    currentOrder = ResultsOrder.INCREASING ;
                }
                reverseListAndNotify();
            }
        });

        Spinner orderSpinner = findViewById(R.id.orderSpinner);

        orderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // The item at position 'position' is selected
                // Perform actions based on the selected item
                String selectedItem = parent.getItemAtPosition(position).toString();
                // Example: Log the selected item
                if (selectedItem.equals("Title")){
                    currentSort = ResultsSort.BY_TITLE ;
                }else if(selectedItem.equals("Publication Year")){
                    currentSort = ResultsSort.BY_YEAR ;
                }
                updateListSorting();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Called when no item is selected
            }
        });

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
                    filterBooks(newText);
                    return true ;
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
                        if(book.count > 0) {
                            book.ID = bookSnapshot.getKey();
                            allBooksList.add(book);
                        }
                    }
                }
                filterBooks("");
                adapter.setBookList(filteredBooks);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database errors
                handleDatabaseError(databaseError);
            }
        });
    }

    // Filter books based on search text
    private void filterBooks(String searchText) {
        filteredBooks.clear();
        if(searchText.isEmpty()){
            filteredBooks = new ArrayList<>(allBooksList) ;
            adapter.setBookList(filteredBooks);
        }else {
            for (Book b : allBooksList) {
                if (b.getTitle().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredBooks.add(b);
                }
            }
        }
        updateListSorting();
    }

    // Handle Firebase database errors
    private void handleDatabaseError(DatabaseError databaseError) {
        // Log error message
        Log.e(TAG, "handleDatabaseError: Failed to read value.", databaseError.toException());
        // Display a toast message to the user
        Toast.makeText(SearchBooks.this, "Failed to retrieve data.", Toast.LENGTH_SHORT).show();
    }

    private void updateListSorting(){
        switch (currentSort){
            case BY_TITLE:
                filteredBooks.sort(new Book.TitleComparator());
                break ;
            case BY_YEAR:
                filteredBooks.sort(new Book.PublicationYearComparator());
                break ;
        }
        Log.d("shit", "Current order:" + currentOrder) ;
        if(currentOrder == ResultsOrder.DECREASING) {
            Collections.reverse(filteredBooks);
        }
        adapter.notifyDataSetChanged();
    }

    private void reverseListAndNotify(){
        Collections.reverse(filteredBooks);
        adapter.notifyDataSetChanged();
    }
}
