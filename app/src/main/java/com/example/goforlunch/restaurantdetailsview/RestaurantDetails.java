package com.example.goforlunch.restaurantdetailsview;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.goforlunch.R;
import com.example.goforlunch.restaurants.tools.RestaurantHelper;
import com.example.goforlunch.restaurants.tools.RestaurantModel;
import com.example.goforlunch.users.UserHelper;
import com.example.goforlunch.users.UserModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Arrays;
import java.util.List;


public class RestaurantDetails extends Fragment {

    private RestaurantModel restaurant;
    private String dateString;
    private String userId;
    private UserModel user;
    private boolean booked;
    private RestaurantDetailsViewModel restaurantDetailsViewModel;
    private FloatingActionButton fab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        restaurantDetailsViewModel = new ViewModelProvider(requireActivity()).get(RestaurantDetailsViewModel.class);
        View view = inflater.inflate(R.layout.fragment_restaurant_details_view, container, false);
        fullScreen(false);
        restaurant = restaurantDetailsViewModel.getRestaurant();
        dateString = restaurantDetailsViewModel.getDateString();
        IsBookedByThisUser();
        userId = user.getUid();
        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If this user dont have already booked this restaurant, click is used to book it
                if(!booked){
                    // if this restaurant is not in the database (so restaurant.getDate() == null)
                    // add the restaurant in db with the user id in the list restaurant.interstedUsersId and the today date
                    if(restaurant.getDate() == null){
                        addRestaurantInFirestore();
                    }
                    //else (this restaurant is in db)
                    else{
                        // if the date is not the today date, that means this restaurant is in db,
                        // but the restaurant.InterstedUsersId is obsolete, so we clear it
                        if(restaurant.getDate() != dateString){restaurant.getInterstedUsersId().clear();}
                        // then we add the user id in the list, and we update the list and the date in db
                        restaurant.getInterstedUsersId().add(userId);
                        RestaurantHelper.updateInterstedUsersList(restaurant.getInterstedUsersId(), restaurant.getId());
                        RestaurantHelper.updateDate(dateString, restaurant.getId());
                    }
                    // anyway we update User informations
                    UserHelper.updateRestaurant(restaurant, userId);
                    UserHelper.updateDate(dateString, userId);
                    //fab.setImageResource(R.drawable.ic_shopping_cart_white);
                }
                // this user has already booked this restaurant, click is used to cancel it
                else{
                    restaurant.getInterstedUsersId().remove(userId);
                    RestaurantHelper.updateInterstedUsersList(restaurant.getInterstedUsersId(), restaurant.getId());
                    // if InterstedUsersId() is empty that means this restaurant have no more book,
                    // so we have to change the restaurant.date,
                    // or this restaurant will continue to be considered as having effective reservations
                    // but can't be null or it will be considered as missing in the db
                    if(restaurant.getInterstedUsersId().isEmpty()){
                        RestaurantHelper.updateDate("obsolete", restaurant.getId());
                    }
                    UserHelper.updateRestaurant(null, userId);
                    //fab.setImageResource(R.drawable.ic_shopping_cart_white);
                }
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fullScreen(true);
    }

    private void fullScreen(Boolean show) {
        ActionBar actionBar = null;//todo
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

    // ADD Restaurant IN FIRESTORE DB

    private void addRestaurantInFirestore(){
        LatLng latLng = restaurant.getLatLng();
        String name = restaurant.getName();
        String vicinity = restaurant.getVicinity();
        String id = restaurant.getId();
        String date = dateString;
        List<String> interstedUsersId = Arrays.asList(userId);
        RestaurantHelper.createRestaurant(latLng, name, vicinity, id, date, interstedUsersId).addOnFailureListener(this.onFailureListener());
    }

    protected OnFailureListener onFailureListener(){
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
            }
        };
    }


    void IsBookedByThisUser(){
        user = restaurantDetailsViewModel.getUser();
        if(restaurant == user.getRestaurant() && dateString.equals(user.getBookingDate())){
            booked = true;
            //fab.setImageResource(R.drawable.ic_shopping_cart_white);
        }
        else{
            booked = false;
            //fab.setImageResource(R.drawable.ic_shopping_cart_white);
        }
    }
}