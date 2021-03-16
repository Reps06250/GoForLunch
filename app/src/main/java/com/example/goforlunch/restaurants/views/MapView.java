package com.example.goforlunch.restaurants.views;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.goforlunch.R;
import com.example.goforlunch.restaurants.models.DbRestaurantModel;
import com.example.goforlunch.restaurants.RestaurantViewModel;
import com.example.goforlunch.restaurants.models.RestaurantModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MapView extends Fragment implements OnMapReadyCallback {

    private RestaurantViewModel restaurantViewModel;
    private Location lastKnownLocation = null;
    private GoogleMap gMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean locationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String TAG = MapView.class.getSimpleName();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.e("MapView", "createView");
        restaurantViewModel =
                new ViewModelProvider(requireActivity()).get(RestaurantViewModel.class);
        View rootView = inflater.inflate(R.layout.fragment_mapview, container, false);

        // if we already save a location in viewmodel, we will not relaunch the location request
        lastKnownLocation = restaurantViewModel.getLastKnowLocation();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        // Build the map.
        com.google.android.gms.maps.MapView mMapView = (com.google.android.gms.maps.MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately
        mMapView.getMapAsync(this);

        //Observe the restaurants list to add markers
        restaurantViewModel.getRestaurantMutableLiveData().observe(getViewLifecycleOwner(), new Observer<List<RestaurantModel>>() {
            @Override
            public void onChanged(@Nullable List<RestaurantModel> restaurantsList) {
                Log.e("MapView", "onChanged");
                Log.e("emptyList", "MapViewOnchanged, restaurantsList = " + restaurantsList.size());
                if(gMap != null && restaurantsList != null){
                    addMarkers(restaurantsList);
                }
            }
        });
        return rootView;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e("MapView", "onMapReady");
        gMap = googleMap;
        // lastknowlocation == nuul means its the first connection, so after getting location
        // (at the end of getDeviceLocation()) we launch the requests in the asynctask GetRestaurantsList,
        // to get the list of all near restaurants
        if(lastKnownLocation == null){
            getLocationPermission();
            updateLocationUI();
            getDeviceLocation();
        }
        else{
            addMarkers(Objects.requireNonNull(restaurantViewModel.getRestaurantMutableLiveData().getValue()));
        }
        // Restore camera position
        if(restaurantViewModel.getCameraPosition() != null){
            gMap.moveCamera(CameraUpdateFactory.newCameraPosition(restaurantViewModel.getCameraPosition()));
        }

        gMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                for( RestaurantModel restaurant : restaurantViewModel.getRestaurantMutableLiveData().getValue()){
                    if(marker.getTitle().equals(restaurant.getPlace().getName() + " : " + restaurant.getPlace().getAddress())){
                        restaurantViewModel.setRestaurant(restaurant);
                        break;
                    }
                }
                NavHostFragment.findNavController(MapView.this)
                        .navigate(R.id.go_to_details);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //save camera position
        restaurantViewModel.setCameraPosition(gMap.getCameraPosition());
    }

    // ---------------------- LOCATION PERMISSION ----------------------

    // Prompt the user for permission.
    private void getLocationPermission() {
        Log.e("MapView", "getLocationPermission");
        if (ContextCompat.checkSelfPermission(this.getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    // Handle permission
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.e("MapView", "onRequestPermissionsResult");
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
//        updateLocationUI();
    }

    // Turn on the My Location layer and the related control on the map.
    private void updateLocationUI() {
        Log.e("MapView", "updateLocationUI");
        if (gMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                gMap.setMyLocationEnabled(true);
                gMap.getUiSettings().setMyLocationButtonEnabled(true);
            }
            else {
                gMap.setMyLocationEnabled(false);
                gMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    // Get the current location of the device and set the position of the map.
    private void getDeviceLocation() {
        Log.e("MapView", "getDeviceLocation");
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Log.e("MapView", "getDeviceLocation, onComplete");
                        if (task.isSuccessful()) {
                            Log.e("MapView", "getDeviceLocation, onComplete, task.isSuccessful()");
                            lastKnownLocation = task.getResult();
                            // Save Location to make the request only one time
                            restaurantViewModel.setLastKnownLocation(lastKnownLocation);
                            if (lastKnownLocation != null) {
                                Log.e("MapView", "getDeviceLocation, lastKnownLocation != null");
                                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), 14));
                                //Launch retrofit request to have the list of all near restaurants
                                restaurantViewModel.getAllRestaurantsList();
                            }
                        }else {
                                Log.e("MapView", "getDeviceLocation, lastKnownLocation == null");
                                LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
                                gMap.moveCamera(CameraUpdateFactory
                                        .newLatLngZoom(defaultLocation, 14));
                                Log.d(TAG, "Current location is null. Using defaults.");
                                Log.e(TAG, "Exception: %s", task.getException());
                                gMap.getUiSettings().setMyLocationButtonEnabled(false);
                            }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    // --------------------------------------------------------------------------------

    // ADD RESTAURANTS ON MAP
    public void addMarkers(List<RestaurantModel> restaurantsList) {
        Log.e("MapView", "addMarkers");
        gMap.clear();
        for(RestaurantModel restaurant : restaurantsList){
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(restaurant.getPlace().getLatLng());
            markerOptions.title(restaurant.getPlace().getName() + " : " + restaurant.getPlace().getAddress());
            Date date = new Date();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            if(restaurant.getDate() == null || !restaurant.getDate().equals(formatter.format(date))){
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_red));
            }
            else{
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_green));
            }
            gMap.addMarker(markerOptions);
        }
    }
}

