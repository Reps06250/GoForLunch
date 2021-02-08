package com.example.goforlunch.restaurants;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

class GetRestaurantsDetails {

    List<String> idList;
    PlacesClient placesClient;
    boolean all;
    AtomicBoolean synchro = new AtomicBoolean(false);
    AtomicBoolean stopExecution = new AtomicBoolean(false);

    public interface Listeners {
        void onPostExecute(List<RestaurantModel> dbRestaurantsList, boolean all);
    }

    private WeakReference<Listeners> callback;

    public GetRestaurantsDetails(Listeners callback, PlacesClient placesClient){
        this.callback = new WeakReference<>(callback);
        this.placesClient = placesClient;
    }

    public void GetDetails(List<String> idList, boolean all){
        this.all = all;
        this.idList = idList;
        int timeOut = 0;
        if (synchro.get()){
            stopExecution.set(true);
            while(synchro.get() && timeOut < 10){
                timeOut++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        new GetDetailsAsyncTask().execute();
    }


    public class GetDetailsAsyncTask extends AsyncTask<Void, Void, List<RestaurantModel>>{

        @Override
        protected List<RestaurantModel> doInBackground(Void... voids) {
            synchro.set(true);
            stopExecution.set(false);
            List<RestaurantModel> restaurantList = new ArrayList<>();
            Log.e("getRestso", "getdetails");
            restaurantList.clear();
            if(idList.isEmpty()){
                return restaurantList;
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
                    if(stopExecution.get()){
                        return restaurantList;
                    }
                    Log.e("getRestso", "getdetails boucle");
                    FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(id, placeFields);
                    Task<FetchPlaceResponse> response = placesClient.fetchPlace(placeRequest);
                    try {
                        Log.e("getRestso", "getdetails try");
                        Tasks.await(response);
                        if (response.isComplete()) {
                            Log.e("getRestso", "getdetails is complete");
                            Place place = response.getResult().getPlace();
                            // if called from nearby we have only restaurants
                            if (all) {
                                Log.e("getRestso", "getdetails add");
                                restaurantList.add(new RestaurantModel(place.getLatLng(), place.getName(), place.getAddress(), id, null, null));
                            }
                            // else check if the establishment is a restaurant
                            else {
                                if (place.getTypes().contains(Place.Type.RESTAURANT)) {
                                    restaurantList.add(new RestaurantModel(place.getLatLng(), place.getName(), place.getAddress(), id, null, null));
                                }
                            }
                        }
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return restaurantList;
        }


        @Override
        protected void onPostExecute(List<RestaurantModel> restaurantList) {
            synchro.set(false);
            callback.get().onPostExecute(restaurantList, all);
            Log.e("getRestso", "onpostexecute ");
            super.onPostExecute(restaurantList);
        }

    }
}
