package com.example.goforlunch.users;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.goforlunch.MainActivity;
import com.example.goforlunch.R;
import com.example.goforlunch.restaurants.RestaurantViewModel;
import com.example.goforlunch.restaurants.tools.ListViewAdapter;
import com.example.goforlunch.restaurants.tools.RestaurantModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class UsersListView extends Fragment implements UserAdapter.UserRvListener {

    private UserListViewModel userListViewModel;
    private List<UserModel> usersList = null;
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
        Date date = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        dateString = formatter.format(date);
        getList();

        return view;
    }

    private void getList() {
        UserHelper.getAllUsers(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Get the query snapshot from the task result
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null) {
                        // Get the users list from the query snapshot
                        usersList = querySnapshot.toObjects(UserModel.class);
                        Collections.sort(usersList);
                        adapter = new UserAdapter(usersList, userRvListener);
                        userRv.setAdapter(adapter);
                        userRv.setLayoutManager(new LinearLayoutManager(getContext()));
                    }
                } else {
                    Log.w(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        UserModel user = usersList.get(position);
        if(user.getBookingDate() != null && user.getBookingDate().equals(dateString)){
            MainActivity.setRestaurant(user.getRestaurant());
            NavHostFragment.findNavController(this)
                    .navigate(R.id.go_to_details);
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
                nameTextView.setText(user.getUsername() + " is eating in " + user.getRestaurant().getName());
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
