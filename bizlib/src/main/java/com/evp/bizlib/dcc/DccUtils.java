package com.evp.bizlib.dcc;

import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.model.ETransType;
import com.evp.commonlib.currency.CountryCode;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.utils.LogUtils;
import com.evp.poslib.gl.convert.ConvertHelper;

public class DccUtils {
    private static final String TAG = DccUtils.class.getSimpleName();

    public static boolean isTrxDcc(Acquirer acq, String transType) {
        if(acq == null) {
            LogUtils.e(TAG, "DCC Acquirer missing!");
            return false;
        }
        if(transType.equals(ETransType.SALE.name())
            || transType.equals(ETransType.PREAUTH.name())
            || transType.equals(ETransType.REFUND.name())) {
            LogUtils.i(TAG, "DCC available and supported for transaction type");
            return true;
        }
        LogUtils.e(TAG, "DCC not supported for transaction type.");
        return false;
    }

    public static boolean isTrxDcc(Acquirer acq, byte[] cardCurr, String transType) {
        if(acq == null) {
            LogUtils.e(TAG, "DCC Acquirer missing!");
            return false;
        }
        if(cardCurr == null || cardCurr.length <= 0) {
            LogUtils.e(TAG, "Card currency missing!");
            return false;
        }

        final String cardCurrency = ConvertHelper.getConvert().bcdToStr(cardCurr).substring(1);
        LogUtils.i(TAG, "Card currency: " + cardCurrency);

        final String edcCurrency = String.valueOf(
                CountryCode.getByCode(CurrencyConverter.getDefCurrency().getCountry()).getNumeric());
        LogUtils.i(TAG, "EDC currency: " + edcCurrency);

        if(cardCurrency.contains(edcCurrency)) {
            LogUtils.i(TAG, "No DCC card");
            return false;
        }

        if(transType.equals(ETransType.SALE.name())
                || transType.equals(ETransType.PREAUTH.name())
                || transType.equals(ETransType.REFUND.name())) {
            LogUtils.i(TAG, "DCC available and supported for transaction type");
            return true;
        }

        LogUtils.e(TAG, "DCC not supported for transaction type.");
        return false;
    }

    public static String getExRateForPrint(String exchangeRate) {
        try {
            String exRateStr = String.format("%s.%s", exchangeRate.substring(0, exchangeRate.length() - 4), exchangeRate.substring(4));
            double exRateDbl = Double.parseDouble(exRateStr);
            return Double.toString(exRateDbl);
        } catch (NumberFormatException | IndexOutOfBoundsException | NullPointerException e) {
            LogUtils.e(TAG, e);
        }
        return exchangeRate;
    }
}
