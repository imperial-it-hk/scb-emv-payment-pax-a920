
package com.evp.pay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.evp.abl.core.ATransaction;
import com.evp.abl.core.ActionResult;
import com.evp.invoke.InvokeConst;
import com.evp.invoke.InvokeReceiver;
import com.evp.invoke.InvokeSender;
import com.evp.pay.app.ActivityStack;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.trans.component.Component;
import com.evp.pay.utils.RxUtils;
import com.evp.view.UserGuideManager;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * splash activity
 */
public class SplashActivity extends AppCompatActivity {
    /**
     * The constant REQ_SELF_TEST.
     */
    public static final int REQ_SELF_TEST = 1;

    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isInstalledNeptune = Component.neptuneInstalled(this, dialog -> finish());
        if (!isInstalledNeptune) {
            return;
        }
        if (FinancialApplication.getController().isFirstRun()) {
            UserGuideManager.getInstance().setEnable(true);
            SelfTestActivity.onSelfTest(this, REQ_SELF_TEST);
        } else {
            startInvokeReceiver();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_SELF_TEST) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
                return;
            }
            startInvokeReceiver();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private final ATransaction.TransEndListener invokeTransEndListener = new ATransaction.TransEndListener() {

        @Override
        public void onEnd(ActionResult result) {
            InvokeSender.send(context);
            startActivity(new Intent(context, MainActivity.class));
            ActivityStack.getInstance().pop();
        }
    };

    private void startInvokeReceiver() {
        context = this;
        startActivity(new Intent(this, MainActivity.class));
        RxUtils.addDisposable(Observable.timer(InvokeConst.DEFAULT_RECEIVER_DELAY_IN_MS, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
            if(!InvokeReceiver.execTrans(getIntent(), SplashActivity.this, invokeTransEndListener)) {
                finishAffinity();
            }
        }));
        finish();
    }

    @Override
    protected void onDestroy() {
        RxUtils.release();
        super.onDestroy();
    }
}