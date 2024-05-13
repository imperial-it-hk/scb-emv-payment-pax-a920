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
 * 20190108  	         Kim.L                   Create
 * ===========================================================================================
 */
package com.evp.pay.trans.task;

import static com.evp.pay.record.PrinterUtils.getVoucherNum;
import static com.evp.pay.trans.model.PrintType.RECEIPT;
import static com.evp.pay.trans.model.PrintType.RECEIPT_CUSTOMER;

import android.content.Context;

import com.evp.abl.core.AAction;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.trans.action.ActionInputTransData;
import com.evp.pay.trans.action.ActionPrintPreview;
import com.evp.pay.trans.action.ActionPrintReport;
import com.evp.pay.trans.action.ActionPrintTransReceipt;
import com.evp.pay.trans.action.ActionSendEmail;
import com.evp.pay.trans.action.ActionSendSMS;
import com.evp.pay.trans.action.activity.PrintPreviewActivity;
import com.evp.pay.trans.model.PrintType;
import com.evp.payment.evpscb.R;

import java.util.Objects;

/**
 * The type Print task.
 */
public class PrintTask extends BaseTask {
    private TransData transData;
    private PrintType printType;
    private boolean isReprint;

    /**
     * Instantiates a new Print task.
     *
     * @param context       the context
     * @param transData     the trans data
     * @param transListener the trans listener
     */
    public PrintTask(Context context, TransData transData, TransEndListener transListener, PrintType printType, boolean isReprint) {
        super(context, transListener);
        this.transData = transData;
        this.printType = printType;
        this.isReprint = isReprint;
    }

    public void setIsReprint(boolean isReprint) {
        this.isReprint = isReprint;
    }

    /**
     * Gen trans end listener trans end listener.
     *
     * @param task  the task
     * @param state the state
     * @return the trans end listener
     */
    public static TransEndListener genTransEndListener(final BaseTask task, final String state) {
        return new TransEndListener() {

            @Override
            public void onEnd(final ActionResult result) {
                FinancialApplication.getApp().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        task.onActionResult(state, result);
                    }
                });
            }
        };
    }

    /**
     * The enum State.
     */
    enum State {
        /**
         * Print preview state.
         */
        PRINT_PREVIEW,
        /**
         * Print ticket state.
         */
        PRINT_TICKET,
        /**
         * Enter phone num state.
         */
        ENTER_PHONE_NUM,
        /**
         * Enter email state.
         */
        ENTER_EMAIL,
        /**
         * Send sms state.
         */
        SEND_SMS,
        /**
         * Send email state.
         */
        SEND_EMAIL,
        /**
         * Print report state.
         */
        PRINT_REPORT
    }

    @Override
    protected void bindStateOnAction() {
        //print preview action
        ActionPrintPreview printPreviewAction = new ActionPrintPreview(
                new AAction.ActionStartListener() {

                    @Override
                    public void onStart(AAction action) {
                        ((ActionPrintPreview) action).setParam(getCurrentContext(), transData, printType, isReprint);
                    }
                });
        bind(State.PRINT_PREVIEW.toString(), printPreviewAction);

        // get Telephone num
        ActionInputTransData phoneAction = new ActionInputTransData(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionInputTransData) action).setParam(getCurrentContext(), getString(R.string.paperless)).setInputLine(
                        getString(R.string.prompt_phone_number), ActionInputTransData.EInputType.PHONE, 20, false);
            }
        });
        bind(State.ENTER_PHONE_NUM.toString(), phoneAction);

        // get email address
        ActionInputTransData emailAction = new ActionInputTransData(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionInputTransData) action).setParam(getCurrentContext(), getString(R.string.paperless))
                        .setInputLine(getString(R.string.prompt_email_address), ActionInputTransData.EInputType.EMAIL, 100, false);
            }
        });
        bind(State.ENTER_EMAIL.toString(), emailAction);

        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(
                new AAction.ActionStartListener() {

                    @Override
                    public void onStart(AAction action) {
                        ((ActionPrintTransReceipt) action).setParam(getCurrentContext(), transData, printType, isReprint);
                    }
                });
        bind(State.PRINT_TICKET.toString(), printTransReceiptAction, true);

        ActionPrintReport printReportAction = new ActionPrintReport(
                new AAction.ActionStartListener() {

                    @Override
                    public void onStart(AAction action) {
                        ((ActionPrintReport) action).setParam(getCurrentContext(), printType, transData);
                    }
                });
        bind(State.PRINT_REPORT.toString(), printReportAction, true);

        ActionSendSMS sendSMSAction = new ActionSendSMS(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {

                ((ActionSendSMS) action).setParam(getCurrentContext(), transData);
            }
        });
        bind(State.SEND_SMS.toString(), sendSMSAction);

        ActionSendEmail sendEmailAction = new ActionSendEmail(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionSendEmail) action).setParam(getCurrentContext(), transData);
            }
        });
        bind(State.SEND_EMAIL.toString(), sendEmailAction);

        gotoState(State.PRINT_PREVIEW.toString());

    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        State state = State.valueOf(currentState);
        switch (state) {
            case PRINT_PREVIEW:
                goPrintBranch(result);
                break;
            case ENTER_PHONE_NUM:
                onEnterSMSBranch(result);
                break;
            case ENTER_EMAIL:
                onEnterEmailBranch(result);
                break;
            case SEND_SMS:
            case SEND_EMAIL:
                onSendBranch(result);
                break;
            case PRINT_TICKET:
                if (printType == RECEIPT && getVoucherNum() > 1 && !transData.getTransType().equals(ETransType.OLS_ENQUIRY.name())) {
                    printType = RECEIPT_CUSTOMER;
                    gotoState(State.PRINT_PREVIEW.toString());
                    break;
                }
            case PRINT_REPORT:
            default:
                transEnd(result);
                break;
        }
    }

    private void onEnterSMSBranch(ActionResult result) {
        if (result.getRet() == TransResult.SUCC) {
            transData.setPhoneNum((String) result.getData());
            gotoState(State.SEND_SMS.toString());
        } else if (result.getRet() == TransResult.ERR_USER_CANCEL) {
            resetState(State.PRINT_PREVIEW.toString());
        } else {
            gotoState(State.PRINT_PREVIEW.toString());
        }
    }

    private void onEnterEmailBranch(ActionResult result) {
        if (result.getRet() == TransResult.SUCC) {
            transData.setEmail((String) result.getData());
            gotoState(State.SEND_EMAIL.toString());
        } else if (result.getRet() == TransResult.ERR_USER_CANCEL) {
            resetState(State.PRINT_PREVIEW.toString());
        } else {
            gotoState(State.PRINT_PREVIEW.toString());
        }
    }

    private void onSendBranch(ActionResult result) {
        if (result.getRet() == TransResult.SUCC) {
            // end trans
            transEnd(result);
        } else {
            ETransType eTransType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
            dispResult(Objects.requireNonNull(eTransType).getTransName(), result, null);
            gotoState(State.PRINT_PREVIEW.toString());
        }
    }

    private void goPrintBranch(ActionResult result) {
        if (result.getRet() != TransResult.SUCC) {
            // end trans
            transEnd(result);
            return;
        }
        String string = (String) result.getData();

        if (printType == PrintType.SUMMARY_REPORT || printType == PrintType.AUDIT_REPORT || printType == PrintType.LAST_SETTLEMENT) {
            //print report
            gotoState(State.PRINT_REPORT.toString());
        } else if (string != null && string.equals(PrintPreviewActivity.PRINT_BUTTON)) {
            //print ticket
            gotoState(State.PRINT_TICKET.toString());
        } else if (string != null && string.equals(PrintPreviewActivity.SMS_BUTTON)) {
            gotoState(State.ENTER_PHONE_NUM.toString());
        } else if (string != null && string.equals(PrintPreviewActivity.EMAIL_BUTTON)) {
            gotoState(State.ENTER_EMAIL.toString());
        } else {
            //end trans directly, not print
            transEnd(new ActionResult(TransResult.SUCC, transData));
        }
    }
}
