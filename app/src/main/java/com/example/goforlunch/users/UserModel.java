package com.example.goforlunch.users;

import androidx.annotation.Nullable;

import com.example.goforlunch.restaurants.tools.RestaurantModel;

import java.util.Date;

public class UserModel implements Comparable<UserModel>{

    private String uid;
    private String username;
    @Nullable
    private String urlPicture;
    private String bookingDate;
    private RestaurantModel restaurant;

    public UserModel() { } //needed for firebase

    public UserModel(String uid, String username, String urlPicture, RestaurantModel restaurant, String bookingDate) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
        this.bookingDate = bookingDate;
        this.restaurant = restaurant;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Nullable
    public String getUrlPicture() {
        return urlPicture;
    }

    public void setUrlPicture(@Nullable String urlPicture) {
        this.urlPicture = urlPicture;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public RestaurantModel getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(RestaurantModel restaurant) {
        this.restaurant = restaurant;
    }

    @Override
    public int compareTo(UserModel o) {
        return this.getBookingDate().compareTo(o.getBookingDate());
    }
}
