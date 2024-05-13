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

import static com.evp.commonlib.utils.ConvertUtils.binToAscii;
import static com.evp.commonlib.utils.ConvertUtils.getPaddedString;

import android.text.TextUtils;

import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.onlinepacket.IPacker;
import com.evp.bizlib.onlinepacket.OnlinePacketConst;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.LogUtils;
import com.evp.pay.app.ActivityStack;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.record.PrinterUtils;
import com.evp.pay.trans.RedeemTrans;
import com.evp.pay.trans.component.Component;
import com.evp.pay.utils.Log8583Utils;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.settings.SysParam;
import com.pax.dal.exceptions.PedDevException;
import com.sankuai.waimai.router.Router;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * The type Online.
 */
public class Online {

    private static final String TAG = "Online";

    private TransProcessListener listener;
    private ATcp tcp;
    private IPacker<TransData, byte[]> packager;
    private byte[] sendData;

    /**
     * Online int.
     *
     * @param transData the trans data
     * @param listener  the listener
     * @return the int
     * @throws PedDevException the ped dev exception
     */
    public int online(TransData transData, final TransProcessListener listener) throws PedDevException {
        return this.online(transData, listener, true, true);
    }

    /**
     * Online int.
     *
     * @param transData  the trans data
     * @param listener   the listener
     * @param reconnect  the reconnect
     * @param forceClose the force close
     * @return the int
     * @throws PedDevException the ped dev exception
     */
    public int online(TransData transData, final TransProcessListener listener, final boolean reconnect, final boolean forceClose) throws PedDevException {
        try {
            this.listener = listener;
            onShowMsg(Utils.getString(R.string.wait_process));

            ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
            //Create packager object
            if (transData.getReversalStatus() == TransData.ReversalStatus.REVERSAL) {
                packager = Router.getService(IPacker.class, OnlinePacketConst.PACKET_REVERSAL);
            } else {
                packager = Router.getService(IPacker.class, OnlinePacketConst.PACKET_HEADER+transType.name());
            }

            //Try to connect to host
            int ret = doConnect(transData.getAcquirer(), reconnect);
            if (ret != TransResult.SUCC) {
                return ret;
            }

            //Create message for host
            ret = doPack(transData);
            if (ret != TransResult.SUCC) {
                return ret;
            }

            //Mark this transaction as online
            transData.setOnlineTrans(true);

            //Send message to host
            LogUtils.hex(TAG, "SEND", sendData);
            ret = tcp.onSend(sendData);
            if (ret != 0) {
                return TransResult.ERR_SEND;
            }

            //Mark reversal status and save transaction to DB
            initReversalStatus(transData);

            //Increase STAN number
            if (transData.getReversalStatus() != TransData.ReversalStatus.REVERSAL
                    && transType != ETransType.OFFLINE_SALE)
            {
                Component.incStanNo();
            }

            //Wait for response from host
            TcpResponse tcpResponse = tcp.onRecv();

            if (tcpResponse.getRetCode() != TransResult.SUCC) {
                //Update status and reason of reversal
                if (isDupPackNonNull(transData.getTransType()) && transData.getReversalStatus() != TransData.ReversalStatus.REVERSAL) {
                    transData.setReversalStatus(TransData.ReversalStatus.PENDING);
                    transData.setDupReason(TransData.DUP_REASON_NO_RECV);
                    GreendaoHelper.getTransDataHelper().update(transData);
                }
                return TransResult.ERR_RECV;
            }

            return doUnpack(transData, tcpResponse);
        } finally {
            if (forceClose) {
                close();
            }
        }
    }

    private int doConnect(Acquirer acquirer, boolean reconnect) {
        if (reconnect) {
            tcp = getTcpClient(acquirer);
            if (tcp == null) {
                return TransResult.ERR_CONNECT;
            }
        }
        tcp.setTransProcessListener(listener);
        if (reconnect) {
            LogUtils.i(TAG, String.format("%s%s%s%s", "Connecting to: ", acquirer.getIp(), ":", acquirer.getPort()));
            int ret = tcp.onConnect();
            if (ret != 0) {
                return TransResult.ERR_CONNECT;
            }
        }
        return TransResult.SUCC;
    }

    private int doPack(TransData transData) throws PedDevException {
        onShowMsg(Utils.getString(R.string.wait_process));
        byte[] req = packager.pack(transData);
        if (SysParam.getInstance().getBoolean(R.string.EDC_ENABLE_SAVE_LOG)) {
            int index = (int) SysParam.getInstance().get(R.string.LOG_FILE_INDEX, 1);
            Log8583Utils.saveLog("8583log_" + index + ".txt", req, true);
        }

        if (SysParam.getInstance().getBoolean(R.string.EDC_ENABLE_PRINT_LOG)) {
            PrinterUtils.printLastTransOnlineLog(ActivityStack.getInstance().top(),req,true);
        }
        if (req.length == 0) {
            return TransResult.ERR_PACK;
        }
        sendData = new byte[2 + req.length];
        sendData[0] = (byte) (req.length / 256);
        sendData[1] = (byte) (req.length % 256);
        System.arraycopy(req, 0, sendData, 2, req.length);
        return TransResult.SUCC;
    }

    private int doUnpack(TransData transData, TcpResponse tcpResponse) throws PedDevException {
        //Is this DEMO mode?
        if (TcpDemoMode.class.equals(tcp.getClass())) {
            createDummyRecvData(transData);
            increaseTraceNo(transData);
            return TransResult.SUCC;
        }

        LogUtils.hex(TAG, "RECV", tcpResponse.getData());
        if (SysParam.getInstance().getBoolean(R.string.EDC_ENABLE_SAVE_LOG)) {
            int index = (int) SysParam.getInstance().get(R.string.LOG_FILE_INDEX, 1);
            Log8583Utils.saveLog("8583log_" + index + ".txt", tcpResponse.getData(), false);
        }

        if (SysParam.getInstance().getBoolean(R.string.EDC_ENABLE_PRINT_LOG)) {
            PrinterUtils.printLastTransOnlineLog(ActivityStack.getInstance().top(),tcpResponse.getData(),false);
        }

        if (isDupPackNonNull(transData.getTransType()) && transData.getReversalStatus() == TransData.ReversalStatus.REVERSAL) {
            return packager.unpack(transData, tcpResponse.getData());
        }

        int ret = packager.unpack(transData, tcpResponse.getData());
        //In case MAC was wrong we mark transaction for reversal
        if (ret == TransResult.ERR_MAC && isDupPackNonNull(transData.getTransType()) &&
                transData.getReversalStatus() != TransData.ReversalStatus.REVERSAL) {
            transData.setReversalStatus(TransData.ReversalStatus.PENDING);
            transData.setDupReason(TransData.DUP_REASON_MACWRONG);
            transData.setDateTime(transData.getDateTime());
            GreendaoHelper.getTransDataHelper().update(transData);
        }

        //Get rid of reversal in case it's not required
        if (isReceivedPackageWrong(ret)) {
            GreendaoHelper.getTransDataHelper().deleteDupRecord(transData.getAcquirer());
        } else {
            //Increase invoice number only when transaction was approved
            increaseTraceNo(transData);
        }

        return ret;
    }

    private void increaseTraceNo(TransData transData) {
        ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());

        if (transData.getReversalStatus() != TransData.ReversalStatus.REVERSAL
                && transType != ETransType.OFFLINE_SALE
                && transType != ETransType.SETTLE
                && transType != ETransType.BATCH_UP
                && transType != ETransType.ECHO
                && transType != ETransType.TWK_DOWNLOAD
                && transType != ETransType.TMK_DOWNLOAD
                && transType != ETransType.VOID
                && transType != ETransType.DCC_GET_RATE) {
            Component.incTransNo();
        }
    }

    private static boolean isDupPackNonNull(String transTypeStr){
        ETransType transType = ConvertUtils.enumValue(ETransType.class, transTypeStr);
        return !TextUtils.isEmpty(Objects.requireNonNull(transType).getDupMsgType());
    }

    private void initReversalStatus(TransData transData) {
        if (isDupPackNonNull(transData.getTransType()) && transData.getReversalStatus() == TransData.ReversalStatus.NORMAL) {
            transData.setReversalStatus(TransData.ReversalStatus.PENDING);
            if (transData.getOfflineSendState() != null) {
                GreendaoHelper.getTransDataHelper().update(transData);
            } else {
                GreendaoHelper.getTransDataHelper().insert(transData);
            }
        }
    }

    private boolean isReceivedPackageWrong(int ret) {
        return ret == TransResult.ERR_PACKET
                || ret == TransResult.ERR_PROC_CODE
                || ret == TransResult.ERR_TRANS_AMT
                || ret == TransResult.ERR_TRACE_NO
                || ret == TransResult.ERR_STAN_NO
                || ret == TransResult.ERR_TERM_ID
                || ret == TransResult.ERR_MERCH_ID;
    }

    /**
     * Close.
     */
    public void close() {
        if (tcp != null) {
            tcp.onClose();
        }
    }

    private void onShowMsg(String msg) {
        if (listener != null) {
            listener.onShowProgress(msg, SysParam.getInstance().getInt(R.string.COMM_TIMEOUT));
        }
    }

    private ATcp getTcpClient(Acquirer acquirer) {
        SysParam.CommSslType commSslType = ConvertUtils.enumValue(SysParam.CommSslType.class, acquirer.getSslType());
        if (Component.isDemo()) {
            return new TcpDemoMode();
        } else if (SysParam.CommSslType.SSL == commSslType) {
            InputStream inputStream;
            try {
                inputStream = FinancialApplication.getApp().getAssets().open(Constants.SCB_CACERT_PATH);
            } catch (IOException e) {
                LogUtils.e(TAG, "", e);
                return null;
            }
            LogUtils.i(TAG, "Using SSL connection");
            return new TcpSsl(inputStream);
        } else {
            LogUtils.i(TAG, "Using standard connection");
            return new TcpNoSsl();
        }
    }

    private void createDummyRecvData(TransData transData) {
        ETransType trans = ConvertUtils.enumValue(ETransType.class, transData.getTransType());

        if (trans != ETransType.VOID && trans != ETransType.REFUND) {
            transData.setAuthCode(transData.getDateTime().substring(8));
        }

        transData.setRefNo(transData.getDateTime().substring(2));
        transData.setResponseCode("00");
        if (trans == ETransType.SETTLE) {
            transData.setResponseCode("95");
        }
        // 测试交易上送， 模拟平台下发脚本
        byte[] rspF55 = ConvertUtils.asciiToBin("72289F1804AABBCCDD86098424000004AABBCCDD86098418000004AABBCCDD86098416000004AABBCCDD");
        transData.setRecvIccData(rspF55);

        transData.setDccForeignAmount("000000012345");
        transData.setDccExchangeRate("87654321");
        transData.setDccCurrencyCode("840");

        //region IPP OLS
        String f63 = "";
        switch (trans != ETransType.VOID ? trans : ConvertUtils.enumValue(ETransType.class, transData.getOrigTransType())) {
            case INSTALLMENT:
                if (trans != ETransType.VOID)
                    f63 = "0102353820202056455249464F4E4520535550504C4945522020202020205456204C4344204449474954414C20202020202020202020202020534F4E5920544830303030303030303030303030303030"
                            + binToAscii(getPaddedString(transData.getAmount(), 12).getBytes())
                            + binToAscii(getPaddedString(String.valueOf((Utils.parseLongSafe(transData.getAmount(), 0) / Utils.parseLongSafe(transData.getPaymentTerm() ,1 ))), 12).getBytes())
                            ;
                break;
            case OLS_ENQUIRY:
                f63 = "30303002534342435331425054544D47202D2043432053434220504F42505402430000000125000000000000000000534342544D474250544D434152442020202020202020202042505400430099945367000099945367004A30000000000000000000";
                break;
            case REDEEM:
                switch (ConvertUtils.getEnum(RedeemTrans.Plan.class, transData.getPaymentPlan())) {
                    case VOUCHER:
                        f63 = "4c30310153434250303142505453434220504f494e545320504f4f4c2420200052"
                                + getPaddedString(transData.getRedeemQty(), 10) + "00"
                                + "0000002469000000000000000153434250303142505453434220504f494e545320504f4f4c24202000430000178615000000178615000000000000000000000000"
                                ;
                        break;
                    case DISCOUNT:
                        transData.setAmount(getPaddedString(String.valueOf(Utils.parseLongSafe(transData.getAmount(), 0) / 2), 12));
                        f63 = "5830330153434250303142505453434220504f494e545320504f4f4c2420200052"
                                + transData.getAmount()
                                + transData.getAmount()
                                + "0000000050000153434250303142505453434220504f494e545320504f4f4c24202000430000185000000000185000000000";
                        break;
                    case POINT_CREDIT:
                        transData.setAmount(getPaddedString(String.valueOf(Utils.parseLongSafe(transData.getAmount(), 0) / 2), 12));
                        f63 = "5830330153434250303142505453434220504f494e545320504f4f4c2420200052"
                                + getPaddedString(transData.getRedeemQty(), 10) + "00"
                                + transData.getAmount()
                                + "0000000000000153434250303142505453434220504f494e545320504f4f4c24202000430000185000000000185000000000";
                        break;
                    case SPECIAL_REDEEM:
                        f63 = "4c303201" +
                                binToAscii(String.format("%-20s", transData.getProductCode()).getBytes()) +
                                "444953434f554e5420313325202020202020202020202020202020202020" +
                                getPaddedString(transData.getRedeemQty(), 12) +
                                "000000000100" +
                                "000000010000" +
                                "000000000000000000000000594530303030310153434250303142505453434220504f494e545320504f4f4c24202000520000000100000000000100000000000000000153434250303142505453434220504f494e545320504f4f4c24202000430000190870000000190870000000000000000000000000";
                        break;
                }
                break;
        }
        if (!f63.isEmpty())
            transData.setField63(ConvertUtils.asciiToBin(f63));
        //endregion
    }
}
