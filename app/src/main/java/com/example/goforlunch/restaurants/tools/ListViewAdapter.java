package com.example.goforlunch.restaurants.tools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.goforlunch.R;

import java.util.List;

public class ListViewAdapter extends RecyclerView.Adapter<ListViewAdapter.ViewHolder> {

    private List<RestaurantModel> restaurantsList;
    private RestaurantRvListener restaurantRvListener;
    public TextView nameTextView;
    public TextView addressTextView;
    public TextView timeTableTextView;

    public ListViewAdapter(List<RestaurantModel> restaurantModelList, RestaurantRvListener restaurantRvListener){
        this.restaurantsList = restaurantModelList;
        this.restaurantRvListener = restaurantRvListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{


        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            nameTextView = (TextView) itemView.findViewById(R.id.restaurant_name);
            addressTextView = (TextView) itemView.findViewById(R.id.restaurant_address);
            timeTableTextView = (TextView) itemView.findViewById(R.id.restaurant_timetable);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            restaurantRvListener.onItemClick(position);
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
        RestaurantModel restaurant = restaurantsList.get(position);
        nameTextView.setText(restaurant.getName());
        addressTextView.setText(restaurant.getVicinity());
        timeTableTextView.setText("todo");
    }

    @Override
    public int getItemCount() {
        //  TODO évite le crash si on click sur listeView avant le chargement deds données
        return restaurantsList.size();
    }

    public interface RestaurantRvListener{
        void onItemClick(int position);
    }
}
