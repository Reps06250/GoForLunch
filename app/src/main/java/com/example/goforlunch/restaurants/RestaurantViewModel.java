package com.example.goforlunch.restaurants;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.goforlunch.R;
import com.example.goforlunch.restaurants.retrofit.NearByApi;
import com.example.goforlunch.restaurants.retrofit.models.NearByApiResponse;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;



public class RestaurantViewModel extends AndroidViewModel implements  GetRestaurantsList.Listeners{

    private Location lastKnowLocation;
    private CameraPosition cameraPosition;
    private MutableLiveData<List<RestaurantModel>> restaurantsListMutableLiveData;
    private List<RestaurantModel> allRestaurantsList = new ArrayList<>();
    private int position;
    private List<RestaurantModel> restaurantsDbList;
    private GetRestaurantsList getRestaurantsList;
    private boolean asyncTaskOnExecution = false;

    public RestaurantViewModel(@NonNull Application application) {
        super(application);
        restaurantsListMutableLiveData = new MutableLiveData<>();
    }

    // ----------------------------- GET LISTS OF RESTAURANTS ------------------------------

    // Get all near resataurants, launch only one time when we get the location
    public void getAllRestaurantsList() {
        Log.e("getRestso", "getall");
        Context context = getApplication().getApplicationContext();
        getRestaurantsList = new GetRestaurantsList(this, lastKnowLocation, true, context);
    }

    // Get search restaurants list
    public void getFiltredRestaurantsList(String newText) {
        // boolean asyncTaskOnExecution is use to stop the last asynctask if the text change
        if(asyncTaskOnExecution) {
            getRestaurantsList.cancel(true);
        }
        // text not null, launch the request to filtre with the newText
        if(newText != null && newText.length() != 0){
            asyncTaskOnExecution = true;
            getRestaurantsList = new GetRestaurantsList(this,false, newText);
        }
        else{
            // newText is null, show all restaurants
            restaurantsListMutableLiveData.postValue(allRestaurantsList);
        }
    }

    @Override
    public void onPostExecute(List<RestaurantModel> restaurantsList, boolean all) {
        Log.e("getRestso", "postexe");
        if (all){
            allRestaurantsList = restaurantsList;
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

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public List<RestaurantModel> getRestaurantsDbList() {
        return restaurantsDbList;
    }

    public void setRestaurantsDbList(List<RestaurantModel> restaurantsDbList) {
        this.restaurantsDbList = restaurantsDbList;
    }
}