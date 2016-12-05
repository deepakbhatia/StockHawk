package com.bazaar.mizaaz.ui;

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bazaar.mizaaz.R;
import com.bazaar.mizaaz.data.Contract;
import com.bazaar.mizaaz.message.NetworkChangeMessage;
import com.bazaar.mizaaz.sync.QuoteSyncJob;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;

//Todo
//Stock Duplicate DONE
//Widget
//Delete Stock DONE
//Delete Current Selection DONE
public class MainActivity extends ActionBarActivity implements StockListFragment.Callback {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final String DETAILFRAGMENT_TAG = "DFTAG";

    private boolean mTwoPane;
    private EventBus bus = EventBus.getDefault();
    @BindView(R.id.fragment_stock_detail_container_empty_view)
    TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        /* Register for network change event */
        bus.register(this);

        if (findViewById(R.id.fragment_stock_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_stock_detail_container, new StockDetailActivityFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        StockListFragment stockListFragment = (StockListFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.stock_list_fragment_tag));



        if (stockListFragment == null)
        {
            stockListFragment =  new StockListFragment();


        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_stock_list, stockListFragment, getString(R.string.stock_list_fragment_tag))
                .commit();


        stockListFragment.setTwoPane(mTwoPane);
        QuoteSyncJob.initialize(this);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*String location = Utility.getPreferredLocation( this );
        // update the location in our second pane using the fragment manager
        if (location != null && !location.equals(mLocation)) {
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if ( null != ff ) {
                ff.onLocationChanged();
            }
            DetailFragment df = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if ( null != df ) {
                df.onLocationChanged(location);
            }
            mLocation = location;
        }*/
    }

    public void addStock(String symbol){
        ((StockListFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_stock_list)).addStock(symbol);
    }
    @Override
    public void onItemSelected(String symbol) {

        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.

            Bundle args = new Bundle();
            args.putString(StockDetailActivityFragment.DETAIL_SYMBOL,symbol);
            //args.putString(StockDetailActivityFragment.DETAIL_URI, contentUri.toString());
            args.putParcelable(StockDetailActivityFragment.DETAIL_URI, Contract.Quote.makeUriForStock(symbol));
            /*StockDetailActivityFragment df = (StockDetailActivityFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if ( null != df ) {
                df.updateFragment(symbol,contentUri.toString());
            }*/
            StockDetailActivityFragment fragment =  new StockDetailActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_stock_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent openStockDetailIntent = new Intent(this, StockDetailActivity.class);

            openStockDetailIntent.putExtra(StockDetailActivityFragment.DETAIL_SYMBOL,symbol);
            openStockDetailIntent.putExtra(StockDetailActivityFragment.DETAIL_URI,Contract.Quote.makeUriForStock(symbol));

            startActivity(openStockDetailIntent);
        }
    }

    @Subscribe
    public void onEvent(NetworkChangeMessage event){

        View rootView = (View)findViewById(R.id.main_activity_root);

        Snackbar connectMessageBar = Snackbar.make(rootView,event.message,Snackbar.LENGTH_LONG);

        connectMessageBar.show();
    }

    @Override
    protected void onDestroy() {
        // Unregister
        bus.unregister(this);
        super.onDestroy();
    }



}
