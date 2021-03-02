package com.example.goforlunch.restaurants.tools;

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
    public List<String> interstedUsersId;
    public List<String> interstedUersName;
    public OpeningHours openingHours;
    public String phoneNumber;
    public List<PhotoMetadata> photoMetadata;
    public Place.BusinessStatus businessStatus;

    public RestaurantModel(LatLng latLng, String name, String vicinity, String id, OpeningHours openingHours,
                           String phoneNumber, List<PhotoMetadata> photoMetadatas, Place.BusinessStatus businessStatus, String date, List<String> interstedUsersId, List<String> interstedUsersName) {
        this.latLng = latLng;
        this.name = name;
        this.vicinity = vicinity;
        this.id = id;
        this.date = date;
        this.interstedUsersId = interstedUsersId;
        this.interstedUersName = interstedUsersName;
        this.openingHours = openingHours;
        this.phoneNumber = phoneNumber;
        this.photoMetadata = photoMetadatas;
        this.businessStatus = businessStatus;
    }


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

    public List<String> getInterstedUsersId() {
        return interstedUsersId;
    }

    public void setInterstedUsersId(List<String> interstedUsersId) {
        this.interstedUsersId = interstedUsersId;
    }

    public List<String> getInterstedUersName() {
        return interstedUersName;
    }

    public void setInterstedUersName(List<String> interstedUersName) {
        this.interstedUersName = interstedUersName;
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
}
