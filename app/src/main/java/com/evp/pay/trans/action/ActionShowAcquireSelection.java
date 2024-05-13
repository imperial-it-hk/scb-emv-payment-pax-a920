package com.evp.pay.trans.action;

import android.content.Context;
import android.content.Intent;

import com.evp.abl.core.AAction;
import com.evp.pay.trans.action.activity.AcquirerSelectionMenuActivity;

public class ActionShowAcquireSelection extends AAction {
    private Context context;

    public ActionShowAcquireSelection(ActionStartListener listener) {
        super(listener);
    }

    public void setParams(Context context) {
        this.context = context;
    }

    @Override
    protected void process() {
        Intent intent = new Intent(context, AcquirerSelectionMenuActivity.class);
        context.startActivity(intent);
    }
}
