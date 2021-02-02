package com.example.goforlunch.users;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class UserHelper {

    private static final String COLLECTION_NAME = "users";

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getUsersCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<Void> createUser(String uid, String username, String urlPicture) {
        UserModel userModelToCreate = new UserModel(uid, username, urlPicture);
        return getUsersCollection().document(uid).set(userModelToCreate);
    }

    // --- GET ---

    public static Task<DocumentSnapshot> getUser(String uid){
        return getUsersCollection().document(uid).get();
    }

    // --- UPDATE ---

    public static Task<Void> updateUsername(String username, String uid) {
        return getUsersCollection().document(uid).update("username", username);
    }

    public static Task<Void> updateUrlPicture(String urlPicture, String uid) {
        return getUsersCollection().document(uid).update("urlPicture", urlPicture);
    }

    // --- DELETE ---

    public static Task<Void> deleteUser(String uid) {
        return getUsersCollection().document(uid).delete();
    }

    public static void getAllUsers(OnCompleteListener<QuerySnapshot> onCompleteListener){
         getUsersCollection().get().addOnCompleteListener(onCompleteListener);
    }

}
