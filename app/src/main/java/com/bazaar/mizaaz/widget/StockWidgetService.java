package com.bazaar.mizaaz.widget;

/**
 * Created by obelix on 03/12/2016.
 */

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bazaar.mizaaz.R;
import com.bazaar.mizaaz.data.Contract;
import com.bazaar.mizaaz.data.Stock;
import com.bazaar.mizaaz.ui.StockDetailActivityFragment;

/**
 * This is the service that provides the factory to be bound to the collection service.
 */
public class StockWidgetService extends RemoteViewsService {


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}
/**
 * This is the factory that will provide data to the collection widget.
 */
class StockRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private final Context mContext;
    private Cursor mCursor;

    private final int mAppWidgetId;
    public StockRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }
    public void onCreate() {
        // Since we reload the cursor in onDataSetChanged() which gets called immediately after
        // onCreate(), we do nothing here.
    }
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }
    public int getCount() {
        return mCursor.getCount();
    }
    public RemoteViews getViewAt(int position) {
        // Get the data for this position from the content provider
        float stockPrice = 0;
        String stockSymbol = "";
        float stockPercentChange = 0;
        float stockAbsoluteChange = 0;
        String stockDate = "";
        float stockOpen = 0;
        float stockClose = 0;

        String stockHistory = "";
        if (mCursor.moveToPosition(position)) {

            stockPrice = mCursor.getFloat(Contract.Quote.POSITION_PRICE);
            stockSymbol = mCursor.getString(Contract.Quote.POSITION_SYMBOL);
            stockPercentChange = mCursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
            stockAbsoluteChange = mCursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);

            stockHistory = mCursor.getString(Contract.Quote.POSITION_HISTORY);
            stockDate = mCursor.getString(Contract.Quote.POSITION_DATE);
            stockOpen = mCursor.getFloat(Contract.Quote.POSITION_OPEN);
            stockClose = mCursor.getFloat(Contract.Quote.POSITION_PREVIOUS_CLOSE);


        }
        // Set Stock Value for layout item with the Price, Symbol & Stock Change
        final int itemId = R.layout.widget_item_quote;
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), itemId);
        rv.setTextViewText(R.id.priceValueWidget, String.valueOf(stockPrice));
        rv.setTextViewText(R.id.symbolValueWidget, String.valueOf(stockSymbol));

        Stock stock = new Stock();
        stock.stockSymbol = stockSymbol;
        stock.stockCurrentPrice = stockPrice;
        stock.percentStockChange = stockPercentChange;
        stock.absoluteStockChange = stockAbsoluteChange;
        stock.stockHistory = stockHistory;
        stock.dateValue = stockDate;
        stock.stockOpen = stockOpen;
        stock.previousClose = stockClose;

        String change = "";
        if (stockAbsoluteChange > 0) {
            change ="+";
            //rv.setInt(R.id.changeValueWidget,"",R.drawable.percent_change_pill_green);
            rv.setTextColor(R.id.changeValueWidget, mContext.getResources().getColor(R.color.material_green_700));

        } else {
            //rv.setInt(R.id.changeValueWidget,"",R.drawable.percent_change_pill_red);
            rv.setTextColor(R.id.changeValueWidget, mContext.getResources().getColor(R.color.material_red_700));

        }
        rv.setTextViewText(R.id.changeValueWidget, change+String.valueOf(stockPercentChange)+"%");

        // Set the click intent so that we can handle it and show a toast message
        final Intent fillInIntent = new Intent();
        final Bundle extras = new Bundle();
        extras.putString(StockDetailActivityFragment.DETAIL_SYMBOL,stock.stockSymbol);
        extras.putParcelable(StockDetailActivityFragment.DETAIL_URI,Contract.Quote.makeUriForStock(stock.stockSymbol));
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.stock_widget_item, fillInIntent);
        return rv;
    }
    public RemoteViews getLoadingView() {
        // We aren't going to return a default loading view in this sample
        return null;
    }
    public int getViewTypeCount() {
        // Technically, we have two types of views (the dark and light background views)
        return 2;
    }
    public long getItemId(int position) {
        return position;
    }
    public boolean hasStableIds() {
        return true;
    }
    public void onDataSetChanged() {
        // Refresh the cursor
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = mContext.getContentResolver().query(Contract.Quote.uri,  Contract.Quote.QUOTE_COLUMNS, null,
                null, Contract.Quote.COLUMN_SYMBOL);

    }
}
