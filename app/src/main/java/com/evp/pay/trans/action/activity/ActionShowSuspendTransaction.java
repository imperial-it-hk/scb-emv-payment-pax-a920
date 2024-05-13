package com.evp.pay.trans.action.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.evp.abl.core.AAction;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.EUIParamKeys;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class ActionShowSuspendTransaction extends AAction {
    private Context context;
    private Map<String, String> map;
    private String title;
    private String fundingSource;

    public ActionShowSuspendTransaction(ActionStartListener listener) {
        super(listener);
    }

    public void setParam(Context context, String title, Map<String, String> map, String fundingSource) {
        this.context = context;
        this.title = title;
        this.map = map;
        this.fundingSource = fundingSource;
    }

    @Override
    protected void process() {
        FinancialApplication.getApp().runOnUiThread(new ActionShowSuspendTransaction.ProcessRunnable(map));
    }

    private class ProcessRunnable implements Runnable {
        private ArrayList<String> leftColumns = new ArrayList<>();
        private ArrayList<String> rightColumns = new ArrayList<>();

        /**
         * Instantiates a new Process runnable.
         *
         * @param promptValue the prompt value
         */
        ProcessRunnable(Map<String, String> promptValue) {
            updateColumns(promptValue);
        }


        @Override
        public void run() {

            Bundle bundle = new Bundle();
            bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
            bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
            bundle.putStringArrayList(EUIParamKeys.ARRAY_LIST_1.toString(), leftColumns);
            bundle.putStringArrayList(EUIParamKeys.ARRAY_LIST_2.toString(), rightColumns);
            if (fundingSource != null) {
                bundle.putString("fundingSource", fundingSource);
            }

            Intent intent = new Intent(context, SuspendTransDataActivity.class);
            intent.putExtras(bundle);
            context.startActivity(intent);
        }

        private void updateColumns(Map<String, String> promptValue) {
            Set<Map.Entry<String, String>> entries = promptValue.entrySet();
            for (Map.Entry<String, String> next : entries) {
                leftColumns.add(next.getKey());
                String value = next.getValue();
                rightColumns.add(value == null ? "" : value);
            }
        }
    }
}
