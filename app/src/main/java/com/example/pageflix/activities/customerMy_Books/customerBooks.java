package com.example.pageflix.activities.customerMy_Books;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.pageflix.R;
import com.example.pageflix.activities.main.mainCustomer;
import com.example.pageflix.entities.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class customerBooks extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> listData;
    private List<String> list_temp_bookID, filteredBooks;
    private DatabaseReference dbRef;
    private String customerID;
    private String USER_KEY = "Customer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_books);
        init();
        getDataFromDB();
        setOnClickIten();
//        search();
    }
    public void init(){
        listView = findViewById(R.id.listView);
        listData = new ArrayList<>();
        filteredBooks = new ArrayList<>();
        list_temp_bookID = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
        listView.setAdapter(adapter);
        customerID = FirebaseAuth.getInstance().getCurrentUser().getUid(); //find LibrarianID (unique key)
        dbRef = FirebaseDatabase.getInstance().getReference(USER_KEY).child(customerID).child("Books");
    }
//    public void search() {
//        SearchView searchView = findViewById(R.id.searchView);
//        searchView.setQueryHint("Search..."); // Set hint text
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                filter(newText);
//                return true;
//            }
//        });
//    }
//
//    private void filter(String searchText) {
//        filteredBooks.clear();
//        if (searchText.isEmpty()) {
//            // If the search text is empty, add all items from the original list to listData
//            getDataFromDB() ;
//        } else {
//            // If the search text is not empty, filter based on the search text
//            for (String bookInfo : listData) {
//                if (bookInfo.toLowerCase().contains(searchText.toLowerCase())) {
//                    filteredBooks.add(bookInfo);
//                }
//            }
//        }
//        // Update the adapter with the new filtered list data
//        adapter.clear();
//        adapter.addAll(filteredBooks);
//        adapter.notifyDataSetChanged();
//    }

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
                                    list_temp_bookID.add(bookKey);
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
    private void setOnClickIten(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String book = list_temp_bookID.get(position);
                Intent intent = new Intent(getApplicationContext(), bookReview.class);
                intent.putExtra("bookID",book);
                startActivity(intent);
            }
        });

    }
    public void backToPreviousScreen(View v) {
        Intent intent = new Intent(this, mainCustomer.class);// from Login Customer screen to First screen
        startActivity(intent);
    }
}