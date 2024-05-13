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
package com.evp.pay.record;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.evp.abl.core.AAction;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.card.PanUtils;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.invoke.InvokeConst;
import com.evp.invoke.InvokeResponseData;
import com.evp.invoke.InvokeSender;
import com.evp.pay.BaseActivity;
import com.evp.pay.app.ActivityStack;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.trans.TransContext;
import com.evp.pay.trans.action.ActionDispTransDetail;
import com.evp.pay.trans.action.ActionInputTransData;
import com.evp.pay.trans.action.ActionInputTransData.EInputType;
import com.evp.pay.trans.component.Component;
import com.evp.pay.utils.ToastUtils;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.settings.NewSpinnerAdapter;
import com.evp.view.PagerSlidingTabStrip;
import com.evp.view.dialog.DialogUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;


/**
 * The type Trans query activity.
 */
public class TransQueryActivity extends BaseActivity {

    private Acquirer acquirer = null;
    private NewSpinnerAdapter<Acquirer> adapter;
    private PagerAdapter pagerAdapter;

    private String navTitle;
    private boolean supportDoTrans = true;
    private BroadcastReceiver receiver;
    private boolean voidTransWasMade = false;

    @Override
    protected void loadParam() {
        String[] titles = new String[]{getString(R.string.history_detail), getString(R.string.history_total)};
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        supportDoTrans = getIntent().getBooleanExtra(EUIParamKeys.SUPPORT_DO_TRANS.toString(), true);

        List<Acquirer> listAcquirers = FinancialApplication.getAcqManager().findAllAcquirers();
        if (acquirer == null) {
            for (Acquirer acq : listAcquirers) {
                if (acq.getName().equals(FinancialApplication.getAcqManager().getCurAcq().getName())) {
                    acquirer = acq;
                    break;
                }
            }
        }

        adapter = new NewSpinnerAdapter<>(this);
        adapter.setListInfo(listAcquirers);
        adapter.setOnTextUpdateListener(new NewSpinnerAdapter.OnTextUpdateListener() {
            @Override
            public String onTextUpdate(final List<?> list, int position) {
                return ((Acquirer) list.get(position)).getName();
            }
        });
        pagerAdapter = new MyAdapter(getSupportFragmentManager(), titles);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_trans_query_layout;
    }

    @Override
    protected String getTitleString() {
        return navTitle;
    }

    @Override
    protected void initViews() {
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        Spinner spinner = (Spinner) findViewById(R.id.trans_history_acq_list);

        if (adapter.getCount() > 0) {
            spinner.setVisibility(View.VISIBLE);
            spinner.setAdapter(adapter);

            spinner.setSelection(adapter.getListInfo().indexOf(acquirer));
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int pos, long id) {
                    Acquirer newAcquirer = adapter.getListInfo().get(pos);
                    if (!newAcquirer.getId().equals(acquirer.getId())) {
                        acquirer = newAcquirer;
                        pagerAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Another interface callback
                }
            });
        }
        pager.setAdapter(pagerAdapter);
        tabs.setViewPager(pager);
        if (getIntent().getBooleanExtra(EUIParamKeys.IS_INVOKE.toString(), false)) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(InvokeConst.VOID_END_EVENT);
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) { voidTransWasMade = true; }
            };
            LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (voidTransWasMade) {
            finish();
        }
    }

    @Override
    public void finish() {
        if (receiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
            receiver = null;
            if(!voidTransWasMade) {
                InvokeResponseData.createResponseData(ETransType.TRANS_LOG, new ActionResult(TransResult.SUCC, null));
            }
            InvokeSender.send(this);
        }
        super.finish();
    }

    @Override
    protected void setListeners() {
        //do nothing
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.query_action, menu);

        if (!supportDoTrans) {
            menu.removeItem(R.id.history_menu_print_trans_last);
            menu.removeItem(R.id.history_menu_print_trans_detail);
            menu.removeItem(R.id.history_menu_print_trans_total);
            menu.removeItem(R.id.history_menu_print_last_total);
        }

        return super.onCreateOptionsMenu(menu);
    }


    private class MyAdapter extends FragmentPagerAdapter {
        private String[] titles;

        /**
         * Instantiates a new My adapter.
         *
         * @param fm     the fm
         * @param titles the titles
         */
        MyAdapter(FragmentManager fm, String[] titles) {
            super(fm);
            this.titles = titles;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new TransDetailFragment();
                case 1:
                    return new TransTotalFragment();
                default:
                    return null;
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            switch (position) {
                case 0:
                    container.setBackgroundColor(secondaryColor);
                    TransDetailFragment f1 = (TransDetailFragment) super.instantiateItem(container, position);
                    f1.setAcquirerName(acquirer.getName());
                    f1.setSupportDoTrans(supportDoTrans);
                    return f1;
                case 1:
                    container.setBackgroundColor(secondaryColor);
                    TransTotalFragment f2 = (TransTotalFragment) super.instantiateItem(container, position);
                    f2.setAcquirerName(acquirer.getName());
                    return f2;
                default:
                    return null;
            }
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }

    @Override
    public void onClickProtected(View v) {
        // do nothing
    }

    @Override
    public boolean onOptionsItemSelectedSub(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(100);
                finish();
                return true;
            case R.id.history_menu_search:
                queryTransRecordByTransNo();
                return true;
            case R.id.history_menu_print_trans_last:
                FinancialApplication.getApp().runInBackground(new Runnable() {
                    @Override
                    public void run() {
                        int result = PrinterUtils.printLastTrans(TransQueryActivity.this);
                        if (result != TransResult.SUCC) {
                            DialogUtils.showErrMessage(TransQueryActivity.this,
                                    getString(R.string.transType_print), getString(R.string.err_no_trans),
                                    null, Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                    }
                });
                return true;
            case R.id.history_menu_print_trans_detail:
                FinancialApplication.getApp().runInBackground(new Runnable() {
                    @Override
                    public void run() {
                        //AET-112
                        int result = PrinterUtils.printAuditReport(TransQueryActivity.this, acquirer);
                        if (result != TransResult.SUCC) {
                            DialogUtils.showErrMessage(TransQueryActivity.this,
                                    getString(R.string.transType_print), getString(R.string.err_no_trans),
                                    null, Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                    }
                });
                return true;
            case R.id.history_menu_print_trans_total:
                FinancialApplication.getApp().runInBackground(new Runnable() {
                    @Override
                    public void run() {
                        int result = PrinterUtils.printSummaryReport(TransQueryActivity.this, acquirer);
                        if (result != TransResult.SUCC) {
                            DialogUtils.showErrMessage(TransQueryActivity.this,
                                    getString(R.string.transType_print), getString(R.string.err_no_trans),
                                    null, Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                    }
                });
                return true;
            case R.id.history_menu_print_last_total:
                FinancialApplication.getApp().runInBackground(new Runnable() {
                    @Override
                    public void run() {
                        int result = PrinterUtils.printLastTransTotal(TransQueryActivity.this, acquirer);
                        if (result != TransResult.SUCC) {
                            DialogUtils.showErrMessage(TransQueryActivity.this,
                                    getString(R.string.transType_print), getString(R.string.err_no_trans),
                                    null, Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelectedSub(item);
        }
    }

    private void queryTransRecordByTransNo() {

        final ActionInputTransData inputTransDataAction = new ActionInputTransData(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputTransData) action).setParam(TransQueryActivity.this,
                        getString(R.string.trans_history)).setInputLine(getString(R.string.prompt_input_transno),
                        EInputType.NUM, 6, false);
            }

        });

        inputTransDataAction.setEndListener(new AAction.ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult result) {
                inputTransDataAction.setFinished(false);
                TransContext.getInstance().setCurrentAction(null);
                if (result.getRet() != TransResult.SUCC) {
                    ActivityStack.getInstance().pop();
                    return;
                }

                String content = (String) result.getData();
                if (content == null || content.isEmpty()) {
                    ToastUtils.showMessage(R.string.please_input_again);
                    return;
                }
                long transNo = Utils.parseLongSafe(content, -1);
                TransData transData = GreendaoHelper.getTransDataHelper().findTransDataByTraceNo(transNo);

                if (transData == null) {
                    ToastUtils.showMessage(R.string.err_no_orig_trans);
                    return;
                }

                final LinkedHashMap<String, String> map = prepareValuesForDisp(transData);

                final ActionDispTransDetail dispTransDetailAction = new ActionDispTransDetail(
                        new AAction.ActionStartListener() {

                            @Override
                            public void onStart(AAction action) {

                                ((ActionDispTransDetail) action).setParam(TransQueryActivity.this,
                                        getString(R.string.trans_history), map, null);

                            }
                        });
                dispTransDetailAction.setEndListener(new AAction.ActionEndListener() {

                    @Override
                    public void onEnd(AAction action, ActionResult result) {
                        ActivityStack.getInstance().popTo(TransQueryActivity.this);
                        dispTransDetailAction.setFinished(false);
                        TransContext.getInstance().setCurrentAction(null);
                    }
                });

                dispTransDetailAction.execute();
            }
        });

        inputTransDataAction.execute();

    }

    private LinkedHashMap<String, String> prepareValuesForDisp(TransData transData) {

        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
        String amount;
        if (Objects.requireNonNull(transType).isSymbolNegative()) {
            amount = CurrencyConverter.convert(0 - Utils.parseLongSafe(transData.getAmount(), 0), transData.getCurrency());
        } else {
            amount = CurrencyConverter.convert(Utils.parseLongSafe(transData.getAmount(), 0), transData.getCurrency());
        }

        String formattedDate = ConvertUtils.convert(transData.getDateTime(), Constants.TIME_PATTERN_TRANS,
                Constants.TIME_PATTERN_DISPLAY);

        map.put(getString(R.string.history_detail_type), transType.getTransName());
        map.put(getString(R.string.history_detail_amount), amount);
        map.put(getString(R.string.history_detail_card_no), PanUtils.maskCardNo(transData.getPan(), transData.getIssuer().getPanMaskPattern()));
        map.put(getString(R.string.history_detail_auth_code), transData.getAuthCode());
        map.put(getString(R.string.history_detail_ref_no), transData.getRefNo());
        map.put(getString(R.string.history_detail_trace_no), Component.getPaddedNumber(transData.getTraceNo(), 6));
        map.put(getString(R.string.dateTime), formattedDate);
        return map;
    }

}
