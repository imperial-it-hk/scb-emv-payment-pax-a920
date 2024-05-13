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

import android.view.View;

import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.receipt.ReceiptConst;
import com.evp.config.ConfigUtils;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.trans.receipt.PrintListener;
import com.evp.pay.utils.EmailInfo;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.sankuai.waimai.router.Router;

/**
 * receipt email transaction
 */
public class ReceiptEmailTrans extends AReceiptEmail {

    /**
     * Send int.
     *
     * @param transData the trans data
     * @param emailInfo the email info
     * @param isRePrint the is re print
     * @param listener  the listener
     * @return the int
     */
    public int send(TransData transData, EmailInfo emailInfo, boolean isRePrint, PrintListener listener) {
        if (!transData.getIssuer().isAllowPrint())
            return 0;

        this.listener = listener;
        if (listener != null)
            listener.onShowMessage(null, Utils.getString(R.string.wait_send));
        View receiptView = Router.callMethod(ReceiptConst.RECEIPT_TRANSDETAIL, FinancialApplication.getApp(), transData, false, 0, ConfigUtils.getInstance());
        int ret = sendHtmlEmail(emailInfo, transData.getEmail(), generateReceiptSubject(transData), "Receipt", receiptView.getDrawingCache());

        if (listener != null) {
            listener.onEnd();
        }

        return ret;
    }

    private String generateReceiptSubject(TransData transData) {
        return "Receipt:[" + transData.getTransType() + "]" + transData.getDateTime();
    }
}
