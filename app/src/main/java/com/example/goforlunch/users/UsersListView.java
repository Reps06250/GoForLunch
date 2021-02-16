package com.example.goforlunch.users;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.goforlunch.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

public class UsersListView extends Fragment {

    private UserListViewModel userListViewModel;
    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter<UserModel, UserViewHolder> userAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        userListViewModel = new ViewModelProvider(requireActivity()).get(UserListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_user_list,container, false);

        RecyclerView userRv = root.findViewById(R.id.user_rv);
        userRv.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseFirestore = FirebaseFirestore.getInstance();
        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(userListViewModel.getData(), UserModel.class)
                .build();

        userAdapter = new FirestoreRecyclerAdapter<UserModel, UserViewHolder>(options) {
            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
                return new UserViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull UserModel userModel) {
                holder.setUserName(userModel.getUsername());
            }
        };
        userAdapter.startListening();
        userRv.setAdapter(userAdapter);
        return root;
    }

    private class UserViewHolder extends RecyclerView.ViewHolder {
        private View view;
        UserViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        void setUserName(String userName) {
            TextView textView = view.findViewById(R.id.user_name);
            textView.setText(userName);
        }
    }
}