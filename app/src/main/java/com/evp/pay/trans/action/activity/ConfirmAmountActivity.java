package com.evp.pay.trans.action.activity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;

public class ConfirmAmountActivity extends BaseActivityWithTickForAction {
    private String title;
    private String amount;
    private String currency;
    private Button confirmBtn;
    private Button cancelBtn;
    private String fundingSource;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_comfirm_amount;
    }

    @Override
    protected void initViews() {
        LinearLayout paymentIconLayout = (LinearLayout) findViewById(R.id.bar_payment_layout);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.root);
        linearLayout.setBackgroundColor(secondaryColor);

        TextView amountTextView = (TextView) findViewById(R.id.amount_textview);
        TextView currencyTextView = (TextView) findViewById(R.id.currency_textview);
        ImageView paymentIconView = (ImageView) findViewById(R.id.bar_payment_icon);
        TextView typeTextView = (TextView) findViewById(R.id.title_textview);

        typeTextView.setText(ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_SALE));

        if (fundingSource != null) {
            paymentIconView.setImageBitmap(ConfigUtils.getInstance().getWalletImage(fundingSource));
        } else {
            paymentIconView.setVisibility(View.INVISIBLE);
        }

        if (primaryColor != -1) {
            paymentIconLayout.setBackground(new ColorDrawable(primaryColor));
        }

        confirmBtn = (Button) findViewById(R.id.confirm_btn);
        confirmBtn.setText(ConfigUtils.getInstance().getString("buttonConfirm"));
        cancelBtn = (Button) findViewById(R.id.clear_btn);
        cancelBtn.setText(ConfigUtils.getInstance().getString("buttonCancel"));

        amount = CurrencyConverter.convert(Utils.parseLongSafe(amount, 0));
        amountTextView.setText(amount);
        currencyTextView.setText(currency);
    }

    @Override
    protected String getTitleString() {
        return "";
    }

    @Override
    protected void setListeners() {
        cancelBtn.setOnClickListener(this);
        confirmBtn.setOnClickListener(this);
    }

    @Override
    protected void loadParam() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            amount = extras.getString(EUIParamKeys.TRANS_AMOUNT.toString());
            currency = extras.getString(EUIParamKeys.CURRENCY.toString());
            fundingSource = extras.getString(EUIParamKeys.FUNDING_SOURCE.toString());
        }
        title = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
    }

    @Override
    public void onClickProtected(View v) {
        switch (v.getId()) {
            case R.id.confirm_btn:
                confirmAmount();
                break;
            case R.id.clear_btn:
                cancelTrans();
                break;
            default:
                break;
        }
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }

    public void confirmAmount() {
        finish(new ActionResult(TransResult.SUCC, null));
        return;
    }

    public void cancelTrans() {
        onKeyBackDown();
        return;
    }
}