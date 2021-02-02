package com.example.goforlunch.users;

import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class UserListViewModel extends ViewModel {

    Query query;

    public UserListViewModel() {
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        query = rootRef.collection("users")
                .orderBy("username", Query.Direction.ASCENDING);
    }

    public Query getData(){
        return query;
    }
}

