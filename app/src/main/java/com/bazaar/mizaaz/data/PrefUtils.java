package com.bazaar.mizaaz.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bazaar.mizaaz.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

public final class PrefUtils {

    private PrefUtils() {
    }

    public static Set<String> getStocks(Context context) {
        String stocksKey = context.getString(R.string.pref_stocks_key);
        String initializedKey = context.getString(R.string.pref_stocks_init_key);

        String[] defaultStocksList = context.getResources().getStringArray(R.array.default_stocks);
        //String[] defaultStocksList = new String[10];
        HashSet<String> defaultStocks = new HashSet<>(Arrays.asList(defaultStocksList));
        SharedPreferences stockListPref = context.getSharedPreferences("stockList", MODE_PRIVATE);
        SharedPreferences stockAppPrefs = context.getSharedPreferences("stock_pref", MODE_PRIVATE);



       if(!stockAppPrefs.contains(initializedKey)){
            SharedPreferences.Editor stockListEditor = stockListPref.edit();
           stockListEditor.clear();
           stockListEditor.putStringSet(stocksKey, defaultStocks);
           stockListEditor.apply();

           SharedPreferences.Editor editor = stockAppPrefs.edit();
           editor.putBoolean(initializedKey, true);
            editor.apply();
           return defaultStocks;
        }


        return stockListPref.getStringSet(stocksKey, new HashSet<String>());

    }

    private static void editStockPref(Context context, String symbol, Boolean add) {

        SharedPreferences stockListPref = context.getSharedPreferences("stockList", MODE_PRIVATE);

        String key = context.getString(R.string.pref_stocks_key);
        Set<String> stocks = stockListPref.getStringSet(key,new HashSet<String>());
        if (add) {
            stocks.add(symbol);
        } else {
            stocks.remove(symbol);
        }

        SharedPreferences.Editor editor = stockListPref.edit();
        editor.clear();
        editor.putStringSet(key,stocks);
        editor.apply();
    }

    public static boolean checkStock(Context context, String symbol){
        Set<String> stocks = getStocks(context);

        return stocks.contains(symbol);
    }

    public static int stockSize(Context context){
        return getStocks(context).size();
    }
    public static void addStock(Context context, String symbol) {
        editStockPref(context, symbol, true);
    }

    public static void removeStock(Context context, String symbol) {
        editStockPref(context, symbol, false);
    }

    public static String getDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String defaultValue = context.getString(R.string.pref_display_mode_default);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(key, defaultValue);
    }

    public static void toggleDisplayMode(Context context) {
        String key = context.getString(R.string.pref_display_mode_key);
        String absoluteKey = context.getString(R.string.pref_display_mode_absolute_key);
        String percentageKey = context.getString(R.string.pref_display_mode_percentage_key);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String displayMode = getDisplayMode(context);

        SharedPreferences.Editor editor = prefs.edit();

        if (displayMode.equals(absoluteKey)) {
            editor.putString(key, percentageKey);
        } else {
            editor.putString(key, absoluteKey);
        }

        editor.apply();
    }

}
