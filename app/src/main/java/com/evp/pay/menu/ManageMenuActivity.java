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

import com.evp.abl.core.AAction;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.utils.LogUtils;
import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.invoke.InvokeResponseData;
import com.evp.invoke.InvokeSender;
import com.evp.pay.ConfigFirstActivity;
import com.evp.pay.app.ActivityStack;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.record.TransQueryActivity;
import com.evp.pay.trans.TransContext;
import com.evp.pay.trans.action.ActionDispSingleLineMsg;
import com.evp.pay.trans.action.ActionInputPassword;
import com.evp.pay.trans.action.ActionUpdateParam;
import com.evp.settings.SelectLanguageActivity;
import com.evp.view.MenuPage;
import com.evp.view.dialog.DialogUtils;

/**
 * The type Manage menu activity.
 */
public class ManageMenuActivity extends BaseMenuActivity implements MenuPage.OnProcessListener {
    private boolean invokeSent;

    @Override
    public MenuPage createMenuPage() {
        MenuPage.Builder builder = new MenuPage.Builder(ManageMenuActivity.this, 9, 3);
        ConfigUtils.getInstance().getMenu(builder, ConfigConst.MenuType.MANAGEMENT_MENU);
        MenuPage menuPage = builder.create();
        menuPage.setOnProcessListener(this);
        return menuPage;
    }

    private AAction createDispActionForVersion() {
        ActionDispSingleLineMsg displayInfoAction = new ActionDispSingleLineMsg(action -> ((ActionDispSingleLineMsg) action)
                .setParam(
                        ManageMenuActivity.this,
                        ConfigUtils.getInstance().getString("labelTransactionVersion"),
                        ConfigUtils.getInstance().getString("softwareVersionLabel"),
                        FinancialApplication.getVersion(),
                        60
                )
        );

        displayInfoAction.setEndListener((action, result) -> {
            ActivityStack.getInstance().pop();
            TransContext.getInstance().getCurrentAction().setFinished(false); //AET-229
            TransContext.getInstance().setCurrentAction(null); //fix leaks
        });

        return displayInfoAction;
    }

    private AAction createActionForUpdateParam() {
        ActionUpdateParam actionUpdateParam = new ActionUpdateParam(action -> ((ActionUpdateParam) action)
                .setParam(
                        ManageMenuActivity.this,
                        true
                )
        );

        actionUpdateParam.setEndListener((action, result) -> TransContext.getInstance().setCurrentAction(null));

        return actionUpdateParam;
    }

    private AAction createInputPwdActionForSettings() {
        ActionInputPassword inputPasswordAction = new ActionInputPassword(action -> ((ActionInputPassword) action)
                .setParam(
                        ManageMenuActivity.this,
                        6,
                        ConfigUtils.getInstance().getString("passwordEnterAdminLabel"),
                        null
                )
        );

        inputPasswordAction.setEndListener((action, result) -> {
            TransContext.getInstance().setCurrentAction(null); //fix leaks
            if (result.getRet() != TransResult.SUCC) {
                return;
            }
            String data = (String) result.getData();
            if (!data.equals(ConfigUtils.getInstance().getDeviceConf(ConfigConst.ADMIN_PASSWORD))) {
                DialogUtils.showErrMessage(
                        ManageMenuActivity.this,
                        ConfigUtils.getInstance().getString("labelTransactionConfig"),
                        ConfigUtils.getInstance().getString("passwordIsIncorrectLabel"),
                        null,
                        Constants.FAILED_DIALOG_SHOW_TIME);
                return;
            }
            Intent intent = new Intent(ManageMenuActivity.this, ConfigFirstActivity.class);
            startActivity(intent);
        });

        return inputPasswordAction;

    }

    private AAction createInputPwdActionForManagePassword() {
        ActionInputPassword inputPasswordAction = new ActionInputPassword(action -> ((ActionInputPassword) action)
                .setParam(
                        ManageMenuActivity.this,
                        6,
                        ConfigUtils.getInstance().getString("passwordEnterMerchantLabel"),
                        null
                )
        );

        inputPasswordAction.setEndListener((action, result) -> {
            TransContext.getInstance().setCurrentAction(null); //fix leaks
            if (result.getRet() != TransResult.SUCC) {
                return;
            }
            String data = (String) result.getData();
            if (!data.equals(ConfigUtils.getInstance().getDeviceConf(ConfigConst.MERCHANT_PASSWORD))) {
                DialogUtils.showErrMessage(
                        ManageMenuActivity.this,
                        ConfigUtils.getInstance().getString("passwordLabel"),
                        ConfigUtils.getInstance().getString("passwordIsIncorrectLabel"),
                        null,
                        Constants.FAILED_DIALOG_SHOW_TIME);
                return;
            }
            Intent intent = new Intent(ManageMenuActivity.this, PasswordMenuActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(EUIParamKeys.NAV_TITLE.toString(), ConfigUtils.getInstance().getString("passwordLabel"));
            bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        return inputPasswordAction;
    }

    @Override
    public void process(String index) {
        switch (index) {
            case ConfigConst.TRANSACTION_PASSWORD:
                createInputPwdActionForManagePassword().execute();
                break;
            case ConfigConst.TRANSACTION_CONFIG:
                createInputPwdActionForSettings().execute();
                break;
            case ConfigConst.TRANSACTION_HISTORY:
                Intent intent = new Intent(this, TransQueryActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(EUIParamKeys.NAV_TITLE.toString(), ConfigUtils.getInstance().getString("labelTransactionHistory"));
                bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case ConfigConst.TRANSACTION_VERSION:
                createDispActionForVersion().execute();
                break;
            case ConfigConst.TRANSACTION_UPDATE:
                createActionForUpdateParam().execute();
                break;
            case ConfigConst.TRANSACTION_LANGUAGE:
                Intent intentForSelectLanguage = new Intent(this, SelectLanguageActivity.class);
                Bundle bundleForSelectLanguage = new Bundle();
                bundleForSelectLanguage.putString(EUIParamKeys.NAV_TITLE.toString(), ConfigUtils.getInstance().getString("labelTransactionLanguage"));
                bundleForSelectLanguage.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
                intentForSelectLanguage.putExtras(bundleForSelectLanguage);
                startActivity(intentForSelectLanguage);
                break;
            default:
                break;
        }
    }

    @Override
    public void process(int index) {
        LogUtils.i(TAG, "NOT IN USE!!!");
    }

    @Override
    public void finish() {
        if(startsFromInvoke && !invokeSent) {
            invokeSent = true;
            InvokeResponseData.createResponseData(ETransType.SETTING, new ActionResult(TransResult.SUCC, null));
            InvokeSender.send(this);
        }
        super.finish();
    }
}
