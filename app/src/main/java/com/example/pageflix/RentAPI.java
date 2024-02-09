package com.example.pageflix;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
public class RentAPI {
    FirebaseDatabase db ;
    FirebaseAuth fbAuth ;
    DatabaseReference libsRef, booksRef, customersRef, rentalsRef ;

    public void initDBSettings(){
        db = FirebaseDatabase.getInstance() ;
        fbAuth = FirebaseAuth.getInstance() ;
        libsRef = db.getReference("Librarian") ;
        booksRef = db.getReference("Books") ;
        customersRef = db.getReference("Customer") ;
        rentalsRef = db.getReference().child("rentals") ;
    }

    public void rent(String libID, String bookID) {
        Log.d("shit", "renting shit!!! " + libID + " , " + bookID);
        initDBSettings();
        String customerID = fbAuth.getUid() ;
        updateLib(libID, bookID) ;
        updateBook(libID, bookID);
        updateCustomer(bookID, customerID);
        updateRental(libID, bookID, customerID);

    }
    private void updateLib(String libID, String bookID){
        DatabaseReference librarianRef = libsRef.child(libID).child("BooksID").child(bookID).child("count");
        librarianRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer value = snapshot.getValue(Integer.class) ;
                value-- ;
                librarianRef.setValue(value).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.v("shit", "lib updated!!") ;

                    }
                }) ;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void updateBook(String libID, String bookID){
        DatabaseReference bookLibCountRef = booksRef.child(bookID).child("LibraryID").child(libID).child("count");
        DatabaseReference bookTotalCountRef = booksRef.child(bookID).child("count") ;
        bookLibCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer value = snapshot.getValue(Integer.class) ;
                value-- ;
                bookLibCountRef.setValue(value).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.v("shit", "book updated!!") ;

                    }
                }) ;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        bookTotalCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer value = snapshot.getValue(Integer.class) ;
                value-- ;
                bookTotalCountRef.setValue(value).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.v("shit", "book total updated!!") ;

                    }
                }) ;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void updateCustomer(String bookID, String customerID){
        DatabaseReference customerRef = customersRef.child(customerID).child("Books").child(bookID).child("count");
        customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer value = snapshot.getValue(Integer.class) ;
                if(value == null){
                    value = 1 ;
                }else {
                    value++;
                }
                customerRef.setValue(value).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.v("shit", "customer updated!!") ;
                    }
                }) ;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void updateRental(String libID, String bookID, String customerID){
        DatabaseReference rentalRef = rentalsRef.push() ;
        Rental rental = new Rental(libID, customerID, bookID) ;
        rentalRef.setValue(rental).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.v("shit", "rental updated!!") ;
            }
        }) ;
    }
}
