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
 * 20200825  	         xieYb                   Create
 * ===========================================================================================
 */
package com.evp.pay.record;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.evp.abl.core.ATransaction;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.AppConstants;
import com.evp.bizlib.card.PanUtils;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.ResourceUtil;
import com.evp.pay.app.ActivityStack;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.trans.AdjustTrans;
import com.evp.pay.trans.RedeemTrans;
import com.evp.pay.trans.SaleVoidTrans;
import com.evp.pay.trans.component.Component;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;

public class TransDetailAdapter extends PagedListAdapter<TransData, RecyclerView.ViewHolder> {
    private int mExpandedPosition = -1;
    private boolean supportDoTrans = true;
    protected TransDetailAdapter(@NonNull DiffUtil.ItemCallback<TransData> diffCallback, boolean supportDoTrans) {
        super(diffCallback);
        this.supportDoTrans = supportDoTrans;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trans_item, parent, false);
        return new TransHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final boolean isExpanded = position == mExpandedPosition;
        final TransHolder transHolder = (TransHolder) holder;
        transHolder.toggle.setBackground(ResourceUtil.getDrawable(com.pax.edc.expandablerecyclerview.R.drawable.touch_bg));
        transHolder.expandView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        transHolder.toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int old = mExpandedPosition;
                if (mExpandedPosition != -1) {
                    notifyItemChanged(mExpandedPosition);
                }
                mExpandedPosition = isExpanded ? -1 : transHolder.getAdapterPosition();
                notifyItemChanged(transHolder.getAdapterPosition());
                if (old != transHolder.getAdapterPosition()) {
                    ((RecyclerView) transHolder.itemView.getParent()).scrollToPosition(transHolder.getAdapterPosition());
                }
            }
        });
        TransData item = getItem(position);
        if (item == null) {
            return;
        }
        ETransType transType = ConvertUtils.enumValue(ETransType.class, item.getTransType());
        ETransType origTransType = ConvertUtils.enumValue(ETransType.class, item.getOrigTransType());
        if (transType == null) {
            return;
        }
        if (origTransType != null) {
            transHolder.transTypeTv.setText(String.format("%s(%s)", transType.getTransName(), origTransType.getTransName()));
        } else {
            transHolder.transTypeTv.setText(transType.getTransName());
        }

        //AET-18
        String amount;
//region OLS
        if (transType == ETransType.REDEEM || origTransType == ETransType.REDEEM) {
            String title = RedeemTrans.Companion.getPlanName(item.getPaymentPlan());
            transHolder.transPointTv.setText(item.getFormattedRedeemPts());
            transHolder.transPointTv.setVisibility(View.VISIBLE);
            if (origTransType != null) {
                transHolder.transTypeTv.setText(String.format("%s(%s)", transType.getTransName(), title));
            } else {
                transHolder.transTypeTv.setText(title);
            }
        }
//endregion
        if (!transType.isSymbolNegative()) {
            transHolder.transAmountTv.setTextColor(ResourceUtil.getColor(R.color.accent_amount));
            amount = CurrencyConverter.convert(Utils.parseLongSafe(item.getAmount(), 0), item.getCurrency());
            if (transType == ETransType.REDEEM || origTransType == ETransType.REDEEM)
                amount = item.getFormattedNetSaleAmt();
        } else {
            transHolder.transAmountTv.setTextColor(ResourceUtil.getColor(R.color.accent));
            amount = CurrencyConverter.convert(0 - Utils.parseLongSafe(item.getAmount(), 0), item.getCurrency()); //AET-18
            if (origTransType == ETransType.REFUND) {
                transHolder.transAmountTv.setTextColor(ResourceUtil.getColor(R.color.accent_amount));
                amount = CurrencyConverter.convert(Utils.parseLongSafe(item.getAmount(), 0), item.getCurrency()); //AET-18
            } else if (transType == ETransType.REDEEM || origTransType == ETransType.REDEEM) {
                amount = "-" + item.getFormattedNetSaleAmt();
            }
        }
        transHolder.transAmountTv.setText(amount);

        if (item.getIssuer() != null) {
            transHolder.transIssuerTv.setText(item.getIssuer().getName());
        }
        transHolder.transNoTv.setText(Component.getPaddedNumber(item.getTraceNo(), 6));

        String formattedDate = ConvertUtils.convert(item.getDateTime(), Constants.TIME_PATTERN_TRANS,
                Constants.TIME_PATTERN_DISPLAY2);
        transHolder.transDateTv.setText(formattedDate);

        if (transHolder.expandView.getVisibility() == View.VISIBLE) {
            updateExpandableLayout(item, transHolder);
        }
    }

    void updateExpandableLayout(final TransData transData, final TransHolder transHolder) {
        ETransType eTransType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
        if (eTransType == null) {
            return;
        }
        String state = getState(transData);
        String cardNo = getCardNo(transData);
        String authCode = transData.getAuthCode();
        String refNo = transData.getRefNo();

        transHolder.historyDetailState.setText(state);
        transHolder.historyDetailCardNo.setText(cardNo);
        transHolder.historyDetailAuthCode.setText(authCode != null ? authCode : "");
        transHolder.historyDetailRefNo.setText(refNo != null ? refNo : "");

        transHolder.historyTransActionLayout.setEnabled(supportDoTrans);

        if (transData.getOfflineSendState() != null &&
                transData.getOfflineSendState() == TransData.OfflineStatus.OFFLINE_SENT) {
            transHolder.voidBtn.setEnabled(!transData.getTransState().equals(TransData.ETransStatus.VOIDED));
            transHolder.adjustBtn.setEnabled(eTransType.isAdjustAllowed()
                    && !transData.getTransState().equals(TransData.ETransStatus.VOIDED) && AdjustTrans.isAdjustSupported(transData));
        } else if (transData.getTransState().equals(TransData.ETransStatus.NORMAL)) {
            transHolder.voidBtn.setEnabled(eTransType.isVoidAllowed());
            transHolder.adjustBtn.setEnabled(eTransType.isAdjustAllowed() && AdjustTrans.isAdjustSupported(transData));
        } else if (transData.getTransState().equals(TransData.ETransStatus.ADJUSTED) &&
                transData.getOfflineSendState() != null) {
            transHolder.voidBtn.setEnabled(eTransType.isVoidAllowed()
                    && transData.getOfflineSendState() == TransData.OfflineStatus.OFFLINE_SENT);
            transHolder.adjustBtn.setEnabled(eTransType.isAdjustAllowed() && AdjustTrans.isAdjustSupported(transData));
        } else {
            transHolder.voidBtn.setEnabled(false);
            transHolder.adjustBtn.setEnabled(false);
        }
        transHolder.reprintBtn.setEnabled(supportDoTrans);
        transHolder.voidBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SaleVoidTrans(ActivityStack.getInstance().top(), transData, null).execute();
            }
        });
        transHolder.adjustBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AdjustTrans(ActivityStack.getInstance().top(), transData, new ATransaction.TransEndListener() {
                    @Override
                    public void onEnd(ActionResult result) {
                        if (result.getRet() == TransResult.SUCC) {
                            FinancialApplication.getApp().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    notifyItemChanged(transHolder.getAdapterPosition());
                                }
                            });
                        }
                    }
                }).execute();
            }
        });
        transHolder.reprintBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FinancialApplication.getApp().runInBackground(new Runnable() {
                    @Override
                    public void run() {
                        PrinterUtils.printTransAgain(ActivityStack.getInstance().top(), transData);
                    }
                });
            }
        });
    }

    private String getState(TransData transData) {
        TransData.ETransStatus temp = transData.getTransState();
        String state = "";
        if (transData.isOnlineTrans()) {
            if (temp.equals(TransData.ETransStatus.NORMAL)) {
                state = ResourceUtil.getString(R.string.state_normal);
            } else if (temp.equals(TransData.ETransStatus.VOIDED)) {
                state = ResourceUtil.getString(R.string.state_voided);
            } else if (temp.equals(TransData.ETransStatus.ADJUSTED)) {
                state = getAdjustState(temp, transData.getOfflineSendState());
            }
        } else {
            state = getAdjustState(temp, transData.getOfflineSendState());
        }
        return state;
    }

    private String getAdjustState(TransData.ETransStatus transStatus, TransData.OfflineStatus offlineStatus) {
        String state;
        if (offlineStatus == TransData.OfflineStatus.OFFLINE_SENT) {
            state = ResourceUtil.getString(R.string.state_uploaded);
        } else if (offlineStatus == TransData.OfflineStatus.OFFLINE_NOT_SENT) {
            state = ResourceUtil.getString(R.string.state_not_sent);
        } else if (offlineStatus == TransData.OfflineStatus.OFFLINE_ERR_SEND) {
            state = ResourceUtil.getString(R.string.state_sent_error);
        } else if (offlineStatus == TransData.OfflineStatus.OFFLINE_ERR_RESP) {
            state = ResourceUtil.getString(R.string.state_response_error);
        } else {
            state = ResourceUtil.getString(R.string.state_unknow_error);
        }

        if (transStatus.equals(TransData.ETransStatus.ADJUSTED)) {
            state += "(" + ResourceUtil.getString(R.string.state_adjusted) + ")";
        }
        if (transStatus.equals(TransData.ETransStatus.VOIDED)) {
            state += "(" + ResourceUtil.getString(R.string.state_voided) + ")";
        }
        return state;
    }

    private String getCardNo(TransData transData) {
        Acquirer acquirer = transData.getAcquirer();
        if(AppConstants.QR_ACQUIRER.equals(acquirer.getName())) {
            return null;
        }

        String cardNo;
        ETransType eTransType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
        if (eTransType == ETransType.PREAUTH) {
            cardNo = transData.getPan();
        } else {
            if (!transData.isOnlineTrans()) {
                cardNo = transData.getPan();
            } else {
                cardNo = PanUtils.maskCardNo(transData.getPan(), transData.getIssuer().getPanMaskPattern());
            }
        }
        cardNo += " /" + transData.getEnterMode().toString();

        return cardNo;
    }

    private static class TransHolder extends RecyclerView.ViewHolder {

        private TextView transTypeTv;
        private TextView transAmountTv;
        private TextView transPointTv; //OLS
        private TextView transIssuerTv;
        private TextView transNoTv;
        private TextView transDateTv;
        private TextView historyDetailState;
        private TextView historyDetailCardNo;
        private TextView historyDetailAuthCode;
        private TextView historyDetailRefNo;
        private View historyTransActionLayout;
        private Button voidBtn;
        private Button adjustBtn;
        private Button reprintBtn;

        private LinearLayout toggle;
        private LinearLayout expandView;

        private TransHolder(@NonNull View itemView) {
            super(itemView);
            transTypeTv = (TextView) itemView.findViewById(R.id.trans_type_tv);
            transAmountTv = (TextView) itemView.findViewById(R.id.trans_amount_tv);
            transPointTv = (TextView) itemView.findViewById(R.id.trans_point_tv); //OLS
            transIssuerTv = (TextView) itemView.findViewById(R.id.issuer_type_tv);
            transNoTv = (TextView) itemView.findViewById(R.id.trans_no_tv);
            transDateTv = (TextView) itemView.findViewById(R.id.trans_date_tv);

            historyDetailState = (TextView) itemView.findViewById(R.id.history_detail_state);
            historyDetailCardNo = (TextView) itemView.findViewById(R.id.history_detail_card_no);
            historyDetailAuthCode = (TextView) itemView.findViewById(R.id.history_detail_auth_code);
            historyDetailRefNo = (TextView) itemView.findViewById(R.id.history_detail_ref_no);

            historyTransActionLayout = itemView.findViewById(R.id.history_trans_action);

            voidBtn = (Button) itemView.findViewById(R.id.history_trans_action_void);
            adjustBtn = (Button) itemView.findViewById(R.id.history_trans_action_adjust);
            reprintBtn = (Button) itemView.findViewById(R.id.history_trans_action_reprint);

            toggle = (LinearLayout) itemView.findViewById(R.id.trans_item_header);
            expandView = (LinearLayout) itemView.findViewById(R.id.expandable);
        }
    }
}
