package com.example.goforlunch;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.goforlunch.restaurants.RestaurantHelper;
import com.example.goforlunch.restaurants.RestaurantModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.ref.WeakReference;
import java.util.List;

import static android.content.ContentValues.TAG;

class GetDbRestaurantsList extends AsyncTask<Void, Void, List<RestaurantModel>> {

    List<RestaurantModel> restaurantModelList;

    public interface Listeners {
        void onPostExecute(List<RestaurantModel> dbRestaurantsList);
    }

    private final WeakReference<Listeners> callback;

    public GetDbRestaurantsList(Listeners callback){
        this.callback = new WeakReference<>(callback);
    }

    @Override
    protected List<RestaurantModel> doInBackground(Void... voids) {
        RestaurantHelper.getAllRestaurants(new GetAllRestaurantsOnCompleteListener());
        return null;
    }

    @Override
    protected void onPostExecute(List<RestaurantModel> restaurantModels) {
        this.callback.get().onPostExecute(restaurantModels);
        super.onPostExecute(restaurantModels);
    }

    private class GetAllRestaurantsOnCompleteListener implements OnCompleteListener<QuerySnapshot>{
        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if (task.isSuccessful()) {
                // Get the query snapshot from the task result
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {
                    // Get the contact list from the query snapshot
                    restaurantModelList = querySnapshot.toObjects(RestaurantModel.class);
                }

            } else {
                Log.w(TAG, "Error getting documents: ", task.getException());
            }
        }
    }
}
