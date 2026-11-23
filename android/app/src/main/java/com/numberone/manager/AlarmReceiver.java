package com.numberone.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String body = intent.getStringExtra("body");
        int id = intent.getIntExtra("notificationId", 1);
        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26 && manager.getNotificationChannel("number_one_alerts") == null) {
            NotificationChannel channel = new NotificationChannel("number_one_alerts", "تنبيهات جلسات NUMBER ONE", NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            manager.createNotificationChannel(channel);
        }
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(context, "number_one_alerts") : new Notification.Builder(context);
        builder.setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle(title).setContentText(body).setStyle(new Notification.BigTextStyle().bigText(body)).setAutoCancel(true).setSound(sound).setPriority(Notification.PRIORITY_MAX);
        manager.notify(id, builder.build());
    }
}