package com.example.anki;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;


// Manages the Settings Screen
public class Settings_Screen extends AppCompatActivity {

    final String BackgroundColor = Tables_Screen.backgroundColor;
    final int BackgroundTheme = Tables_Screen.BackgroundTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(BackgroundTheme);
        setContentView(R.layout.settings_screen);

        // The background color buttons
        final RadioButton Light = findViewById(R.id.LightMode);
        final RadioButton Dark = findViewById(R.id.DarkMode);
        final RadioButton Grey = findViewById(R.id.GreyMode);

        final RadioGroup BackgroundColorManager = findViewById(R.id.BackgroundColorManager);

        // Choose the already selected background color
        if(BackgroundColor.equals("Light")){
            BackgroundColorManager.check(R.id.LightMode);
        }

        if(BackgroundColor.equals("Dark")){
            BackgroundColorManager.check(R.id.DarkMode);
        }

        if(BackgroundColor.equals("Grey")){
            BackgroundColorManager.check(R.id.GreyMode);
        }

        // Setts the Buttons for background theme
        Light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangeBackgroundTheme("Light");
            }
        });

        Dark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangeBackgroundTheme("Dark");

            }
        });

        Grey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangeBackgroundTheme("Grey");
            }
        });


        // For Notification Time
        final RadioButton none = findViewById(R.id.none_frequency);
        final RadioButton daily = findViewById(R.id.daily_frequency);
        final RadioButton semiweekly = findViewById(R.id.semiweekly_frequency);
        final RadioButton weekly = findViewById(R.id.weekly_frequency);

        final RadioGroup NotificationFrequencyManager = findViewById(R.id.Notification_frequency);

        final String frequency = Tables_Screen.NotificationFrequency;
        // Choose the already selected notification frequency
        if(frequency.equals("none")){
            NotificationFrequencyManager.check(R.id.none_frequency);
        }

        if(frequency.equals("daily")){
            NotificationFrequencyManager.check(R.id.daily_frequency);
        }

        if(frequency.equals("semiweekly")){
            NotificationFrequencyManager.check(R.id.semiweekly_frequency);
        }

        if(frequency.equals("weekly")){
            NotificationFrequencyManager.check(R.id.weekly_frequency);
        }

        // Setts the buttons for notification frequency
        none.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangeNotificationFrequency("none");
            }
        });

        daily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangeNotificationFrequency("daily");
            }
        });

        semiweekly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangeNotificationFrequency("semiweekly");
            }
        });

        weekly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangeNotificationFrequency("weekly");
            }
        });
    }

    public void ChangeBackgroundTheme(String BackgroundColor){
        // Changes the current Background theme to the chosen one, and saves it,
        // so that, when the user opens the app again his chosen background theme will already be set

        // for saving the current background theme
        final SharedPreferences.Editor editor = Tables_Screen.editor;

        if(BackgroundColor.equals("Light")){
            Tables_Screen.backgroundColor = "Light";
            editor.putInt("Background color", 1);
            Tables_Screen.BackgroundTheme = R.style.LightTheme;
        }

        if(BackgroundColor.equals("Dark")){
            Tables_Screen.backgroundColor = "Dark";
            editor.putInt("Background color", 2);
            Tables_Screen.BackgroundTheme = R.style.DarkTheme;
        }

        if(BackgroundColor.equals("Grey")){
            Tables_Screen.backgroundColor = "Grey";
            editor.putInt("Background color", 3);
            Tables_Screen.BackgroundTheme = R.style.GreyTheme;
        }
        editor.apply();
        Intent intent = new Intent(Settings_Screen.this, Settings_Screen.this.getClass());
        intent.putExtra(getString(R.string.Origin_Activity),getIntent().getStringExtra(getString(R.string.Origin_Activity))); // Saves the last Activity that was open before the settings
        startActivity(intent);

    }

    public void ChangeNotificationFrequency(String frequency){
        // Changes the current frequency to the chosen one, and saves it,
        // so that, when the user opens the app again his chosen frequency will already be set

        final SharedPreferences.Editor editor = Tables_Screen.editor;

        if(frequency.equals("none")){
            NotificationService.Your_X_SECS = 0;
            editor.putString(getString(R.string.Notification_frequncy), "none");
            stopService(new Intent(this, NotificationService.class));
            NotificationService.Your_X_SECS = 1;

        }

        if(frequency.equals("daily")){
            NotificationService.Your_X_SECS = NotificationService.Your_X_SECS * 24 * 60 * 60;// once a day
            editor.putString(getString(R.string.Notification_frequncy), "daily");
            startService(new Intent(this, NotificationService.class));// start the nutification thingy
        }

        if(frequency.equals("semiweekly")){
            NotificationService.Your_X_SECS = NotificationService.Your_X_SECS * 3 * 24 * 60 * 60;// once a day
            editor.putString(getString(R.string.Notification_frequncy), "semiweekly");
            startService(new Intent(this, NotificationService.class));// start the nutification thingy
        }

        if(frequency.equals("weekly")){
            NotificationService.Your_X_SECS = NotificationService.Your_X_SECS * 7 * 24 * 60 * 60;// once a day
            editor.putString(getString(R.string.Notification_frequncy), "weekly");
            startService(new Intent(this, NotificationService.class));// start the nutification thingy
        }
        editor.apply();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // This function is used in case the user press the back button on the phone.
        // This function makes that when the user press the back button it will return him to the previous activity
        // that was open before the seetings screen
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // if the user press the back button
            Intent intent = getIntent();
            String origin_activity = intent.getStringExtra(getString(R.string.Origin_Activity));
            if(origin_activity.equals(getString(R.string.Tables_screen))){
                intent = new Intent(this, Tables_Screen.class);
                intent.putExtra(getString(R.string.Notification_Running), true);
                startActivity(intent);
            }

            if(origin_activity.equals(getString(R.string.MainScreen))){
                intent = new Intent(this, MainScreen.class);
                startActivity(intent);
            }

            if(origin_activity.equals(getString(R.string.new_word_layout))){
                intent = new Intent(this, New_Word_screen.class);
                startActivity(intent);
            }

            if(origin_activity.equals(getString(R.string.Train_Screen))){
                intent = new Intent(this, Train_Screen.class);
                startActivity(intent);
            }

            if(origin_activity.equals(getString(R.string.Word_Updater_Screen))){
                intent = new Intent(this, Word_Updater_Screen.class);
                startActivity(intent);
            }
        }
        return true;
    }
}