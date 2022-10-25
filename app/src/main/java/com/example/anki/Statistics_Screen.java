package com.example.anki;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;

public class Statistics_Screen extends AppCompatActivity {

    final SqlHelper dbHelper = Tables_Screen.dbHelper;
    final SQLiteDatabase db = dbHelper.getWritableDatabase();

    boolean IsInsertOrder = true;

    ListView statistics_screen;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Tables_Screen.BackgroundTheme);
        setContentView(R.layout.statistics__screen);

        statistics_screen = findViewById(R.id.statistics_list_view);

        statistics_screen.setAdapter(new Word_List(Create_Statistics_Word_List(), this, "statistics") );// create the list view

        RadioGroup radioGroup = findViewById(R.id.RadioGroupStatistics);

        RadioButton insert_order = findViewById(R.id.InsertOrderRadioButton);
        RadioButton high_to_low_order = findViewById(R.id.SuccessRateOrderRadioButton);

        radioGroup.check(R.id.InsertOrderRadioButton);

        insert_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IsInsertOrder = true;
                statistics_screen.setAdapter(new Word_List(Create_Statistics_Word_List(), context, "statistics") );// create the list view
            }
        });

        high_to_low_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IsInsertOrder = false;
                statistics_screen.setAdapter(new Word_List(Create_Statistics_Word_List(), context, "statistics") );// create the list view
            }
        });
    }

    public ArrayList<String> Create_Statistics_Word_List(){
        // Create an ArrayList of all the words in the table and their according details
        ArrayList<String> list = new ArrayList<String>();

        Cursor cursor = db.rawQuery("SELECT * FROM " + MainScreen.table_name, null);

        if(IsInsertOrder){
            String word_details;

            while(cursor.moveToNext()){
                // %;#;% is code word for splitting the text later
                word_details = "word:  " + cursor.getString(1) + "\nit's meaning:  " + cursor.getString(2) + "%;#;%" + cursor.getString(7) +"%;#;%" + cursor.getString(8);
                list.add(word_details);
            }
        }

        if(!IsInsertOrder){
            int cursorLength = cursor.getCount();

            int[] idList = new int[cursorLength];
            double[] percentageList = new double[cursorLength];

            int i = 0;

            while(cursor.moveToNext()){
                double review_times = Double.parseDouble(cursor.getString(7));
                double correct_review_times = Double.parseDouble(cursor.getString(8));

                double percentage = correct_review_times/review_times;

                idList[i] = Integer.parseInt(cursor.getString(0));
                percentageList[i] = percentage;
                i++;
            }

            for(i = 0; i<cursorLength -1; i++){

                double max = percentageList[i];
                int maxId = idList[i];
                int place = i;

                for(int k = i; k<cursorLength; k++){
                    if(max < percentageList[k]){
                        max = percentageList[k];
                        maxId = idList[k];
                        place = k;
                    }
                }

                percentageList[place] = percentageList[i];
                percentageList[i] = max;

                idList[place] = idList[i];
                idList[i] = maxId;
            }

            String word_details;

            for(i = 0; i<cursorLength;i++){
                System.out.println(idList[i]);
                cursor = db.rawQuery("SELECT * FROM " + MainScreen.table_name + " WHERE id = " + idList[i], null);
                cursor.moveToFirst();
                word_details = "word:  " + cursor.getString(1) + "\nit's meaning:  " + cursor.getString(2) + "%;#;%" + cursor.getString(7) +"%;#;%" + cursor.getString(8);
                list.add(word_details);
            }
        }

        return list;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // if the user press the back button
            Intent intent = getIntent();
            String origin_activity = intent.getStringExtra(getString(R.string.Origin_Activity));
            if(origin_activity.equals(getString(R.string.Tables_screen))){
                intent = new Intent(this, Tables_Screen.class);
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