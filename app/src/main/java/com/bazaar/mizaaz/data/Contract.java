package com.bazaar.mizaaz.data;


import android.net.Uri;
import android.provider.BaseColumns;

public class Contract {

    public static final String AUTHORITY = "com.bazaar.mizaaz";

    private static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_QUOTE = "quote";
    public static final String PATH_QUOTE_WITH_SYMBOL = "quote/*";
    public static final String PATH_QUOTE_HISTORY_WITH_SYMBOL = "quote/history/*";
    public static final String PATH_QUOTE_DATE_WITH_SYMBOL = "trend/*/#";
    public static final String PATH_QUOTE_TREND = "trend";



    public static final class Quote implements BaseColumns {

        public static final Uri uri = BASE_URI.buildUpon().appendPath(PATH_QUOTE).build();

        public static final String TABLE_NAME = "quotes";

        public static final String COLUMN_SYMBOL = "symbol";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_ABSOLUTE_CHANGE = "absolute_change";
        public static final String COLUMN_PERCENTAGE_CHANGE = "percentage_change";
        public static final String COLUMN_HISTORY = "history";
        public static final String COLUMN_DATE = "date";

        public static final String COLUMN_OPEN = "today_open";
        public static final String COLUMN_PREVIOUS_CLOSE = "previous_close";


        public static final int POSITION_SYMBOL = 1;
        public static final int POSITION_PRICE = 2;
        public static final int POSITION_ABSOLUTE_CHANGE = 3;
        public static final int POSITION_PERCENTAGE_CHANGE = 4;
        public static final int POSITION_HISTORY = 5;
        public static final int POSITION_DATE = 6;

        public static final int POSITION_OPEN = 7;
        public static final int POSITION_PREVIOUS_CLOSE = 8;


        public static final String[] QUOTE_COLUMNS = {
                _ID,
                COLUMN_SYMBOL,
                COLUMN_PRICE,
                COLUMN_ABSOLUTE_CHANGE,
                COLUMN_PERCENTAGE_CHANGE,
                COLUMN_HISTORY,
                COLUMN_DATE,
                COLUMN_OPEN,
                COLUMN_PREVIOUS_CLOSE
        };

        public static Uri makeUriForStock(String symbol) {
            return uri.buildUpon().appendPath(symbol).build();
        }

        public static Uri makeDateUriForStock(String symbol,String date) {
            return uri.buildUpon().appendPath(date).appendPath(symbol).build();
        }

        public static String getStockFromUri(Uri uri) {
            return uri.getLastPathSegment();
        }


    }
}
