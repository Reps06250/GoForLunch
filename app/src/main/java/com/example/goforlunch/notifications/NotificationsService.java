package com.example.goforlunch.notifications;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.goforlunch.MainActivity;
import com.example.goforlunch.R;
import com.example.goforlunch.restaurants.models.DbRestaurantModel;
import com.example.goforlunch.restaurants.models.RestaurantModel;
import com.example.goforlunch.restaurants.tools.GetRestaurantsList;
import com.example.goforlunch.users.UserHelper;
import com.example.goforlunch.users.UserModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class NotificationsService extends FirebaseMessagingService implements  GetRestaurantsList.Listeners{

    private UserModel user;

    @SuppressLint("WrongThread")
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.e("notification", "onMessageReceived");
        super.onMessageReceived(remoteMessage);
        user = getCurrentUserModel();
        Date date = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = formatter.format(date);
        if (remoteMessage.getNotification() != null && user.getBookingDate().equals(dateString)) {
            GetRestaurantsList getRestaurantsList = new GetRestaurantsList(this,"notification", user.getRestaurantId(), this);
            getRestaurantsList.execute();
        }
    }

    @Override
    public void onPostExecute(List<RestaurantModel> restaurantsList, String from, List<DbRestaurantModel> dbRestaurantList) {
        String liste;
        RestaurantModel restaurant = restaurantsList.get(0);
        if(restaurant.getInterestedUsers().size() == 1){ liste = "/n Alone :("; }
        else{ liste = getCoUsersString(restaurant); }
        String message = "Is in the restaurant : " + restaurant.getPlace().getName() + ", " + restaurant.getPlace().getAddress() + liste;
        this.sendVisualNotification(message);
    }

    private String getCoUsersString(RestaurantModel restaurant) {
        String finalString = "/nWith : ";
        int forNoCommaAtFirst = 0;
        for(UserModel interstedUser : restaurant.getInterestedUsers()){
                if(forNoCommaAtFirst == 0){
                    finalString = finalString + interstedUser.getUsername();
                    forNoCommaAtFirst++;
                }
                else{
                    finalString = finalString + ", " + interstedUser.getUsername();
                }
        }
        return finalString;
    }

    private void sendVisualNotification(String messageBody) {

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        // Create a Style for the Notification
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(getString(R.string.notification_title));
        inboxStyle.addLine(messageBody);

        // Create a Channel
        String channelId = getString(R.string.default_notification_channel_id);

        // Build a Notification object
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_restaurant)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.notification_title))
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentIntent(pendingIntent)
                        .setStyle(inboxStyle);

        // Add the Notification to the Notification Manager and show it.
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Support Version >= Android 8
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Message provenant de Firebase";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        // Show notification
        String NOTIFICATION_TAG = "FIREBASE";
        int NOTIFICATION_ID = 007;
        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notificationBuilder.build());
    }

    private UserModel getCurrentUserModel(){
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        UserHelper.getUser(userId).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                user = documentSnapshot.toObject(UserModel.class);
            }
        });
        return user;
    }

}
