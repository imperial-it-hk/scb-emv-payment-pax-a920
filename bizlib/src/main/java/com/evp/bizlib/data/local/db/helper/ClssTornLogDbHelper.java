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
 * 20190108  	         xieYb                   Create
 * ===========================================================================================
 */
package com.evp.bizlib.data.local.db.helper;

import com.evp.bizlib.data.entity.ClssTornLog;

/**
 * emv contactless tornlog database helper
 */
public class ClssTornLogDbHelper extends BaseDaoHelper {
    private static class LazyHolder {
        public static final ClssTornLogDbHelper INSTANCE = new ClssTornLogDbHelper(ClssTornLog.class);
    }

    /**
     * get singleton instance
     * @return ClssTornLogDbHelper
     */
    public static ClssTornLogDbHelper getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * init ClssTornLogDbHelper
     * @param entityClass entityClass
     */
    public ClssTornLogDbHelper(Class entityClass) {
        super(entityClass);
    }

}
