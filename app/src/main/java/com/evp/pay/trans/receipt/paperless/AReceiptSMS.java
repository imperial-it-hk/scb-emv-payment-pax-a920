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
package com.evp.pay.trans.receipt.paperless;

import android.telephony.SmsManager;

import com.evp.commonlib.utils.LogUtils;
import com.evp.pay.trans.receipt.PrintListener;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;

/**
 * The type A receipt sms.
 */
abstract class AReceiptSMS {

    private static final String TAG = "AReceiptSMS";

    /**
     * The Listener.
     */
    protected PrintListener listener;

    /**
     * Send text message int.
     *
     * @param phoneNo the phone no
     * @param message the message
     * @return the int
     */
//send SMS
    public int sendTextMessage(String phoneNo, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, message, null, null);
        } catch (Exception e) {
            LogUtils.w(TAG, "", e);
            if (listener != null)
                listener.onShowMessage(null, Utils.getString(R.string.err_sms_sent_fail));
            return -1;
        }
        return 0;
    }
}
