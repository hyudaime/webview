
package com.app.digilearn.service;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.app.digilearn.DashboardActivity;
import com.app.digilearn.MainActivity;
import com.app.digilearn.R;
import com.app.digilearn.model.JsonNotif;
import com.app.digilearn.model.JsonNotifString;
import com.app.digilearn.model.Notification;
import com.app.digilearn.util.NotificationUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import androidx.core.app.NotificationCompat;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    private NotificationUtils notificationUtils;
    private String mTitle, mMessage;
    private String mId;
    private Intent resultIntent;
    private String idXendid;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {


        try {
            Log.e(TAG, "From: " + remoteMessage.getFrom());

            if (remoteMessage == null)
                return;

            // Check if message contains a data payload.
            if (remoteMessage.getData().size() > 0) {
                Log.e(TAG, "DataOrder Payload: " + remoteMessage.getData().toString());
                try {
                    // JSONObject json = new JSONObject(remoteMessage.getData().toString());

                    JSONObject json = new JSONObject(String.valueOf(remoteMessage.getData().toString()));

                    handleDataMessage(json);

                } catch (Exception e) {

                    Map<String, String> params = remoteMessage.getData();
                    JSONObject object = new JSONObject(params);

                    String id = "";
                    String title = "";
                    String message = "";
                    try {
                        id = object.getString("body");
                        title = object.getString("click_action");
                        message = object.getString("title");
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }

                    handleErrorDataMessage(id, title, message);

                }

                notificationUtils = new NotificationUtils(getApplicationContext());
                notificationUtils.playNotificationSound();

            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        notificationUtils = new NotificationUtils(getApplicationContext());
        notificationUtils.playNotificationSound();
    }

    private void handleDataMessage(JSONObject json) {

        try {


            Gson gson = new Gson();
            JsonNotif responseNotif = new JsonNotif();


            Object aObj = json.get("body");
            if (aObj instanceof Integer) {
                responseNotif = gson.fromJson(json.toString(), JsonNotif.class);
            } else {
                JsonNotifString temp = gson.fromJson(json.toString(), JsonNotifString.class);
                responseNotif.setBody(temp.getBody());
                responseNotif.setIcon(temp.getIcon());
                responseNotif.setSound(temp.getSound());
                responseNotif.setTitle(temp.getTitle());
                responseNotif.setClickAction(temp.getClickAction());
                responseNotif.setShowInForeground(temp.getShowInForeground());
                idXendid = temp.getBody();
            }

            mTitle = responseNotif.getClickAction();
            mMessage = responseNotif.getTitle();
            mId = responseNotif.getBody();

            handleErrorDataMessage(mId, mTitle, mMessage);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

    }

    private void handleErrorDataMessage(String mId, String mTitle, String mMessage) {

        try {

            if (mTitle.equalsIgnoreCase("Notifikasi") || mTitle.contains("Notifikasi")
                    || mMessage.equalsIgnoreCase("Notifikasi") || mMessage.contains("Notifikasi")) {
                resultIntent = new Intent(getApplicationContext(), DashboardActivity.class);
                Notification notification = new Notification(mId, mTitle, mMessage);
                Bundle mBundle = new Bundle();
                mBundle.putParcelable("detail", notification);
                resultIntent.putExtras(mBundle);
                //  showNotificationMessage(getApplicationContext(), mTitle, mMessage, "", resultIntent);

            } else {
                resultIntent = new Intent(getApplicationContext(), MainActivity.class);
            }

            Log.e(TAG, mTitle);
            Log.e(TAG, mMessage);

            resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_ONE_SHOT);
            String channelId = "Default";
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(mTitle)
                    .setContentText(mMessage).setAutoCancel(true).setContentIntent(pendingIntent);

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, "Default channel", NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(channel);
            }
            manager.notify(0, builder.build());


        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }


    }


    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
        Log.v(TAG, "showNotificationMessage");
    }


}
