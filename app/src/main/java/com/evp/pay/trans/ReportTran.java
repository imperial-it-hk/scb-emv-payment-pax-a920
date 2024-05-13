package com.evp.pay.trans;

import android.content.Context;

import com.evp.abl.core.AAction;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.config.ConfigUtils;
import com.evp.pay.trans.action.ActionSaleMenu;
import com.evp.pay.trans.action.ActionSelectPaymentChannelType;
import com.evp.pay.trans.action.ActionSelectReportType;
import com.evp.pay.trans.model.PrintType;
import com.evp.pay.trans.task.PrintTask;

public class ReportTran extends BaseTrans {
    private String selectedPaymentChannel;
    private String reportType;


    public ReportTran(Context context, TransEndListener transListener) {
        super(context, ETransType.REPORT, transListener);
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        State state = State.valueOf(currentState);
        switch (state) {
            case CHANNEL_SELECTION_TYPE:
                afterSelectChannel(result);
                break;
            case SELECT_REPORT_TYPE:
                afterSelectReportType(result);
                break;
            case SELECT_PAYMENT_CHANNEL:
                afterSelectPaymentChannel(result);
                break;
            default:
                transEnd(result);
                break;
        }
    }

    @Override
    protected void bindStateOnAction() {
        ActionSaleMenu actionSaleMenu = new ActionSaleMenu(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionSaleMenu) action).setParam(getCurrentContext());
            }
        });
        bind(State.SELECT_PAYMENT_CHANNEL.toString(), actionSaleMenu, true);

        ActionSelectReportType actionSelectReportType = new ActionSelectReportType(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionSelectReportType) action).setParam(getCurrentContext());
            }
        });
        bind(State.SELECT_REPORT_TYPE.toString(), actionSelectReportType, true);

        ActionSelectPaymentChannelType actionSelectPaymentChannelType = new ActionSelectPaymentChannelType(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionSelectPaymentChannelType) action).setParam(getCurrentContext(), reportType == "SUMMARY" ? ConfigUtils.getInstance().getString("buttonSummaryReport") :  ConfigUtils.getInstance().getString("buttonAuditReport"));
            }
        });
        bind(State.CHANNEL_SELECTION_TYPE.toString(), actionSelectPaymentChannelType, true);

        PrintTask summaryPrintTask = new PrintTask(getCurrentContext(), transData, PrintTask.genTransEndListener(ReportTran.this, State.SUMMARY_REPORT_PRINT.toString()), PrintType.SUMMARY_REPORT, false);
        bind(State.SUMMARY_REPORT_PRINT.toString(), summaryPrintTask);

        PrintTask audiPrintTask = new PrintTask(getCurrentContext(), transData, PrintTask.genTransEndListener(ReportTran.this, State.AUDIT_REPORT_PRINT.toString()), PrintType.AUDIT_REPORT, false);
        bind(State.AUDIT_REPORT_PRINT.toString(), audiPrintTask);

        gotoState(State.SELECT_REPORT_TYPE.toString());
    }

    enum State {
        SELECT_REPORT_TYPE,
        CHANNEL_SELECTION_TYPE,
        SELECT_PAYMENT_CHANNEL,
        SUMMARY_REPORT_PRINT,
        AUDIT_REPORT_PRINT
    }

    private void afterSelectPaymentChannel(ActionResult result) {
        selectedPaymentChannel = result.getData().toString();
        if (reportType == "SUMMARY_REPORT") {
            gotoState(State.SUMMARY_REPORT_PRINT.toString());
        } else {
//            gotoState(State.AUDIT_PRINT.toString());
        }
    }

    private void afterSelectReportType(ActionResult result) {
        if (result.getRet() != TransResult.SUCC) {
            transEnd(result);
            return;
        }
        PrintType printType = (PrintType) result.getData();
        transData.setAcquirer((Acquirer)result.getData1());
        if (printType == PrintType.SUMMARY_REPORT) {
            gotoState(State.SUMMARY_REPORT_PRINT.toString());
        } else {
            gotoState(State.AUDIT_REPORT_PRINT.toString());
        }
    }

    private void afterSelectChannel(ActionResult result){
        if (result.getRet() != TransResult.SUCC) {
            transEnd(result);
        }
        if(result.getData().toString() == "ALL"){
            if(reportType == "SUMMARY"){
                gotoState(State.SUMMARY_REPORT_PRINT.toString());
            }else {
//                gotoState(State.AUDIT_PRINT.toString());
            }
        }else {
            gotoState(State.SELECT_PAYMENT_CHANNEL.toString());
        }
    }
}
