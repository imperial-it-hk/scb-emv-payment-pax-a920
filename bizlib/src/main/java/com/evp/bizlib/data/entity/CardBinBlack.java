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
 * 20190108  	         Kim.L                   Create
 * ===========================================================================================
 */
package com.evp.bizlib.data.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Unique;

// for split black card bin table from the common table
@Entity(nameInDb = "card_bin_black")
public class CardBinBlack {
    public static final String ID_FIELD_NAME = "id";
    public static final String BIN_FIELD_NAME = "card_bin";


    @Id(autoincrement = true)
    @Property(nameInDb = ID_FIELD_NAME)
    protected Long id;
    // 卡号
    @Property(nameInDb = BIN_FIELD_NAME)
    @Unique
    protected String bin;
    protected int cardNoLen;
    @Generated(hash = 1079076009)
    public CardBinBlack(Long id, String bin, int cardNoLen) {
        this.id = id;
        this.bin = bin;
        this.cardNoLen = cardNoLen;
    }
    @Generated(hash = 1478329469)
    public CardBinBlack() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getBin() {
        return this.bin;
    }
    public void setBin(String bin) {
        this.bin = bin;
    }
    public int getCardNoLen() {
        return this.cardNoLen;
    }
    public void setCardNoLen(int cardNoLen) {
        this.cardNoLen = cardNoLen;
    }
}
