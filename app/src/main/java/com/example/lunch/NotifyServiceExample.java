package com.example.lunch;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class NotifyServiceExample extends Service {
    public static final String TAG = "ExampleJobService";
    boolean flag = false;
    int NOTIFICATION_ID = 234;
    String CHANNEL_ID = "mychannelid";
    CharSequence name = "mychannel";
    String Description = "MYCHANNEL";

    @Override
    public void onCreate() {
        Log.d(TAG,"this1");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        for (; ; ) {
            Log.d(TAG,"this1");
            try {
                someTask();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    void someTask() throws InterruptedException {
        String notifyDayNumber = Calendar.MONDAY + " " + Calendar.TUESDAY + " " + Calendar.THURSDAY + " " + Calendar.WEDNESDAY + " " + Calendar.FRIDAY;
        Calendar calendar = Calendar.getInstance();
        String temp = calendar.get(Calendar.DAY_OF_WEEK) + "";
        if (notifyDayNumber.contains(temp)) {
            if (calendar.get(Calendar.HOUR_OF_DAY) == 12 && calendar.get(Calendar.MINUTE) == 10 && !flag) {
                flag = true;

                Intent intent = new Intent(NotifyServiceExample.this, MainActivity.class);
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);

                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
                    mChannel.setDescription(Description);
                    notificationManager.createNotificationChannel(mChannel);
                }

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(NotifyServiceExample.this, CHANNEL_ID);
                mBuilder.setSmallIcon(android.R.drawable.ic_notification_overlay);
                mBuilder.setContentTitle("Напоминалка");
                mBuilder.setContentText("Будете сегодня кушать?");
                mBuilder.setAutoCancel(true);
                mBuilder.setContentIntent(pendingIntent);
                mBuilder.setChannelId(CHANNEL_ID);
                mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

                notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            } else if (flag) {
                TimeUnit.SECONDS.sleep(200);
                flag = false;
            }
        } else {
            TimeUnit.SECONDS.sleep(60*60);
            flag = false;
        }
    }
}
