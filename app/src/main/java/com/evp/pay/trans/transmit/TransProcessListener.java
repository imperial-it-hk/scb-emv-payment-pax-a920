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
package com.evp.pay.trans.transmit;

import com.evp.bizlib.data.entity.TransData;

/**
 * transaction process listener
 */
public interface TransProcessListener {
    /**
     * On show progress.
     *
     * @param message the message
     * @param timeout the timeout
     */
//show progress
    void onShowProgress(String message, int timeout);

    /**
     * On show warning.
     *
     * @param message the message
     * @param timeout the timeout
     */
//show warning
    void onShowWarning(String message, int timeout);

    /**
     * On update progress title.
     *
     * @param title the title
     */
//on update progress title
    void onUpdateProgressTitle(String title);

    /**
     * On hide progress.
     */
//hide progress
    void onHideProgress();

    /**
     * On show normal message int.
     *
     * @param message     the message
     * @param timeout     the timeout
     * @param confirmable the confirmable
     * @return the int
     */
//show normal message
    int onShowNormalMessage(String message, int timeout, boolean confirmable);

    /**
     * On show err message int.
     *
     * @param message     the message
     * @param timeout     the timeout
     * @param confirmable the confirmable
     * @return the int
     */
//show error message
    int onShowErrMessage(String message, int timeout, boolean confirmable);

    /**
     * On show err message int.
     *
     * @param title       the title
     * @param message     the message
     * @param timeout     the timeout
     * @param confirmable the confirmable
     * @return the int
     */
//show error message
    int onShowErrMessage(String title, String message, int timeout, boolean confirmable);

    /**
     * On input online pin int.
     *
     * @param transData the trans data
     * @return the int
     */
//on input online pin
    int onInputOnlinePin(TransData transData);

    /**
     * On show remove message.
     *
     * @param message     the message
     * @param timeout     the timeout
     * @param conformable the conformable
     */
//show remove message
    void onShowRemoveMessage(String message, int timeout, boolean conformable);

    /**
     * On hide message.
     */
//on hide message
    void onHideMessage();
}