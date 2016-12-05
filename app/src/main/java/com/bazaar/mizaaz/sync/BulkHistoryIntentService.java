package com.bazaar.mizaaz.sync;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

import timber.log.Timber;

/**
 * Created by obelix on 25/11/2016.
 */

public class BulkHistoryIntentService extends IntentService {

    private static final int HISTORY_PERIOD = -10;
    public BulkHistoryIntentService() {
        super(BulkHistoryIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Timber.d("Intent handled");

        SharedPreferences sharedPreferences = getSharedPreferences("stockApp",MODE_PRIVATE);
        Calendar from =  Calendar.getInstance();

        if(!sharedPreferences.contains("first_run"))
        {
            from.add(Calendar.YEAR, HISTORY_PERIOD);

            SharedPreferences.Editor prefEdit = sharedPreferences.edit();
            prefEdit.putBoolean("first_run",false);
            prefEdit.apply();


        }
        else
        {
            from.add(Calendar.DAY_OF_MONTH, -1);

        }
        //QuoteSyncJob.getHistoricalQuotes(getApplicationContext(),from);
    }
}
