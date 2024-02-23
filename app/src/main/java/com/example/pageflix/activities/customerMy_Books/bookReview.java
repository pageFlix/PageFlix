package com.example.pageflix.activities.customerMy_Books;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.pageflix.R;
import com.example.pageflix.activities.borrowedBooks.list_ordered_books;
import com.example.pageflix.activities.main.mainCustomer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class bookReview extends AppCompatActivity {
    private String  idBook,idCustomer;
    private EditText edReview;
    private RatingBar bookRating;
    private DatabaseReference dbRef;
    private String BOOK_REVIEW = "book_review";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_review);
        init();
    }
    private void init(){
        edReview = findViewById(R.id.edReview);
        bookRating = findViewById(R.id.bookRating); // Initialize RatingBar
        idCustomer =  FirebaseAuth.getInstance().getCurrentUser().getUid(); //find LibrarianID (unique key)
        Intent i = getIntent();
        if (i != null){
            idBook = i.getStringExtra("bookID");
            dbRef = FirebaseDatabase.getInstance().getReference(BOOK_REVIEW).child(idBook);
            addText();
        }
    }
    private void addText(){
        dbRef.child(idCustomer).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    String text = snapshot.child("review").getValue(String.class);
                    edReview.setText(text);
                    Float rating = snapshot.child("rating").getValue(Float.class);
                    if (rating != null) {
                        bookRating.setRating(rating);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void addReview(View v){
        String review = edReview.getText().toString();
        float rating = bookRating.getRating(); // Get the selected rating
        if(!TextUtils.isEmpty(review)) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("review",review);
            hashMap.put("rating", rating);
            dbRef.child(idCustomer).updateChildren(hashMap);
            Toast.makeText(this, "Thanks for the review", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, mainCustomer.class);// from Login Customer screen to First screen
            startActivity(intent);
        }else{
            Toast.makeText(this, "Book review is empty", Toast.LENGTH_SHORT).show();
        }
    }
    public void backToPreviousScreen(View v){
        Intent intent = new Intent(this, customerBooks.class);// from Login Customer screen to First screen
        startActivity(intent);
    }
}