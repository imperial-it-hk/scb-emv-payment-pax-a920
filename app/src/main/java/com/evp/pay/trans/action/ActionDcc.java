package com.evp.pay.trans.action;

import android.content.Context;
import android.content.Intent;

import com.evp.abl.core.AAction;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.trans.action.activity.DccGetCardholderDecisionActivity;

public class ActionDcc extends AAction {
    private Context context;
    private String title;
    private String domesticAmount;
    private String foreignAmount;

    public ActionDcc(ActionStartListener listener) {
        super(listener);
    }

    public void setParam(Context context, String title, String domesticAmount, String foreignAmount) {
        this.context = context;
        this.title = title;
        this.domesticAmount = domesticAmount;
        this.foreignAmount = foreignAmount;
    }

    @Override
    protected void process() {
        Intent intent = new Intent(context, DccGetCardholderDecisionActivity.class);
        intent.putExtra(EUIParamKeys.NAV_TITLE.toString(), title);
        intent.putExtra(EUIParamKeys.DCC_DOMESTIC_AMOUNT.toString(), domesticAmount);
        intent.putExtra(EUIParamKeys.DCC_FOREIGN_AMOUNT.toString(), foreignAmount);
        context.startActivity(intent);
    }
}
