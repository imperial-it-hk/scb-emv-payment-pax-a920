package com.evp.pay.trans.action;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.evp.abl.core.AAction;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.trans.action.activity.QRCodeActivity;
import com.evp.pay.utils.Utils;

public class ActionShowQRCodePage extends AAction {
    private Context context;
    private String qrcode;
    private String amount;
    private String currency;
    private String fundingSource;
    private String fundingSourceImagePath;

    public ActionShowQRCodePage(ActionStartListener listener) {
        super(listener);
    }

    public void setParam(Context context, String qrcode, String amount, String currency, String fundingSource, String fundingSourceImagePath) {
        this.context = context;
        this.qrcode = qrcode;
        this.amount = amount;
        this.currency = currency;
        this.fundingSource = fundingSource;
        this.fundingSourceImagePath = fundingSourceImagePath;
    }

    @Override
    protected void process() {
        amount = CurrencyConverter.convert(Utils.parseLongSafe(amount, 0), true);
        Intent intentForQrCodePage = new Intent(context, QRCodeActivity.class);
        Bundle bundleForQrCodePage = new Bundle();
        bundleForQrCodePage.putString(EUIParamKeys.NAV_TITLE.toString(), currency + " " + amount);
        bundleForQrCodePage.putString("qrcode", qrcode);
        bundleForQrCodePage.putString(EUIParamKeys.FUNDING_SOURCE.toString(), fundingSource);
        bundleForQrCodePage.putString("fundingSourceImagePath", fundingSourceImagePath);
        bundleForQrCodePage.putString("amount", amount);
        bundleForQrCodePage.putString("currency", currency);
        intentForQrCodePage.putExtras(bundleForQrCodePage);
        context.startActivity(intentForQrCodePage);
    }
}
