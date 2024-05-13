package com.evp.pay.trans.action;

import android.content.Context;
import android.content.Intent;

import com.evp.abl.core.AAction;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.menu.QRPaymentCSBMenuActivity;

public class ActionQRPaymentCSBMenu extends AAction {
    private Context context;

    public ActionQRPaymentCSBMenu(ActionStartListener listener) {
        super(listener);
    }

    public void setParam(Context context) {
        this.context = context;
    }

    @Override
    protected void process() {
        Intent intentForQrCodeMenu = new Intent(context, QRPaymentCSBMenuActivity.class);
        intentForQrCodeMenu.putExtra(EUIParamKeys.NAV_TITLE.toString(), "Payment Platform");
        context.startActivity(intentForQrCodeMenu);
    }
}
