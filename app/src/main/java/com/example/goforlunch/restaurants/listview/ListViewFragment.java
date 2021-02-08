package com.example.goforlunch.restaurants.listview;

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

import com.example.goforlunch.R;
import com.example.goforlunch.restaurants.RestaurantModel;
import com.example.goforlunch.restaurants.RestaurantViewModel;

import java.util.List;

public class ListViewFragment extends Fragment implements Adapter.RestaurantRvListener {

    private RestaurantViewModel restaurantViewModel;
    private List<RestaurantModel> restaurantsList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        restaurantViewModel = new ViewModelProvider(requireActivity()).get(RestaurantViewModel.class);
        View root = inflater.inflate(R.layout.fragment_listview, container, false);
        RecyclerView rv = (RecyclerView) root.findViewById(R.id.rv);

        restaurantsList = restaurantViewModel.getRestaurantMutableLiveData().getValue();

        Adapter adapter = new Adapter(restaurantsList,this);
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
        restaurantViewModel.setPosition(position);
        NavHostFragment.findNavController(ListViewFragment.this)
                .navigate(R.id.list_to_details);
    }
}