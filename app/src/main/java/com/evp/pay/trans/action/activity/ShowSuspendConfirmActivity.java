package com.evp.pay.trans.action.activity;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.config.ConfigUtils;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.payment.evpscb.R;

public class ShowSuspendConfirmActivity extends BaseActivityWithTickForAction {
    Button okBtn;
    Button cancelBtn;
    String title;
    String traceNo;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_suspend_confirm;
    }

    @Override
    protected void initViews() {
        TextView traceNoTextView = (TextView) findViewById(R.id.suspend_trace_no);
        traceNoTextView.setText(ConfigUtils.getInstance().getString("traceNoLabel") + ": " + traceNo);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.root);
        linearLayout.setBackgroundColor(secondaryColor);
        TextView textView3 = (TextView) findViewById(R.id.textView3);
        textView3.setText(ConfigUtils.getInstance().getString("buttonSuspendedQR"));

        okBtn = (Button) findViewById(R.id.confirm_delete_suspend_btn);
        okBtn.setText(ConfigUtils.getInstance().getString("buttonOk"));
        cancelBtn = (Button) findViewById(R.id.cancel_suspend_btn);
        cancelBtn.setText(ConfigUtils.getInstance().getString("buttonCancel"));
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }

    @Override
    public void onClickProtected(View v) {
        switch (v.getId()) {
            case R.id.cancel_suspend_btn:
                finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
                break;
            case R.id.confirm_delete_suspend_btn:
                finish(new ActionResult(TransResult.SUCC, null));
                break;
            default:
                finish(new ActionResult(TransResult.ERR_ABORTED, null));
                break;
        }
    }

    @Override
    protected void setListeners() {
        okBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
    }

    @Override
    protected void loadParam() {
        title = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        traceNo = getIntent().getStringExtra("trace_no");
    }

    @Override
    protected String getTitleString() {
        return title;
    }
}
