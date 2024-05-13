package com.evp.pay.trans.transmit;

import android.util.Base64;

import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.entity.TransTotal;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.bizlib.ped.PedHelper;
import com.evp.commonlib.utils.KeyUtils;
import com.evp.commonlib.utils.LogUtils;
import com.evp.device.Device;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.trans.component.Component;
import com.evp.pay.trans.model.Controller;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.pax.dal.exceptions.PedDevException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;

public class TransDigio {
    private static final String TAG = "TransDigio";

    private Digio digio = new Digio();

    public ArrayList<Object> perform(TransData transData, TransProcessListener listener) throws PedDevException {
        return digio.digio(transData, listener);
    }

    public int settle(TransTotal total, TransProcessListener listener) throws PedDevException {
        int ret;
        if (FinancialApplication.getController().get(Controller.BATCH_UP_STATUS) != Controller.Constant.BATCH_UP) {
            ret = settleRequest(total, listener);
            if (ret != TransResult.SUCC) {
                if (listener != null) {
                    listener.onHideProgress();
                }
                return ret;
            }
        }

        ret = batchUp(total,listener);
        if (ret != TransResult.SUCC) {
            if (listener != null) {
                listener.onHideProgress();
            }
            return ret;
        }

        return TransResult.SUCC;
    }

    private int settleRequest(TransTotal total, TransProcessListener listener) {

        TransData transData = Component.transInit();
        transData.setTransType(ETransType.SETTLE.name());
        transData.setProcCode(ETransType.SETTLE.getProcCode());
        if (listener != null) {
            listener.onUpdateProgressTitle(ETransType.SETTLE.getTransName());
        }

        ArrayList<Object> result = digio.digio(transData, listener);
        int ret = (int) result.get(0);
        if (listener != null) {
            listener.onHideProgress();
        }
        if (ret != TransResult.SUCC) {
            return ret;
        }

        FinancialApplication.getController().set(Controller.BATCH_UP_STATUS, Controller.Constant.BATCH_UP);
        FinancialApplication.getController().set(Controller.BATCH_NUM, 0);

        return TransResult.SUCC;
    }

    private int batchUp(TransTotal total, TransProcessListener listener) throws PedDevException {
        int ret;
        if (listener != null) {
            listener.onUpdateProgressTitle(ETransType.BATCH_UP.getTransName());
        }

        long cnt = GreendaoHelper.getTransDataHelper().countOf();
        if (cnt <= 0) {
            FinancialApplication.getController().set(Controller.BATCH_UP_STATUS, Controller.Constant.WORKED);
            return TransResult.ERR_NO_TRANS;
        }

        ret = batchUpEnd(total, listener);
        if (ret != TransResult.SUCC) {
            return ret;
        }

        //Renew TLE TWK keys for all acqurers
        Acquirer defaultAcq = FinancialApplication.getAcqManager().getCurAcq();
        Acquirer acq = total.getAcquirer();

        if(acq.getTleEnabled()) {
            FinancialApplication.getAcqManager().setCurAcq(acq);
            ret = tleKeysDownload(listener, true);
            if (ret != TransResult.SUCC) {
                FinancialApplication.getAcqManager().setCurAcq(defaultAcq);
                //settlement should be considered ok even tle key renew fail?
            } else {
                Device.beepOk();
                listener.onShowNormalMessage(acq.getName()
                                + " - "
                                + Utils.getString(R.string.acq_tle_keys_download)
                                + System.getProperty("line.separator")
                                + Utils.getString(R.string.dialog_trans_succ),
                        Constants.SUCCESS_DIALOG_SHOW_TIME,
                        true);
            }
        }

        return TransResult.SUCC;
    }

    private int batchUpEnd(TransTotal total, TransProcessListener listener) throws PedDevException {
        if (listener != null) {
            listener.onUpdateProgressTitle(ETransType.SETTLE_END.getTransName());
        }
        TransData transData = Component.transInit();

        transData.setTransType(ETransType.SETTLE_END.name());
        transData.setProcCode(ETransType.SETTLE_END.getProcCode());
        transData.setBatchNo(total.getAcquirer().getCurrBatchNo());

        ArrayList<Object> result = digio.digio(transData, listener);
        int ret = (int) result.get(0);
        return ret;
    }

    public int tleKeysDownload(TransProcessListener listener, boolean downloadOnlyTwk) throws PedDevException {
        TransData transData = Component.transInit();
        ArrayList<Object> result;
        int ret;

        String setId = transData.getAcquirer().getTleKeySetId();

        //Generate random EDC TMK
        byte[] edcTmk = new byte[16];
        new Random().nextBytes(edcTmk);

        //Save EDC TMK to secure memory
        PedHelper.writeTMK(KeyUtils.getTmkIndex(setId), edcTmk);
        PedHelper.writeCleanTDK(KeyUtils.getTsdkIndex(setId), edcTmk, null);

        if(!downloadOnlyTwk) {
            //TMK
            transData = Component.transInit();
            transData.setTransType(ETransType.TMK_DOWNLOAD.name());
            transData.setProcCode(ETransType.TMK_DOWNLOAD.getProcCode());
            LogUtils.i(TAG, "STARTED TLE TMK download for acquirer: " + transData.getAcquirer().getName());
            if (listener != null) {
                listener.onUpdateProgressTitle(transData.getAcquirer().getName() + " - " + ETransType.TMK_DOWNLOAD.getTransName());
            }

            result = digio.digio(transData, listener);
            ret = (int) result.get(0);
            if (ret != TransResult.SUCC) {
                if (listener != null) {
                    listener.onShowErrMessage(Utils.getString(R.string.err_tmk_download_failed), Constants.FAILED_DIALOG_SHOW_TIME, false);
                    listener.onHideProgress();
                }
                return ret;
            }

            //Parse TMK from response
            byte[] aesTmk = transData.getTmkKey();
            if (aesTmk != null) {
                try {
                    byte[] encryptedAesTmk = PedHelper.encTDes(KeyUtils.getTsdkIndex(setId), aesTmk);
                    PedHelper.writeTAESK(KeyUtils.getTmkIndex(setId), KeyUtils.getTaeskIndex(setId), encryptedAesTmk, null);
                } catch (PedDevException e) {
                    LogUtils.d(TAG, "Parsing Digio TMK failed...");
                    LogUtils.e(TAG, e);
                    return TransResult.ERR_PARAM;
                }
            } else {
                LogUtils.e(TAG, "TMK parse failed");
                return TransResult.ERR_UNPACK;
            }

            //Compare KCV for TMK
            byte[] tmkKcv = PedHelper.encAesCbc(KeyUtils.getTaeskIndex(setId), new byte[16], new byte[16]);
            String tmkKcvStr = null;
            try {
                tmkKcvStr = new String(Base64.encode(tmkKcv, Base64.NO_WRAP), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String recievedTmkKcv = transData.getTmkKcv();
            LogUtils.i(TAG, "Injected TMK KCV: " + tmkKcvStr);
            LogUtils.i(TAG, "Received TMK KCV: " + recievedTmkKcv);
            if (!tmkKcvStr.equals(recievedTmkKcv)) {
                LogUtils.e(TAG, "TMK KCV ERROR!");
                return TransResult.ERR_TLE_KCV_MISMATCH;
            }

            transData.getAcquirer().setDigioTmkKeyIndex(transData.getTmkKeyIndex());
            GreendaoHelper.getAcquirerHelper().update(transData.getAcquirer());
        }

        //TWK
        transData = Component.transInit();
        transData.setTransType(ETransType.TWK_DOWNLOAD.name());
        transData.setProcCode(ETransType.TWK_DOWNLOAD.getProcCode());

        LogUtils.i(TAG, "STARTED TLE TWK download for acquirer: " + transData.getAcquirer().getName());
        if (listener != null) {
            listener.onUpdateProgressTitle(transData.getAcquirer().getName() + " - " + ETransType.TWK_DOWNLOAD.getTransName());
        }
        result = digio.digio(transData, listener);
        ret = (int) result.get(0);
        if (ret != TransResult.SUCC) {
            if (listener != null) {
                listener.onShowErrMessage(Utils.getString(R.string.err_twk_download_failed), Constants.FAILED_DIALOG_SHOW_TIME, false);
                listener.onHideProgress();
            }
            return ret;
        }

        //Parse TWK from response
        byte[] aesTwk = transData.getDekKey();
        if (aesTwk != null) {
            try {
                byte[] encryptedAesTwk = PedHelper.encTDes(KeyUtils.getTsdkIndex(setId), aesTwk);
                PedHelper.writeTAESK(KeyUtils.getTmkIndex(setId), KeyUtils.getTsaeskIndex(setId), encryptedAesTwk, null);
            } catch (PedDevException e) {
                LogUtils.e(TAG, e);
                return TransResult.ERR_PARAM;
            }
        } else {
            LogUtils.e(TAG, "TWK parse failed");
            return TransResult.ERR_UNPACK;
        }

        //Compare KCV for TWK
        byte[] twkKcv = PedHelper.encAesCbc(KeyUtils.getTsaeskIndex(setId), new byte[16], new byte[16]);
        String twkKcvStr = null;
        try {
            twkKcvStr = new String(Base64.encode(twkKcv, Base64.NO_WRAP), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String recievedTwkKcv = transData.getDekKcv();
        LogUtils.i(TAG, "Injected TWK KCV: " + twkKcvStr);
        LogUtils.i(TAG, "Received TWK KCV: " + recievedTwkKcv);
        if (!twkKcvStr.equals(recievedTwkKcv)) {
            LogUtils.e(TAG, "TWK KCV ERROR!");
            return TransResult.ERR_TLE_KCV_MISMATCH;
        }

        transData.getAcquirer().setDigioDekKeyIndex(transData.getDekKeyIndex());
        GreendaoHelper.getAcquirerHelper().update(transData.getAcquirer());

        return TransResult.SUCC;
    }

    public int registerTag30(TransProcessListener listener) {
        TransData transData = Component.transInit();
        transData.setTransType(ETransType.QR_REGISTER_TAG30.name());

        ArrayList<Object> result;
        int ret;

        result = digio.digio(transData, listener);
        ret = (int) result.get(0);
        if (ret != TransResult.SUCC) {
            if (listener != null) {
                listener.onShowErrMessage(Utils.getString(R.string.err_tag30_register), Constants.FAILED_DIALOG_SHOW_TIME, false);
                listener.onHideProgress();
            }
            return ret;
        }

        return TransResult.SUCC;
    }

    public int registerQrcs(TransProcessListener listener) {
        TransData transData = Component.transInit();
        transData.setTransType(ETransType.QR_REGISTER_QRCS.name());

        ArrayList<Object> result;
        int ret;

        result = digio.digio(transData, listener);
        ret = (int) result.get(0);
        if (ret != TransResult.SUCC) {
            if (listener != null) {
                listener.onShowErrMessage(Utils.getString(R.string.err_qrcs_register), Constants.FAILED_DIALOG_SHOW_TIME, false);
                listener.onHideProgress();
            }
            return ret;
        }

        return TransResult.SUCC;
    }
}
