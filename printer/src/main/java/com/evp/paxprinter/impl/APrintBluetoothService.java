/*
 *  * ===========================================================================================
 *  * = COPYRIGHT
 *  *          PAX Computer Technology(Shenzhen); CO., LTD PROPRIETARY INFORMATION
 *  *   This software is supplied under the terms of a license agreement or nondisclosure
 *  *   agreement with PAX Computer Technology(Shenzhen); CO., LTD and may not be copied or
 *  *   disclosed except in accordance with the terms in that agreement.
 *  *     Copyright (C); 2019-? PAX Computer Technology(Shenzhen); CO., LTD All rights reserved.
 *  * Description: // Detail description about the voidction of this module,
 *  *             // interfaces with the other modules, and dependencies.
 *  * Revision History:
 *  * Date                  Author	                 Action
 *  * 20200713  	         xieYb                   Modify
 *  * ===========================================================================================
 *
 */
package com.evp.paxprinter.impl;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.ConditionVariable;
import android.text.TextUtils;

import com.evp.commonlib.bluetooth.BluetoothManager;
import com.evp.commonlib.event.RxBus;
import com.evp.commonlib.sp.SharedPrefUtil;
import com.evp.paxprinter.IPrintService;
import com.evp.paxprinter.btscan.BluetoothDeviceListActivity;
import com.evp.paxprinter.constant.Constant;
import com.evp.poslib.print.entity.PrinterInfo;
import com.evp.poslib.print.exception.EPrinterException;
import com.evp.poslib.print.exception.PrinterException;
import com.evp.poslib.print.impl.ABtPrinter;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Bluetooth workflow for both BE and BP printer
 */
public abstract class APrintBluetoothService implements IPrintService {
    private CompositeDisposable disposable = new CompositeDisposable();
    ABtPrinter btPrinter;
    private int printResult = -1;
    protected PrinterException exception;
    protected Context context;
    protected Bitmap bitmap;
    protected String line;

    public void clearPreviousMac(Context context) {
        final SharedPrefUtil sharedPref = new SharedPrefUtil(context);
        sharedPref.putString(Constant.SP_PREVIOUS_CONNECTED_PRINTER, "");
    }

    /**
     * observe from BluetoothDeviceListActivity which will post the scan result
     *
     * @return scan result
     */
    protected Observable<PrinterInfo> scanResult() {
        return RxBus.getInstance().toObservable().map(new Function<Object, PrinterInfo>() {
            @Override
            public PrinterInfo apply(Object address) throws Exception {
                return (PrinterInfo) address;
            }
        });
    }

    /**
     * open bluetooth
     * @param context context
     * @return whether bluetooth opened
     */
    protected Observable<Boolean> openBT(final Context context) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                if (!BluetoothManager.getInstance(context).isBtEnabled()) {
                    BluetoothManager.getInstance(context).openBluetooth();
                }
                emitter.onNext(true);
            }
        });
    }

    /**
     * show a scan page to discovery printer device
     * @param context applicationContext
     * @return whether navigate to BluetoothDeviceListActivity,The reason why we use String,not Boolean
     * is to distinguish between connectBt and printBt.(because we will handle it together somewhere)
     */
    protected Observable<String> scanBT(final Context context) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                Intent intent = new Intent(context, BluetoothDeviceListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                emitter.onNext("scan bluetooth device");
            }
        }).subscribeOn(AndroidSchedulers.mainThread());
    }

    /**
     * connect device which use BE、BP Related technologies。
     *
     * @return whether connected
     */
    protected Observable<Boolean> connectBt() {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                emitter.onNext(btPrinter.connect());
            }
        }).subscribeOn(Schedulers.io());
    }

    /**
     * print bitmap
     * @return print result
     */
    protected Observable<Integer> printBt() {
        return Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                try {
                    printResult = printByType();
                    emitter.onNext(printResult);
                }catch (Throwable throwable){
                    Exceptions.propagate(throwable);
                }

            }
        });
    }

    /**
     * print singleLine
     *
     * @param singleLine singleLine text
     * @return print result
     */
    protected Observable<Integer> printBt(final String singleLine) {
        return Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                printResult = btPrinter.print(singleLine);
                emitter.onNext(printResult);
            }
        });
    }

    public void disConnect() {
        if (btPrinter != null) {
            btPrinter.disConnect();
        }
    }

    protected int print() throws PrinterException{
        exception = null;
        final SharedPrefUtil sharedPref = new SharedPrefUtil(context);
        final ConditionVariable cv = new ConditionVariable();
        //wait for scan result
        disposable.add(scanResult().flatMap(new Function<PrinterInfo, ObservableSource<Boolean>>() {
            @Override
            public ObservableSource<Boolean> apply(@NonNull PrinterInfo printerInfo) throws Exception {

                if (!TextUtils.isEmpty(printerInfo.getIdentifier())) {
                    sharedPref.putString(Constant.SP_PREVIOUS_CONNECTED_PRINTER, printerInfo.toString());
                } else {
                    //cancel in scan activity
                    throw Exceptions.propagate(new PrinterException(EPrinterException.PRINTER_CANCEL));
                }
                btPrinter = getBtPrinter(context, printerInfo);
                return connectBt();
            }
        }).flatMap(new Function<Boolean, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(Boolean connected) throws Exception {
                if (connected) {
                    try {
                        return printBt();
                    }catch (Throwable throwable){
                        throw Exceptions.propagate(throwable);
                    }

                } else {
                    return scanBT(context);
                }
            }
        }).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                if (o instanceof Integer) {
                    printResult = (int) o;
                    cv.open();
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws PrinterException {
                exception = (PrinterException) throwable.getCause();
                cv.open();
            }
        }));

        //normal bluetooth workflow
        disposable.add(openBT(context).flatMap(new Function<Boolean, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(Boolean btOpened) throws Exception {
                SharedPrefUtil sharedPref = new SharedPrefUtil(context);
                String previousPrinter = sharedPref.getString(Constant.SP_PREVIOUS_CONNECTED_PRINTER, "");
                if (!TextUtils.isEmpty(previousPrinter)) {
                    //fetch previous saved printer,if printer powered off,we need to scan for another available one.
                    int i = previousPrinter.indexOf("\n");
                    String address = previousPrinter.substring(previousPrinter.length() - 17);
                    boolean paired = BluetoothManager.getInstance(context).isPaired(address);
                    //if you want to connect to another printer,you can unPaired from settings.
                    if (paired) {
                        PrinterInfo printerInfo = new PrinterInfo(previousPrinter.substring(0, i), address);
                        btPrinter =getBtPrinter(context, printerInfo);
                        return connectBt();
                    } else {
                        return scanBT(context);
                    }

                } else {//no previous printer record.
                    return scanBT(context);
                }
            }
        }).flatMap(new Function<Object, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(Object o) throws Exception {
                if (o instanceof Boolean) {
                    boolean connect = (boolean) o;
                    if (connect) {
                        try {
                            return printBt();
                        }catch (Throwable throwable){
                            Exceptions.propagate(throwable);
                        }

                    } else {
                        return scanBT(context);
                    }
                }
                return Observable.just(o);
            }
        }).subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {
                if (o instanceof Integer) {
                    printResult = (int) o;
                    cv.open();
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                exception = (PrinterException) throwable.getCause();
                cv.open();
            }
        }));
        cv.block();
        disposable.clear();
        if ((exception != null) ) {
            throw exception;
        }
        return printResult;
    }

    /**
     * Gets Printer Service Type
     * @param context application context
     * @param printerInfo printerInfo
     * @return ABtPrinter
     */
    protected abstract ABtPrinter getBtPrinter(Context context,PrinterInfo printerInfo);

    /**
     * Gets Print Content Type,either bitmap or string
     * @return print result
     * @throws PrinterException PrinterException
     */
    protected abstract int printByType() throws PrinterException;

}
