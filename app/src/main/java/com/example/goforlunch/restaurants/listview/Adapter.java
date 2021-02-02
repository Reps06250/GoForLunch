package com.example.goforlunch.restaurants.listview;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.goforlunch.R;
import com.example.goforlunch.restaurants.RestaurantModel;

import java.util.List;

class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private List<RestaurantModel> restaurantsList;
    private RestaurantRvListener restaurantRvListener;

    public Adapter(List<RestaurantModel> restaurantModelList, RestaurantRvListener restaurantRvListener){
        this.restaurantsList = restaurantModelList;
        this.restaurantRvListener = restaurantRvListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView nameTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            nameTextView = (TextView) itemView.findViewById(R.id.restaurant_name);
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
        RestaurantModel restaurantModel = restaurantsList.get(position);
        // Set item views based on your views and data model
        TextView textView = holder.nameTextView;
        textView.setText(restaurantModel.getName());
    }

    @Override
    public int getItemCount() {
        //  TODO évite le crash si on click sur listeView avant le chargement deds données
        return restaurantsList.size();
    }

    interface RestaurantRvListener{
        void onItemClick(int position);
    }
}
