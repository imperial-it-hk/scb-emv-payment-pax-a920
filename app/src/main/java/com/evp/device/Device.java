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
package com.evp.device;

import android.os.Build;
import android.os.SystemClock;

import androidx.annotation.IntRange;

import com.evp.bizlib.params.ParamHelper;
import com.evp.commonlib.utils.LogUtils;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.payment.evpscb.R;
import com.evp.settings.SysParam;
import com.pax.dal.ICardReaderHelper;
import com.pax.dal.IPicc;
import com.pax.dal.IScanner;
import com.pax.dal.entity.EBeepMode;
import com.pax.dal.entity.ENavigationKey;
import com.pax.dal.entity.EPiccType;
import com.pax.dal.entity.EReaderType;
import com.pax.dal.entity.EScannerType;
import com.pax.dal.entity.PollingResult;
import com.pax.dal.exceptions.IccDevException;
import com.pax.dal.exceptions.MagDevException;
import com.pax.dal.exceptions.PiccDevException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Device method
 */
public class Device {

    private static final String TAG = "Device";

    private Device() {
        //do nothing
    }

    /**
     * beep ok
     */
    public static void beepOk() {
        FinancialApplication.getDal().getSys().beep(EBeepMode.FREQUENCE_LEVEL_3, 100);
        FinancialApplication.getDal().getSys().beep(EBeepMode.FREQUENCE_LEVEL_4, 100);
        FinancialApplication.getDal().getSys().beep(EBeepMode.FREQUENCE_LEVEL_5, 100);
    }

    /**
     * beep error
     */
    public static void beepErr() {
        if (FinancialApplication.getDal() != null) {
            FinancialApplication.getDal().getSys().beep(EBeepMode.FREQUENCE_LEVEL_6, 200);
        }
    }

    /**
     * beep prompt
     */
    public static void beepPrompt() {
        FinancialApplication.getDal().getSys().beep(EBeepMode.FREQUENCE_LEVEL_6, 50);
    }

    /**
     * get formatted date/time
     *
     * @param pattern date format, e.g.{@link Constants#TIME_PATTERN_TRANS}
     * @return formatted data value
     */
    public static String getTime(String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.US);
        return dateFormat.format(new Date());
    }

    /**
     * enable/disable status bar
     *
     * @param enable true/false
     */
    public static void enableStatusBar(boolean enable) {
        FinancialApplication.getDal().getSys().enableStatusBar(enable);
    }

    /**
     * enable/disable home and recent key
     *
     * @param enable true/false
     */
    public static void enableHomeRecentKey(boolean enable) {
        FinancialApplication.getDal().getSys().enableNavigationKey(ENavigationKey.HOME, enable);
        FinancialApplication.getDal().getSys().enableNavigationKey(ENavigationKey.RECENT, enable);
    }

    /**
     * Sets picc led.
     *
     * @param index  the index
     * @param status the status
     */
    public static void setPiccLed(final @IntRange(from = -1, to = 3) int index, int status) {
        final IPicc picc = FinancialApplication.getDal().getPicc(EPiccType.INTERNAL);
        try {
            if (index >= 0 && status > 0) {
                picc.setLed((byte) (1 << (3 - index)));
            } else {
                picc.setLed((byte) 0);
            }
        } catch (PiccDevException e) {
            LogUtils.e(TAG, "", e);
        }
    }

    /**
     * Sets picc led with exception.
     *
     * @param index  the index
     * @param status the status
     * @throws PiccDevException the picc dev exception
     */
    public static void setPiccLedWithException(final @IntRange(from = -1, to = 3) int index, int status) throws PiccDevException {
        final IPicc picc = FinancialApplication.getDal().getPicc(EPiccType.INTERNAL);
        if (index >= 0 && status > 0) {
            picc.setLed((byte) (1 << (3 - index)));
        } else {
            picc.setLed((byte) 0);
        }
    }

    /**
     * remove card listener, for showing message which polling
     */
    public interface RemoveCardListener {
        /**
         * On show msg.
         *
         * @param result the result
         */
        void onShowMsg(PollingResult result);
    }

    /**
     * force to remove card with prompting message
     *
     * @param listener remove card listener
     */
    public static void removeCard(RemoveCardListener listener) {
        boolean needShow = true;
        ICardReaderHelper helper = FinancialApplication.getDal().getCardReaderHelper();

        try {
            PollingResult result;
            EReaderType readerType = EReaderType.ICC_PICC;
            if (ParamHelper.isClssExternalResult()){
                readerType = EReaderType.ICC_PICCEXTERNAL;
            }
            while ((result = helper.polling(readerType, 100)).getReaderType() == EReaderType.ICC || result.getReaderType() == EReaderType.PICC||result.getReaderType() == EReaderType.PICCEXTERNAL) {
                // remove card prompt
                if (listener != null && needShow) {
                    needShow = false;
                    listener.onShowMsg(result);
                }
                SystemClock.sleep(500);
                Device.beepErr();
            }
            SysParam.getInstance().set(R.string.RESULT_READER_TYPE, EReaderType.DEFAULT.ordinal());
        } catch (MagDevException | IccDevException | PiccDevException e) {
            //ignore the warning
        }
    }

    /**
     * 获取扫码器
     *
     * @return the scanner
     */
    public static IScanner getScanner() {
        return FinancialApplication.getDal().getScanner(EScannerType.REAR);
    }


    /**
     * 得到设备的型号
     *
     * @return device model
     */
    public static String getDeviceModel() {
        return Build.MODEL.toUpperCase(); //机器型号
    }
}
