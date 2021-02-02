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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLSession;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.ContentValues.TAG;


public class RestaurantViewModel extends AndroidViewModel {

    private Location lastKnowLocation;
    private CameraPosition cameraPosition;
    private MutableLiveData<List<RestaurantModel>> restaurantsListMutableLiveData;
    private List<RestaurantModel> allRestaurantsList = new ArrayList<>();
    private List<RestaurantModel> restaurantsList = new ArrayList<>();
    private List<String> idList = new ArrayList<>();
    private final PlacesClient placesClient;
    private int PROXIMITY_RADIUS = 2500;
    private int position;
    private List<RestaurantModel> restaurantsDbList;



    public RestaurantViewModel(@NonNull Application application) {
        super(application);
        restaurantsListMutableLiveData = new MutableLiveData<>();
        // Construct a PlacesClient
        Places.initialize(getApplication().getApplicationContext(),getApplication().getResources().getString(R.string.key));
        placesClient = Places.createClient(getApplication().getApplicationContext());
    }

    // ----------------------------- GET LISTS OF RESTAURANTS ------------------------------

    // Get all near resataurants, launch only one time when we get the location
    public void getAllRestaurantsList() {
        idList.clear();
        Call<NearByApiResponse> call = getApiService().getNearbyPlaces("restaurant", lastKnowLocation.getLatitude() + "," + lastKnowLocation.getLongitude(), PROXIMITY_RADIUS);
        call.enqueue(new Callback<NearByApiResponse>() {
            @Override
            public void onResponse(Call<NearByApiResponse> call, Response<NearByApiResponse> response) {
                try {
                    for (int i = 0; i < response.body().getResults().size(); i++) {
                        idList.add(response.body().getResults().get(i).getPlaceId());
                    }
                } catch (Exception e) {
                    Log.d("new", "There is an error");
                    e.printStackTrace();
                }
                getDetails(true);
            }
            @Override
            public void onFailure(Call<NearByApiResponse> call, Throwable t) {
                Log.d("new", t.toString());
                t.printStackTrace();
                PROXIMITY_RADIUS += 4000;
            }
        });
    }

    public void getFiltredRestaurantsList(String newText) {
        idList.clear();
        restaurantsList.clear();
        Log.e("filtred", newText);
        Log.e("filtred", "text size : " + newText.length());

        if(newText != null && newText.length() != 0){
            AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
            // Create a RectangularBounds object.
            RectangularBounds bounds = RectangularBounds.newInstance(
                    new LatLng(lastKnowLocation.getLatitude()-0.01, lastKnowLocation.getLongitude()+0.01),
                    new LatLng(lastKnowLocation.getLatitude()+0.01, lastKnowLocation.getLongitude()-0.01));
            FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                    .setLocationBias(bounds)
                    //.setLocationRestriction(bounds)
                    .setOrigin(new LatLng(lastKnowLocation.getLatitude(),lastKnowLocation.getLongitude()))
                    .setTypeFilter(TypeFilter.ESTABLISHMENT)
                    .setSessionToken(token)
                    .setQuery(newText)
                    .build();

            placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
                Log.e("filtred", "get prediction");
                int i = 0;
                for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                    Log.e("filtred", "size = " + response.getAutocompletePredictions().size());
                    Log.e("filtred", String.valueOf(prediction.getDistanceMeters()) + "m");
                    if(prediction.getDistanceMeters() <= 25000) {
                        Log.e("filtred", "add id" + prediction.getPlaceId());
                        idList.add(prediction.getPlaceId());
                    }
                    i++;
                    Log.e("filtred", "i = " + i);
                    if(i == response.getAutocompletePredictions().size()){
                        Log.e("filtred", "lanch getdetails with " + idList.size() + " ids");
                        getDetails(false);
                    }
                }
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    Log.e("filtred", "place not found");
                    Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                }
            });
        }
        else{
            Log.e("filtred", "new text null ou 0, return all");
            restaurantsListMutableLiveData.postValue(allRestaurantsList);
        }
    }

    public void getDetails(Boolean all){
        Log.e("filtred", "getdetails");
        if(idList.isEmpty()){
            Log.e("filtred", "idlist is empty, restaurantsListMutableLiveData.postValue(restaurantsList) = " + restaurantsList.size() + "items");
            restaurantsListMutableLiveData.setValue(restaurantsList);
        }
        else{
            AtomicInteger listSize = new AtomicInteger(idList.size());
            // Specify the fields to return.
            final List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.TYPES,
                    Place.Field.ADDRESS,
                    Place.Field.BUSINESS_STATUS,
                    Place.Field.LAT_LNG,
                    Place.Field.OPENING_HOURS,
                    Place.Field.PHONE_NUMBER,
                    Place.Field.PHOTO_METADATAS,
                    Place.Field.PRICE_LEVEL,
                    Place.Field.RATING,
                    Place.Field.USER_RATINGS_TOTAL,
                    Place.Field.WEBSITE_URI);
            for(String id : idList){
                FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(id, placeFields);
                placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
                    Place place = response.getPlace();
                    if(all){
                        allRestaurantsList.add(new RestaurantModel(place.getLatLng(),place.getName(),place.getAddress(), id, null, null));
                        restaurantsList.add(new RestaurantModel(place.getLatLng(),place.getName(),place.getAddress(), id, null, null));
                    }
                    else{
                        if(place.getTypes().contains(Place.Type.RESTAURANT)){
                            restaurantsList.add(new RestaurantModel(place.getLatLng(),place.getName(),place.getAddress(), id, null, null));
                        }
                    }
                    listSize.getAndDecrement();
                    if(listSize.intValue() == 0){
                        Log.e("filtred", "idlist is NOT empty, restaurantsListMutableLiveData.postValue(restaurantsList) = " + restaurantsList.size() + place.getTypes());
                        restaurantsListMutableLiveData.postValue(restaurantsList);
                    }
                }).addOnFailureListener((exception) -> {
                    if (exception instanceof ApiException) {
                        listSize.getAndDecrement();
                        if(listSize.intValue() == 0){
                            Log.e("filtred", "exception. idlist is NOT empty, restaurantsListMutableLiveData.postValue(restaurantsList) = " + restaurantsList.size() + "items");
                            restaurantsListMutableLiveData.postValue(restaurantsList);
                        }
                    }
                });
//            FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(id, placeFields);
//            Task<FetchPlaceResponse> response = placesClient.fetchPlace(placeRequest);
//            try {
//                Tasks.await(response);
//                if(response.isComplete()){
//                    Place place= response.getResult().getPlace();
//                    restaurantsList.add(new Restaurant(place.getLatLng(),place.getName(),place.getAddress()));
//                }
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            }
        }

//        restaurantMutableLiveData.postValue(restaurantsList);
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
}