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
 * 20190621  	         xieYb                   Create
 * ===========================================================================================
 */
package com.evp.bizlib.data.local.db.helper.upgrade.history;

import android.database.SQLException;

import com.evp.bizlib.data.local.db.dao.AcqIssuerRelationDao;
import com.evp.bizlib.data.local.db.dao.AcquirerDao;
import com.evp.bizlib.data.local.db.dao.CardBinDao;
import com.evp.bizlib.data.local.db.dao.CardRangeDao;
import com.evp.bizlib.data.local.db.dao.ClssTornLogDao;
import com.evp.bizlib.data.local.db.dao.EmvAidDao;
import com.evp.bizlib.data.local.db.dao.EmvCapkDao;
import com.evp.bizlib.data.local.db.dao.IssuerDao;
import com.evp.bizlib.data.local.db.dao.TransDataDao;
import com.evp.bizlib.data.local.db.dao.TransTotalDao;
import com.evp.bizlib.data.local.db.helper.upgrade.DbUpgrade;
import com.evp.commonlib.utils.LogUtils;

import org.greenrobot.greendao.database.Database;

/**
 * update database from version 4 to 5
 */
public class Upgrade4To5 extends DbUpgrade {

    @Override
    protected void upgrade(Database db)  {
        try {
            DbUpgrade.upgradeTable(db, AcquirerDao.class,
                    IssuerDao.class,
                    AcqIssuerRelationDao.class,
                    CardBinDao.class,
                    CardRangeDao.class,
                    ClssTornLogDao.class,
                    EmvAidDao.class,
                    EmvCapkDao.class,
                    TransDataDao.class,
                    TransTotalDao.class);

        } catch (SQLException e) {
            LogUtils.e(TAG,e);
        }
    }
}
