package com.evp.pay.trans.action;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.evp.abl.core.AAction;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.trans.action.activity.SelectPaymentChannelTypeActivity;

public class ActionSelectPaymentChannelType extends AAction {
    private Context context;
    private  String title;

    public ActionSelectPaymentChannelType(ActionStartListener listener){
        super(listener);
    }

    public void setParam(Context context, String title){
        this.context = context;
        this.title = title;
    }
    @Override
    protected void process() {
        Intent intent = new Intent(context, SelectPaymentChannelTypeActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }
}
