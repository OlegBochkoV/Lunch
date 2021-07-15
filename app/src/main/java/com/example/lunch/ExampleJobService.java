package com.example.lunch;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.sql.Time;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class ExampleJobService extends JobService {
    public static final String TAG = "ExampleJobService";
    private boolean jobCancelled = false;
    boolean flag = false;
    int NOTIFICATION_ID = 234;
    String CHANNEL_ID = "mychannelid";
    CharSequence name = "mychannel";
    String Description = "MYCHANNEL";

    @Override
    public boolean onStartJob(JobParameters params) {
        doBackgroundWork(params);
        return true;
    }

    void doBackgroundWork(final JobParameters params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (; ; ) {
                    try {
                        someTask();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (jobCancelled)
                        return;
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "Я работаю");
                }
            }
        }).start();
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job stop");
        jobCancelled = true;
        return false;
    }

    void someTask() throws InterruptedException {
        String notifyDayNumber = Calendar.MONDAY + " " + Calendar.TUESDAY + " " + Calendar.THURSDAY + " " + Calendar.WEDNESDAY + " " + Calendar.FRIDAY;
        Calendar calendar = Calendar.getInstance();
        String temp = calendar.get(Calendar.DAY_OF_WEEK) + "";
        if (notifyDayNumber.contains(temp)) {
            if (calendar.get(Calendar.HOUR_OF_DAY) == 11 && (calendar.get(Calendar.MINUTE) >= 10 && calendar.get(Calendar.MINUTE) <= 25) && !flag) {
                flag = true;

                Intent intent = new Intent(ExampleJobService.this, MainActivity.class);
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);

                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
                    mChannel.setDescription(Description);
                    notificationManager.createNotificationChannel(mChannel);
                }

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ExampleJobService.this, CHANNEL_ID);
                mBuilder.setSmallIcon(android.R.drawable.ic_notification_overlay);
                mBuilder.setContentTitle("Напоминалка");
                mBuilder.setContentText("Будете сегодня кушать?");
                mBuilder.setAutoCancel(true);
                mBuilder.setContentIntent(pendingIntent);
                mBuilder.setChannelId(CHANNEL_ID);
                mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

                notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            } else if (flag) {
                TimeUnit.SECONDS.sleep(900);
                flag = false;
            }
        } else {
            flag = false;
        }
    }
}
