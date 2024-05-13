package com.evp.pay.menu;

import android.content.Intent;
import android.os.Bundle;

import com.evp.abl.core.AAction;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.pay.constant.Constants;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.record.TransQueryActivity;
import com.evp.pay.trans.TransContext;
import com.evp.pay.trans.action.ActionInputPassword;
import com.evp.pay.trans.action.ActionInputTransData;
import com.evp.pay.trans.action.ActionPrintPreview;
import com.evp.pay.trans.model.PrintType;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.view.MenuPage;
import com.evp.view.dialog.DialogUtils;

public class QueryMenuActivity extends BaseMenuActivity implements MenuPage.OnProcessListener {
    private MenuPage menuPage;
    private TransData transData;

    @Override
    public MenuPage createMenuPage() {
        MenuPage.Builder builder = new MenuPage.Builder(QueryMenuActivity.this, 9, 3)
                .addActionItem(getString(R.string.last_transaction), R.drawable.last_batch_total)
                .addActionItem(getString(R.string.suspended_qr), R.drawable.scanner)
                .addMenuItem(getString(R.string.any_transaction), R.drawable.app_query);

        menuPage = builder.create();
        menuPage.setOnProcessListener(this);
        return menuPage;
    }

    private AAction createInputTraceNoPageForQuery() {
        ActionInputTransData actionInputTransData = new ActionInputTransData(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputPassword) action).setParam(QueryMenuActivity.this, 6,
                        getString(R.string.prompt_sys_pwd), null);
            }
        });
        actionInputTransData.setEndListener(new AAction.ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult result) {
                TransContext.getInstance().setCurrentAction(null); //fix leaks

                if (result.getRet() != TransResult.SUCC) {
                    return;
                }
                String content = result.getData().toString();
                if (content == null) {
                    transData = GreendaoHelper.getTransDataHelper().findLastTransData();
                } else {
                    transData = GreendaoHelper.getTransDataHelper().findTransDataByTraceNo(Utils.parseLongSafe(content, -1));
                }

                if (transData == null) {
                    DialogUtils.showErrMessage(QueryMenuActivity.this, "Inquiry",
                            getString(R.string.err_no_orig_trans), null, Constants.FAILED_DIALOG_SHOW_TIME);
                    return;
                }

            }
        });

        return actionInputTransData;
    }

    private AAction createPrintPreview() {
        ActionPrintPreview actionPrintPreview = new ActionPrintPreview(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionPrintPreview) action).setParam(QueryMenuActivity.this, transData, PrintType.RECEIPT, true);
            }
        });
        return  actionPrintPreview;
    }

    @Override
    public void process(int index) {
        switch (index) {
            case 0:
                break;
            case 1:
                Intent intentForSuspendedQr = new Intent(this, TransQueryActivity.class);
                Bundle bundleForSuspendedQr = new Bundle();
                bundleForSuspendedQr.putString(EUIParamKeys.NAV_TITLE.toString(), getString(R.string.trans_history));
                bundleForSuspendedQr.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
                intentForSuspendedQr.putExtras(bundleForSuspendedQr);
                startActivity(intentForSuspendedQr);
                break;
            case 2:
                createInputTraceNoPageForQuery().execute();
                break;
            default:
                break;
        }
    }
}
