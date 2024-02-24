package com.example.pageflix.entities;

import com.example.pageflix.R;

import java.io.Serializable;

public class Review implements Serializable {
    public int rating ;
    public String review, customerName ;

    public Review(){
    }
    public Review(int rating, String review){
        this.rating = rating ;
        this.review = review ;
    }

    public String getReview() {
        return review;
    }

    public int getRating() {
        return rating;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
