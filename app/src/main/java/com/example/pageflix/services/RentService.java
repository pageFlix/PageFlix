package com.example.pageflix.services;
import android.util.Log;

import androidx.annotation.NonNull;
import com.example.pageflix.entities.Rental;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RentService {
    FirebaseDatabase db ;
    FirebaseAuth fbAuth ;

    DatabaseReference librarianRef, bookLibCountRef, bookTotalCountRef, customerRef, rentalsRef ;

    String customerID, libID, bookID ;
    public void rent(String libID, String bookID) {

        initDBSettings(libID, bookID);
        synchronizedUpdate(); ;
    }
    public void initDBSettings(String libID, String bookID){
        db = FirebaseDatabase.getInstance() ;
        fbAuth = FirebaseAuth.getInstance() ;
        customerID = fbAuth.getUid() ;
        this.libID = libID ;
        this.bookID = bookID ;
        librarianRef = db.getReference("Librarian").child(libID).child("BooksID").child(bookID).child("count");
        bookLibCountRef = db.getReference("Books").child(bookID).child("LibraryID").child(libID).child("count");
        bookTotalCountRef = db.getReference("Books").child(bookID).child("count") ;
        rentalsRef = db.getReference("rentals") ;
        customerRef = db.getReference("Customer").child(customerID).child("Books").child(bookID).child("count");

    }


    private void synchronizedUpdate(){
        librarianRef.get().addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                Integer libCount = task1.getResult().getValue(Integer.class);
                bookLibCountRef.get().addOnCompleteListener(task2 -> {
                    if(task2.isSuccessful()) {
                        Integer booksLibCount = task2.getResult().getValue(Integer.class);
                        bookTotalCountRef.get().addOnCompleteListener(task3 -> {
                            if(task3.isSuccessful()) {
                                Integer booksTotal = task3.getResult().getValue(Integer.class) ;
                                customerRef.get().addOnCompleteListener(task4 -> {
                                    if(task4.isSuccessful()) {
                                        Integer customerCount = task4.getResult().getValue(Integer.class);
                                        verifyValues(libCount, booksLibCount, booksTotal, customerCount);
                                    }
                                });
                            }
                        }) ;
                    }
                });
            }
        }) ;
    }

    private void verifyValues(Integer libCount, Integer booksLibCount, Integer booksTotal, Integer customerCount) {
        if(libCount != null && booksLibCount != null && booksTotal != null){
            if(customerCount == null){
                customerCount = 0 ;
            }
            if(libCount > 0 && booksLibCount > 0 && booksTotal > 0 && customerCount >= 0){
                libCount-- ;
                booksLibCount-- ;
                booksTotal-- ;
                customerCount++ ;
                updateValues(libCount, booksLibCount, booksTotal, customerCount) ;
            }
        }
    }

    private void updateValues(Integer libCount, Integer booksLibCount, Integer booksTotal, Integer customerCount) {
        librarianRef.setValue(libCount).addOnCompleteListener(task1 ->{
            if (task1.isSuccessful()){
                bookLibCountRef.setValue(booksLibCount).addOnCompleteListener(task2 -> {
                    if(task2.isSuccessful()){
                        bookTotalCountRef.setValue(booksTotal).addOnCompleteListener(task3 ->{
                            if(task3.isSuccessful()){
                                customerRef.setValue(customerCount).addOnCompleteListener(task4 ->{
                                    updateRental();
                                }) ;
                            }
                        }) ;
                    }
                }) ;
            }
        }) ;
    }

    private void updateRental() {
        DatabaseReference rentalRef = rentalsRef.push();
        Rental rental = new Rental(libID, customerID, bookID);
        rentalRef.setValue(rental).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });
    }
}