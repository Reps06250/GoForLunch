package com.example.goforlunch.restaurants;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.goforlunch.restaurants.tools.GetRestaurantsList;
import com.example.goforlunch.restaurants.tools.RestaurantModel;
import com.google.android.gms.maps.model.CameraPosition;
import java.util.ArrayList;
import java.util.List;


public class RestaurantViewModel extends AndroidViewModel implements  GetRestaurantsList.Listeners{

    private Location lastKnowLocation;
    private CameraPosition cameraPosition;
    private MutableLiveData<List<RestaurantModel>> restaurantsListMutableLiveData;
    private List<RestaurantModel> allRestaurantsList = new ArrayList<>();
    private List<RestaurantModel> dBRestaurantsList = new ArrayList<>();
    private GetRestaurantsList getRestaurantsList;
    private boolean asyncTaskOnExecution = false;
    private RestaurantModel restaurant;

    public RestaurantViewModel(@NonNull Application application) {
        super(application);
        restaurantsListMutableLiveData = new MutableLiveData<>();
    }

    // ----------------------------- GET LISTS OF RESTAURANTS ------------------------------

    // Get all near resataurants, launch only one time when we get the location
    public void getAllRestaurantsList() {
        Context context = getApplication().getApplicationContext();
        // Location in needed to know the center of the research,
        // the boolean all to know if we want all near restaurant (true) or a filtred list (false)
        getRestaurantsList = new GetRestaurantsList(this, lastKnowLocation, true, context);
        getRestaurantsList.execute();
    }

    // Get search restaurants list
    public void getFiltredRestaurantsList(String newText) {
        Context context = getApplication().getApplicationContext();
        // boolean asyncTaskOnExecution is use to stop the last asynctask if its running when the text change
        if(asyncTaskOnExecution) {
            getRestaurantsList.cancel(true);
        }
        // text not null, launch the request to filtre with the newText
        if(newText != null && newText.length() != 0){
            asyncTaskOnExecution = true;
            getRestaurantsList = new GetRestaurantsList(this,false, lastKnowLocation,newText, dBRestaurantsList, context);
            getRestaurantsList.execute();
        }
        else{
            // newText is null, show all restaurants
            restaurantsListMutableLiveData.postValue(allRestaurantsList);
        }
    }


    @Override
    public void onPostExecute(List<RestaurantModel> restaurantsList, boolean all, List<RestaurantModel> dbRestaurantList) {
        Log.e("getRestso", "postexe");
        if (all){
            allRestaurantsList = restaurantsList;
            this.dBRestaurantsList = dbRestaurantList;
        }
        restaurantsListMutableLiveData.postValue(restaurantsList);
        asyncTaskOnExecution = false;
    }

    // ------------------------- GETTERS AND SETTERS -------------------------------

    public  MutableLiveData<List<RestaurantModel>> getRestaurantMutableLiveData() {
        return restaurantsListMutableLiveData;
    }
    public void setLastKnownLocation(Location lastKnownLocation) {
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

    public List<RestaurantModel> getdBRestaurantsList() {
        return dBRestaurantsList;
    }

    public void setdBRestaurantsList(List<RestaurantModel> dBRestaurantsList) {
        this.dBRestaurantsList = dBRestaurantsList;
    }

    public RestaurantModel getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(RestaurantModel restaurant) {
        this.restaurant = restaurant;
    }
}