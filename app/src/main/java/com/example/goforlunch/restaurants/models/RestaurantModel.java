package com.example.goforlunch.restaurants.models;

import android.graphics.Bitmap;

import com.example.goforlunch.users.UserModel;
import com.google.android.libraries.places.api.model.Place;

import java.util.List;

public class RestaurantModel {

    public String date;
    public float stars;
    public List<UserModel> interestedUsers;
    public Bitmap bitmap;
    public Place place;

    public RestaurantModel(Place place, String date, int stars, List<UserModel> interestedUsers, Bitmap bitmap) {

        this.date = date;
        this.stars = stars;
        this.interestedUsers = interestedUsers;
        this.bitmap = bitmap;
        this.place = place;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public List<UserModel> getInterestedUsers() {
        return interestedUsers;
    }

    public void setInterestedUsers(List<UserModel> interestedUsers) {
        this.interestedUsers = interestedUsers;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }
}
