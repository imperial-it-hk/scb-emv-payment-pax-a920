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

public class ActionVoid extends AAction {
    private TransProcessListener transProcessListenerImpl;
    private Context context;
    private TransData transData;
    private TransData originTransData;

    public ActionVoid(ActionStartListener listener) {
        super(listener);
    }

    public void setParam(Context context, TransData transData, TransData originTransData) {
        this.context = context;
        this.transData = transData;
        this.originTransData = originTransData;
    }

    @Override
    protected void process() {
        FinancialApplication.getApp().runInBackground(new Runnable() {
            @Override
            public void run() {
                transProcessListenerImpl = new TransProcessListenerImpl(context);
                transProcessListenerImpl.onShowProgress(ConfigUtils.getInstance().getString("processLabel"), 0);

                transData.setTransType(ETransType.VOID.name());

                Acquirer acquirer = transData.getAcquirer();

                int ret = TransResult.ERR_ABORTED;
                Object data = null;

                try {
                    switch (acquirer.getName()) {
                        case AppConstants.QR_ACQUIRER:
                            ArrayList<Object> result = null;
                            result = new TransDigio().perform(transData, transProcessListenerImpl);
                            ret = (int)result.get(0);
                            data = result.get(1);
                            break;
                    }
                } catch (PedDevException e) {
                    e.printStackTrace();
                }

                transProcessListenerImpl.onHideProgress();
                setResult(new ActionResult(ret, data));
            }
        });
    }
}
