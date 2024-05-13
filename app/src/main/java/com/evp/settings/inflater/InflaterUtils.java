/*
 *
 *  ============================================================================
 *  PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *  This software is supplied under the terms of a license agreement or nondisclosure
 *  agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *  disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2019 -? PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.
 *  Description:
 *  Revision History:
 *  Date	             Author	                Action
 *  20190418   	     ligq           	Create/Add/Modify/Delete
 *  ============================================================================
 *
 */

package com.evp.settings.inflater;

import com.evp.pay.utils.RxUtils;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;

/**
 * The type Inflater utils.
 *
 * @author ligq
 * @date 2019 /4/18 16:05
 */
public class InflaterUtils {
    /**
     * Init data.
     *
     * @param <T>      the type parameter
     * @param t        the t
     * @param listener the listener
     */
    public static <T> void initData(final T t, final InflaterListener<T> listener) {
        RxUtils.release();
        RxUtils.addDisposable(Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) {
                emitter.onNext(listener.init());
                emitter.onComplete();
            }
        }).compose(RxUtils.<Boolean>ioMain())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) {
                        if (aBoolean) {
                            listener.next(t);
                        }
                    }
                }));
    }

    /**
     * The interface Inflater listener.
     *
     * @param <T> the type parameter
     */
    public interface InflaterListener<T> {
        /**
         * Init boolean.
         *
         * @return the boolean
         */
        boolean init();

        /**
         * Next.
         *
         * @param t the t
         */
        void next(T t);
    }
}
