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
 * 20190108  	         xieYb                   Create
 * ===========================================================================================
 */

package com.evp.mvp.presenter;


import android.app.Activity;
import android.content.Context;
import android.os.ConditionVariable;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.AppConstants;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.SettledTransData;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.entity.TransTotal;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.config.ConfigUtils;
import com.evp.device.Device;
import com.evp.mvp.contract.SettleContract;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.record.PrinterUtils;
import com.evp.pay.trans.component.Component;
import com.evp.pay.trans.model.Controller;
import com.evp.pay.trans.transmit.TransDigio;
import com.evp.pay.trans.transmit.TransOnline;
import com.evp.pay.trans.transmit.TransProcessListener;
import com.evp.pay.trans.transmit.TransProcessListenerImpl;
import com.evp.pay.trans.transmit.Transmit;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.view.dialog.CustomAlertDialog;
import com.evp.view.dialog.DialogUtils;
import com.pax.dal.exceptions.PedDevException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The type Settle presenter.
 */
public class SettlePresenter extends SettleContract.Presenter {
    /**
     * The Select acqs.
     */
    public ArrayList<String> selectAcqs = new ArrayList();
    /**
     * The Acquirer.
     */
    public Acquirer acquirer;
    private TransTotal total = new TransTotal();
    //AET-41
    private String acquirerDef;
    private Context context;
    /**
     * The Cv.
     */
    final ConditionVariable cv = new ConditionVariable();

    /**
     * Instantiates a new Settle presenter.
     *
     * @param context the context
     */
    public SettlePresenter(Context context) {
        super(context);

        this.context = context;
    }

    @Override
    public void init(ArrayList<String> selectAcqs) {
        //AET-41
        acquirerDef = FinancialApplication.getAcqManager().getCurAcq().getName();
        this.selectAcqs = selectAcqs;
    }

    @Override
    public void doSettlement(boolean isThisAutoSettle) {
        FinancialApplication.getApp().runInBackground(new Runnable() {

            private String genPromptMsg(int cnt, int total) {
                return Utils.getString(R.string.settle_settled) + "[" + cnt + "/" + total + "]";
            }

            private int getSuspendedCount() {
                int suspendedCount = 0;
                List<TransData> transDataList = GreendaoHelper.getTransDataHelper().findAllTransData(acquirer, false, true, false);
                for (TransData temp : transDataList) {
                    if (temp.getTransState() == TransData.ETransStatus.SUSPENDED) {
                        ++suspendedCount;
                    }
                }
                return suspendedCount;
            }

            private void inquiry(final CustomAlertDialog alertDialog) {
                FinancialApplication.getApp().runInBackground(new Runnable() {
                    @Override
                    public void run() {
                        TransProcessListener transProcessListener = new TransProcessListenerImpl(context);
                        transProcessListener.onShowProgress(ConfigUtils.getInstance().getString("processLabel"), 0);

                        List<TransData> transDataList = GreendaoHelper.getTransDataHelper().findAllTransData(acquirer, false, true, false);
                        for (TransData suspendedTransData : transDataList) {
                            if (suspendedTransData.getTransState() == TransData.ETransStatus.SUSPENDED) {
                                suspendedTransData.setTransType(ETransType.QR_INQUIRY.name());
                                try {
                                    ArrayList<Object> result = new TransDigio().perform(suspendedTransData, transProcessListener);
                                } catch (PedDevException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        transProcessListener.onHideProgress();
                        alertDialog.dismiss();
                        settle();
                    }
                });
            }

            private void inquiryBeforeSettlement(final int suspendedCount) {
                FinancialApplication.getApp().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUtils.showConfirmDialog(context, ConfigUtils.getInstance().getString("messageInquirySuspendedQrLabel") + " ["+suspendedCount+" trans]", new CustomAlertDialog.OnCustomClickListener() {
                            @Override
                            public void onClick(CustomAlertDialog alertDialog) {
                                alertDialog.dismiss();

                                FinancialApplication.getApp().runInBackground(new Runnable() {
                                    @Override
                                    public void run() {
                                        settle();
                                    }
                                });
                            }
                        }, new CustomAlertDialog.OnCustomClickListener() {
                            @Override
                            public void onClick(CustomAlertDialog alertDialog) {
                                inquiry(alertDialog);
                            }
                        });
                    }
                });
            }

            private void settle() {
                TransProcessListener transProcessListenerImpl = new TransProcessListenerImpl(getContext());
                try {
                    int ret;
                    int cnt = 0;
                    for (final String i : selectAcqs) {
                        FinancialApplication.getApp().runOnUiThread(() -> setCurrAcquirerContent(i));
                        cv.close();
                        cv.block();

                        FinancialApplication.getController().set(Controller.SETTLE_STATUS, Controller.Constant.SETTLE);

                        if(acquirer.getName().equals(AppConstants.QR_ACQUIRER)) {
                            ret = new TransDigio().settle(total, transProcessListenerImpl);

                            List<TransData> transDataList = GreendaoHelper.getTransDataHelper().findAllPendingQrsSettlementSaleTransData();
                            for (TransData transDataItem : transDataList) {
                                SettledTransData settledTransDataTemp = new SettledTransData();

                                if (transDataItem.getIssuer() != null) {
                                    settledTransDataTemp.setIssuer(transDataItem.getIssuer());
                                }
                                settledTransDataTemp.setTraceNo(transDataItem.getTraceNo());
                                settledTransDataTemp.setStanNo(transDataItem.getStanNo());
                                settledTransDataTemp.setTransType(transDataItem.getTransType());
                                settledTransDataTemp.setAmount(transDataItem.getAmount());
                                settledTransDataTemp.setAmountCNY(transDataItem.getAmountCNY());
                                settledTransDataTemp.setExchangeRate(transDataItem.getExchangeRate());
                                settledTransDataTemp.setCurrencyCode(transDataItem.getCurrencyCode());
                                settledTransDataTemp.setOrigBatchNo(transDataItem.getBatchNo());
                                settledTransDataTemp.setBatchNo(transDataItem.getBatchNo());
                                settledTransDataTemp.setOrigAuthCode(transDataItem.getAuthCode());
                                settledTransDataTemp.setAuthCode(transDataItem.getAuthCode());
                                settledTransDataTemp.setOrigRefNo(transDataItem.getRefNo());
                                settledTransDataTemp.setRefNo(transDataItem.getRefNo());
                                settledTransDataTemp.setOrigTransNo(transDataItem.getTraceNo());
                                settledTransDataTemp.setPan(transDataItem.getPan());
                                settledTransDataTemp.setExpDate(transDataItem.getExpDate());
                                settledTransDataTemp.setAcquirer(transDataItem.getAcquirer());
                                settledTransDataTemp.setPaymentId(transDataItem.getPaymentId());
                                settledTransDataTemp.setFundingSource(transDataItem.getFundingSource());
                                settledTransDataTemp.setOrigTransType(transDataItem.getTransType());
                                settledTransDataTemp.setOrigDateTime(transDataItem.getDateTime());
                                settledTransDataTemp.setSendingBankCode(transDataItem.getSendingBankCode());
                                settledTransDataTemp.setMerchantPan(transDataItem.getMerchantPan());
                                settledTransDataTemp.setConsumerPan(transDataItem.getConsumerPan());
                                settledTransDataTemp.setPaymentChannel(transDataItem.getPaymentChannel());
                                settledTransDataTemp.setQrCodeId(transDataItem.getQrCodeId());
                                settledTransDataTemp.setTransactionId(transDataItem.getTransactionId());
                                settledTransDataTemp.setBillPaymentRef1(transDataItem.getBillPaymentRef1());
                                settledTransDataTemp.setBillPaymentRef2(transDataItem.getBillPaymentRef2());
                                settledTransDataTemp.setBillPaymentRef3(transDataItem.getBillPaymentRef3());
                                settledTransDataTemp.setReversalStatus(SettledTransData.ReversalStatus.NORMAL);
                                if (transDataItem.getDateTime() != null) {
                                    settledTransDataTemp.setDateTime(transDataItem.getDateTime());
                                } else if (transDataItem.getOrigDateTime() != null) {
                                    settledTransDataTemp.setDateTime(transDataItem.getDateTime());
                                } else {
                                    settledTransDataTemp.setDateTime(Device.getTime(Constants.TIME_PATTERN_TRANS));
                                }
                                settledTransDataTemp.setPayeeProxyId(transDataItem.getPayeeProxyId());
                                settledTransDataTemp.setPayeeProxyType(transDataItem.getPayeeProxyType());
                                settledTransDataTemp.setPayeeAccountNumber(transDataItem.getPayeeAccountNumber());
                                settledTransDataTemp.setPayerProxyId(transDataItem.getPayerProxyId());
                                settledTransDataTemp.setPayerProxyType(transDataItem.getPayerProxyType());
                                settledTransDataTemp.setPayerAccountNumber(transDataItem.getPayerAccountNumber());
                                settledTransDataTemp.setReceivingBankCode(transDataItem.getReceivingBankCode());
                                settledTransDataTemp.setThaiQRTag(transDataItem.getThaiQRTag());
                                settledTransDataTemp.setIsPullSlip(transDataItem.getIsPullSlip());
                                settledTransDataTemp.setQrcsTraceNo(transDataItem.getQrcsTraceNo());
                                settledTransDataTemp.setSaleType(transDataItem.getSaleType());
                                settledTransDataTemp.setIsBSC(transDataItem.getIsBSC());

                                GreendaoHelper.getSettledTransDataDbHelper().insert(settledTransDataTemp);
                            }
                        } else {
                            // 处理冲正
                            new Transmit().sendReversal(acquirer, transProcessListenerImpl); // AET-255
                            //check if zero total AET-75
                            if (total.isZero()) {
                                transProcessListenerImpl.onShowNormalMessage(String.format("%s%s%s", acquirer.getName(), System.getProperty("line.separator"), Utils.getString(R.string.err_no_trans)), Constants.SUCCESS_DIALOG_SHOW_TIME, true);
                                continue;
                            }
                            // 结算
                            ret = new TransOnline().settle(total, transProcessListenerImpl);
                            //reFetch total,because offline send may happen during settle
                            total = GreendaoHelper.getTransTotalHelper().calcTotal(acquirer);
                            transProcessListenerImpl.onHideProgress();
                        }

                        if (ret != TransResult.SUCC && ret != TransResult.SUCC_NOREQ_BATCH && ret != TransResult.ERR_NO_TRANS) {
                            proxyView.finish(new ActionResult(ret, null));
                            return;
                        }

                        ++cnt;
                        // 记上批总计，置清除交易记录标志
                        total.setAcquirer(acquirer);
                        total.setAcquirer_id(acquirer.getId());
                        total.setMerchantID(acquirer.getMerchantId());
                        total.setTerminalID(acquirer.getTerminalId());
                        total.setBatchNo(acquirer.getCurrBatchNo());
                        total.setDateTime(Device.getTime(Constants.TIME_PATTERN_TRANS));
                        total.setClosed(true);
                        GreendaoHelper.getTransTotalHelper().insert(total);

                        PrinterUtils.printSettle((Activity) getContext(), total);
                        if(!isThisAutoSettle) {
                            proxyView.printDetail(false);
                            proxyView.printDetail(true);
                        }

                        // 批上送结算,将批上送断点赋值为0
                        FinancialApplication.getController().set(Controller.BATCH_UP_STATUS, Controller.Constant.WORKED);
                        FinancialApplication.getController().set(Controller.SETTLE_STATUS, Controller.Constant.WORKED);
                        FinancialApplication.getController().set(Controller.LAST_SETTLE_DATE, Utils.getStartOfDay(new Date()).getTime());

                        // 清除交易流水
                        if (GreendaoHelper.getTransDataHelper().deleteAllTransData(FinancialApplication.getAcqManager().getCurAcq())) {
                            Component.incBatchNo();
                        }
                    }

                    //AET-256
                    transProcessListenerImpl.onShowNormalMessage(genPromptMsg(cnt, selectAcqs.size()), Constants.SUCCESS_DIALOG_SHOW_TIME, true);
                    proxyView.finish(new ActionResult(TransResult.SUCC, null));
                } catch (PedDevException e) {
                    transProcessListenerImpl.onShowErrMessage(e.getMessage(),
                            Constants.FAILED_DIALOG_SHOW_TIME, false);
                } finally {
                    //AET-41, AET-62, AET-280
                    FinancialApplication.getAcqManager().setCurAcq(FinancialApplication.getAcqManager().findAcquirer(acquirerDef));
                }
            }

            @Override
            public void run() {
                int suspendedCount = getSuspendedCount();
                if (suspendedCount > 0) {
                    inquiryBeforeSettlement(suspendedCount);
                } else {
                    settle();
                }
            }
        });
    }

    @Override
    public void setCurrAcquirerContent(String acquirerName) {
        acquirer = FinancialApplication.getAcqManager().findAcquirer(acquirerName);
        if (acquirer == null) {
            return;
        }
        ///set current acquirer,settle print need it
        FinancialApplication.getAcqManager().setCurAcq(acquirer);

        total = GreendaoHelper.getTransTotalHelper().calcTotal(acquirer);
        String saleAmt = CurrencyConverter.convert(total.getSaleTotalAmt());
        //AET-18
        String refundAmt = CurrencyConverter.convert(0 - total.getRefundTotalAmt());
        String voidSaleAmt = CurrencyConverter.convert(0 - total.getSaleVoidTotalAmt());
        String voidRefundAmt = CurrencyConverter.convert(total.getRefundVoidTotalAmt());
        String offlineAmt = CurrencyConverter.convert(total.getOfflineTotalAmt());

        proxyView.setCurrAcquirerContent(acquirerName, acquirer, saleAmt, refundAmt, voidSaleAmt, voidRefundAmt, offlineAmt, total);

        cv.open();
    }

}
