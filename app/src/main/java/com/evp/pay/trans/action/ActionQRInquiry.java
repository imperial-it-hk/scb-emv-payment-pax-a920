package com.evp.pay.trans.action;

import android.content.Context;

import com.evp.abl.core.AAction;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.AppConstants;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.config.ConfigUtils;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.trans.transmit.TransDigio;
import com.evp.pay.trans.transmit.TransProcessListener;
import com.evp.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.dal.exceptions.PedDevException;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ActionQRInquiry extends AAction {
    private TransProcessListener transProcessListenerImpl;
    private Context context;
    private TransData transData;
    private int retryCount = 0;

    public ActionQRInquiry(ActionStartListener listener) {
        super(listener);
    }

    public void setParam(Context context, TransData transData) {
        this.context = context;
        this.transData = transData;
    }

    private void processOnce() {
        transProcessListenerImpl.onShowProgress(ConfigUtils.getInstance().getString("processLabel"), 60);

        transData.setTransType(ETransType.QR_INQUIRY.name());

        int ret = TransResult.ERR_ABORTED;
        Object data = null;

        Acquirer acquirer = transData.getAcquirer();
        switch (acquirer.getName()) {
            case AppConstants.QR_ACQUIRER:
                try {
                    ArrayList<Object> result = null;
                    result = new TransDigio().perform(transData, transProcessListenerImpl);
                    ret = (int) result.get(0);
                    data = result.get(1);
                } catch (PedDevException e) {
                    e.printStackTrace();
                }

                if (ret == TransResult.ERR_TRANS_NOT_FOUND) {
                    if (++retryCount < acquirer.getInquiryRetries()) {
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                processOnce();
                            }
                        }, acquirer.getInquiryTimeout() * 1000);
                        return;
                    }
                }
                break;
        }

        transProcessListenerImpl.onHideProgress();
        setResult(new ActionResult(ret, data));
    }

    @Override
    protected void process() {
        FinancialApplication.getApp().runInBackground(new Runnable() {
            @Override
            public void run() {
                transProcessListenerImpl = new TransProcessListenerImpl(context);

                processOnce();
            }
        });
    }
}
