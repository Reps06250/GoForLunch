package com.example.goforlunch.restaurants;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.goforlunch.restaurants.models.RestaurantModel;
import com.example.goforlunch.restaurants.tools.GetRestaurantsList;
import com.example.goforlunch.restaurants.models.DbRestaurantModel;
import com.example.goforlunch.users.UserModel;
import com.google.android.gms.maps.model.CameraPosition;
import java.util.ArrayList;
import java.util.List;


public class RestaurantViewModel extends AndroidViewModel implements  GetRestaurantsList.Listeners{

    private Location lastKnowLocation = null;
    private CameraPosition cameraPosition;
    private MutableLiveData<List<RestaurantModel>> restaurantsListMutableLiveData;
    private List<RestaurantModel> allRestaurantsList = new ArrayList<>();
    private List<DbRestaurantModel> dbRestaurantsList = new ArrayList<>();
    private GetRestaurantsList getRestaurantsList;
    private boolean asyncTaskOnExecution = false;
    private RestaurantModel restaurant;
    private UserModel user;

    public RestaurantViewModel(@NonNull Application application) {
        super(application);
        restaurantsListMutableLiveData = new MutableLiveData<>();
    }

    // ----------------------------- GET LISTS OF RESTAURANTS ------------------------------

    // Get all near resataurants, launch only one time when we get the location
    public void getAllRestaurantsList() {
        Log.e("restoView", "getAll");
        Context context = getApplication().getApplicationContext();
        // Location in needed to know the center of the research,
        // the boolean all to know if we want all near restaurant (true) or a filtred list (false)
        getRestaurantsList = new GetRestaurantsList(this, lastKnowLocation, "map", context);
        getRestaurantsList.execute();
    }

    // Get search restaurants list
    public void getFiltredRestaurantsList(String newText) {
        Context context = getApplication().getApplicationContext();
        // boolean asyncTaskOnExecution is use to stop the last asynctask if its running when the text change
        if(asyncTaskOnExecution) {
            Log.e("emptyList", "getFiltredRestaurantsList cancel");
            getRestaurantsList.cancel(true);
        }
        // text not null, launch the request to filtre with the newText
        if(newText != null && newText.length() != 0){
            Log.e("emptyList", "getFiltredRestaurantsList " + newText);
            asyncTaskOnExecution = true;
            getRestaurantsList = new GetRestaurantsList(this,"searchBar", lastKnowLocation,newText, dbRestaurantsList, context);
            getRestaurantsList.execute();
        }
        else{
            // newText is null, show all restaurants
            Log.e("emptyList", "getFiltredRestaurantsList all " + allRestaurantsList.size());
            restaurantsListMutableLiveData.postValue(allRestaurantsList);
        }
    }


    @Override
    public void onPostExecute(List<RestaurantModel> restaurantsList, String from, List<DbRestaurantModel> dbRestaurantsList) {
        Log.e("restoView", "postexe" + from);
        if (from.equals("map")){
            this.allRestaurantsList = restaurantsList;
            this.dbRestaurantsList = dbRestaurantsList;
        }
        restaurantsListMutableLiveData.postValue(restaurantsList);
        asyncTaskOnExecution = false;
    }

    // ------------------------- GETTERS AND SETTERS -------------------------------

    public  MutableLiveData<List<RestaurantModel>> getRestaurantMutableLiveData() {
        return restaurantsListMutableLiveData;
    }
    public void setLastKnownLocation(Location lastKnownLocation) {
        Log.e("MapView", "setLastKnownLocation");
        this.lastKnowLocation = lastKnownLocation;
    }
    public Location getLastKnowLocation() {
        return lastKnowLocation;
    }

    public CameraPosition getCameraPosition() {
        return cameraPosition;
    }

    public void setCameraPosition(CameraPosition cameraPosition) {
        this.cameraPosition = cameraPosition;
    }

    public List<DbRestaurantModel> getDbRestaurantsList() {
        return dbRestaurantsList;
    }

    public void setAllRestaurantsList(List<RestaurantModel> allRestaurantsList) {
        this.allRestaurantsList = allRestaurantsList;
    }

    public void setDbRestaurantsList(List<DbRestaurantModel> dbRestaurantsList) {
        this.dbRestaurantsList = dbRestaurantsList;
    }

    public RestaurantModel getRestaurant() {
        Log.e("restoView", "get resto " + restaurant.getPlace().getName());
        return restaurant;
    }

    public void setRestaurant(RestaurantModel restaurant) {
        Log.e("restoView", "set resto " + restaurant.getPlace().getName());
        this.restaurant = restaurant;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }
}