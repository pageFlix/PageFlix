package com.example.pageflix.entities;

import com.google.firebase.database.Exclude;

import java.util.Comparator;

public class Book {
    public String title, author, category, year, description, ID;
    public int count , age;
    public Book() {
    }
    public Book(String title, String author, String year, int count, String category, String description,int age) {
        this.author = author;
        this.category = category;
        this.year = year;
        this.description = description;
        this.title = title;
        this.count = count;
        this.age = age;
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

    @Override
    public String toString() {
        return "Book{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", category='" + category + '\'' +
                ", year='" + year + '\'' +
                ", description='" + description + '\'' +
                ", count=" + count +
                '}';
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    // Comparator for sorting by title
    public static class TitleComparator implements Comparator<Book> {
        @Override
        public int compare(Book b1, Book b2) {
            return b1.getTitle().compareToIgnoreCase(b2.getTitle());
        }
    }

    // Comparator for sorting by publication year
    public static class PublicationYearComparator implements Comparator<Book> {
        @Override
        public int compare(Book b1, Book b2) {
            return Integer.compare(Integer.parseInt(b1.getYear()), Integer.parseInt(b2.getYear()));
        }
    }
    public static class CategoryComparator implements Comparator<Book> {
        @Override
        public int compare(Book b1, Book b2) {
            return b1.getCategory().compareToIgnoreCase(b2.getCategory()) ;
        }
    }
}

