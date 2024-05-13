package com.evp.pay.trans.action;

import android.content.Context;
import android.content.Intent;

import com.evp.abl.core.AAction;
import com.evp.config.ConfigUtils;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.menu.QRPaymentBSCMenuActivity;

public class ActionQRPaymentBSCMenu extends AAction {
    private Context context;

    public ActionQRPaymentBSCMenu(ActionStartListener listener) {
        super(listener);
    }

    public void setParam(Context context) {
        this.context = context;
    }

    @Override
    protected void process() {
        Intent intentForQrCodeMenu = new Intent(context, QRPaymentBSCMenuActivity.class);
        intentForQrCodeMenu.putExtra(EUIParamKeys.NAV_TITLE.toString(), ConfigUtils.getInstance().getString("buttonAllPayment"));
        context.startActivity(intentForQrCodeMenu);
    }
}
