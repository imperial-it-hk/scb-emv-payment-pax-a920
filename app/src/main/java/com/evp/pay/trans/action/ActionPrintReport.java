package com.evp.pay.trans.action;

import android.app.Activity;
import android.content.Context;

import com.evp.abl.core.AAction;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.AppConstants;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.record.PrinterUtils;
import com.evp.pay.trans.model.PrintType;

public class ActionPrintReport extends AAction {
    private Context context;
    private PrintType printType;
    private TransData transData;

    /**
     * derived classes must call super(listener) to set
     *
     * @param listener {@link ActionStartListener}
     */
    public ActionPrintReport(ActionStartListener listener) {
        super(listener);
    }

    /**
     * Sets param.
     *
     * @param context   the context
     * @param printType the print type
     */
    public void setParam(Context context, PrintType printType, TransData transData) {
        this.context = context;
        this.printType = printType;
        this.transData = transData;
    }

    /**
     * action process
     */
    @Override
    protected void process() {
        FinancialApplication.getApp().runInBackground(new Runnable() {

            @Override
            public void run() {
                if (printType == PrintType.LAST_SETTLEMENT) {
                    switch (transData.getAcquirer().getName()) {
                        case AppConstants.QR_ACQUIRER:
                            PrinterUtils.printLastSettlement((Activity) context, transData.getAcquirer());
                        default:
                            PrinterUtils.printLastTransTotal((Activity) context, transData.getAcquirer());
                    }
                } else if (printType == PrintType.SUMMARY_REPORT) {
                    PrinterUtils.printSummaryReport((Activity) context, transData.getAcquirer());
                } else {
                    PrinterUtils.printAuditReport((Activity) context, transData.getAcquirer());
                }
                setResult(new ActionResult(TransResult.SUCC, null));
            }
        });
    }

}

