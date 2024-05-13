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

import com.evp.bizlib.data.entity.CardBinBlack;
import com.evp.bizlib.data.local.db.dao.CardBinBlackDao;

import java.util.List;

public class CardBinBlackDbHelper extends BaseDaoHelper {
    private static class LazyHolder {
        public static final CardBinBlackDbHelper INSTANCE = new CardBinBlackDbHelper(CardBinBlack.class);
    }

    public static CardBinBlackDbHelper getInstance() {
        return LazyHolder.INSTANCE;
    }

    public CardBinBlackDbHelper(Class entityClass) {
        super(entityClass);
    }

    /**
     * check if the card is black
     * select * from card_bin_black where ? like "
     * + CardBin.BIN_FIELD_NAME + "||'%'", cardNo
     */
    public final boolean isBlack(String cardNo) {
        if (cardNo == null || cardNo.isEmpty()) {
            return false;
        }
        List list = getNoSessionQuery().where(CardBinBlackDao.Properties.Bin.like(String.format("%s%%", cardNo))).list();
        return !list.isEmpty();

    }
}
