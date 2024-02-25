package com.example.pageflix.activities.librarian_activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.pageflix.R;
import com.example.pageflix.activities.main.mainLibrarian;
import com.example.pageflix.entities.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class inStockScreen extends AppCompatActivity {
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> listData;
    private DatabaseReference dbRef;
    private String LibrarianID;
    private String USER_KEY = "Librarian";
    private String BOOK_KEY = "BooksID";
    private List<String> filteredBooks ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_stock_screen);
        init();
        getDataFromDB();
        search();
    }

    public void init() {
        listView = findViewById(R.id.listView);
        listData = new ArrayList<>();
        filteredBooks = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
        listView.setAdapter(adapter);
        LibrarianID = FirebaseAuth.getInstance().getCurrentUser().getUid(); //find LibrarianID (unique key)
        dbRef = FirebaseDatabase.getInstance().getReference(USER_KEY).child(LibrarianID).child(BOOK_KEY);
    }
    public void search() {
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setQueryHint("Search..."); // Set hint text
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                    filter(newText);
                return true;
            }
        });
    }

    private void filter(String searchText) {
        filteredBooks.clear();
        if (searchText.isEmpty()) {
            // If the search text is empty, add all items from the original list to listData
            getDataFromDB() ;
        } else {
            // If the search text is not empty, filter based on the search text
            for (String bookInfo : listData) {
                if (bookInfo.toLowerCase().contains(searchText.toLowerCase())) {
                    filteredBooks.add(bookInfo);
                }
            }
        }
        // Update the adapter with the new filtered list data
        adapter.clear();
        adapter.addAll(filteredBooks);
        adapter.notifyDataSetChanged();
    }

    private void getDataFromDB() {
        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (listData.size() > 0) listData.clear();// check if list clean
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String bookKey = ds.getKey();
                    int countBookInLib = ds.child("count").getValue(Integer.class);

                    if (countBookInLib > 0) {
                        DatabaseReference bookDB = FirebaseDatabase.getInstance().getReference("Books").child(bookKey);
                        bookDB.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Book book = dataSnapshot.getValue(Book.class);
                                if (book != null) {
                                    listData.add("Title: " + book.getTitle() + "\nAuthor: " + book.getAuthor() + "\nYear: " + book.getYear() + "\nCount: " + countBookInLib);
                                    adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        dbRef.addValueEventListener(vListener);
    }


    public void backToPreviousScreen(View v) {
        Intent intent = new Intent(this, mainLibrarian.class);// from Login Customer screen to First screen
        startActivity(intent);
    }
}
