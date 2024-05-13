package com.evp.bizlib.smallamt;

import com.evp.bizlib.AppConstants;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.Issuer;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.model.ETransType;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.LogUtils;

public class SmallAmtUtils {
    private static final String TAG = SmallAmtUtils.class.getSimpleName();

    static public boolean isTrxSmallAmt(final TransData transData) {
        if(transData.getHasPin()) {
            LogUtils.i(TAG, "PIN entered - small amount not applied.");
            return false;
        }

        if(transData.isSignFree()) {
            LogUtils.i(TAG, "Card holder NO CVM - small amount not applied.");
            return false;
        }

        TransData.EnterMode enterMode = transData.getEnterMode();
        if(enterMode != TransData.EnterMode.INSERT
            && enterMode != TransData.EnterMode.CLSS)
        {
            LogUtils.i(TAG, "Enter mode is not CT or CTLS - small amount not applied.");
            return false;
        }

        ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
        if(transType != ETransType.SALE
            && transType != ETransType.PREAUTH
            && transType != ETransType.REFUND)
        {
            LogUtils.i(TAG, "Transaction type is not allowed for small amount.");
            return false;
        }

        Acquirer acquirer = transData.getAcquirer();
        if(acquirer != null && acquirer.getName().equals(AppConstants.DCC_ACQUIRER)) {
            LogUtils.i(TAG, "DCC acquirer detected - small amount not applied.");
            return false;
        }

        Issuer issuer = transData.getIssuer();
        if(issuer == null) {
            LogUtils.i(TAG, "Issuer not found!");
            return false;
        }

        long amt = ConvertUtils.parseLongSafe(transData.getAmount(), -1);
        if(amt == -1) {
            LogUtils.i(TAG, "Amount ERROR!");
            return false;
        }

        if(amt > issuer.getSmallAmtLimit()) {
            LogUtils.i(TAG, "Transaction amount exceeded small amount.");
            return false;
        }

        if(transType == ETransType.PREAUTH
                && issuer.getName().equals(AppConstants.MC_ISSUER)
                && transData.getEnterMode() == TransData.EnterMode.INSERT)
        {
            LogUtils.i(TAG, "MasterCard Chip for Pre-Auth transaction is not allowed for small amount.");
            return false;
        }

        LogUtils.i(TAG, "Transaction is small amount capable.");
        return true;
    }
}
