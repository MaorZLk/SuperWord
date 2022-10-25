package com.example.anki;

import android.content.ContentValues;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Train_Screen extends AppCompatActivity {

    SQLiteDatabase db = MainScreen.mainDB;
    public Cursor cursor;
    final Date_Orginizer date_orginizer = new Date_Orginizer();

    public static boolean NoMoreWords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Tables_Screen.BackgroundTheme);
        setContentView(R.layout.train__layout);

        NoMoreWords = false;
        final TextView wordText = findViewById(R.id.NewWord);
        final EditText meaningText = findViewById(R.id.MeaningTextEdit);
        final TextView notesText = findViewById(R.id.NotesText);
        final Button CheckButton = findViewById(R.id.CheckButton);
        final EditText EditNotesText = findViewById(R.id.EditNotesText);
        final Button EditNotesButton = findViewById(R.id.UpdateNotesButton);
        final Button BackButton = findViewById(R.id.ExitButton);


        cursor = db.rawQuery("SELECT * FROM " + MainScreen.table_name, null);
        cursor.moveToFirst();
        find_ready_word();

        if(NoMoreWords)
            this.finish();
        wordText.setText(cursor.getString(1));


        CheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(CheckButton.getText().toString().equals("Continue")){
                    cursor.moveToNext();
                    find_ready_word();
                    if(!NoMoreWords)
                        wordText.setText(cursor.getString(1));
                    meaningText.setText("");
                    meaningText.setBackgroundColor(Color.TRANSPARENT);
                    notesText.setVisibility(View.INVISIBLE);
                    notesText.setText("");
                    EditNotesButton.setVisibility(View.INVISIBLE);
                    CheckButton.setText("Check");
                    return;
                }

                if(CheckButton.getText().toString().equals("Check")){
                    String meaning = meaningText.getText().toString();
                    if(meaning.matches("")){
                        meaningText.setHint("Must write a meaning");
                        meaningText.setHintTextColor(Color.RED);
                    }
                    else{
                        notesText.setText(cursor.getString(3));
                        notesText.setVisibility(View.VISIBLE);
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm");
                        String date = dateFormat.format(calendar.getTime());
                        String[] date_split = date.split(":");

                        int time_between_reviews = Integer.parseInt(cursor.getString(5));
                        String newDate;

                        int Reviewed_Correct_Times = cursor.getInt(8);

                        // Change the word to lowercase because there is no difference between Jump and jump, for example.
                        String meaning_lowercase = "";
                        for (int i=0; i<meaning.length(); i++){
                            meaning_lowercase = meaning_lowercase + Character.toLowerCase(meaning.charAt(i));
                        }
                        String real_meaning_lowercase = "";
                        String real_meaning = cursor.getString(2);
                        for(int i=0; i<real_meaning.length(); i++){
                            real_meaning_lowercase = real_meaning_lowercase + Character.toLowerCase(real_meaning.charAt(i));
                        }

                        if(meaning_lowercase.equals(real_meaning_lowercase)){
                            meaningText.setBackgroundColor(Color.GREEN);
                            time_between_reviews = time_between_reviews * 2;
                            newDate = date_orginizer.Get_New_date(date_split,time_between_reviews);
                            Reviewed_Correct_Times++;
                        }

                        else{
                            meaningText.setBackgroundColor(Color.RED);
                            if (time_between_reviews > 1)
                                time_between_reviews = time_between_reviews / 2;

                            newDate = date_orginizer.Get_New_date(date_split,time_between_reviews);
                            meaning = cursor.getString(2);
                        }

                        ContentValues cv = new ContentValues();
                        cv.put(SqlHelper.COLUMN_WORD, cursor.getString(1));
                        cv.put(SqlHelper.COLUMN_DESCRIPTION, meaning);
                        cv.put(SqlHelper.COLUMN_LEARNER_HELPER, cursor.getString(3));
                        cv.put(SqlHelper.COLUMN_LAST_TIME_REVISITED, date);
                        cv.put(SqlHelper.COLUMN_TIME_BETWEEN_REVIEWS, time_between_reviews);// need to research this
                        cv.put(SqlHelper.COLUMN_NEXT_REVIEW_TIME, newDate);
                        cv.put(SqlHelper.COLUMN_REVIEWED_TIMES, GetReviewedTimes(cursor.getInt(0)));
                        cv.put(SqlHelper.COLUMN_REVIEWED_CORRECT_TIMES, Reviewed_Correct_Times);
                        db.update(MainScreen.table_name, cv, "id = ?", new String[] { cursor.getString(0) });
                        CheckButton.setText("Continue");
                        return;
                    }
                }
            }
        });

        notesText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notesText.setVisibility(View.GONE);
                EditNotesText.setText(notesText.getText().toString());
                EditNotesText.setVisibility(View.VISIBLE);
                EditNotesButton.setVisibility(View.VISIBLE);
            }
        });

        EditNotesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditNotesText.setVisibility(View.INVISIBLE);
                notesText.setVisibility(View.VISIBLE);
                EditNotesButton.setVisibility(View.INVISIBLE);

                String new_Notes = EditNotesText.getText().toString();
                notesText.setText(new_Notes);

                ContentValues cv = new ContentValues();
                cv.put(SqlHelper.COLUMN_WORD, cursor.getString(1));
                cv.put(SqlHelper.COLUMN_DESCRIPTION, cursor.getString(2));
                cv.put(SqlHelper.COLUMN_LEARNER_HELPER, new_Notes);
                cv.put(SqlHelper.COLUMN_LAST_TIME_REVISITED, cursor.getString(4));
                cv.put(SqlHelper.COLUMN_TIME_BETWEEN_REVIEWS, cursor.getString(5));
                cv.put(SqlHelper.COLUMN_NEXT_REVIEW_TIME, cursor.getString(6));
                cv.put(SqlHelper.COLUMN_REVIEWED_TIMES, cursor.getString(7));
                cv.put(SqlHelper.COLUMN_REVIEWED_CORRECT_TIMES, cursor.getString(8));
                db.update(MainScreen.table_name, cv, "id = ?", new String[] { cursor.getString(0) });
            }
        });

        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Go_Back(false);
            }
        });
    }

    public void find_ready_word(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm");
        String date = dateFormat.format(calendar.getTime());
        String[] date_split = date.split(":");

        int[] date_now = new int[date_split.length];

        for(int i=0;i<date_now.length;i++)
            date_now[i] = Integer.parseInt(date_split[i]);
;
        boolean table_not_ended = true;
        System.out.println(cursor.getPosition());
        System.out.println(cursor.getCount());

        while(table_not_ended && (cursor.getPosition() != cursor.getCount())){
            String date_review = cursor.getString(6);
            date_split = date_review.split(":");

            int[] review_time = new int[date_split.length];

            for(int i=0;i<date_now.length;i++)
                review_time[i] = Integer.parseInt(date_split[i]);

            if(Check_If_Review_Ready(date_now, review_time))
                return;
            table_not_ended = cursor.moveToNext();
        }
        Go_Back(true);
    }

    public boolean Check_If_Review_Ready(int[] now, int[] review){
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

    public void Go_Back(boolean No_More_Words) {
        if(No_More_Words) {
            Toast toast = Toast.makeText(getApplicationContext(), "You don't have any more words to train on right now", Toast.LENGTH_LONG);
            toast.show();
        }
        Intent intent = new Intent(this, MainScreen.class);
        startActivity(intent);
        NoMoreWords = true;
    }

    public int GetReviewedTimes(int id){
        Cursor cursor = db.rawQuery("select * from " + MainScreen.table_name + " where id = '" + id + "'" , null);
        cursor.moveToFirst();
        String reviewedTimes = cursor.getString(7);
        int intReviewedTimes = Integer.parseInt(reviewedTimes) +1;
        cursor.close();
        return intReviewedTimes;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            Go_Back(false);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

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
        intent.putExtra(getString(R.string.Origin_Activity),getString(R.string.Train_Screen));
        startActivity(intent);
    }

    public void openSettingsScreen(){
        Intent intent = new Intent(this, Settings_Screen.class);
        intent.putExtra(getString(R.string.Origin_Activity),getString(R.string.Train_Screen));
        startActivity(intent);
    }
}







