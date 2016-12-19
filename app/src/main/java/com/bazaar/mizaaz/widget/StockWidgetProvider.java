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

    private static final String WIDGET_CLICK = "";

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();
        if(action.equals(QuoteSyncJob.ACTION_DATA_UPDATED)){
            final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            final ComponentName cn = new ComponentName(context, StockWidgetProvider.class);
            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.stock_list_widget);

        }
        if (action.equals(WIDGET_CLICK)) {
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

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            views.setRemoteAdapter(R.id.stock_list_widget, intent);

            views.setEmptyView(R.id.stock_list_widget, R.id.widget_error);

            final Intent onClickIntent = new Intent(context, StockWidgetProvider.class);
            onClickIntent.setAction(StockWidgetProvider.WIDGET_CLICK);
            onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            final PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, 0,
                    onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.stock_list_widget, onClickPendingIntent);


            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
