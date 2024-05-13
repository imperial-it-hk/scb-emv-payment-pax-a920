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

public class PrintSelectionActivity extends BaseActivityWithTickForAction {
    private Button btnLastTransaction;
    private Button btnLastSettlement;
    private Button btnAnyTransaction;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_print_selection;
    }

    @Override
    protected String getTitleString() {
        return ConfigUtils.getInstance().getString("printLabel");
    }

    @Override
    protected void initViews() {
        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.root);
        constraintLayout.setBackgroundColor(secondaryColor);

        btnLastTransaction = (Button) findViewById(R.id.last_transaction_btn);
        btnLastSettlement = (Button) findViewById(R.id.last_settlement_btn);
        btnAnyTransaction = (Button) findViewById(R.id.any_transaction_btn);

        if (primaryColor != -1) {
            btnLastTransaction.setBackground(new ColorDrawable(primaryColor));
            btnLastSettlement.setBackground(new ColorDrawable(primaryColor));
            btnAnyTransaction.setBackground(new ColorDrawable(primaryColor));
        }

        btnLastTransaction.setText(ConfigUtils.getInstance().getString("buttonLastTransaction"));
        btnAnyTransaction.setText(ConfigUtils.getInstance().getString("buttonAnyTransaction"));
        btnLastSettlement.setText(ConfigUtils.getInstance().getString("buttonLastSettlement"));
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }

    @Override
    protected void setListeners() {
        btnLastTransaction.setOnClickListener(this);
        btnLastSettlement.setOnClickListener(this);
        btnAnyTransaction.setOnClickListener(this);
    }

    @Override
    protected void loadParam() {

    }

    @Override
    public void onClickProtected(View v) {

        switch (v.getId()) {
            case R.id.last_transaction_btn:
                finish(new ActionResult(TransResult.SUCC, "LAST_TRANSACTION"));
                break;
            case R.id.last_settlement_btn:
                finish(new ActionResult(TransResult.SUCC, "LAST_SETTLEMENT"));
                break;
            case R.id.any_transaction_btn:
                finish(new ActionResult(TransResult.SUCC, "ANY_TRANSACTION"));
                break;
            default:
                break;
        }
    }
}
