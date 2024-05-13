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
package com.evp.pay.utils;

import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.utils.LogUtils;
import com.evp.config.ConfigUtils;
import com.evp.payment.evpscb.R;

import java.util.HashMap;
import java.util.Map;

/**
 * response error message list
 */
public class TransResultUtils {
    private static final Map<Integer, String> messageMap = new HashMap<>();
    static {
        messageMap.put(TransResult.SUCC, ConfigUtils.getInstance().getString("transactionSuccess"));
        messageMap.put(TransResult.ERR_TIMEOUT, ConfigUtils.getInstance().getString("transactionTimeoutLabel"));
        messageMap.put(TransResult.ERR_CONNECT, ConfigUtils.getInstance().getString("connectionTimeoutLabel"));
        messageMap.put(TransResult.ERR_SEND, ConfigUtils.getInstance().getString("sendError"));
        messageMap.put(TransResult.ERR_RECV, ConfigUtils.getInstance().getString("receiveError"));
        messageMap.put(TransResult.ERR_PACK, ConfigUtils.getInstance().getString("packError"));
        messageMap.put(TransResult.ERR_UNPACK, ConfigUtils.getInstance().getString("unpackError"));
        messageMap.put(TransResult.ERR_PACKET, ConfigUtils.getInstance().getString("illegalPackError"));
        messageMap.put(TransResult.ERR_MAC, ConfigUtils.getInstance().getString("macError"));
        messageMap.put(TransResult.ERR_PROC_CODE, ConfigUtils.getInstance().getString("processErrorLabel"));
        messageMap.put(TransResult.ERR_MSG, ConfigUtils.getInstance().getString("unpackMessageTypeCodeError"));
        messageMap.put(TransResult.ERR_TRANS_AMT, ConfigUtils.getInstance().getString("unpackAmountError"));
        messageMap.put(TransResult.ERR_TRACE_NO, ConfigUtils.getInstance().getString("unpackTraceNoError"));
        messageMap.put(TransResult.ERR_STAN_NO, ConfigUtils.getInstance().getString("unpackStanNoError"));
        messageMap.put(TransResult.ERR_ENTERED_DATE, ConfigUtils.getInstance().getString("enteredDateError"));
        messageMap.put(TransResult.ERR_TERM_ID, ConfigUtils.getInstance().getString("unpackTerminalIdError"));
        messageMap.put(TransResult.ERR_MERCH_ID, ConfigUtils.getInstance().getString("unpackMerchantIdError"));
        messageMap.put(TransResult.ERR_NO_TRANS, ConfigUtils.getInstance().getString("noTransError"));
        messageMap.put(TransResult.ERR_NO_ORIG_TRANS,ConfigUtils.getInstance().getString("originalTransNotExistError"));
        messageMap.put(TransResult.ERR_HAS_VOIDED, ConfigUtils.getInstance().getString("voidedTransactionError"));
        messageMap.put(TransResult.ERR_VOID_UNSUPPORTED, ConfigUtils.getInstance().getString("voidedTransactionNotSupportError"));
        messageMap.put(TransResult.ERR_COMM_CHANNEL,ConfigUtils.getInstance().getString("openCommunicationError"));
        messageMap.put(TransResult.ERR_HOST_REJECT, ConfigUtils.getInstance().getString("hostRejectedError"));
        messageMap.put(TransResult.ERR_ABORTED, ConfigUtils.getInstance().getString("abortError"));
        messageMap.put(TransResult.ERR_USER_CANCEL, ConfigUtils.getInstance().getString("transactionCancelError"));
        messageMap.put(TransResult.ERR_NEED_SETTLE_NOW, ConfigUtils.getInstance().getString("transactionExistedNowError"));
        messageMap.put(TransResult.ERR_NEED_SETTLE_LATER, ConfigUtils.getInstance().getString("transactionExistedLaterError"));
        messageMap.put(TransResult.ERR_NO_FREE_SPACE, ConfigUtils.getInstance().getString("noFreeSpaceError"));
        messageMap.put(TransResult.ERR_NOT_SUPPORT_TRANS, ConfigUtils.getInstance().getString("unsupportedTransaction"));
        messageMap.put(TransResult.ERR_BATCH_UP_NOT_COMPLETED, ConfigUtils.getInstance().getString("lastBatchSettleError"));
        messageMap.put(TransResult.ERR_CARD_NO, ConfigUtils.getInstance().getString("noMatchOriginalCardError"));
        messageMap.put(TransResult.ERR_PASSWORD, ConfigUtils.getInstance().getString("passwordIsIncorrectLabel"));
        messageMap.put(TransResult.ERR_PARAM, ConfigUtils.getInstance().getString("parameterError"));
        messageMap.put(TransResult.ERR_AMOUNT, ConfigUtils.getInstance().getString("amountExceed"));
        messageMap.put(TransResult.ERR_CARD_DENIED, ConfigUtils.getInstance().getString("deniedCard"));
        messageMap.put(TransResult.ERR_ADJUST_UNSUPPORTED, ConfigUtils.getInstance().getString("notAdjustableTransError"));
        messageMap.put(TransResult.ERR_CARD_UNSUPPORTED, ConfigUtils.getInstance().getString("unSupportedCardError"));
        messageMap.put(TransResult.ERR_CARD_EXPIRED, ConfigUtils.getInstance().getString("expiredCardError"));
        messageMap.put(TransResult.ERR_CARD_INVALID, ConfigUtils.getInstance().getString("cardPanError"));
        messageMap.put(TransResult.ERR_UNSUPPORTED_FUNC, ConfigUtils.getInstance().getString("unsupportedFunError"));
        messageMap.put(TransResult.ERR_CLSS_PRE_PROC, ConfigUtils.getInstance().getString("clssPreProcError"));
        messageMap.put(TransResult.ERR_INVALID_EMV_QR, ConfigUtils.getInstance().getString("invalidQrCode"));
        messageMap.put(TransResult.ERR_INVALID_BT_PRINTER,ConfigUtils.getInstance().getString("invalidBluetoothPrinter"));
        messageMap.put(TransResult.ERR_BT_PRINT_CANCEL, ConfigUtils.getInstance().getString("printCancel"));
        messageMap.put(TransResult.ERR_BT_CONNECT, ConfigUtils.getInstance().getString("bluetoothConnError"));
        messageMap.put(TransResult.ERR_TLE_KEYS_MISSING, ConfigUtils.getInstance().getString("tleDownloadError"));
        messageMap.put(TransResult.ERR_TLE_KCV_MISMATCH, ConfigUtils.getInstance().getString("tleKcvError"));
        messageMap.put(TransResult.ERR_INVALID_QR, ConfigUtils.getInstance().getString("invalidQrCode"));
        messageMap.put(TransResult.ERR_TRANS_NOT_FOUND, ConfigUtils.getInstance().getString("transNotFound"));
        messageMap.put(TransResult.ERR_LAST_SETTLE_FAILED, ConfigUtils.getInstance().getString("lastSettleFailedError"));
        messageMap.put(TransResult.ERR_NO_SETTLE_FROM_YESTERDAY, ConfigUtils.getInstance().getString("lastNoSettleFromYesterday"));
    }

    private TransResultUtils() {

    }

    /**
     * get response message by result code
     *
     * @param ret result code
     * @return response message
     */
    public static String getMessage(int ret) {
        String message;
        String resource = messageMap.get(ret);
        if (resource == null) {
            try {
                message = Utils.getString(ret);
            } catch (Exception e) {
                LogUtils.e("getMessage", "", e);
                message = Utils.getString(R.string.err_undefine) + "[" + ret + "]";
            }
        } else {
            message = resource;
        }
        return message;
    }
}
