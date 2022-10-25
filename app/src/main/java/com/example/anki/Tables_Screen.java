package com.example.anki;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tables_Screen extends AppCompatActivity {

    public static SqlHelper dbHelper; // My database
    final Context context = this;// for accessing from function and for saving changed background in settings screen
    public static String backgroundColor;

    public static int BackgroundTheme;

    public static SharedPreferences.Editor editor;

    public static String NotificationFrequency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the current Background color chosen by the user
        final SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // If it is the first time a user opens the app, the color chosen will be white (i.e. 1)
        final int background_int = sharedPreferences.getInt(getString(R.string.Background_color), 1);
        System.out.println(background_int);

        // Set the correct background color
        switch (background_int){
            case 1:
                backgroundColor = "Light";
                BackgroundTheme = R.style.LightTheme;
                break;

            case 2:
                backgroundColor = "Dark";
                BackgroundTheme = R.style.DarkTheme;
                break;

            case 3:
                backgroundColor = "Grey";
                BackgroundTheme = R.style.GreyTheme;
                break;
        }
        setTheme(BackgroundTheme);
        setContentView(R.layout.tables_screem);

        // for the notification Manging system
        NotificationFrequency = sharedPreferences.getString(getString(R.string.Notification_frequncy), "daily");

        if(NotificationFrequency.equals("none")){
            NotificationService.Your_X_SECS = NotificationService.Your_X_SECS * 0;
        }

        if(NotificationFrequency.equals("daily")){
            NotificationService.Your_X_SECS = NotificationService.Your_X_SECS * 24 * 60 * 60;// once a day
        }

        if(NotificationFrequency.equals("semiweekly")){
            NotificationService.Your_X_SECS = NotificationService.Your_X_SECS * 3 * 24 * 60 * 60;// once a day
        }

        if(NotificationFrequency.equals("weekly")){
            NotificationService.Your_X_SECS = NotificationService.Your_X_SECS * 7 * 24 * 60 * 60;// once a day
        }
        System.out.println(NotificationService.Your_X_SECS);
        System.out.println(backgroundColor);

        // Start the service
        Intent intent = getIntent();
        boolean isServiceRunning = intent.getBooleanExtra(getString(R.string.Notification_Running), false);
        if(isServiceRunning == false){
            startService(new Intent(this, NotificationService.class));// start the notification service
        }

        dbHelper = new SqlHelper(this); // Creates database

        final Button new_table_button = findViewById(R.id.NewTable);
        final ListView table_manager = findViewById(R.id.Table_Manager);

        final View screenView = findViewById(R.id.screen);

        new_table_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Here put popup window
                showAlertDialog();

            }
        });
        ArrayList<String> list = Create_Table_List();
        if(list != null)
            table_manager.setAdapter(new Word_List(list, this, "table") );
    }

    public ArrayList<String> Create_Table_List(){
        ArrayList<String> list = new ArrayList<String>();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor= db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        String table_name;

        if (cursor.moveToFirst()) {
            cursor.moveToNext();
            while (!cursor.isAfterLast()) {
                table_name = cursor.getString( cursor.getColumnIndex("name"));
                System.out.println(table_name);
                list.add(table_name + "!@#%&" + GetNumOfWordsInGroup(db, table_name));

                cursor.moveToNext();
            }
        }
        return list;
    }

    public int GetNumOfWordsInGroup(SQLiteDatabase db, String table_name){
        Cursor cursor= db.rawQuery("select count(*) from "+table_name, null);
        cursor.moveToFirst();
        int count= cursor.getInt(0);
        cursor.close();
        return count;
    }

    public void showAlertDialog(){

        // create alert builder
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // set new table layout
        final View new_table_layout = getLayoutInflater().inflate(R.layout.new_table_activity, null);
        builder.setView(new_table_layout);

        // Listen to the EditText
        final EditText new_table_name = new_table_layout.findViewById(R.id.TableNameEditText);

        // Add buttons
        final Button create_table = new_table_layout.findViewById(R.id.CreateGroupButton);
        final TextView cancel = new_table_layout.findViewById(R.id.ExitButton);

        create_table.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String table_name = new_table_name.getText().toString();

                // Find any spaces in the string
                Pattern pattern = Pattern.compile("\\s");
                Matcher matcher = pattern.matcher(table_name);

                // Check if string is empty
                if(table_name.isEmpty()){
                    new_table_name.setHint("Must write a packet name");
                    new_table_name.setHintTextColor(Color.RED);
                }

                // Find any spaces in the string
                else if(matcher.find()){
                    new_table_name.setText("");
                    new_table_name.setHint("Packet name must be one word");
                    new_table_name.setHintTextColor(Color.RED);
                }

                // Check if the table name is only a number
                else if(table_name.matches("-?\\d+(\\.\\d+)?")){
                    new_table_name.setText("");
                    new_table_name.setHint("Packet name must be a word");
                    new_table_name.setHintTextColor(Color.RED);
                }

                else{
                    MainScreen.table_name = table_name;
                    Intent intent = new Intent(context, MainScreen.class);
                    startActivity(intent);
                }
            }
        });


        final AlertDialog dialog = builder.create();
        dialog.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true); // to make the activity dissappear without closing th service
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        menu.findItem(R.id.statistics).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.Settings:
                openSettingsScreen();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openSettingsScreen(){
        Intent intent = new Intent(context, Settings_Screen.class);
        intent.putExtra(getString(R.string.Origin_Activity),getString(R.string.Tables_screen));
        startActivity(intent);
    }


}