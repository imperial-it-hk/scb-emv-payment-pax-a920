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

import com.evp.bizlib.data.entity.EmvCapk;
import com.evp.bizlib.data.local.db.dao.EmvCapkDao;

/**
 * capk database helper
 */
public class EmvCapkDbHelper extends BaseDaoHelper {
    private static class LazyHolder {
        public static final EmvCapkDbHelper INSTANCE = new EmvCapkDbHelper(EmvCapk.class);
    }

    public static EmvCapkDbHelper getInstance() {
        return LazyHolder.INSTANCE;
    }

    public EmvCapkDbHelper(Class entityClass) {
        super(entityClass);
    }

    public final EmvCapk findCAPK(String rid) {
        if (rid == null || rid.isEmpty()) {
            return null;
        }
        return (EmvCapk) getNoSessionQuery().where(EmvCapkDao.Properties.RID.eq(rid)).unique();
    }
}
