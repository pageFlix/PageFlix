package com.example.pageflix.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.pageflix.R;
import com.example.pageflix.entities.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class customerListBooks extends AppCompatActivity {
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> listData;
    private DatabaseReference db_customer;
    private String CustomerID;
    private String CUSTOMER_KEY = "Customer";
    private  String RENTALS = "rentals";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_list_books);
        init();;
    }
    public void init(){
        listView = findViewById(R.id.listView);
        listData = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
        listView.setAdapter(adapter);
        CustomerID = FirebaseAuth.getInstance().getCurrentUser().getUid(); //find LibrarianID (unique key)
        db_customer = FirebaseDatabase.getInstance().getReference(CUSTOMER_KEY).child("Books");
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
                        bookDB.addListenerForSingleValueEvent(new ValueEventListener() {
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
        db_customer.addListenerForSingleValueEvent(vListener);
    }

    public void backToPreviousScreen(View v) {
        Intent intent = new Intent(this, mainCustomer.class);
        startActivity(intent);
    }
}