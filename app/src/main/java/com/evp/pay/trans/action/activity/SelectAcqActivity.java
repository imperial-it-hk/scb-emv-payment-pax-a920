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
 * 20190108  	         xieYb                   Create
 * ===========================================================================================
 */
package com.evp.pay.trans.action.activity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.TransTotal;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.utils.LogUtils;
import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.mvp.contract.SelectAcqContract;
import com.evp.mvp.presenter.SelectAcqPresenter;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.utils.ToastUtils;
import com.evp.payment.evpscb.R;
import com.pax.edc.expandablerecyclerview.BaseViewHolder;
import com.pax.edc.expandablerecyclerview.ExpandItemAnimator;
import com.pax.edc.expandablerecyclerview.ExpandableRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The type Select acq activity.
 */
public class SelectAcqActivity extends BaseActivityWithTickForAction implements SelectAcqContract.View {
    /**
     * The constant ACQ_NAME.
     */
    public static final String ACQ_NAME = "acq_name";
    private CheckBox mCheck;
    private Button mSettle;
    private RecyclerView mRecyclerView;

    private ExpandableRecyclerAdapter<Map<String, String>> acquirerListAdapter;

    private List<String> checkedAcqs;

    private SelectAcqPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        presenter = new SelectAcqPresenter(this);
        presenter.attachView(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_selectacq_layout;
    }

    @Override
    protected void loadParam() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            checkedAcqs = bundle.getStringArrayList(Constants.ACQUIRER_NAME);
            presenter.initParam((ArrayList<String>) checkedAcqs);
        }
    }

    @Override
    protected void initViews() {
        mSettle = findViewById(R.id.select_acq_settle);
        mSettle.setText(ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_SETTLE));
        mSettle.setBackgroundColor(primaryColor);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.root);
        relativeLayout.setBackgroundColor(secondaryColor);
        mSettle.requestFocus();
        mCheck = (CheckBox) findViewById(R.id.item_select_acq_check);
        acquirerListAdapter = new ExpandableRecyclerAdapter<>(SelectAcqActivity.this, R.layout.selectacq_item,
                new ExpandableRecyclerAdapter.ItemViewListener<Map<String, String>>() {
                    @Override
                    public BaseViewHolder<Map<String, String>> generate(View view) {
                        return new AcqSettleViewHolder(view);
                    }
                })
                .setDataBeanList(new ArrayList<Map<String, String>>());
        mRecyclerView = (RecyclerView) findViewById(R.id.select_acq_list);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(acquirerListAdapter);
        mRecyclerView.setItemAnimator(new ExpandItemAnimator());

        confirmBtnChange(presenter.checkedAcqs.isEmpty());
        presenter.initAdapterData();
    }

    @Override
    protected String getTitleString() {
        return getString(R.string.settle_select_acquirer);
    }

    @Override
    protected void setListeners() {
        mSettle.setOnClickListener(this);
        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                tickTimer.start();
                return false;
            }
        });
        mCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tickTimer.start();
                //AET-39
                presenter.selectAcqCheck(isChecked);
            }
        });
        if (presenter.checkedAcqs.size() == FinancialApplication.getAcqManager().findAllAcquirers().size()) {
            mCheck.setChecked(true);
        }
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }

    @Override
    protected void onClickProtected(View v) {
        if (v.getId() == R.id.select_acq_settle) {
            presenter.finish2SettleAcq();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }

    @Override
    public void showAdapterData(ArrayList<Map<String, String>> myListArray) {
        acquirerListAdapter.setDataBeanList(myListArray);
        acquirerListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSelectAcqCheck() {
        confirmBtnChange(!presenter.checkedAcqs.isEmpty());
        acquirerListAdapter.notifyDataSetChanged();
    }

    @Override
    public void finish(ActionResult result) {
        super.finish(result);
    }

    @Override
    public void showToast(int message) {
        ToastUtils.showMessage(message);
    }

    @Override
    public void confirmBtnChange(boolean confirm) {
        mSettle.setEnabled(confirm);
    }

    @Override
    public void setChecked(boolean checked) {
        mCheck.setChecked(checked);
    }

    private class AcqSettleViewHolder extends BaseViewHolder<Map<String, String>> {

        private TextView textView;
        private CheckBox checkBox;
        private TextView acqName;
        private TextView merchantName;
        private TextView merchantId;
        private TextView terminalId;
        private TextView batchNo;
        private TextView saleSum;
        private TextView saleAmt;
        private TextView refundSum;
        private TextView refundAmt;
        private TextView voidSaleSum;
        private TextView voidSaleAmt;
        private TextView voidRefundSum;
        private TextView voidRefundAmt;
        private TextView offlineSum;
        private TextView offlineAmt;
        private Button settleConfirm;

        /**
         * Instantiates a new Acq settle view holder.
         *
         * @param itemView the item view
         */
        public AcqSettleViewHolder(View itemView) {
            super(itemView, R.id.select_acq_item_header, R.id.expandable);
        }

        @Override
        protected void initView() {
            textView = (TextView) itemView.findViewById(R.id.expandable_toggle_button);
            checkBox = (CheckBox) itemView.findViewById(R.id.item_select_acq_check);

            acqName = (TextView) itemView.findViewById(R.id.settle_acquirer_name);
            merchantName = (TextView) itemView.findViewById(R.id.settle_merchant_name);
            merchantId = (TextView) itemView.findViewById(R.id.settle_merchant_id);
            terminalId = (TextView) itemView.findViewById(R.id.settle_terminal_id);
            batchNo = (TextView) itemView.findViewById(R.id.settle_batch_num);

            saleSum = (TextView) itemView.findViewById(R.id.sale_total_sum);
            saleAmt = (TextView) itemView.findViewById(R.id.sale_total_amount);
            refundSum = (TextView) itemView.findViewById(R.id.refund_total_sum);
            refundAmt = (TextView) itemView.findViewById(R.id.refund_total_amount);

            voidSaleSum = (TextView) itemView.findViewById(R.id.void_sale_total_sum);
            voidSaleAmt = (TextView) itemView.findViewById(R.id.void_sale_total_amount);
            voidRefundSum = (TextView) itemView.findViewById(R.id.void_refund_total_sum);
            voidRefundAmt = (TextView) itemView.findViewById(R.id.void_refund_total_amount);
            offlineSum = (TextView) itemView.findViewById(R.id.offline_total_sum);
            offlineAmt = (TextView) itemView.findViewById(R.id.offline_total_amount);

            settleConfirm = (Button) itemView.findViewById(R.id.settle_confirm);

            textView.setText(ConfigUtils.getInstance().getString("allLabel"));
        }

        @Override
        protected void setListener() {
            settleConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    presenter.settle(findAcquirer(getAdapterPosition()));
                }
            });
        }

        @Override
        public void bindView(final Map<String, String> dataBean, final BaseViewHolder viewHolder, final int pos) {
            final String acquireName = dataBean.get(ACQ_NAME);
            textView.setText(acquireName);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    tickTimer.start();
                    LogUtils.d("SelectAcq", "onCheckedChanged  " + pos);
                    presenter.onCheckedChange(acquireName, isChecked);
                }
            });

            //AET-39
            checkBox.setChecked(presenter.checkedAcqs.contains(acquireName));

            if (viewHolder.getExpandView().getVisibility() == View.VISIBLE) {
                updateValueTable(pos);
            }
        }

        private void updateValueTable(final int position) {
            String acquirerName = findAcquirer(position);
            Acquirer acquirer = FinancialApplication.getAcqManager().findAcquirer(acquirerName);
            TransTotal total = GreendaoHelper.getTransTotalHelper().calcTotal(acquirer);

            acqName.setText(acquirer.getName());
            merchantName.setText(getString(R.string.settle_merchant_name));
            merchantId.setText(acquirer.getMerchantId());
            terminalId.setText(acquirer.getTerminalId());
            batchNo.setText(String.valueOf(acquirer.getCurrBatchNo()));

            String saleAmtStr = CurrencyConverter.convert(total.getSaleTotalAmt());
            //AET-18
            String refundAmtStr = CurrencyConverter.convert(0 - total.getRefundTotalAmt());
            String voidSaleAmtStr = CurrencyConverter.convert(0 - total.getSaleVoidTotalAmt());
            String voidRefundAmtStr = CurrencyConverter.convert(0 - total.getRefundVoidTotalAmt());
            String offlineAmtStr = CurrencyConverter.convert(total.getOfflineTotalAmt());

            saleSum.setText(String.valueOf(total.getSaleTotalNum()));
            saleAmt.setText(saleAmtStr);
            refundSum.setText(String.valueOf(total.getRefundTotalNum()));
            refundAmt.setText(refundAmtStr);

            voidSaleSum.setText(String.valueOf(total.getSaleVoidTotalNum()));
            voidSaleAmt.setText(voidSaleAmtStr);
            voidRefundSum.setText(String.valueOf(total.getRefundVoidTotalNum()));
            voidRefundAmt.setText(voidRefundAmtStr);
            offlineAmt.setText(offlineAmtStr);
            offlineSum.setText(String.valueOf(total.getOfflineTotalNum()));
        }

        private String findAcquirer(int position) {
            return acquirerListAdapter.getDataBeanList().get(position).get(ACQ_NAME);
        }
    }

}
