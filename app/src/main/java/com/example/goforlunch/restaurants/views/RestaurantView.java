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

import com.example.goforlunch.MainActivity;
import com.example.goforlunch.R;
import com.example.goforlunch.restaurants.RestaurantViewModel;
import com.example.goforlunch.restaurants.tools.RestaurantHelper;
import com.example.goforlunch.restaurants.tools.RestaurantModel;
import com.example.goforlunch.users.UserHelper;
import com.example.goforlunch.users.UserModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;




public class RestaurantView extends Fragment {

    private RestaurantModel restaurant;
    private String dateString;
    private String userId;
    private UserModel user;
    private RestaurantViewModel restaurantViewModel;
    private FloatingActionButton fab;
    private boolean fromList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        restaurantViewModel =
                new ViewModelProvider(requireActivity()).get(RestaurantViewModel.class);
        restaurant = MainActivity.getRestaurant();
        // create ContextThemeWrapper from the original Activity Context with the custom theme
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.Theme_GoForLunch_NoActionBar);
        // clone the inflater using the ContextThemeWrapper
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        View view = localInflater.inflate(R.layout.fragment_restaurant_details_view, container, false);
        fullScreen(false);
        Date date = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        dateString = formatter.format(date);
        user = MainActivity.user;
        userId = user.getUid();
        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If this user dont have already booked this restaurant, click is used to book it
                if(!isBookedByThisUser()){
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
                        RestaurantHelper.updateInterstedUsersId(restaurant.getInterstedUsersId(), restaurant.getId());
                        RestaurantHelper.updateDate(dateString, restaurant.getId());
                    }
                    // anyway we update User informations
                    UserHelper.updateRestaurant(restaurant, userId);
                    UserHelper.updateDate(dateString, userId);
                    fab.setImageResource(R.drawable.ic_check);
                }
                // this user has already booked this restaurant, click is used to cancel it
                else{
                    restaurant.getInterstedUsersId().remove(userId);
                    RestaurantHelper.updateInterstedUsersId(restaurant.getInterstedUsersId(), restaurant.getId());
                    // if InterstedUsersId() is empty that means this restaurant have no more book,
                    // so we have to change the restaurant.date,
                    // or this restaurant will continue to be considered as having effective reservations
                    // but can't be null or it will be considered as missing in the db
                    if(restaurant.getInterstedUsersId().isEmpty()){
                        RestaurantHelper.updateDate("obsolete", restaurant.getId());
                    }
                    UserHelper.updateRestaurant(null, userId);
                    fab.setImageResource(R.drawable.ic_check_red);
                }
            }
        });
        TextView nameTv = view.findViewById(R.id.restaurant_view_name);
        nameTv.setText(restaurant.getName());
        return view;
    }

    @Override
    public void onDestroyView() {
        fullScreen(true);
        super.onDestroyView();
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

    // ADD Restaurant IN FIRESTORE DB

    private void addRestaurantInFirestore(){
        LatLng latLng = restaurant.getLatLng();
        String name = restaurant.getName();
        String vicinity = restaurant.getVicinity();
        String id = restaurant.getId();
        String date = dateString;
        List<String> interstedUsersId = Arrays.asList(userId);
        List<String> interstedUsersName = Arrays.asList(user.getUsername());
        RestaurantHelper.createRestaurant(latLng, name, vicinity, id,restaurant.getOpeningHours(),restaurant.getPhoneNumber(),restaurant.getPhotoMetadata(),restaurant.getBusinessStatus(), date, interstedUsersId, interstedUsersName).addOnFailureListener(this.onFailureListener());
    }

    protected OnFailureListener onFailureListener(){
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
            }
        };
    }


    public boolean isBookedByThisUser(){
        boolean booked;
        Log.e("userrr", user.getUsername());
        if(!fromList){
            Log.e("userrr", "booked !fromlist");
            booked = true;
            fab.setImageResource(R.drawable.ic_check);
        }
        else if((restaurant.getInterstedUsersId().contains(userId)) && dateString.equals(restaurant.getDate())){
            Log.e("userrr", "booked else if");
            booked = true;
            fab.setImageResource(R.drawable.ic_check);
        }
        else{
            Log.e("userrr", "booked false");
            booked = false;
            fab.setImageResource(R.drawable.ic_check_red);
        }
        return booked;
    }
}