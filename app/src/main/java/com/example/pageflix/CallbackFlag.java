package com.example.pageflix;

import com.google.firebase.database.DatabaseReference;

public interface CallbackFlag {
    void checkBook(boolean bookFound, DatabaseReference db);
}
