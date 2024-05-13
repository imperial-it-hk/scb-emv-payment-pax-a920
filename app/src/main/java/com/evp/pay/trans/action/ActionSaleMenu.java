package com.evp.pay.trans.action;

import android.content.Context;
import android.content.Intent;

import com.evp.abl.core.AAction;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.menu.PaymentPlatformMenuActivity;

public class ActionSaleMenu extends AAction {
    private Context context;
    public ActionSaleMenu(ActionStartListener listener) {
        super(listener);
    }

    public void setParam(Context context) {
        this.context = context;
    }

    @Override
    protected void process() {
        Intent intentForPaymentFlatformMenu = new Intent(context, PaymentPlatformMenuActivity.class);
        intentForPaymentFlatformMenu.putExtra(EUIParamKeys.NAV_TITLE.toString(), "Payment Platform");
        context.startActivity(intentForPaymentFlatformMenu);
    }
}
