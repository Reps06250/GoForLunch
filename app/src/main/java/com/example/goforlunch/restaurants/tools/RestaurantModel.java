package com.example.goforlunch.restaurants.tools;

import com.example.goforlunch.users.UserModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;

import java.util.Date;
import java.util.List;

public class RestaurantModel {

    public LatLng latLng;
    public String name;
    public String vicinity;
    public String id;
    public String date;
    public int stars;
    public List<String> knownUsersId;
    public List<UserModel> interstedUers;
    public OpeningHours openingHours;
    public String phoneNumber;
    public List<PhotoMetadata> photoMetadata;
    public Place.BusinessStatus businessStatus;

    public RestaurantModel(LatLng latLng, String name, String vicinity, String id, OpeningHours openingHours,
                           String phoneNumber, List<PhotoMetadata> photoMetadatas, Place.BusinessStatus businessStatus, String date, int stars, List<String> knownUsersId, List<UserModel> interstedUsers) {
        this.latLng = latLng;
        this.name = name;
        this.vicinity = vicinity;
        this.id = id;
        this.date = date;
        this.stars = stars;
        this.knownUsersId = knownUsersId;
        this.interstedUers = interstedUsers;
        this.openingHours = openingHours;
        this.phoneNumber = phoneNumber;
        this.photoMetadata = photoMetadatas;
        this.businessStatus = businessStatus;
    }

    public RestaurantModel() {}


    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<String> getknownUsersId() {
        return knownUsersId;
    }

    public void setknownUsersId(List<String> interstedUsersId) {
        this.knownUsersId = interstedUsersId;
    }

    public List<UserModel> getInterstedUers() {
        return interstedUers;
    }

    public void setInterstedUers(List<UserModel> interstedUersName) {
        this.interstedUers = interstedUersName;
    }

    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(OpeningHours openingHours) {
        this.openingHours = openingHours;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<PhotoMetadata> getPhotoMetadata() {
        return photoMetadata;
    }

    public void setPhotoMetadata(List<PhotoMetadata> photoMetadata) {
        this.photoMetadata = photoMetadata;
    }

    public Place.BusinessStatus getBusinessStatus() {
        return businessStatus;
    }

    public void setBusinessStatus(Place.BusinessStatus businessStatus) {
        this.businessStatus = businessStatus;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }
}
