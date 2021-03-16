package com.example.goforlunch.restaurants.tools;

import com.example.goforlunch.restaurants.models.DbRestaurantModel;
import com.example.goforlunch.users.UserModel;
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

    public static Task<Void> createRestaurant(String id, String date, int star, List<UserModel> interestedUsers)
    {
        DbRestaurantModel dbRestaurantModelToCreate = new DbRestaurantModel(id,date,star,interestedUsers);
        return getRestaurantsCollection().document(id).set(dbRestaurantModelToCreate);
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

    public static Task<Void> updateInterstedUsers(List<UserModel> interestedUsers, String id) {
        return getRestaurantsCollection().document(id).update("interestedUsers", interestedUsers);
    }
    public static Task<Void> updateStars(int stars, String id) {
        return getRestaurantsCollection().document(id).update("stars", stars);
    }

    // --- DELETE ---

    public static Task<Void> deleteRestaurant(String id) {
        return getRestaurantsCollection().document(id).delete();
    }


}
