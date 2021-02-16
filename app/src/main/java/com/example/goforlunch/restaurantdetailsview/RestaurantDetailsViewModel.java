package com.example.goforlunch.restaurantdetailsview;

import android.annotation.SuppressLint;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.goforlunch.eventbus.MyEventBus;
import com.example.goforlunch.restaurants.RestaurantViewModel;
import com.example.goforlunch.restaurants.tools.RestaurantModel;
import com.example.goforlunch.users.UserHelper;
import com.example.goforlunch.users.UserModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import de.greenrobot.event.EventBus;

class RestaurantDetailsViewModel extends AndroidViewModel {

    private RestaurantModel restaurant;
    private String dateString;
    private UserModel user;
    private boolean fromList;


    public RestaurantDetailsViewModel(@NonNull Application application) {
        super(application);
        EventBus.getDefault().register(this);
        Date date = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        dateString = formatter.format(date);
        GetCurrentUser();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(MyEventBus event){
        restaurant = event.GetMyRestaurant();
        fromList = event.isFromList();
    }

    void GetCurrentUser(){
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        UserHelper.getUser(userId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                user = documentSnapshot.toObject(UserModel.class);
                if(!fromList){
                    restaurant = user.getRestaurant();
                }
            }
        });
    }

    public RestaurantModel getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(RestaurantModel restaurant) {
        this.restaurant = restaurant;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }
}
