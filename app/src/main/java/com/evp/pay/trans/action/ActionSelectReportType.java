package com.evp.pay.trans.action;

import android.content.Context;
import android.content.Intent;

import com.evp.abl.core.AAction;
import com.evp.pay.trans.action.activity.SelectReportActivity;

public class ActionSelectReportType extends AAction {
    private Context context;

    public ActionSelectReportType(ActionStartListener listener){
        super(listener);
    }

    public void setParam(Context context){
        this.context = context;
    }

    @Override
    protected void process() {
        Intent intent = new Intent(context, SelectReportActivity.class);
        context.startActivity(intent);
    }
}
