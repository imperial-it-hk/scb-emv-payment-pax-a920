package com.evp.pay.trans.action;

import android.content.Context;
import android.content.Intent;

import com.evp.abl.core.AAction;
import com.evp.pay.trans.action.activity.RefundMenuActivity;

public class ActionShowRefundSelection extends AAction {
    private Context context;
    public ActionShowRefundSelection(ActionStartListener listener){
        super(listener);
    }

    public void setParam(Context context){
        this.context = context;
    }
    @Override
    protected void process() {
        Intent intent = new Intent(context, RefundMenuActivity.class);
        context.startActivity(intent);
    }
}
