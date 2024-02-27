package com.example.pageflix.activities.customer_activities;

import android.os.Bundle;
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
import com.example.pageflix.entities.User;
import com.google.firebase.auth.FirebaseAuth;
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
    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private List<Book> allBooksList ;
    private List<Book> filteredBooks ;
    private DatabaseReference booksRef;
    private Integer customerAge ;

    private enum ResultsSort { BY_TITLE, BY_YEAR, BY_CATEGORY}
    private enum ResultsOrder{ INCREASING, DECREASING }

    private ResultsOrder currentOrder ;
    private ResultsSort currentSort ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initVariables() ;
        setContentView(R.layout.activity_search_books);
        setOrderSpinnerView();
        setupRecyclerView();
        setupFirebaseDatabase();
        setupSearchView();
        getBooksList();
    }

    private void initVariables(){
        allBooksList = new ArrayList<>();
        filteredBooks = new ArrayList<>();
        setCustomerAge() ;
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
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (selectedItem.equals("Title")){
                    currentSort = ResultsSort.BY_TITLE ;
                }else if(selectedItem.equals("Publication Year")){
                    currentSort = ResultsSort.BY_YEAR ;
                }else if(selectedItem.equals("Category")){
                    currentSort = ResultsSort.BY_CATEGORY ;
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
        Query allBooksRef = booksRef.limitToFirst(1000);
        allBooksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Iterate through each book in the dataSnapshot
                for (DataSnapshot bookSnapshot : dataSnapshot.getChildren()) {
                    // Convert each book to a Book object and add it to the list
                    Book book = bookSnapshot.getValue(Book.class);
                    if (book != null) {
                        if(book.count > 0) {
                            book.ID = bookSnapshot.getKey();
                            if(book.getAge() <= customerAge) {
                                allBooksList.add(book);
                            }
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
            case BY_CATEGORY:
                filteredBooks.sort(new Book.CategoryComparator());
        }
        if(currentOrder == ResultsOrder.DECREASING) {
            Collections.reverse(filteredBooks);
        }
        adapter.notifyDataSetChanged();
    }

    private void reverseListAndNotify(){
        Collections.reverse(filteredBooks);
        adapter.notifyDataSetChanged();
    }

    private void setCustomerAge(){
        String uid = FirebaseAuth.getInstance().getUid();
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("Customer").child(uid);
        customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User customer = snapshot.getValue(User.class) ;
                String birthDate = customer.getBirthDay() ;

                // Split the birthdate string by "/"
                String[] parts = birthDate.split("/");

                // Extract day, month, and year
                int birthDay = Integer.parseInt(parts[0]);
                int birthMonth = Integer.parseInt(parts[1]);
                int birthYear = Integer.parseInt(parts[2]);

                // Get the current date
                java.util.Calendar now = java.util.Calendar.getInstance();
                int currentYear = now.get(java.util.Calendar.YEAR);
                int currentMonth = now.get(java.util.Calendar.MONTH) + 1; // Months are zero-based
                int currentDay = now.get(java.util.Calendar.DAY_OF_MONTH);

                // Calculate age
                int age = currentYear - birthYear;
                if (birthMonth > currentMonth || (birthMonth == currentMonth && birthDay > currentDay)) {
                    age--; // Subtract 1 if birthday hasn't occurred yet this year
                }
                customerAge = age ;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
