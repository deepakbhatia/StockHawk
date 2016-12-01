package com.bazaar.mizaaz.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;


public class StockProvider extends ContentProvider {

    static final int QUOTE = 100;
    static final int QUOTE_FOR_SYMBOL = 101;
    static final int QUOTE_HISTORY_FOR_SYMBOL = 102;
    static final int QUOTE_DATE_FOR_SYMBOL = 103;
    static final int TREND_QUOTE  = 104;

    static UriMatcher uriMatcher = buildUriMatcher();

    private DbHelper dbHelper;
    private static final SQLiteQueryBuilder sStockByDateQueryBuilder;

    static {
        sStockByDateQueryBuilder = new SQLiteQueryBuilder();
    }
    static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(Contract.AUTHORITY, Contract.PATH_QUOTE, QUOTE);

        matcher.addURI(Contract.AUTHORITY, Contract.PATH_QUOTE_WITH_SYMBOL, QUOTE_FOR_SYMBOL);
        matcher.addURI(Contract.AUTHORITY, Contract.PATH_QUOTE_HISTORY_WITH_SYMBOL, QUOTE_HISTORY_FOR_SYMBOL);
        matcher.addURI(Contract.AUTHORITY, Contract.PATH_QUOTE_DATE_WITH_SYMBOL, QUOTE_DATE_FOR_SYMBOL);

        //Time Based QUOTE
        matcher.addURI(Contract.AUTHORITY, Contract.PATH_QUOTE_TREND, TREND_QUOTE);

        return matcher;
    }

    //symbol = ? AND date = ?
    private static final String sSymbolSettingAndDateRange =
            Contract.HistoryQuote.TABLE_NAME +
                    "." + Contract.HistoryQuote.COLUMN_SYMBOL + " = ? AND " +
                    Contract.HistoryQuote.COLUMN_DATE + " <= ? ";


    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor returnCursor = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Log.d("Query:Uri",""+uriMatcher.match(uri));

        switch (uriMatcher.match(uri)) {
            case QUOTE:
                Log.d("QUOTE",""+uri.toString());

                returnCursor = db.query(
                        Contract.Quote.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case QUOTE_FOR_SYMBOL:
                Log.d("QUOTE_FOR_SYMBOL",""+Contract.Quote.getStockFromUri(uri));
                returnCursor = db.query(
                        Contract.Quote.TABLE_NAME,
                        projection,
                        Contract.Quote.COLUMN_SYMBOL + " = ?",
                        new String[]{Contract.Quote.getStockFromUri(uri)},
                        null,
                        null,
                        sortOrder
                );
               // returnCursor.moveToFirst();
                //Log.d("QUOTE_FOR_SYMBOL:",                ""+returnCursor);
                break;
            case QUOTE_HISTORY_FOR_SYMBOL:
                returnCursor = db.query(
                        Contract.Quote.TABLE_NAME,
                        new String[]{Contract.Quote.COLUMN_HISTORY},
                        Contract.Quote.COLUMN_SYMBOL + " = ?",
                        new String[]{Contract.Quote.getStockFromUri(uri)},
                        null,
                        null,
                        sortOrder
                );

                break;

            case QUOTE_DATE_FOR_SYMBOL:
                getStockByDate(uri, projection, sortOrder);

                break;
            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);
        }

        if(returnCursor!=null)
        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);

/*        if (db.isOpen()) {
            db.close();
        }*/

        return returnCursor;
    }


    /*

        Uri weatherUri = Contract.HistoryQuote.
        makeDateUriForStock(symbol,
        long date);


     */

    private Cursor getStockByDate(
            Uri uri, String[] projection, String sortOrder) {
        String symbol = Contract.HistoryQuote.getStockFromUri(uri);
        long date = Long.parseLong(Contract.HistoryQuote.getDateFromUri(uri));



        return sStockByDateQueryBuilder.query(dbHelper.getReadableDatabase(),
                projection,
                sSymbolSettingAndDateRange,
                new String[]{symbol, Long.toString(date)},
                null,
                null,
                sortOrder
        );
    }
    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Uri returnUri;
        Log.d("uriMatcher",uri.toString());
        switch (uriMatcher.match(uri)) {
            case QUOTE:
                Log.d("uriMatcher",uri.toString());

                db.insert(
                        Contract.Quote.TABLE_NAME,
                        null,
                        values
                );
                returnUri = Contract.Quote.uri;
                break;
            case TREND_QUOTE:
                Log.d("uriMatcher",uri.toString());

                db.insert(
                        Contract.HistoryQuote.TABLE_NAME,
                        null,
                        values
                );
                returnUri = Contract.HistoryQuote.uri;
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        //db.close();

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted;

        if (null == selection) selection = "1";
        switch (uriMatcher.match(uri)) {
            case QUOTE:
                rowsDeleted = db.delete(
                        Contract.Quote.TABLE_NAME,
                        selection,
                        selectionArgs
                );

                break;

            case QUOTE_FOR_SYMBOL:
                String symbol = Contract.Quote.getStockFromUri(uri);
                rowsDeleted = db.delete(
                        Contract.Quote.TABLE_NAME,
                        '"' + symbol + '"' + " =" + Contract.Quote.COLUMN_SYMBOL,
                        selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        //db.close();
        return rowsDeleted;
    }



    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        int updateRecord = db.update(Contract.Quote.TABLE_NAME
                ,values
                ,Contract.Quote.COLUMN_SYMBOL + " = ?"
                ,selectionArgs);

        Log.d("updateuri",""+updateRecord+":"+selectionArgs[0]);
        getContext().getContentResolver().notifyChange(uri, null);
        //db.close();
        return updateRecord;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        switch (uriMatcher.match(uri)) {
            case QUOTE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        db.insert(
                                Contract.Quote.TABLE_NAME,
                                null,
                                value
                        );
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                //db.close();

                return returnCount;
            case TREND_QUOTE:
                db.beginTransaction();
                int valueCount = 0;
                try {
                    for (ContentValues value : values) {
                        db.insert(
                                Contract.HistoryQuote.TABLE_NAME,
                                null,
                                value
                        );
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                //db.close();

                return valueCount;
            default:
                //db.close();
                return super.bulkInsert(uri, values);
        }


    }
}