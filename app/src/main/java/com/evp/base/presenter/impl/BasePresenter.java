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
package com.evp.base.presenter.impl;

import android.content.Context;

import com.evp.base.presenter.IPresenter;
import com.evp.base.view.IView;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by zhangyp on 2019/4/18
 *
 * @param <V> the type parameter
 */
public class BasePresenter<V extends IView> implements IPresenter<V> {
    private WeakReference<V> weakView;
    private WeakReference<Context> weakConText;

    /**
     * Instantiates a new Base presenter.
     *
     * @param context the context
     */
    public BasePresenter(Context context) {
        this.weakConText = new WeakReference(context);
    }

    /**
     * Gets context.
     *
     * @return the context
     */
    public Context getContext() {
        return this.weakConText.get();
    }

    /**
     * The Proxy view.
     */
    public V proxyView;

    @Override
    public void attachView(V view) {
        weakView = new WeakReference<V>(view);
        if (this.weakView != null) {
            V v = (V) weakView.get();
            if (v != null) {
                InvocationHandler handler = new ViewInvocationHandler(v);
                proxyView = (V) Proxy.newProxyInstance(view.getClass().getClassLoader(), view.getClass().getInterfaces(), handler);
            }
        }
    }

    @Override
    public void detachView() {
        if (weakView != null) {
            weakView.clear();
            weakView = null;
        }
    }

    private class ViewInvocationHandler implements InvocationHandler {
        private IView view;

        /**
         * Instantiates a new View invocation handler.
         *
         * @param view the view
         */
        public ViewInvocationHandler(IView view) {
            this.view = view;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (isAttachView()) {
                return method.invoke(view,args);
            } else {
                return null;
            }
        }

        private boolean isAttachView() {
            return weakView != null && weakView.get() != null;
        }
    }
}
