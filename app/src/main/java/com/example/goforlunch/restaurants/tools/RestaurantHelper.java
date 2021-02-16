package com.example.goforlunch.restaurants.tools;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

    public static Task<Void> createRestaurant(LatLng latLng, String name, String vicinity, String id, String date, List<String> interstedUsersId) {
        RestaurantModel restaurantModelToCreate = new RestaurantModel(latLng,name,vicinity,id,date,interstedUsersId);
        return getRestaurantsCollection().document(id).set(restaurantModelToCreate);
    }

    // --- GET ---

    public static Task<DocumentSnapshot> getRestaurant(String id){
        return getRestaurantsCollection().document(id).get();
    }

    // --- UPDATE ---

    public static Task<Void> updateDate(String date, String id) {
        return getRestaurantsCollection().document(id).update("date", date);
    }
    public static Task<Void> updateInterstedUsersList(List<String> interestedUsersList, String id) {
        return getRestaurantsCollection().document(id).update("interested_users_list", interestedUsersList);
    }

    // --- DELETE ---

    public static Task<Void> deleteRestaurant(String id) {
        return getRestaurantsCollection().document(id).delete();
    }

    public static void getAllRestaurants(OnCompleteListener<QuerySnapshot> onCompleteListener){
        getRestaurantsCollection().get().addOnCompleteListener(onCompleteListener);
    }

}
