package com.example.goforlunch.users;


import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.goforlunch.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class UsersListView extends Fragment {

    private UserListViewModel userListViewModel;
    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter<UserModel, UserViewHolder> userAdapter;
    private View view;
    private userRvListener userRvListener;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        userListViewModel = new ViewModelProvider(requireActivity()).get(UserListViewModel.class);
        view = inflater.inflate(R.layout.fragment_user_list,container, false);

        RecyclerView userRv = view.findViewById(R.id.user_rv);
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
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull UserModel user) {
                holder.setUserName(user);
            }
        };
        userAdapter.startListening();
        userRv.setAdapter(userAdapter);
        return view;
    }

    private class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private View view;
        private TextView textView = view.findViewById(R.id.user_name);

        UserViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setUserName(UserModel user) {
            if(user.getRestaurant().getName() != null){
                textView.setText(user.getUsername() + " is eating in " + user.getRestaurant().getName());
                textView.setTypeface(null, Typeface.BOLD);
            }
            else{
                textView.setText(user.getUsername() + " hasn't decided yet");
            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            userRvListener.onItemClick(position);
        }

//        public void setUserImage(String urlPicture) {
//            ImageView userImage = (ImageView) view.findViewById(R.id.user_image);
//            Glide.with(getContext())
//                    .load(urlPicture)
//                    .into(userImage);
//        }
    }

    public interface userRvListener{
        void onItemClick(int position);
    }
}