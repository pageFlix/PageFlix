package com.example.pageflix.activities.librarian_activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pageflix.R;
import com.example.pageflix.activities.main.mainLibrarian;
import com.example.pageflix.entities.Book;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LibraryBookCountChange extends AppCompatActivity {
    private TextView tvTitle;
    private EditText edAddCount, edRemoveCount;
    private String addCount, removeCount;

    private String BOOKS = "Books"; // DataBase name for Librarians
    private String LibrarianID,idBook,title;
    private DatabaseReference LibrarianDB, bookDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_book_count_change);
        init();
        getIntentMain();
    }
    private void init(){
        tvTitle = findViewById(R.id.tvTitle);
        edAddCount = findViewById(R.id.edAddCount);
        edRemoveCount = findViewById(R.id.edRemoveCount);
        LibrarianID = FirebaseAuth.getInstance().getCurrentUser().getUid(); //find LibrarianID (unique key)
    }
    private void getIntentMain(){
        Intent i = getIntent();
        if (i != null){
            idBook = i.getStringExtra("idBook");
            title = i.getStringExtra("title");
            tvTitle.setText(title);
            LibrarianDB = FirebaseDatabase.getInstance().getReference("Librarian").child(LibrarianID)
                    .child("BooksID").child(idBook);
            bookDB =FirebaseDatabase.getInstance().getReference(BOOKS).child(idBook);
        }
    }
    private void addBookCounnt(DatabaseReference dataBase, int CurrentCount){
        dataBase.child("count").setValue(CurrentCount); // Increment count by 1
    }
    private void removeBookCounnt(DatabaseReference dataBase, int CurrentCount){
        dataBase.child("count").setValue(CurrentCount); // Increment count by 1
    }
    public void addButton(View v){
        addCount = edAddCount.getText().toString().trim();
        if (!TextUtils.isEmpty(addCount)) {
            int add = Integer.valueOf(addCount);
            LibrarianDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int count = snapshot.child("count").getValue(Integer.class);
                    addBookCounnt(LibrarianDB,(count+add));
                    addBookCounnt(bookDB.child("LibraryID").child(LibrarianID),(count+add));
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            bookDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Book book = snapshot.getValue(Book.class);
                    int count = book.getCount();
                    addBookCounnt(bookDB, (count+add));
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            Toast.makeText(getApplicationContext(), "Successful!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, inStockScreen.class);// from Login Customer screen to First screen
            startActivity(intent);
        }
        else  {
            Toast.makeText(getApplicationContext(), "Counter  is empty", Toast.LENGTH_SHORT).show();
        }
    }

    public void removeButton(View v){
        removeCount = edRemoveCount.getText().toString().trim();
        if (!TextUtils.isEmpty(removeCount)) {
            int remove = Integer.valueOf(removeCount);
            LibrarianDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int count = snapshot.child("count").getValue(Integer.class);
                    int check = count - remove;
                    if (check < 0) {
                        removeBookCounnt(LibrarianDB, (0));
                        removeBookCounnt(bookDB.child("LibraryID").child(LibrarianID), (0));
                        bookDB.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Book book = snapshot.getValue(Book.class);
                                int countTotal = book.getCount();
                                removeBookCounnt(bookDB, (countTotal - count));
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    else {
                        removeBookCounnt(LibrarianDB, check);
                        removeBookCounnt(bookDB.child("LibraryID").child(LibrarianID), check);
                        bookDB.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Book book = snapshot.getValue(Book.class);
                                int countTotal = book.getCount();
                                removeBookCounnt(bookDB, (countTotal - remove));

                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            Toast.makeText(getApplicationContext(), "Successful!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, inStockScreen.class);// from Login Customer screen to First screen
            startActivity(intent);
        }
        else  {
            Toast.makeText(getApplicationContext(), "Counter  is empty", Toast.LENGTH_SHORT).show();
        }
    }

    public void backToPreviousScreen(View v){
        Intent intent = new Intent(this, inStockScreen.class);// from Login Customer screen to First screen
        startActivity(intent);
    }
}