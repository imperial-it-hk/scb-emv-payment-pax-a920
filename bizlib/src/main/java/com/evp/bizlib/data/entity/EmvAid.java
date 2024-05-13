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

import androidx.annotation.IntDef;
import androidx.annotation.IntRange;

import com.alibaba.fastjson.annotation.JSONField;
import com.evp.bizlib.data.local.GreendaoHelper;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

@Entity(nameInDb = "aid")
public class EmvAid implements Serializable {
    private static final long serialVersionUID = 1L;
    @IntDef({PART_MATCH, FULL_MATCH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SelType {
    }

    public static final int PART_MATCH = 0;
    public static final int FULL_MATCH = 1;

    public static final String ID_FIELD_NAME = "id";
    public static final String AID_FIELD_NAME = "aid";


    @Id(autoincrement = true)
    @Property(nameInDb = ID_FIELD_NAME)
    @JSONField
    private Long id;
    /**
     * name
     */
    @NotNull
    private String appName;
    /**
     * aid
     */
    @Property(nameInDb = AID_FIELD_NAME)
    @NotNull
    @Unique
    private String aid;
    /**
     * PART_MATCH/FULL_MATCH
     */
    @NotNull
    @SelType
    private int selFlag;
    /**
     * priority
     */
    @NotNull
    private int priority;
    /**
     * tag DF21
     */
    @NotNull
    private long rdCVMLmt;
    /**
     * tag DF20
     */
    @NotNull
    private long rdClssTxnLmt;
    /**
     * tag DF19
     */
    @NotNull
    private long rdClssFLmt;

    /**
     * clss floor limit flag
     * 0- Deactivated
     * 1- Active and exist
     * 2- Active but not exist
     */
    @NotNull
    @IntRange(from = 0, to = 2)
    private int rdClssFLmtFlg;
    /**
     * clss transaction limit flag
     * 0- Deactivated
     * 1- Active and exist
     * 2- Active but not exist
     */
    @NotNull
    @IntRange(from = 0, to = 2)
    private int rdClssTxnLmtFlg;
    /**
     * clss CVM limit flag
     * 0- Deactivated
     * 1- Active and exist
     * 2- Active but not exist
     */
    @NotNull
    @IntRange(from = 0, to = 2)
    private int rdCVMLmtFlg;

    /**
     * target percent
     */
    @NotNull
    @IntRange(from = 0, to = 100)
    private int targetPer;
    /**
     * max target percent
     */
    @NotNull
    @IntRange(from = 0, to = 100)
    private int maxTargetPer;
    /**
     * floor limit check flag
     * 0- don't check
     * 1- Check
     */
    @NotNull
    @IntRange(from = 0, to = 1)
    private int floorLimitCheckFlg;
    /**
     * do random transaction selection
     */
    @NotNull
    private boolean randTransSel;
    /**
     * velocity check
     */
    @NotNull
    private boolean velocityCheck;
    /**
     * floor limit
     */
    @NotNull
    private long floorLimit;
    /**
     * threshold
     */
    @NotNull
    private long threshold;
    /**
     * TAC denial
     */
    private String tacDenial;
    /**
     * TAC online
     */
    private String tacOnline;
    /**
     * TAC default
     */
    private String tacDefault;
    /**
     * acquirer id
     */
    private String acquirerId;
    /**
     * dDOL
     */
    private String dDOL;
    /**
     * tDOL
     */
    private String tDOL;
    /**
     * application version
     */
    private String version;
    /**
     * risk management data
     */
    private String riskManageData;
    /**
     * JCB CTLS specific TERMINAL INTERCHANGE PROFILE
     */
    private String jcbClssTermIntProfile;
    /**
     * JCB CTLS specific TERMINAL COMPATIBILITY INDICATOR
     */
    private int jcbClssTermCompatIndicator;
    /**
     * JCB CTLS specific COMBINATION OPTION
     */
    private String jcbClssCombinationOpt;
    /**
     * Terminal capabilities per AID
     */
    private String terminalCapabilities;
    /**
     * Enable or disable this AID for CTLS
     */
    private boolean enableClss;

    @Generated(hash = 961542096)
    public EmvAid(Long id, @NotNull String appName, @NotNull String aid, int selFlag, int priority, long rdCVMLmt, long rdClssTxnLmt,
            long rdClssFLmt, int rdClssFLmtFlg, int rdClssTxnLmtFlg, int rdCVMLmtFlg, int targetPer, int maxTargetPer,
            int floorLimitCheckFlg, boolean randTransSel, boolean velocityCheck, long floorLimit, long threshold, String tacDenial,
            String tacOnline, String tacDefault, String acquirerId, String dDOL, String tDOL, String version, String riskManageData,
            String jcbClssTermIntProfile, int jcbClssTermCompatIndicator, String jcbClssCombinationOpt, String terminalCapabilities,
            boolean enableClss) {
        this.id = id;
        this.appName = appName;
        this.aid = aid;
        this.selFlag = selFlag;
        this.priority = priority;
        this.rdCVMLmt = rdCVMLmt;
        this.rdClssTxnLmt = rdClssTxnLmt;
        this.rdClssFLmt = rdClssFLmt;
        this.rdClssFLmtFlg = rdClssFLmtFlg;
        this.rdClssTxnLmtFlg = rdClssTxnLmtFlg;
        this.rdCVMLmtFlg = rdCVMLmtFlg;
        this.targetPer = targetPer;
        this.maxTargetPer = maxTargetPer;
        this.floorLimitCheckFlg = floorLimitCheckFlg;
        this.randTransSel = randTransSel;
        this.velocityCheck = velocityCheck;
        this.floorLimit = floorLimit;
        this.threshold = threshold;
        this.tacDenial = tacDenial;
        this.tacOnline = tacOnline;
        this.tacDefault = tacDefault;
        this.acquirerId = acquirerId;
        this.dDOL = dDOL;
        this.tDOL = tDOL;
        this.version = version;
        this.riskManageData = riskManageData;
        this.jcbClssTermIntProfile = jcbClssTermIntProfile;
        this.jcbClssTermCompatIndicator = jcbClssTermCompatIndicator;
        this.jcbClssCombinationOpt = jcbClssCombinationOpt;
        this.terminalCapabilities = terminalCapabilities;
        this.enableClss = enableClss;
    }

    @Generated(hash = 1782441615)
    public EmvAid() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public int getSelFlag() {
        return selFlag;
    }

    public void setSelFlag(@SelType int selFlag) {
        this.selFlag = selFlag;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getRdCVMLmt() {
        return rdCVMLmt;
    }

    public void setRdCVMLmt(long rdCVMLmt) {
        this.rdCVMLmt = rdCVMLmt;
    }

    public long getRdClssTxnLmt() {
        return rdClssTxnLmt;
    }

    public void setRdClssTxnLmt(long rdClssTxnLmt) {
        this.rdClssTxnLmt = rdClssTxnLmt;
    }

    public long getRdClssFLmt() {
        return rdClssFLmt;
    }

    public void setRdClssFLmt(long rdClssFLmt) {
        this.rdClssFLmt = rdClssFLmt;
    }

    public int getRdClssFLmtFlg() {
        return rdClssFLmtFlg;
    }

    public void setRdClssFLmtFlg(@IntRange(from = 0, to = 2) int rdClssFLmtFlg) {
        this.rdClssFLmtFlg = rdClssFLmtFlg;
    }

    public int getRdClssTxnLmtFlg() {
        return rdClssTxnLmtFlg;
    }

    public void setRdClssTxnLmtFlg(@IntRange(from = 0, to = 2) int rdClssTxnLmtFlg) {
        this.rdClssTxnLmtFlg = rdClssTxnLmtFlg;
    }

    public int getRdCVMLmtFlg() {
        return rdCVMLmtFlg;
    }

    public void setRdCVMLmtFlg(@IntRange(from = 0, to = 2) int rdCVMLmtFlg) {
        this.rdCVMLmtFlg = rdCVMLmtFlg;
    }

    public int getTargetPer() {
        return targetPer;
    }

    public void setTargetPer(@IntRange(from = 0, to = 100) int targetPer) {
        this.targetPer = targetPer;
    }

    public int getMaxTargetPer() {
        return maxTargetPer;
    }

    public void setMaxTargetPer(@IntRange(from = 0, to = 100) int maxTargetPer) {
        this.maxTargetPer = maxTargetPer;
    }

    public int getFloorLimitCheckFlg() {
        return floorLimitCheckFlg;
    }

    public void setFloorLimitCheckFlg(@IntRange(from = 0, to = 1) int floorLimitCheckFlg) {
        this.floorLimitCheckFlg = floorLimitCheckFlg;
    }

    public boolean getRandTransSel() {
        return randTransSel;
    }

    public void setRandTransSel(boolean randTransSel) {
        this.randTransSel = randTransSel;
    }

    public boolean getVelocityCheck() {
        return velocityCheck;
    }

    public void setVelocityCheck(boolean velocityCheck) {
        this.velocityCheck = velocityCheck;
    }

    public long getFloorLimit() {
        return floorLimit;
    }

    public void setFloorLimit(long floorLimit) {
        this.floorLimit = floorLimit;
    }

    public long getThreshold() {
        return threshold;
    }

    public void setThreshold(long threshold) {
        this.threshold = threshold;
    }

    public String getTacDenial() {
        return tacDenial;
    }

    public void setTacDenial(String tacDenial) {
        this.tacDenial = tacDenial;
    }

    public String getTacOnline() {
        return tacOnline;
    }

    public void setTacOnline(String tacOnline) {
        this.tacOnline = tacOnline;
    }

    public String getTacDefault() {
        return tacDefault;
    }

    public void setTacDefault(String tacDefault) {
        this.tacDefault = tacDefault;
    }

    public String getAcquirerId() {
        return acquirerId;
    }

    public void setAcquirerId(String acquirerId) {
        this.acquirerId = acquirerId;
    }

    public String getDDOL() {
        return dDOL;
    }

    public void setDDOL(String dDOL) {
        this.dDOL = dDOL;
    }

    public String getTDOL() {
        return tDOL;
    }

    public void setTDOL(String tDOL) {
        this.tDOL = tDOL;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRiskManageData() {
        return riskManageData;
    }

    public void setRiskManageData(String riskManageData) {
        this.riskManageData = riskManageData;
    }

    @Override
    public String toString() {
        return appName;
    }

    public static boolean load(List<EmvAid> aids) {
        return GreendaoHelper.getEmvAidHelper().insert(aids);
    }

    public static boolean deleteAllRecords() {
        return GreendaoHelper.getEmvAidHelper().deleteAll();
    }

    public String getJcbClssTermIntProfile() {
        return this.jcbClssTermIntProfile;
    }

    public void setJcbClssTermIntProfile(String jcbClssTermIntProfile) {
        this.jcbClssTermIntProfile = jcbClssTermIntProfile;
    }

    public int getJcbClssTermCompatIndicator() {
        return this.jcbClssTermCompatIndicator;
    }

    public void setJcbClssTermCompatIndicator(int jcbClssTermCompatIndicator) {
        this.jcbClssTermCompatIndicator = jcbClssTermCompatIndicator;
    }

    public String getJcbClssCombinationOpt() {
        return this.jcbClssCombinationOpt;
    }

    public void setJcbClssCombinationOpt(String jcbClssCombinationOpt) {
        this.jcbClssCombinationOpt = jcbClssCombinationOpt;
    }

    public String getTerminalCapabilities() {
        return this.terminalCapabilities;
    }

    public void setTerminalCapabilities(String terminalCapabilities) {
        this.terminalCapabilities = terminalCapabilities;
    }

    public boolean getEnableClss() {
        return this.enableClss;
    }

    public void setEnableClss(boolean enableClss) {
        this.enableClss = enableClss;
    }
}
