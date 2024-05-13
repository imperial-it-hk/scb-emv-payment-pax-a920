package com.evp.pay.trans.action;

import android.content.Context;
import android.content.Intent;

import com.evp.abl.core.AAction;
import com.evp.pay.trans.action.activity.PrintSelectionActivity;

public class PrintSelectionAction extends AAction {
    private Context context;

    public PrintSelectionAction(ActionStartListener listener) {
        super(listener);
    }

    public void setParam(Context context) {
        this.context = context;
    }

    @Override
    protected void process() {
        Intent intent = new Intent(context, PrintSelectionActivity.class);
        context.startActivity(intent);
    }
}
