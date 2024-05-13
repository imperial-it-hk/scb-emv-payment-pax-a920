/*
 *
 *  ============================================================================
 *  PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *  This software is supplied under the terms of a license agreement or nondisclosure
 *  agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *  disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2019 -? PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.
 *  Description:
 *  Revision History:
 *  Date	             Author	                Action
 *  20190417   	     ligq           	Create/Add/Modify/Delete
 *  ============================================================================
 *
 */

package com.evp.settings.adapter;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.view.View;

import com.evp.adapter.CommonAdapter;
import com.evp.adapter.ViewHolder;
import com.evp.bizlib.AppConstants;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.bizlib.ped.PedHelper;
import com.evp.device.Device;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.record.PrinterUtils;
import com.evp.pay.trans.model.Controller;
import com.evp.pay.trans.transmit.TransDigio;
import com.evp.pay.trans.transmit.TransOnline;
import com.evp.pay.trans.transmit.TransProcessListener;
import com.evp.pay.trans.transmit.TransProcessListenerImpl;
import com.evp.pay.utils.RxUtils;
import com.evp.pay.utils.ToastUtils;
import com.evp.pay.utils.TransResultUtils;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.view.dialog.CustomAlertDialog;
import com.evp.view.dialog.DialogUtils;
import com.pax.dal.exceptions.PedDevException;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * The type Config other adapter.
 *
 * @author ligq
 * @date 2019 /4/17 15:57
 */
public class ConfigOtherAdapter extends CommonAdapter<ConfigOtherAdapter.ItemOther> {
    private static final String CLEARTRADE_REVERSAL = Utils.getString(R.string.om_clearTrade_menu_reversal);
    private static final String CLEARTRADE_BATCH = Utils.getString(R.string.om_clearTrade_menu_clear_batch);
    private static final String CLEARTRADE_KEY = Utils.getString(R.string.om_clearTrade_menu_key);
    private static final String PRINT_AID = Utils.getString(R.string.om_paramPrint_menu_print_aid_para);
    private static final String PRINT_CAPK = Utils.getString(R.string.om_paramPrint_menu_print_capk_para);
    private static final String GO_DATE = Utils.getString(R.string.go_system_setting_date);
    private static final String TLE_KEYS_DOWNLOAD = Utils.getString(R.string.acq_tle_keys_download);
    private static final String TAG30_REGISTER = Utils.getString(R.string.tag30_register);
    private static final String QRCS_REGISTER = Utils.getString(R.string.qrcs_register);
    private static final String EXIT_APP = Utils.getString(R.string.exit_application);

    /**
     * Instantiates a new Config other adapter.
     *
     * @param context  the context
     * @param layoutId the layout id
     * @param dataList the data list
     */
    public ConfigOtherAdapter(@NotNull Context context, int layoutId, @NotNull List<ItemOther> dataList) {
        super(context, layoutId, dataList);
    }

    @Override
    protected void convert(@NotNull ViewHolder holder, final ItemOther itemOther, int position) {
        final String title = itemOther.getTitle();
        holder.setText(R.id.tv_item, title);
        holder.setImageResource(R.id.iv_item, itemOther.getIconId());

        holder.setOnClickListener(R.id.rl_item, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (title.equals(CLEARTRADE_REVERSAL) || title.equals(CLEARTRADE_BATCH) || title.equals(CLEARTRADE_KEY)) {
                    proceedWithClear(title);
                } else if (title.equals(PRINT_AID) || title.equals(PRINT_CAPK)) {
                    paraPrint(title);
                } else if (title.equals(GO_DATE)) {
                    Utils.callSystemSettings(mContext, Settings.ACTION_DATE_SETTINGS);
                } else if (title.equals(TLE_KEYS_DOWNLOAD)) {
                    tleKeysDownloadFunc();
                } else if (title.equals(TAG30_REGISTER)) {
                    registerTag30();
                } else if(title.equals(QRCS_REGISTER)){
                    registerQrcs();
                } else if(title.equals(EXIT_APP)){
                    exitApp();
                }
            }
        });
    }

    private void paraPrint(final String title) {
        RxUtils.runInBackgroud(new Runnable() {
            @Override
            public void run() {
                if (PRINT_CAPK.equals(title)) {
                    PrinterUtils.printCapk((Activity) mContext);
                } else {
                    PrinterUtils.printAid((Activity) mContext);
                }
                Device.beepOk();
            }
        });
    }

    private void proceedWithClear(final String title) {
        DialogUtils.showConfirmDialog(mContext,
                String.format("%s. %s", title, Utils.getString(R.string.question_are_you_sure)),
                null,
                alertDialog -> {
                    alertDialog.dismiss();
                    doClear(title);
                });
    }

    private void doClear(final String title) {
        final CustomAlertDialog dialog = DialogUtils.showProcessingMessage(mContext, Utils.getString(R.string.wait_process), -1);
        dialog.show();
        RxUtils.release();
        RxUtils.addDisposable(Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
                    boolean result = false;
                    if (title.equals(CLEARTRADE_REVERSAL)) {
                        result = GreendaoHelper.getTransDataHelper().deleteDupRecord();
                    } else if (title.equals(CLEARTRADE_BATCH)) {
                        FinancialApplication.getController().set(Controller.BATCH_UP_STATUS, Controller.Constant.WORKED);
                        FinancialApplication.getController().set(Controller.SETTLE_STATUS, Controller.Constant.WORKED);
                        FinancialApplication.getController().set(Controller.LAST_SETTLE_DATE, Utils.getStartOfDay(new Date()).getTime());
                        boolean isDone = GreendaoHelper.getTransDataHelper().deleteAllTransData();
                        GreendaoHelper.getTransTotalHelper().deleteAll();
                        FinancialApplication.getController().set(Controller.CLEAR_LOG, Controller.Constant.NO);
                        result = isDone;
                        //Increase batch No. for all acquirers
                        List<Acquirer> acquirers = FinancialApplication.getAcqManager().findAllAcquirers();
                        for(Acquirer acq : acquirers) {
                            int batchNo = acq.getCurrBatchNo();
                            if (batchNo >= Constants.MAX_BATCH_NO) {
                                batchNo = 0;
                            }
                            batchNo++;
                            acq.setCurrBatchNo(batchNo);
                            FinancialApplication.getAcqManager().updateAcquirer(acq);
                        }
                    } else if (title.equals(CLEARTRADE_KEY)) {
                        result = PedHelper.eraseKeys();
                    }
                    emitter.onNext(result);
                    emitter.onComplete();
                })
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aBoolean -> {
                            dialog.dismiss();
                            if (aBoolean) {
                                DialogUtils.showSuccMessage(mContext, "", null,
                                        Constants.SUCCESS_DIALOG_SHOW_TIME);
                            } else {
                                Device.beepErr();
                            }
                        }, throwable -> {
                            dialog.dismiss();
                            Device.beepErr();
                        })
        );
    }

    private void tleKeysDownloadFunc() {
        if (FinancialApplication.getController().isFirstRun()) {
            ToastUtils.showMessage(R.string.wait_2_init_device);
            return;
        }

        RxUtils.runInBackgroud(new Runnable() {
            @Override
            public void run() {
                Acquirer defaultAcq = FinancialApplication.getAcqManager().getCurAcq();
                List<Acquirer> allAcquirers = FinancialApplication.getAcqManager().findAllAcquirers();
                for (Acquirer acq : allAcquirers) {
                    if (!acq.getTleEnabled()) {
                        continue;
                    }
                    int ret = TransResult.ERR_ABORTED;
                    TransProcessListener listener = new TransProcessListenerImpl(mContext);
                    try {
                        FinancialApplication.getAcqManager().setCurAcq(acq);
                        if(acq.getName().equals(AppConstants.QR_ACQUIRER)) {
                            ret = new TransDigio().tleKeysDownload(listener, false);
                        } else {
                            ret = new TransOnline().tleKeysDownload(listener, false);
                        }
                    } catch (PedDevException e) {
                        listener.onShowErrMessage(e.getMessage(), Constants.FAILED_DIALOG_SHOW_TIME, false);
                    }
                    listener.onHideProgress();
                    if (ret == TransResult.SUCC) {
                        Device.beepOk();
                        listener.onShowNormalMessage(acq.getName()
                                        + " - "
                                        + Utils.getString(R.string.acq_tle_keys_download)
                                        + System.getProperty("line.separator")
                                        + Utils.getString(R.string.dialog_trans_succ),
                                Constants.SUCCESS_DIALOG_SHOW_TIME,
                                true);
                    } else if (ret != TransResult.ERR_ABORTED && ret != TransResult.ERR_HOST_REJECT) {
                        listener.onShowErrMessage(TransResultUtils.getMessage(ret),
                                Constants.FAILED_DIALOG_SHOW_TIME, false);
                    }
                }
                FinancialApplication.getAcqManager().setCurAcq(defaultAcq);
            }
        });
    }

    private void registerTag30() {
        if (FinancialApplication.getController().isFirstRun()) {
            ToastUtils.showMessage(R.string.wait_2_init_device);
            return;
        }
        RxUtils.runInBackgroud(new Runnable() {
            @Override
            public void run() {
            TransProcessListener listener = new TransProcessListenerImpl(mContext);

            int ret = new TransDigio().registerTag30(listener);

            listener.onHideProgress();
            if (ret == TransResult.SUCC) {
                Device.beepOk();
                listener.onShowNormalMessage(
                        Utils.getString(R.string.tag30_register)
                                + System.getProperty("line.separator")
                                + Utils.getString(R.string.dialog_trans_succ),
                        Constants.SUCCESS_DIALOG_SHOW_TIME,
                        true);
            } else if (ret != TransResult.ERR_ABORTED && ret != TransResult.ERR_HOST_REJECT) {
                listener.onShowErrMessage(TransResultUtils.getMessage(ret),
                        Constants.FAILED_DIALOG_SHOW_TIME, false);
            }
                }
        });
    }

    private void registerQrcs() {
        if (FinancialApplication.getController().isFirstRun()) {
            ToastUtils.showMessage(R.string.wait_2_init_device);
            return;
        }
        RxUtils.runInBackgroud(new Runnable() {
            @Override
            public void run() {
                TransProcessListener listener = new TransProcessListenerImpl(mContext);

                int ret = new TransDigio().registerQrcs(listener);

                listener.onHideProgress();
                if (ret == TransResult.SUCC) {
                    Device.beepOk();
                    listener.onShowNormalMessage(
                            Utils.getString(R.string.qrcs_register)
                                    + System.getProperty("line.separator")
                                    + Utils.getString(R.string.dialog_trans_succ),
                            Constants.SUCCESS_DIALOG_SHOW_TIME,
                            true);
                } else if (ret != TransResult.ERR_ABORTED && ret != TransResult.ERR_HOST_REJECT) {
                    listener.onShowErrMessage(TransResultUtils.getMessage(ret),
                            Constants.FAILED_DIALOG_SHOW_TIME, false);
                }
            }
        });
    }

    private void exitApp() {
        DialogUtils.showExitAppDialog(mContext);
    }

    /**
     * The type Item other.
     */
    @SuppressWarnings("unused")
    public static class ItemOther {
        private String title;
        private int iconId;

        /**
         * Instantiates a new Item other.
         */
        public ItemOther() {
        }

        /**
         * Instantiates a new Item other.
         *
         * @param title  the title
         * @param iconId the icon id
         */
        public ItemOther(String title, int iconId) {
            this.title = title;
            this.iconId = iconId;
        }

        /**
         * Gets title.
         *
         * @return the title
         */
        public String getTitle() {
            return title;
        }

        /**
         * Sets title.
         *
         * @param title the title
         */
        public void setTitle(String title) {
            this.title = title;
        }

        /**
         * Gets icon id.
         *
         * @return the icon id
         */
        public int getIconId() {
            return iconId;
        }

        /**
         * Sets icon id.
         *
         * @param iconId the icon id
         */
        public void setIconId(int iconId) {
            this.iconId = iconId;
        }
    }
}
