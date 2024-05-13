package com.evp.pay.trans.action.activity;

import android.view.View;
import android.widget.Button;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;


public class SelectItemActivity extends BaseActivityWithTickForAction {
    private String title = "";
    private String[] itemTexts;
    private static final int[] BUTTON_IDS = {
            R.id.item_0,R.id.item_1,R.id.item_2,R.id.item_3,R.id.item_4,R.id.item_5,R.id.item_6,R.id.item_7,R.id.item_8,R.id.item_9,
    };
    @Override
    protected int getLayoutId() {
        return R.layout.activity_select_item;
    }

    @Override
    protected void initViews() {
        for (int i = 0; i < itemTexts.length; i++) {
            Button button = (Button) findViewById(BUTTON_IDS[i]);
            if (primaryColor != -1)
                button.setBackgroundColor(primaryColor);
            button.setText(itemTexts[i]);
            button.setOnClickListener(this);
            button.setVisibility(View.VISIBLE);
        }
        if (secondaryColor != -1)
            findViewById(R.id.item_0).getRootView().setBackgroundColor(secondaryColor);
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }


    protected String getTitleString() {
        return title;
    }

    @Override
    protected void setListeners() {
    }

    @Override
    protected void loadParam() {
        title = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        itemTexts = getIntent().getStringArrayExtra(EUIParamKeys.IPP_OLS_TYPE.toString());
    }

    @Override
    public void onClickProtected(View v) {
        finish(new ActionResult(TransResult.SUCC, Utils.parseIntSafe(v.getTag().toString(), 0)));
    }
}
