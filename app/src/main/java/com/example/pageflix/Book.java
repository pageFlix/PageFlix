package com.example.pageflix;

import com.google.firebase.database.Exclude;

public class Book {
    public String title, author, category, year, description;
    public int count;
    public Book() {
    }
    public Book(String author, String category, String year, String description, String title, int count) {
        this.author = author;
        this.category = category;
        this.year = year;
        this.description = description;
        this.title = title;
        this.count = count;
    }

    public Book(String title, String author, String year, int count) {
        this.title = title;
        this.author = author;
        this.year = year;
        this.count = count;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getCategory() {
        return category;
    }

    public String getYear() {
        return year;
    }

    public String getDescription() {
        return description;
    }

    public int getCount() {
        return count;
    }
}

