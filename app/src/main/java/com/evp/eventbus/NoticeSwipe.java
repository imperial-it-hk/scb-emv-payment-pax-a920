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
package com.evp.eventbus;

/**
 * Created by zhangyp on 2019/4/18
 */
public class NoticeSwipe {
    /**
     * The Notice msg.
     */
    public String noticeMsg;

    /**
     * Instantiates a new Notice swipe.
     *
     * @param noticeMsg the notice msg
     */
    public NoticeSwipe(String noticeMsg) {
        this.noticeMsg=noticeMsg;
    }

    /**
     * The constant FUNC_SEARCH_CLOSED.
     */
    public static final int FUNC_SEARCH_CLOSED = 102;
}
