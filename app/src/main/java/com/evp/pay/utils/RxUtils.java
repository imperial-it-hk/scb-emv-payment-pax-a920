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
 * 20190108  	         ligq                    Create
 * ===========================================================================================
 */
package com.evp.pay.utils;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * The type Rx utils.
 */
public class RxUtils {
    private static CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    /**
     * this method actually executes the runnable in main thread which is not expected
     *
     * @param runnable the runnable
     */
    @Deprecated
    public static void runInThread(final Runnable runnable) {
        mCompositeDisposable.add(Observable.just(0).compose(RxUtils.<Integer>ioThread()).subscribe(new Consumer() {
            @Override
            public void accept(Object o) throws Exception {
                runnable.run();
            }
        }));
    }

    /**
     * execute a runnable in new thread
     *
     * @param runnable the runnable to be executed in new thread
     */
    public static void runInBackgroud(final Runnable runnable) {
        mCompositeDisposable.add(Observable.just(0).subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).subscribe(new Consumer() {
            @Override
            public void accept(Object o) throws Exception {
                runnable.run();
            }
        }));
    }


    /**
     * Io thread observable transformer.
     *
     * @param <T> the type parameter
     * @return the observable transformer
     */
    public static <T> ObservableTransformer<T, T> ioThread() {
        return new ObservableTransformer<T, T>() {

            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                upstream.subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread());
                return upstream;
            }
        };
    }

    /**
     * Io main observable transformer.
     *
     * @param <T> the type parameter
     * @return the observable transformer
     */
    public static <T> ObservableTransformer<T, T> ioMain() {
        return new ObservableTransformer<T, T>() {

            @Override
            public ObservableSource<T> apply(Observable<T> upstream) {
                upstream.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
                return upstream;
            }
        };
    }


    /**
     * Add disposable.
     *
     * @param disposable the disposable
     */
    public static void addDisposable(Disposable disposable) {
        mCompositeDisposable.add(disposable);
    }

    /**
     * Release.
     */
    public static void release() {
        if (!mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.clear();
        }
    }
}
