package com.example.goforlunch;

import android.content.Intent;
import android.os.Bundle;

import com.example.goforlunch.restaurants.RestaurantViewModel;
import com.example.goforlunch.users.UserHelper;
import com.example.goforlunch.users.UserModel;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.util.Log;
import android.view.MenuInflater;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity{

    private final int RC_SIGN_IN = 123;
    public UserModel user;
    private RestaurantViewModel restaurantViewModel;
    private AppBarConfiguration appBarConfiguration;
    private NavigationView navigationView;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startSignInActivity();

        restaurantViewModel = new ViewModelProvider(this).get(RestaurantViewModel.class);
        setContentView(R.layout.activity_main);

        //TOOLBAR
        Toolbar toolbar = findViewById (R.id.toolbar);
        setSupportActionBar (toolbar);

        //NAVIGATION DRAWER and BOTTOM NAVIGATION
        //je déclare mon navigation drawer et le navigation view
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById ( R.id.navigation_view );
        // je configure mon appBar
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_map, R.id.navigation_restaurants_list, R.id.navigation_users_list)
                .setOpenableLayout(drawer)
                .build();
        //je déclare le navcontroller avec le fragment hote
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        // Configure ma Toolbar pour une utilisation avec le NavController.
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        // Configure mon NavigationDrawer pour une utilisation avec le NavController.
        NavigationUI.setupWithNavController(navigationView, navController);
        // je déclare mon bottomNavigation
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        // Configure mon BottomNavigation pour une utilisation avec le NavController.
        NavigationUI.setupWithNavController(bottomNavigation, navController);
        drawerClick();

        View navView = navigationView.inflateHeaderView ( R.layout.nav_header_main);
//            NavProfileImage = navView.findViewById ( R.id.nav_profile_image );
//            NavProfileFullName =  navView.findViewById ( R.id.nav_user_full_name );
//            NavProfileEmail =  navView.findViewById ( R.id.nav_user_email );
    }

    private void drawerClick() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if(id == R.id.fragment_restaurant_view && user.getRestaurantId() != null) {
                    NavHostFragment.findNavController(getCurrentFragment())
                            .navigate(R.id.go_to_details);
                }
                else if(id == R.id.settings) {
                }
                else if(id == R.id.log_out) {
                    FirebaseAuth.getInstance().signOut();
                    startSignInActivity();
                }
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    public Fragment getCurrentFragment(){
        for (Fragment fragment : getSupportFragmentManager().getFragments()){
            if(fragment.isVisible()){
                return fragment;
            }
        }
        return null;
    }


    //This will help to automatically handle the back arrow navigation and also the back button when pressed
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();
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
//                invalidateOptionsMenu(); //todo
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                Log.e("emptyList", "onQueryTextChange " + newText);
                restaurantViewModel.getFiltredRestaurantsList(newText);
                return false;
            }
        });
        return true;
    }


    //     ----------------- LOGIN ---------------------
    //LAUNCH
    private void startSignInActivity(){
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginTheme)
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
//        Snackbar.make(constraintLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data){
        IdpResponse response = IdpResponse.fromResultIntent(data);
        ConstraintLayout constraintLayout = findViewById(R.id.container);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) { // SUCCESS
//                showSnackBar(constraintLayout, getString(R.string.connection_succeed));
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
            DocumentReference userRef = UserHelper.getUsersCollection().document(this.getCurrentUser().getUid());
            userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (!document.exists()) {
                            String urlPicture = (getCurrentUser().getPhotoUrl() != null) ? getCurrentUser().getPhotoUrl().toString() : null;
                            String username = getCurrentUser().getDisplayName();
                            String uid = getCurrentUser().getUid();
                            UserHelper.createUser(uid, username, urlPicture, null, null).addOnFailureListener(onFailureListener());
                            user = new UserModel(uid, username, urlPicture, null, null);
                            restaurantViewModel.setUser(user);
                        }
                        else{
                            getCurrentUserModel();
                        }
                    }
                }
            });
        }
    }

    void getCurrentUserModel(){
        Log.e("userrr", "getUser");
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        UserHelper.getUser(userId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                user = documentSnapshot.toObject(UserModel.class);
                restaurantViewModel.setUser(user);
                Log.e("userrr", "getUser success " + user.getUsername());
            }
        });
    }

    protected OnFailureListener onFailureListener(){
        return e -> Toast.makeText(getApplicationContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
    }
}


