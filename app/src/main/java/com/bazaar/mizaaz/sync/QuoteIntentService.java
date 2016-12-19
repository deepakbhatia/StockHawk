package com.bazaar.mizaaz.sync;

import android.app.IntentService;
import android.content.Intent;

import java.util.Calendar;


public class QuoteIntentService extends IntentService {

    private static final int HISTORY_PERIOD = -2;

    public QuoteIntentService() {
        super(QuoteIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Calendar from =  Calendar.getInstance();

        from.add(Calendar.YEAR, HISTORY_PERIOD);

        /*if(!sharedPreferences.contains("first_run"))
        {

            SharedPreferences.Editor prefEdit = sharedPreferences.edit();
            prefEdit.putBoolean("first_run",false);
            prefEdit.commit();


        }
        else
        {
            from.add(Calendar.DATE, -1);

        }*/
        QuoteSyncJob.getQuotes(getApplicationContext(),from);
    }
}
