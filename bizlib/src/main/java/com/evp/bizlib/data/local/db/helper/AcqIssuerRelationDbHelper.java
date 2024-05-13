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
import com.evp.bizlib.data.entity.Issuer;
import com.evp.bizlib.data.local.db.dao.AcqIssuerRelationDao;
import com.evp.commonlib.utils.LogUtils;

/**
 * RelationShip of Acquirer and Issuer
 */
public class AcqIssuerRelationDbHelper extends BaseDaoHelper {
    private static class LazyHolder {
        public static final AcqIssuerRelationDbHelper INSTANCE = new AcqIssuerRelationDbHelper(AcqIssuerRelation.class);
    }

    public static AcqIssuerRelationDbHelper getInstance() {
        return LazyHolder.INSTANCE;
    }

    public AcqIssuerRelationDbHelper(Class entityClass) {
        super(entityClass);
    }

    /**
     * binRelationShip of Acquirer and Issuer
     * @param acquirer Acquirer
     * @param issuer Issuer
     * @return status
     */
    public final boolean bindAcqAndIssuer(Acquirer acquirer, Issuer issuer) {

        boolean success;
        try {
            AcqIssuerRelation relation = this.findRelation(acquirer, issuer);
            if (relation == null) {
                insert(new AcqIssuerRelation(acquirer, issuer));
            }
            success = true;
        } catch (Exception var5) {
            LogUtils.e(TAG, var5);
            success = false;
        }
        return success;
    }

    /**
     * find relationship between Acquirer and Issuer
     * @param acquirer Acquirer
     * @param issuer Issuer
     * @return AcqIssuerRelation
     */
    public final AcqIssuerRelation findRelation(Acquirer acquirer, Issuer issuer) {
        return (AcqIssuerRelation) this.getNoSessionQuery()
                .where(AcqIssuerRelationDao.Properties.Acquirer_id.eq(acquirer.getId()), AcqIssuerRelationDao.Properties.Issuer_id.eq(issuer.getId()))
                .unique();
    }
}
