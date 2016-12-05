package com.bazaar.mizaaz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.bazaar.mizaaz.R;

import butterknife.BindView;

import static com.bazaar.mizaaz.ui.MainActivity.DETAILFRAGMENT_TAG;


public class StockDetailActivity extends AppCompatActivity {

    @BindView(R.id.fragment_stock_detail_container_empty_view)
    TextView emptyView;

    private String symbol;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        //View back = (View)findViewById(android.R.id.home);
        //getSupportActionBar()..requestFocus();
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stock_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.detailViewToolbar);

        setSupportActionBar(toolbar);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent stockIntent = getIntent();

        if(stockIntent.hasExtra(StockDetailActivityFragment.DETAIL_SYMBOL))
            symbol = stockIntent.getStringExtra(StockDetailActivityFragment.DETAIL_SYMBOL);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            Bundle args = new Bundle();
            args.putString(StockDetailActivityFragment.DETAIL_SYMBOL,symbol);
            //args.putString(StockDetailActivityFragment.DETAIL_URI, stockJson);
            args.putParcelable(StockDetailActivityFragment.DETAIL_URI, getIntent().getParcelableExtra(StockDetailActivityFragment.DETAIL_URI));


            StockDetailActivityFragment fragment = new StockDetailActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_stock_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    /*//Receive Message on no Stock Data in Detail
    @Subscribe
    public void onEvent(EmptyStockMessage event){

       if(event.empty)
       {
           emptyView.setVisibility(View.VISIBLE);

       }else{
           emptyView.setVisibility(View.GONE);

       }
    }
*/

}


