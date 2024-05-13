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
package com.evp.pay;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import com.evp.abl.core.ATransaction;
import com.evp.bizlib.AppConstants;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.utils.LogUtils;
import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.eemv.utils.Converter;
import com.evp.pay.app.ActivityStack;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.menu.ManageMenuActivity;
import com.evp.pay.trans.AdjustTrans;
import com.evp.pay.trans.EchoTestTrans;
import com.evp.pay.trans.InstallmentTrans;
import com.evp.pay.trans.OfflineSaleTrans;
import com.evp.pay.trans.PreAuthCancelTrans;
import com.evp.pay.trans.PreAuthCompleteTrans;
import com.evp.pay.trans.PreAuthTrans;
import com.evp.pay.trans.PrintTrans;
import com.evp.pay.trans.QueryTrans;
import com.evp.pay.trans.RedeemTrans;
import com.evp.pay.trans.RefundTrans;
import com.evp.pay.trans.ReportTran;
import com.evp.pay.trans.SaleTrans;
import com.evp.pay.trans.SaleVoidTrans;
import com.evp.pay.trans.SettleTrans;
import com.evp.pay.trans.transmit.TransProcessListenerImpl;
import com.evp.pay.utils.EditorActionListener;
import com.evp.pay.utils.EnterAmountTextWatcher;
import com.evp.pay.utils.Utils;
import com.evp.pay.utils.ViewUtils;
import com.evp.payment.evpscb.R;
import com.evp.settings.SysParam;
import com.evp.view.MenuPage;
import com.evp.view.UserGuideManager;
import com.evp.view.dialog.DialogUtils;
import com.evp.view.keyboard.CustomKeyboardEditText;
import com.shizhefei.guide.GuideHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The type Main activity.
 */
public class MainActivity extends BaseActivity implements MenuPage.OnProcessListener {
    private CustomKeyboardEditText amountEdit;
    private MenuPage menuPage;
    private LinearLayout mLayout;
    private static volatile boolean freeForAutoSettle = false;

    private final ATransaction.TransEndListener transEndListener = result -> FinancialApplication.getApp().runOnUiThread(this::resetUI);

    @Override
    public void process(String index) {
        freeForAutoSettle = false;
        switch (index) {
            case ConfigConst.TRANSACTION_VOID:
                new SaleVoidTrans(MainActivity.this, transEndListener).execute();
                break;
            case ConfigConst.TRANSACTION_REFUND:
                new RefundTrans(MainActivity.this, transEndListener).execute();
                break;
            case ConfigConst.TRANSACTION_INSTALLMENT:
                new InstallmentTrans(MainActivity.this, "", InstallmentTrans.Plan.NO_IPP, "", "", "", transEndListener).setBackToMain(true).execute();
                break;
            case ConfigConst.TRANSACTION_SETTLE:
                new SettleTrans(MainActivity.this, null).execute();
                break;
            case ConfigConst.TRANSACTION_QUERY:
                new QueryTrans(MainActivity.this, transEndListener).execute();
                break;
            case ConfigConst.TRANSACTION_PRINT:
                new PrintTrans(MainActivity.this, transEndListener).execute();
                break;
            case ConfigConst.TRANSACTION_REPORT:
                new ReportTran(MainActivity.this, transEndListener).execute();
                break;
            case ConfigConst.TRANSACTION_REDEEM:
                new RedeemTrans(MainActivity.this, "", RedeemTrans.Plan.NO_REDEEM, "", "", transEndListener).setBackToMain(true).execute();
                break;
            case ConfigConst.TRANSACTION_POINT_INQUIRY:
                new RedeemTrans(MainActivity.this, "", RedeemTrans.Plan.ENQUIRY, "", "", transEndListener).setBackToMain(true).execute();
                break;
            case ConfigConst.TRANSACTION_MANAGEMENT:
                managementFunc();
                break;
            case ConfigConst.TRANSACTION_OFFLINE_SALE:
                new OfflineSaleTrans(MainActivity.this, transEndListener).execute();
                break;
            case ConfigConst.TRANSACTION_ADJUST:
                new AdjustTrans(MainActivity.this, transEndListener).execute();
                break;
            case ConfigConst.TRANSACTION_PRE_AUTH:
                new PreAuthTrans(MainActivity.this, false, transEndListener).execute();
                break;
            case ConfigConst.TRANSACTION_PRE_AUTH_CANCEL:
                new PreAuthCancelTrans(MainActivity.this, false, transEndListener).execute();
                break;
            case ConfigConst.TRANSACTION_PRE_AUTH_COMPLETE:
                new PreAuthCompleteTrans(MainActivity.this, false, transEndListener).execute();
                break;
            case ConfigConst.TRANSACTION_ECHO:
                new EchoTestTrans(MainActivity.this, transEndListener).execute();
                break;
            default:
                freeForAutoSettle = true;
                break;
        }
    }

    @Override
    public void process(int index) {
        LogUtils.i(TAG, "NOT IN USE!!!");
    }

    private class MainEditorActionListener extends EditorActionListener {

        private final WeakReference<MainActivity> weakRefMainAction;

        /**
         * Instantiates a new Main editor action listener.
         *
         * @param mainActivity the main activity
         */
        public MainEditorActionListener(MainActivity mainActivity) {
            this.weakRefMainAction = new WeakReference<>(mainActivity);
        }

        @Override
        public void onKeyCustomAction(int actionId) {
            MainActivity mainActivity = weakRefMainAction.get();
            if (null == mainActivity) {
                return;
            }

            String strAmount = "0.00";
            if(mainActivity.amountEdit.getText() != null) {
                strAmount = mainActivity.amountEdit.getText().toString().trim();
            }
            String amount = CurrencyConverter.parse(strAmount).toString();

            //Hide and reset keyboard.
            //Important to not trigger keyboard timeout during transaction because it will lead to unexpected call of onKeyCancel()
            //which will lead to unexpected possibility of auto settlement
            mainActivity.dismissAmountEdit();

            if (!"0".equals(amount)) {
                LogUtils.d(TAG, "amount:" + amount);
                switch (actionId) {
                    case EditorActionListener.QR_KEY_PRESSED:
                        new SaleTrans(MainActivity.this, AppConstants.SALE_TYPE_QR, amount, (byte) 20, true, transEndListener).execute();
                        break;
                    case EditorActionListener.SCAN_KEY_PRESSED:
                        new SaleTrans(MainActivity.this, AppConstants.SALE_TYPE_SCAN, amount, (byte) 20, true, transEndListener).execute();
                        break;
                    case EditorActionListener.CARD_KEY_PRESSED:
                        new SaleTrans(MainActivity.this, AppConstants.SALE_TYPE_CARD, amount, (byte)-1, true, transEndListener).execute();
                        break;
                }
            } else {
                mainActivity.dismissAmountEdit();
            }
        }

        @Override
        public void onKeyOk() { LogUtils.e(TAG, "Not in use!"); }

        @Override
        public void onKeyCancel() {
            MainActivity mainActivity = weakRefMainAction.get();
            if (null == mainActivity) {
                return;
            }
            mainActivity.dismissAmountEdit();
            freeForAutoSettle = true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        FinancialApplication.getApp().runInBackground(() -> {
            FinancialApplication.setAidParamList(Converter.toAidParams());
            FinancialApplication.setCapkList(Converter.toCapk());
        });
        super.onCreate(savedInstanceState);

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            ActivityStack.getInstance().pop();
        }
        Window window = MainActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && primaryColor != -1) {
            window.setStatusBarColor(primaryColor);
        }

        amountEdit.setOnClickListener(v -> {
            freeForAutoSettle = false;
            mLayout.setVisibility(View.INVISIBLE);
        });

        amountEdit.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                initUserGuideView();
                amountEdit.removeOnAttachStateChangeListener(this);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {

            }
        });

        startAutoSettlementThread();
    }

    private void initUserGuideView() {
        if (!UserGuideManager.getInstance().isEnabled()) {
            return;
        }
        final GuideHelper guideHelper = new GuideHelper(this);
        GuideHelper.TipData tipData = new GuideHelper.TipData(FinancialApplication.isJapanese() ? R.drawable.tip_enter_amount_jap : R.drawable.tip_enter_amount, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, amountEdit);
        tipData.setViewBg(getResources().getDrawable(R.drawable.white_bg));
        GuideHelper.TipData tipOk = new GuideHelper.TipData(FinancialApplication.isJapanese() ? R.drawable.tip_ok_btn_jap : R.drawable.tip_ok_btn_en, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        tipOk.setLocation(0, -ViewUtils.dp2px(150));
        tipOk.setOnClickListener(v -> {
            guideHelper.nextPage();
            UserGuideManager.getInstance().setEnable(false);
        });
        guideHelper.addPage(false, tipData, tipOk);
        guideHelper.show(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.d(TAG, "onResume: " + this);
        ActivityStack.getInstance().popTo(this);
        resetUI();
    }

    @Override
    protected void loadParam() {
        //If widget call MainActivity, need to show keyboard immediately.
        CurrencyConverter.setDefCurrency(SysParam.getInstance().getString(R.string.EDC_CURRENCY_LIST));
    }

    /**
     * reset MainActivity
     */
    private void resetUI() {
        menuPage.setCurrentPager(0);
        dismissAmountEdit();
        freeForAutoSettle = true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViews() {
        enableActionBar(false);
        LinearLayout main = findViewById(R.id.main);
        // set amount input box
        amountEdit = findViewById(R.id.amount_editText);
        amountEdit.setText("");

        mLayout = findViewById(R.id.ll_gallery);
        menuPage = createMenu();
        mLayout.addView(menuPage);

        if (primaryColor != -1) {
            amountEdit.setBackgroundDrawable(new ColorDrawable(primaryColor));
        }

        if (secondaryColor != -1) {
            main.setBackgroundColor(secondaryColor);
        }

        amountEdit.clearFocus();
    }

    /*
     * create menu
     */
    private MenuPage createMenu() {
        MenuPage.Builder builder = new MenuPage.Builder(MainActivity.this, 9, 3);
        ConfigUtils.getInstance().getMenu(builder, ConfigConst.MenuType.MAIN_MENU);
        return builder.create();
    }

    @Override
    protected void setListeners() {
        EnterAmountTextWatcher amountWatcher = new EnterAmountTextWatcher();
        amountEdit.addTextChangedListener(amountWatcher);
        amountEdit.setOnEditorActionListener(new MainEditorActionListener(this));
        menuPage.setOnProcessListener(this);
    }

    @Override
    protected boolean onKeyBackDown() {
        // exit current app
        DialogUtils.showExitAppDialog(MainActivity.this);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_ENTER == keyCode
                && KeyEvent.ACTION_DOWN == event.getAction()) {
            amountEdit.requestFocus();
        }
        return super.onKeyDown(keyCode, event);
    }

    private synchronized void dismissAmountEdit() {
        amountEdit.setText("");
        amountEdit.clearFocus();
        mLayout.setVisibility(View.VISIBLE);
    }

    private void managementFunc() {
        Intent intent = new Intent(this, ManageMenuActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(EUIParamKeys.NAV_TITLE.toString(), ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_MANAGEMENT));
        bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void startAutoSettlementThread() {
        if(!Boolean.parseBoolean(ConfigUtils.getInstance().getDeviceConf(ConfigConst.ENABLE_AUTO_SETTLEMENT))) {
            LogUtils.i(TAG, "Auto settlement is disabled.");
            return;
        }
        FinancialApplication.getApp().runInBackground(new Runnable() {
            @Override
            public void run() {
                final int EXTRA_SECONDS = 30;
                final int SHORT_SLEEP = 30000;
                final int LONG_SLEEP = 120000;
                boolean finishHim = false;
                while (!finishHim) {
                    final String settleTimeStr = ConfigUtils.getInstance().getDeviceConf(ConfigConst.AUTO_SETTLEMENT_TIME);
                    final Date settleTime = Utils.addTimeToDate(new Date(), settleTimeStr, "HH:mm");
                    final Date settleTimeMinusExtra = Utils.addSeconds(settleTime, -EXTRA_SECONDS);
                    final Date settleTimePlusExtra = Utils.addSeconds(settleTime, EXTRA_SECONDS);
                    final Date now = new Date();
                    if (now.equals(settleTime) || (now.before(settleTimePlusExtra) && now.after(settleTimeMinusExtra)))
                    {
                        LogUtils.i(TAG, "Auto settlement time happened.");
                        waitTillCanDoAutoSettle();
                        doAutoSettlement();
                        finishHim = Sleep(LONG_SLEEP);
                        continue;
                    }
                    finishHim = Sleep(SHORT_SLEEP);
                }
            }

            private void waitTillCanDoAutoSettle() {
                while(!freeForAutoSettle) {
                    Sleep(500);
                }
            }

            private void doAutoSettlement() {
                LogUtils.i(TAG, "Auto settlement START");

                TransProcessListenerImpl listener = new TransProcessListenerImpl(MainActivity.this);
                listener.onShowProgress(ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_AUTO_SETTLE), 4);
                Sleep(3500);
                listener.onHideProgress();

                ArrayList<String> allAcquirers = new ArrayList<>();
                List<Acquirer> Acquirers = FinancialApplication.getAcqManager().findAllAcquirers();
                for(Acquirer acq: Acquirers) {
                    allAcquirers.add(acq.getName());
                }
                new SettleTrans(MainActivity.this, null, allAcquirers, true).execute();

                LogUtils.i(TAG, "Auto settlement END");
            }

            private boolean Sleep(long timeoutMs) {
                try {
                    Thread.sleep(timeoutMs);
                } catch (InterruptedException e) {
                    LogUtils.i(TAG, "Interrupted!!!.");
                    return true;
                }
                return false;
            }
        });
    }
}
