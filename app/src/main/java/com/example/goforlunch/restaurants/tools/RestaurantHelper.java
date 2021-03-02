package com.example.goforlunch.restaurants.tools;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class RestaurantHelper {

    private static final String COLLECTION_NAME = "restaurants";

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getRestaurantsCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<Void> createRestaurant(LatLng latLng, String name, String vicinity, String id, OpeningHours openingHours,
                                              String phoneNumber, List<PhotoMetadata> photoMetadatas, Place.BusinessStatus businessStatus,
                                              String date, List<String> interstedUsersId, List<String> interstedUsersName) {
        RestaurantModel restaurantModelToCreate = new RestaurantModel(latLng,name,vicinity,id,openingHours,phoneNumber,photoMetadatas,businessStatus,date,interstedUsersId, interstedUsersName);
        return getRestaurantsCollection().document(id).set(restaurantModelToCreate);
    }

    // --- GET ---

    public static Task<DocumentSnapshot> getRestaurant(String id){
        return getRestaurantsCollection().document(id).get();
    }

    public static void getAllRestaurants(OnCompleteListener<QuerySnapshot> onCompleteListener){
        getRestaurantsCollection().get().addOnCompleteListener(onCompleteListener);
    }

    // --- UPDATE ---

    public static Task<Void> updateDate(String date, String id) {
        return getRestaurantsCollection().document(id).update("date", date);
    }
    public static Task<Void> updateInterstedUsersId(List<String> interestedUsersId, String id) {
        return getRestaurantsCollection().document(id).update("interested_users_list", interestedUsersId);
    }
    public static Task<Void> updateInterstedUsersName(List<String> interestedUsersName, String id) {
        return getRestaurantsCollection().document(id).update("interested_users_name", interestedUsersName);
    }

    // --- DELETE ---

    public static Task<Void> deleteRestaurant(String id) {
        return getRestaurantsCollection().document(id).delete();
    }


}
