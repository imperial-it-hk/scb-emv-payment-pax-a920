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

import androidx.annotation.NonNull;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;
import java.util.Calendar;

/**
 * issuer table
 */
@Entity(nameInDb = "issuer")
public class Issuer implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ID_FIELD_NAME = "issuer_id";
    public static final String NAME_FIELD_NAME = "issuer_name";

    /**
     * id
     */
    @Id(autoincrement = true)
    @Property(nameInDb = ID_FIELD_NAME)
    private Long id;

    /**
     * name
     */
    @Property(nameInDb = NAME_FIELD_NAME)
    @Unique
    private String name;

    private long nonEmvTranFloorLimit;

    private float adjustPercent;

    private String panMaskPattern;

    private boolean isEnableAdjust = true;

    private boolean isEnableOffline = true;

    private boolean isEnableRefund = true;

    private boolean isAllowExpiry = true;

    private boolean isAllowManualPan = true;

    private boolean isAllowCheckExpiry = false;

    private boolean isAllowPrint = true;

    private boolean isAllowCheckPanMod10 = true;

    private boolean nonEmvTranRequirePIN = true;

    private boolean isRequireMaskExpiry = true;

    private String bindToAcquirer;

    private long smallAmtLimit;

    public Issuer() {
    }

    public Issuer(String name) {
        this.setName(name);
    }

    public Issuer(Long id, String name) {
        this.setId(id);
        this.setName(name);
    }

    @Generated(hash = 1544024995)
    public Issuer(Long id, String name, long nonEmvTranFloorLimit, float adjustPercent, String panMaskPattern, boolean isEnableAdjust,
            boolean isEnableOffline, boolean isEnableRefund, boolean isAllowExpiry, boolean isAllowManualPan,
            boolean isAllowCheckExpiry, boolean isAllowPrint, boolean isAllowCheckPanMod10, boolean nonEmvTranRequirePIN,
            boolean isRequireMaskExpiry, String bindToAcquirer, long smallAmtLimit) {
        this.id = id;
        this.name = name;
        this.nonEmvTranFloorLimit = nonEmvTranFloorLimit;
        this.adjustPercent = adjustPercent;
        this.panMaskPattern = panMaskPattern;
        this.isEnableAdjust = isEnableAdjust;
        this.isEnableOffline = isEnableOffline;
        this.isEnableRefund = isEnableRefund;
        this.isAllowExpiry = isAllowExpiry;
        this.isAllowManualPan = isAllowManualPan;
        this.isAllowCheckExpiry = isAllowCheckExpiry;
        this.isAllowPrint = isAllowPrint;
        this.isAllowCheckPanMod10 = isAllowCheckPanMod10;
        this.nonEmvTranRequirePIN = nonEmvTranRequirePIN;
        this.isRequireMaskExpiry = isRequireMaskExpiry;
        this.bindToAcquirer = bindToAcquirer;
        this.smallAmtLimit = smallAmtLimit;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getNonEmvTranFloorLimit() {
        return nonEmvTranFloorLimit;
    }

    public void setNonEmvTranFloorLimit(long nonEmvTranFloorLimit) {
        this.nonEmvTranFloorLimit = nonEmvTranFloorLimit;
    }

    public float getAdjustPercent() {
        return adjustPercent;
    }

    public void setAdjustPercent(float adjustPercent) {
        this.adjustPercent = adjustPercent;
    }

    public String getPanMaskPattern() {
        return panMaskPattern;
    }

    public void setPanMaskPattern(String panMaskPattern) {
        this.panMaskPattern = panMaskPattern;
    }

    public boolean isEnableAdjust() {
        return isEnableAdjust;
    }

    public void setEnableAdjust(boolean enableAdjust) {
        isEnableAdjust = enableAdjust;
    }

    public boolean isEnableOffline() {
        return isEnableOffline;
    }

    public void setEnableOffline(boolean enableOffline) {
        isEnableOffline = enableOffline;
    }

    public boolean isAllowExpiry() {
        return isAllowExpiry;
    }

    public void setAllowExpiry(boolean allowExpiry) {
        isAllowExpiry = allowExpiry;
    }

    public boolean isAllowManualPan() {
        return isAllowManualPan;
    }

    public void setAllowManualPan(boolean allowManualPan) {
        isAllowManualPan = allowManualPan;
    }

    public boolean isAllowCheckExpiry() {
        return isAllowCheckExpiry;
    }

    public void setAllowCheckExpiry(boolean allowCheckExpiry) {
        isAllowCheckExpiry = allowCheckExpiry;
    }

    public boolean isAllowPrint() {
        return isAllowPrint;
    }

    public void setAllowPrint(boolean allowPrint) {
        isAllowPrint = allowPrint;
    }

    public boolean isAllowCheckPanMod10() {
        return isAllowCheckPanMod10;
    }

    public void setAllowCheckPanMod10(boolean allowCheckPanMod10) {
        isAllowCheckPanMod10 = allowCheckPanMod10;
    }

    public boolean isNonEmvTranRequirePIN() {
        return nonEmvTranRequirePIN;
    }

    public void setNonEmvTranRequirePIN(boolean nonEmvTranRequirePIN) {
        this.nonEmvTranRequirePIN = nonEmvTranRequirePIN;
    }

    public boolean isRequireMaskExpiry() {
        return isRequireMaskExpiry;
    }

    public void setRequireMaskExpiry(boolean requireMaskExpiry) {
        isRequireMaskExpiry = requireMaskExpiry;
    }

    public static boolean validPan(final Issuer issuer, String pan) {
        if (!issuer.isAllowCheckPanMod10()) {
            return true;
        }

        boolean flag = false;
        int result = 0;
        for (int i = (pan.length() - 1); i >= 0; --i) {
            int tmp = pan.charAt(i) & 15;
            if (flag) {
                tmp *= 2;
            }
            if (tmp > 9) {
                tmp -= 9;
            }
            result = (tmp + result) % 10;
            flag = !flag;
        }

        return result == 0;
    }

    public static boolean validCardExpiry(final Issuer issuer, String date) {
        if (!issuer.isAllowExpiry() || !issuer.isAllowCheckExpiry()) {
            return true;
        }
        Calendar now = Calendar.getInstance();

        int year = Integer.parseInt(date.substring(0, 2));
        year += year > 80 ? 1900 : 2000;
        int month = Integer.parseInt(date.substring(2, 4));

        return !(year < now.get(Calendar.YEAR) ||
                (year <= now.get(Calendar.YEAR) && month < now.get(Calendar.MONTH)+1));
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && getClass() == obj.getClass() && ((Issuer) obj).getName().equals(getName());
    }

    @Override
    public int hashCode() {
        return 17 * id.hashCode() + name.hashCode();
    }

    public void update(@NonNull Issuer issuer) {
        adjustPercent = issuer.getAdjustPercent();
        nonEmvTranFloorLimit = issuer.getNonEmvTranFloorLimit();
        panMaskPattern = issuer.getPanMaskPattern();
        isAllowCheckExpiry = issuer.isAllowCheckExpiry;
        isAllowCheckPanMod10 = issuer.isAllowCheckPanMod10();
        isAllowExpiry = issuer.isAllowExpiry();
        isAllowManualPan = issuer.isAllowManualPan();
        isAllowPrint = issuer.isAllowPrint();
        isEnableAdjust = issuer.isEnableAdjust();
        isEnableOffline = issuer.isEnableOffline();
        isRequireMaskExpiry = issuer.isRequireMaskExpiry();
        nonEmvTranRequirePIN = issuer.isNonEmvTranRequirePIN();
        isEnableRefund = issuer.getIsEnableRefund();
    }

    public boolean getIsEnableAdjust() {
        return this.isEnableAdjust;
    }

    public void setIsEnableAdjust(boolean isEnableAdjust) {
        this.isEnableAdjust = isEnableAdjust;
    }

    public boolean getIsEnableOffline() {
        return this.isEnableOffline;
    }

    public void setIsEnableOffline(boolean isEnableOffline) {
        this.isEnableOffline = isEnableOffline;
    }

    public boolean getIsAllowExpiry() {
        return this.isAllowExpiry;
    }

    public void setIsAllowExpiry(boolean isAllowExpiry) {
        this.isAllowExpiry = isAllowExpiry;
    }

    public boolean getIsAllowManualPan() {
        return this.isAllowManualPan;
    }

    public void setIsAllowManualPan(boolean isAllowManualPan) {
        this.isAllowManualPan = isAllowManualPan;
    }

    public boolean getIsAllowCheckExpiry() {
        return this.isAllowCheckExpiry;
    }

    public void setIsAllowCheckExpiry(boolean isAllowCheckExpiry) {
        this.isAllowCheckExpiry = isAllowCheckExpiry;
    }

    public boolean getIsAllowPrint() {
        return this.isAllowPrint;
    }

    public void setIsAllowPrint(boolean isAllowPrint) {
        this.isAllowPrint = isAllowPrint;
    }

    public boolean getIsAllowCheckPanMod10() {
        return this.isAllowCheckPanMod10;
    }

    public void setIsAllowCheckPanMod10(boolean isAllowCheckPanMod10) {
        this.isAllowCheckPanMod10 = isAllowCheckPanMod10;
    }

    public boolean getIsRequirePIN() {
        return this.nonEmvTranRequirePIN;
    }

    public void setIsRequirePIN(boolean isRequirePIN) {
        this.nonEmvTranRequirePIN = isRequirePIN;
    }

    public boolean getIsRequireMaskExpiry() {
        return this.isRequireMaskExpiry;
    }

    public void setIsRequireMaskExpiry(boolean isRequireMaskExpiry) {
        this.isRequireMaskExpiry = isRequireMaskExpiry;
    }

    public String getBindToAcquirer() {
        return this.bindToAcquirer;
    }

    public void setBindToAcquirer(String bindToAcquirer) {
        this.bindToAcquirer = bindToAcquirer;
    }

    public boolean getNonEmvTranRequirePIN() {
        return this.nonEmvTranRequirePIN;
    }

    public long getSmallAmtLimit() {
        return this.smallAmtLimit;
    }

    public void setSmallAmtLimit(long smallAmtLimit) {
        this.smallAmtLimit = smallAmtLimit;
    }

    public boolean getIsEnableRefund() {
        return this.isEnableRefund;
    }

    public void setIsEnableRefund(boolean isEnableRefund) {
        this.isEnableRefund = isEnableRefund;
    }
}
