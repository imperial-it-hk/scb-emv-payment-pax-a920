package com.evp.pay.trans;

import android.content.Context;

import com.evp.abl.core.AAction;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.AppConstants;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.LogUtils;
import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.trans.action.ActionInputTransData;
import com.evp.pay.trans.action.ActionShowAcquireSelection;
import com.evp.pay.trans.action.PrintSelectionAction;
import com.evp.pay.trans.component.Component;
import com.evp.pay.trans.model.PrintType;
import com.evp.pay.trans.task.PrintTask;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;

import java.util.List;
import java.util.Objects;

public class PrintTrans extends BaseTrans {
    private String selectedAction;
    TransData origTransData;

    public PrintTrans(Context context, TransEndListener transListener) {
        super(context, ETransType.PRINT, transListener);
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        int ret = result.getRet();
        State state = State.valueOf(currentState);

        switch (state) {
            case ACTION_SELECT:
                if (result.getRet() != TransResult.SUCC) {
                    transEnd(result);
                    return;
                }
                afterSelectAction((String) result.getData());
                break;
            case ENTER_TRANSNO:
                onEnterTraceNo(result);
                break;
            case ACQUIRER_SELECTION:
                afterSelectAcquirer(result);
                break;
            case SETTLEMENT_PRINT:
            case PRINT:
                if (result.getRet() != TransResult.SUCC || Utils.needBtPrint()) {
                    // end trans
                    transEnd(result);
                } else {
                    transEnd(new ActionResult(TransResult.SUCC, null));
                }
                break;
            default:
                transEnd(result);
                break;
        }
    }

    @Override
    protected void bindStateOnAction() {
        PrintSelectionAction printSelectionAction = new PrintSelectionAction(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((PrintSelectionAction) action).setParam(getCurrentContext());
            }
        });
        bind(State.ACTION_SELECT.toString(), printSelectionAction, false);

        ActionInputTransData enterTransNoAction = new ActionInputTransData(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputTransData) action).setParam(getCurrentContext(), getTranDetailTitle(selectedAction))
                        .setInputLine(ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_PRINT), ActionInputTransData.EInputType.NUM, 6, false);
            }
        });
        bind(QueryTrans.State.ENTER_TRANSNO.toString(), enterTransNoAction, true);

        ActionShowAcquireSelection actionShowAcquireSelection = new ActionShowAcquireSelection(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionShowAcquireSelection) action).setParams(getCurrentContext());
            }
        });
        bind(State.ACQUIRER_SELECTION.toString(), actionShowAcquireSelection, true);

        PrintTask printTask = new PrintTask(getCurrentContext(), transData, PrintTask.genTransEndListener(PrintTrans.this, State.PRINT.toString()), PrintType.RECEIPT, true);
        bind(State.PRINT.toString(), printTask);

        PrintTask settlementPrintTask = new PrintTask(getCurrentContext(), transData, PrintTask.genTransEndListener(PrintTrans.this, State.SETTLEMENT_PRINT.toString()), PrintType.RECEIPT, false);
        bind(State.SETTLEMENT_PRINT.toString(), settlementPrintTask);

        PrintTask lastSettlementPrintTask = new PrintTask(getCurrentContext(), transData, PrintTask.genTransEndListener(PrintTrans.this, State.SETTLEMENT_PRINT.toString()), PrintType.LAST_SETTLEMENT, true);
        bind(State.LAST_SETTLEMENT_PRINT.toString(), lastSettlementPrintTask);

        gotoState(State.ACTION_SELECT.toString());
    }

    enum State {
        ACTION_SELECT,
        PRINT,
        SETTLEMENT_PRINT,
        ENTER_TRANSNO,
        LAST_SETTLEMENT_PRINT,
        ACQUIRER_SELECTION,
    }

    private void afterSelectAction(String selectedAction) {
        this.selectedAction = selectedAction;
        switch (selectedAction) {
            case "LAST_TRANSACTION":
                TransData lastTransData = GreendaoHelper.getTransDataHelper().findLastTransData();
                if (lastTransData == null) {
                    if (Component.isDemo()) {
                        origTransData = new TransData();
                        Component.transInit(origTransData);
                        origTransData.setTransType(ETransType.SALE.toString());
                        origTransData.setRefNo("12345678910");
                        origTransData.setFundingSource("alipay");
                        origTransData.setAmount("1234");
                        origTransData.setAmountCNY("1234");
                        origTransData.setExchangeRate("0.19");
                        origTransData.setPaymentId("412615100000052021130074940");
                        origTransData.setStanNo(1);
                        origTransData.setTraceNo(1);
                    }
                    else {
                        transEnd(new ActionResult(TransResult.ERR_NO_TRANS, null));
                        return;
                    }
                }
                origTransData = lastTransData;
                copyOrigTransData();
                gotoState(State.PRINT.toString());
                break;
            case "LAST_SETTLEMENT":
                gotoState(State.ACQUIRER_SELECTION.toString());
                break;
            case "ANY_TRANSACTION":
                gotoState(State.ENTER_TRANSNO.toString());
                break;
            default:
                transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
                break;
        }
    }

    private void copyOrigTransData() {
        if(origTransData == null) {
            LogUtils.e(TAG, "ERROR - origTransData is NULL!!!");
            return;
        }

        //card trans print origTrans directly
        if (origTransData.getFundingSource() == null || origTransData.getFundingSource().isEmpty()) {
            PrintTask printTask = new PrintTask(getCurrentContext(), origTransData, PrintTask.genTransEndListener(PrintTrans.this, State.PRINT.toString()), PrintType.RECEIPT, true);
            bind(State.PRINT.toString(), printTask);
            return;
        }

        if(origTransData.getAcquirer().getName().equals(AppConstants.QR_ACQUIRER)) {
            transData.setEnterMode(origTransData.getEnterMode());
            transData.setTraceNo(origTransData.getTraceNo());
            transData.setStanNo(origTransData.getStanNo());
            if (origTransData.getTransState() == TransData.ETransStatus.VOIDED) {
                transData.setTransType(ETransType.VOID.name());
            } else {
                transData.setTransType(origTransData.getTransType());
            }
            transData.setAmount(origTransData.getAmount());
            transData.setAmountCNY(origTransData.getAmountCNY());
            transData.setExchangeRate(origTransData.getExchangeRate());
            transData.setCurrencyCode(origTransData.getCurrencyCode());
            transData.setOrigBatchNo(origTransData.getBatchNo());
            transData.setBatchNo(origTransData.getBatchNo());
            transData.setOrigAuthCode(origTransData.getAuthCode());
            transData.setAuthCode(origTransData.getAuthCode());
            transData.setOrigRefNo(origTransData.getRefNo());
            transData.setRefNo(origTransData.getRefNo());
            transData.setOrigTransNo(origTransData.getTraceNo());
            transData.setPan(origTransData.getPan());
            transData.setExpDate(origTransData.getExpDate());
            transData.setAcquirer(origTransData.getAcquirer());
            transData.setPaymentId(origTransData.getPaymentId());
            transData.setTransState(origTransData.getTransState());
            transData.setFundingSource(origTransData.getFundingSource());
            transData.setDateTime(origTransData.getDateTime());
            transData.setOrigTransType(origTransData.getTransType());
            transData.setOrigDateTime(origTransData.getDateTime());
            transData.setSendingBankCode(origTransData.getSendingBankCode());
            transData.setMerchantPan(origTransData.getMerchantPan());
            transData.setConsumerPan(origTransData.getConsumerPan());
            transData.setPaymentChannel(origTransData.getPaymentChannel());
            transData.setQrCodeId(origTransData.getQrCodeId());
            transData.setTransactionId(origTransData.getTransactionId());
            transData.setBillPaymentRef1(origTransData.getBillPaymentRef1());
            transData.setBillPaymentRef2(origTransData.getBillPaymentRef2());
            transData.setBillPaymentRef3(origTransData.getBillPaymentRef3());
            transData.setQrcsTraceNo(origTransData.getQrcsTraceNo());
            transData.setIsBSC(origTransData.getIsBSC());
        } else {
            transData.setAmount(origTransData.getAmount());
            transData.setOrigBatchNo(origTransData.getBatchNo());
            transData.setOrigAuthCode(origTransData.getAuthCode());
            transData.setOrigRefNo(origTransData.getRefNo());
            transData.setOrigTransNo(origTransData.getTraceNo());
            transData.setPan(origTransData.getPan());
            transData.setExpDate(origTransData.getExpDate());
            transData.setAcquirer(origTransData.getAcquirer());
            transData.setIssuer(origTransData.getIssuer());
            transData.setOrigTransType(origTransData.getTransType());
            transData.setOrigDateTime(origTransData.getDateTime());
            transData.setEnterMode(origTransData.getEnterMode());
            transData.setEmvAppName(origTransData.getEmvAppName());
            transData.setAid(origTransData.getAid());
            transData.setEmvAppLabel(origTransData.getEmvAppLabel());
            transData.setTrack2(origTransData.getTrack2());
            transData.setTrack3(origTransData.getTrack3());
        }
    }

    private void onEnterTraceNo(ActionResult result) {
        if (result.getRet() != TransResult.SUCC) {
            transEnd(result);
            return;
        }
        String content = (String) result.getData();
        long transNo;
        if (content == null) {
            TransData transData = GreendaoHelper.getTransDataHelper().findLastTransData();
            if (transData == null && !Component.isDemo()) {
                transEnd(new ActionResult(TransResult.ERR_NO_TRANS, null));
                return;
            }
            transNo = transData.getTraceNo();
        } else {
            transNo = Utils.parseLongSafe(content, -1);
        }
        validateOrigTransData(transNo);
    }

    private void validateOrigTransData(long origTransNo) {
        origTransData = GreendaoHelper.getTransDataHelper().findTransDataByTraceNo(origTransNo);
        if (origTransData == null) {
            // trans not exist
            if (Component.isDemo()) {
                origTransData = new TransData();
                Component.transInit(origTransData);
                origTransData.setTransType(ETransType.SALE.toString());
                origTransData.setRefNo("12345678910");
                origTransData.setFundingSource("alipay");
                origTransData.setAmount("1234");
                origTransData.setAmountCNY("1234");
                origTransData.setExchangeRate("0.19");
                origTransData.setPaymentId("412615100000052021130074940");
                origTransData.setStanNo(1);
                origTransData.setTraceNo(1);
            } else {
                transEnd(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
                return;
            }
        }
        ETransType trType = ConvertUtils.enumValue(ETransType.class, origTransData.getTransType());

        // only sale and refund trans can be revoked
        // AET-101 AET-139
        boolean isOfflineSent = trType == ETransType.OFFLINE_SALE &&
                origTransData.getOfflineSendState() != null &&
                origTransData.getOfflineSendState() == TransData.OfflineStatus.OFFLINE_SENT;
        boolean isAdjustedNotSent = origTransData.getTransState() == TransData.ETransStatus.ADJUSTED &&
                origTransData.getOfflineSendState() != null &&
                origTransData.getOfflineSendState() == TransData.OfflineStatus.OFFLINE_NOT_SENT;
        if ((!Objects.requireNonNull(trType).isVoidAllowed() && !isOfflineSent) || isAdjustedNotSent) {
            transEnd(new ActionResult(TransResult.ERR_VOID_UNSUPPORTED, null));
            return;
        }


        copyOrigTransData();
        gotoState(QueryTrans.State.PRINT.toString());
    }

    private String getTranDetailTitle(String selectedAction) {
        switch (selectedAction) {
            case "LAST_TRANSACTION":
                return getString(R.string.last_transaction);
            case "ANY_TRANSACTION":
                return getString(R.string.any_transaction);
            default:
                return "";
        }
    }

    private void afterSelectAcquirer(ActionResult result) {
        if (result.getRet() != TransResult.SUCC) {
            transEnd(result);
            return;
        }
        String selectedAcquirerName = (String) result.getData();
        Acquirer selectedAcquirer = FinancialApplication.getAcqManager().findAcquirer(selectedAcquirerName);
        if (selectedAcquirer == null && selectedAcquirerName != "All") {
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
        }
        if (selectedAcquirerName == "All") {
            // All Acquire Settlement
            List<Acquirer> acquirerList = FinancialApplication.getAcqManager().findAllAcquirers();
            for (Acquirer acquirer : acquirerList) {

            }
        } else {
            transData.setAcquirer(selectedAcquirer);
            gotoState(State.LAST_SETTLEMENT_PRINT.toString());
        }
    }
}
