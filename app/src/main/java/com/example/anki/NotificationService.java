package com.example.anki;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

// This is the notification service, it will send the user a notifiaction with the words that a ready for training,
// and will be sent based on the time the user has choosen
public class NotificationService extends Service {

    Timer timer;
    TimerTask timerTask;
    String TAG = "Timers";
    public static int Your_X_SECS = 1; // this will controll the amount of time between each notification
    // The user will be able to change it while in the settings
    // If the user has chosen not to get any notification this variable will be set to 0, in this case no
    // notification will be sent

    final Context context = this;
    final String CHANNEL_ID = "SuperWord";
    final SqlHelper dpHelper = Tables_Screen.dbHelper;

    @Override
    public IBinder onBind(Intent arg0){
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        createNotificationChannel();

        startTimer();
        return  START_STICKY;
    }

    @Override
    public void onCreate(){
        Log.e(TAG, "onCreate");

    }

    @Override
    public void onDestroy(){
        Log.e(TAG, "onDestroy");
        stoptimertask();
        super.onDestroy();
    }

    final Handler handler = new Handler();

    public void startTimer(){
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        if(Your_X_SECS != 0)
            timer.schedule(timerTask, 5000, Your_X_SECS*1000);

    }

    public void stoptimertask(){
        //stop the timer if it's not already null
        if(timer != null){
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask(){

        timerTask = new TimerTask(){
            public void run(){

                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        //Sending the notification
                        Intent intent = new Intent(context, Tables_Screen.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                                .setSmallIcon(R.drawable.cerclebackground)
                                .setContentTitle("SuperWord")
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(createNotificationText()))
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true);

                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

                        final int notificationId = 1; // simple text
                        // notificationId is a unique int for each notification that you must define
                        notificationManager.notify(notificationId, builder.build());
                    }
                });
            }
        };
    }


    private void createNotificationChannel(){
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private String createNotificationText(){
        // This creates the Text for the notification, it gets all the words from every table, and checks which ones are available
        // Thus the text will be: Table_name = amount_of_ready_words
        SQLiteDatabase db = dpHelper.getWritableDatabase();
        Cursor cursor= db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        String text = "You have words to review: \n";

        cursor.moveToFirst();

        while(cursor.moveToNext()){
            int numReadyWords = countReadyWords(cursor.getString(0), db);
            if(numReadyWords == 0)
                continue;
            text = text + cursor.getString(0) + ": " + numReadyWords+"\n";

        }
        return text;
    }

    private int countReadyWords(String table_name, SQLiteDatabase db){
        // Counts the amount of words ready in a table
        int count = 0;
        Cursor cursor = db.rawQuery("SELECT * FROM "+ table_name, null);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm");
        String date = dateFormat.format(calendar.getTime());
        String[] date_split = date.split(":");

        int[] date_now = new int[date_split.length];

        for(int i=0;i<date_now.length;i++)
            date_now[i] = Integer.parseInt(date_split[i]);

        boolean words_remain = cursor.moveToFirst();

        while(words_remain){
            String date_review = cursor.getString(6);
            date_split = date_review.split(":");

            int[] review_time = new int[date_split.length];

            for(int i=0;i<date_now.length;i++)
                review_time[i] = Integer.parseInt(date_split[i]);

            if(Check_If_Review_Ready(date_now, review_time))
                count++;

            words_remain = cursor.moveToNext();
        }
        return count;
    }

    private boolean Check_If_Review_Ready(int[] now, int[] review){
        // Words are ready for training only if the review date and time of the word is already in the past

        boolean Review_ready = false;
        for(int i=0; i<now.length-1;i++){
            if(now[i]>review[i]){
                Review_ready = true;
            }
        }
        if((!Review_ready) && (now[3] == review[3]))
            Review_ready = true;
        return Review_ready;
    }
}
