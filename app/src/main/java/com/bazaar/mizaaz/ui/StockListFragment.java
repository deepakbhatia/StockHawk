package com.bazaar.mizaaz.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bazaar.mizaaz.R;
import com.bazaar.mizaaz.data.Contract;
import com.bazaar.mizaaz.data.PrefUtils;
import com.bazaar.mizaaz.sync.QuoteSyncJob;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import timber.log.Timber;

/**
 * Created by obelix on 29/11/2016.
 */

public class StockListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener,
        StockAdapter.StockAdapterOnClickHandler {

    private static final int STOCK_LOADER = 0;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.error)
    TextView error;
    private StockAdapter adapter;
    private String stockHistory;

    HashMap stockHistoryMap = new HashMap();
    private boolean mTwoPane = false;
    private static FragmentActivity mContext;
    private Unbinder unbinder;

    @Override
    public void onClick(String symbol) {
        Timber.d("Symbol clicked: %s", symbol);

        ((Callback)getActivity()).onItemSelected(symbol,(String)stockHistoryMap.get(symbol));
        /*Intent openStockDetailIntent = new Intent(getActivity(),StockDetailActivity.class);

        openStockDetailIntent.putExtra("symbol",symbol);
        openStockDetailIntent.putExtra("stockHistory",(String)stockHistoryMap.get(symbol));
        startActivity(openStockDetailIntent);*/
    }

    public interface Callback{
        public void onItemSelected(String symbol, String uri);
    }

    public static interface ClickListener{
        public void onClick(View view, int position);
        public void onLongClick(View view,int position);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root_view = inflater.inflate(R.layout.fragment_main,container,false);

        unbinder = ButterKnife.bind(this,root_view);

        //recyclerView = (RecyclerView)root_view.findViewById(R.id.recycler_view);

        adapter = new StockAdapter(getActivity(), this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));



        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);
        onRefresh();


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String symbol = adapter.getSymbolAtPosition(viewHolder.getAdapterPosition());
                PrefUtils.removeStock(getActivity(), symbol);
                getActivity().getContentResolver().delete(Contract.Quote.makeUriForStock(symbol), null, null);
            }
        }).attachToRecyclerView(recyclerView);

        setHasOptionsMenu(true);

        return root_view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext  = getActivity();
    }

    private  boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    private  void update(){
        QuoteSyncJob.syncImmediately(getActivity());
        getActivity().getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);
    }
    @Override
    public void onRefresh() {


        update();
        if (!networkUp() && adapter.getItemCount() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            error.setText(getString(R.string.error_no_network));
            error.setVisibility(View.VISIBLE);
        } else if (!networkUp()) {
            swipeRefreshLayout.setRefreshing(false);
            //Toast.makeText(this, R.string.toast_no_connectivity, Toast.LENGTH_LONG).show();
        } else if (PrefUtils.getStocks(getActivity()).size() == 0) {
            Timber.d("WHYAREWEHERE");
            swipeRefreshLayout.setRefreshing(false);
            error.setText(getString(R.string.error_no_stocks));
            error.setVisibility(View.VISIBLE);
        } else {
            error.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //getActivity().getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);
    }

    @OnClick(R.id.fab)
    public void addStockDialog(View view) {
        new AddStockDialog().show(getChildFragmentManager(), "StockDialogFragment");
    }

    public  void addStock(String symbol) {
        if (symbol != null && !symbol.isEmpty()) {

            if (networkUp()) {
                swipeRefreshLayout.setRefreshing(true);
            } else {
                String message = getString(R.string.toast_stock_added_no_connectivity, symbol);
                //Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }

            PrefUtils.addStock(mContext, symbol);

            update();

        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                Contract.Quote.uri,
                Contract.Quote.QUOTE_COLUMNS,
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        swipeRefreshLayout.setRefreshing(false);

        if (data.getCount() != 0) {
            error.setVisibility(View.GONE);
        }
        //TODO
        Log.d("DATASIZE",""+data.getColumnNames()[0]);
        Log.d("DATASIZE",""+data.getColumnNames()[1]);
        Log.d("DATASIZE",""+data.getColumnNames()[2]);
        Log.d("DATASIZE",""+data.getColumnNames()[3]);
        Log.d("DATASIZE",""+data.getColumnNames()[4]);
        Log.d("DATASIZE",""+data.getColumnNames()[5]);

        adapter.setCursor(data);

        stockHistoryMap = new HashMap();
        while(data.moveToNext()){
            Log.d("WHYAREWEHERE",""+data.getString(Contract.Quote.POSITION_SYMBOL));

            stockHistoryMap.put(data.getString(Contract.Quote.POSITION_SYMBOL),data.getString(Contract.Quote.POSITION_HISTORY));

        }

    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        swipeRefreshLayout.setRefreshing(false);
        adapter.setCursor(null);
    }


    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (PrefUtils.getDisplayMode(getActivity())
                .equals(getString(R.string.pref_display_mode_absolute_key))) {
            item.setIcon(R.drawable.ic_percentage);
        } else {
            item.setIcon(R.drawable.ic_dollar);
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_activity_settings, menu);
        MenuItem item = menu.findItem(R.id.action_change_units);
        setDisplayModeMenuItemIcon(item);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_change_units) {
            PrefUtils.toggleDisplayMode(getActivity());
            setDisplayModeMenuItemIcon(item);
            adapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setTwoPane(boolean mTwoPane){
        this.mTwoPane = mTwoPane;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}

