package com.example.goforlunch.restaurants.tools;

import android.util.Log;

import com.example.goforlunch.users.UserHelper;
import com.example.goforlunch.users.UserModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Objects;

public class GetCurrentUserModel {

    private static UserModel user;

    public GetCurrentUserModel(){
        Log.e("userrr", "getUser");
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        UserHelper.getUser(userId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                user = documentSnapshot.toObject(UserModel.class);
                Log.e("userrr", "getUser success " + user.getUsername());
            }
        });
    }

    public static UserModel getUser() {
        return user;
    }
}
