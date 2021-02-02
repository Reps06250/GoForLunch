package com.example.goforlunch.restaurants.details;

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
import com.example.goforlunch.restaurants.RestaurantHelper;
import com.example.goforlunch.restaurants.RestaurantModel;
import com.example.goforlunch.restaurants.RestaurantViewModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class RestaurantView extends Fragment {

    private RestaurantViewModel restaurantViewModel;
    private RestaurantModel restaurant;
    private String dateString;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        restaurantViewModel =
                new ViewModelProvider(requireActivity()).get(RestaurantViewModel.class);
        View view = inflater.inflate(R.layout.fragment_restaurant_view, container, false);
        fullScreen(false);
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/YYYY");
        dateString = formatter.format(date);
        restaurant = restaurantViewModel.getRestaurantMutableLiveData().getValue().get(restaurantViewModel.getPosition());
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(restaurant.getDate().equals(dateString)){
                    restaurant.getInterstedUsersId().add(userId);
                    RestaurantHelper.updateInterstedUsersList(restaurant.getInterstedUsersId(), restaurant.getId());
                }
                else{
                    addRestaurantInFirestore();
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
        ActionBar actionBar = null;
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            BottomNavigationView bottomNavigationView = activity.findViewById(R.id.nav_view);
            actionBar = activity.getSupportActionBar();
            if(show){
                bottomNavigationView.setVisibility(View.VISIBLE);
                actionBar.show();
            }
            else{
                bottomNavigationView.setVisibility(View.GONE);
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

}