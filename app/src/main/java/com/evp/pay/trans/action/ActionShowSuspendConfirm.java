package com.evp.pay.trans.action;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.evp.abl.core.AAction;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.trans.action.activity.ShowSuspendConfirmActivity;

public class ActionShowSuspendConfirm extends AAction {
    private Context context;
    private String title;
    private String traceNo;

    public ActionShowSuspendConfirm(ActionStartListener listener) {
        super(listener);
    }

    public void setParam(Context context, String title, String traceNo) {
        this.context = context;
        this.title = title;
        this.traceNo = traceNo;
    }

    @Override
    protected void process() {
        Intent intent = new Intent(context, ShowSuspendConfirmActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
        bundle.putString("trace_no", traceNo);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }
}
