package com.example.goforlunch.restaurants.tools;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.goforlunch.R;
import com.example.goforlunch.restaurants.RestaurantViewModel;
import com.example.goforlunch.restaurants.models.DbRestaurantModel;
import com.example.goforlunch.restaurants.models.RestaurantModel;
import com.google.android.gms.maps.model.LatLng;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.List;

public class ListViewAdapter extends RecyclerView.Adapter<ListViewAdapter.ViewHolder> {

    private List<RestaurantModel> restaurantsList;
    private RestaurantRvListener restaurantRvListener;
    private Location location;

    public ListViewAdapter(List<RestaurantModel> restaurantModelList, RestaurantRvListener restaurantRvListener, Location location){
        this.restaurantsList = restaurantModelList;
        this.restaurantRvListener = restaurantRvListener;
        this.location = location;
        String rlist = restaurantsList.size() + " ";
        for(RestaurantModel resto : restaurantsList){
            rlist =rlist + resto.getPlace().getName() + " ";
        }
        Log.e("rlistadapter", rlist);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView nameTextView;
        public TextView addressTextView;
        public TextView timeTableTextView;
        public TextView distanceTextView;
        public ImageView restaurantPhoto;
        public RatingBar ratingBar;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            nameTextView = (TextView) itemView.findViewById(R.id.name_tv);
            addressTextView = (TextView) itemView.findViewById(R.id.address_tv);
            timeTableTextView = (TextView) itemView.findViewById(R.id.time_table_tv);
            distanceTextView = (TextView) itemView.findViewById(R.id.distance_tv);
            restaurantPhoto = (ImageView) itemView.findViewById(R.id.restaurant_photo);
            ratingBar = (RatingBar) itemView.findViewById(R.id.rating_stars);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            restaurantRvListener.onItemClick(position);
        }

        private void setImage(RestaurantModel restaurant) {
            if(restaurant.getBitmap() == null){
                restaurantPhoto.setImageResource(R.drawable.no_pictures);
            }
            else{
                restaurantPhoto.setImageBitmap(restaurant.getBitmap());
            }
        }

        public void setRating(RestaurantModel restaurant) {
            if(restaurant.getStars() == 0){
                ratingBar.setVisibility(View.GONE);
            }
            else{
                ratingBar.setRating(restaurant.getStars());
            }
        }

        public void setTimeTable(RestaurantModel restaurant){
//            String timeTable = "";
//            LocalTime localTime = LocalTime.now();
//            int cHour = localTime.getHour();
//            String cWeekDay = getCurrentWeekDay();
//            for(int i = 0; i < restaurant.getPlace().getOpeningHours().getWeekdayText().size(); i++){
//                if(cWeekDay.equals(restaurant.getPlace().getOpeningHours().getWeekdayText().get(i).split(":"))){
//                    if(cHour < 13){
//                        timeTable  = restaurant.getPlace().getOpeningHours().getWeekdayText().get(i);
//                    }
//                    else if (i == restaurant.getPlace().getOpeningHours().getWeekdayText().size()){
//                        timeTable  = restaurant.getPlace().getOpeningHours().getWeekdayText().get(0);
//                    }
//                    else {
//                        timeTable  = restaurant.getPlace().getOpeningHours().getWeekdayText().get(i+1);
//                    }
//
//                }
//            }
            timeTableTextView.setText(restaurant.getPlace().getOpeningHours().getWeekdayText().get(0));
        }

        private String getCurrentWeekDay() {
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            switch (day) {
                case Calendar.SUNDAY:
                    return "Sunday";
                case Calendar.MONDAY:
                    return "Monday";
                case Calendar.TUESDAY:
                    return "Tuesday";
                case Calendar.THURSDAY:
                    return "Thursday";
                case Calendar.WEDNESDAY:
                    return "Wednesday";
                case Calendar.FRIDAY:
                    return "Friday";
                case Calendar.SATURDAY:
                    return "Saturday";
            }
            return null;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.item_restaurant, parent, false);
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String rlist = restaurantsList.size() + " ";
        for(RestaurantModel resto : restaurantsList){
            rlist =rlist + resto.getPlace().getName() + " ";
        }
        Log.e("rlistOnBind", rlist);
        RestaurantModel restaurant = restaurantsList.get(position);
        Log.e("rlistOnBind2", position + " " + restaurant.getPlace().getName());
        holder.setImage(restaurant);
        holder.nameTextView.setText(restaurant.getPlace().getName());
        holder.addressTextView.setText(getShortAddress(restaurant.getPlace().getAddress()));
        holder.distanceTextView.setText(getDistance(restaurant.getPlace().getLatLng()) + "m");
        holder.timeTableTextView.setText("todo");
        holder.setRating(restaurant);
        if(restaurant.getPlace().getOpeningHours() != null){
            holder.setTimeTable(restaurant);
        }
        else{
            holder.timeTableTextView.setText("unknow");
        }
    }


    public String getShortAddress(String address){
        char someChar = ',';
        int count = 0;
        for (int i = 0; i < address.length(); i++) {
            if (address.charAt(i) == someChar) {
                count++;
            }
        }
        return address.split(",")[count-3];
    }

    private int getDistance(LatLng latLng) {
        float distance;
        Location restaurantLocation = new Location("");
        restaurantLocation.setLatitude(latLng.latitude);
        restaurantLocation.setLongitude(latLng.longitude);
        distance = location.distanceTo(restaurantLocation);
        return Math.round(distance);
    }

    @Override
    public int getItemCount() {
        return restaurantsList.size();
    }

    public interface RestaurantRvListener{
        void onItemClick(int position);
    }
}
