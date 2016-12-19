package com.bazaar.mizaaz.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.bazaar.mizaaz.R;
import com.bazaar.mizaaz.sync.QuoteSyncJob;
import com.bazaar.mizaaz.ui.StockDetailActivity;
import com.bazaar.mizaaz.ui.StockDetailActivityFragment;

/**
 * Created by obelix on 07/12/2016.
 */

public class StockWidgetProvider extends AppWidgetProvider {

    private static final String CLICK_ACTION = "";

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();
        if(action.equals(QuoteSyncJob.ACTION_DATA_UPDATED)){
            final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            final ComponentName cn = new ComponentName(context, StockWidgetProvider.class);
            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.stock_list_widget);

        }
        if (action.equals(CLICK_ACTION)) {
            final String stockSymbol = intent.getStringExtra(StockDetailActivityFragment.DETAIL_SYMBOL);
            final Uri mUri = intent.getParcelableExtra(StockDetailActivityFragment.DETAIL_URI);

            Intent widgetIntentClick = new Intent(context, StockDetailActivity.class);
            widgetIntentClick.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
            widgetIntentClick.putExtra(StockDetailActivityFragment.DETAIL_SYMBOL,stockSymbol);
            widgetIntentClick.putExtra(StockDetailActivityFragment.DETAIL_URI, mUri);

            context.startActivity(widgetIntentClick);
        }
        super.onReceive(context, intent);
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            // Create an Intent to launch ExampleActivity
            Intent intent = new Intent(context, StockWidgetService.class);
/*
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
*/

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            views.setRemoteAdapter(R.id.stock_list_widget, intent);

            views.setEmptyView(R.id.stock_list_widget, R.id.widget_error);

            final Intent onClickIntent = new Intent(context, StockWidgetProvider.class);
            onClickIntent.setAction(StockWidgetProvider.CLICK_ACTION);
            onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            //onClickIntent.setData(Uri.parse(onClickIntent.toUri(Intent.URI_INTENT_SCHEME)));
            final PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, 0,
                    onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.stock_list_widget, onClickPendingIntent);



            // Get the layout for the App Widget and attach an on-click listener
            // to the button



            //views.setOnClickFillInIntent(R.id.stock_list_widget, intent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}