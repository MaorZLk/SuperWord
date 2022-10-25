package com.example.anki;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class Word_List extends BaseAdapter implements ListAdapter {

    public static String text;
    private String state;

    private ArrayList<String> list = new ArrayList<String>();
    private Context context;

    public Word_List(ArrayList<String> list, Context context, String state) {
        this.state = state;
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return 0;
        //just return 0 if your list items do not have an Id variable.
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            context.setTheme(Tables_Screen.BackgroundTheme);
            view = inflater.inflate(R.layout.word_list, null);
        }

        if(state.equals("word")){
            //Handle TextView and display string from your list
            final TextView word = (TextView) view.findViewById(R.id.WordDetails);
            final TextView id = (TextView) view.findViewById(R.id.id);
            final ImageButton DeleteWordButton = view.findViewById(R.id.DeleteWordImageButton);
            DeleteWordButton.setVisibility(View.VISIBLE);

            String wordlist = list.get(position);
            String[] wordlist2 = wordlist.split("%;#;%");
            if(wordlist2.length>2){
                System.out.println(wordlist2.length);
            }
            id.setText(wordlist2[0]);
            System.out.println(wordlist2[0]);
            word.setText(wordlist2[1]);

            word.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    text = id.getText().toString();
                    Intent intent = new Intent(context, Word_Updater_Screen.class);
                    context.startActivity(intent);

                }
            });

            DeleteWordButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println(id.getText().toString());
                    text = id.getText().toString();
                    String Sid = id.getText().toString().substring(text.indexOf("id:  "));
                    final String id = Sid.replace("id:  ", "");
                    int ans = MainScreen.mainDB.delete(MainScreen.table_name, "id = ?", new String[] {id});
                    if(ans != 1)
                        System.out.println("BAD");

                    else
                        System.out.println("Good");
                    Intent intent = new Intent(context, MainScreen.class);
                    context.startActivity(intent);
                }
            });
        }

        if(state.equals("table")){
            final TextView table = (TextView) view.findViewById(R.id.WordDetails);
            final TextView table_num = (TextView) view.findViewById(R.id.WordsCount);
            final ImageButton DeleteTableButton = view.findViewById(R.id.DeleteWordImageButton);
            DeleteTableButton.setVisibility(View.VISIBLE);
            table_num.setVisibility(View.VISIBLE);

            final String table_name = list.get(position);
            final String[] table_split = table_name.split("!@#%&");
            System.out.println(table_split[0]);
            System.out.println(table_split[1]);

            table.setText(table_split[0]);
            table_num.setText(table_split[1]);

            table.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println(table_split[0]);
                    MainScreen.table_name = table_split[0];
                    System.out.println(table_split[0]);
                    Intent intent = new Intent(context, MainScreen.class);
                    context.startActivity(intent);
                    System.out.println(table_split[0]);

                }
            });

            DeleteTableButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SQLiteDatabase db = Tables_Screen.dbHelper.getWritableDatabase();
                    db.execSQL("DROP TABLE IF EXISTS " + table_split[0]);
                    Intent intent = new Intent(context, Tables_Screen.class);
                    intent.putExtra(context.getString(R.string.Notification_Running), true);
                    context.startActivity(intent);
                }
            });
        }

        if(state.equals("day time")){
            final TextView hour = (TextView) view.findViewById(R.id.WordDetails);
            final TextView words_num = (TextView) view.findViewById(R.id.WordsCount);

            words_num.setVisibility(View.VISIBLE);

            final String hour_detials = list.get(position);
            final String[] hour_split = hour_detials.split("!@#%&%#@!*");

            System.out.println(hour_split[0]);
            System.out.println(hour_split[1]);

            hour.setText(hour_split[0]);
            words_num.setText(hour_split[1]);
        }

        if(state.equals("statistics")){
            final TextView word = (TextView) view.findViewById(R.id.WordDetails);
            final TextView success_percentage = (TextView) view.findViewById(R.id.WordsCount);

            success_percentage.setVisibility(View.VISIBLE);

            String wordlist = list.get(position);
            String[] wordlist2 = wordlist.split("%;#;%");

            word.setText(wordlist2[0]);

            double review_times = Double.parseDouble(wordlist2[1]);
            double correct_review_times = Double.parseDouble(wordlist2[2]);

            String text;
            if(review_times == 0){
                text = "0%";
            }
            else{
                double correct_percentage = (correct_review_times/review_times)*100;
                System.out.println(correct_percentage);
                text = correct_percentage + "%";
            }
            success_percentage.setText(text);
        }

        return view;
    }
}

