package com.example.pageflix;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BookPopupActivity extends Activity {

    private TextView bookTitleTextView, bookAuthorTextView, bookYearTextView, bookDescriptionTextView;
    private RecyclerView libraryRecyclerView;
    private Button rentButton;

    private String bookID ;
    private LibraryAdapter adapter ;
    private List<Library> libraries;
    private RentAPI rentAPI ;

    private List<String> libIDs ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_book_details);
        libraries = new ArrayList<>();
        libIDs = new ArrayList<>() ;
        this.rentAPI = new RentAPI() ;
        initViews();
        setBookProps();
        getLibrariesWithBook();
        setListeners();
    }

    // Setup Firebase Database

    public void getLibrariesWithBook() {
        Log.d("shit", "bookID= " + bookID) ;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference booksRef = database.getReference("Books");
        booksRef.child(bookID).child("LibraryID").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot libIDsSnap : dataSnapshot.getChildren()) {
                    getLibraryByID(libIDsSnap.getKey());
                    Log.d("shit", "LibID: " + libIDsSnap.getKey()) ;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Error", "Failed to read value.", databaseError.toException());            }
        });



    }

    private void getLibraryByID(String id){
        DatabaseReference librariesRef = FirebaseDatabase.getInstance().getReference("Librarian") ;
            librariesRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Library library = dataSnapshot.getValue(Library.class);
                        if (library != null) {
                            library.ID = dataSnapshot.getKey() ;
                            libraries.add(library) ;
                        }
                    } else {
                        Log.e("shit","libraries not found by the id's") ;
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            }) ;
    }
    private void initViews(){
        bookTitleTextView = findViewById(R.id.bookTitleTextView);
        bookAuthorTextView = findViewById(R.id.bookAuthorTextView);
        bookYearTextView = findViewById(R.id.bookYearTextView);
        bookDescriptionTextView = findViewById(R.id.bookDescriptionTextView);
        rentButton = findViewById(R.id.rentButton);
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        libraryRecyclerView = findViewById(R.id.libraryRecyclerView);
        adapter = new LibraryAdapter(libraries);
        libraryRecyclerView.setAdapter(adapter);
        libraryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    private void setBookProps(){
        // Retrieve book details from intent
        String bookTitle = getIntent().getStringExtra("title");
        String bookAuthor = getIntent().getStringExtra("author");
        String bookYear = getIntent().getStringExtra("year");
        String bookDescription = getIntent().getStringExtra("description");
        this.bookID = getIntent().getStringExtra("bookID") ;
        // Set book details to TextViews
        bookTitleTextView.setText(bookTitle);
        bookAuthorTextView.setText("Author: " + bookAuthor);
        bookYearTextView.setText("Publication Year: " + bookYear);
        bookDescriptionTextView.setText("Description: " + bookDescription);
    }

    private void setListeners(){
        // Rent button click listener
        rentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedLibIndex = adapter.getSelectedItem() ;
                if(selectedLibIndex != -1){
                    rentAPI.rent(libraries.get(selectedLibIndex).ID, bookID) ;
                }else{
                    Toast.makeText(getApplicationContext(), "Please choose library", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}