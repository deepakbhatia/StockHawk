package com.bazaar.mizaaz.sync;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

import java.util.Calendar;

/**
 * Created by obelix on 04/12/2016.
 */

public class StockQuoteService extends GcmTaskService {

    private static final String TAG = StockQuoteService.class.getSimpleName();
    public static final String TAG_TASK_ONEOFF = "TAG_TASK_ONEOFF";
    public static final String TAG_TASK_PERIODIC = "TAG_TASK_PERIODIC";
    private static final int HISTORY_PERIOD = -10;


    public StockQuoteService(){}

    public StockQuoteService(Context context){
    }
    @Override
    public void onInitializeTasks() {
        super.onInitializeTasks();
        // Reschedule removed tasks here
        QuoteSyncJob.initialize(getApplicationContext());
    }
    @Override
    public int onRunTask(TaskParams taskParams) {
        Calendar from =  Calendar.getInstance();

        switch (taskParams.getTag()) {
            case TAG_TASK_ONEOFF:
                from.add(Calendar.YEAR, HISTORY_PERIOD);
                QuoteSyncJob.getQuotes(getApplicationContext(),from);

                // This is where useful work would go
                return GcmNetworkManager.RESULT_SUCCESS;
            case TAG_TASK_PERIODIC:
                from.add(Calendar.YEAR, HISTORY_PERIOD);
                QuoteSyncJob.getQuotes(getApplicationContext(),from);
                // This is where useful work would go
                return GcmNetworkManager.RESULT_SUCCESS;
            default:
                return GcmNetworkManager.RESULT_FAILURE;
        }
    }
}
