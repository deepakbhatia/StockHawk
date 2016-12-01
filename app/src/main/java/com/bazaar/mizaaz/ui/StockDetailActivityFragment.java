package com.bazaar.mizaaz.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bazaar.mizaaz.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class StockDetailActivityFragment extends Fragment {
    static final String DETAIL_URI = "URI";
    static final String DETAIL_SYMBOL = "SYMBOL";

    String symbol;

    String stockHistory;

    @BindView(R.id.stockDetailChart)
    LineChart stockChart;
    XAxis xAxis;

    public StockDetailActivityFragment() {
    }

    public static StockDetailActivityFragment newInstance(String symbol, String stockHistory){
        StockDetailActivityFragment stockDetailActivityFragment = new StockDetailActivityFragment();

        Bundle arguments = new Bundle();
        arguments.putString(StockDetailActivityFragment.DETAIL_SYMBOL,symbol);
        arguments.putString(StockDetailActivityFragment.DETAIL_URI,stockHistory);
        stockDetailActivityFragment.setArguments(arguments);

        return stockDetailActivityFragment;
    }
    private void processStockHistory()
    {
        Log.d("onLoadFinished:data",""+stockHistory+":");
        String[] lines = stockHistory.split("\n");

        Log.d("onLoadFinished:data",""+stockHistory+":"+lines[0]);
        ArrayList<Entry> entries = new ArrayList<Entry>();
        ArrayList<String> dateValues = new ArrayList<>();
        for(int i=0;i<lines.length;i++){
            StringTokenizer dataToken = new StringTokenizer(lines[i],",");

            long date = Long.valueOf(dataToken.nextToken().trim());
            String dateString = new SimpleDateFormat("MM/dd/yyyy").format(new Date(date));
            float price = Float.valueOf(dataToken.nextToken().trim());

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

            if(stockDetailArg.containsKey(StockDetailActivityFragment.DETAIL_URI))
                stockHistory = stockDetailArg.getString(StockDetailActivityFragment.DETAIL_URI);

            if(stockHistory != null){
                processStockHistory();
            }

            //stockChart = (LineChart)stockDetailView.findViewById(R.id.stockDetailChart);

            stockChart.setBackgroundColor(getActivity().getResources().getColor(android.R.color.white));
            xAxis = stockChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setTextSize(10f);
            xAxis.setTextColor(Color.WHITE);
            xAxis.setDrawAxisLine(true);
            xAxis.setDrawGridLines(false);
            xAxis.setTextColor(Color.rgb(255, 192, 56));
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

    public  void updateFragment(String symbol,String stockHistory){
        this.symbol = symbol;
        this.stockHistory = stockHistory;
        processStockHistory();

    }
    public  void updateChart(ArrayList<Entry> chartEntry, ArrayList<String> dateValues)
    {
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

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1); // add the datasets

        LineData lineData = new LineData(dateValues,set1);
        /*for(ILineDataSet dataD : dataSets){
            lineData.addDataSet(dataD);
        }
*/



        stockChart.setData(lineData);
        stockChart.notifyDataSetChanged();
        stockChart.invalidate();
    }
}
