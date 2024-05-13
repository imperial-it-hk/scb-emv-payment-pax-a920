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

import com.evp.bizlib.data.entity.CardRange;
import com.evp.bizlib.data.entity.Issuer;
import com.evp.bizlib.data.local.db.dao.CardRangeDao;

import java.util.List;

public class CardRangeDbHelper extends BaseDaoHelper {
    private static class LazyHolder {
        public static final CardRangeDbHelper INSTANCE = new CardRangeDbHelper(CardRange.class);
    }

    public static CardRangeDbHelper getInstance() {
        return LazyHolder.INSTANCE;
    }

    public CardRangeDbHelper(Class entityClass) {
        super(entityClass);
    }

    public final CardRange findCardRange(Long lowLimit, Long highLimit) {
        return (CardRange) getNoSessionQuery().where(CardRangeDao.Properties.PanRangeLow.eq(lowLimit),
                CardRangeDao.Properties.PanRangeHigh.eq(highLimit))
                .unique();
    }

    /**
     * WHERE (low <= ? AND high >= ?) @1
     * WHERE (length = 0 OR ? = length) @2
     * WHERE @1 AND @2
     * order by (high - low)
     */
    public final CardRange findCardRange(String pan) {
        String subPan = pan.substring(0, 10);
        List list = getNoSessionQuery().where(CardRangeDao.Properties.PanRangeLow.le(subPan),
                CardRangeDao.Properties.PanRangeHigh.ge(subPan))
                .whereOr(CardRangeDao.Properties.PanLength.eq(0)
                        , CardRangeDao.Properties.PanLength.eq(pan.length()))
                .orderRaw(CardRangeDao.Properties.PanRangeHigh.columnName + "-" + CardRangeDao.Properties.PanRangeLow.columnName)
                .list();
        if (list != null && !list.isEmpty()) {
            return (CardRange) list.get(0);
        }
        return null;
    }

    public final CardRange findTpnCardRange(String pan) {
        String subPan = pan.substring(0, 10);
        List list = getNoSessionQuery().where(CardRangeDao.Properties.PanRangeLow.le(subPan),
                CardRangeDao.Properties.PanRangeHigh.ge(subPan))
                .where(CardRangeDao.Properties.Name.eq("TPN"))
                .whereOr(CardRangeDao.Properties.PanLength.eq(0)
                        , CardRangeDao.Properties.PanLength.eq(pan.length()))
                .orderRaw(CardRangeDao.Properties.PanRangeHigh.columnName + "-" + CardRangeDao.Properties.PanRangeLow.columnName)
                .list();
        if (list != null && !list.isEmpty()) {
            return (CardRange) list.get(0);
        }
        return null;
    }

    public final CardRange findTscCardRange(String pan) {
        String subPan = pan.substring(0, 10);
        List list = getNoSessionQuery().where(CardRangeDao.Properties.PanRangeLow.le(subPan),
                CardRangeDao.Properties.PanRangeHigh.ge(subPan))
                .where(CardRangeDao.Properties.Name.eq("TSC"))
                .whereOr(CardRangeDao.Properties.PanLength.eq(0)
                        , CardRangeDao.Properties.PanLength.eq(pan.length()))
                .orderRaw(CardRangeDao.Properties.PanRangeHigh.columnName + "-" + CardRangeDao.Properties.PanRangeLow.columnName)
                .list();
        if (list != null && !list.isEmpty()) {
            return (CardRange) list.get(0);
        }
        return null;
    }

    public final List<CardRange> findCardRange(Issuer issuer) {
        return getNoSessionQuery().where(CardRangeDao.Properties.Issuer_id.eq(issuer.getId())).list();
    }

}
