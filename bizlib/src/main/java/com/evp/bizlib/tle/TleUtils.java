package com.evp.bizlib.tle;

import androidx.annotation.NonNull;

import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.model.ETransType;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.CryptoUtils;
import com.evp.commonlib.utils.LogUtils;

import org.greenrobot.greendao.annotation.NotNull;

public class TleUtils {
    private static final String TAG = TleUtils.class.getSimpleName();

    public static boolean isTrxTle(@NonNull TransData transData) {
        ETransType trxType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
        return isTrxTle(transData.getAcquirer(), trxType);
    }

    public static boolean isTrxTle(@NonNull Acquirer acquirer, @NotNull ETransType transType)
    {
        if(!acquirer.getTleEnabled()) {
            return false;
        }

        if (transType == ETransType.SALE
                || transType == ETransType.VOID
                || transType == ETransType.REFUND
                || transType == ETransType.OLS_ENQUIRY
                || transType == ETransType.REDEEM
                || transType == ETransType.INSTALLMENT
                || transType == ETransType.ADJUST
                || transType == ETransType.BATCH_UP
                || transType == ETransType.PREAUTH
                || transType == ETransType.PREAUTH_COMPLETE
                || transType == ETransType.PREAUTH_CANCEL
                || transType == ETransType.OFFLINE_SALE)
        {
            return true;
        }

        return false;
    }

    public static String getTleErrorCode(byte[] field63) {
        String ret = "LE";
        if(field63 == null) {
            LogUtils.e(TAG, "Field 63 is null.");
            return ret;
        }
        if(field63.length < 5) {
            LogUtils.e(TAG, "Field 63 not long enough.");
            return ret;
        }
        return new String(field63, 4 ,2);
    }

    public static boolean isTrxTleKeyDL(@NonNull TransData transData) {
        ETransType trxType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
        if(!transData.getAcquirer().getTleEnabled()) {
            return false;
        }
        if (trxType == ETransType.TMK_DOWNLOAD || trxType == ETransType.TWK_DOWNLOAD) {
            return true;
        }
        return false;
    }

    public static boolean getTleTmkFromField62(TransData transData) {
        byte[] field62 = transData.getField62();
        if (field62 == null || field62.length <= 0) {
            LogUtils.e(TAG, "Error - TMK Field 62 is empty!");
            return false;
        }
        LogUtils.i(TAG, "Field 62 length is: " + field62.length);
        if(field62.length < TleConst.TMK_FIELD62_SIZE) {
            LogUtils.e(TAG, "Error - TMK Field 62 not long enough!");
            return false;
        }
        String htleHead = new String(field62, 0, 4);
        if(!htleHead.contains(TleConst.TLE_HEAD)) {
            LogUtils.e(TAG, "Error - TMK Field 62 not HTLE!");
            return false;
        }

        //Parse encrypted TMK
        byte[] encTmk = new byte[256];
        System.arraycopy(field62, 11, encTmk, 0, 256);
        LogUtils.hex(TAG, "Received encrypted TMK: ", encTmk);

        //Parse TMK Key Check Value
        byte[] tmkKcv = new byte[6];
        System.arraycopy(field62, 11 + 256, tmkKcv, 0, 6);
        String tmkKcvStr = new String(tmkKcv);
        LogUtils.i(TAG, "Received TMK KCV: " + tmkKcvStr);
        transData.setTmkKcv(tmkKcvStr);

        //Parse TMK ID
        byte[] tmkKeyId = new byte[4];
        System.arraycopy(field62, 11 + 256 + 6, tmkKeyId, 0, 4);
        LogUtils.hex(TAG, "Received TMK ID: ", tmkKeyId);
        transData.getAcquirer().setTleCurrentTmkId(tmkKeyId);

        //Decrypt parsed TMK using RSA private key
        byte[] clrTmk = CryptoUtils.decryptWithRsaPubKey(transData.getRsaKeyPair(), encTmk);
        if (clrTmk == null || clrTmk.length <= 0)
            return false;
        LogUtils.hex(TAG, "Received clear TMK: ", clrTmk);
        transData.setTmkKey(clrTmk);

        //Calculate TMK KCV
        byte[] calcKcv = CryptoUtils.calcTleKcv(clrTmk);
        if (calcKcv == null || calcKcv.length <= 0)
            return false;
        LogUtils.hex(TAG, "Calculated TMK KCV: ", calcKcv);
        String calcKcvStr = ConvertUtils.binToAscii(calcKcv);
        calcKcvStr = calcKcvStr.substring(0, 6);

        if(!calcKcvStr.contains(tmkKcvStr)) {
            LogUtils.i(TAG, "ERROR - TMK KCV NOT EQUAL!");
            return false;
        }
        LogUtils.i(TAG, "TMK KCV OK");

        return true;
    }

    public static boolean getTleTwkFromField62(TransData transData) {
        byte[] field62 = transData.getField62();
        if (field62 == null || field62.length <= 0) {
            LogUtils.e(TAG, "Error - TWK Field 62 is empty!");
            return false;
        }
        LogUtils.i(TAG, "Field 62 length is: " + field62.length);
        if(field62.length < TleConst.TWK_FIELD62_SIZE) {
            LogUtils.e(TAG, "Error - TWK Field 62 not long enough!");
            return false;
        }
        String htleHead = new String(field62, 0, 4);
        if(!htleHead.contains(TleConst.TLE_HEAD)) {
            LogUtils.e(TAG, "Error - TWK Field 62 not HTLE!");
            return false;
        }

        //Parse TWK ID
        byte[] twkId = new byte[4];
        System.arraycopy(field62, 7, twkId, 0, 4);
        LogUtils.hex(TAG, "Received TWK-ID: ", twkId);
        transData.getAcquirer().setTleCurrentTwkId(twkId);

        //Parse encrypted TWK-DEK
        byte[] twkDek = new byte[16];
        System.arraycopy(field62, 11, twkDek, 0, 16);
        LogUtils.hex(TAG, "Received TWK-DEK: ", twkDek);
        transData.setDekKey(twkDek);

        //Parse encrypted TWK-MAK
        byte[] twkMak = new byte[16];
        System.arraycopy(field62, 27, twkMak, 0, 16);
        LogUtils.hex(TAG, "Received TWK-MAK: ", twkMak);
        transData.setMakKey(twkMak);

        //Parse TWK-DEK KCV
        byte[] twkDekKcv = new byte[8];
        System.arraycopy(field62, 43, twkDekKcv, 0, 8);
        String twkDekKcvStr = new String(twkDekKcv);
        LogUtils.i(TAG, "Received TWK-DEK-KCV: " + twkDekKcvStr);
        transData.setTdkKcv(twkDekKcvStr);

        //Parse TWK-MAK KCV
        byte[] twkMakKcv = new byte[8];
        System.arraycopy(field62, 51, twkMakKcv, 0, 8);
        String twkMakKcvStr = new String(twkMakKcv);
        LogUtils.i(TAG, "Received TWK-MAK-KCV: " + twkMakKcvStr);
        transData.setTakKcv(twkMakKcvStr);

        //Parse Acquirer ID
        byte[] acqId = new byte[3];
        System.arraycopy(field62, 59, acqId, 0, 3);
        String acqIdStr = new String(acqId);
        LogUtils.i(TAG, "Received AcqID: " + acqIdStr);
        String currAcqId = transData.getAcquirer().getTleAcquirerId();
        LogUtils.i(TAG, "Current AcqID: " + acqIdStr);
        if(!currAcqId.contains(acqIdStr)) {
            LogUtils.i(TAG, "New AcqID received!");
            transData.getAcquirer().setTleAcquirerlId(acqIdStr);
        }

        //Check for PIN key presence
        boolean pinKeyReceived = false;
        if(field62.length > 63 && field62[62] == 'P' && field62[63] == 'K') {
            pinKeyReceived = true;
        }

        //Parse PIN key & PIN key KCV
        if(pinKeyReceived) {
            byte[] twkPk = new byte[16];
            System.arraycopy(field62, 64, twkPk, 0, 16);
            LogUtils.hex(TAG, "Received TWK-PK: ", twkPk);
            transData.setPinKey(twkPk);

            byte[] twkPkKcv = new byte[8];
            System.arraycopy(field62, 80, twkPkKcv, 0, 8);
            String twkPkKcvStr = new String(twkPkKcv);
            LogUtils.i(TAG, "Received TWK-PK-KCV: " + twkPkKcvStr);
            transData.setTpkKcv(twkPkKcvStr);
        }

        return true;
    }
}
