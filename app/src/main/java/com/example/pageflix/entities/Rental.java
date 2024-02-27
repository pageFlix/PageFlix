package com.example.pageflix.entities;

public class Rental {
    public String customerID, libraryID, bookID ;
    public long timestamp; // Add timestamp field
    public boolean ifReturned, ifAccept;

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

    public Rental(String libraryID, String customerID, String bookID,long timestamp, boolean ifReturned,boolean ifAccept) {
        this.customerID = customerID;
        this.libraryID = libraryID;
        this.bookID = bookID;
        this.timestamp = timestamp; // Set timestamp
        this.ifReturned = ifReturned;
        this.ifAccept = ifAccept;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    public boolean isIfReturned() {
        return ifReturned;
    }

    public void setIfReturned(boolean ifReturned) {
        this.ifReturned = ifReturned;
    }

    public boolean isIfAccept() {
        return ifAccept;
    }

    public void setIfAccept(boolean ifAccept) {
        this.ifAccept = ifAccept;
    }
}
