package com.bazaar.mizaaz.ui;



import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bazaar.mizaaz.R;
import com.bazaar.mizaaz.data.Contract;
import com.bazaar.mizaaz.data.PrefUtils;
import com.bazaar.mizaaz.message.BackPressMessage;
import com.bazaar.mizaaz.message.GetStockUri;
import com.bazaar.mizaaz.message.NetworkChangeMessage;
import com.bazaar.mizaaz.message.StockUpdateFail;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements StockListFragment.Callback {

    public static final String DETAILFRAGMENT_TAG = StockDetailActivityFragment.class.getSimpleName();

    private boolean mTwoPane;
    private EventBus bus = EventBus.getDefault();
    @BindView(R.id.fragment_stock_detail_container_empty_view)
    TextView emptyView;

    @BindView(R.id.stockFab)
    FloatingActionButton fab;

    Snackbar connectMessageBar;

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
        //QuoteSyncJob.initialize(this);


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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_CANCELED){
            EventBus.getDefault().post(new BackPressMessage(true));
        }
    }

    @OnClick(R.id.stockFab)
    public void addStockDialog(View view) {

        new AddStockDialog().show(getSupportFragmentManager(), getString(R.string.stock_dialog_frag));
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
            args.putParcelable(StockDetailActivityFragment.DETAIL_URI, Contract.Quote.makeUriForStock(symbol));

            StockDetailActivityFragment fragment =  new StockDetailActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_stock_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent openStockDetailIntent = new Intent(this, StockDetailActivity.class);

            openStockDetailIntent.putExtra(StockDetailActivityFragment.DETAIL_SYMBOL,symbol);
            openStockDetailIntent.putExtra(StockDetailActivityFragment.DETAIL_URI,Contract.Quote.makeUriForStock(symbol));

            startActivityForResult(openStockDetailIntent,RESULT_CANCELED);
        }
    }

    @Subscribe
    public void noStockEvent(GetStockUri event){

        String symbol = event.symbol;

        String message  = String.format(getResources().getString(R.string.no_stock_exists),symbol);

        View rootView = (View)findViewById(R.id.main_activity_root);


        final Snackbar noStockMessageBar = Snackbar.make(rootView,message,Snackbar.LENGTH_INDEFINITE);

        noStockMessageBar.setAction(R.string.dismiss_connection_message, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noStockMessageBar.dismiss();
            }
        });

        noStockMessageBar.show();

        PrefUtils.removeStock(this, symbol);


    }
    @Subscribe
    public void networkChangeEvent(NetworkChangeMessage event){

        View rootView = (View)findViewById(R.id.main_activity_root);

        if(event.connected){
            if(connectMessageBar!=null && connectMessageBar.isShown())
            {
                connectMessageBar.dismiss();

            }
        }else{
            connectMessageBar  = Snackbar.make(rootView,event.message,Snackbar.LENGTH_INDEFINITE);
            connectMessageBar.setAction(R.string.dismiss_connection_message, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    connectMessageBar.dismiss();
                }
            });

            connectMessageBar.show();
        }


    }


    @Subscribe
    public void stockUpdateFailEvent(StockUpdateFail updateFail){
        View rootView = (View)findViewById(R.id.main_activity_root);

        Snackbar connectMessageBar = Snackbar.make(rootView,updateFail.updateMessage,Snackbar.LENGTH_LONG);

        connectMessageBar.show();
    }
    @Override
    protected void onDestroy() {
        // Unregister
        bus.unregister(this);
        super.onDestroy();
    }





}
