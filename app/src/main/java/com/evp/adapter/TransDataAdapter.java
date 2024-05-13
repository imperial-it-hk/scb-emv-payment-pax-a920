package com.evp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.evp.bizlib.data.entity.TransData;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.config.ConfigUtils;
import com.evp.pay.constant.Constants;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;

import java.util.Currency;
import java.util.List;

public class TransDataAdapter extends ArrayAdapter<TransData> {
    private Context context;
    private List<TransData> transDataList;

    public TransDataAdapter(Context context, List<TransData> list) {
        super(context, 0, list);
        this.context = context;
        this.transDataList = list;
    }

    @Override
    public int getCount() {
        return transDataList.size();
    }

    @Override
    public TransData getItem(int i) {
        return transDataList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View listItem = view;
        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(R.layout.activity_suspend_item, viewGroup, false);
        }

        TransData currentTransData = transDataList.get(i);
        ImageView paymentIcon = (ImageView) listItem.findViewById(R.id.payment_icon);
        paymentIcon.setImageBitmap(ConfigUtils.getInstance().getWalletImage(currentTransData.getFundingSource()));

        TextView transDate = (TextView) listItem.findViewById(R.id.trans_date_textview);
        String formattedDate = ConvertUtils.convert(currentTransData.getDateTime(), Constants.TIME_PATTERN_TRANS,
                Constants.TIME_PATTERN_DISPLAY3);
        String formattedTime = ConvertUtils.convert(currentTransData.getDateTime(), Constants.TIME_PATTERN_TRANS, Constants.TIME_PATTERN);
        transDate.setText(formattedDate + "     " + formattedTime);

        TextView statusTextView = (TextView) listItem.findViewById(R.id.status_text);
        statusTextView.setText(ConfigUtils.getInstance().getString("suspendedLabel"));

        TextView currencyTextView = (TextView) listItem.findViewById(R.id.currency_text);
        currencyTextView.setText(Currency.getInstance(currentTransData.getCurrency()).getCurrencyCode());

        TextView amountTExtView = (TextView) listItem.findViewById(R.id.amount_text);
        amountTExtView.setText(CurrencyConverter.convert(Utils.parseLongSafe(currentTransData.getAmount(), 0), currentTransData.getCurrency()));
        return listItem;
    }
}
