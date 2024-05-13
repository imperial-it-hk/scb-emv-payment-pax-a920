package com.evp.pay.trans.action.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.config.ConfigUtils;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.utils.ViewUtils;
import com.evp.payment.evpscb.R;

import java.util.ArrayList;

public class SuspendTransDataActivity extends BaseActivityWithTickForAction {
    private Button payBtn;
    private Button deleteBtn;

    private String navTitle;
    private boolean navBack;

    private ArrayList<String> leftColumns = new ArrayList<>();
    private ArrayList<String> rightColumns = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_suspend_detail;
    }

    @Override
    protected String getTitleString() {
        return navTitle;
    }

    @Override
    protected void initViews() {
        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.root);
        constraintLayout.setBackgroundColor(secondaryColor);
        LinearLayout llDetailContainer = (LinearLayout) findViewById(R.id.detail_layout);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = 15;

        for (int i = 0; i < leftColumns.size(); i++) {
            LinearLayout layer = ViewUtils.genSingleLineLayout(SuspendTransDataActivity.this, leftColumns.get(i),
                    rightColumns.get(i));
            llDetailContainer.addView(layer, params);
        }

        payBtn = (Button) findViewById(R.id.pay_btn);
        payBtn.setText(ConfigUtils.getInstance().getString("buttonPay"));
        deleteBtn = (Button) findViewById(R.id.delete_btn);
        deleteBtn.setText(ConfigUtils.getInstance().getString("buttonDelete"));
    }

    @Override
    protected void setListeners() {
        enableBackAction(navBack);
        payBtn.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);
    }

    @Override
    public void onClickProtected(View v) {
        switch (v.getId()) {
            case R.id.pay_btn:
                finish(new ActionResult(TransResult.SUCC, "pay"));
                break;
            case R.id.delete_btn:
                finish(new ActionResult(TransResult.SUCC, "delete"));
                break;
            default:
                finish(new ActionResult(TransResult.ERR_ABORTED, null));
                break;
        }
    }

    @Override
    protected void loadParam() {
        Bundle bundle = getIntent().getExtras();
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        navBack = getIntent().getBooleanExtra(EUIParamKeys.NAV_BACK.toString(), false);
        leftColumns = bundle.getStringArrayList(EUIParamKeys.ARRAY_LIST_1.toString());
        rightColumns = bundle.getStringArrayList(EUIParamKeys.ARRAY_LIST_2.toString());
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }
}
