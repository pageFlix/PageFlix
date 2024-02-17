package com.example.pageflix.interfaces;

import com.google.firebase.database.DatabaseReference;

public interface CallbackFlag {
    void checkBook(boolean bookFound, DatabaseReference db, int countbook);
}
