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

import com.evp.bizlib.data.entity.EmvAid;
import com.evp.bizlib.data.local.db.dao.EmvAidDao;

/**
 * Database operation helper of EmvAid
 */
public class EmvAidDbHelper extends BaseDaoHelper {
    private static class LazyHolder {
        public static final EmvAidDbHelper INSTANCE = new EmvAidDbHelper(EmvAid.class);
    }

    public static EmvAidDbHelper getInstance() {
        return LazyHolder.INSTANCE;
    }

    public EmvAidDbHelper(Class entityClass) {
        super(entityClass);
    }

    /**
     * find EmvAid by aid
     * @param aid aid
     * @return EmvAid
     */
    public final EmvAid findAID(String aid) {
        if (aid == null || aid.isEmpty()) {
            return null;
        }
        return (EmvAid) getNoSessionQuery().where(EmvAidDao.Properties.Aid.eq(aid)).unique();
    }

}
