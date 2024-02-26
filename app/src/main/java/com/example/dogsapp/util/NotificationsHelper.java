package com.example.dogsapp.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.dogsapp.R;
import com.example.dogsapp.view.MainActivity;

public class NotificationsHelper {
    private static final String CHANNEL_ID = "Dogs channel id";
    private static final int NOTIFICATION_ID = 123;
    @SuppressLint("StaticFieldLeak")
    private static NotificationsHelper instance;
    private final Context context;

    private NotificationsHelper(Context context) {
        this.context = context;
    }

    public static NotificationsHelper getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationsHelper(context);
        }
        return instance;
    }

    public void createNotification() {
        createNotificationChannel();

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);


        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.dog);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.dog_icon)
                .setLargeIcon(icon)
                .setContentTitle("Dog retrieved")
                .setContentText("This is a notification to let you know that the dog information has been retrieved")
                .setStyle(
                        new NotificationCompat.BigPictureStyle()
                                .bigPicture(icon)
                                .bigLargeIcon(null)
                )
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String description = "Dogs retrieved notifications channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
            channel.setDescription(description);
            @SuppressLint("ServiceCast") NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
