package com.evp.pay.trans.action.activity;

import android.view.View;
import android.widget.Button;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.payment.evpscb.R;

public class SelectPaymentChannelTypeActivity extends BaseActivityWithTickForAction {
    Button allBtn;
    Button selectBtn;
    String title;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_select_channel_type;
    }

    @Override
    protected void initViews() {
        allBtn = (Button) findViewById(R.id.all_channel);
        selectBtn = (Button) findViewById(R.id.select_channel);
    }

    @Override
    protected void setListeners() {
        allBtn.setOnClickListener(this);
        selectBtn.setOnClickListener(this);
    }

    @Override
    protected void loadParam() {
        title = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
    }

    protected String getTitleString() {
        return title;
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }

    @Override
    public void onClickProtected(View v) {
        switch (v.getId()){
            case R.id.all_channel:
                finish(new ActionResult(TransResult.SUCC, "ALL"));
                break;
            case R.id.select_channel:
                finish(new ActionResult(TransResult.SUCC, "SELECT"));
                break;
            default:
                finish(new ActionResult(TransResult.ERR_ABORTED, null));
                break;
        }
    }
}
