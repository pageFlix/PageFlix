package com.example.pageflix.activities.customer_activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pageflix.R;
import com.example.pageflix.activities.MapActivity;
import com.example.pageflix.activities.customerMy_Books.customerBooks;
import com.example.pageflix.entities.Review;
import com.example.pageflix.services.RentService;
import com.example.pageflix.adapters.LibraryAdapter;
import com.example.pageflix.entities.Library;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BookPopupActivity extends Activity {

    //----- Views variables -----
    private TextView bookTitleTextView, bookAuthorTextView, bookYearTextView, bookCategoryTextView, bookDescriptionTextView;
    private RecyclerView libraryRecyclerView;
    private Button rentButton, reviewsButton;
    private RatingBar bookRatingBar ;
    private LibraryAdapter adapter ;
    private RentService rentService;

    //----- Data holders -----
    private List<Library> libraries;
    private List<Review> bookReviews ;

    //----- Primitives -----
    private float rating ;
    private String bookID ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_popup);
        libraries = new ArrayList<>();
        bookReviews = new ArrayList<>() ;
        this.rentService = new RentService() ;
        initViews();
        setBookProps();
        getLibrariesWithBook();
        getReviews();
        setListeners();
    }

    /** Adding each of the libraries containing the selected book.
     Libs which do not contain the book will be not added to the list */
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
                            //----- Only if the book is in the current Library, then ad the library -----
                            //----- to the list.                                                    -----
                            if(countValue > 0){
                                getAndAddToList(libIDsSnap.getKey());
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

    /** Gets a library by id, and adding it to the list of the libraries that hold the current book */
    private void getAndAddToList(String id){
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
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        }) ;
    }

    /** Gets all of the reviews of the current book and its average rating */
    private void getReviews(){
        DatabaseReference reviewsRef = FirebaseDatabase.getInstance().getReference("book_review") ;
        reviewsRef.child(bookID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int numOfReviews = 0 ;
                if (snapshot.exists()){
                    for (DataSnapshot reviewSnap: snapshot.getChildren()) {
                        Review review = reviewSnap.getValue(Review.class);
                        getAndSetCustomerName(review, reviewSnap.getKey());
                        bookReviews.add(review) ;
                        rating += review.rating ;
                        numOfReviews++ ;
                    }
                    if(numOfReviews == 0){
                        bookRatingBar.setRating(0);
                    }else {
                        rating /= numOfReviews;
                        bookRatingBar.setRating(rating);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /** Fits to each review the corresponding customer name */
    private void getAndSetCustomerName(Review review, String customerID){
        DatabaseReference customersRef = FirebaseDatabase.getInstance().getReference("Customer") ;
        customersRef.child(customerID).child("firstName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    review.customerName = snapshot.getValue(String.class) ;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /** Initialize the views variables */
    private void initViews(){
        bookTitleTextView = findViewById(R.id.bookTitleTextView);
        bookAuthorTextView = findViewById(R.id.bookAuthorTextView);
        bookYearTextView = findViewById(R.id.bookYearTextView);
        bookDescriptionTextView = findViewById(R.id.bookDescriptionTextView);
        bookCategoryTextView = findViewById(R.id.bookCategoryTextView) ;
        rentButton = findViewById(R.id.rentButton);
        reviewsButton = findViewById(R.id.viewReviewsButton) ;
        bookRatingBar = findViewById(R.id.bookRatingBar) ;
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        libraryRecyclerView = findViewById(R.id.libraryRecyclerView);
        adapter = new LibraryAdapter(libraries);
        libraryRecyclerView.setAdapter(adapter);
        libraryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /** Showing the book details in their corresponding elements */
    private void setBookProps(){
        // Retrieve book details from intent
        String bookTitle = getIntent().getStringExtra("title");
        String bookAuthor = getIntent().getStringExtra("author");
        String bookYear = getIntent().getStringExtra("year");
        String bookDescription = getIntent().getStringExtra("description");
        String bookCategory = getIntent().getStringExtra("category") ;
        this.bookID = getIntent().getStringExtra("bookID") ;
        // Set book details to TextViews
        bookTitleTextView.setText(bookTitle);
        bookAuthorTextView.setText("Author: " + bookAuthor);
        bookYearTextView.setText("Publication Year: " + bookYear);
        bookCategoryTextView.setText("Category: " + bookCategory);
        bookDescriptionTextView.setText("Description: " + bookDescription);
    }

    private void setListeners() {
        // -----On rent button pressed-----
        rentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedLibIndex = adapter.getSelectedItem();
                if (selectedLibIndex != -1) {
                    validateAndRent(libraries.get(selectedLibIndex).ID, selectedLibIndex) ;
                } else {
                    Toast.makeText(getApplicationContext(), "Please choose a library", Toast.LENGTH_SHORT).show();
                }
            }
        }) ;

        //----- On reviews button pressed, open the reviews Activity -----
        reviewsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BookPopupActivity.this, ReviewsActivity.class);
                intent.putExtra("reviews", (Serializable) bookReviews);
                startActivity(intent);
            }
        });
    }

    /** When rent occurs, navigate by google maps to the library address */
    private void navigateToMapActivity(Library library) {
        //sending the library adrres to MapActivity class to start the navigation
        Intent intent = new Intent(BookPopupActivity.this, MapActivity.class);
        // Pass address data to MapActivity
        intent.putExtra("city", library.getCity());
        intent.putExtra("street", library.getStreet());
        intent.putExtra("number", library.getNumber());
        startActivity(intent);
    }

    /** Validates the user isn't present in the blocked list of the library it has chosen
     *  and initiates the rent operation.
     * @param libID - The id of the library chosen by the user
     * @param selectedLibIndex - The selected library index in the libs list
     */
    private void validateAndRent(String libID, int selectedLibIndex) {
        DatabaseReference blockedRef = FirebaseDatabase.getInstance().getReference("Blocklist") ;
        String uid = FirebaseAuth.getInstance().getUid();
        blockedRef.child(libID).child(uid).child("block").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isBlocked = snapshot.getValue(Boolean.class) ;
                if(isBlocked != null){
                    if(isBlocked){
                        Toast.makeText(BookPopupActivity.this, "You have been blocked by the library, please contact them for further information.", Toast.LENGTH_LONG).show() ;
                        return ;
                    }
                }
                rentService.rent(libID, bookID);
                // starting dialogAlert for navigation to library
                AlertDialog.Builder builder = new AlertDialog.Builder(BookPopupActivity.this);
                builder.setMessage("Order has been made successfully, Do you want to start navigation to " + libraries.get(selectedLibIndex).getLibraryName())
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            // if the yes button was pressed then google maps will start with the
                            // customer location and the library address
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Pass the address data to MapActivity
                                navigateToMapActivity(libraries.get(selectedLibIndex));
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(BookPopupActivity.this, customerBooks.class);
                                startActivity(intent);
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show() ;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}

