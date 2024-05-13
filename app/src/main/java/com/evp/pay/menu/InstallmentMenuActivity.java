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
package com.evp.pay.menu;

import com.evp.pay.trans.InstallmentTrans;
import com.evp.pay.trans.InstallmentTrans.Plan;
import com.evp.payment.evpscb.R;
import com.evp.view.MenuPage;

/**
 * pre auth activity
 */
public class InstallmentMenuActivity extends BaseMenuActivity implements MenuPage.OnProcessListener {

    public MenuPage createMenuPage() {
        MenuPage.Builder builder = new MenuPage.Builder(InstallmentMenuActivity.this, 4, 2)
                .addTransItem(getString(R.string.installment_normal), R.drawable.app_install)
                .addTransItem(getString(R.string.installment_fix_rate), R.drawable.app_install)
                .addTransItem(getString(R.string.installment_special), R.drawable.app_install);
        MenuPage menuPage = builder.create();
        menuPage.setOnProcessListener(this);
        return menuPage;
    }

    @Override
    public void process(int index) {
        Plan plan;
        switch (index) {
            default:
            case 0:
                plan = Plan.NORMAL_IPP;
                break;
            case 1:
                plan = Plan.FIX_RATE_IPP;
                break;
            case 2:
                plan = Plan.SPECIAL_IPP;
                break;
        }
        new InstallmentTrans(InstallmentMenuActivity.this, "", plan, "", "","", null).setBackToMain(true).execute();
    }
}
