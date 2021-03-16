package com.example.goforlunch.users;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.goforlunch.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

public class UsersListView extends Fragment implements UserAdapter.UserRvListener {

    private UserListViewModel userListViewModel;
    private UserAdapter adapter;
    private View view;
    private RecyclerView userRv;
    private UserAdapter.UserRvListener userRvListener;
    private String dateString;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        userRvListener = this;
        userListViewModel = new ViewModelProvider(requireActivity()).get(UserListViewModel.class);
        view = inflater.inflate(R.layout.fragment_user_list, container, false);
        userRv = view.findViewById(R.id.user_rv);
        dateString = userListViewModel.getDateString();
        userListViewModel.getUserListMld().observe(getViewLifecycleOwner(), new Observer<List<UserModel>>() {
            @Override
            public void onChanged(List<UserModel> userModels) {
                adapter = new UserAdapter(userModels, userRvListener);
                userRv.setAdapter(adapter);
                userRv.setLayoutManager(new LinearLayoutManager(getContext()));
            }
        });
        return view;
    }


    @Override
    public void onItemClick(int position){
        UserModel user = userListViewModel.getUserListMld().getValue().get(position);
        if(user.getBookingDate() != null && user.getBookingDate().equals(dateString)){
            Bundle restaurantId = new Bundle();
            restaurantId.putString("restaurantId", user.getRestaurantId());
            Log.e("userView", "restaurant id : " + user.getRestaurantId());
            NavHostFragment.findNavController(this)
                    .navigate(R.id.go_to_details, restaurantId);
        }
    }
}

    class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

        public List<UserModel> usersList;
        public UserRvListener userRvListener;
        public TextView nameTextView;
        public String dateString;

        public UserAdapter(List<UserModel> usersList, UserRvListener userRvListener){
            this.usersList = usersList;
            this.userRvListener = userRvListener;
            Date date = new Date();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            dateString = formatter.format(date);
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

            public ViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                nameTextView = (TextView) itemView.findViewById(R.id.user_name);
            }

            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                userRvListener.onItemClick(position);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.item_user, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            UserModel user = usersList.get(position);
            if(user.getBookingDate() != null && user.getBookingDate().equals(dateString)){
                nameTextView.setText(user.getUsername() + " is eating in " );
                nameTextView.setTypeface(null, Typeface.BOLD);
            }
            else{
                nameTextView.setText(user.getUsername() + " hasn't decided yet");
                nameTextView.setTypeface(null, Typeface.ITALIC);
            }
        }


        @Override
        public int getItemCount() {
            return usersList.size();
        }

        public interface UserRvListener{
            void onItemClick(int position);
        }
}
