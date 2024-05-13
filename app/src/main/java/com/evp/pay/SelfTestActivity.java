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
 * 20190108  	         lixc                    Create
 * ===========================================================================================
 */
package com.evp.pay;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.commonlib.utils.LogUtils;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.trans.component.Component;
import com.evp.pay.trans.model.Controller;
import com.evp.update.Updater;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;

import java.util.Date;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;

/**
 * The type Self test activity.
 */
public class SelfTestActivity extends AppCompatActivity {

    private static final String TAG = "SelfTest";

    /**
     * The constant REQ_INITIALIZE.
     */
    public static final int REQ_INITIALIZE = 1;

    private Disposable disposable;
    private boolean isFirstRun = true;
    private TextView textView;

    /**
     * On self test.
     *
     * @param activity    the activity
     * @param requestCode the request code
     */
    public static void onSelfTest(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, SelfTestActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selftest_layout);
        textView = (TextView) findViewById(R.id.selfTest);

        boolean isInstalledNeptune = Component.neptuneInstalled(this, new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface arg0) {
                finishFailed();
            }
        });

        if (!isInstalledNeptune) {
            return;
        }

        FinancialApplication.getController().set(Controller.NEED_SET_WIZARD, Controller.Constant.YES);
        onCheckLog();

        isFirstRun = FinancialApplication.getController().isFirstRun();
        if (!isFirstRun) {
            onActivityResult(REQ_INITIALIZE, 0, null);
            return;
        }

        CheckPwdActivity.onCheckPwd(SelfTestActivity.this, REQ_INITIALIZE);
        FinancialApplication.getApp().runInBackground(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                Updater defaultValues = new Updater();
                defaultValues.loadNonEmvParameters();
                defaultValues.loadEmvParameters();
                long end = System.currentTimeMillis();
                LogUtils.d("insertTime","=======SelfTestActivity==insertAcquirer+initEMVParam==插入时间time = "+String.valueOf(end-start)+"============");
                FinancialApplication.getApp().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(getString(R.string.selfTest_succ));
                    }
                });
            }
        });
    }

    private void onCheckLog() {
        if (FinancialApplication.getController().get(Controller.CLEAR_LOG) == Controller.Constant.YES
                && GreendaoHelper.getTransDataHelper().deleteAllTransData()
                && GreendaoHelper.getTransTotalHelper().deleteAll()) {
            FinancialApplication.getController().set(Controller.CLEAR_LOG, Controller.Constant.NO);
            FinancialApplication.getController().set(Controller.BATCH_UP_STATUS, Controller.Constant.WORKED);
            FinancialApplication.getController().set(Controller.SETTLE_STATUS, Controller.Constant.WORKED);
            FinancialApplication.getController().set(Controller.LAST_SETTLE_DATE, Utils.addDays(Utils.getStartOfDay(new Date()), -1).getTime());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        if (REQ_INITIALIZE == requestCode) {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE
                    , Manifest.permission.ACCESS_COARSE_LOCATION};
            disposable = Utils.callPermissions(SelfTestActivity.this, permissions, new Action() {
                @Override
                public void run() throws Exception {
                    LogUtils.e(TAG, "{run}");//执行顺序——2
                    finishOk();
                }
            }, getString(R.string.permission_rationale_storage));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void finishOk() {
        Intent intent = getIntent();
        setResult(RESULT_OK, intent);
        finish();
    }

    private void finishFailed() {
        Intent intent = getIntent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        super.onDestroy();
    }
}
