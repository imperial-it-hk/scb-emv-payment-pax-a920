package com.evp.pay.trans.action;

import android.content.Context;

import com.evp.abl.core.AAction;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.device.Device;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.trans.transmit.TransOnline;
import com.evp.pay.trans.transmit.TransProcessListener;
import com.evp.pay.trans.transmit.TransProcessListenerImpl;
import com.evp.pay.utils.RxUtils;
import com.evp.pay.utils.ToastUtils;
import com.evp.pay.utils.TransResultUtils;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.pax.dal.exceptions.PedDevException;

import java.util.List;

public class ActionEchoTest  extends AAction {
    private Context context;
    private String title;

    public ActionEchoTest(ActionStartListener listener) {
        super(listener);
    }

    public void setParam(Context context, String title) {
        this.context = context;
        this.title = title;
    }

    @Override
    protected void process() {
        if (FinancialApplication.getController().isFirstRun()) {
            ToastUtils.showMessage(R.string.wait_2_init_device);
            return;
        }
        RxUtils.runInBackgroud(() -> {
            boolean failure = false;
            Acquirer defaultAcq = FinancialApplication.getAcqManager().getCurAcq();
            List<Acquirer> allAcquirers = FinancialApplication.getAcqManager().findAllAcquirers();
            for (Acquirer acq : allAcquirers) {
                int ret = -1;
                TransProcessListener listener = new TransProcessListenerImpl(context);
                FinancialApplication.getAcqManager().setCurAcq(acq);
                try {
                    ret = new TransOnline().echo(listener);
                } catch (PedDevException e) {
                    listener.onShowErrMessage(e.getMessage(), Constants.FAILED_DIALOG_SHOW_TIME, false);
                }
                listener.onHideProgress();
                if (ret == TransResult.SUCC) {
                    Device.beepOk();
                    listener.onShowNormalMessage(
                            String.format("%s %s %s%s%s",
                                    acq.getName(),
                                    "-",
                                    title,
                                    System.getProperty("line.separator"),
                                    Utils.getString(R.string.dialog_trans_succ)),
                            Constants.SUCCESS_DIALOG_SHOW_TIME,
                            true);
                } else if (ret != TransResult.ERR_ABORTED && ret != TransResult.ERR_HOST_REJECT) {
                    failure = true;
                    listener.onShowErrMessage(
                            String.format("%s %s %s",
                                    acq.getName(),
                                    "-",
                                    TransResultUtils.getMessage(ret)),
                            Constants.FAILED_DIALOG_SHOW_TIME, true);
                }
            }
            FinancialApplication.getAcqManager().setCurAcq(defaultAcq);
            if(failure) {
                setResult(new ActionResult(TransResult.ERR_HOST_REJECT, null));
            } else {
                setResult(new ActionResult(TransResult.SUCC, null));
            }
        });
    }
}
