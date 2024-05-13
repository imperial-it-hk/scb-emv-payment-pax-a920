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

import com.evp.commonlib.utils.LogUtils;
import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.password.ChangeAdministratorPwdActivity;
import com.evp.pay.password.ChangeOfflineSalePwdActivity;
import com.evp.pay.password.ChangeMerchantPwdActivity;
import com.evp.pay.password.ChangeRefundPwdActivity;
import com.evp.pay.password.ChangeSettlePwdActivity;
import com.evp.pay.password.ChangeVoidPwdActivity;
import com.evp.view.MenuPage;

/**
 * The type Password menu activity.
 */
public class PasswordMenuActivity extends BaseMenuActivity implements MenuPage.OnProcessListener {

    /**
     * Change password
     */
    public MenuPage createMenuPage() {
        MenuPage.Builder builder = new MenuPage.Builder(PasswordMenuActivity.this, 6, 3);
        ConfigUtils.getInstance().getMenu(builder, ConfigConst.MenuType.PASSWORD_MENU);
        MenuPage menuPage = builder.create();
        menuPage.setOnProcessListener(this);
        return menuPage;
    }

    @Override
    public void process(String index) {
        switch (index) {
            case ConfigConst.TRANSACTION_PWD_MERCHANT:
                menuItemProcess(ChangeMerchantPwdActivity.class, ConfigUtils.getInstance().getString("labelTransactionPwdMerchant"));
                break;
            case ConfigConst.TRANSACTION_PWD_VOID:
                menuItemProcess(ChangeVoidPwdActivity.class, ConfigUtils.getInstance().getString("labelTransactionVoid"));
                break;
            case ConfigConst.TRANSACTION_PWD_REFUND:
                menuItemProcess(ChangeRefundPwdActivity.class, ConfigUtils.getInstance().getString("labelTransactionRefund"));
                break;
            case ConfigConst.TRANSACTION_PWD_OFFLINE_SALE:
                menuItemProcess(ChangeOfflineSalePwdActivity.class, ConfigUtils.getInstance().getString("labelTransactionAdjust"));
                break;
            case ConfigConst.TRANSACTION_PWD_SETTLEMENT:
                menuItemProcess(ChangeSettlePwdActivity.class, ConfigUtils.getInstance().getString("labelTransactionSettlement"));
                break;
            case ConfigConst.TRANSACTION_PWD_ADMIN:
                menuItemProcess(ChangeAdministratorPwdActivity.class, ConfigUtils.getInstance().getString("labelTransactionAdministrator"));
                break;
            default:
                break;
        }
    }

    @Override
    public void process(int index) {
        LogUtils.i(TAG, "NOT IN USE!!!");
    }

    private void menuItemProcess(Class<?> cls, String title) {
        Intent intent = new Intent(this, cls);
        Bundle bundle = new Bundle();
        bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
        bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
