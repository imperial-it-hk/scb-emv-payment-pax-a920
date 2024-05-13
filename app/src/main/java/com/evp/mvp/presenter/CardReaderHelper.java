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
 * Date	                 Author	                Action
 * 20200921  	         xieYb                  Create
 * ===========================================================================================
 */
package com.evp.mvp.presenter;

import android.os.SystemClock;
import android.text.TextUtils;

import androidx.annotation.MainThread;

import com.evp.bizlib.data.model.SearchMode;
import com.evp.commonlib.utils.LogUtils;
import com.evp.device.Device;
import com.evp.eventbus.CardDetectEvent;
import com.evp.eventbus.NoticeSwipe;
import com.evp.pay.app.FinancialApplication;
import com.evp.payment.evpscb.R;
import com.evp.settings.SysParam;
import com.pax.dal.IIcc;
import com.pax.dal.IMag;
import com.pax.dal.IPicc;
import com.pax.dal.entity.EDetectMode;
import com.pax.dal.entity.EPiccType;
import com.pax.dal.entity.EReaderType;
import com.pax.dal.entity.PiccCardInfo;
import com.pax.dal.entity.TrackData;
import com.pax.dal.exceptions.IccDevException;
import com.pax.dal.exceptions.MagDevException;
import com.pax.dal.exceptions.PiccDevException;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@MainThread
public class CardReaderHelper{
    private static final String TAG = "CardReaderHelper";
    private IIcc icc = FinancialApplication.getDal().getIcc();
    private IMag mag = FinancialApplication.getDal().getMag();
    private IPicc internalPicc = FinancialApplication.getDal().getPicc(EPiccType.INTERNAL);
    private IPicc exPicc = FinancialApplication.getDal().getPicc(EPiccType.EXTERNAL);
    private boolean isSupportMag = false;
    private boolean isSupportIcc = false;
    private boolean isSupportInternalPicc = false;
    private boolean isSupportExPicc = false;

    private boolean isMagDisabled = false;
    private boolean isIccDisabled = false;
    private boolean isInternalPiccDisabled = false;

    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private AtomicBoolean isSwiped = new AtomicBoolean(false);
    private AtomicInteger detectResult = new AtomicInteger(-1);
    private byte readType;

    private List<String> DEVICE_MAG_ICC_CONFLICT = Arrays.asList("A60", "ARIES6", "ARIES8");
    private static final int ICC_UNEXPECTED_ERROR = 97;
    public void polling(byte readType){
        this.readType = readType;
        isRunning.set(true);
        isSwiped.set(false);
        detectResult.set(-1);
        startDetect(readType);
    }

    private void startDetect(byte readType) {
        isSupportMag = SearchMode.isSupportMag(readType);
        isSupportIcc = SearchMode.isSupportIcc(readType);
        isSupportExPicc = SearchMode.isSupportExternalPicc(readType);
        isSupportInternalPicc = SearchMode.isSupportInternalPicc(readType);
        if (isSupportMag || isSupportIcc || isSupportExPicc){
            FinancialApplication.getApp().runInBackground(new DetectMagIccExPicc());
        }
        if (isSupportInternalPicc){
            FinancialApplication.getApp().runInBackground(new DetectInternalPicc());
        }
    }


    private class DetectMagIccExPicc implements Runnable {

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            boolean thisIsFallback = false;
            if (isSupportMag){
                try {
                    mag.close();
                    mag.open();
                    mag.reset();
                } catch (MagDevException e) {
                    FinancialApplication.getApp().doEvent(new NoticeSwipe(String.format("%s%s", e.getErrModule(), e.getErrMsg())));
                    LogUtils.e(e);
                }
            }
            if (isSupportExPicc){
                try {
                    exPicc.close();
                    exPicc.open();
                } catch (PiccDevException e) {
                    FinancialApplication.getApp().doEvent(new NoticeSwipe(String.format("%s%s", e.getErrModule(), e.getErrMsg())));
                    LogUtils.e(TAG, e);
                }
            }
            if (isSupportIcc){
                try {
                    icc.close((byte) 0);
                } catch (IccDevException e) {
                    FinancialApplication.getApp().doEvent(new NoticeSwipe(String.format("%s%s", e.getErrModule(), e.getErrMsg())));
                    LogUtils.e(TAG, e);
                }
            }
            while (isRunning.get()){
                if (isSupportMag && !isMagDisabled){
                    try {
                        if (mag.isSwiped()){
                            isSwiped.set(true);
                            TrackData trackData = mag.read();
                            if (TextUtils.isEmpty(trackData.getTrack1()) && TextUtils.isEmpty(trackData.getTrack2()) && TextUtils.isEmpty(trackData.getTrack3())) {
                                LogUtils.i(TAG, "track data is null");
                                continue;
                            }
                            if (isRunning.get()){
                                isRunning.set(false);
                                FinancialApplication.getApp().doEvent(CardDetectEvent.onMagDetected(trackData.getTrack1(),trackData.getTrack2(),trackData.getTrack3(),thisIsFallback));
                                detectResult.set(SearchMode.SWIPE);
                                SysParam.getInstance().set(R.string.RESULT_READER_TYPE, EReaderType.MAG.ordinal());
                                break;
                            }
                        }
                    } catch (MagDevException e) {
                        LogUtils.e(TAG, e);
                        //close the mag function from pax store
                        if (e.getErrCode() == NoticeSwipe.FUNC_SEARCH_CLOSED) {
                            isMagDisabled = true;
                            //here needn't to update isSupportMag as false for the final mag close.
                            FinancialApplication.getApp().doEvent(new NoticeSwipe("MAG_Disabled"));
                        }
                    }
                }
                if (isSupportIcc && !isIccDisabled){
                    try {
                        if (icc.detect((byte) 0)){
                            byte[] res = icc.init((byte) 0);
                            if (res != null){
                                isRunning.set(false);
                                FinancialApplication.getApp().doEvent(CardDetectEvent.onIcDetected());
                                detectResult.set(SearchMode.INSERT);
                                SysParam.getInstance().set(R.string.RESULT_READER_TYPE, EReaderType.ICC.ordinal());
                                break;
                            }else {
                                LogUtils.w(TAG,"Failed to Reset IC card and return the content of answer to reset");
                                //notice to insert again or swipe
                                if (DEVICE_MAG_ICC_CONFLICT.contains(Device.getDeviceModel().toUpperCase())) {
                                    FinancialApplication.getApp().doEvent(new NoticeSwipe(FinancialApplication.getApp().getResources().getString(R.string.icc_error_swipe_card)));
                                }
                            }
                        }
                    } catch (IccDevException e) {
                        LogUtils.e(e);
                        if (e.getErrCode() == ICC_UNEXPECTED_ERROR) {
                            LogUtils.w(TAG,"IC card unExcepted exception,for example,Card inserted upside down");
                            thisIsFallback = true;
                            FinancialApplication.getApp().doEvent(new NoticeSwipe(FinancialApplication.getApp().getResources().getString(R.string.icc_error_swipe_card)));
                        }
                        //close icc function from pax store
                        if (e.getErrCode() == NoticeSwipe.FUNC_SEARCH_CLOSED) {
                            isIccDisabled = true;
                            //here needn't to update isSupportIcc as false for the final ic close.
                            FinancialApplication.getApp().doEvent(new NoticeSwipe("ICC_Disabled"));
                        }
                    }
                }
                if (isSupportExPicc){
                    try {
                        PiccCardInfo exPiccInfo = exPicc.detect(EDetectMode.EMV_AB);
                        if (exPiccInfo != null){
                            isRunning.set(false);
                            FinancialApplication.getApp().doEvent(CardDetectEvent.onExPiccDetected(exPiccInfo.getSerialInfo()));
                            detectResult.set(SearchMode.EXTERNAL_WAVE);
                            SysParam.getInstance().set(R.string.RESULT_READER_TYPE, EReaderType.PICCEXTERNAL.ordinal());
                            break;
                        }
                    } catch (PiccDevException e) {
                        LogUtils.e(TAG,e);
                    }
                }
            }
            if (isSupportMag) {
                try {
                    LogUtils.d(TAG, "==============mag.close();=================");
                    mag.close();
                } catch (Exception e) {
                    LogUtils.e(TAG, e);
                }
            }

            if (isSupportIcc && detectResult.get() != SearchMode.INSERT) {
                try {
                    LogUtils.d(TAG, "==============icc.close();=================");
                    icc.close((byte) 0);
                } catch (Exception e) {
                    LogUtils.e(TAG, e);
                }
            }

            if (isSupportExPicc && detectResult.get() != SearchMode.EXTERNAL_WAVE) {

                try {
                    LogUtils.d(TAG, "==============piccExternal.close();=================");
                    exPicc.close();
                } catch (Exception e) {
                    LogUtils.e(TAG, e);
                }
            }
        }
    }

    private class DetectInternalPicc implements Runnable {
        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            try {
                internalPicc.close();
                LogUtils.d(TAG,"internalPicc ready to open");
                internalPicc.open();
            } catch (PiccDevException e) {
                FinancialApplication.getApp().doEvent(new NoticeSwipe(String.format("%s%s", e.getErrModule(), e.getErrMsg())));
                LogUtils.e(TAG, e);
            }
            while (isRunning.get() && !isInternalPiccDisabled){
                try {
                    SystemClock.sleep(1000); //Let it be here so MAG will get chance to be detected
                    PiccCardInfo internalPiccInfo = internalPicc.detect(EDetectMode.EMV_AB);
                    if (internalPiccInfo != null && !isSwiped.get()){
                        isRunning.set(false);
                        FinancialApplication.getApp().doEvent(CardDetectEvent.onInternalPiccDetected(internalPiccInfo.getSerialInfo()));
                        detectResult.set(SearchMode.INTERNAL_WAVE);
                        SysParam.getInstance().set(R.string.RESULT_READER_TYPE, EReaderType.PICC.ordinal());
                        break;
                    }
                } catch (PiccDevException e) {
                    if (e.getErrCode() == NoticeSwipe.FUNC_SEARCH_CLOSED) {
                        isInternalPiccDisabled = true;
                        //here needn't to update isSupportInternalPicc as false for the final picc close.
                        FinancialApplication.getApp().doEvent(new NoticeSwipe("PICC_Disabled"));
                    }else{
                        FinancialApplication.getApp().doEvent(new NoticeSwipe(e.getErrMsg()));
                    }
                    LogUtils.e(TAG,e);
                }
            }
            //close internal picc except when the contactless card is detected
            if ( isSupportInternalPicc && detectResult.get() != SearchMode.INTERNAL_WAVE) {

                try {
                    LogUtils.d(TAG, "==============piccInternal.close();=================");
                    internalPicc.close();
                } catch (Exception e) {
                    LogUtils.e(TAG, e.getMessage());
                }
            }
        }
    }

    /**
     * stop polling,all the reader will be closed except the the mode saved in detectResult
     * for example,if the detectResult is SearchMode.INSERT,that means IC card has been detected,
     * it needed to stay open until emv process finished,so don't forget to close IC after emv process
     * finished,PICC is the same.
     */
    public void stopPolling() {
        isRunning.set(false);
    }
}
