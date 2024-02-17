package com.example.pageflix;

import com.example.pageflix.entities.Book;

public interface BookInfoCallback {
    void onBookInfoReceived(Book book);
}