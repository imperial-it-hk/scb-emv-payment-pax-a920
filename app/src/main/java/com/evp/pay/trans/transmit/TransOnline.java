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
 * 20190108  	         Steven.W                Create
 * ===========================================================================================
 */
package com.evp.pay.trans.transmit;

import static com.evp.commonlib.utils.ConvertUtils.asciiToBin;
import static com.evp.commonlib.utils.ConvertUtils.binToAscii;
import static com.evp.commonlib.utils.ConvertUtils.getPaddedString;
import static com.evp.pay.constant.Constants.LMIC_SPECIAL_PRODUCT;
import static com.evp.pay.constant.Constants.OLS_VERSION;

import com.evp.bizlib.AppConstants;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.CardBinBlack;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.entity.TransData.OfflineStatus;
import com.evp.bizlib.data.entity.TransTotal;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.bizlib.ped.PedHelper;
import com.evp.bizlib.tle.TleUtils;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.KeyUtils;
import com.evp.commonlib.utils.LogUtils;
import com.evp.device.Device;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.trans.RedeemTrans;
import com.evp.pay.trans.component.Component;
import com.evp.pay.trans.model.Controller;
import com.evp.pay.utils.ResponseCode;
import com.evp.pay.utils.TransResultUtils;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.settings.SysParam;
import com.pax.dal.entity.EPedKeyType;
import com.pax.dal.exceptions.PedDevException;

import java.util.ArrayList;
import java.util.List;

/**
 * 单独联机处理， 例如签到
 *
 * @author Steven.W
 */
public class TransOnline {
    private static final String TAG = "TransOnline";

    private Online online = new Online();

    /**
     * 检查应答码
     *
     * @return {@link TransResult}
     */
    private int checkRspCode(TransData transData, TransProcessListener listener) {
        if (!"00".equals(transData.getResponseCode())) {
            if (listener != null) {
                ResponseCode responseCode = FinancialApplication.getRspCode().parse(transData.getResponseCode());
                listener.onHideProgress();
                listener.onShowErrMessage(responseCode.getMessage(),
                        Constants.FAILED_DIALOG_SHOW_TIME, false);
            }
            return TransResult.ERR_HOST_REJECT;
        }
        return TransResult.SUCC;
    }

    /**
     * 保存黑名单
     *
     * @param blackList the black list array
     */
    @SuppressWarnings("unused")
    private void writeBlack(byte[] blackList) {
        if (blackList == null) {
            return;
        }
        int loc = 0;
        while (loc < blackList.length) {
            int len = Integer.parseInt(new String(new byte[]{blackList[loc], blackList[loc + 1]}));
            byte[] cardNo = new byte[len];
            if (len + loc + 2 > blackList.length) {
                return;
            }
            System.arraycopy(blackList, loc + 2, cardNo, 0, len);
            CardBinBlack cardBinBlack = new CardBinBlack();
            cardBinBlack.setBin(new String(cardNo));
            cardBinBlack.setCardNoLen(cardNo.length);
            GreendaoHelper.getCardBinBlackHelper().insert(cardBinBlack);
            loc += 2 + len;
        }
    }

    /**
     * 结算
     *
     * @param total    the total
     * @param listener the listener
     * @return {@link TransResult}
     * @throws PedDevException the ped dev exception
     */
    public int settle(TransTotal total, TransProcessListener listener) throws PedDevException {
        Transmit transmit = new Transmit();

        //Send pending offline transactions
        int ret = transmit.sendOfflineTrans(listener);
        if (ret != TransResult.SUCC) {
            return ret;
        }

        //Send pending reversals
        ret = transmit.sendReversal(total.getAcquirer(), listener);
        if (ret == TransResult.ERR_ABORTED) {
            return ret;
        }

        //Settlement
        ret = settleRequest(total, listener);
        if (ret != TransResult.SUCC) {
            if (listener != null) {
                listener.onHideProgress();
            }
            return ret;
        }

        //Batch upload if required
        ret = batchUp(total,listener);
        if (ret != TransResult.SUCC) {
            if (listener != null) {
                listener.onHideProgress();
            }
            return ret;
        }

        return TransResult.SUCC;
    }


    private byte[] settleRequestF63(TransTotal total) {
        //region OLS
        if (total.getAcquirer().getName().equals(AppConstants.OLS_ACQUIRER))
            return RedeemTrans.Companion.settleField63(total.getAcquirer());
        //endregion

        String saleAmt;
        String saleNum;
        String refundAmt;
        String refundNum;

        String buf;
        saleNum = Component.getPaddedNumber(total.getSaleTotalNum(), 3);
        saleAmt = Component.getPaddedNumber(total.getSaleTotalAmt(), 12);
        refundNum = Component.getPaddedNumber(total.getRefundTotalNum() + total.getVoidTotalNum(), 3);
        refundAmt = Component.getPaddedNumber(total.getRefundTotalAmt() + total.getVoidTotalAmt(), 12);

        buf = saleNum + saleAmt + refundNum + refundAmt;
        buf += "000000000000000000000000000000000000000000000000000000000000";
        return buf.getBytes();
    }

    /**
     * 结算请求
     *
     * @return {@link TransResult}
     */
    private int settleRequest(TransTotal total, TransProcessListener listener) throws PedDevException {

        TransData transData = Component.transInit();
        transData.setTransType(ETransType.SETTLE.name());
        transData.setProcCode(ETransType.SETTLE.getProcCode());
        if (listener != null) {
            listener.onUpdateProgressTitle(ETransType.SETTLE.getTransName());
        }

        transData.setField63(settleRequestF63(total));

        int ret = online.online(transData, listener);
        if (listener != null) {
            listener.onHideProgress();
        }
        if (ret != TransResult.SUCC) {
            return ret;
        }
        ResponseCode responseCode = FinancialApplication.getRspCode().parse(transData.getResponseCode());

        //region OLS
        //convert OLS total miss match code (R5) to normal miss match code
        if (total.getAcquirer().getName().equals(AppConstants.OLS_ACQUIRER)) {
            if (responseCode.getCode().equals("R5"))
                responseCode.setCode("95");
        }
        //endregion

        //AET-31
        if (!"95".equals(responseCode.getCode())) {
            if ("00".equals(responseCode.getCode())) {
                return TransResult.SUCC_NOREQ_BATCH;
            }
            if (listener != null) {
                listener.onShowErrMessage(responseCode.getMessage(), Constants.FAILED_DIALOG_SHOW_TIME, false);
            }
            return TransResult.ERR_HOST_REJECT;
        }

        FinancialApplication.getController().set(Controller.BATCH_UP_STATUS, Controller.Constant.BATCH_UP);
        FinancialApplication.getController().set(Controller.BATCH_NUM, 0);

        return TransResult.SUCC;
    }

    /**
     * 批上送
     *
     * @return {@link TransResult}
     */
    private int batchUp(TransTotal total, TransProcessListener listener) throws PedDevException {
        int ret;
        if (listener != null) {
            listener.onUpdateProgressTitle(ETransType.BATCH_UP.getTransName());
        }
        // 获取交易记录条数
        long cnt = GreendaoHelper.getTransDataHelper().countOf();
        if (cnt <= 0) {
            FinancialApplication.getController().set(Controller.BATCH_UP_STATUS, Controller.Constant.WORKED);
            return TransResult.ERR_NO_TRANS;
        }
        // 获取交易重复次数
        int resendTimes = SysParam.getInstance().getInt(R.string.COMM_REDIAL_TIMES);
        int sendCnt = 0;
        final boolean[] left = new boolean[]{false};
        while (sendCnt < resendTimes + 1) {
            // 1)(对账平不送)全部磁条卡离线类交易，包括结算调整
            // 2)(对账平不送)基于PBOC标准的借/贷记IC卡脱机消费(含小额支付)成功交易
            // 3)(不存在)基于PBOC标准的电子钱包IC卡脱机消费成功交易 --- 不存在

            // 4)(对账平不送)全部磁条卡的请求类联机成功交易明细
            ret = new AllMagCardTransBatch(listener, new BatchUpListener() {

                @Override
                public void onLeftResult(boolean l) {
                    left[0] = l;
                }
            }).process();
            if (ret != TransResult.SUCC) {
                return ret;
            }
            // 5)(对账平不送)磁条卡和基于PBOC借/贷记标准IC卡的通知类交易明细，包括退货和预授权完成(通知)交易
            // 6)(对账平也送)为了上送基于PBOC标准的借/贷记IC卡成功交易产生的TC值，所有成功的IC卡借贷记联机交易明细全部重新上送
            // 7)(对账平也送)为了让发卡方了解基于PBOC标准的借/贷记IC卡脱机消费(含小额支付)交易的全部情况，上送所有失败的脱机消费交易明细
            // 8)(对账平也送)为了让发卡方防范基于PBOC标准的借/贷记IC卡风险交易，上送所有ARPC错但卡片仍然承兑的IC卡借贷记联机交易明细
            // 9)(不存在)为了上送基于PBOC标准的电子钱包IC卡成功圈存交易产生的TAC值，上送所有圈存确认的交易明细
            if (!left[0]) {
                break;
            }
            left[0] = false;
            sendCnt++;
        }
        // 10)(对账平也送)最后需上送批上送结束报文
        ret = batchUpEnd(total, listener);
        if (ret != TransResult.SUCC) {
            return ret;
        }

        //Renew TLE TWK keys for all acqurers
        Acquirer defaultAcq = FinancialApplication.getAcqManager().getCurAcq();
        Acquirer acq = total.getAcquirer();

        if(acq.getTleEnabled()) {
            FinancialApplication.getAcqManager().setCurAcq(acq);
            ret = tleKeysDownload(listener, true);
            if (ret != TransResult.SUCC) {
                FinancialApplication.getAcqManager().setCurAcq(defaultAcq);
            } else {
                Device.beepOk();
                listener.onShowNormalMessage(acq.getName()
                                + " - "
                                + Utils.getString(R.string.acq_tle_keys_download)
                                + System.getProperty("line.separator")
                                + Utils.getString(R.string.dialog_trans_succ),
                        Constants.SUCCESS_DIALOG_SHOW_TIME,
                        true);
            }
        }
        FinancialApplication.getAcqManager().setCurAcq(defaultAcq);

        return TransResult.SUCC;
    }

    /**
     * 结算结束
     *
     * @return {@link TransResult}
     */
    private int batchUpEnd(TransTotal total, TransProcessListener listener) throws PedDevException {
        if (listener != null) {
            listener.onUpdateProgressTitle(ETransType.SETTLE_END.getTransName());
        }
        TransData transData = Component.transInit();
        transData.setField63(settleRequestF63(total));

        transData.setTransType(ETransType.SETTLE_END.name());
        transData.setProcCode(ETransType.SETTLE_END.getProcCode());
        transData.setBatchNo(total.getAcquirer().getCurrBatchNo());
        return online.online(transData, listener);
    }

    /**
     * The interface Batch up listener.
     */
    interface BatchUpListener {
        /**
         * On left result.
         *
         * @param left the left
         */
        void onLeftResult(boolean left);
    }

    /**
     * 全部磁条卡的请求类联机成功交易明细上送
     */
    private class AllMagCardTransBatch {
        private final TransProcessListener listener;
        private final BatchUpListener batchUpListener;
        private boolean isFirst = true;
        private int ret = TransResult.SUCC;

        /**
         * Instantiates a new All mag card trans batch.
         *
         * @param listener        the listener
         * @param batchUpListener the batch up listener
         */
        AllMagCardTransBatch(TransProcessListener listener,
                             BatchUpListener batchUpListener) {
            this.listener = listener;
            this.batchUpListener = batchUpListener;
        }

        //region OLS
        private byte[] uploadRedeemF63(TransData transLog) {
            LogUtils.d("datetime " + transLog.getDateTime());
            String cludPoolId = transLog.getClubPoolId() == null? "000000": getPaddedString(transLog.getClubPoolId(), 6);
            String redeemPts = getPaddedString(transLog.getRedeemPts() == null? "": transLog.getRedeemPts() , 12);
            String redeemAmt = getPaddedString(transLog.getRedeemAmt() == null? "": transLog.getRedeemAmt() , 12);
            String f63 = OLS_VERSION
                    .concat(binToAscii(LMIC_SPECIAL_PRODUCT.getBytes()))
                    .concat(binToAscii("000".getBytes()))
                    .concat(transLog.getDateTime().substring(8))
                    .concat(transLog.getDateTime().substring(0, 8))
                    .concat("01")
                    .concat(binToAscii(cludPoolId.getBytes()))
                    .concat(binToAscii("R".getBytes()))
                    .concat(binToAscii("C".getBytes()))
                    .concat(redeemPts)
                    .concat(redeemAmt)
                    .concat(getPaddedString("", 12))
                    .concat(getPaddedString("", 16));
            LogUtils.d("f63 " + f63);
            return asciiToBin(f63);
        }
        //endregion

        private TransData genTranData(TransData transLog) {
            if (transLog.getTransType().equals(ETransType.VOID.name()))
                return null;

            TransData transData = Component.transInit();

            transData.setProcCode(transLog.getProcCode());
            transData.setAmount(transLog.getAmount());
            transData.setDateTime(transLog.getDateTime());
            transData.setEnterMode(transLog.getEnterMode());
            transData.setNii(transLog.getNii());
            transData.setRefNo(transLog.getRefNo());
            transData.setResponseCode(transLog.getResponseCode());
            transData.setAuthCode(transLog.getAuthCode());
            transData.getAcquirer().setTerminalId(transLog.getAcquirer().getTerminalId());
            transData.getAcquirer().setMerchantId(transLog.getAcquirer().getMerchantId());
            transData.setTipAmount(transLog.getTipAmount());
            transData.setOrigTransNo(transLog.getTraceNo());
            transData.setOrigStanNo(transLog.getStanNo());
            transData.setPan(transLog.getPan());
            transData.setExpDate(transLog.getExpDate());
            transData.setCardSerialNo(transLog.getCardSerialNo());
            transData.setOrigTransType(transLog.getTransType());
            transData.setDccCurrencyCode(transLog.getDccCurrencyCode());
            transData.setDccExchangeRate(transLog.getDccExchangeRate());
            transData.setDccForeignAmount(transLog.getDccForeignAmount());
            
            if (transData.getTransState() == TransData.ETransStatus.VOIDED)
                transData.setAmount("0");

            transData.setTransType(ETransType.BATCH_UP.name());

            //region OLS
            if (transLog.getTransType().equals(ETransType.REDEEM.name()))
                transData.setOrigField63(uploadRedeemF63(transLog));
            //endregion

            return transData;
        }

        private boolean continueOnline(TransData transData) throws PedDevException {
            if (transData == null) {
                return true;
            }
            ret = online.online(transData, listener, isFirst, false);
            isFirst = false;
            if (ret != TransResult.SUCC) {
                if (ret != TransResult.ERR_RECV) {
                    return false;
                }
                batchUpListener.onLeftResult(true);// 批上送交易无应答时，终端应在本轮上送完毕后再重发，而非立即重发
            }
            return true;
        }

        /**
         * Process int.
         *
         * @return {@link TransResult}
         * @throws PedDevException the ped dev exception
         */
        int process() throws PedDevException {
            List<TransData> allTrans = GreendaoHelper.getTransDataHelper().findAllTransData(FinancialApplication.getAcqManager().getCurAcq(), true, false, false);
            if (allTrans.isEmpty()) {
                return TransResult.ERR_NO_TRANS;
            }
            int transCnt = allTrans.size();

            isFirst = true;
            for (int cnt = 0; cnt < transCnt; cnt++) {
                updateProgressTitle(cnt + 1, transCnt);
                if (!continueOnline(genTranData(allTrans.get(cnt)))) {
                    break;
                }
            }
            online.close();
            return ret;
        }

        private void updateProgressTitle(int cnt, int total) {
            if (listener != null) {
                listener.onUpdateProgressTitle(ETransType.BATCH_UP.getTransName() + "[" + cnt + "/" + total + "]");
            }
        }
    }

    /**
     * 脱机交易上送
     *
     * @param listener              isOnline 是否为下一笔联机交易
     * @return {@link TransResult}
     * @throws PedDevException the ped dev exception
     */
    public int offlineTransSend(TransProcessListener listener) throws PedDevException {
        final List<TransData.OfflineStatus> defFilter = new ArrayList<>();
        defFilter.add(TransData.OfflineStatus.OFFLINE_NOT_SENT);

        List<TransData> records = GreendaoHelper.getTransDataHelper().findOfflineTransData(defFilter);
        if (records.isEmpty()) {
            return TransResult.SUCC;
        }

        if (listener != null) {
            listener.onUpdateProgressTitle(ETransType.OFFLINE_SALE.getTransName());
        }

        int ret = new OfflineTransProc(1, records, listener).process();
        if (ret != TransResult.SUCC) {
            return ret;
        }

        return TransResult.SUCC;
    }

    /****************************************************************************
     * OfflineTransProc 离线交易上送处理
     ****************************************************************************/
    private class OfflineTransProc {
        private final int sendMaxTime;
        private final List<TransData> records;
        private final TransProcessListener listener;

        private int dupNum = 0;// 重发次数
        private boolean isLastTime = false;
        private int sendCount = 0;
        private TransData transData;
        private int result = TransResult.SUCC;

        /**
         * Instantiates a new Offline trans proc.
         *
         * @param sendMaxTime the send max time
         * @param records     the records
         * @param listener    the listener
         */
        OfflineTransProc(int sendMaxTime, List<TransData> records, TransProcessListener listener) {
            this.sendMaxTime = sendMaxTime;
            this.records = records;
            this.listener = listener;
        }

        private boolean isFilteredOfflineTran(TransData record) {
            // 跳过上送不成功的和应答码非"00"的交易
            return record.getOfflineSendState() == null || !record.getOfflineSendState().equals(OfflineStatus.OFFLINE_NOT_SENT);
        }

        private boolean uploadAll() throws PedDevException {
            for (TransData record : records) {
                if (isFilteredOfflineTran(record)) {
                    continue;
                }
                boolean isContinue = handleOnlineResult(record, uploadOne(record));
                if (!isContinue && result != TransResult.SUCC) {
                    return false;
                }
            }
            return true;
        }

        private int uploadOne(TransData record) throws PedDevException {
            sendCount++;
            if (listener != null) {
                listener.onUpdateProgressTitle(ETransType.OFFLINE_SALE.getTransName() + "[" + sendCount + "]");
            }
            transData = new TransData(record);
            transData.setTransType(ETransType.OFFLINE_SALE.name());
            transData.setProcCode(ETransType.OFFLINE_SALE.getProcCode());
            Component.transInit(transData);
            transData.setTraceNo(record.getTraceNo());
            return online.online(transData, listener);
        }

        private boolean handleOnlineResult(TransData record, int ret) {
            return ret != TransResult.SUCC ? handleOnlineFailedCase(record, ret)
                    : handleOnlineSuccCase(record);
        }

        private boolean handleOnlineFailedCase(TransData record, int ret) {
            if (ret == TransResult.ERR_CONNECT || ret == TransResult.ERR_SEND || ret == TransResult.ERR_PACK
                    || ret == TransResult.ERR_MAC) {
                // 如果是发送数据时发生错误(连接错、发送错、数据包错、接收失败、MAC错)，则直接退出，不进行重发
                showErrMsg(TransResultUtils.getMessage(ret));
                result = TransResult.ERR_ABORTED;
                return false;
            }

            // BCTC要求离线交易上送时，如果平台无应答要离线交易上送次数上送
            // 未达到上送次数，继续送， 如果已达到上送次数，但接收失败按失败处理，不再上送
            if (ret == TransResult.ERR_RECV && !isLastTime) {
                return true;
            }
            record.setOfflineSendState(OfflineStatus.OFFLINE_ERR_SEND);
            GreendaoHelper.getTransDataHelper().update(record);
            return false;
        }

        private boolean handleOnlineSuccCase(TransData record) {
            ResponseCode responseCode = FinancialApplication.getRspCode().parse(transData.getResponseCode());
            // 返回码失败处理
            if ("A0".equals(responseCode.getCode())) {
                showErrMsg(responseCode.getMessage());
                result = TransResult.ERR_ABORTED;
                return false;
            }
            if (!"00".equals(responseCode.getCode()) && !"94".equals(responseCode.getCode())) { //AET-28
                showErrMsg(responseCode.getMessage());
                record.setOfflineSendState(OfflineStatus.OFFLINE_ERR_RESP);
                GreendaoHelper.getTransDataHelper().update(record);
                return true;
            }

            record.setSettleDateTime(transData.getSettleDateTime() != null ? transData.getSettleDateTime() : "");
            record.setAuthCode(transData.getAuthCode() != null ? transData.getAuthCode() : "");
            record.setRefNo(transData.getRefNo());

            record.setAcqCode(transData.getAcqCode() != null ? transData.getAcqCode() : "");
            record.setIssuerCode(transData.getIssuerCode() != null ? transData.getIssuerCode() : "");

            record.setReserved(transData.getReserved() != null ? transData.getReserved() : "");

            record.setAuthCode(transData.getAuthCode());
            record.setOfflineSendState(OfflineStatus.OFFLINE_SENT);
            GreendaoHelper.getTransDataHelper().update(record);
            return false;
        }

        /**
         * Process int.
         *
         * @return the int
         * @throws PedDevException the ped dev exception
         */
        int process() throws PedDevException {
            while (dupNum < sendMaxTime + 1) {
                sendCount = 0;
                if (dupNum == sendMaxTime) {
                    isLastTime = true;
                }
                if (!uploadAll()) {
                    return result;
                }
                dupNum++;
            }
            if (listener != null) {
                listener.onHideProgress();
            }
            return TransResult.SUCC;
        }

        private void showErrMsg(String str) {
            if (listener != null) {
                listener.onShowErrMessage(str, Constants.FAILED_DIALOG_SHOW_TIME, false);
            }
        }
    }

    /**
     * 回响功能
     *
     * @param listener the listener
     * @return {@link TransResultUtils}
     * @throws PedDevException the ped dev exception
     */
    public int echo(TransProcessListener listener) throws PedDevException {
        TransData transData = Component.transInit();
        int ret;
        transData.setTransType(ETransType.ECHO.name());
        transData.setProcCode(ETransType.ECHO.getProcCode());
        if (listener != null) {
            listener.onUpdateProgressTitle(transData.getAcquirer().getName() + " - " + ETransType.ECHO.getTransName());
        }
        ret = online.online(transData, listener);
        if (ret != TransResult.SUCC) {
            if (listener != null) {
                listener.onHideProgress();
            }
            return ret;
        }
        ret = checkRspCode(transData, listener);
        return ret;
    }

    public int tleKeysDownload(TransProcessListener listener, boolean downloadOnlyTwk) throws PedDevException {
        TransData transData;
        int ret;

        if(!downloadOnlyTwk) {
            //TMK
            transData = Component.transInit();
            transData.setTransType(ETransType.TMK_DOWNLOAD.name());
            transData.setProcCode(ETransType.TMK_DOWNLOAD.getProcCode());
            LogUtils.i(TAG, "STARTED TLE TMK download for acquirer: " + transData.getAcquirer().getName());
            if (listener != null) {
                listener.onUpdateProgressTitle(transData.getAcquirer().getName() + " - " + ETransType.TMK_DOWNLOAD.getTransName());
            }
            ret = online.online(transData, listener);
            if (ret != TransResult.SUCC) {
                if (listener != null) {
                    listener.onHideProgress();
                }
                return ret;
            }
            ret = checkRspCode(transData, listener);
            if (ret != TransResult.SUCC) {
                if (listener != null) {
                    listener.onShowErrMessage(Utils.getString(R.string.err_tmk_download_failed), Constants.FAILED_DIALOG_SHOW_TIME, false);
                }
                return ret;
            }
            //Parse TMK from response
            if (TleUtils.getTleTmkFromField62(transData)) {
                try {
                    String setId = transData.getAcquirer().getTleKeySetId();
                    PedHelper.writeTMK(KeyUtils.getTmkIndex(setId), transData.getTmkKey());
                    PedHelper.writeCleanTDK(KeyUtils.getTsdkIndex(setId), transData.getTmkKey(), null);
                } catch (PedDevException e) {
                    LogUtils.e(TAG, e);
                    return TransResult.ERR_PARAM;
                }
            } else {
                LogUtils.e(TAG, "TMK parse failed");
                return TransResult.ERR_UNPACK;
            }
            GreendaoHelper.getAcquirerHelper().update(transData.getAcquirer());

            //Compare KCV for TMK
            byte[] tmkKcv = PedHelper.getKcv(EPedKeyType.TMK, KeyUtils.getTmkIndex(transData.getAcquirer().getTleKeySetId()));
            String tmkKcvStr = ConvertUtils.binToAscii(tmkKcv);
            String recievedTmkKcv = transData.getTmkKcv();
            LogUtils.i(TAG, "Injected TMK KCV: " + tmkKcvStr);
            LogUtils.i(TAG, "Received TMK KCV: " + recievedTmkKcv);
            if (!tmkKcvStr.contains(recievedTmkKcv)) {
                LogUtils.e(TAG, "TMK KCV ERROR!");
                return TransResult.ERR_TLE_KCV_MISMATCH;
            }
        }

        //TWK
        transData = Component.transInit();
        transData.setTransType(ETransType.TWK_DOWNLOAD.name());
        transData.setProcCode(ETransType.TWK_DOWNLOAD.getProcCode());
        LogUtils.i(TAG, "STARTED TLE TWK download for acquirer: " + transData.getAcquirer().getName());
        if (listener != null) {
            listener.onUpdateProgressTitle(transData.getAcquirer().getName() + " - " + ETransType.TWK_DOWNLOAD.getTransName());
        }
        ret = online.online(transData, listener);
        if (ret != TransResult.SUCC) {
            if (listener != null) {
                listener.onHideProgress();
            }
            return ret;
        }
        ret = checkRspCode(transData, listener);
        if (ret != TransResult.SUCC) {
            if (listener != null) {
                listener.onShowErrMessage(Utils.getString(R.string.err_twk_download_failed), Constants.FAILED_DIALOG_SHOW_TIME, false);
            }
            return ret;
        }
        String setId = transData.getAcquirer().getTleKeySetId();
        boolean pinKeyPresent = false;
        if (TleUtils.getTleTwkFromField62(transData)) {
            byte[] pinKey = transData.getPinKey();
            if(pinKey != null && pinKey.length > 0) {
                pinKeyPresent = true;
            }
            try {
                byte[] vector = new byte[8];
                byte[] cleanTdk = PedHelper.decrTDes(KeyUtils.getTsdkIndex(setId), transData.getDekKey(), vector);
                byte[] cleanTak = PedHelper.decrTDes(KeyUtils.getTsdkIndex(setId), transData.getMakKey(), vector);
                byte[] paxStyleTdk = PedHelper.encTDes(KeyUtils.getTsdkIndex(setId), cleanTdk);
                byte[] paxStyleTak = PedHelper.encTDes(KeyUtils.getTsdkIndex(setId), cleanTak);
                PedHelper.writeTDK(KeyUtils.getTmkIndex(setId), KeyUtils.getTdkIndex(setId), paxStyleTdk, null);
                PedHelper.writeTDK(KeyUtils.getTmkIndex(setId), KeyUtils.getTakIndex(setId), paxStyleTak, null);
                if(pinKeyPresent) {
                    byte[] cleanTpk = PedHelper.decrTDes(KeyUtils.getTsdkIndex(setId), pinKey, vector);
                    byte[] paxStyleTpk = PedHelper.encTDes(KeyUtils.getTsdkIndex(setId), cleanTpk);
                    PedHelper.writeTPK(KeyUtils.getTmkIndex(setId), KeyUtils.getTpkIndex(setId), paxStyleTpk, null);
                }
            } catch (PedDevException e) {
                LogUtils.e(TAG, e);
                return TransResult.ERR_PARAM;
            }
        } else {
            LogUtils.e(TAG, "TWK parse failed");
            return TransResult.ERR_UNPACK;
        }
        GreendaoHelper.getAcquirerHelper().update(transData.getAcquirer());

        //Compare KCV for TDK
        byte[] tdkKcv = PedHelper.getKcv(EPedKeyType.TDK, KeyUtils.getTdkIndex(setId));
        String tdkKcvStr = ConvertUtils.binToAscii(tdkKcv);
        String receivedTdkKcv = transData.getTdkKcv();
        LogUtils.i(TAG, "Injected TDK KCV: " + tdkKcvStr);
        LogUtils.i(TAG, "Received TDK KCV: " + receivedTdkKcv);
        if(!tdkKcvStr.contains(receivedTdkKcv)) {
            LogUtils.e(TAG, "TDK KCV ERROR!");
            return TransResult.ERR_TLE_KCV_MISMATCH;
        }

        //Compare KCV for TAK
        byte[] takKcv = PedHelper.getKcv(EPedKeyType.TDK, KeyUtils.getTakIndex(setId));
        String takKcvStr = ConvertUtils.binToAscii(takKcv);
        String receivedTakKcv = transData.getTakKcv();
        LogUtils.i(TAG, "Injected TAK KCV: " + takKcvStr);
        LogUtils.i(TAG, "Received TAK KCV: " + receivedTakKcv);
        if(!takKcvStr.contains(receivedTakKcv)) {
            LogUtils.e(TAG, "TAK KCV ERROR!");
            return TransResult.ERR_TLE_KCV_MISMATCH;
        }

        //Compare KCV for TPK
        if(pinKeyPresent) {
            byte[] tpkKcv = PedHelper.getKcv(EPedKeyType.TPK, KeyUtils.getTpkIndex(setId));
            String tpkKcvStr = ConvertUtils.binToAscii(tpkKcv);
            String receivedTpkKcv = transData.getTpkKcv();
            LogUtils.i(TAG, "Injected TPK KCV: " + tpkKcvStr);
            LogUtils.i(TAG, "Received TPK KCV: " + receivedTpkKcv);
            if(!tpkKcvStr.contains(receivedTpkKcv)) {
                LogUtils.e(TAG, "TPK KCV ERROR!");
                return TransResult.ERR_TLE_KCV_MISMATCH;
            }
        }

        return TransResult.SUCC;
    }
}
