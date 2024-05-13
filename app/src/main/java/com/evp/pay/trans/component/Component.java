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
package com.evp.pay.trans.component;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.StatFs;

import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.EmvTerminal;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.entity.TransData.ETransStatus;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.data.model.SearchMode;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.bizlib.ped.PedHelper;
import com.evp.commonlib.currency.CountryCode;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.KeyUtils;
import com.evp.commonlib.utils.LogUtils;
import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.device.Device;
import com.evp.eemv.entity.ClssInputParam;
import com.evp.eemv.entity.Config;
import com.evp.eemv.entity.InputParam;
import com.evp.eemv.enums.ECvmResult;
import com.evp.eemv.enums.EFlowType;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.trans.model.Controller;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.poslib.gl.convert.ConvertHelper;
import com.evp.poslib.gl.convert.IConvert;
import com.evp.poslib.neptune.Sdk;
import com.evp.settings.SysParam;
import com.evp.view.dialog.CustomAlertDialog;
import com.pax.dal.entity.EPedKeyType;

import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * The type Component.
 */
public class Component {

    private static final String TAG = "Component";

    private Component() {
        //do nothing
    }

    /**
     * 交易预处理，检查是否签到， 是否需要结束， 是否继续批上送， 是否支持该交易， 是否需要参数下载
     *
     * @param context   unused
     * @param transType {@link ETransType}
     * @return {@link TransResult}
     */
    public static int transPreDeal(final Context context, ETransType transType) {
        if (!isNeedPreDeal(transType)) {
            return TransResult.SUCC;
        }
        if (!hasFreeSpace()) {
            return TransResult.ERR_NO_FREE_SPACE;
        }
        int ret = checkSettle();
        if (ret != TransResult.SUCC) {
            return ret;
        }
        if (isNeedBatchUp()) {
            return TransResult.ERR_BATCH_UP_NOT_COMPLETED;
        }
        if (!isSupportTran(transType)) {
            return TransResult.ERR_NOT_SUPPORT_TRANS;
        }
        if (!areTleKeysInjected()) {
            return TransResult.ERR_TLE_KEYS_MISSING;
        }

        return ret;
    }


    /**
     * 获取读卡方式
     *
     * @param transType ：交易类型{@link ETransType}
     * @return {@link SearchMode}
     */
    public static byte getCardReadMode(ETransType transType) {
        byte mode = transType.getReadMode();
        if (mode == 0) {
            return mode;
        }
        mode &= ~SearchMode.QR; // This is here for historical purposes only. Let it be here as it's
        return mode;
    }

    /**
     * 根据交易类型、冲正标识确认当前交易是否预处理
     *
     * @param transType {@link ETransType}
     * @return true:需要预处理 false:不需要预处理 备注：签到，签退，结算，参数下发，公钥下载，冲正类不需要预处理,新增交易类型时，需修改添加交易类型判断
     */
    private static boolean isNeedPreDeal(ETransType transType) {
        if(transType == ETransType.ECHO || transType == ETransType.SETTLE) {
            return false;
        }
        return true;
    }

    /**
     * 检查是否达结算要求
     *
     * @return 0：不用结算 1：结算提醒,立即 2：结算提醒，稍后 3：结算提醒,空间不足
     */
    private static int checkSettle() {
        long cnt = GreendaoHelper.getTransDataHelper().countOf();
        long maxCnt = SysParam.getInstance().getInt(R.string.MAX_TRANS_COUNT, Integer.MAX_VALUE);
        if (cnt >= maxCnt) {
            if (cnt >= maxCnt + 10) {
                return TransResult.ERR_NEED_SETTLE_NOW;
            } else {
                return TransResult.ERR_NEED_SETTLE_LATER;
            }
        }

        if(Boolean.parseBoolean(ConfigUtils.getInstance().getDeviceConf(ConfigConst.ENABLE_FORCE_SETTLEMENT))) {
            if (FinancialApplication.getController().get(Controller.SETTLE_STATUS) == Controller.Constant.SETTLE) {
                return TransResult.ERR_LAST_SETTLE_FAILED;
            }

            Date lastSettleDate = new Date(FinancialApplication.getController().getLong(Controller.LAST_SETTLE_DATE));
            Date today = Utils.getStartOfDay(new Date());
            Date yesterday = Utils.addDays(today, -1);
            if (!lastSettleDate.equals(today) && !lastSettleDate.equals(yesterday)) {
                return TransResult.ERR_NO_SETTLE_FROM_YESTERDAY;
            }
        }

        return TransResult.SUCC;
    }

    /**
     * 判断是否有剩余空间
     *
     * @return true: 有空间 false：无空间
     */
    @SuppressWarnings("deprecation")
    private static boolean hasFreeSpace() {
        File dataPath = Environment.getDataDirectory();
        StatFs dataFs = new StatFs(dataPath.getPath());
        long sizes = (long) dataFs.getFreeBlocks() * (long) dataFs.getBlockSize();
        long available = sizes / (1024 * 1024);
        return available > 1;
    }

    private static boolean isNeedBatchUp() {
        return FinancialApplication.getController().get(Controller.BATCH_UP_STATUS) == Controller.Constant.BATCH_UP;
    }

    /**
     * 判断是否支持该交易
     *
     * @param transType {@link ETransType}
     */
    private static boolean isSupportTran(ETransType transType) {
        switch (transType) {
            case SALE:
                return SysParam.getInstance().getBoolean(R.string.TTS_SALE);
            case VOID:
                return SysParam.getInstance().getBoolean(R.string.TTS_VOID);
            case REFUND:
                return SysParam.getInstance().getBoolean(R.string.TTS_REFUND);
            case PREAUTH:
                return SysParam.getInstance().getBoolean(R.string.TTS_PREAUTH);
            default:
                break;
        }

        return true;
    }

    private static boolean areTleKeysInjected() {
        Acquirer defaultAcq = FinancialApplication.getAcqManager().getCurAcq();
        List<Acquirer> allAcquirers = FinancialApplication.getAcqManager().findAllAcquirers();
        for (Acquirer acq : allAcquirers) {
            if (!acq.getTleEnabled()) {
                continue;
            }
            if(!PedHelper.isKeyInjected(EPedKeyType.TMK, KeyUtils.getTmkIndex(acq.getTleKeySetId()))) {
                return false;
            }
        }
        FinancialApplication.getAcqManager().setCurAcq(defaultAcq);
        return true;
    }

    /**
     * convert {@link TransData} to {@link InputParam} for EMV and CLSS
     *
     * @param transData {@link TransData}
     * @return {@link InputParam}
     */
    public static InputParam toInputParam(TransData transData) {
        InputParam inputParam = new InputParam();
        convertTransData2InputParam(transData, inputParam);
        return inputParam;
    }

    /**
     * To clss input param clss input param.
     *
     * @param transData the trans data
     * @return the clss input param
     */
    public static ClssInputParam toClssInputParam(TransData transData) {
        ClssInputParam inputParam = new ClssInputParam();
        convertTransData2InputParam(transData, inputParam);
        inputParam.setAmtZeroNoAllowedFlg(1);
        inputParam.setCrypto17Flg(true);
        inputParam.setStatusCheckFlg(false);
        inputParam.setReaderTTQ("3600C000");//36008000
        inputParam.setDomesticOnly(false);
        List<ECvmResult> list = new ArrayList<>();
        list.add(ECvmResult.REQ_SIG);
        list.add(ECvmResult.REQ_ONLINE_PIN);
        inputParam.setCvmReq(list);
        inputParam.setEnDDAVerNo((byte) 0);
        return inputParam;
    }

    private static void convertTransData2InputParam(TransData transData, InputParam inputParam) {
        ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());

        String amount = transData.getAmount();
        if (amount == null || amount.isEmpty()) {
            amount = "0";
        }
        inputParam.setAmount(amount);
        inputParam.setCashBackAmount("0");
        inputParam.setPciTimeout(60 * 1000);

        if(transType == ETransType.OFFLINE_SALE) {
            inputParam.setFlowType(EFlowType.SIMPLE);
            inputParam.setEnableCardAuth(false);
            inputParam.setSupportCVM(false);
        } else {
            inputParam.setFlowType(EFlowType.COMPLETE);
        }

        byte[] procCode = ConvertHelper.getConvert()
                .strToBcdPaddingRight(transType != null ? transType.getProcCode() : null);
        inputParam.setTag9CValue(procCode[0]);

        // 根据交易类型判断是否强制联机
        inputParam.setForceOnline(true);

        inputParam.setTransDate(transData.getDateTime().substring(0, 8));
        inputParam.setTransTime(transData.getDateTime().substring(8));
        inputParam.setTransTraceNo(Component.getPaddedNumber(transData.getTraceNo(), 6));

    }

    /**
     * Get emv config.
     *
     * @return the config
     */
    public static Config getEmvConfig() {
        Config cfg = new Config();
        Currency current = Currency.getInstance(CurrencyConverter.getDefCurrency());
        String currency = String.valueOf(
                CountryCode.getByCode(CurrencyConverter.getDefCurrency().getCountry()).getCurrencyNumeric());
        String country = String.valueOf(
                CountryCode.getByCode(CurrencyConverter.getDefCurrency().getCountry()).getNumeric());

        cfg.setCountryCode(country);
        cfg.setForceOnline(false);
        cfg.setGetDataPIN(true);
        cfg.setReferCurrCode(currency);
        cfg.setReferCurrCon(1000);
        cfg.setReferCurrExp((byte) current.getDefaultFractionDigits());
        cfg.setSurportPSESel(true);
        cfg.setTransCurrCode(currency);
        cfg.setTransCurrExp((byte) current.getDefaultFractionDigits());
        cfg.setTermId(FinancialApplication.getAcqManager().getCurAcq().getTerminalId());
        cfg.setMerchId(FinancialApplication.getAcqManager().getCurAcq().getMerchantId());
        cfg.setMerchName(SysParam.getInstance().getString(R.string.EDC_MERCHANT_NAME_EN));
        cfg.setTermAIP("0800");
        cfg.setBypassPin(true);
        cfg.setBatchCapture((byte) 1);
        cfg.setUseTermAIPFlag(true);
        cfg.setBypassAllFlag(true);
        cfg.setTransType((byte) 0);

        EmvTerminal emvTerminalData = GreendaoHelper.getEmvTerminalHelper().findEmvTerminal();
        cfg.setTermType(ConvertUtils.asciiToBin(emvTerminalData.getTerminalType())[0]);
        cfg.setCapability("E008C8"); //Dummy value will be overwritten by AID terminal capabilities once we get AID
        cfg.setExCapability(emvTerminalData.getTerminalAdditionalCapabilities());
        cfg.setMerchCateCode(emvTerminalData.getMerchantCategoryCode());
        return cfg;
    }

    /**
     * 流水号+1
     */
    public static void incTransNo() {
        long transNo = SysParam.getInstance().getInt(R.string.EDC_TRACE_NO);
        if (transNo >= Constants.MAX_TRANS_NO) {
            transNo = 0;
        }
        transNo++;
        SysParam.getInstance().set(R.string.EDC_TRACE_NO, transNo);
        LogUtils.i(TAG, "Increased Transaction NO to: " + transNo);
    }

    public static void incStanNo() {
        long stanNo = SysParam.getInstance().getInt(R.string.EDC_STAN_NO);
        if (stanNo >= Constants.MAX_STAN_NO) {
            stanNo = 0;
        }
        stanNo++;
        SysParam.getInstance().set(R.string.EDC_STAN_NO, stanNo);
        LogUtils.i(TAG, "Increased STAN NO to: " + stanNo);
    }

    /**
     * 批次号+1
     */
    public static void incBatchNo() {
        int batchNo = FinancialApplication.getAcqManager().getCurAcq().getCurrBatchNo();
        if (batchNo >= Constants.MAX_BATCH_NO) {
            batchNo = 0;
        }
        batchNo++;

        FinancialApplication.getAcqManager().getCurAcq().setCurrBatchNo(batchNo);
        FinancialApplication.getAcqManager().updateAcquirer(FinancialApplication.getAcqManager().getCurAcq());
    }

    /**
     * Gets padded number.
     *
     * @param num   the num
     * @param digit the digit
     * @return the padded number
     */
    public static String getPaddedNumber(long num, int digit) {
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        nf.setGroupingUsed(false);
        nf.setMaximumIntegerDigits(digit);
        nf.setMinimumIntegerDigits(digit);
        return nf.format(num);
    }

    /**
     * Gets padded string.
     *
     * @param str    the str
     * @param maxLen the max len
     * @param ch     the ch
     * @return the padded string
     */
    public static String getPaddedString(String str, int maxLen, char ch) {
        return ConvertHelper.getConvert().stringPadding(str, ch, maxLen, IConvert.EPaddingPosition.PADDING_LEFT);
    }

    /**
     * 交易初始化
     *
     * @return {@link TransData}
     */
    public static TransData transInit() {
        TransData transData = new TransData();
        transInit(transData);
        return transData;
    }

    /**
     * 交易初始化
     *
     * @param transData {@link TransData}
     */
    public static void transInit(TransData transData) {
        Acquirer acquirer = FinancialApplication.getAcqManager().getCurAcq();

        transData.setTraceNo(getTransNo());
        transData.setBatchNo(acquirer.getCurrBatchNo());
        transData.setDateTime(Device.getTime(Constants.TIME_PATTERN_TRANS));
        transData.setHeader("");
        transData.setNii(acquirer.getNii());
        transData.setStanNo(getStanNo());
        transData.setDupReason("06");
        transData.setTransState(ETransStatus.NORMAL);
        transData.setAcquirer(acquirer);
        transData.setCurrency(CurrencyConverter.getDefCurrency());
    }

    // 获取流水号
    public static long getTransNo() {
        long transNo = SysParam.getInstance().getInt(R.string.EDC_TRACE_NO);
        if (transNo == 0) {
            transNo += 1;
            SysParam.getInstance().set(R.string.EDC_TRACE_NO, transNo);
        }
        LogUtils.i(TAG, "Current Transaction NO is: " + transNo);
        return transNo;
    }

    public static long getStanNo() {
        long stanNo = SysParam.getInstance().getInt(R.string.EDC_STAN_NO);
        if (stanNo == 0) {
            stanNo += 1;
            SysParam.getInstance().set(R.string.EDC_STAN_NO, stanNo);
        }
        LogUtils.i(TAG, "Current STAN NO is: " + stanNo);
        return stanNo;
    }

    /**
     * 是否免签
     *
     * @param transData {@link TransData}
     * @return true : 免签 false: 需要签名
     */
    public static boolean isSignatureFree(TransData transData) {
        return transData.getSignFree();
    }

    /**
     * check whether the Neptune is installed, if not, display prompt
     *
     * @param context                  the context
     * @param noNeptuneDismissListener the no neptune dismiss listener
     * @return the boolean
     */
    public static boolean neptuneInstalled(Context context, DialogInterface.OnDismissListener noNeptuneDismissListener) {
        if (!Sdk.isPaxDevice()) {
            return true;
        }
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo("com.pax.ipp.neptune", 0);
        } catch (NameNotFoundException e) {
            LogUtils.e(TAG, "", e);
        }

        if (packageInfo == null) {
            CustomAlertDialog dialog = new CustomAlertDialog(context, CustomAlertDialog.ERROR_TYPE, 5);
            dialog.setContentText(context.getString(R.string.please_install_neptune));
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
            dialog.setOnDismissListener(noNeptuneDismissListener);
            return false;
        }
        return true;
    }

    /**
     * Is demo boolean.
     *
     * @return the boolean
     */
    public static boolean isDemo() {
        String commType = SysParam.getInstance().getString(R.string.COMM_TYPE);
        return SysParam.CommType.DEMO.equals(commType);
    }
}
