package com.example.goforlunch.eventbus;

import com.example.goforlunch.restaurants.tools.RestaurantModel;

public class MyEventBus {

    private RestaurantModel restaurant;
    private boolean fromList;

    public MyEventBus(RestaurantModel restaurant, boolean fromList) {
        this.restaurant = restaurant;
        this.fromList = fromList;
    }

    public RestaurantModel GetMyRestaurant(){
        return restaurant;
    }

    public boolean isFromList() {
        return fromList;
    }
}
