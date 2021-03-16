package com.example.goforlunch.users;

import androidx.annotation.Nullable;

public class UserModel implements Comparable<UserModel>{

    private String uid;
    private String username;
    @Nullable
    private String urlPicture;
    private String bookingDate;
    private String restaurantId;

    public UserModel() { } //needed for firebase

    public UserModel(String uid, String username, String urlPicture, String restaurantId, String bookingDate) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
        this.bookingDate = bookingDate;
        this.restaurantId = restaurantId;
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

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    @Override
    public int compareTo(UserModel o) {
        return this.getBookingDate().compareTo(o.getBookingDate());
    }
}
