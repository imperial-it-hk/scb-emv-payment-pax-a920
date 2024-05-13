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

import com.evp.bizlib.data.entity.AcqIssuerRelation;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.local.db.dao.AcqIssuerRelationDao;
import com.evp.bizlib.data.local.db.dao.AcquirerDao;

import org.greenrobot.greendao.query.QueryBuilder;

/**
 * Database operation helper of Acquirer
 */
public class AcquirerDbHelper extends BaseDaoHelper {
    private static class LazyHolder {
        public static final AcquirerDbHelper INSTANCE = new AcquirerDbHelper(Acquirer.class);
    }

    public static AcquirerDbHelper getInstance() {
        return LazyHolder.INSTANCE;
    }

    public AcquirerDbHelper(Class entityClass) {
        super(entityClass);
    }

    /**
     * find Acquirer by name
     * @param acquirerName acquirerName
     * @return Acquirer
     */
    public final Acquirer findAcquirer(String acquirerName) {
        return (Acquirer) this.getNoSessionQuery()
                .where(AcquirerDao.Properties.Name.eq(acquirerName))
                .unique();
    }

    public final Acquirer findAcquirer(long issuerId) {
        QueryBuilder builder = this.getNoSessionQuery();
        builder.join(AcqIssuerRelation.class, AcqIssuerRelationDao.Properties.Acquirer_id)
                .where(AcqIssuerRelationDao.Properties.Issuer_id.eq(issuerId));
        return (Acquirer) builder.list().get(0);
    }
}
