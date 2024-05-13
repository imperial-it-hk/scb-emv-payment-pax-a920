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

import com.evp.pay.trans.PreAuthCancelTrans;
import com.evp.pay.trans.PreAuthCompleteTrans;
import com.evp.pay.trans.PreAuthTrans;
import com.evp.payment.evpscb.R;
import com.evp.view.MenuPage;

/**
 * pre auth activity
 */
public class AuthMenuActivity extends BaseMenuActivity implements MenuPage.OnProcessListener {

    public MenuPage createMenuPage() {
        MenuPage.Builder builder = new MenuPage.Builder(AuthMenuActivity.this, 3, 3)
                .addTransItem(getString(R.string.trans_preAuth), R.drawable.app_auth)
                .addTransItem(getString(R.string.trans_preAuthCancel), R.drawable.app_auth_cancel)
                .addTransItem(getString(R.string.trans_preAuthComplete), R.drawable.app_auth_finish);
        MenuPage menuPage = builder.create();
        menuPage.setOnProcessListener(this);
        return menuPage;
    }

    @Override
    public void process(int index) {
        switch (index) {
            case 0:
                new PreAuthTrans(AuthMenuActivity.this, true, null).setBackToMain(true).execute();
                break;
            case 1:
                new PreAuthCancelTrans(AuthMenuActivity.this, true, null).setBackToMain(true).execute();
                break;
            case 2:
                new PreAuthCompleteTrans(AuthMenuActivity.this, true, null).setBackToMain(true).execute();
                break;
            default:
                break;
        }
    }
}
