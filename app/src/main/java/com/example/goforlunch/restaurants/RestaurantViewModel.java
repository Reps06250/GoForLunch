package com.example.goforlunch.restaurants;

import android.app.Application;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.goforlunch.R;
import com.example.goforlunch.restaurants.retrofit.NearByApi;
import com.example.goforlunch.restaurants.retrofit.models.NearByApiResponse;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.ContentValues.TAG;


public class RestaurantViewModel extends AndroidViewModel implements  GetRestaurantsDetails.Listeners{

    private Location lastKnowLocation;
    private CameraPosition cameraPosition;
    private MutableLiveData<List<RestaurantModel>> restaurantsListMutableLiveData;
    private List<RestaurantModel> allRestaurantsList = new ArrayList<>();
    private List<String> idList = new ArrayList<>();
    private final PlacesClient placesClient;
    private int PROXIMITY_RADIUS = 1000;
    private int position;
    private List<RestaurantModel> restaurantsDbList;
    private GetRestaurantsDetails getRestaurantsDetails;



    public RestaurantViewModel(@NonNull Application application) {
        super(application);
        restaurantsListMutableLiveData = new MutableLiveData<>();
        // Construct a PlacesClient
        Places.initialize(getApplication().getApplicationContext(),getApplication().getResources().getString(R.string.key));
        placesClient = Places.createClient(getApplication().getApplicationContext());
        getRestaurantsDetails = new GetRestaurantsDetails(this, placesClient);
    }

    // ----------------------------- GET LISTS OF RESTAURANTS ------------------------------

    // Get all near resataurants, launch only one time when we get the location
    public void getAllRestaurantsList() {
        Call<NearByApiResponse> call = getApiService().getNearbyPlaces("restaurant", lastKnowLocation.getLatitude() + "," + lastKnowLocation.getLongitude(), PROXIMITY_RADIUS);
        call.enqueue(new Callback<NearByApiResponse>() {
            @Override
            public void onResponse(Call<NearByApiResponse> call, Response<NearByApiResponse> response) {
                try {
                    for (int i = 0; i < response.body().getResults().size(); i++) {
                        // create a list with all the restaurants ids
                        idList.add(response.body().getResults().get(i).getPlaceId());
                    }
                } catch (Exception e) {
                    Log.d("new", "There is an error");
                    e.printStackTrace();
                }
                // launch getDetails to have details of all restaurants in the area and add them in a restaurantsList
                // boolean is use to know if getails is execute from getAllRestaurantsList() or getFiltredRestaurantsList()
                getDetails(true);
                Log.e("getRestso", "launck getdetails true " + idList.size() +" ids");
            }
            @Override
            public void onFailure(Call<NearByApiResponse> call, Throwable t) {
                Log.d("new", t.toString());
                t.printStackTrace();
            }
        });
    }

    public void getFiltredRestaurantsList(String newText) {
        // launch from main activity with the query text
        idList.clear();
        // text not null, launch the request to filtre with the newText
        if(newText != null && newText.length() != 0){
            // text not null, launch the request to filtre with the newText
            AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
            // Create a RectangularBounds object.
            LatLng center = new LatLng(lastKnowLocation.getLatitude(), lastKnowLocation.getLongitude());
            RectangularBounds bounds = RectangularBounds.newInstance(
                    toBounds(center, PROXIMITY_RADIUS).southwest,
                    toBounds(center, PROXIMITY_RADIUS).northeast);
            FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                    //.setLocationBias(bounds)
                    .setLocationRestriction(bounds)
                    .setOrigin(new LatLng(lastKnowLocation.getLatitude(),lastKnowLocation.getLongitude()))
                    .setTypeFilter(TypeFilter.ESTABLISHMENT)
                    .setSessionToken(token)
                    .setQuery(newText)
                    .build();

            placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
                int i = 0;
                for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                    if(prediction.getDistanceMeters() <= PROXIMITY_RADIUS) {
                        idList.add(prediction.getPlaceId());
                    }
                    i++;
                    if(i == response.getAutocompletePredictions().size()){
                        getDetails(false);
                        Log.e("getRestso", "launch getdetails false");
                    }
                }
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                }
            });
        }
        else{
            // newText is null, show all restaurants
            restaurantsListMutableLiveData.postValue(allRestaurantsList);
        }
    }

    public LatLngBounds toBounds(LatLng center, int radiusInMeters) {
        double distanceFromCenterToCorner = radiusInMeters * Math.sqrt(2.0);
        LatLng southwestCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0);
        LatLng northeastCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0);
        return new LatLngBounds(southwestCorner, northeastCorner);
    }

    private RestaurantModel checkInDbRestaurantsList(String id){
        for(RestaurantModel restaurant : restaurantsDbList){
            if(restaurant.getId().equals(id)){
                return restaurant;
            }
        }
        return null;
    }


    // --------------------------- INSTANCIATE RETROFIT ----------------------------

    public NearByApi getApiService() {
        NearByApi nearByApi = null;
        if (nearByApi == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().retryOnConnectionFailure(true).readTimeout(80, TimeUnit.SECONDS).connectTimeout(80, TimeUnit.SECONDS).addInterceptor(interceptor).build();

            Retrofit retrofit = new Retrofit.Builder().baseUrl("https://maps.googleapis.com/maps/").addConverterFactory(getApiConvertorFactory()).client(client).build();

            nearByApi = retrofit.create(NearByApi.class);
            return nearByApi;
        } else {
            return nearByApi;
        }
    }

    private static GsonConverterFactory getApiConvertorFactory() {return GsonConverterFactory.create();}


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

    @Override
    public void onPostExecute(List<RestaurantModel> restaurantsList, boolean all) {
        Log.e("getRestso", "postexe");
        if (all){
            allRestaurantsList = restaurantsList;
        }
        restaurantsListMutableLiveData.postValue(restaurantsList);
    }

    public void getDetails(boolean all){
        Log.e("getRestso", "launck getdetails");
        getRestaurantsDetails.GetDetails(idList, all);
    }
}