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
 * 20190108  	         Steven.S                Create
 * ===========================================================================================
 */

package com.evp.pay.utils.lightscanner.tool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.evp.commonlib.utils.LogUtils;
import com.evp.pay.utils.lightscanner.LightScannerActivity;

/**
 * Created by Steven.S on 2018/5/23/0023.
 */
public class LightScanner {
    /**
     * The constant SCAN_INTENT_ACTION.
     */
    public static final String SCAN_INTENT_ACTION = "android.intent.action.OpenLightScanner";
    /**
     * The constant FLAGS.
     */
    public static final String FLAGS = "FLAGS";
    /**
     * The constant QR_CODE_STR.
     */
    public static final String QR_CODE_STR = "QR_CODE_STR";
    /**
     * The constant TIMEOUT.
     */
    public static final String TIMEOUT = "TIMEOUT";
    /**
     * The constant SUCCESS_FLAG.
     */
    public static final int SUCCESS_FLAG = 0;
    /**
     * The constant CANCEL_FLAG.
     */
    public static final int CANCEL_FLAG = 1;
    /**
     * The constant ERROR_FLAG.
     */
    public static final int TIMEOUT_FLAG = 2;

    private Context context;
    private LightScannerListener listener;
    private int mTimeout = 2 * 60; //默认超时时间为2分钟
    private boolean isBroadcastRegistered = false;
    private String qrCodeStr;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(SCAN_INTENT_ACTION)){
                int flag = intent.getIntExtra(FLAGS, CANCEL_FLAG);
                switch (flag){
                    case SUCCESS_FLAG:
                        qrCodeStr = intent.getStringExtra(QR_CODE_STR);
                        LogUtils.i("LightScanner", "qrCode: " + qrCodeStr);
                        if(listener != null){
                            listener.onReadSuccess(qrCodeStr);
                        }
                        break;
                    case CANCEL_FLAG:
                        if(listener != null){
                            listener.onCancel();
                        }
                        break;
                    case TIMEOUT_FLAG:
                        if(listener != null){
                            listener.onTimeOut();
                        }
                        break;
                    default:
                        if(listener != null){
                            listener.onReadError();
                        }
                        break;
                }
            }
        }
    };

    /**
     * Instantiates a new Light scanner.
     *
     * @param context the context
     */
    public LightScanner(Context context){
        this.context = context;
    }

    /**
     * Instantiates a new Light scanner.
     *
     * @param context    the context
     * @param timeoutSec the timeout sec
     */
    public LightScanner(Context context, int timeoutSec){
        this.context = context;
        this.mTimeout = timeoutSec;
    }

    /**
     * The interface Light scanner listener.
     */
    public interface LightScannerListener{
        /**
         * On read success.
         *
         * @param result the result
         */
        void onReadSuccess(String result);

        /**
         * On read error.
         */
        void onReadError();

        /**
         * On cancel.
         */
        void onCancel();

        /**
         * on timeOut
         */
        void onTimeOut();
    }

    /**
     * Start.
     *
     * @param listener the listener
     */
    public void start(LightScannerListener listener){
        this.listener = listener;
    }

    /**
     * Open.
     */
    public void open(){
        Intent intent = new Intent(context, LightScannerActivity.class);
        intent.putExtra(TIMEOUT, mTimeout);
        context.startActivity(intent);

        registerOpenBroadcastReceiver();
    }

    /**
     * Close.
     */
    public void close(){
        unRegisterMyBroadcastReceiver();

        Intent intent = new Intent(LightScannerActivity.CLOSE_SCANNER_INTENT_ACTION);
        context.sendBroadcast(intent);
    }

    private void registerOpenBroadcastReceiver(){
        if(!isBroadcastRegistered){
            isBroadcastRegistered = !isBroadcastRegistered;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SCAN_INTENT_ACTION);
            context.registerReceiver(receiver, intentFilter);
        }
    }

    private void unRegisterMyBroadcastReceiver(){
        if(isBroadcastRegistered){
            isBroadcastRegistered = !isBroadcastRegistered;
            context.unregisterReceiver(receiver);
        }
    }
}
