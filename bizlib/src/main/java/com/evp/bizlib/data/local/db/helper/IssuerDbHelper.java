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
import com.evp.bizlib.data.local.db.dao.IssuerDao;

import java.util.ArrayList;
import java.util.List;

/**
 * Database operation helper of Issuer
 */
public class IssuerDbHelper extends BaseDaoHelper {
    private static class LazyHolder {
        public static final IssuerDbHelper INSTANCE = new IssuerDbHelper(Issuer.class);
    }

    public static IssuerDbHelper getInstance() {
        return LazyHolder.INSTANCE;
    }

    public IssuerDbHelper(Class entityClass) {
        super(entityClass);
    }

    public final Issuer findIssuer(String issuerName) {
        if (issuerName == null || issuerName.isEmpty()) {
            return null;
        }
        return (Issuer) getNoSessionQuery().where(IssuerDao.Properties.Name.eq(issuerName)).unique();
    }

    public final Issuer findIssuerByIssuerId(long issuerId) {
        return (Issuer) getNoSessionQuery().where(IssuerDao.Properties.Id.eq(issuerId)).unique();
    }

    public final List lookupIssuersForAcquirer(Acquirer acquirer) {
        ArrayList<Issuer> issuerList = new ArrayList<Issuer>();
        List<AcqIssuerRelation> list = getDaoSession().getAcqIssuerRelationDao()
                .queryBuilder()
                .where(AcqIssuerRelationDao.Properties.Acquirer_id.eq(acquirer.getId()))
                .list();
        if (list.isEmpty()) {
            return issuerList;
        }
        for (AcqIssuerRelation item : list) {
            issuerList.add(item.getIssuer());
        }
        return issuerList;
    }

}
