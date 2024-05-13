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
package com.evp.bizlib.data.model;

import com.evp.bizlib.R;
import com.evp.commonlib.application.BaseApplication;

public enum ETransType {

    ECHO("0800", "", "990000", "",
            BaseApplication.getAppContext().getString(R.string.trans_echo), (byte) 0x00,
            false, false, false, false, false, true),

    SETTLE("0500", "", "920000", "",
            BaseApplication.getAppContext().getString(R.string.trans_settle), (byte) 0x00,
            false, true, true, false, false, true),

    BATCH_UP("0320", "", "000001", "",
            BaseApplication.getAppContext().getString(R.string.trans_batch_up), (byte) 0x00,
            false, false, false, false, false, true),

    SETTLE_END("0500", "", "960000", "",
            BaseApplication.getAppContext().getString(R.string.trans_settle_end), (byte) 0x00,
            false, false, false, false, false, true),

    SALE("0200", "0400", "000000", "00",
            BaseApplication.getAppContext().getString(R.string.trans_sale), ETransType.READ_MODE_ALL,
            true, true, true, true, false, true),

    VOID("0200", "0400", "020000", "00",
            BaseApplication.getAppContext().getString(R.string.trans_void), (byte) 0x00,
            true, true, false, false, true, true),

    REFUND("0200", "0400", "200000", "00",
            BaseApplication.getAppContext().getString(R.string.trans_refund), (byte) (SearchMode.SWIPE | SearchMode.INSERT | SearchMode.KEYIN),
            true, true, false, true, true, true),

    ADJUST("", "", "000000", "",
            BaseApplication.getAppContext().getString(R.string.trans_adjust), (byte) 0x00,
            false, false, false, false, false, true),

    PREAUTH("0100", "0400", "000000", "00",
            BaseApplication.getAppContext().getString(R.string.trans_preAuth), ETransType.READ_MODE_ALL,
            true, true, false, false, false, true),

    PREAUTH_COMPLETE("0200", "0400", "000000", "06",
            BaseApplication.getAppContext().getString(R.string.trans_preAuthComplete), (byte) (SearchMode.SWIPE | SearchMode.INSERT | SearchMode.KEYIN),
            true, true, false, false, false, false),

    PREAUTH_CANCEL("0100", "0400", "200000", "00",
            BaseApplication.getAppContext().getString(R.string.trans_preAuthCancel), (byte) (SearchMode.SWIPE | SearchMode.INSERT | SearchMode.KEYIN),
            true, true, false, false, false, false),

    OFFLINE_SALE("0220", "", "000000", "00",
            BaseApplication.getAppContext().getString(R.string.trans_offline_send), (byte) (SearchMode.SWIPE | SearchMode.INSERT | SearchMode.KEYIN),
            true, true, true, true, false, true),

    INSTALLMENT("0200", "0400", "000000", "00",
            BaseApplication.getAppContext().getString(R.string.trans_installment), ETransType.READ_MODE_ALL,
            true, true, false, true, false, true),

    REDEEM("0200", "0400", "000000", "00",
            BaseApplication.getAppContext().getString(R.string.trans_redeem), (byte) (SearchMode.SWIPE | SearchMode.INSERT | SearchMode.KEYIN),
            true, true, false, true, false, true),

    OLS_ENQUIRY("0100", "", "310000", "00",
            BaseApplication.getAppContext().getString(R.string.trans_point_enquiry), (byte) (SearchMode.SWIPE | SearchMode.INSERT | SearchMode.KEYIN),
            false, false, false, false, false, true),

    TMK_DOWNLOAD("0800", "", "970000", "",
           BaseApplication.getAppContext().getString(R.string.trans_tmk_download), (byte) 0x00,
            false, false, false, false, false, true),

    TWK_DOWNLOAD("0800", "", "970400", "",
            BaseApplication.getAppContext().getString(R.string.trans_twk_download), (byte) 0x00,
            false, false, false, false, false, true),

    QR_INQUIRY("", "", "", "",
            BaseApplication.getAppContext().getString(R.string.trans_inquiry), (byte) 0x00,
            false, false, false, true, false, true),

    QR_CANCEL("", "", "", "",
            BaseApplication.getAppContext().getString(R.string.trans_cancel), (byte) 0x00,
            false, false, false, false, false, true),

    QR_REGISTER_TAG30("", "", "", "",
            BaseApplication.getAppContext().getString(R.string.register_tag30), (byte) 0x00,
            false, false, false, false, false, true),

    QR_REGISTER_QRCS("", "", "", "",
            BaseApplication.getAppContext().getString(R.string.register_qrcs), (byte) 0x00,
            false, false, false, false, false, true),

    PULL_SLIP("0800", "", "000000", "00",
            BaseApplication.getAppContext().getString(R.string.pull_slip), (byte) 0x00,
            false, false, false, false, false, true),

    PRINT("0800", "", "000000", "00",
            BaseApplication.getAppContext().getString(R.string.receipt_print), (byte) 0x00,
            false, false, false, false, false, true),

    REPORT("0800", "", "000000", "00",
            BaseApplication.getAppContext().getString(R.string.report), (byte) 0x00,
            false, false, false, false, false, true),

    DCC_GET_RATE("0800", "", "000000", "00",
            BaseApplication.getAppContext().getString(R.string.trans_dcc_get_rate), (byte) 0x00,
            false, false, false, false, false, true),

    TRANS_LOG("", "", "", "",
            BaseApplication.getAppContext().getString(R.string.trans_log), (byte) 0x00,
            false, false, false, true, false, true),

    SETTING("", "", "", "",
            BaseApplication.getAppContext().getString(R.string.setting), (byte) 0x00,
            false, false, false, true, false, true),

    UNKNOWN_TRANSACTION("", "", "", "",
            BaseApplication.getAppContext().getString(R.string.unknown), (byte) 0x00,
            false, false, false, true, false, true);

    private static final byte READ_MODE_ALL = (byte) (SearchMode.SWIPE | SearchMode.INSERT | SearchMode.WAVE | SearchMode.KEYIN);
    private String msgType;
    private String dupMsgType;
    private String procCode;
    private String serviceCode;
    private String transName;
    private byte readMode;
    private boolean isDupSendAllowed;
    private boolean isScriptSendAllowed;
    private boolean isAdjustAllowed;
    private boolean isVoidAllowed;
    private boolean isSymbolNegative;
    private boolean isPinAllowed;

    /**
     * @param msgType             ：消息类型码
     * @param dupMsgType          : 冲正消息类型码
     * @param procCode            : 处理码
     * @param serviceCode         ：服务码
     * @param readMode            : read mode
     * @param transName           : 交易名称
     * @param isDupSendAllowed    ：是否冲正上送
     * @param isScriptSendAllowed ：是否脚本结果上送
     * @param isAdjustAllowed     ：is allowed to adjust
     * @param isVoidAllowed       : is allowed to void
     * @param isSymbolNegative    : is symbol negative
     */
    ETransType(String msgType, String dupMsgType, String procCode, String serviceCode,
               String transName, byte readMode, boolean isDupSendAllowed, boolean isScriptSendAllowed,
               boolean isAdjustAllowed, boolean isVoidAllowed, boolean isSymbolNegative, boolean isPinAllowed) {
        this.msgType = msgType;
        this.dupMsgType = dupMsgType;
        this.procCode = procCode;
        this.serviceCode = serviceCode;
        this.transName = transName;
        this.readMode = readMode;
        this.isDupSendAllowed = isDupSendAllowed;
        this.isScriptSendAllowed = isScriptSendAllowed;
        this.isAdjustAllowed = isAdjustAllowed;
        this.isVoidAllowed = isVoidAllowed;
        this.isSymbolNegative = isSymbolNegative;
        this.isPinAllowed = isPinAllowed;
    }

    public String getMsgType() {
        return msgType;
    }

    public String getDupMsgType() {
        return dupMsgType;
    }

    public String getProcCode() {
        return procCode;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public String getTransName() {
        return transName;
    }

    public byte getReadMode() {
        return readMode;
    }

    public boolean isDupSendAllowed() {
        return isDupSendAllowed;
    }

    public boolean isScriptSendAllowed() {
        return isScriptSendAllowed;
    }

    public boolean isAdjustAllowed() {
        return isAdjustAllowed;
    }

    public boolean isVoidAllowed() {
        return isVoidAllowed;
    }

    public boolean isSymbolNegative() {
        return isSymbolNegative;
    }

    public boolean isPinAllowed() { return isPinAllowed; }
}