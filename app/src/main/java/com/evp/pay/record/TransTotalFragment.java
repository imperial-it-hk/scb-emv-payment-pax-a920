/*
 * ===========================================================================================
 * = COPYRIGHT
 *          PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or nondisclosure
 *   agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *   disclosed except in accordance with the terms in that agreement.
 *     Copyright (C) 2019-? PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.
 * Description: // Detail description about the function of this module,
 *             // interfaces with the other modules, and dependencies.
 * Revision History:
 * Date                  Author	                 Action
 * 20190108  	         Steven.W                Create
 * ===========================================================================================
 */
package com.evp.pay.record;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.TransTotal;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.pay.app.FinancialApplication;
import com.evp.payment.evpscb.R;

/**
 * The type Trans total fragment.
 */
public class TransTotalFragment extends Fragment {

    private TextView saleNumberTv;
    private TextView saleAmountTv;
    private TextView refundNumberTv;
    private TextView refundAmountTv;
    private TextView voidedSaleNumberTv;
    private TextView voidedSaleAmountTv;
    private TextView voidedRefundNumberTv;
    private TextView voidedRefundAmountTv;
    private TextView offlineNumberTv;
    private TextView offlineAmountTv;

    private String acquirerName = "";

    /**
     * Instantiates a new Trans total fragment.
     */
    public TransTotalFragment() {
        //do nothing
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_trans_total_layout, container, false);

        saleNumberTv = (TextView) view.findViewById(R.id.sale_total_sum);
        saleAmountTv = (TextView) view.findViewById(R.id.sale_total_amount);

        refundNumberTv = (TextView) view.findViewById(R.id.refund_total_sum);
        refundAmountTv = (TextView) view.findViewById(R.id.refund_total_amount);

        voidedSaleNumberTv = (TextView) view.findViewById(R.id.void_sale_total_sum);
        voidedSaleAmountTv = (TextView) view.findViewById(R.id.void_sale_total_amount);

        voidedRefundNumberTv = (TextView) view.findViewById(R.id.void_refund_total_sum);
        voidedRefundAmountTv = (TextView) view.findViewById(R.id.void_refund_total_amount);

        offlineNumberTv = (TextView) view.findViewById(R.id.offline_total_sum);
        offlineAmountTv = (TextView) view.findViewById(R.id.offline_total_amount);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initTables();
    }

    private void initTables() {
        Acquirer acquirer = FinancialApplication.getAcqManager().findAcquirer(acquirerName);
        if (acquirer == null)
            return;

        TransTotal total = GreendaoHelper.getTransTotalHelper().calcTotal(acquirer);
        String saleAmt = CurrencyConverter.convert(total.getSaleTotalAmt());
        //AET-18
        String refundAmt = CurrencyConverter.convert(0 - total.getRefundTotalAmt());
        String voidSaleAmt = CurrencyConverter.convert(0 - total.getSaleVoidTotalAmt());
        String voidRefundAmt = CurrencyConverter.convert(total.getRefundVoidTotalAmt());
        String offlineAmt = CurrencyConverter.convert( total.getOfflineTotalAmt());

        saleNumberTv.setText(String.valueOf(total.getSaleTotalNum()));
        saleAmountTv.setText(saleAmt);

        refundNumberTv.setText(String.valueOf(total.getRefundTotalNum()));
        refundAmountTv.setText(refundAmt);

        voidedSaleNumberTv.setText(String.valueOf(total.getSaleVoidTotalNum()));
        voidedSaleAmountTv.setText(voidSaleAmt);

        voidedRefundNumberTv.setText(String.valueOf(total.getRefundVoidTotalNum()));
        voidedRefundAmountTv.setText(voidRefundAmt);

        offlineNumberTv.setText(String.valueOf(total.getOfflineTotalNum()));
        offlineAmountTv.setText(offlineAmt);

    }

    /**
     * Gets acquirer name.
     *
     * @return the acquirer name
     */
    public String getAcquirerName() {
        return acquirerName;
    }

    /**
     * Sets acquirer name.
     *
     * @param acquirerName the acquirer name
     */
    public void setAcquirerName(String acquirerName) {
        this.acquirerName = acquirerName;
    }
}
