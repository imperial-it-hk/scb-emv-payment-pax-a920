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
package com.evp.base.presenter;

import com.evp.base.view.IView;

/**
 * Created by zhangyp on 2019/4/18
 *
 * @param <V> the type parameter
 */
public interface IPresenter<V extends IView> {
    /**
     * Attach view.
     *
     * @param view the view
     */
    void attachView(V view);

    /**
     * Detach view.
     */
    void detachView();
}
