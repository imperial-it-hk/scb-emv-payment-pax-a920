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

import com.evp.pay.trans.RedeemTrans;
import com.evp.pay.trans.RedeemTrans.Plan;
import com.evp.payment.evpscb.R;
import com.evp.view.MenuPage;


public class RedeemTypeMenuActivity extends BaseMenuActivity implements MenuPage.OnProcessListener {

    public MenuPage createMenuPage() {
        MenuPage.Builder builder = new MenuPage.Builder(RedeemTypeMenuActivity.this, 4, 2)
                .addTransItem(getString(R.string.redeem_voucher), R.drawable.app_auth)
                .addTransItem(getString(R.string.redeem_point_credit), R.drawable.app_auth)
                .addTransItem(getString(R.string.redeem_discount), R.drawable.app_auth)
                .addTransItem(getString(R.string.redeem_product), R.drawable.app_auth);
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
                plan = Plan.VOUCHER;
                break;
            case 1:
                plan = Plan.POINT_CREDIT;
                break;
            case 2:
                plan = Plan.DISCOUNT;
                break;
            case 3:
                plan = Plan.SPECIAL_REDEEM;
                break;
        }
        new RedeemTrans(RedeemTypeMenuActivity.this, "", plan, "", "", null).setBackToMain(true).execute();
    }
}
