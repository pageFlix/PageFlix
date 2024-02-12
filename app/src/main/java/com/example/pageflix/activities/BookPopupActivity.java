package com.example.pageflix.activities;

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

import com.example.pageflix.R;
import com.example.pageflix.services.RentService;
import com.example.pageflix.adapters.LibraryAdapter;
import com.example.pageflix.entities.Library;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BookPopupActivity extends Activity {

    private TextView bookTitleTextView, bookAuthorTextView, bookYearTextView, bookDescriptionTextView;
    private RecyclerView libraryRecyclerView;
    private Button rentButton;

    private String bookID ;
    private LibraryAdapter adapter ;
    private List<Library> libraries;
    private RentService rentService;

    private List<String> libIDs ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_book_details);
        libraries = new ArrayList<>();
        libIDs = new ArrayList<>() ;
        this.rentService = new RentService() ;
        initViews();
        setBookProps();
        getLibrariesWithBook();
        setListeners();
    }

    //-----Adding each of the libraries containing the selected book.      -----//
    //-----Libs which do not contain the book will be not added to the list-----//
    public void getLibrariesWithBook() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference booksRef = database.getReference("Books");
        booksRef.child(bookID).child("LibraryID").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot libIDsSnap : dataSnapshot.getChildren()) {
                    // Access the value of libIDsSnap
                    Object value = libIDsSnap.getValue();

                    if (value instanceof Map) {
                        // If the value is a Map, try to extract the "count" value
                        Object countObject = ((Map<?, ?>) value).get("count");
                        if (countObject instanceof Long) {
                            // Convert to Integer if necessary
                            Integer countValue = ((Long) countObject).intValue();
                            if(countValue > 0){
                                gertAndAddToList(libIDsSnap.getKey());
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Error", "Failed to read value.", databaseError.toException());            }
        });



    }

    private void gertAndAddToList(String id){
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
                    rentService.rent(libraries.get(selectedLibIndex).ID, bookID) ;
                }else{
                    Toast.makeText(getApplicationContext(), "Please choose library", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}