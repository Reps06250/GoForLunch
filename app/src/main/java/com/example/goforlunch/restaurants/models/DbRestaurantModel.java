package com.example.goforlunch.restaurants.models;

import com.example.goforlunch.users.UserModel;

import java.util.List;

public class DbRestaurantModel {

    public String date;
    public int stars;
    public List<UserModel> interestedUsers;
    public String id;

    public DbRestaurantModel(String id, String date, int stars, List<UserModel> interestedUsers) {
        this.date = date;
        this.stars = stars;
        this.interestedUsers = interestedUsers;
        this.id = id;
    }

    public DbRestaurantModel() {}

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getStars() {
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
