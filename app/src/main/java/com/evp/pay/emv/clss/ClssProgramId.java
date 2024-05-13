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
 * 20190618  	         Roy                     Create
 * ===========================================================================================
 */

package com.evp.pay.emv.clss;
import java.io.Serializable;

/**
 * emv contactless programId
 */
public class ClssProgramId implements Serializable {
    /**
     * Instantiates a new Clss program id.
     */
    public ClssProgramId() {
    }

    private String aucRdClssTxnLmt;
    private String aucRdCVMLmt;
    private String aucRdClssFLmt;
    private String aucTermFLmt;
    private String aucProgramId;
    private int ucPrgramIdLen;
    private int ucRdClssFLmtFlg;
    private int ucRdClssTxnLmtFlg;
    private int ucRdCVMLmtFlg;
    private int ucTermFLmtFlg;
    private int ucStatusCheckFlg;
    private int ucAmtZeroNoAllowed;
    private int ucDynamicLimitSet;
    private int ucRFU;

    /**
     * Gets auc rd clss txn lmt.
     *
     * @return the auc rd clss txn lmt
     */
    public String getAucRdClssTxnLmt() {
        return aucRdClssTxnLmt;
    }

    /**
     * Sets auc rd clss txn lmt.
     *
     * @param aucRdClssTxnLmt the auc rd clss txn lmt
     */
    public void setAucRdClssTxnLmt(String aucRdClssTxnLmt) {
        this.aucRdClssTxnLmt = aucRdClssTxnLmt;
    }

    /**
     * Gets auc rd cvm lmt.
     *
     * @return the auc rd cvm lmt
     */
    public String getAucRdCVMLmt() {
        return aucRdCVMLmt;
    }

    /**
     * Sets auc rd cvm lmt.
     *
     * @param aucRdCVMLmt the auc rd cvm lmt
     */
    public void setAucRdCVMLmt(String aucRdCVMLmt) {
        this.aucRdCVMLmt = aucRdCVMLmt;
    }

    /**
     * Gets auc rd clss f lmt.
     *
     * @return the auc rd clss f lmt
     */
    public String getAucRdClssFLmt() {
        return aucRdClssFLmt;
    }

    /**
     * Sets auc rd clss f lmt.
     *
     * @param aucRdClssFLmt the auc rd clss f lmt
     */
    public void setAucRdClssFLmt(String aucRdClssFLmt) {
        this.aucRdClssFLmt = aucRdClssFLmt;
    }

    /**
     * Gets auc term f lmt.
     *
     * @return the auc term f lmt
     */
    public String getAucTermFLmt() {
        return aucTermFLmt;
    }

    /**
     * Sets auc term f lmt.
     *
     * @param aucTermFLmt the auc term f lmt
     */
    public void setAucTermFLmt(String aucTermFLmt) {
        this.aucTermFLmt = aucTermFLmt;
    }

    /**
     * Gets auc program id.
     *
     * @return the auc program id
     */
    public String getAucProgramId() {
        return aucProgramId;
    }

    /**
     * Sets auc program id.
     *
     * @param aucProgramId the auc program id
     */
    public void setAucProgramId(String aucProgramId) {
        this.aucProgramId = aucProgramId;
    }

    /**
     * Gets uc prgram id len.
     *
     * @return the uc prgram id len
     */
    public int getUcPrgramIdLen() {
        return ucPrgramIdLen;
    }

    /**
     * Sets uc prgram id len.
     *
     * @param ucPrgramIdLen the uc prgram id len
     */
    public void setUcPrgramIdLen(int ucPrgramIdLen) {
        this.ucPrgramIdLen = ucPrgramIdLen;
    }

    /**
     * Gets uc rd clss f lmt flg.
     *
     * @return the uc rd clss f lmt flg
     */
    public int getUcRdClssFLmtFlg() {
        return ucRdClssFLmtFlg;
    }

    /**
     * Sets uc rd clss f lmt flg.
     *
     * @param ucRdClssFLmtFlg the uc rd clss f lmt flg
     */
    public void setUcRdClssFLmtFlg(int ucRdClssFLmtFlg) {
        this.ucRdClssFLmtFlg = ucRdClssFLmtFlg;
    }

    /**
     * Gets uc rd clss txn lmt flg.
     *
     * @return the uc rd clss txn lmt flg
     */
    public int getUcRdClssTxnLmtFlg() {
        return ucRdClssTxnLmtFlg;
    }

    /**
     * Sets uc rd clss txn lmt flg.
     *
     * @param ucRdClssTxnLmtFlg the uc rd clss txn lmt flg
     */
    public void setUcRdClssTxnLmtFlg(int ucRdClssTxnLmtFlg) {
        this.ucRdClssTxnLmtFlg = ucRdClssTxnLmtFlg;
    }

    /**
     * Gets uc rd cvm lmt flg.
     *
     * @return the uc rd cvm lmt flg
     */
    public int getUcRdCVMLmtFlg() {
        return ucRdCVMLmtFlg;
    }

    /**
     * Sets uc rd cvm lmt flg.
     *
     * @param ucRdCVMLmtFlg the uc rd cvm lmt flg
     */
    public void setUcRdCVMLmtFlg(int ucRdCVMLmtFlg) {
        this.ucRdCVMLmtFlg = ucRdCVMLmtFlg;
    }

    /**
     * Gets uc term f lmt flg.
     *
     * @return the uc term f lmt flg
     */
    public int getUcTermFLmtFlg() {
        return ucTermFLmtFlg;
    }

    /**
     * Sets uc term f lmt flg.
     *
     * @param ucTermFLmtFlg the uc term f lmt flg
     */
    public void setUcTermFLmtFlg(int ucTermFLmtFlg) {
        this.ucTermFLmtFlg = ucTermFLmtFlg;
    }

    /**
     * Gets uc status check flg.
     *
     * @return the uc status check flg
     */
    public int getUcStatusCheckFlg() {
        return ucStatusCheckFlg;
    }

    /**
     * Sets uc status check flg.
     *
     * @param ucStatusCheckFlg the uc status check flg
     */
    public void setUcStatusCheckFlg(int ucStatusCheckFlg) {
        this.ucStatusCheckFlg = ucStatusCheckFlg;
    }

    /**
     * Gets uc amt zero no allowed.
     *
     * @return the uc amt zero no allowed
     */
    public int getUcAmtZeroNoAllowed() {
        return ucAmtZeroNoAllowed;
    }

    /**
     * Sets uc amt zero no allowed.
     *
     * @param ucAmtZeroNoAllowed the uc amt zero no allowed
     */
    public void setUcAmtZeroNoAllowed(int ucAmtZeroNoAllowed) {
        this.ucAmtZeroNoAllowed = ucAmtZeroNoAllowed;
    }

    /**
     * Gets uc dynamic limit set.
     *
     * @return the uc dynamic limit set
     */
    public int getUcDynamicLimitSet() {
        return ucDynamicLimitSet;
    }

    /**
     * Sets uc dynamic limit set.
     *
     * @param ucDynamicLimitSet the uc dynamic limit set
     */
    public void setUcDynamicLimitSet(int ucDynamicLimitSet) {
        this.ucDynamicLimitSet = ucDynamicLimitSet;
    }

    /**
     * Gets uc rfu.
     *
     * @return the uc rfu
     */
    public int getUcRFU() {
        return ucRFU;
    }

    /**
     * Sets uc rfu.
     *
     * @param ucRFU the uc rfu
     */
    public void setUcRFU(int ucRFU) {
        this.ucRFU = ucRFU;
    }
}
