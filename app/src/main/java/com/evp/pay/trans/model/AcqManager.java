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
 * 20190108  	         Frose.L                 Create
 * ===========================================================================================
 */
package com.evp.pay.trans.model;

import com.evp.bizlib.AppConstants;
import com.evp.bizlib.data.entity.AcqIssuerRelation;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.CardRange;
import com.evp.bizlib.data.entity.Issuer;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.data.local.db.helper.AcquirerDbHelper;
import com.evp.bizlib.data.model.ETransType;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.LogUtils;
import com.evp.payment.evpscb.R;
import com.evp.settings.SysParam;

import java.util.List;

/**
 * Acquirer Manager
 */
public class AcqManager {

    private static final String TAG = "AcqManager";

    private static AcqManager acqmanager;
    private Acquirer acquirer;

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static synchronized AcqManager getInstance() {
        if (acqmanager == null) {
            acqmanager = new AcqManager();
            init();
        }
        return acqmanager;
    }

    /**
     * Sets cur acq.
     *
     * @param acq the acq
     */
    public void setCurAcq(Acquirer acq) {
        acquirer = acq;
    }

    /**
     * Gets cur acq.
     *
     * @return the cur acq
     */
    public Acquirer getCurAcq() {
        return acquirer;
    }

    /**
     * check whether issuer is supported
     *
     * @param issuer issuer
     * @return result boolean
     */
    public boolean isIssuerSupported(final Issuer issuer) {
        try {
            List<Issuer> issuers = GreendaoHelper.getIssuerHelper().lookupIssuersForAcquirer(acqmanager.acquirer);
            for (Issuer tmp : issuers) {
                if (tmp.getName().equals(issuer.getName())) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            LogUtils.e(TAG, "", e);
        }
        return false;
    }

    /**
     * find issuer by pan
     *
     * @param pan card pan
     * @return issuer issuer
     */
    public Issuer findIssuerByPan(final String pan) {
        if (pan == null || pan.isEmpty()) {
            return null;
        }
        CardRange cardRange = GreendaoHelper.getCardRangeHelper().findCardRange(pan);
        if (cardRange == null) {
            return null;
        } else {
            return cardRange.getIssuer();
        }
    }

    /**
     * find issuer by pan
     * then find acquirer by issuer id and then set current acquirer
     * also set NII, BatchNo and acquirer for transData
     *
     * @param pan card pan
     * @param transData TransData object
     * @return issuer issuer
     */
    public Issuer findIssuerAndSetAcquirerByPan(final String pan, TransData transData) {
        if (pan == null || pan.isEmpty()) {
            return null;
        }
        CardRange cardRange = GreendaoHelper.getCardRangeHelper().findCardRange(pan);
        if (cardRange == null) {
            return null;
        }

        Issuer issuer = cardRange.getIssuer();
        Acquirer acquirer;
        switch (ConvertUtils.enumValue(ETransType.class,transData.getTransType())) {
            case INSTALLMENT:
                acquirer = findAcquirer(AppConstants.IPP_ACQUIRER);
                break;
            case OLS_ENQUIRY: case REDEEM:
                acquirer = findAcquirer(AppConstants.OLS_ACQUIRER);
                break;
            default:
                acquirer = findAcquirerByIssuerId(issuer.getId());
                break;
        }
        if(acquirer == null) {
            return null;
        }


        LogUtils.i(TAG, "Acquirer set to: " + acquirer.getName());
        setCurAcq(acquirer);
        if (transData != null) {
            transData.setAcquirer(acquirer);
            transData.setNii(acquirer.getNii());
            transData.setBatchNo(acquirer.getCurrBatchNo());
            transData.setIssuer(issuer);
        }
        return issuer;
    }

    public boolean switchToAcquirer(final String acquirerName, TransData transData) {
        Acquirer acquirer = findAcquirer(acquirerName);
        if(acquirer == null) {
            return false;
        }
        transData.setAcquirer(acquirer);
        transData.setNii(acquirer.getNii());
        transData.setBatchNo(acquirer.getCurrBatchNo());
        return true;
    }

    /**
     * find acquirer by issuer id
     *
     * @param issuerId issuer id
     * @return Acquirer object
     */
    public Acquirer findAcquirerByIssuerId(final long issuerId) {
        return AcquirerDbHelper.getInstance().findAcquirer(issuerId);
    }

    /**
     * delete all issuer
     */
    public boolean deleteAllIssuer() {
        return GreendaoHelper.getIssuerHelper().deleteAll();
    }

    /**
     * insert acquirer list
     *
     * @param acquirerList acquirer list
     * @return insert result
     */
    public boolean insertAcquirer(List<Acquirer> acquirerList) {
        return GreendaoHelper.getAcquirerHelper().insert(acquirerList);
    }

    /**
     * find acquirer by name
     *
     * @param acquirerName acquirer name
     * @return acquirer acquirer
     */
    public Acquirer findAcquirer(final String acquirerName) {
        return GreendaoHelper.getAcquirerHelper().findAcquirer(acquirerName);
    }

    /**
     * find all aqcuirers
     *
     * @return all aqcuirers
     */
    public List<Acquirer> findAllAcquirers() {
        return GreendaoHelper.getAcquirerHelper().loadAll();
    }

    /**
     * update acquirer
     *
     * @param acquirer acquirer
     * @return update result
     */
    public boolean updateAcquirer(final Acquirer acquirer) {
       return GreendaoHelper.getAcquirerHelper().update(acquirer);
    }

    /**
     * delete all acquirer
     */
    public boolean deleteAllAcquirer() {
        return GreendaoHelper.getAcquirerHelper().deleteAll();
    }

    /**
     * delete all card range
     */
    public boolean deleteAllCardRange() {
        return GreendaoHelper.getCardRangeHelper().deleteAll();
    }

    /**
     * find all acquirer and issuer relation
     *
     * @return acquirer and issuer relation list
     */
    public List<AcqIssuerRelation> findAllAcqIssuerRelation() {
        return GreendaoHelper.getAcqIssuerRelationHelper().loadAll();
    }

    /**
     * delete all acquirer and issuer relation
     */
    public boolean deleteAllAcqIssuerRelation() {
        return GreendaoHelper.getAcqIssuerRelationHelper().deleteAll();
    }

    /**
     * insert issuer list
     *
     * @param issuerList issuer list
     */
    public boolean insertIssuer(List<Issuer> issuerList) {
        return GreendaoHelper.getIssuerHelper().insert(issuerList);
    }

    /**
     * find issuer by name
     *
     * @param issuerName issuer name
     * @return issuer issuer
     */
    public Issuer findIssuer(final String issuerName) {
        return GreendaoHelper.getIssuerHelper().findIssuer(issuerName);
    }

    /**
     * find all issuer
     *
     * @return all issuer
     */
    public List<Issuer> findAllIssuers() {
        return GreendaoHelper.getIssuerHelper().loadAll();
    }

    /**
     * bind Acquirer and Issuer
     *
     * @param root   Acquirer
     * @param issuer Issuer
     * @return bind result
     */
    public boolean bind(final Acquirer root, final Issuer issuer) {
        return GreendaoHelper.getAcqIssuerRelationHelper().bindAcqAndIssuer(root, issuer);
    }

    /**
     * check whether Acquirer and Issuer is bind
     *
     * @param root   Acquirer
     * @param issuer Issuer
     * @return bind result
     */
    public boolean isBind(final Acquirer root, final Issuer issuer) {
        return GreendaoHelper.getAcqIssuerRelationHelper().findRelation(root, issuer) != null;
    }

    /**
     * bind acquirer and issuer relation
     *
     * @param acqIssuerRelationList acqIssuerRelationList
     * @return bind result
     */
    public boolean insertAcqIssuerRelation(List<AcqIssuerRelation> acqIssuerRelationList) {
        return GreendaoHelper.getAcqIssuerRelationHelper().insert(acqIssuerRelationList);
    }

    /**
     * update issuer
     *
     * @param issuer issuer
     */
    public void updateIssuer(final Issuer issuer) {
        GreendaoHelper.getIssuerHelper().update(issuer);
    }

    /**
     * insert card range
     *
     * @param cardRangeList card range list
     * @return insert result
     */
    public boolean insertCardRange(List<CardRange> cardRangeList) {
        return GreendaoHelper.getCardRangeHelper().insert(cardRangeList);
    }

    /**
     * find all card range
     *
     * @return all card range
     */
    public List<CardRange> findAllCardRanges() {
        return GreendaoHelper.getCardRangeHelper().loadAll();
    }

    /**
     * init acquirer
     */
    private static void init() {
        String name = SysParam.getInstance().getString(R.string.ACQ_NAME);
        if (!"".equals(name)) {
            Acquirer acquirer = GreendaoHelper.getAcquirerHelper().findAcquirer(name);
            if (acquirer != null) {
                acqmanager.setCurAcq(acquirer);
            }
        }
    }
}
