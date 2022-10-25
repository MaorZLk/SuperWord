package com.example.anki;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class Word_Updater_Screen extends AppCompatActivity {

    SQLiteDatabase db = MainScreen.mainDB;
    String text = Word_List.text;

    public EditText word_edit;
    public EditText meaning_edit;
    public EditText notes_edit;

    final int RequestCodeWord = 1000;
    final int RequestCodeMeaning = 2000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setTheme(Tables_Screen.BackgroundTheme);
            setContentView(R.layout.word_updater_layout);

            word_edit = findViewById(R.id.WordUpdateTextEdit);
            meaning_edit = findViewById(R.id.MeaningUpdateTextEdit);
            notes_edit = findViewById(R.id.NotesUpdateTextEdit);

            final Button update_button = findViewById(R.id.UpdateButton);
            final Button cancel_button = findViewById(R.id.CancelManagerButton);

            final ImageButton VoiceToTextWord = findViewById(R.id.VoiceToText_Word_UpdateScreen);
            final ImageButton VoiceToTextMeaning = findViewById(R.id.VoiceToText_Meaning_UpdateScreen);

            String Sid = text.substring(text.indexOf("id:  "));
            final String id = Sid.replace("id:  ", "");

            final Cursor cursor = db.rawQuery("SELECT * FROM " + MainScreen.table_name +" WHERE ID = ?", new String[] { id });
            cursor.moveToFirst();

            word_edit.setText(cursor.getString(1));
            meaning_edit.setText(cursor.getString(2));
            notes_edit.setText(cursor.getString(3));

            update_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String new_word = word_edit.getText().toString();
                    String new_meaning = meaning_edit.getText().toString();
                    String new_notes = notes_edit.getText().toString();

                    if(new_word.equals("") || new_meaning.equals("")){
                        Toast toast = Toast.makeText(getApplicationContext(), "Word and/or meaning can't be empty", Toast.LENGTH_LONG);
                        toast.show();
                        word_edit.setText(cursor.getString(1));
                        meaning_edit.setText(cursor.getString(2));
                    }
                    else{
                        ContentValues cv = new ContentValues();
                        cv.put(SqlHelper.COLUMN_WORD, new_word);
                        cv.put(SqlHelper.COLUMN_DESCRIPTION, new_meaning);
                        cv.put(SqlHelper.COLUMN_LEARNER_HELPER, new_notes);
                        cv.put(SqlHelper.COLUMN_LAST_TIME_REVISITED, cursor.getString(4));
                        cv.put(SqlHelper.COLUMN_TIME_BETWEEN_REVIEWS, cursor.getString(5));
                        cv.put(SqlHelper.COLUMN_NEXT_REVIEW_TIME, cursor.getString(6));
                        db.update(MainScreen.table_name, cv, "id = ?", new String[] { id });
                        back();
                    }
                }
            });

            cancel_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    back();
                }
            });

        VoiceToTextWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say The Word");

                startActivityForResult(intent, RequestCodeWord);
            }
        });

        VoiceToTextMeaning.setOnClickListener(new View.OnClickListener() {
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            back();
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case RequestCodeWord:
                if(resultCode == RESULT_OK && null!=data){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    word_edit.setText(result.get(0));
                }
                break;
            case RequestCodeMeaning:
                if(resultCode == RESULT_OK && null!=data){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    meaning_edit.setText(result.get(0));
                }
                break;
        }
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
        intent.putExtra(getString(R.string.Origin_Activity),getString(R.string.Word_Updater_Screen));
        startActivity(intent);
    }

    public void openSettingsScreen(){
        Intent intent = new Intent(this, Settings_Screen.class);
        intent.putExtra(getString(R.string.Origin_Activity),getString(R.string.Word_Updater_Screen));
        startActivity(intent);
    }
}