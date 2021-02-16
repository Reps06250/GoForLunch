package com.example.goforlunch.restaurants.tools;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.goforlunch.restaurants.tools.retrofit.models.NearByApiResponse;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.SphericalUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

import static android.content.ContentValues.TAG;

public class GetRestaurantsList extends AsyncTask<Void, Void, List<RestaurantModel>>{

    public interface Listeners {
        void onPostExecute(List<RestaurantModel> restaurantsList, boolean all, List<RestaurantModel> dbRestaurantList);
    }
    private List<RestaurantModel> dbRestaurantList;
    private PlacesClient placesClient;
    private boolean all;
    private String newText;
    private final WeakReference<Listeners> callback;
    private Location lastKnowLocation;
    private final int PROXIMITY_RADIUS = 1000;
    private List<RestaurantModel> restaurantsList = new ArrayList<>();
    private List<String> idList = new ArrayList<>();


    public GetRestaurantsList(Listeners callback, Location lastKnowLocation, boolean all, Context context){
        Log.e("getRestso", "async constructor");
        this.callback = new WeakReference<>(callback);
        this.all = all;
        this.lastKnowLocation =lastKnowLocation;
        // Construct a PlacesClient
        Places.initialize(context,"AIzaSyBCV2Ijz4s1L-iD5oNIYnkedASbqqkKujE");
        placesClient = Places.createClient(context);
    }

    public GetRestaurantsList(Listeners callback, boolean all, Location lastKnowLocation, String newText, List<RestaurantModel> dbRestaurantList){
        Log.e("getRestso", "async constructor");
        this.callback = new WeakReference<>(callback);
        this.all = all;
        this.newText = newText;
        this.lastKnowLocation =lastKnowLocation; //Todo vérifier si je peux supprimer la ligne
        this.dbRestaurantList = dbRestaurantList;
    }

    @Override
    protected List<RestaurantModel> doInBackground(Void... voids) {
        Log.e("getRestso", "back");
        if(all){
            Log.e("getRestso", "back all");
            // all = true, so its the first connection, we need the list of restaurants in db
            // and the list of all near restaurants
            getAllIdList();
        }
        else{
            Log.e("getRestso", "back filter");
            // else we need the filtred list
            getFiltredIdList();
        }
        return restaurantsList;
    }

    @Override
    protected void onPostExecute(List<RestaurantModel> restaurantList) {
        if(!isCancelled()){
            // we send the list. The boolean all is needed to know if we have to save the restaurants list
            // as allRestaurantsList in viewmodel. Then we dont have to relaunch the request when we back of a search
            callback.get().onPostExecute(restaurantList, all, dbRestaurantList);
            Log.e("getRestso", "post exe " + restaurantList.size());
        }
        super.onPostExecute(restaurantList);
    }

    //--------------------------- GET THE LIST OF RESTAURANTS IN DATABASE ----------------------------------

    private class GetRestaurantsInDb implements OnCompleteListener<QuerySnapshot> {
        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {
            Log.e("getRestso", "getDB oncomplete");
            if (task.isSuccessful()) {
                // Get the query snapshot from the task result
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {
                    // Get the contact list from the query snapshot
                    dbRestaurantList = querySnapshot.toObjects(RestaurantModel.class);
                    getDetails();
                }

            } else {
                Log.w(TAG, "Error getting documents: ", task.getException());
            }
        }
    }


    //-------------------- GET ALL THE NEAR RESTAURANTS ID AND CREATE A ID LIST ----------------------------

    public List<String> getAllIdList() {
        Log.e("getRestso", "allId");
        Call<NearByApiResponse> call = getApiService().getNearbyPlaces(
                "restaurant", lastKnowLocation.getLatitude() + "," + lastKnowLocation.getLongitude(), PROXIMITY_RADIUS);
        call.enqueue(new Callback<NearByApiResponse>() {
            @Override
            public void onResponse(Call<NearByApiResponse> call, Response<NearByApiResponse> response) {
                try {
                    for (int i = 0; i < response.body().getResults().size(); i++) {
                        // create a list with all the restaurants ids
                        idList.add(response.body().getResults().get(i).getPlaceId());
                        if(i == response.body().getResults().size()-1){
                            RestaurantHelper.getAllRestaurants(new GetRestaurantsInDb());
                        }
                        Log.e("getRestso", "idlistsize" + idList.size());
                    }
                } catch (Exception e) {
                    Log.d("new", "There is an error");
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<NearByApiResponse> call, Throwable t) {
                Log.d("new", t.toString());
                t.printStackTrace();
            }
        });
        return idList;
    }

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

    interface NearByApi {
        @GET("api/place/nearbysearch/json?sensor=true&key=AIzaSyDqhFfSN4_hc3aMphxtGbRsaoiHKaW12iQ")
        Call<NearByApiResponse> getNearbyPlaces(@Query("type") String type, @Query("location") String location, @Query("radius") int radius);
    }

    private static GsonConverterFactory getApiConvertorFactory() {return GsonConverterFactory.create();}


    // ------------------------ GET ALL THE SEARCH RESTAURANTS ID AND CREATE A ID LIST ----------------------------

    public List<String> getFiltredIdList(){
        idList.clear();
        LatLng center = new LatLng(lastKnowLocation.getLatitude(), lastKnowLocation.getLongitude());
        RectangularBounds bounds = RectangularBounds.newInstance(
                toBounds(center, PROXIMITY_RADIUS).southwest,
                toBounds(center, PROXIMITY_RADIUS).northeast);
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setLocationRestriction(bounds)
                .setOrigin(center)
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setSessionToken(token)
                .setQuery(newText)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                if(isCancelled()){
                    return;
                }
                idList.add(prediction.getPlaceId());
                if(idList.size() == response.getAutocompletePredictions().size()){
                    getDetails();
                }
                Log.e("getRestso", "idlistsize" + idList.size());
            }
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + apiException.getStatusCode());
            }
        });
        return idList;
    }

    public LatLngBounds toBounds(LatLng center, int radiusInMeters) {
        double distanceFromCenterToCorner = radiusInMeters * Math.sqrt(2.0);
        LatLng southwestCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0);
        LatLng northeastCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0);
        return new LatLngBounds(southwestCorner, northeastCorner);
    }

    //-------------------- GET DETAILS OF RESTAURANTS FROM THE ID LIST AND CREATE A RESTAURANTS LIST -------------------------------

    public List<RestaurantModel> getDetails(){
        Log.e("getRestso", "getdetails" + idList.size());
        restaurantsList.clear(); //TODO vérifier si je peux le virer
        if(idList.isEmpty()){
            return restaurantsList;
        }
        else {
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
            for (String id : idList) {
                Log.e("getRestso", "getdetails boucle" + idList.size());
                for (RestaurantModel restaurant : dbRestaurantList){
                    if(id.equals(restaurant.getId())){
                        Log.e("getRestso", "check db");
                        restaurantsList.add(restaurant);
                        idList.remove(id);
                    }
                }
                if(isCancelled()){
                    Log.e("getRestso", "details cancel");
                    return restaurantsList;
                }

                else{
                    FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(id, placeFields);
                    Task<FetchPlaceResponse> response = placesClient.fetchPlace(placeRequest);
                    Log.e("getRestso", "getdetails request");
//                    try {
//                        Log.e("getRestso", "getdetails try");
//                        Tasks.await(response);
                        if (response.isComplete()) {
                            Log.e("getRestso", "getdetails is complete");
                            Place place = response.getResult().getPlace();
                            // if called from nearby we have only restaurants
                            if (all) {
                                Log.e("getRestso", "getdetails add all");
                                restaurantsList.add(new RestaurantModel(place.getLatLng(), place.getName(), place.getAddress(), id, null, null));
                                idList.remove(id);
                            }
                            // else check if the establishment is a restaurant
                            else {
                                if (place.getTypes().contains(Place.Type.RESTAURANT)) {
                                    Log.e("getRestso", "getdetails add not all");
                                    restaurantsList.add(new RestaurantModel(place.getLatLng(), place.getName(), place.getAddress(), id, null, null));
                                    idList.remove(id);
                                }
                            }
                            if(idList.isEmpty()){
                                onPostExecute(restaurantsList);
                            }
                        }
//                    } catch (ExecutionException | InterruptedException e) {
//                        e.printStackTrace();
//                    }
                }
            }
        }
        return restaurantsList;
    }
}
