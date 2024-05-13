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

import com.evp.bizlib.data.entity.CardBin;
import com.evp.bizlib.data.local.db.dao.CardBinDao;

import java.util.List;

/**
 * card bin database helper
 */
public class CardBinDbHelper extends BaseDaoHelper {
    private static class LazyHolder {
        public static final CardBinDbHelper INSTANCE = new CardBinDbHelper(CardBin.class);
    }

    /**
     * Get singleton instance
     * @return CardBinDbHelper
     */
    public static CardBinDbHelper getInstance() {
        return LazyHolder.INSTANCE;
    }

    public CardBinDbHelper(Class entityClass) {
        super(entityClass);
    }

    /**
     * check specific card number is in the card bin table
     * @param cardNo cardNo
     * @return check result
     */
    public final boolean isInCardBinTable(String cardNo) {
        if (cardNo == null || cardNo.isEmpty()) {
            return false;
        }
        List list = getNoSessionQuery().where(CardBinDao.Properties.Bin.like(String.format("%s%%", cardNo))).list();
        return !list.isEmpty();
    }
}
