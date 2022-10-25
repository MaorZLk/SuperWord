package com.example.anki;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

// Creates and saves a new word in the SQLite database
public class New_Word_screen extends AppCompatActivity {

    public static String new_word;
    public static String meaning;
    public static String notes;

    public EditText new_wordText;
    public EditText meaningText;
    public EditText notesText;

    public SQLiteDatabase db = MainScreen.mainDB;

    final Date_Orginizer date_orginizer = new Date_Orginizer();

    final int RequestCodeWord = 1000;
    final int RequestCodeMeaning = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Tables_Screen.BackgroundTheme);
        setContentView(R.layout.new_word_latyout);


        final Button add_new_word_button = findViewById(R.id.AddNewWordButton);
        final Button cancel_button = findViewById(R.id.CancelButton);

        final ImageButton VoiceToText_Word = findViewById(R.id.VoiceToText_Word_NewWordScreen);
        final ImageButton VoiceToText_Meaning = findViewById(R.id.VoiceToText_Meaning_NewWordScreen);

        new_wordText = findViewById(R.id.NewWordTextEdit);
        meaningText = findViewById(R.id.MeaningTextEdit);
        notesText = findViewById(R.id.NotesTextEdit);

        // Collects all the details the user has putted in
        new_word = new_wordText.getText().toString();
        meaning = meaningText.getText().toString();
        notes = notesText.getText().toString();

        add_new_word_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Saves the new word details in the SQL database
                new_word = new_wordText.getText().toString();
                meaning = meaningText.getText().toString();
                notes = notesText.getText().toString();

               if(!new_word.matches("") && !meaning.matches("")){
                   Cursor cursor = db.rawQuery("SELECT * FROM " + MainScreen.table_name, null);
                   boolean word_exists = false;
                   while(cursor.moveToNext()) {
                       if (new_word.equals(cursor.getString(1)))
                           word_exists = true;
                   }
                   if(word_exists){
                       Toast toast = Toast.makeText(getApplicationContext(), "This word already exists", Toast.LENGTH_LONG);
                       toast.show();
                   }
                   else{
                       InsertNewWord(MainScreen.table_name, new_word, meaning, notes);
                       back();
                   }
               }
               else{
                   if(new_word.matches("")){
                       new_wordText.setHint("Must write a word");
                       new_wordText.setHintTextColor(Color.RED);
                   }
                   if(meaning.matches("")){
                       meaningText.setHint("Must write a meaning");
                       meaningText.setHintTextColor(Color.RED);
                   }
               }

            }
        });

        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back();
            }
        });

        VoiceToText_Word.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say The Word");

                startActivityForResult(intent, RequestCodeWord);
            }
        });

        VoiceToText_Meaning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Allow the user to input the meaning vocally
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say The Meaning");

                startActivityForResult(intent, RequestCodeMeaning);
            }
        });
    }
    public void back(){
        Intent intent = new Intent(this, MainScreen.class);
        startActivity(intent);
    }

    public void InsertNewWord(String table_name, String word, String description, String notes){
        // puts the data in the SQL database
        SQLiteDatabase db = MainScreen.mainDB;
        ContentValues cv = new ContentValues();

        // get the date when the word was created
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd:HH:mm");
        String date = dateFormat.format(calendar.getTime());
        String[] date_split = date.split(":");

        String new_date = date_orginizer.Get_New_date(date_split, 1); // starts with 1, so that the user will be able to review his word after an hour

        // The data it self
        cv.put(SqlHelper.COLUMN_WORD, word);
        cv.put(SqlHelper.COLUMN_DESCRIPTION, description);
        cv.put(SqlHelper.COLUMN_LEARNER_HELPER, notes);
        cv.put(SqlHelper.COLUMN_LAST_TIME_REVISITED, date);
        cv.put(SqlHelper.COLUMN_TIME_BETWEEN_REVIEWS, 1);
        cv.put(SqlHelper.COLUMN_NEXT_REVIEW_TIME, new_date);
        cv.put(SqlHelper.COLUMN_REVIEWED_TIMES, 0);
        cv.put(SqlHelper.COLUMN_REVIEWED_CORRECT_TIMES, 0);
        long result = db.insert(table_name,null, cv);
        System.out.println(result); // For error checking

        // FOR CHECKING - DELETE later
        Cursor cursor = db.rawQuery("SELECT * FROM " + MainScreen.table_name, null);
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case RequestCodeWord:
                if(resultCode == RESULT_OK && null!=data){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    new_wordText.setText(result.get(0));
                }
                break;
            case RequestCodeMeaning:
                if(resultCode == RESULT_OK && null!=data){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    meaningText.setText(result.get(0));
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            back();
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    // Manage the menu
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
        intent.putExtra(getString(R.string.Origin_Activity),getString(R.string.new_word_layout));
        startActivity(intent);
    }

    public void openSettingsScreen(){
        Intent intent = new Intent(this, Settings_Screen.class);
        intent.putExtra(getString(R.string.Origin_Activity),getString(R.string.new_word_layout));
        startActivity(intent);
    }
}
