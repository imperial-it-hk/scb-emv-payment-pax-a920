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
 * 20190108  	         laiyi                   Create
 * ===========================================================================================
 */
package com.evp.pay.trans.action;

import android.content.Context;

import com.evp.abl.core.AAction;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.config.ConfigUtils;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.settings.SysParam;
import com.evp.view.dialog.DialogUtils;

/**
 * The type Action update param.
 */
public class ActionUpdateParam extends AAction {
    private Context context;
    private boolean disNoParam;

    /**
     * derived classes must call super(listener) to set
     *
     * @param listener {@link ActionStartListener}
     */
    public ActionUpdateParam(ActionStartListener listener) {
        super(listener);

    }

    /**
     * Sets param.
     *
     * @param context    the context
     * @param disNoParam the dis no param
     */
    public void setParam(Context context, boolean disNoParam) {
        this.context = context;
        this.disNoParam = disNoParam;
    }

    /**
     * action process
     */
    @Override
    protected void process() {
        if (FinancialApplication.getDownloadManager().isUpdateRequired()) {
            FinancialApplication.getApp().runOnUiThread(() -> DialogUtils.showConfirmDialog(context, context.getString(R.string.update_param_now_or_not), alertDialog -> {
                alertDialog.dismiss();
                setResult(new ActionResult(TransResult.SUCC, null));
            }, alertDialog -> {
                alertDialog.dismiss();
                if (GreendaoHelper.getTransDataHelper().countOf() == 0) {
                    boolean success = FinancialApplication.getDownloadManager().updateData();
                    if (success) {
                        DialogUtils.showMessage(context, ConfigUtils.getInstance().getString("updateSuccessLabel"), dialog -> {
                            setResult(new ActionResult(TransResult.ERR_ABORTED, null));
                            SysParam.getInstance().set(Utils.getString(R.string.IS_PAXSTORE_UPDATE_PARAM_EXCEPTION), false);
                            SysParam.getInstance().set(Utils.getString(R.string.PAXSTORE_UPDATE_PARAM_EXCEPTION), "");
                            Utils.restart();
                        }, Constants.SUCCESS_DIALOG_SHOW_TIME);
                    } else {
                        String prompt;
                        if (SysParam.getInstance().getBoolean(R.string.IS_PAXSTORE_UPDATE_PARAM_EXCEPTION)) {
                            prompt = SysParam.getInstance().getString(Utils.getString(R.string.PAXSTORE_UPDATE_PARAM_EXCEPTION));
                        } else {
                            prompt = ConfigUtils.getInstance().getString("updateFailedLabel");
                        }
                        DialogUtils.showErrMessage(context, null, prompt, dialog -> setResult(new ActionResult(TransResult.ERR_ABORTED, null)), Constants.FAILED_DIALOG_SHOW_TIME_LONG);
                    }
                } else {
                    DialogUtils.showErrMessage(context, null, ConfigUtils.getInstance().getString("allTransactionNeedSettledLabel"), dialog -> setResult(new ActionResult(TransResult.ERR_ABORTED, null)), Constants.FAILED_DIALOG_SHOW_TIME);
                }
            }));
        } else if (disNoParam) {
            DialogUtils.showErrMessage(context, null, context.getString(R.string.no_update_param), dialog -> setResult(new ActionResult(TransResult.ERR_ABORTED, null)), Constants.FAILED_DIALOG_SHOW_TIME);
        } else {
            setResult(new ActionResult(TransResult.SUCC, null));
        }
    }
}
