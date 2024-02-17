package com.example.pageflix.interfaces;

import com.example.pageflix.entities.Book;

public interface BookInfoCallback {
    void onBookInfoReceived(Book book);
}