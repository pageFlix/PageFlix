package com.example.pageflix;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
    private String BOOK_KEY = "Books";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_stock_screen);
        init();
        getDataFromDB();
    }

    public void init() {
        listView = findViewById(R.id.listView);
        listData = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
        listView.setAdapter(adapter);
        LibrarianID = FirebaseAuth.getInstance().getCurrentUser().getUid(); //find LibrarianID (unique key)
        dbRef = FirebaseDatabase.getInstance().getReference(USER_KEY).child(LibrarianID).child(BOOK_KEY);
        ;
    }

    private void getDataFromDB() {
        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (listData.size() > 0) listData.clear();// check if list clean
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Book book = ds.getValue(Book.class);
                    assert book != null;// check if not empty
                    if (book.count > 0) {
                        listData.add("Title: " + book.title + "\nAuthor: " + book.author + "\nYear: " + book.year + "\nCount: " + book.count);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        dbRef.addValueEventListener(vListener);
    }

    public void backToPreviousScreen(View v) {
        Intent intent = new Intent(this, mainLibrarian.class);// from Login Customer screen to First screen
        startActivity(intent);
    }
}
