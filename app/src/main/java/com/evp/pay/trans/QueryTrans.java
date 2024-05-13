package com.evp.pay.trans;

import android.content.Context;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.AppConstants;
import com.evp.bizlib.card.PanUtils;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.config.ConfigUtils;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.trans.action.ActionDispTransDetail;
import com.evp.pay.trans.action.ActionInputTransData;
import com.evp.pay.trans.action.ActionPullSlip;
import com.evp.pay.trans.action.ActionQRCancelCSB;
import com.evp.pay.trans.action.ActionQRInquiry;
import com.evp.pay.trans.action.ActionQuerySelectAction;
import com.evp.pay.trans.action.ActionShowSuspendConfirm;
import com.evp.pay.trans.action.ActionShowSuspendQrTable;
import com.evp.pay.trans.action.activity.ActionShowSuspendTransaction;
import com.evp.pay.trans.component.Component;
import com.evp.pay.trans.model.PrintType;
import com.evp.pay.trans.task.PrintTask;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;

import java.util.LinkedHashMap;
import java.util.Objects;

public class QueryTrans extends BaseTrans {
    private String selectedAction;
    private TransData origTransData;
    private String selectedSuspendTraceNo;
    private PrintTask printTask;

    public QueryTrans(Context context, TransEndListener transListener) {
        super(context, ETransType.QR_INQUIRY, transListener);
    }

    @Override
    public void bindStateOnAction() {
        // Select Action
        ActionQuerySelectAction querySelectAction = new ActionQuerySelectAction(action -> ((ActionQuerySelectAction) action)
                .setParam(
                        getCurrentContext()
                )
        );
        bind(State.SELECT_ACTION.toString(), querySelectAction, true);

        ActionDispTransDetail dispTransDetail = new ActionDispTransDetail(action -> {
            ETransType eTransType = ConvertUtils.enumValue(ETransType.class, origTransData.getTransType());
            String transType = eTransType != null ? eTransType.getTransName() : "";
            String amount = CurrencyConverter.convert(Utils.parseLongSafe(origTransData.getAmount(), 0), transData.getCurrency());

            transData.setEnterMode(origTransData.getEnterMode());
            transData.setTrack2(origTransData.getTrack2());
            transData.setTrack3(origTransData.getTrack3());

            LinkedHashMap<String, String> transDetails = new LinkedHashMap<>();
            String formattedDate = ConvertUtils.convert(origTransData.getDateTime(), Constants.TIME_PATTERN_TRANS,
                    Constants.TIME_PATTERN_DISPLAY);
            transDetails.put(ConfigUtils.getInstance().getString("transTypeLabel"), transType);
            if ((origTransData.getEnterMode() != null && origTransData.getEnterMode().equals(TransData.EnterMode.QR)) || origTransData.getFundingSource() != null) {
                transDetails.put(ConfigUtils.getInstance().getString("walletLabel"), ConfigUtils.getInstance().getWalletName(origTransData.getFundingSource()));
            } else {
                if (origTransData.getIssuer() != null) {
                    transDetails.put(getString(R.string.history_detail_card_no), PanUtils.maskCardNo(origTransData.getPan(), origTransData.getIssuer().getPanMaskPattern()));
                }
                transDetails.put(getString(R.string.history_detail_auth_code), origTransData.getAuthCode());
                transDetails.put(getString(R.string.history_detail_ref_no), origTransData.getRefNo());
            }
            transDetails.put(ConfigUtils.getInstance().getString("amountLabel"), amount);
            transDetails.put(ConfigUtils.getInstance().getString("traceNoLabel"), Component.getPaddedNumber(origTransData.getTraceNo(), 6));
            transDetails.put(ConfigUtils.getInstance().getString("dateTimeLabel"), formattedDate);

            ((ActionDispTransDetail) action).setParam(
                    getCurrentContext(),
                    getTranDetailTitle(selectedAction),
                    transDetails,
                    origTransData.getFundingSource()
            );
        });
        bind(State.SHOW_TRANSACTION.toString(), dispTransDetail, true);

        ActionShowSuspendTransaction actionShowSuspendTransaction = new ActionShowSuspendTransaction(action -> {
            ETransType eTransType = ConvertUtils.enumValue(ETransType.class, origTransData.getTransType());
            String transType = eTransType != null ? eTransType.getTransName() : "";
            String amount = CurrencyConverter.convert(Utils.parseLongSafe(origTransData.getAmount(), 0), origTransData.getCurrency());

            transData.setEnterMode(origTransData.getEnterMode());
            transData.setTrack2(origTransData.getTrack2());
            transData.setTrack3(origTransData.getTrack3());

            String formattedDate = ConvertUtils.convert(origTransData.getDateTime(), Constants.TIME_PATTERN_TRANS,
                    Constants.TIME_PATTERN_DISPLAY);

            LinkedHashMap<String, String> transDetail = new LinkedHashMap<>();
            transDetail.put(ConfigUtils.getInstance().getString("transTypeLabel"), transType);
            transDetail.put(ConfigUtils.getInstance().getString("amountLabel"), amount);
            transDetail.put(ConfigUtils.getInstance().getString("walletLabel"), ConfigUtils.getInstance().getWalletName(origTransData.getFundingSource()));
            transDetail.put(ConfigUtils.getInstance().getString("traceNoLabel"), Component.getPaddedNumber(origTransData.getTraceNo(), 6));
            transDetail.put(ConfigUtils.getInstance().getString("dateTimeLabel"), formattedDate);

            ((ActionShowSuspendTransaction) action)
                    .setParam(
                            getCurrentContext(),
                            ConfigUtils.getInstance().getString("buttonSuspendedQR"),
                            transDetail,
                            origTransData.getFundingSource()
                    );
        });
        bind(State.SHOW_SUSPEND_TRANSACTION.toString(), actionShowSuspendTransaction, true);

        ActionShowSuspendQrTable actionShowSuspendQrTable = new ActionShowSuspendQrTable(action -> ((ActionShowSuspendQrTable) action)
                .setParam(
                        getCurrentContext(),
                        ConfigUtils.getInstance().getString("buttonSuspended")
                )
        );
        bind(State.SHOW_SUSPEND_LIST.toString(), actionShowSuspendQrTable, true);

        ActionInputTransData enterTransNoAction = new ActionInputTransData(action -> ((ActionInputTransData) action)
                .setParam(
                        getCurrentContext(),
                        getTranDetailTitle(selectedAction)
                )
                .setInputLine(
                        ConfigUtils.getInstance().getString("inputTraceLabel"),
                        ActionInputTransData.EInputType.NUM,
                        6,
                        false
                )
        );
        bind(State.ENTER_TRANSNO.toString(), enterTransNoAction, false);

        ActionInputTransData enterTransRefAction = new ActionInputTransData(action -> ((ActionInputTransData) action)
                .setParam(
                        getCurrentContext(),
                        ConfigUtils.getInstance().getString("buttonPullSlip"),
                        null,
                        180
                )
                .setInputLine(
                        ConfigUtils.getInstance().getString("enterTransRef"),
                        ActionInputTransData.EInputType.TEXT,
                        25,
                        25,
                        false
                )
        );
        bind(State.ENTER_TRANREF.toString(), enterTransRefAction, true);

        ActionInputTransData enterRef1Action = new ActionInputTransData(action -> ((ActionInputTransData) action)
                .setParam(
                        getCurrentContext(),
                        ConfigUtils.getInstance().getString("buttonPullSlip"),
                        null,
                        180
                )
                .setInputLine(
                        ConfigUtils.getInstance().getString("enterRef1"),
                        ActionInputTransData.EInputType.NUM,
                        20,
                        20,
                        false
                )
        );
        bind(State.ENTER_REF1.toString(), enterRef1Action, true);

        ActionInputTransData enterRef3Action = new ActionInputTransData(action -> ((ActionInputTransData) action)
                .setParam(
                        getCurrentContext(),
                        ConfigUtils.getInstance().getString("buttonPullSlip"),
                        null,
                        180
                )
                .setInputLine(
                        ConfigUtils.getInstance().getString("enterRef3"),
                        ActionInputTransData.EInputType.TEXT,
                        11,
                        11,
                        false
                )
        );
        bind(State.ENTER_REF3.toString(), enterRef3Action, true);

        // Pull slip
        ActionPullSlip actionPullSlip = new ActionPullSlip(action -> ((ActionPullSlip) action)
                .setParam(
                        getCurrentContext(),
                        transData
                )
        );
        bind(State.PULL_SLIP.toString(), actionPullSlip, true);

        printTask = new PrintTask(getCurrentContext(), transData, PrintTask.genTransEndListener(QueryTrans.this, State.PRINT.toString()), PrintType.RECEIPT, false);
        bind(State.PRINT.toString(), printTask);

        final ActionQRInquiry actionQRInquiry = new ActionQRInquiry(action -> ((ActionQRInquiry) action)
                .setParam(
                        getCurrentContext(),
                        transData
                )
        );
        bind(State.INQUIRY.toString(), actionQRInquiry, true);

        ActionShowSuspendConfirm actionShowSuspendConfirm = new ActionShowSuspendConfirm(action -> ((ActionShowSuspendConfirm) action)
                .setParam(
                        getCurrentContext(),
                        ConfigUtils.getInstance().getString("buttonSuspendedQR"),
                        selectedSuspendTraceNo
                )
        );
        bind(State.SHOW_DELETE_CONFIRMATION.toString(), actionShowSuspendConfirm, true);

        // Cancel QRCode
        ActionQRCancelCSB qrCancelCSBAction = new ActionQRCancelCSB(action -> ((ActionQRCancelCSB) action)
                .setParam(
                        getCurrentContext(),
                        transData
                )
        );
        bind(State.CANCEL_QR.toString(), qrCancelCSBAction, true);

        gotoState(State.SELECT_ACTION.toString());

    }

    /**
     * The enum State.
     */
    enum State {
        SELECT_ACTION,
        SHOW_TRANSACTION,
        PULL_SLIP,
        SHOW_SUSPEND_LIST,
        SHOW_SUSPEND_TRANSACTION,
        INQUIRY,
        SHOW_DELETE_CONFIRMATION,
        ENTER_TRANSNO,
        CANCEL_QR,
        ENTER_TRANREF,
        ENTER_REF1,
        ENTER_REF3,
        PRINT,
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        int ret = result.getRet();
        State state = State.valueOf(currentState);

        switch (state) {
            case SELECT_ACTION:
                afterSelectAction((String) result.getData());
                break;
            case SHOW_TRANSACTION:
                if (ret != TransResult.SUCC) {
                    transEnd(result);
                    return;
                }
                if (origTransData != null && origTransData.getTransState() != null) {
                    if (origTransData.getTransState() == TransData.ETransStatus.PENDING) {
                        gotoState(State.INQUIRY.toString());
                    } else {
                        gotoState(State.PRINT.toString());
                    }
                } else {
                    transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
                }
                break;
            case PULL_SLIP:
                afterPullSlip(result);
                break;
            case ENTER_TRANREF:
                onEnterTransRef(result);
                break;
            case ENTER_REF1:
                onEnterRef1(result);
                break;
            case ENTER_REF3:
                onEnterRef3(result);
                break;
            case SHOW_SUSPEND_LIST:
                afterShowSuspendList(result);
                break;
            case SHOW_SUSPEND_TRANSACTION:
                afterShowSuspendTransaction(result);
                break;
            case SHOW_DELETE_CONFIRMATION:
                afterShowDeleteConfirm(result);
                break;
            case INQUIRY:
                afterInquiry(result);
                break;
            case ENTER_TRANSNO:
                onEnterTraceNo(result);
                break;
            case CANCEL_QR:
                afterCancelCSB(result);
                break;
            case PRINT:
                if (result.getRet() == TransResult.SUCC || Utils.needBtPrint()) {
                    // end trans
                    transEnd(result);
                } else {
                    transEnd(new ActionResult(TransResult.SUCC, null));
                }
                return;
            default:
                transEnd(result);
                break;
        }
    }

    private void afterSelectAction(String selectedAction) {
        this.selectedAction = selectedAction;
        switch (selectedAction) {
            case "LAST_TRANSACTION":
                printTask.setIsReprint(true);
                TransData lastTransData = GreendaoHelper.getTransDataHelper().findLastQrTransData();
                if (lastTransData == null) {
                    transEnd(new ActionResult(TransResult.ERR_NO_TRANS, null));
                    return;
                }
                origTransData = lastTransData;

                copyOrigTransData();

                if (origTransData.getTransState() == TransData.ETransStatus.SUSPENDED) {
                    selectedSuspendTraceNo = Component.getPaddedNumber(origTransData.getTraceNo(), 6);
                    gotoState(State.SHOW_SUSPEND_TRANSACTION.toString());
                } else {
                    gotoState(State.SHOW_TRANSACTION.toString());
                }
                break;
            case "SUSPENDED_TRANSACTION":
                printTask.setIsReprint(false);
                gotoState(State.SHOW_SUSPEND_LIST.toString());
                break;
            case "ANY_TRANSACTION":
                printTask.setIsReprint(true);
                gotoState(State.ENTER_TRANSNO.toString());
                break;
            case "PULL_SLIP":
                printTask.setIsReprint(true);
                gotoState(State.ENTER_TRANREF.toString());
                break;
            default:
                transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
                break;
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
            TransData transData = GreendaoHelper.getTransDataHelper().findLastTransData(false);
            if (transData == null) {
                transEnd(new ActionResult(TransResult.ERR_NO_TRANS, null));
                return;
            }
            transNo = transData.getTraceNo();
        } else {
            transNo = Utils.parseLongSafe(content, -1);
        }
        validateOrigTransData(transNo, false);
    }

    private void onEnterTransRef(ActionResult result) {
        if (result.getRet() != TransResult.SUCC) {
            transEnd(result);
            return;
        }

        String transRef = (String) result.getData();
        transData.setAcquirer(FinancialApplication.getAcqManager().findAcquirer(AppConstants.QR_ACQUIRER));

        if (transRef != null) {
            transData.setTransType(ETransType.PULL_SLIP.name());
            transData.setFundingSource("promptpay");
            transData.setSendingBankCode("014");
            transData.setRefNo(transRef);
        } else {
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
            return;
        }

        gotoState(State.ENTER_REF1.toString());
    }

    private void onEnterRef1(ActionResult result) {
        if (result.getRet() != TransResult.SUCC) {
            transEnd(result);
            return;
        }

        String ref1 = (String) result.getData();
        if (ref1 != null) {
            transData.setBillPaymentRef1(ref1);
        } else {
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
            return;
        }
        gotoState(State.ENTER_REF3.toString());
    }

    private void onEnterRef3(ActionResult result) {
        if (result.getRet() != TransResult.SUCC) {
            transEnd(result);
            return;
        }
        String ref3 = (String) result.getData();
        if (ref3 == null) {
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
            return;
        } else {
            transData.setBillPaymentRef3(ref3);
        }
        gotoState(State.PULL_SLIP.toString());
    }

    // check original trans data
    private void validateOrigTransData(long origTransNo, boolean allowSuspended) {
        origTransData = GreendaoHelper.getTransDataHelper().findTransDataByTraceNo(origTransNo);
        if (origTransData == null
                || (!allowSuspended && origTransData.getTransState() == TransData.ETransStatus.SUSPENDED)
                || !origTransData.getEnterMode().equals(TransData.EnterMode.QR))
        {
            // trans not exist
            transEnd(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
            return;
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

        if (origTransData.getTransState() == TransData.ETransStatus.SUSPENDED) {
            gotoState(State.SHOW_SUSPEND_TRANSACTION.toString());
        } else {
            gotoState(State.SHOW_TRANSACTION.toString());
        }
    }

    // set original trans data
    private void copyOrigTransData() {
        if (origTransData == null) {
            return;
        }

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
        transData.setPayeeProxyId(origTransData.getPayeeProxyId());
        transData.setPayeeProxyType(origTransData.getPayeeProxyType());
        transData.setPayeeAccountNumber(origTransData.getPayeeAccountNumber());
        transData.setPayerProxyId(origTransData.getPayerProxyId());
        transData.setPayerProxyType(origTransData.getPayerProxyType());
        transData.setPayerAccountNumber(origTransData.getPayerAccountNumber());
        transData.setReceivingBankCode(origTransData.getReceivingBankCode());
        transData.setThaiQRTag(origTransData.getThaiQRTag());
        transData.setIsPullSlip(origTransData.getIsPullSlip());
        transData.setQrcsTraceNo(origTransData.getQrcsTraceNo());
        transData.setIsBSC(origTransData.getIsBSC());
    }

    private void afterShowSuspendList(ActionResult result) {
        selectedSuspendTraceNo = result.getData().toString();
        String content = selectedSuspendTraceNo;
        long transNo;
        transNo = Utils.parseLongSafe(content, -1);
        validateOrigTransData(transNo, true);
    }

    private String getTranDetailTitle(String selectedAction) {
        switch (selectedAction) {
            case "LAST_TRANSACTION":
                return getString(R.string.last_transaction);
            case "SUSPENDED_TRANSACTION":
                return getString(R.string.suspended_qr);
            case "ANY_TRANSACTION":
                return getString(R.string.any_transaction);
            default:
                return "";
        }
    }

    private void afterShowSuspendTransaction(ActionResult result) {
        if (result.getRet() != TransResult.SUCC || result.getData().toString() == "") {
            transEnd(new ActionResult(TransResult.ERR_HAS_VOIDED, null));
            return;
        }

        if ("pay".equals(result.getData().toString())) {
            gotoState(State.INQUIRY.toString());
        } else {
            gotoState(State.SHOW_DELETE_CONFIRMATION.toString());
        }
    }

    private void afterShowDeleteConfirm(ActionResult result) {
        if (result.getRet() != TransResult.SUCC) {
            transEnd(result);
            return;
        } else {
            gotoState(State.CANCEL_QR.toString());
        }
    }

    public void afterCancelCSB(ActionResult result) {
        int ret = result.getRet();
        Object data = result.getData();

        setTransType(ETransType.QR_CANCEL);

        if (ret == TransResult.SUCC) {
            if (!Component.isDemo()) {
                // Delete Suspend QR
                GreendaoHelper.getTransDataHelper().delete(origTransData);
            }
            transEnd(new ActionResult(TransResult.SUCC, null));
        } else {
            transEnd(result);
        }
    }

    private void afterPullSlip(ActionResult result) {
        if (Component.isDemo()) {
            transData.setAmount("10000");
            transData.setBillerId("EVP21551");
        }
        if (result.getRet() != TransResult.SUCC) {
            transEnd(result);
            return;
        }
        gotoState(State.PRINT.toString());
    }

    private void afterInquiry(ActionResult result) {
        if (result.getRet() == TransResult.SUCC) {
            if (Component.isDemo()) {
                origTransData.setTransState(TransData.ETransStatus.NORMAL);
            }
            if (result.getData() != null) {
                TransData inquiryTransData = (TransData) result.getData();

                // update origTransData
                if (inquiryTransData.getTransState() != null && inquiryTransData.getTransState() != origTransData.getTransState()) {
                    origTransData.setTransState(inquiryTransData.getTransState());
                }

                if (inquiryTransData.getAmountCNY() != null && !inquiryTransData.getAmountCNY().equals(origTransData.getAmountCNY())) {
                    origTransData.setAmountCNY(inquiryTransData.getAmountCNY());
                }

                if (inquiryTransData.getExchangeRate() != null && !inquiryTransData.getExchangeRate().equals(origTransData.getExchangeRate())) {
                    origTransData.setExchangeRate(inquiryTransData.getExchangeRate());
                }

                if (inquiryTransData.getTransactionId() != null && !inquiryTransData.getTransactionId().equals(origTransData.getTransactionId())) {
                    origTransData.setTransactionId(inquiryTransData.getTransactionId());
                }

                if (inquiryTransData.getBillerId() != null && !inquiryTransData.getBillerId().equals(origTransData.getBillerId())) {
                    origTransData.setBillerId(inquiryTransData.getBillerId());
                }

                if (inquiryTransData.getSendingBankCode() != null && !inquiryTransData.getSendingBankCode().equals(origTransData.getSendingBankCode())) {
                    origTransData.setSendingBankCode(inquiryTransData.getSendingBankCode());
                }

                if (inquiryTransData.getMerchantPan() != null && !inquiryTransData.getMerchantPan().equals(origTransData.getMerchantPan())) {
                    origTransData.setMerchantPan(inquiryTransData.getMerchantPan());
                }

                if (inquiryTransData.getConsumerPan() != null && !inquiryTransData.getConsumerPan().equals(origTransData.getConsumerPan())) {
                    origTransData.setConsumerPan(inquiryTransData.getConsumerPan());
                }

                if (inquiryTransData.getPaymentChannel() != null && !inquiryTransData.getPaymentChannel().equals(origTransData.getPaymentChannel())) {
                    origTransData.setPaymentChannel(inquiryTransData.getPaymentChannel());
                }

                if (inquiryTransData.getAuthCode() != null && !inquiryTransData.getAuthCode().equals(origTransData.getAuthCode())) {
                    origTransData.setAuthCode(inquiryTransData.getAuthCode());
                }

                GreendaoHelper.getTransDataHelper().update(origTransData);
            }

            if (origTransData.getTransState() == TransData.ETransStatus.SUSPENDED) {
                transEnd(new ActionResult(TransResult.SUCC, null));
                return;
            }

            gotoState(State.PRINT.toString());
        } else {
            transEnd(result);
            return;
        }
    }
}
