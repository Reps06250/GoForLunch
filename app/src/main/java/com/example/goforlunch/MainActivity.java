package com.example.goforlunch;

import android.content.Intent;
import android.os.Bundle;

import com.example.goforlunch.eventbus.MyEventBus;
import com.example.goforlunch.restaurants.RestaurantViewModel;
import com.example.goforlunch.users.UserHelper;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.view.MenuInflater;

import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Objects;

import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity{

    private static final int RC_SIGN_IN = 123;
    private static RestaurantViewModel restaurantViewModel;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restaurantViewModel = new ViewModelProvider(this).get(RestaurantViewModel.class);
        setContentView(R.layout.activity_main);

        //TOOLBAR
        Toolbar toolbar = findViewById (R.id.toolbar);
        setSupportActionBar (toolbar);
//        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled (true);
//        getSupportActionBar ().setDisplayShowHomeEnabled(true);

        //NAVIGATION DRAWER and BOTTOM NAVIGATION
        //je déclare mon navigation drawer et le navigation view
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById ( R.id.navigation_view );
        // je configure mon appBar
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_map, R.id.navigation_restaurants_list, R.id.navigation_users_list)
                .setOpenableLayout(drawer)
                .build();
        //je déclare le navcontroller avec le fragment hote
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //je lie mon appbar et mon navcontroller
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        //getSupportActionBar ().setDisplayHomeAsUpEnabled ( true );
        // je déclare mon bottomNavigation
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        //je lie mon bottomNavigation et mon navcontroller
        NavigationUI.setupWithNavController(bottomNavigation, navController);

        View navView = navigationView.inflateHeaderView ( R.layout.nav_header_main);
//            NavProfileImage = navView.findViewById ( R.id.nav_profile_image );
//            NavProfileFullName =  navView.findViewById ( R.id.nav_user_full_name );
//            NavProfileEmail =  navView.findViewById ( R.id.nav_user_email );

        //Handle visibility of the application bottom navigation
//            navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
//                @Override
//                public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
//                    if(destination.getId()== R.id.nav_profile || destination.getId()== R.id.nav_settings
//                            ||destination.getId()== R.id.nav_feedback || destination.getId()== R.id.nav_friends){
//                        bottomNavigation.setVisibility(View.GONE);
//                    }
//                    else{
//                        bottomNavigation.setVisibility(View.VISIBLE);
//                    }
//                }
//            });
    }

    //This will help to automatically handle the back arrow navigation and also the back button when pressed
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    //MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                invalidateOptionsMenu();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                restaurantViewModel.getFiltredRestaurantsList(newText);
                return false;
            }
        });
        return true;
    }



    //     ----------------- LOGIN ---------------------
    //TODO voir si je peux foutre ca dans une classe a part
    //LAUNCH
    private void startSignInActivity(){
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        //.setTheme(R.style.LoginTheme)
                        .setAvailableProviders(
                                Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build(),
                                        new AuthUI.IdpConfig.GoogleBuilder().build()))
                        .setIsSmartLockEnabled(false, true)
                        //.setLogo(R.drawable.ic_logo_auth)
                        .build(),
                RC_SIGN_IN);
    }


    //HANDLE
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.handleResponseAfterSignIn(requestCode, resultCode, data);
    }

    private void showSnackBar(ConstraintLayout constraintLayout, String message){
        Snackbar.make(constraintLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data){
        IdpResponse response = IdpResponse.fromResultIntent(data);
        ConstraintLayout constraintLayout = findViewById(R.id.container);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) { // SUCCESS
                showSnackBar(constraintLayout, getString(R.string.connection_succeed));
                createUserInFirestore();
            } else { // ERRORS
                if (response == null) {
                    showSnackBar(constraintLayout, getString(R.string.error_authentication_canceled));
                } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackBar(constraintLayout, getString(R.string.error_no_internet));
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackBar(constraintLayout, getString(R.string.error_unknown_error));
                }
            }
        }
    }

    // ADD USER IN FIRESTORE DB
    @Nullable
    protected FirebaseUser getCurrentUser(){ return FirebaseAuth.getInstance().getCurrentUser(); }

    private void createUserInFirestore(){ //Todo verifier qu il ne se recrée pas
        if (this.getCurrentUser() != null){
            String urlPicture = (this.getCurrentUser().getPhotoUrl() != null) ? this.getCurrentUser().getPhotoUrl().toString() : null;
            String username = this.getCurrentUser().getDisplayName();
            String uid = this.getCurrentUser().getUid();
            UserHelper.createUser(uid, username, urlPicture, null, null).addOnFailureListener(this.onFailureListener());
        }
    }

    protected OnFailureListener onFailureListener(){
        return e -> Toast.makeText(getApplicationContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
    }
}


