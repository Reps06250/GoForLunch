package com.example.goforlunch.restaurants.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.goforlunch.MainActivity;
import com.example.goforlunch.R;
import com.example.goforlunch.restaurants.tools.ListViewAdapter;
import com.example.goforlunch.restaurants.tools.RestaurantModel;
import com.example.goforlunch.restaurants.RestaurantViewModel;

import java.util.List;


public class ListView extends Fragment implements ListViewAdapter.RestaurantRvListener {

    private List<RestaurantModel> restaurantsList;
    private ListViewAdapter adapter;
    private RecyclerView rv;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        RestaurantViewModel restaurantViewModel = new ViewModelProvider(requireActivity()).get(RestaurantViewModel.class);
        View root = inflater.inflate(R.layout.fragment_listview, container, false);
        rv = (RecyclerView) root.findViewById(R.id.rv);

        restaurantsList = restaurantViewModel.getRestaurantMutableLiveData().getValue();

        adapter = new ListViewAdapter(restaurantsList,this);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        restaurantViewModel.getRestaurantMutableLiveData().observe(getViewLifecycleOwner(), new Observer<List<RestaurantModel>>() {
            @Override
            public void onChanged(@Nullable List<RestaurantModel> restaurantsList) {
                rv.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });
        return root;
    }

    @Override
    public void onItemClick(int position) {
        MainActivity.setRestaurant(restaurantsList.get(position));
        NavHostFragment.findNavController(ListView.this)
                .navigate(R.id.go_to_details);
    }
}