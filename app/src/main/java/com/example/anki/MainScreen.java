package com.example.anki;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainScreen extends AppCompatActivity implements View.OnClickListener{

    // Important Variables
    private SqlHelper dbHelper;
    public static SQLiteDatabase mainDB;
    public static String table_name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Tables_Screen.BackgroundTheme);
        setContentView(R.layout.main_screen);

        // Create a new SQL table with the name of the language
        dbHelper = Tables_Screen.dbHelper;
        mainDB = dbHelper.getWritableDatabase();
        dbHelper.onCreate(mainDB, table_name);

        final Button train = findViewById(R.id.TrainButton);
        final Button newWord = findViewById(R.id.NewWordButton);
        final Button wordManagerButton = findViewById(R.id.wordManagerButton);
        final ListView wordManager = findViewById(R.id.Word_Manager);
        final TextView imgBackButton = findViewById(R.id.ArrowBackButton);

        //Control the TimeManager and all the buttons inside it
        Time_Manager_Manager();

        // Set number of word ready to train on the train button
        train.setText("Train: " + SetNumberOfWordsReadyToTrain());

        // for error checking - delete later
        Cursor cursor = mainDB.rawQuery("SELECT * FROM " + MainScreen.table_name, null);
        StringBuffer buffer = new StringBuffer();
        while(cursor.moveToNext()){
            buffer.append("Id: " + cursor.getString(0)+"\n");
            buffer.append("word: " + cursor.getString(1)+"\n");
            buffer.append("description: " + cursor.getString(2)+"\n");
            buffer.append("notes: " + cursor.getString(3)+"\n");
            buffer.append("last_time_revisited: " + cursor.getString(4)+"\n");
            buffer.append("time_between_reviews: " + cursor.getString(5)+"\n");
            buffer.append("next_review_time: " + cursor.getString(6)+"\n");
            buffer.append("reviewed_times: " + cursor.getString(7)+"\n");
            buffer.append("correct_reviewd_times: " + cursor.getString(8)+"\n\n\n");
        }
        System.out.println(buffer.toString());

        train.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // opens the train screen
                // first it checks if there are any words to train
                Cursor cursor = mainDB.rawQuery("SELECT count(*) FROM " + table_name, null);
                cursor.moveToFirst();
                int count_of_rows = cursor.getInt(0);
                cursor.close();
                if(count_of_rows == 0){
                    Toast toast = Toast.makeText(getApplicationContext(), "To train on words, first you need to create them", Toast.LENGTH_SHORT);
                    toast.show();
                    toast = Toast.makeText(getApplicationContext(), "For that press on ADD NEW WORD", Toast.LENGTH_SHORT);
                    toast.show();
                    cursor.close();
                    return;
                }// if no words created yet

                boolean HasWords = Check_If_Has_Words_To_Train();

                if(HasWords == false){
                    Toast toast = Toast.makeText(getApplicationContext(), "You don't have any words to train on right now", Toast.LENGTH_LONG);
                    toast.show();
                    toast = Toast.makeText(getApplicationContext(), "You need to wait for your reviews to appear again", Toast.LENGTH_LONG);
                    toast.show();
                    cursor.close();
                    return;
                }// if there are no available words to train
                open_Train_screen();
            }
        });

        newWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                open_New_Word_Screen();
            }
        });

        wordManager.setAdapter(new Word_List(Create_Word_List(), this, "word") );// create the list view

        // The button closes and opens the Time Manager
        wordManagerButton.setText("Word Manager                                                 ⇧");

        wordManagerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(wordManagerButton.getText().toString().equals("Word Manager                                                 ⇧")){
                    wordManager.setVisibility(View.VISIBLE);
                    wordManagerButton.setText("Word Manager                                                 ⇩");
                }
                else{
                    wordManager.setVisibility(View.INVISIBLE);
                    wordManagerButton.setText("Word Manager                                                 ⇧");
                }

            }
        });
        wordManager.setVisibility(View.INVISIBLE);


        imgBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                table_name = "";
                back();
            }
        });

    }

    @Override
    public void onClick(View v) {
        // the function that all the time manger buttons will use
        // opens a listview and shows all the reviews a user gets each hour in the day he chose

        ListView Reviews_of_the_day = findViewById(R.id.Reviews_of_the_day);
        LinearLayout linearLayout = findViewById(R.id.TimeManager);

        if(Reviews_of_the_day.getVisibility() == View.GONE) {
            // If they haven't been pressed yet
            for (int childIndex = 0; childIndex < linearLayout.getChildCount(); childIndex++) {
                if (childIndex != linearLayout.indexOfChild(v)) {
                    linearLayout.getChildAt(childIndex).setVisibility(View.GONE);
                }
            }

            Reviews_of_the_day.setVisibility(View.VISIBLE);

            int day_chosen = linearLayout.indexOfChild(v);

            int current_day_of_the_week = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm");
            String date = dateFormat.format(calendar.getTime());
            String[] date_split = date.split(":");

            // If we are talking about a future day get the right date
            int new_day = Integer.parseInt(date_split[2]) + (day_chosen - (current_day_of_the_week - 1));
            date_split[2] = String.valueOf(new_day);

            boolean is_current_day = (day_chosen - (current_day_of_the_week - 1)) == 0;


            Reviews_of_the_day.setAdapter(new Word_List(Create_Day_Order_List(date_split, is_current_day), this, "day time"));
        }

        else{
            // If the user pressed the button
            // closes the listview and shows the other buttons
            Reviews_of_the_day.setVisibility(View.GONE);

            for (int childIndex = 0; childIndex < linearLayout.getChildCount(); childIndex++) {
                if (childIndex != linearLayout.indexOfChild(v) && childIndex != linearLayout.indexOfChild(Reviews_of_the_day)) {
                    linearLayout.getChildAt(childIndex).setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public void Time_Manager_Manager(){
        // Creates all the buttons for the Time Manager
        Button[] days_of_the_week = new Button[7];

        final Button sunday = findViewById(R.id.SundayButton);
        sunday.setOnClickListener(this);
        days_of_the_week[0] = sunday;

        final Button monday = findViewById(R.id.MondayButton);
        monday.setOnClickListener(this);
        days_of_the_week[1] = monday;

        final Button Tuesday = findViewById(R.id.TuesdayButton);
        Tuesday.setOnClickListener(this);
        days_of_the_week[2] = Tuesday;

        final Button Wednesday = findViewById(R.id.WednesdayButton);
        Wednesday.setOnClickListener(this);
        days_of_the_week[3] = Wednesday;

        final Button Thursday = findViewById(R.id.ThursdayButton);
        Thursday.setOnClickListener(this);
        days_of_the_week[4] = Thursday;

        final Button Friday = findViewById(R.id.FridayButton);
        Friday.setOnClickListener(this);
        days_of_the_week[5] = Friday;

        final Button Saturday = findViewById(R.id.SaturdayButton);
        Saturday.setOnClickListener(this);
        days_of_the_week[6] = Saturday;

        int current_day_of_the_week = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        for(int day = current_day_of_the_week-1; day < days_of_the_week.length; day++){
            // add to future days the amount of words the user will get to review

            // Get the current date
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm");
            String date = dateFormat.format(calendar.getTime());
            String[] date_split = date.split(":");

            // If we are talking about a future day get the right date
            int new_day = Integer.parseInt(date_split[2]) + (day - (current_day_of_the_week-1));
            date_split[2] = String.valueOf(new_day);

            boolean is_current_day = (day - (current_day_of_the_week-1))==0;
            int amount_of_words_to_review = Get_Amount_Review_In_Day(date_split, is_current_day);

            // Add the amount of words to the button text
            String day_name = days_of_the_week[day].getText().toString();
            int index_of_plus = day_name.indexOf("+");

            String new_day_name = day_name.substring(0, index_of_plus);

            new_day_name = new_day_name + "+" + amount_of_words_to_review;

            days_of_the_week[day].setText(new_day_name);
        }

        for(int day = 0; day<days_of_the_week.length;day++){
            //disable all the buttons that don't have reviews in them
            String day_name = days_of_the_week[day].getText().toString();
            int index_of_plus = day_name.indexOf("+");

            int number_of_review = Integer.parseInt(day_name.substring(index_of_plus+1));

            if(number_of_review ==0){
                days_of_the_week[day].setEnabled(false);
            }
        }
    }// Manages the Time Manger Buttons

    public void open_New_Word_Screen(){
        Intent intent = new Intent(this, New_Word_screen.class);
        startActivity(intent);
    }

    public void open_Train_screen(){
        Intent intent = new Intent(this, Train_Screen.class);
        startActivity(intent);
    }

    public boolean Check_If_Has_Words_To_Train(){
        // Words are ready for training only if the review date and time of the word is already in the past
        // Basically it compares the current date to the next review date of the word
        Cursor cursor = mainDB.rawQuery("SELECT * FROM " + table_name, null);
        cursor.moveToFirst();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm");
        String date = dateFormat.format(calendar.getTime());
        String[] date_split = date.split(":");

        int[] date_now = new int[date_split.length];

        for(int i=0;i<date_now.length;i++)
            date_now[i] = Integer.parseInt(date_split[i]);

        boolean table_not_ended = true;

        while(table_not_ended){
            String date_review = cursor.getString(6);
            date_split = date_review.split(":");

            int[] review_time = new int[date_split.length];

            for(int i=0;i<date_now.length;i++)
                review_time[i] = Integer.parseInt(date_split[i]);

            if(Check_If_Review_Ready(date_now, review_time))
                return true;
            table_not_ended = cursor.moveToNext();
        }
        return false;
    } // checks if there are words ready to train

    public boolean Check_If_Review_Ready(int[] now, int[] review){
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
    } // Checks if the word is ready to review

    public ArrayList<String> Create_Word_List(){
        // Create an ArrayList of all the words in the table and their according details
        ArrayList<String> list = new ArrayList<String>();

        Cursor cursor = mainDB.rawQuery("SELECT * FROM " + MainScreen.table_name, null);

        String word_details;

        while(cursor.moveToNext()){
            String[] date = cursor.getString(6).split(":");

            // %;#;% is code word for splitting the text later, used in class Word_List
            String review_time = date[2]+"/"+date[1]+"/"+date[0]+"  "+date[3]+":"+date[4]+"0";
            word_details = "id:  " + cursor.getString(0) + "%;#;%word:  " + cursor.getString(1) + "\nit's meaning:  " + cursor.getString(2) + "\nNotes:  " + cursor.getString(3) +
                    "\nNext review time:  " + review_time;
            list.add(word_details);
        }

        return list;
    } // Create an ArrayList of all the words in the table and their according details

    public ArrayList<String> Create_Day_Order_List(String[] date_split, boolean is_current_day){
        //returns the amount of new words each hour the user gets in a day

        ArrayList<String> list = new ArrayList<String>();

        int[] day_chosen = new int[date_split.length];

        for(int i=0;i<day_chosen.length;i++)
            day_chosen[i] = Integer.parseInt(date_split[i]);

        if(is_current_day) {
            // collects the amount of words ready to review after the current hour today
            for(int hour = day_chosen[3]; hour<24; hour++) {

                Cursor cursor = mainDB.rawQuery("SELECT * FROM " + MainScreen.table_name, null);

                boolean more_words = cursor.moveToFirst();

                int amount_of_words_per_hour = 0;

                while (more_words) {
                    String review_date = cursor.getString(6);
                    String[] review_date_split = review_date.split(":");

                    int[] review_date_int = new int[review_date_split.length];

                    for (int i = 0; i < review_date_int.length; i++)
                        review_date_int[i] = Integer.parseInt(review_date_split[i]);

                    Boolean same_date = true;

                    for (int i = 0; i < 3; i++) {
                        if (review_date_int[i] != day_chosen[i])
                            same_date = false;
                    }
                    if (same_date && hour == review_date_int[3]) {
                        amount_of_words_per_hour++;
                    }

                    more_words = cursor.moveToNext();
                }
                if(amount_of_words_per_hour>0) {
                    String hour_text = hour + ":00!@#%&%#@!" + amount_of_words_per_hour;
                    list.add(hour_text);
                }
            }
        }

        else{
            // the same but from hour 0 on the following days
            for(int hour = 0; hour<24; hour++) {

                Cursor cursor = mainDB.rawQuery("SELECT * FROM " + MainScreen.table_name, null);

                boolean more_words = cursor.moveToFirst();

                int amount_of_words_per_hour = 0;

                while (more_words) {
                    String review_date = cursor.getString(6);
                    String[] review_date_split = review_date.split(":");

                    int[] review_date_int = new int[review_date_split.length];

                    for (int i = 0; i < review_date_int.length; i++)
                        review_date_int[i] = Integer.parseInt(review_date_split[i]);

                    Boolean same_date = true;

                    for (int i = 0; i < 3; i++) {
                        if (review_date_int[i] != day_chosen[i])
                            same_date = false;
                    }

                    if (same_date && hour == review_date_int[3]) {
                        amount_of_words_per_hour++;
                    }
                }
                if(amount_of_words_per_hour>0) {
                    String hour_text = hour + ":00%;#;%" + amount_of_words_per_hour;
                    list.add(hour_text);
                }
            }
        }
        return  list;
    } //returns the amount of new words each hour the user gets in a day

    public int SetNumberOfWordsReadyToTrain(){
        // Returns the amount of words ready for training and reviewing
        int counter = 0;
        Cursor cursor = mainDB.rawQuery("SELECT * FROM " + table_name, null);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm");
        String date = dateFormat.format(calendar.getTime());
        String[] date_split = date.split(":");

        int[] date_now = new int[date_split.length];

        for(int i=0;i<date_now.length;i++)
            date_now[i] = Integer.parseInt(date_split[i]);

        while(cursor.moveToNext()){
            String date_review = cursor.getString(6);
            date_split = date_review.split(":");

            int[] review_time = new int[date_split.length];

            for(int i=0;i<date_now.length;i++)
                review_time[i] = Integer.parseInt(date_split[i]);

            if(Check_If_Review_Ready(date_now, review_time))
                counter = counter +1;
        }
        return counter;
    } // Returns the amount of words ready for training and reviewing

    public int Get_Amount_Review_In_Day(String[] day, boolean is_current_day){
        //Return the amount of reviews that are ready

        int Amount_Review_In_Day = 0;

        int[] day_chosen = new int[day.length];

        for(int i=0;i<day_chosen.length;i++)
            day_chosen[i] = Integer.parseInt(day[i]);

        Cursor cursor = mainDB.rawQuery("SELECT * FROM " + table_name, null);

        boolean more_words = cursor.moveToFirst();

        while(more_words){
            String review_date = cursor.getString(6);
            String[] review_date_split = review_date.split(":");

            int[] review_date_int = new int[review_date_split.length];

            for(int i=0;i<review_date_int.length;i++)
                review_date_int[i] = Integer.parseInt(review_date_split[i]);

            Boolean same_date = true;

            for (int i = 0; i < 3; i++) {
                if (review_date_int[i] != day_chosen[i])
                    same_date = false;
            }

            if(same_date) {
                if (is_current_day && review_date_int[3] > day_chosen[3]) {
                    Amount_Review_In_Day++;
                    more_words = cursor.moveToNext();
                    continue;
                }

                if (!is_current_day) {
                    Amount_Review_In_Day++;
                }
            }
            more_words = cursor.moveToNext();
        }

        return Amount_Review_In_Day;
    } //Return the amount of reviews that are ready

    public void back(){
        Intent intent = new Intent(this, Tables_Screen.class);
        intent.putExtra(getString(R.string.Notification_Running), true);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            back();
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    // The next functions are for the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.statistics:
                openStatisticsScreen();
                return true;

            case R.id.Settings:
                openSettingsScreen();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openStatisticsScreen(){
        Intent intent = new Intent(this, Statistics_Screen.class);
        intent.putExtra(getString(R.string.Origin_Activity),getString(R.string.MainScreen));
        startActivity(intent);
    }

    public void openSettingsScreen(){
        Intent intent = new Intent(this, Settings_Screen.class);
        intent.putExtra(getString(R.string.Origin_Activity),getString(R.string.MainScreen));
        startActivity(intent);
    }

}