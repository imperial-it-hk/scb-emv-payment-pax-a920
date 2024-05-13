package com.evp.pay.trans.action;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.evp.abl.core.AAction;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.trans.action.activity.ConfirmAmountActivity;

public class ActionShowConfirmAmount extends AAction {
    private Context context;
    private String title;
    private String amount;
    private String currency;
    private String fundingSource;

    public ActionShowConfirmAmount(ActionStartListener listener) {
        super(listener);
    }

    public void setParam(Context context, String title, String amount, String currency, String fundingSource) {
        this.context = context;
        this.title = title;
        this.amount = amount;
        this.currency = currency;
        this.fundingSource = fundingSource;
    }

    @Override
    protected void process() {
        Intent intentForConfirmAmountActivity = new Intent(context, ConfirmAmountActivity.class);
        Bundle bundleForConfirmAmountActivity = new Bundle();
        bundleForConfirmAmountActivity.putString(EUIParamKeys.NAV_TITLE.toString(), title);
        bundleForConfirmAmountActivity.putString(EUIParamKeys.TRANS_AMOUNT.toString(), amount);
        bundleForConfirmAmountActivity.putString(EUIParamKeys.CURRENCY.toString(), currency);
        bundleForConfirmAmountActivity.putString(EUIParamKeys.FUNDING_SOURCE.toString(), fundingSource);
        intentForConfirmAmountActivity.putExtras(bundleForConfirmAmountActivity);
        context.startActivity(intentForConfirmAmountActivity);
    }
}
