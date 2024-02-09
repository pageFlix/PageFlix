package com.example.pageflix;

public class Library {
    String LibraryName, CellNumber, City, Street, Number, email, ID;

    public Library(String libraryName, String cellNumber, String city, String street, String number, String email) {
        LibraryName = libraryName;
        CellNumber = cellNumber;
        City = city;
        Street = street;
        Number = number;
        this.email = email;
    }
    public Library(){

    }

    public String getLibraryName() {
        return LibraryName;
    }

    public void setLibraryName(String libraryName) {
        LibraryName = libraryName;
    }

    public String getCellNumber() {
        return CellNumber;
    }

    public void setCellNumber(String cellNumber) {
        CellNumber = cellNumber;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getStreet() {
        return Street;
    }

    public void setStreet(String street) {
        Street = street;
    }

    public String getNumber() {
        return Number;
    }

    public void setNumber(String number) {
        Number = number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Library{" +
                "LibraryName='" + LibraryName + '\'' +
                ", CellNumber='" + CellNumber + '\'' +
                ", City='" + City + '\'' +
                ", Street='" + Street + '\'' +
                ", Number='" + Number + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}