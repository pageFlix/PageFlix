package com.example.pageflix;

public class Rental {
    public String customerID, libraryID, bookID ;

    public Rental() {

    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getLibraryID() {
        return libraryID;
    }

    public void setLibraryID(String libraryID) {
        this.libraryID = libraryID;
    }

    public String getBookID() {
        return bookID;
    }

    public void setBookID(String bookID) {
        this.bookID = bookID;
    }

    public Rental(String libraryID, String customerID, String bookID) {
        this.customerID = customerID;
        this.libraryID = libraryID;
        this.bookID = bookID;
    }
}
