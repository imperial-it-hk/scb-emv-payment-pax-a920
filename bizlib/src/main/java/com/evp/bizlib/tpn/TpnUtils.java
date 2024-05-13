package com.evp.bizlib.tpn;

import com.evp.bizlib.AppConstants;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.CardRange;
import com.evp.bizlib.data.entity.Issuer;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.commonlib.utils.LogUtils;

import java.util.Arrays;

public class TpnUtils {
    private static final String TAG = "TpnUtils";

    public static boolean isThisTpnOrTscAid(byte[] aid, byte aidLength) {
        if(aid.length != aidLength) {
            byte[] correctAid = new byte[aidLength];
            System.arraycopy(aid, 0, correctAid, 0, aidLength);
            aid = correctAid;
        }
        if(!isThisTpnOrTscAid(aid)) {
            LogUtils.i(TAG, "No TPN or TSC AID detected.");
            return false;
        }
        return true;
    }

    public static boolean isThisTpnOrTscAid(byte[] aid) {
        if(Arrays.equals(aid, AppConstants.TPN_AID)) {
            return true;
        }
        if(Arrays.equals(aid, AppConstants.TSC_AID)) {
            return true;
        }
        return false;
    }

    public static boolean isThisTpnCard(byte[] aid, String pan) {
        if(aid == null) {
            return false;
        }
        if(!isThisTpnOrTscAid(aid)) {
            LogUtils.i(TAG, "No TPN or TSC AID detected.");
            return false;
        }
        CardRange cardRange = GreendaoHelper.getCardRangeHelper().findTpnCardRange(pan);
        if (cardRange == null) {
            LogUtils.i(TAG, "No TPN card detected.");
            return false;
        }
        LogUtils.i(TAG, "TPN card detected.");
        return true;
    }

    public static boolean isThisTscCard(byte[] aid, String pan) {
        if(aid == null) {
            return false;
        }
        if(!isThisTpnOrTscAid(aid)) {
            LogUtils.i(TAG, "No TPN or TSC AID detected.");
            return false;
        }
        CardRange cardRange = GreendaoHelper.getCardRangeHelper().findTscCardRange(pan);
        if (cardRange == null) {
            LogUtils.i(TAG, "No TSC card detected.");
            return false;
        }
        LogUtils.i(TAG, "TSC card detected.");
        return true;
    }

    public static boolean setTpnAcqAndIssuer(Acquirer acquirer, Issuer issuer, TransData transData) {
        if (issuer != null && acquirer != null) {
            transData.setIssuer(issuer);
            transData.setAcquirer(acquirer);
            transData.setNii(acquirer.getNii());
            transData.setBatchNo(acquirer.getCurrBatchNo());
            LogUtils.i(TAG, "TPN acquirer and issuer set.");
            return true;
        }
        LogUtils.i(TAG, "TPN acquirer and issuer set FAILED.");
        return false;
    }

    public static boolean setTscAcqAndIssuer(Acquirer acquirer, Issuer issuer, TransData transData) {
        if (issuer != null && acquirer != null) {
            transData.setIssuer(issuer);
            transData.setAcquirer(acquirer);
            transData.setNii(acquirer.getNii());
            transData.setBatchNo(acquirer.getCurrBatchNo());
            LogUtils.i(TAG, "TSC acquirer and issuer set.");
            return true;
        }
        LogUtils.i(TAG, "TSC acquirer and issuer set FAILED.");
        return false;
    }
}
