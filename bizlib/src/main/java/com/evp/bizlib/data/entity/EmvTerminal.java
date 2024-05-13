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
 * 20190108  	         Steven.W                Create
 * ===========================================================================================
 */
package com.evp.bizlib.data.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.evp.bizlib.data.local.GreendaoHelper;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;

import java.io.Serializable;
import java.util.List;

@Entity(nameInDb = "emv_terminal")
public class EmvTerminal implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String ID_FIELD_NAME = "id";

    @Id(autoincrement = true)
    @Property(nameInDb = ID_FIELD_NAME)
    @JSONField
    private Long id;

    //terminal type
    @NotNull
    private String terminalType;
    //terminal additional capabilities
    @NotNull
    private String terminalAdditionalCapabilities;
    //merchant category code
    @NotNull
    private String merchantCategoryCode;

    //terminal capabilities are moved to AID configuration

    @Generated(hash = 651916142)
    public EmvTerminal(Long id, @NotNull String terminalType,
            @NotNull String terminalAdditionalCapabilities,
            @NotNull String merchantCategoryCode) {
        this.id = id;
        this.terminalType = terminalType;
        this.terminalAdditionalCapabilities = terminalAdditionalCapabilities;
        this.merchantCategoryCode = merchantCategoryCode;
    }

    @Generated(hash = 999180818)
    public EmvTerminal() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTerminalType() {
        return this.terminalType;
    }

    public void setTerminalType(String terminalType) {
        this.terminalType = terminalType;
    }

    public String getTerminalAdditionalCapabilities() {
        return this.terminalAdditionalCapabilities;
    }

    public void setTerminalAdditionalCapabilities(String terminalAdditionalCapabilities) {
        this.terminalAdditionalCapabilities = terminalAdditionalCapabilities;
    }

    public String getMerchantCategoryCode() {
        return this.merchantCategoryCode;
    }

    public void setMerchantCategoryCode(String merchantCategoryCode) {
        this.merchantCategoryCode = merchantCategoryCode;
    }

    public static boolean load(List<EmvTerminal> emvTerminal) {
        return GreendaoHelper.getEmvTerminalHelper().insert(emvTerminal);
    }

    public static boolean deleteAllRecords() {
        return GreendaoHelper.getEmvTerminalHelper().deleteAll();
    }
}
