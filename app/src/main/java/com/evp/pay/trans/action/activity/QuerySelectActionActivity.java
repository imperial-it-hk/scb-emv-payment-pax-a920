/*
 * ===========================================================================================
 * = COPYRIGHT
 *          PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or nondisclosure
 *   agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *   disclosed except in accordance with the terms in that agreement.
 *     Copyright (C) 2019-? PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.
 * Description: // Detail description about the function of this module,
 *             // interfaces with the other modules, and dependencies.
 * Revision History:
 * Date                  Author	                 Action
 * 20190108  	         huangwp                 Create
 * ===========================================================================================
 */
package com.evp.pay.trans.action.activity;

import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Button;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.config.ConfigUtils;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.payment.evpscb.R;

/**
 * The type User agreement activity.
 */
public class QuerySelectActionActivity extends BaseActivityWithTickForAction implements View.OnClickListener {
    private Button btnLastTransaction;
    private Button btnSuspendedTransaction;
    private Button btnAnyTransaction;
    private Button btnPullSlip;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_query_select_action_layout;
    }

    @Override
    protected void loadParam() {
        //do nothing
    }

    @Override
    protected String getTitleString() {
        return ConfigUtils.getInstance().getString("queryLabel");
    }

    @Override
    protected void initViews() {
        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.root);
        constraintLayout.setBackgroundColor(secondaryColor);
        btnLastTransaction = (Button) findViewById(R.id.last_transaction_btn);
        btnSuspendedTransaction = (Button) findViewById(R.id.suspended_transaction_btn);
        btnAnyTransaction = (Button) findViewById(R.id.any_transaction_btn);
        btnPullSlip = (Button) findViewById(R.id.pull_slip_btn);

        if (primaryColor != -1) {
            btnLastTransaction.setBackground(new ColorDrawable(primaryColor));
            btnSuspendedTransaction.setBackground(new ColorDrawable(primaryColor));
            btnAnyTransaction.setBackground(new ColorDrawable(primaryColor));
            btnPullSlip.setBackground(new ColorDrawable(primaryColor));
        }

        btnLastTransaction.setText(ConfigUtils.getInstance().getString("buttonLastTransaction"));
        btnSuspendedTransaction.setText(ConfigUtils.getInstance().getString("buttonSuspendedQR"));
        btnAnyTransaction.setText(ConfigUtils.getInstance().getString("buttonAnyTransaction"));
        btnPullSlip.setText(ConfigUtils.getInstance().getString("buttonPullSlip"));
    }

    @Override
    protected void setListeners() {
        btnLastTransaction.setOnClickListener(this);
        btnSuspendedTransaction.setOnClickListener(this);
        btnAnyTransaction.setOnClickListener(this);
        btnPullSlip.setOnClickListener(this);
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }

    @Override
    public void onClickProtected(View v) {

        switch (v.getId()) {
            case R.id.last_transaction_btn:
                finish(new ActionResult(TransResult.SUCC, "LAST_TRANSACTION"));
                break;
            case R.id.suspended_transaction_btn:
                finish(new ActionResult(TransResult.SUCC, "SUSPENDED_TRANSACTION"));
                break;
            case R.id.any_transaction_btn:
                finish(new ActionResult(TransResult.SUCC, "ANY_TRANSACTION"));
                break;
            case R.id.pull_slip_btn:
                finish(new ActionResult(TransResult.SUCC, "PULL_SLIP"));
                break;
            default:
                finish(new ActionResult(TransResult.ERR_ABORTED, null));
                break;
        }
    }

}
