package com.bazaar.mizaaz.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import com.bazaar.mizaaz.R;
import com.bazaar.mizaaz.data.Contract;
import com.bazaar.mizaaz.data.PrefUtils;
import com.bazaar.mizaaz.data.Stock;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

import butterknife.BindView;
import butterknife.ButterKnife;

import static java.text.DateFormat.getDateInstance;

/**
 * A placeholder fragment containing a simple view.
 */
public class StockDetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    static final String DETAIL_URI = "URI";
    static final String DETAIL_SYMBOL = "SYMBOL";
    private static final int STOCK_DETAIL_LOADER = 2;

    private String symbol;

    private String stockHistory;

    @BindView(R.id.stockDetailChart)
    LineChart stockChart;
    @BindView(R.id.detail_date_textview)
    TextView detailDateTextView;
    @BindView(R.id.detail_high_textview)
    TextView detailHighTextView;
    @BindView(R.id.detail_change_textview)
    TextView detailChangeTextView;
    @BindView(R.id.detail_low_textview)
    TextView detailLowTextView;
    @BindView(R.id.stock_price_textview)
    TextView detailPriceTextView;
    @BindView(R.id.detail_symbol_textview)
    TextView detailSymbolTextView;
    @BindView(R.id.previousCloseLabel)
    TextView previousCloseLabelTextView;
    @BindView(R.id.todayOpeningLabel)
    TextView todayOpeningLabelTextView;

    private XAxis xAxis;

    private Stock selectedStock;

    final private DecimalFormat dollarFormatWithPlus;
    final private DecimalFormat dollarFormat;
    final private DecimalFormat percentageFormat;
    private Uri mStockUri;


    public StockDetailActivityFragment() {
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }


    private void processStockDetail(){
        String symbol  = selectedStock.stockSymbol;
        detailSymbolTextView.setText(symbol);
        detailPriceTextView.setText(dollarFormat.format(selectedStock.stockCurrentPrice));


        float rawAbsoluteChange = selectedStock.absoluteStockChange;
        float percentageChange = selectedStock.percentStockChange;
        String higherOrLower;
        if (rawAbsoluteChange > 0) {
            detailChangeTextView.setBackgroundResource(R.drawable.percent_change_pill_green);
            higherOrLower = getActivity().getString(R.string.price_increased_by);
        } else {
            detailChangeTextView.setBackgroundResource(R.drawable.percent_change_pill_red);
            higherOrLower = getActivity().getString(R.string.price_decreased_by);
        }

        String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = percentageFormat.format(percentageChange / 100);
        String delta;
        if (PrefUtils.getDisplayMode(getActivity())
                .equals(getActivity().getString(R.string.pref_display_mode_absolute_key))) {
            detailChangeTextView.setText(change);
            delta = change + getActivity().getString(R.string.pref_display_mode_absolute_key);
        } else {
            detailChangeTextView.setText(percentage);
            delta = percentage + getActivity().getString(R.string.pref_display_mode_absolute_key);

        }

        detailChangeTextView.setContentDescription(String.format(
                getActivity().getString(R.string.stock_price_change),symbol,higherOrLower,delta));


        detailHighTextView.setText(String.valueOf(selectedStock.previousClose));
        detailLowTextView.setText(String.valueOf(selectedStock.stockOpen));
        detailDateTextView.setText(String.format(getActivity().getString(R.string.update_date),selectedStock.dateValue));

        previousCloseLabelTextView.setText(getActivity().getString(R.string.previous_day_close));
        todayOpeningLabelTextView.setText(getActivity().getString(R.string.today_opening));


    }
    private void processStockHistory()
    {

        //TODO
        Log.d("onLoadFinished:data",""+stockHistory+":");
        String[] lines = stockHistory.split("\n");

        //TODO
        Log.d("onLoadFinished:data",""+stockHistory+":"+lines[0]);
        ArrayList<Entry> entries = new ArrayList<Entry>();
        ArrayList<String> dateValues = new ArrayList<>();
        for(int i=0;i<lines.length;i++){
            StringTokenizer dataToken = new StringTokenizer(lines[i],",");

            long date = Long.valueOf(dataToken.nextToken().trim());
            String dateString = getDateInstance().format(new Date(date));
            float price = Float.valueOf(dataToken.nextToken().trim());

            //TODO
            Log.d("onLoadFinished:data",""+price);
            entries.add(new Entry( price,i));
            dateValues.add(dateString);
        }

        updateChart(entries,dateValues);
        //((StockDetailActivityFragment)getSupportFragmentManager().findFragmentById(R.id.stockDetailFragment)).updateFragment(entries,dateValues);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View stockDetailView =  inflater.inflate(R.layout.fragment_stock_detail, container, false);

        ButterKnife.bind(this,stockDetailView);

        Bundle stockDetailArg = getArguments();

        if(stockDetailArg !=null){
            if(stockDetailArg.containsKey(StockDetailActivityFragment.DETAIL_SYMBOL))
                symbol = stockDetailArg.getString(StockDetailActivityFragment.DETAIL_SYMBOL);

            if(stockDetailArg.containsKey(StockDetailActivityFragment.DETAIL_URI)){


                mStockUri  = stockDetailArg.getParcelable(StockDetailActivityFragment.DETAIL_URI);


            }

            //TOOD
            Log.d("Loadfinished",mStockUri.toString());
            /*stockHistory = selectedStock.stockHistory;
            //stockHistory = stockDetailArg.getString(StockDetailActivityFragment.DETAIL_URI);

            processStockDetail();

            if(stockHistory != null){
                processStockHistory();
            }*/

            //stockChart = (LineChart)stockDetailView.findViewById(R.id.stockDetailChart);

            stockChart.setBackgroundColor(getActivity().getResources().getColor(android.R.color.white));

            //stockChart.setVisibility(View.GONE);
            xAxis = stockChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setTextSize(12f);
            xAxis.setTextColor(Color.WHITE);
            xAxis.setDrawAxisLine(true);
            xAxis.setDrawGridLines(false);
            xAxis.setTextColor(Color.BLACK);
            xAxis.setAvoidFirstLastClipping(true);
            xAxis.setSpaceBetweenLabels(5);
        /*xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(24f); // one hour
        xAxis.setValueFormatter(new IAxisValueFormatter() {

            private SimpleDateFormat mFormat = new SimpleDateFormat("dd MMM");

            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                long millis = TimeUnit.HOURS.toMillis((long) value);
                return mFormat.format(new Date((long)value));
            }
        });*/
        }

        return stockDetailView;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(STOCK_DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    public  void updateFragment(String symbol, String stockDetail){


        this.symbol = symbol;



        Gson stockGSON = new Gson();

        selectedStock = stockGSON.fromJson(stockDetail,Stock.class);

        stockHistory = selectedStock.stockHistory;
        processStockDetail();

        processStockHistory();

    }
    private void updateChart(ArrayList<Entry> chartEntry, ArrayList<String> dateValues)
    {
        //TODO
        Log.d("updateChart",""+chartEntry.size()+":"+dateValues.size());
        LineDataSet set1 = new LineDataSet(chartEntry, "Label"); // add entries to dataset
        set1.setMode(LineDataSet.Mode.LINEAR);
        set1.setAxisDependency(YAxis.AxisDependency.RIGHT);
        set1.setColor(getActivity().getResources().getColor(R.color.colorAccent));
        set1.setValueTextColor(getActivity().getResources().getColor(R.color.material_blue_700));
        set1.setValueTextSize(10);
        set1.setLineWidth(2f);
        set1.setDrawCircles(true);
        set1.setDrawValues(true);
        set1.setFillAlpha(100);
        set1.setDrawCircleHole(true);
        set1.setCircleRadius(5f);
        set1.setCircleColor(getActivity().getResources().getColor(R.color.material_blue_700));

        LineData lineData = new LineData(dateValues,set1);

        stockChart.setData(lineData);
        stockChart.notifyDataSetChanged();
        stockChart.invalidate();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if ( null != mStockUri ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mStockUri,
                    Contract.Quote.QUOTE_COLUMNS,
                    null,
                    null,
                    null
            );
        }

        //To ensure if there is no data available, the card view also is not visible.

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if(data.getCount() > 0){
            ViewParent vp = getView().getParent();
            if ( vp instanceof CardView) {
                ((View)vp).setVisibility(View.VISIBLE);

            }

            data.moveToFirst();
            selectedStock = new Stock();
            selectedStock.stockHistory = data.getString(Contract.Quote.POSITION_HISTORY);
            selectedStock.previousClose = data.getFloat(Contract.Quote.POSITION_PREVIOUS_CLOSE);
            selectedStock.stockOpen = data.getFloat(Contract.Quote.POSITION_OPEN);
            selectedStock.dateValue = data.getString(Contract.Quote.POSITION_DATE);
            selectedStock.stockSymbol = data.getString(Contract.Quote.POSITION_SYMBOL);
            selectedStock.percentStockChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
            selectedStock.absoluteStockChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            selectedStock.stockCurrentPrice = data.getFloat(Contract.Quote.POSITION_PRICE);
            stockHistory = selectedStock.stockHistory;
            Gson stockGson = new Gson();

            stockGson.toJson(selectedStock);

            //TODO
            Log.d("onLoadFinished:data",""+selectedStock.stockHistory);

            processStockHistory();
            processStockDetail();
        }
        else {
            ViewParent vp = getView().getParent();
            if ( vp instanceof CardView) {
                ((View)vp).setVisibility(View.INVISIBLE);
            }

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
