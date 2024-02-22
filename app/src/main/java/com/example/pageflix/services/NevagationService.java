package com.example.pageflix.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NevagationService {
    FirebaseDatabase db ;
    FirebaseAuth fbAuth ;

    DatabaseReference librarianRef, bookLibCountRef, bookTotalCountRef, customerRef, rentalsRef , db_refToRentalsDB;

    String customerID, libID;



}
