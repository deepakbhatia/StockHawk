package com.bazaar.mizaaz.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.bazaar.mizaaz.R;
import com.bazaar.mizaaz.data.Contract;
import com.bazaar.mizaaz.data.PrefUtils;
import com.bazaar.mizaaz.message.StockUpdateFail;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.quotes.stock.StockQuote;

public final class QuoteSyncJob {



    private static final int ONE_OFF_ID = 3;
    public static final String ACTION_DATA_UPDATED = "com.bazaar.mizaaz.ACTION_DATA_UPDATED";

    private static final int PERIOD = 3600000;

    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;

    static void getQuotes(Context context,Calendar from) {

        Calendar to = Calendar.getInstance();


        try {


            Set<String> stockPref = PrefUtils.getStocks(context);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            if (stockArray.length == 0) {
                return;
            }

            Map<String, Stock> quotes = YahooFinance.get(stockArray);
            Iterator<String> iterator = stockCopy.iterator();

            ArrayList<ContentValues> quoteCVs = new ArrayList<>();

            while (iterator.hasNext()) {
                String symbol = iterator.next();


                Stock stock = quotes.get(symbol);
                StockQuote quote = stock.getQuote();

                if(quote.getPrice() == null)
                {
                    //TODO Snackbar Message for Error in Stock Symbol
                    continue ;
                }
                float previousClose = 0;
                if(quote.getPreviousClose()!= null)
                    previousClose = quote.getPreviousClose().floatValue();
                float dayOpen = 0;
                if(quote.getOpen()!=null)
                    dayOpen = quote.getOpen().floatValue();


                String stockDateStr = quote.getLastTradeDateStr();

                float price = quote.getPrice().floatValue();
                float change = quote.getChange().floatValue();
                float percentChange = quote.getChangeInPercent().floatValue();


                List<HistoricalQuote> history = stock.getHistory(from,to);

                StringBuilder historyBuilder = new StringBuilder();

                long stockPriceDate;
                for (HistoricalQuote it : history) {
                    stockPriceDate = it.getDate().getTimeInMillis();
                    BigDecimal closingPrice = it.getClose();

                    historyBuilder.append(stockPriceDate);
                    historyBuilder.append(", ");
                    historyBuilder.append(closingPrice);
                    historyBuilder.append("\n");
                }

                ContentValues quoteCV = new ContentValues();
                quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);
                quoteCV.put(Contract.Quote.COLUMN_HISTORY, historyBuilder.toString());
                quoteCV.put(Contract.Quote.COLUMN_DATE, stockDateStr);
                quoteCV.put(Contract.Quote.COLUMN_OPEN, dayOpen);
                quoteCV.put(Contract.Quote.COLUMN_PREVIOUS_CLOSE, previousClose);

                quoteCVs.add(quoteCV);


            }

            context.getContentResolver()
                    .bulkInsert(
                            Contract.Quote.uri,
                            quoteCVs.toArray(new ContentValues[quoteCVs.size()]));

            Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                    .setPackage(context.getPackageName());
            context.sendBroadcast(dataUpdatedIntent);


        }
        catch (UnknownHostException exception) {
            EventBus.getDefault().post(new StockUpdateFail(context.getString(R.string.unknown_host_message)));
            Timber.e(exception, "Error fetching stock quotes");
        } catch (SocketTimeoutException exception) {
            EventBus.getDefault().post(new StockUpdateFail(context.getString(R.string.slow_response_exception)));
            Timber.e(exception, "Error fetching stock quotes");
        } catch (IOException exception) {
            EventBus.getDefault().post(new StockUpdateFail(context.getString(R.string.response_processing_issue)));

            Timber.e(exception, "Error fetching stock quotes");
        }

    }

    private  static void schedulePeriodic(Context context) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_ID, new ComponentName(context, QuoteJobService.class));


            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPeriodic(PERIOD)
                    .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            scheduler.schedule(builder.build());
        }else{

            Task task = new PeriodicTask.Builder()
                    .setService(StockQuoteService.class)
                    .setPeriod(3600)
                    .setFlex(5)
                    .setTag(StockQuoteService.TAG_TASK_PERIODIC)
                    .setPersisted(true)
                    .build();

            GcmNetworkManager.getInstance(context).schedule(task);
        }

    }


    synchronized public static void initialize(final Context context) {
        syncImmediately(context);

        schedulePeriodic(context);
    }

    private  static void oneOffCurrent(Context context){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            JobInfo.Builder builder = null;

            builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context, QuoteJobService.class));
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            scheduler.schedule(builder.build());
        }else{


            Task task = new OneoffTask.Builder()
                    .setService(StockQuoteService.class)
                    .setExecutionWindow(0, 30)
                    .setTag(StockQuoteService.TAG_TASK_ONEOFF)
                    .setUpdateCurrent(false)
                    .setRequiredNetwork(Task.NETWORK_STATE_ANY)
                    .setRequiresCharging(false)
                    .build();

            GcmNetworkManager.getInstance(context).schedule(task);
        }



    }

    synchronized public static void syncImmediately(Context context) {


        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();


        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Intent nowIntent = new Intent(context, QuoteIntentService.class);
            context.startService(nowIntent);

        } else {

            oneOffCurrent(context);
        }
    }


}
