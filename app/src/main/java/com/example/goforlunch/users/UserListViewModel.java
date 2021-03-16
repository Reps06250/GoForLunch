package com.example.goforlunch.users;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

public class UserListViewModel extends ViewModel {

    private MutableLiveData<List<UserModel>> userListMld;
    private String dateString;

    public UserListViewModel() {
        userListMld = new MutableLiveData<>();
        getList();
        Date date = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        dateString = formatter.format(date);
    }

    private void getList() {
        UserHelper.getAllUsers(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Get the query snapshot from the task result
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null) {
                        List<UserModel> usersList;
                        // Get the users list from the query snapshot
                        usersList = querySnapshot.toObjects(UserModel.class);
                        Collections.sort(usersList);
                        userListMld.postValue(usersList);
                    }
                } else {
                    Log.w(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    public MutableLiveData<List<UserModel>> getUserListMld() {
        return userListMld;
    }

    public void setUserListMld(MutableLiveData<List<UserModel>> userListMld) {
        this.userListMld = userListMld;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }
}

