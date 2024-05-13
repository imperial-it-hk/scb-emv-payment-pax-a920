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

import com.evp.bizlib.data.entity.TransData;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.pay.trans.receipt.PrintListener;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;

/**
 * receipt SMS transaction
 */
public class ReceiptSMSTrans extends AReceiptSMS {
    /**
     * Send int.
     *
     * @param transData the trans data
     * @param listener  the listener
     * @return the int
     */
//send transData
    public int send(TransData transData, PrintListener listener) {
        if (!transData.getIssuer().isAllowPrint())
            return 0;

        this.listener = listener;
        if (listener != null)
            listener.onShowMessage(null, Utils.getString(R.string.wait_send));
        int ret = sendTextMessage(transData.getPhoneNum(), generateString(transData));

        if (listener != null) {
            listener.onEnd();
        }

        return ret;
    }

    /**
     * Generate string string.
     *
     * @param transData the trans data
     * @return the string
     */
    public String generateString(TransData transData) {
        return "Card No:" + transData.getPan() + "\nTrans Type:" + transData.getTransType()
                + "\nAmount:" + CurrencyConverter.convert(Utils.parseLongSafe(transData.getAmount(), 0), transData.getCurrency())
                + "\nTip:" + CurrencyConverter.convert(Utils.parseLongSafe(transData.getTipAmount(), 0), transData.getCurrency())
                + "\nTransData:" + transData.getDateTime();
    }
}
