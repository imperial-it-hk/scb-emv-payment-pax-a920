
package com.evp.pay.trans.action.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.utils.TickTimer;
import com.evp.payment.evpscb.R;

public class DccGetCardholderDecisionActivity extends BaseActivityWithTickForAction {

    private Button dccDomesticRateButton;
    private Button dccForeignRateButton;
    private String dccDomesticRate;
    private String dccForeignRate;
    private String title;
    private int tickTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tickTimer.start(tickTime);
    }

    @Override
    protected void loadParam() {
        title = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        dccDomesticRate = getIntent().getStringExtra(EUIParamKeys.DCC_DOMESTIC_AMOUNT.toString());
        dccForeignRate = getIntent().getStringExtra(EUIParamKeys.DCC_FOREIGN_AMOUNT.toString());
        tickTime = getIntent().getExtras().getInt(EUIParamKeys.TIKE_TIME.toString(), TickTimer.DEFAULT_TIMEOUT);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_dcc_cardholder_decide;
    }

    @Override
    protected String getTitleString() {
        return title;
    }

    @Override
    protected void initViews() {
        enableBackAction(true);
        dccDomesticRateButton = (Button) findViewById(R.id.buttonDccDomesticRate);
        dccDomesticRateButton.setText(dccDomesticRate);
        dccForeignRateButton = (Button) findViewById(R.id.buttonDccForeignRate);
        dccForeignRateButton.setText(dccForeignRate);
    }

    @Override
    protected void setListeners() {
        dccDomesticRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(new ActionResult(TransResult.SUCC, false));
            }
        });

        dccForeignRateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(new ActionResult(TransResult.SUCC, true));
            }
        });
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }
}
