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
 * 20190108  	         Linhb                   Create
 * ===========================================================================================
 */

package com.evp.eemv;

import com.evp.eemv.exception.EEmvExceptions;
import com.evp.eemv.exception.EmvException;
import com.evp.eemv.utils.Tools;

class Amount {
    byte[] baseAmount;
    byte[] cashBackAmt;

    public Amount() {
        this.baseAmount = new byte[0];
        this.cashBackAmt = new byte[0];
    }

    public String getAmount() {
        return Tools.bytes2String(this.baseAmount);
    }

    public void setAmount(String amount) throws EmvException {
        if (amount.length() > 12) {
            throw new EmvException(EEmvExceptions.EMV_ERR_AMOUNT_FORMAT);
        }

        this.baseAmount = Tools.string2Bytes(amount);
    }

    public String getCashBackAmt() {
        return Tools.bytes2String(this.cashBackAmt);
    }

    public void setCashBackAmt(String cashBackAmt) throws EmvException {
        if (cashBackAmt.length() > 12) {
            throw new EmvException(EEmvExceptions.EMV_ERR_AMOUNT_FORMAT);
        }

        this.cashBackAmt = Tools.string2Bytes(cashBackAmt);
    }
}

/* Location:           E:\Linhb\projects\Android\PaxEEmv_V1.00.00_20170401\lib\PaxEEmv_V1.00.00_20170401.jar
 * Qualified Name:     com.pax.eemv.Amount
 * JD-Core Version:    0.6.0
 */