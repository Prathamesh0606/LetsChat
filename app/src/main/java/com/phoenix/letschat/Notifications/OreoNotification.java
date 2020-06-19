package com.phoenix.letschat.Notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

public class OreoNotification extends ContextWrapper {


    private static final String CHANNEL_ID = "com.phoenix.letschat";
    private static final String CHANNEL_NAME = "letschat";

    private NotificationManager notificationManager;
    public OreoNotification(Context base) {
        super(base);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CreateChannel();
        }
    }
    @TargetApi(Build.VERSION_CODES.O)
    private void CreateChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,CHANNEL_NAME,NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(false);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(channel);


    }

    public NotificationManager getManager() {
        if(notificationManager == null) {
            notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);


        }
        return notificationManager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getOreoNotification(String title, String body, PendingIntent pendingIntent, Uri soudUri, String icon) {

        return new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(Integer.parseInt(icon))
                .setSound(soudUri)
                .setAutoCancel(true);

    }
}
