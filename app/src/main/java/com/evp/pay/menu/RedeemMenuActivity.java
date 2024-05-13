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

import android.content.Intent;
import android.os.Bundle;

import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.trans.RedeemTrans;
import com.evp.pay.trans.RedeemTrans.Plan;
import com.evp.payment.evpscb.R;
import com.evp.view.MenuPage;


public class RedeemMenuActivity extends BaseMenuActivity implements MenuPage.OnProcessListener {

    public MenuPage createMenuPage() {
        MenuPage.Builder builder = new MenuPage.Builder(RedeemMenuActivity.this, 3, 2)
                .addTransItem(getString(R.string.trans_point_enquiry), R.drawable.app_auth)
                .addTransItem(getString(R.string.trans_redeem), R.drawable.app_auth);
        MenuPage menuPage = builder.create();
        menuPage.setOnProcessListener(this);
        return menuPage;
    }

    @Override
    public void process(int index) {
        switch (index) {
            default:
            case 0:
                new RedeemTrans(RedeemMenuActivity.this, "", Plan.ENQUIRY, "", "", null).setBackToMain(true).execute();
                break;
            case 1:
                Intent intent = new Intent(this, RedeemTypeMenuActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(EUIParamKeys.NAV_TITLE.toString(), getString(R.string.trans_redeem));
                bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
        }
    }
}
