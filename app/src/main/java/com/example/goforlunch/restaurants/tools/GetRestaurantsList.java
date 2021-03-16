package com.example.goforlunch.restaurants.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.goforlunch.restaurants.models.DbRestaurantModel;
import com.example.goforlunch.restaurants.models.RestaurantModel;
import com.example.goforlunch.restaurants.tools.retrofit.models.NearByApiResponse;
import com.example.goforlunch.restaurants.tools.retrofit.models.Result;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.SphericalUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

public class GetRestaurantsList extends AsyncTask<Void, Void, Void>{

    public interface Listeners {
        void onPostExecute(List<RestaurantModel> restaurantsList, String from, List<DbRestaurantModel> dbRestaurantList);
    }

    private List<DbRestaurantModel> dbRestaurantsList;
    private final PlacesClient placesClient;
    private final String from;
    private String newText;
    private final WeakReference<Listeners> callback;
    private final Location lastKnowLocation;
    private final int PROXIMITY_RADIUS = 1000;
    private List<RestaurantModel> restaurantsList = new ArrayList<>();
    private List<String> idList = new ArrayList<>();


    public GetRestaurantsList(Listeners callback, Location lastKnowLocation, String from, Context context){
        this.callback = new WeakReference<>(callback);
        this.from = from;
        this.lastKnowLocation =lastKnowLocation;
        // Construct a PlacesClient
        Places.initialize(context,"AIzaSyAIMJQa6CF9DXI0wpN_KnMgZBvyfS-Xf3Y");//todo
        placesClient = Places.createClient(context);
    }

    public GetRestaurantsList(Listeners callback, String from, Location lastKnowLocation, String newText, List<DbRestaurantModel>dbRestaurantsList, Context context){
        this.callback = new WeakReference<>(callback);
        this.from = from;
        this.newText = newText;
        this.lastKnowLocation =lastKnowLocation;
        this.dbRestaurantsList = dbRestaurantsList;
        placesClient = Places.createClient(context);
    }

    public GetRestaurantsList(Listeners callback,String from, String id, Context context){
        this.callback = new WeakReference<>(callback);
        placesClient = Places.createClient(context);
        this.lastKnowLocation = null;
        this.from = from;
        idList.add(id);
        Log.e("userView", "GetRestaurantsList constructor, id : " + idList.get(0));
    }

    @Override
    protected Void doInBackground(Void... voids) {
        switch(from){
            case "map":
                // all = true, so its the first connection, we need the list of restaurants in db
                // and the list of all near restaurants
                RestaurantHelper.getAllRestaurants(new GetRestaurantsInDb());
                break;
            case "searchBar":
                // we need the filtred list
                getFiltredIdList();
                break;
            case "detailsView":
            case "notification":
                getDetails(idList);
                break;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.e("userView", "onpostexe");
        callback.get().onPostExecute(restaurantsList, from, dbRestaurantsList);
    }

    //--------------------------- GET THE LIST OF RESTAURANTS IN DATABASE ----------------------------------

    private class GetRestaurantsInDb implements OnCompleteListener<QuerySnapshot> {
        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if (task.isSuccessful()) {
                // Get the query snapshot from the task result
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {
                    // Get the contact list from the query snapshot
                    dbRestaurantsList = querySnapshot.toObjects(DbRestaurantModel.class);
                    getAllIdList();
                }

            } else {
                Log.w(TAG, "Error getting documents: ", task.getException());
            }
        }
    }


    //-------------------- GET ALL THE NEAR RESTAURANTS ID AND CREATE A ID LIST ----------------------------

    public void getAllIdList() {
        List<String> idList = new ArrayList<>();
        Call<NearByApiResponse> call = getApiService().getNearbyPlaces(
                "restaurant", lastKnowLocation.getLatitude() + "," + lastKnowLocation.getLongitude(), PROXIMITY_RADIUS);
        call.enqueue(new Callback<NearByApiResponse>() {
            @Override
            public void onResponse(Call<NearByApiResponse> call, Response<NearByApiResponse> response) {
                try {
                    for (Result result :  response.body().getResults()) {
                        // create a list with all the restaurants ids
                        idList.add(result.getPlaceId());
                        if(idList.size() == response.body().getResults().size()){
                            getDetails(idList);
                        }
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
        @GET("api/place/nearbysearch/json?sensor=true&key=AIzaSyAIMJQa6CF9DXI0wpN_KnMgZBvyfS-Xf3Y")
        Call<NearByApiResponse> getNearbyPlaces(@Query("type") String type, @Query("location") String location, @Query("radius") int radius);
    }

    private static GsonConverterFactory getApiConvertorFactory() {return GsonConverterFactory.create();}


    // ------------------------ GET ALL THE SEARCH RESTAURANTS ID AND CREATE A ID LIST ----------------------------

    public void getFiltredIdList(){
        List<String> idList = new ArrayList<>();
        Log.e("emptyList", "getFiltredList " + newText);
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
                    Log.e("emptyList", "getFiltredList cancel");
                    return;
                }
                idList.add(prediction.getPlaceId());
                if(idList.size() == response.getAutocompletePredictions().size()){
                    Log.e("emptyList", "getFiltredIdList with " + idList.size());
                    getDetails(idList);
                }
            }
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + apiException.getStatusCode());
            }
        });
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

    public void getDetails(List<String> idList){
        Log.e("userView", "GetDetails, id : " + idList.get(0));
        List<Place> placeList = new ArrayList<>();
        Log.e("userView", "getDetails placeListSize " + placeList.size());
        Log.e("userView", "getDetails idListSize " + idList.size());

        // Specify the fields to return.
            final List<Place.Field> placeFields = Arrays.asList(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.TYPES,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG,
                    Place.Field.OPENING_HOURS,
                    Place.Field.PHONE_NUMBER,
                    Place.Field.PHOTO_METADATAS,
                    Place.Field.WEBSITE_URI);

                for(String id : idList){
                    Log.e("userView", "getDetails boucle for with " + idList.size() + " id");
                    if(isCancelled()){
                        Log.e("userView", "getDetails cancel");
                        return;
                    }
                    else{
                        Log.e("userView", "getDetails else ");
                        FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(id, placeFields);
                        placesClient.fetchPlace(placeRequest).addOnCompleteListener((response) -> {
                            Log.e("userView", "getDetails request with " + id);
                            Place place = response.getResult().getPlace();
                            Log.e("userView", "getDetails request response " + place.getId());
                            // if called from nearby we have only restaurants (all), or we check if the establishment is a restaurant
                            if (!from.equals("searchBar") || place.getTypes().contains(Place.Type.RESTAURANT)) {
                                Log.e("userView", "getDetails, placeList.add " + place.getId());
                                placeList.add(place);
                            }
                            else{
                                Log.e("userView", "getDetails, idList.remove " + place.getId());
                                idList.remove(id);
                            }
                            if(idList.size() == placeList.size()){
                                Log.e("userView", "getDetails, getPhoto ");
                                getPhoto(placeList);
                            }
                        });
                    }
                }
    }

    private void getPhoto(List<Place> placeList) {
        Log.e("userView", "GetPhoto, id : " + placeList.get(0).getId());
        if(placeList.isEmpty()){
            callback.get().onPostExecute(restaurantsList, from, dbRestaurantsList);
            return;
        }
        List<Bitmap> bitmapList = new ArrayList<>();
        for(Place place : placeList){
            if(isCancelled()){
                return;
            }
            // Get the photo metadata.
            final List<PhotoMetadata> metadata = place.getPhotoMetadatas();
            if (metadata == null || metadata.isEmpty()) {
                Log.w(TAG, "No photo metadata.");
                bitmapList.add(null);
            }
            else{
                final PhotoMetadata photoMetadata = metadata.get(0);
                // Create a FetchPhotoRequest.
                final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                        .setMaxWidth(300)
                        .setMaxHeight(300)
                        .build();
                placesClient.fetchPhoto(photoRequest).addOnCompleteListener((response) -> {
                    Bitmap bitmap = response.getResult().getBitmap();
                    bitmapList.add(bitmap) ;
                    if(bitmapList.size() == placeList.size()){
                        addToRestaurantsList(placeList, bitmapList);
                    }
                }).addOnFailureListener((exception) -> {
                    if (exception instanceof ApiException) {
                        Log.e(TAG, "Place not found: " + exception.getMessage());
                    }
                });
            }
        }
    }

    private void addToRestaurantsList(List<Place> placeList, List<Bitmap> bitmapList) {
        for(Place place : placeList){
            if(isCancelled()){
                return;
            }
            Log.e("userView", "addToResaturantsList , id : " + place.getId());
            Bitmap bitmap = bitmapList.get(placeList.indexOf(place));
            boolean notInDb = true;
            if(dbRestaurantsList == null || dbRestaurantsList.isEmpty()){
                Log.e("emptyList", "dbRestaurantsList == null || dbRestaurantsList.isEmpty(), add " + place.getName());
                restaurantsList.add(new RestaurantModel(place, null, 0,null, bitmap));
                Log.e("userView", "dbRestaurantsList == null || dbRestaurantsList.isEmpty(), id : " + restaurantsList.get(0).getPlace().getId());
            }
            else{
                for(DbRestaurantModel dbRestaurant : dbRestaurantsList){
                    if(dbRestaurant.getId().equals(place.getId())){
                        restaurantsList.add(new RestaurantModel(place, dbRestaurant.getDate(), dbRestaurant.getStars(), dbRestaurant.getInterestedUsers(), bitmap));
                        Log.e("userView", "id in db, add " + place.getId());
                        notInDb = false;
                        break;
                    }
                }
            }
            if(notInDb){
                restaurantsList.add(new RestaurantModel(place, null, 0,null, bitmap));
                Log.e("userView", "id not in db, add " + place.getId());
            }

            if(restaurantsList.size() == placeList.size()){
                Log.e("emptyList", "id list is empty, restaurantsList = " + restaurantsList.size());
                // we send the list. The boolean all is needed to know if we have to save the restaurants list
                // as allRestaurantsList in viewmodel. Then we dont have to relaunch the request when we back of a search
                Log.e("userView", "send list with " + restaurantsList.get(0).getPlace().getId());
                callback.get().onPostExecute(restaurantsList, from, dbRestaurantsList);
            }
        }
    }
}
