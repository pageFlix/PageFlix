package com.example.pageflix.activities.customer_activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.pageflix.R;
import com.example.pageflix.activities.main.mainCustomer;
import com.example.pageflix.activities.main.mainLibrarian;
import com.example.pageflix.adapters.ReviewAdapter;
import com.example.pageflix.entities.Book;
import com.example.pageflix.entities.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class reviews_list extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ReviewAdapter adapter;
    private List<Review> reviewList;
    private DatabaseReference dbReview,dbBook;
    private String customerID;
    private String BOOK_REVIEW = "book_review";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews_list);
        init();
        getDataFromDB();
    }

    public void init(){
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewList = new ArrayList<>();
        adapter = new ReviewAdapter(reviewList);
        recyclerView.setAdapter(adapter);
        customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbReview = FirebaseDatabase.getInstance().getReference(BOOK_REVIEW);
        dbBook = FirebaseDatabase.getInstance().getReference("Books");

    }
    private void getDataFromDB() {
        ValueEventListener vListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviewList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String bookKey = ds.getKey();
                    if (bookKey != null) {
                        Review review = snapshot.child(bookKey).child(customerID).getValue(Review.class);
                        if (review != null) {
                            dbBook.child(bookKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Book book = snapshot.getValue(Book.class);
                                    if (book!= null) {
                                        review.customerName = book.getTitle();
                                        reviewList.add(review);
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        dbReview.addValueEventListener(vListener);
    }

    public void backToPreviousScreen(View v) {
        Intent intent = new Intent(this, mainCustomer.class);
        startActivity(intent);
    }
}
