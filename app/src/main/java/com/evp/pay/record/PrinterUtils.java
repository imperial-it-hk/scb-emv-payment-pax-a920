package com.evp.pay.record;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.View;

import com.evp.bizlib.AppConstants;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.EmvAid;
import com.evp.bizlib.data.entity.EmvCapk;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.entity.TransTotal;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.bizlib.receipt.ReceiptConst;
import com.evp.commonlib.utils.LogUtils;
import com.evp.config.ConfigUtils;
import com.evp.device.Device;
import com.evp.eventbus.OnPrintEvent;
import com.evp.paxprinter.IPrintService;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.trans.receipt.PrintListener;
import com.evp.pay.trans.receipt.PrintListenerImpl;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.poslib.print.exception.PrinterException;
import com.evp.settings.SysParam;
import com.pax.gl.pack.IIso8583;
import com.pax.gl.pack.exception.Iso8583Exception;
import com.sankuai.waimai.router.Router;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * entry of print
 */
public class PrinterUtils {
    /**
     * print last transaction
     *
     * @param activity activity context for showing dialog
     * @return print result
     */
    public static int printLastTrans(Activity activity) {
        TransData transData = GreendaoHelper.getTransDataHelper().findLastTransData();
        if (transData == null) {
            return TransResult.ERR_NO_TRANS;
        }
        if (transData.getIssuer() != null && !transData.getIssuer().isAllowPrint()) {
            return TransResult.SUCC;
        }
        PrintListener listener = new PrintListenerImpl(activity);
        listener.onShowMessage(null, Utils.getString(R.string.wait_print));
        int receiptNum = getVoucherNum();
        try {
            for (int i = 0; i < receiptNum; i++) {
                View receiptView = Router.callMethod(ReceiptConst.RECEIPT_TRANSDETAIL, FinancialApplication.getApp(), transData, false, i, ConfigUtils.getInstance());
                print(receiptView.getDrawingCache());
                //region OLS
                if (transData.getTransType() == ETransType.OLS_ENQUIRY.name())
                    break;
                //endregion
            }
        } catch (PrinterException e) {
            Device.beepErr();
            PrintListener.Status status = listener.onConfirm(null, e.getErrMsg());
            if (status == PrintListener.Status.CONTINUE) {
                printLastTrans(activity);
            }
        }
        listener.onEnd();
        return TransResult.SUCC;
    }

    /**
     * print transaction detail
     *
     * @param activity  activity context for showing dialog
     * @param transData transaction data
     * @return print result
     */
    public static int printTransDetail(Activity activity, TransData transData, int copy, boolean isReprint) {
        if (transData.getIssuer() != null && !transData.getIssuer().isAllowPrint()) {
            return TransResult.SUCC;
        }
        if (copy >= getVoucherNum()) {
            return TransResult.SUCC;
        }
        PrintListener listener = new PrintListenerImpl(activity);
        listener.onShowMessage(null, Utils.getString(R.string.wait_print));
        try {
            View receiptView = Router.callMethod(ReceiptConst.RECEIPT_TRANSDETAIL, FinancialApplication.getApp(), transData, isReprint, copy, ConfigUtils.getInstance());
            EventBus.getDefault().post(new OnPrintEvent());
            print(receiptView.getDrawingCache());
        } catch (PrinterException e) {
            Device.beepErr();
            PrintListener.Status status = listener.onConfirm(null, e.getErrMsg());
            if (status == PrintListener.Status.CONTINUE) {
                printTransDetail(activity, transData, copy, isReprint);
            }
        }
        listener.onEnd();
        return TransResult.SUCC;
    }

    /**
     * print transaction detail
     *
     * @param activity  activity context for showing dialog
     * @param transData transaction data
     * @return print result
     */
    public static int printTransAgain(Activity activity, TransData transData) {
        if (transData.getIssuer() != null && !transData.getIssuer().isAllowPrint()) {
            return TransResult.SUCC;
        }
        PrintListener listener = new PrintListenerImpl(activity);
        listener.onShowMessage(null, Utils.getString(R.string.wait_print));
        int receiptNum = getVoucherNum();
        try {
            for (int i = 0; i < receiptNum; i++) {
                View receiptView = Router.callMethod(ReceiptConst.RECEIPT_TRANSDETAIL, FinancialApplication.getApp(), transData, true, i, ConfigUtils.getInstance());
                print(receiptView.getDrawingCache());
                //region OLS
                if (transData.getTransType() == ETransType.OLS_ENQUIRY.name())
                    break;
                //endregion
            }
        } catch (PrinterException e) {
            Device.beepErr();
            PrintListener.Status status = listener.onConfirm(null, e.getErrMsg());
            if (status == PrintListener.Status.CONTINUE) {
                printTransAgain(activity, transData);
            }
        }
        listener.onEnd();
        return TransResult.SUCC;
    }

    /**
     * print settle transaction list
     *
     * @param activity activity context for showing dialog
     * @param acquirer acquirer
     * @return print result
     */
    public static int printSettleTransList(Activity activity, Acquirer acquirer) {
        View receiptView = null;
        switch (acquirer.getName()) {
            case AppConstants.QR_ACQUIRER:
                TransTotal transTotal = GreendaoHelper.getTransTotalHelper().findLastTransTotal(acquirer, true);
                if (transTotal == null) {
                    return TransResult.ERR_NO_TRANS;
                }
                receiptView = Router.callMethod(ReceiptConst.SETTLEMENT_RECEIPT, FinancialApplication.getApp(), transTotal, ConfigUtils.getInstance());
                break;
            default:
                List<String> list = new ArrayList<>();
                list.add(ETransType.SALE.name());
                list.add(ETransType.VOID.name());
                list.add(ETransType.REFUND.name());
                list.add(ETransType.OFFLINE_SALE.name());
                list.add(ETransType.INSTALLMENT.name());
                list.add(ETransType.REDEEM.name());
                List<TransData.ETransStatus> filter = new ArrayList<>();
                filter.add(TransData.ETransStatus.VOIDED);
                //AET-113
                //AET-95
                List<TransData> record = GreendaoHelper.getTransDataHelper().findSettleTransData(list, filter, acquirer);

                if (record.isEmpty()) {
                    return TransResult.ERR_NO_TRANS;
                }
                if (record.get(0).getIssuer() != null && !record.get(0).getIssuer().isAllowPrint()) {
                    return TransResult.SUCC;
                }
                receiptView = Router.callMethod(ReceiptConst.RECEIPT_TRANSLIST,
                        FinancialApplication.getApp(),
                        record,
                        Utils.getString(R.string.print_settle_detail), ConfigUtils.getInstance());
                break;
        }
        PrintListener listener = new PrintListenerImpl(activity);
        listener.onShowMessage(null, Utils.getString(R.string.wait_print));
        try {
            EventBus.getDefault().post(new OnPrintEvent());
            print(receiptView.getDrawingCache());
        } catch (PrinterException e) {
            Device.beepErr();
            PrintListener.Status status = listener.onConfirm(null, e.getErrMsg());
            if (status == PrintListener.Status.CONTINUE) {
                printSettleTransList(activity, acquirer);
            }
        }
        listener.onEnd();
        //bitmaps.clear();
        return TransResult.SUCC;
    }

    /**
     * print history transaction list
     *
     * @param activity activity context for showing dialog
     * @param acquirer acquirer
     * @return print result
     */
    public static int printHistoryTransList(Activity activity, Acquirer acquirer) {
        List<String> list = new ArrayList<>();
        list.add(ETransType.SALE.name());
        list.add(ETransType.VOID.name());
        list.add(ETransType.REFUND.name());
        list.add(ETransType.OFFLINE_SALE.name());
        list.add(ETransType.INSTALLMENT.name());
        list.add(ETransType.REDEEM.name());
        List<TransData.ETransStatus> filter = new ArrayList<>();
        filter.add(TransData.ETransStatus.VOIDED);
        //AET-113
        //AET-95
        List<TransData> record = GreendaoHelper.getTransDataHelper().findTransData(list, filter, acquirer);

        if (record.isEmpty()) {
            return TransResult.ERR_NO_TRANS;
        }
        if (!record.get(0).getIssuer().isAllowPrint()) {
            return TransResult.SUCC;
        }
        PrintListener listener = new PrintListenerImpl(activity);
        listener.onShowMessage(null, Utils.getString(R.string.wait_print));
        View view = Router.callMethod(ReceiptConst.RECEIPT_TRANSLIST,
                FinancialApplication.getApp(),
                record,
                Utils.getString(R.string.print_history_detail), ConfigUtils.getInstance());
        try {
            print(view.getDrawingCache());
        } catch (PrinterException e) {
            Device.beepErr();
            PrintListener.Status status = listener.onConfirm(null, e.getErrMsg());
            if (status == PrintListener.Status.CONTINUE) {
                printHistoryTransList(activity, acquirer);
            }
        }
        listener.onEnd();
        //bitmaps.clear();
        return TransResult.SUCC;
    }

    /**
     * print history transaction total
     *
     * @param activity activity context for showing dialog
     * @param acquirer acquirer
     */
    public static void printHistoryTransTotal(Activity activity, Acquirer acquirer) {
        PrintListener listener = new PrintListenerImpl(activity);
        listener.onShowMessage(null, Utils.getString(R.string.wait_print));
        TransTotal total = GreendaoHelper.getTransTotalHelper().calcTotal(acquirer);
        View view = Router.callMethod(ReceiptConst.RECEIPT_TRANSTOTAL,
                FinancialApplication.getApp(),
                total,
                Utils.getString(R.string.print_history_total), ConfigUtils.getInstance());
        try {
            print(view.getDrawingCache());
        } catch (PrinterException e) {
            Device.beepErr();
            PrintListener.Status status = listener.onConfirm(null, e.getErrMsg());
            if (status == PrintListener.Status.CONTINUE) {
                printHistoryTransTotal(activity, acquirer);
            }
        }
        listener.onEnd();
    }

    /**
     * print last transaction total
     *
     * @param activity activity context for showing dialog
     * @return print result
     */
    public static int printLastTransTotal(Activity activity, Acquirer acquirer) {
        TransTotal total = GreendaoHelper.getTransTotalHelper().findLastTransTotal(acquirer, true);
        if (total == null) {
            return TransResult.ERR_NO_TRANS;
        }
        switch (acquirer.getName()) {
            case AppConstants.QR_ACQUIRER:
                return printLastSettlement(activity, acquirer);
            default:
                return printSettle(activity, total);
        }
    }

    /**
     * print settle
     *
     * @param activity activity context for showing dialog
     * @param total    TransTotal
     */
    public static int printSettle(Activity activity, TransTotal total) {
        PrintListener listener = new PrintListenerImpl(activity);
        listener.onShowMessage(null, Utils.getString(R.string.wait_print));
        View view = Router.callMethod(ReceiptConst.RECEIPT_TRANSTOTAL,
                FinancialApplication.getApp(),
                total,
                Utils.getString(R.string.history_total), ConfigUtils.getInstance());
        try {
            print(view.getDrawingCache());
        } catch (PrinterException e) {
            Device.beepErr();
            PrintListener.Status status = listener.onConfirm(null, e.getErrMsg());
            if (status == PrintListener.Status.CONTINUE) {
                printSettle(activity, total);
            }
        }
        listener.onEnd();
        return TransResult.SUCC;
    }

    /**
     * print failed transaction list
     *
     * @param activity activity context for showing dialog
     * @return print result
     */
    public static int printFailDetail(Activity activity) {
        List<String> list = new ArrayList<>();
        list.add(ETransType.SALE.name());
        list.add(ETransType.PREAUTH.name());
        list.add(ETransType.OFFLINE_SALE.name());

        List<TransData.ETransStatus> filter = new ArrayList<>();
        filter.add(TransData.ETransStatus.VOIDED);
        filter.add(TransData.ETransStatus.ADJUSTED);

        //AET-95
        List<TransData> records = GreendaoHelper.getTransDataHelper().findTransData(list, filter, FinancialApplication.getAcqManager().getCurAcq());
        List<TransData> details = new ArrayList<>();
        if (records.isEmpty()) {
            return TransResult.ERR_NO_TRANS;
        }

        for (TransData record : records) {
            if (record.getOfflineSendState() != null && record.getOfflineSendState() != TransData.OfflineStatus.OFFLINE_SENT) {
                details.add(record);
            }
        }

        if (details.isEmpty()) {
            return TransResult.ERR_NO_TRANS;
        }
        PrintListener listener = new PrintListenerImpl(activity);
        listener.onShowMessage(null, Utils.getString(R.string.wait_print));
        List<Bitmap> bitmaps = Router.callMethod(ReceiptConst.RECEIPT_TRANS_FAILED_LIST,
                FinancialApplication.getApp(),
                details,
                Utils.getString(R.string.print_offline_send_failed));
        try {
            print(bitmaps);
        } catch (PrinterException e) {
            Device.beepErr();
            PrintListener.Status status = listener.onConfirm(null, e.getErrMsg());
            if (status == PrintListener.Status.CONTINUE) {
                printFailDetail(activity);
            }
        }
        listener.onEnd();
        return TransResult.SUCC;
    }

    /**
     * print aid params list
     *
     * @param activity activity context for showing dialog
     * @return print result
     */
    public static int printAid(Activity activity) {
        List<EmvAid> aids = GreendaoHelper.getEmvAidHelper().loadAll();
        if (aids == null || aids.isEmpty()) {
            return TransResult.ERR_PARAM;
        }
        PrintListener listener = new PrintListenerImpl(activity);
        listener.onShowMessage(null, Utils.getString(R.string.wait_print));
        List<Bitmap> bitmaps = Router.callMethod(ReceiptConst.RECEIPT_AID, aids, FinancialApplication.getApp());
        try {
            print(bitmaps);
        } catch (PrinterException e) {
            Device.beepErr();
            PrintListener.Status status = listener.onConfirm(null, e.getErrMsg());
            if (status == PrintListener.Status.CONTINUE) {
                printAid(activity);
            }
        }
        listener.onEnd();
        return TransResult.SUCC;
    }

    /**
     * print capk params list
     *
     * @param activity activity context for showing dialog
     * @return print result
     */
    public static int printCapk(Activity activity) {
        List<EmvCapk> capks = GreendaoHelper.getEmvCapkHelper().loadAll();
        if (capks == null || capks.isEmpty()) {
            return TransResult.ERR_PARAM;
        }
        PrintListener listener = new PrintListenerImpl(activity);
        listener.onShowMessage(null, Utils.getString(R.string.wait_print));
        List<Bitmap> bitmaps = Router.callMethod(ReceiptConst.RECEIPT_CAPK, capks, FinancialApplication.getApp());
        try {
            print(bitmaps);
        } catch (PrinterException e) {
            Device.beepErr();
            PrintListener.Status status = listener.onConfirm(null, e.getErrMsg());
            if (status == PrintListener.Status.CONTINUE) {
                printCapk(activity);
            }
        }
        listener.onEnd();
        return TransResult.SUCC;
    }

    /**
     * print settle transaction list
     *
     * @param bitmapStr bitmap string value
     * @param activity  activity context for showing dialog
     * @return print result
     */
    public static int printBitmapStr(String bitmapStr, Activity activity) {
        PrintListener listener = new PrintListenerImpl(activity);
        listener.onShowMessage(null, Utils.getString(R.string.wait_print));
        // 将json传入的String转换成Bitmap
        byte[] bitmapArray;
        bitmapArray = Base64.decode(bitmapStr, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        try {
            print(bitmap);
        } catch (PrinterException e) {
            Device.beepErr();
            PrintListener.Status status = listener.onConfirm(null, e.getErrMsg());
            if (status == PrintListener.Status.CONTINUE) {
                printBitmapStr(bitmapStr, activity);
            }
        }
        listener.onEnd();
        return TransResult.SUCC;
    }

    public static int printQrCode(Bitmap qrBitmapStr, Bitmap brandBitmapStr, String amount, String currency, String date, String time, Activity activity) {
        PrintListener listener = new PrintListenerImpl(activity);
        listener.onShowMessage(null, Utils.getString(R.string.wait_print));

        List<Bitmap> bitmaps = Router.callMethod(ReceiptConst.QR_SLIP,
                FinancialApplication.getApp(),
                qrBitmapStr,
                brandBitmapStr,
                amount,
                currency,
                date,
                time);
        try {
            print(bitmaps);
        } catch (PrinterException e) {
            Device.beepErr();
            PrintListener.Status status = listener.onConfirm(null, e.getErrMsg());
            if (status == PrintListener.Status.CONTINUE) {
                printQrCode(qrBitmapStr, brandBitmapStr, amount, currency, date, time, activity);
            }
        }
        listener.onEnd();
        return TransResult.SUCC;
    }

    /**
     * print transaction online packet
     *
     * @param activity activity context for showing dialog
     * @param data     iso8583 packet data
     * @param isSend   whether send out
     * @return print result
     */
    public static int printLastTransOnlineLog(Activity activity, byte[] data, boolean isSend) {
        PrintListener listener = new PrintListenerImpl(activity);
        listener.onShowMessage(null, Utils.getString(R.string.wait_print));
        IIso8583 iso8583 = FinancialApplication.getPacker().getIso8583();
        HashMap<String, byte[]> map = null;

        //解析报文
        try {
            map = iso8583.unpack(data, true);
        } catch (Iso8583Exception e) {
            LogUtils.e(e);
        }
        Bitmap bitmap = Router.callMethod(ReceiptConst.RECEIPT_TRANSLOG,
                FinancialApplication.getApp(),
                map,
                isSend);
        try {
            print(bitmap);
        } catch (PrinterException e) {
            Device.beepErr();
            PrintListener.Status status = listener.onConfirm(null, e.getErrMsg());
            if (status == PrintListener.Status.CONTINUE) {
                printLastTransOnlineLog(activity, data, isSend);
            }
        }
        listener.onEnd();
        return TransResult.SUCC;
    }

    /**
     * print bitmap list
     *
     * @param bitmaps bitmap list
     * @return print result
     * @throws PrinterException PrinterException
     */
    public static int print(List<Bitmap> bitmaps) throws PrinterException {
        for (Bitmap bitmap : bitmaps) {
            print(bitmap);
        }
        return TransResult.SUCC;

    }

    /**
     * print bitmap
     *
     * @param bitmap bitmap
     * @return print result
     * @throws PrinterException PrinterException
     */
    public static int print(Bitmap bitmap) throws PrinterException {
        String printType = Utils.getPrintTypeServiceKey();
        IPrintService service = Router.getService(IPrintService.class, printType);
        service.print(bitmap, FinancialApplication.getApp());
        return TransResult.SUCC;

    }

    /**
     * get voucher number
     *
     * @return voucher number
     */
    public static int getVoucherNum() {
        //TODO: mock receipt to be 2 copy
        int receiptNum = SysParam.getInstance().getInt(R.string.EDC_RECEIPT_NUM);
        if (receiptNum < 1 || receiptNum > 3) // receipt copy number is 1-3
        {
            receiptNum = 2;
        }
//        return receiptNum;
        return 2;
    }

    /**
     * print dcc rate slip
     *
     * @param activity activity context for showing dialog
     * @return print result
     */
    public static int printDccRate(Activity activity, TransData transData) {
        PrintListener listener = new PrintListenerImpl(activity);
        listener.onShowMessage(null, Utils.getString(R.string.wait_print));
        View receiptView = Router.callMethod(ReceiptConst.RECEIPT_DCC_RATE, transData, FinancialApplication.getApp(), ConfigUtils.getInstance());
        try {
            print(receiptView.getDrawingCache());
        } catch (PrinterException e) {
            Device.beepErr();
            PrintListener.Status status = listener.onConfirm(null, e.getErrMsg());
            if (status == PrintListener.Status.CONTINUE) {
                printDccRate(activity, transData);
            }
        }
        listener.onEnd();
        return TransResult.SUCC;
    }

    /**
     * print transaction detail
     *
     * @param activity activity context for showing dialog
     * @return print result
     */
    public static int printSummaryReport(Activity activity, Acquirer acquirer) {
        View receiptView;
        switch (acquirer.getName()) {
            case AppConstants.QR_ACQUIRER:
                List<String> list = new ArrayList<>();
                list.add(ETransType.SALE.name());
                list.add(ETransType.VOID.name());
                list.add(ETransType.REFUND.name());
                List<TransData.ETransStatus> filter = new ArrayList<>();
                filter.add(TransData.ETransStatus.VOIDED);
                filter.add(TransData.ETransStatus.SUSPENDED);
                List<TransData> transDataList = GreendaoHelper.getTransDataHelper().findTransData(list, filter);
                if (transDataList.isEmpty()) {
                    return TransResult.ERR_NO_TRANS;
                }
                receiptView = Router.callMethod(ReceiptConst.SUMMARY_REPORT, FinancialApplication.getApp(), transDataList, ConfigUtils.getInstance());

                break;
            default:
                TransTotal transTotal = GreendaoHelper.getTransTotalHelper().calcTotal(acquirer);
                if (transTotal.isZero())
                    return TransResult.ERR_NO_TRANS;

                receiptView = Router.callMethod(ReceiptConst.RECEIPT_TRANSTOTAL, FinancialApplication.getApp(), transTotal,
                        Utils.getString(R.string.report_summary_report), ConfigUtils.getInstance());
                break;
        }
        PrintListener listener = new PrintListenerImpl(activity);
        listener.onShowMessage(null, Utils.getString(R.string.wait_print));
        try {
            EventBus.getDefault().post(new OnPrintEvent());
            print(receiptView.getDrawingCache());
        } catch (PrinterException e) {
            Device.beepErr();
            PrintListener.Status status = listener.onConfirm(null, e.getErrMsg());
            if (status == PrintListener.Status.CONTINUE) {
                printSummaryReport(activity, acquirer);
            }
        }
        listener.onEnd();
        return TransResult.SUCC;
    }

    /**
     * print transaction detail
     *
     * @param activity activity context for showing dialog
     * @return print result
     */
    public static int printAuditReport(Activity activity, Acquirer acquirer) {
        List<String> list = new ArrayList<>();
        list.add(ETransType.SALE.name());
        list.add(ETransType.VOID.name());
        list.add(ETransType.REFUND.name());
        list.add(ETransType.OFFLINE_SALE.name());
        list.add(ETransType.INSTALLMENT.name());
        list.add(ETransType.REDEEM.name());
        List<TransData.ETransStatus> filter = new ArrayList<>();
        filter.add(TransData.ETransStatus.VOIDED);
        filter.add(TransData.ETransStatus.SUSPENDED);
        List<TransData> transDataList = GreendaoHelper.getTransDataHelper().findTransData(list, filter, acquirer);
        if (transDataList == null || transDataList.size() == 0) {
            return TransResult.ERR_NO_TRANS;
        }
        PrintListener listener = new PrintListenerImpl(activity);
        listener.onShowMessage(null, Utils.getString(R.string.wait_print));
        try {
            View receiptView;
            switch (acquirer.getName()) {
                case AppConstants.QR_ACQUIRER:
                    receiptView = Router.callMethod(ReceiptConst.AUDIT_REPORT, FinancialApplication.getApp(), transDataList, ConfigUtils.getInstance());
                    break;
                default:
                    receiptView = Router.callMethod(ReceiptConst.RECEIPT_TRANSLIST, FinancialApplication.getApp(), transDataList,
                            Utils.getString(R.string.report_audit_report), ConfigUtils.getInstance());
                    break;
            }

            EventBus.getDefault().post(new OnPrintEvent());
            print(receiptView.getDrawingCache());
        } catch (PrinterException e) {
            Device.beepErr();
            PrintListener.Status status = listener.onConfirm(null, e.getErrMsg());
            if (status == PrintListener.Status.CONTINUE) {
                printAuditReport(activity, acquirer);
            }
        }
        listener.onEnd();
        return TransResult.SUCC;
    }

    /**
     * print transaction detail
     *
     * @param activity activity context for showing dialog
     * @return print result
     */
    public static int printLastSettlement(Activity activity, Acquirer acquirer) {
        TransTotal transTotal = GreendaoHelper.getTransTotalHelper().findLastTransTotal(acquirer, true);
        if (transTotal == null) {
            return TransResult.ERR_NO_TRANS;
        }
        PrintListener listener = new PrintListenerImpl(activity);
        listener.onShowMessage(null, Utils.getString(R.string.wait_print));
        try {
            View receiptView = Router.callMethod(ReceiptConst.SETTLEMENT_RECEIPT, FinancialApplication.getApp(), transTotal, ConfigUtils.getInstance());
            EventBus.getDefault().post(new OnPrintEvent());
            print(receiptView.getDrawingCache());
        } catch (PrinterException e) {
            Device.beepErr();
            PrintListener.Status status = listener.onConfirm(null, e.getErrMsg());
            if (status == PrintListener.Status.CONTINUE) {
                printLastSettlement(activity, acquirer);
            }
        }
        listener.onEnd();
        return TransResult.SUCC;
    }
}
