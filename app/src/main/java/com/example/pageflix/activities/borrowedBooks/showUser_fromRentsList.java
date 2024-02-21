package com.example.pageflix.activities.borrowedBooks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pageflix.R;
import com.example.pageflix.activities.borrowedBooks.list_ordered_books;
import com.example.pageflix.activities.mainLibrarian;
import com.example.pageflix.entities.Book;
import com.example.pageflix.entities.Rental;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class showUser_fromRentsList extends AppCompatActivity {
    private TextView tvEmail,tvName,tvPhone;
    private String idCustomer, idBook, rentalKey,idLibrarian;
    private  String RENTALS = "rentals";
    private  String CUSTOMER = "Customer";
    private  String LIBRARIAN = "Librarian";
    private  String BOOKS = "Books";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user_from_rents_list);
        init();
        getIntentMain();
    }
    private void init(){
        tvEmail = findViewById(R.id.tvEmail);
        tvName = findViewById(R.id.tvName);
        tvPhone = findViewById(R.id.tvPhone);
        idLibrarian =  FirebaseAuth.getInstance().getCurrentUser().getUid(); //find LibrarianID (unique key)
    }
    private void getIntentMain(){
        Intent i = getIntent();
        if (i != null){
            tvEmail.setText("Email: "+i.getStringExtra("email"));
            tvName.setText("Name: "+i.getStringExtra("fname")+" "+ i.getStringExtra("lname"));
            tvPhone.setText("Phone number: "+i.getStringExtra("phone"));
            idCustomer = i.getStringExtra("idCustomer");
            idBook = i.getStringExtra("idBook");
            rentalKey = i.getStringExtra("rentalKey");

        }
    }
    public void returnBook(View v){
        updateRentalsDB();
        updateLibrarianDB();
        updateCustomerDB();
        updateBooksDB();
        Toast.makeText(getApplicationContext(), "The book was returned", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, mainLibrarian.class);// from Login Customer screen to First screen
        startActivity(intent);
    }
    private void updateRentalsDB(){
        DatabaseReference db =  FirebaseDatabase.getInstance().getReference(RENTALS).child(rentalKey);
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Rental rental = snapshot.getValue(Rental.class);
                if (rental != null){
                rental.setIfReturned(true);
                db.setValue(rental);}
                else {
                    Log.e("updateLibrarianDB", "DataSnapshot is null or does not contain a valid value");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("updateLibrarianDB", "Database error: " + error.getMessage());

            }
        });
    }
    private void updateLibrarianDB(){
        DatabaseReference db =  FirebaseDatabase.getInstance().getReference(LIBRARIAN).child(idLibrarian).child("BooksID").child(idBook).child("count");
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    int count = snapshot.getValue(Integer.class);
                    count++; // Increment count
                    db.setValue(count);
                } else {
                    // Handle the case where the snapshot is null or doesn't contain a valid value
                    Log.e("updateLibrarianDB", "DataSnapshot is null or does not contain a valid value");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("updateLibrarianDB", "Database error: " + error.getMessage());

            }
        });

        }
    private void updateCustomerDB(){
        DatabaseReference db =  FirebaseDatabase.getInstance().getReference(CUSTOMER).child(idCustomer).child("Books").child(idBook).child("count");
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    int count = snapshot.getValue(Integer.class);
                    db.setValue((count - 1));
                }
                else {
                    Log.e("updateLibrarianDB", "DataSnapshot is null or does not contain a valid value");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("updateLibrarianDB", "Database error: " + error.getMessage());

            }
        });
    }
    private void updateBooksDB(){
        DatabaseReference db =  FirebaseDatabase.getInstance().getReference(BOOKS).child(idBook);
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Book book = snapshot.getValue(Book.class);
                db.child("count").setValue((book.getCount() + 1));
                DatabaseReference db_copy = db.child("LibraryID").child(idLibrarian);
                db_copy.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int count = snapshot.child("count").getValue(Integer.class);
                        db_copy.child("count").setValue((count+1));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("updateLibrarianDB", "Database error: " + error.getMessage());

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void backToPreviousScreen(View v){
        Intent intent = new Intent(this, list_ordered_books.class);// from Login Customer screen to First screen
        startActivity(intent);
    }
}