package com.example.anki;

public class Date_Orginizer {
    // This class orginizes the dates for the reviews!!

    public Date_Orginizer(){

    }
    public String Get_New_date(String[] date_split, int TimeBetweenReviews) {
        int day = Integer.parseInt(date_split[0]);
        int month = Integer.parseInt(date_split[1]);
        int year = Integer.parseInt(date_split[2]);
        int hour = Integer.parseInt(date_split[3]);
        int minutes = Integer.parseInt(date_split[4]);

        //find the next time the word will appear in the reviews(after one hour)
        hour = hour + TimeBetweenReviews;

        if (minutes > 0) {
            // the idea here is that the reviews come only at round hours
            hour = hour + 1;
            minutes = 0;
        }

        while (hour >= 24) {
            // correct the date after the addition of the hour
            hour = hour - 24;
            day = day + 1;
            if ((month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) && day > 31) {
                // if the month with 31 days ended
                day = day - 31;
                month = month + 1;
            }

            if ((month == 4 || month == 6 || month == 9 || month == 11) && day > 30) {
                // if the month with 30 days ended
                day = day - 30;
                month = month + 1;
            }

            if (month == 2 && ((year - 2000) % 4 == 0) && day > 29) {
                // if february ended on a leap year
                day = day - 29;
                month = month + 1;
            }

            if (month == 2 && ((year - 2000) % 4 != 0) && day > 28) {
                // if february ended on a regular year
                day = day - 28;
                month = month + 1;
            }

            if (month > 12) {
                // if the year ended
                month = month - 12;
                year = year + 1;
            }
        }

        String new_date = day + ":" + month + ":" + year + ":" + hour + ":" + minutes;
        return new_date;
    }
}
