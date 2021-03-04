package com.example.goforlunch.users;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.goforlunch.restaurants.tools.GetRestaurantsList;
import com.example.goforlunch.restaurants.tools.RestaurantHelper;
import com.example.goforlunch.restaurants.tools.RestaurantModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

public class UserListViewModel extends ViewModel {

    public UserListViewModel() {
    }

}

