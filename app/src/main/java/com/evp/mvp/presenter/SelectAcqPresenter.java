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
package com.evp.mvp.presenter;

import android.content.Context;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.mvp.contract.SelectAcqContract;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.trans.action.activity.SelectAcqActivity;
import com.evp.payment.evpscb.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Select acq presenter.
 */
public class SelectAcqPresenter extends SelectAcqContract.Presenter {
    /**
     * The Checked acqs.
     */
    public List<String> checkedAcqs;

    /**
     * Instantiates a new Select acq presenter.
     *
     * @param context the context
     */
    public SelectAcqPresenter(Context context) {
        super(context);
    }

    @Override
    public void initParam(ArrayList<String> checkedAcqs) {
        if (checkedAcqs == null) {
            checkedAcqs = new ArrayList<>();
        }
        this.checkedAcqs = checkedAcqs;
    }

    @Override
    public void initAdapterData() {
        List<Acquirer> acquirers = FinancialApplication.getAcqManager().findAllAcquirers();
        ArrayList<Map<String, String>> myListArray = new ArrayList<>();
        for (Acquirer item : acquirers) {
            HashMap<String, String> map = new HashMap<>(4);
            map.put(SelectAcqActivity.ACQ_NAME, item.getName());
            myListArray.add(map);
        }
        proxyView.showAdapterData(myListArray);
    }

    @Override
    public void selectAcqCheck(boolean isChecked) {
        if (isChecked) {
            List<Acquirer> acqList = FinancialApplication.getAcqManager().findAllAcquirers();
            for (Acquirer acquirer : acqList) {
                if (!checkedAcqs.contains(acquirer.getName())) {
                    checkedAcqs.add(acquirer.getName());
                }
            }
        } else {
            if (checkedAcqs.size() == FinancialApplication.getAcqManager().findAllAcquirers().size()) {
                checkedAcqs.clear();
            }
        }
        proxyView.onSelectAcqCheck();
    }

    @Override
    public void finish2SettleAcq() {
        if (checkedAcqs.isEmpty()) {
            proxyView.showToast(R.string.err_settle_select_acq);
            return;
        }
        proxyView.finish(new ActionResult(TransResult.SUCC, checkedAcqs));
    }

    @Override
    public void settle(String acquirer) {
        if (acquirer == null || acquirer.isEmpty()) {
            return;
        }
        checkedAcqs.clear();
        checkedAcqs.add(acquirer);
        proxyView.finish(new ActionResult(TransResult.SUCC, checkedAcqs));
    }

    @Override
    public void onCheckedChange(String acquireName, boolean checked) {
        if (checked) {
            if (!checkedAcqs.contains(acquireName)) {
                checkedAcqs.add(acquireName);
            }
        } else {
            checkedAcqs.remove(acquireName);
        }
        proxyView.confirmBtnChange(!checkedAcqs.isEmpty());
        //AET-39
        proxyView.setChecked(checkedAcqs.size() == FinancialApplication.getAcqManager().findAllAcquirers().size());
    }

}
