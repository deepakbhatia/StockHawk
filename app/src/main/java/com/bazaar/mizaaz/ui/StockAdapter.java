package com.bazaar.mizaaz.ui;


import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bazaar.mizaaz.R;
import com.bazaar.mizaaz.data.Contract;
import com.bazaar.mizaaz.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    final private Context context;
    final private DecimalFormat dollarFormatWithPlus;
    final private DecimalFormat dollarFormat;
    final private DecimalFormat percentageFormat;
    private Cursor cursor;
    private final StockAdapterOnClickHandler clickHandler;
    private  View  previousSelected = null;

    StockAdapter(Context context, StockAdapterOnClickHandler clickHandler) {
        this.context = context;
        this.clickHandler = clickHandler;

        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        dollarFormatWithPlus.setNegativePrefix("-$");
        dollarFormatWithPlus.setNegativeSuffix("");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }

    void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }



    String getSymbolAtPosition(int position) {

        if(cursor.moveToPosition(position))
            return cursor.getString(Contract.Quote.POSITION_SYMBOL);
        else
            return null;
    }

    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View item = LayoutInflater.from(context).inflate(R.layout.list_item_quote, parent, false);

        return new StockViewHolder(item);
    }

    @Override
    public void onBindViewHolder(StockViewHolder holder, int position) {

        cursor.moveToPosition(position);

        String symbol  = cursor.getString(Contract.Quote.POSITION_SYMBOL);
        holder.symbol.setText(symbol);
        holder.price.setText(dollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_PRICE)));


        float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);

        float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
        String higherOrLower;
        if (rawAbsoluteChange >= 0) {
            holder.change.setBackgroundResource(R.drawable.percent_change_pill_green);
            higherOrLower = context.getString(R.string.price_increased_by);
        } else {
            holder.change.setBackgroundResource(R.drawable.percent_change_pill_red);
            higherOrLower = context.getString(R.string.price_decreased_by);
        }

        String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = percentageFormat.format(percentageChange / 100);
        String delta ;
        if (PrefUtils.getDisplayMode(context)
                .equals(context.getString(R.string.pref_display_mode_absolute_key))) {
            holder.change.setText(change);
            delta = change + context.getString(R.string.pref_display_mode_absolute_key);
        } else {
            holder.change.setText(percentage);
            delta = percentage + context.getString(R.string.pref_display_mode_percentage_key);

        }
        
        holder.change.setContentDescription(String.format(
                context.getString(R.string.stock_price_change),symbol,higherOrLower,delta));


    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
        }
        return count;
    }


    interface StockAdapterOnClickHandler {
        void onClick(String symbol, int position);
    }

    class StockViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.symbol)
        TextView symbol;

        @BindView(R.id.price)
        TextView price;

        @BindView(R.id.change)
        TextView change;

        StockViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            cursor.moveToPosition(adapterPosition);
            if(previousSelected!=null)
            {
                previousSelected.setActivated(false);
            }
            v.setActivated(true);
            previousSelected = v;
            int symbolColumn = cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL);
            clickHandler.onClick(cursor.getString(symbolColumn),adapterPosition);

        }


    }
}
