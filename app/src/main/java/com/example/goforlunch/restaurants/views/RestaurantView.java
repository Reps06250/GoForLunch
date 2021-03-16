package com.example.goforlunch.restaurants.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.goforlunch.R;
import com.example.goforlunch.restaurants.RestaurantViewModel;
import com.example.goforlunch.restaurants.models.DbRestaurantModel;
import com.example.goforlunch.restaurants.models.RestaurantModel;
import com.example.goforlunch.restaurants.tools.GetRestaurantsList;
import com.example.goforlunch.restaurants.tools.RestaurantHelper;
import com.example.goforlunch.users.UserHelper;
import com.example.goforlunch.users.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;


public class RestaurantView extends Fragment implements  GetRestaurantsList.Listeners{

    private RestaurantModel restaurant;
    private String dateString;
    private String userId;
    private UserModel user;
    private FloatingActionButton fab;
    private RestaurantViewModel restaurantViewModel;
    private TextView nameTv;
    boolean booked;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        restaurantViewModel = new ViewModelProvider(requireActivity()).get(RestaurantViewModel.class);
        // create ContextThemeWrapper from the original Activity Context with the custom theme
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.Theme_GoForLunch_NoActionBar);
        // clone the inflater using the ContextThemeWrapper
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        View view = localInflater.inflate(R.layout.fragment_restaurant_details_view, container, false);
        fullScreen(false);
        dateString = getDate();
        user = restaurantViewModel.getUser();
        userId = user.getUid();
        fab = view.findViewById(R.id.fab);
        nameTv = view.findViewById(R.id.restaurant_view_name);
        Bundle argument = getArguments();
        getRestaurant(argument);
        return view;
    }

    @Override
    public void onDestroyView() {
        fullScreen(true);
        super.onDestroyView();
    }

    private String getDate() {
        Date date = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);
    }

    private void fullScreen(Boolean show) {
        ActionBar actionBar = null;
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            BottomNavigationView bottomNavigationView = activity.findViewById(R.id.bottom_navigation);
            actionBar = activity.getSupportActionBar();
            if(show){
                bottomNavigationView.setVisibility(View.VISIBLE);
                assert actionBar != null;
                actionBar.show();
            }
            else{
                bottomNavigationView.setVisibility(View.GONE);
                assert actionBar != null;
                actionBar.hide();
            }
        }
    }

    private void getRestaurant(Bundle argument){
        if(argument == null){
            restaurant = restaurantViewModel.getRestaurant();
            fabLogic();
        }
        else{
            String placeId = argument.getString("restaurantId", null);
            GetRestaurantsList getRestaurantsList = new GetRestaurantsList(this,"detailsView", placeId, getContext());
            getRestaurantsList.execute();
        }
    }

    @Override
    public void onPostExecute(List<RestaurantModel> restaurantsList, String from, List<DbRestaurantModel> dbRestaurantList) {
        this.restaurant = restaurantsList.get(0);
        fabLogic();
    }

    private void fabLogic() {
        booked = isBookedByThisUser();
        nameTv.setText(restaurant.getPlace().getName());
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If this user dont have already booked this restaurant, click is used to book it
                if(!booked){
                    checkOldRestaurant();
                }
                // this user has already booked this restaurant, click is used to cancel it
                else{
                    updateUser();
                }
            }
        });
    }

    private void checkOldRestaurant() {
        // check if the user have already book a restaurant for next lunch and if it's the same restaurant of the view
        if(!(user.getBookingDate() == null) && user.getBookingDate().equals(dateString) &&
                !user.getRestaurantId().equals(restaurant.getPlace().getId())) {
                // then refresh data of the old restaurant
                RestaurantHelper.getRestaurant(user.getRestaurantId()).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        DbRestaurantModel lRestaurant = documentSnapshot.toObject(DbRestaurantModel.class);
                        if(lRestaurant.getInterestedUsers().size() == 1){
                            RestaurantHelper.updateDate("obsolete", lRestaurant.getId());
                        }
                        removeUser(lRestaurant.getInterestedUsers());
                        RestaurantHelper.updateInterstedUsers(lRestaurant.getInterestedUsers(), lRestaurant.getId());
                        updateUser();
                    }
                });

        }
        else{
            updateUser();
        }
    }

    private void removeUser(List<UserModel> interestedUsers) {
        for(UserModel lUser : interestedUsers){
            if(lUser.getUid().equals(user.getUid())){
                interestedUsers.remove(lUser);
            }
        }
    }


    private void updateUser(){
        String date;
        String restaurantId;
        if(!booked){
            date = dateString;
            restaurantId = restaurant.getPlace().getId();
        }
        else{
            date = null;
            restaurantId = null;
        }
        user.setBookingDate(date);
        user.setRestaurantId(restaurantId);
        UserHelper.updateDate(date, userId);
        UserHelper.updateRestaurantId(restaurantId, userId);
        updateRestaurant();
    }

    private void updateRestaurant() {
        if(!booked){
            // if this restaurant is not in the database (so restaurant.getDate() == null)
            // add the restaurant in db with the user id in the list restaurant.interstedUsersId and the today date
            if(restaurant.getDate() == null){
                restaurant.setInterestedUsers(new ArrayList<>());
                addRestaurantInFirestore();
            }
            //else (this restaurant is in db)
            else{
                // if the date is not the today date, that means this restaurant is in db,
                // but the restaurant.InterstedUsers list is obsolete, so we clear it
                if(restaurant.getDate() != dateString){
                    restaurant.getInterestedUsers().clear();
                    restaurant.setDate(dateString);
                    RestaurantHelper.updateDate(dateString, restaurant.getPlace().getId());
                }
            }
            // then we add the user in the list, update the list in db and the date
            restaurant.getInterestedUsers().add(user);
            fab.setImageResource(R.drawable.ic_baseline);
            fab.getDrawable().mutate().setTint(getResources().getColor(R.color.red));
            booked = true;
        }
        else{
            restaurant.getInterestedUsers().remove(user);
            // if InterstedUsersId() is empty that means this restaurant have no more reservation,
            // so we have to change the restaurant.date,
            // or this restaurant will continue to be considered as having effective reservations
            // but can't be null or it will be considered as missing in the db
            if(restaurant.getInterestedUsers().isEmpty()){
                RestaurantHelper.updateDate("obsolete", restaurant.getPlace().getId());
            }
            fab.setImageResource(R.drawable.ic_check);
            fab.getDrawable().mutate().setTint(getResources().getColor(R.color.teal_200));
            booked = false;
        }
        RestaurantHelper.updateInterstedUsers(restaurant.getInterestedUsers(), restaurant.getPlace().getId());

    }

    public boolean isBookedByThisUser(){
        Log.e("fab", user.getUsername());
        if(user.getBookingDate() == null || !user.getBookingDate().equals(dateString) ||
                user.getRestaurantId() == null || !user.getRestaurantId().equals(restaurant.getPlace().getId())){
            Log.e("fab", "not booked");
            fab.setImageResource(R.drawable.ic_check);
            fab.getDrawable().mutate().setTint(getResources().getColor(R.color.teal_200));
            return false;
        }
        else {
            Log.e("fab", "booked");
            fab.setImageResource(R.drawable.ic_baseline);
            fab.getDrawable().mutate().setTint(getResources().getColor(R.color.red));
            return true;
        }
    }


    // ADD Restaurant IN FIRESTORE DB

    private void addRestaurantInFirestore(){
        RestaurantHelper.createRestaurant(restaurant.getPlace().getId(),dateString, 0, null)
                .addOnFailureListener(this.onFailureListener());
    }

    protected OnFailureListener onFailureListener(){
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
            }
        };
    }
}