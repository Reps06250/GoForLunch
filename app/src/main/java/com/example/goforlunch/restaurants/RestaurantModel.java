package com.example.goforlunch.restaurants;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;
import java.util.List;

public class RestaurantModel {

    public LatLng latLng;
    public String name;
    public String vicinity;
    public String id;
    public String date;
    List<String> interstedUsersId;

    public RestaurantModel(LatLng latLng, String name, String vicinity, String id, String date, List<String> interstedUsersId) {
        this.latLng = latLng;
        this.name = name;
        this.vicinity = vicinity;
        this.id = id;
        this.date = date;
        this.interstedUsersId = interstedUsersId;
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
}
