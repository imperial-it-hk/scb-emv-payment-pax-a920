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
package com.evp.pay.utils;

import static android.content.Context.TELEPHONY_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSONReader;
import com.evp.commonlib.utils.LogUtils;
import com.evp.paxprinter.constant.Constant;
import com.evp.pay.MainActivity;
import com.evp.pay.app.ActivityStack;
import com.evp.pay.app.FinancialApplication;
import com.evp.payment.evpscb.R;
import com.evp.settings.SysParam;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.pax.dal.IDeviceInfo;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * The type Utils.
 */
public class Utils {

    private static final String TAG = "Utils";
    private static final String[] BT_PRINT_DEVICE = {"A60", "Aries8", "Aries6"};

    private Utils() {
        //do nothing
    }

    /**
     * Check ip boolean.
     *
     * @param ip the ip
     * @return the boolean
     */
    public static boolean checkIp(String ip) {
        return ip.matches("((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)");
    }

    /**
     * Change app language.
     *
     * @param context the context
     * @param locale  the locale
     */
    public static void changeAppLanguage(Context context, Locale locale) {
        if (context == null) {
            return;
        }
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);
    }

    /**
     * Gets string.
     *
     * @param resId the res id
     * @return the string
     */
    @NonNull
    public static String getString(@StringRes int resId) {
        return FinancialApplication.getApp().getString(resId);
    }

    /**
     * Gets string.
     *
     * @param resId      the res id
     * @param formatArgs the format args
     * @return the string
     */
    @NonNull
    public static String getString(@StringRes int resId, Object... formatArgs) {
        return FinancialApplication.getApp().getResources().getString(resId, formatArgs);
    }

    /**
     * Call permission disposable.
     *
     * @param activity   the activity
     * @param permission the permission
     * @param action     the action
     * @param failedMsg  the failed msg
     * @return the disposable
     */
// easy way to get permission, for getting more interactions, should show request permission rationale
    public static Disposable callPermission(@NonNull Activity activity, String permission, @NonNull Action action, final String failedMsg) {
        RxPermissions rxPermissions = new RxPermissions(activity); // where this is an Activity instance
        return rxPermissions
                .request(permission)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean granted) throws Exception {
                        LogUtils.e(TAG, "{accept Boolean}");
                        if (!granted) {
                            // 未获取权限
                            ToastUtils.showMessage(failedMsg);
                        }
                        // 在android 6.0之前会默认返回true
                        // 已经获取权限 do nothing
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtils.e(TAG, "{accept Throwable}");//可能是授权异常的情况下的处理
                    }
                }, action);
    }

    /**
     * Call permission disposable.
     *
     * @param activity   the activity
     * @param permission the permission
     * @param action     the action
     * @param failAction the fail action
     * @return the disposable
     */
    public static Disposable callPermission(@NonNull Activity activity, String permission, @NonNull Action action, final Action failAction) {
        RxPermissions rxPermissions = new RxPermissions(activity); // where this is an Activity instance
        return rxPermissions
                .request(permission)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean granted) throws Exception {
                        LogUtils.e(TAG, "{accept Boolean}");
                        if (!granted) {
                            // 未获取权限
                            failAction.run();
                        }
                        // 在android 6.0之前会默认返回true
                        // 已经获取权限 do nothing
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtils.e(TAG, "{accept Throwable}");//可能是授权异常的情况下的处理
                        failAction.run();
                    }
                }, action);
    }

    /**
     * Call permissions disposable.
     *
     * @param activity   the activity
     * @param permission the permission
     * @param action     the action
     * @param failedMsg  the failed msg
     * @return the disposable
     */
    public static Disposable callPermissions(@NonNull Activity activity, String[] permission, @NonNull Action action, final String failedMsg) {
        RxPermissions rxPermissions = new RxPermissions(activity);
        List<Observable<Boolean>> permissionList = new ArrayList<>();
        for (String s : permission) {
            permissionList.add(rxPermissions.request(s));
        }
        return Observable.concat(permissionList)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean granted) throws Exception {
                        if (!granted) {
                            // 未获取权限
                            ToastUtils.showMessage(failedMsg);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        //可能是授权异常的情况下的处理
                        LogUtils.e(TAG, "", throwable);
                    }
                }, action);
    }

    /**
     * Is sms available boolean.
     *
     * @param context the context
     * @return the boolean
     */
    public static boolean isSMSAvailable(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        return (manager.getSimState() == TelephonyManager.SIM_STATE_READY) && (
                ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Is network available boolean.
     *
     * @param context the context
     * @return the boolean
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected() && info.getState() == NetworkInfo.State.CONNECTED) {
                return true;
            }
        }
        return false;
    }

    /**
     * Wakeup screen.
     *
     * @param timeout the timeout
     */
    public static void wakeupScreen(int timeout) {
        PowerManager pm = (PowerManager) FinancialApplication.getApp().getSystemService(Context.POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag") final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
        wl.acquire();
        FinancialApplication.getApp().runOnUiThreadDelay(new Runnable() {
            @Override
            public void run() {
                wl.release();
            }
        }, 1000L * (timeout + 1));
    }

    /**
     * Restart.
     */
    public static void restart() {
        ActivityStack.getInstance().popAll();
        Intent intent = new Intent();
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(FinancialApplication.getApp(), MainActivity.class);
        FinancialApplication.getApp().startActivity(intent);
    }

    /**
     * Call system settings.
     *
     * @param context the context
     * @param action  the action
     */
    public static void callSystemSettings(Context context, String action) {
        context.startActivity(new Intent(action));
    }

    /**
     * Read obj from json list.
     *
     * @param <T>      the type parameter
     * @param fileName the file name
     * @param clz      the clz
     * @return the list
     */
    public static <T> List<T> readObjFromJSON(String fileName, Class<T> clz) {
        List<T> list = new ArrayList<>();
        try (InputStreamReader reader = new InputStreamReader(FinancialApplication.getApp().getAssets().open(fileName))) {
            JSONReader jsonReader = new JSONReader(reader);
            jsonReader.startArray();
            while (jsonReader.hasNext()) {
                T obj = jsonReader.readObject(clz);
                list.add(obj);
            }
            jsonReader.endArray();
            jsonReader.close();
        } catch (IOException e) {
            LogUtils.e(TAG, "", e);
        }
        return list;
    }

    /**
     * Read obj from json list.
     *
     * @param <T>  the type parameter
     * @param path path to file
     * @param clz  the clz
     * @return the list
     */
    public static <T> List<T> readObjFromJsonInPath(String path, Class<T> clz) {
        List<T> list = new ArrayList<>();
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(path))) {
            JSONReader jsonReader = new JSONReader(reader);
            jsonReader.startArray();
            while (jsonReader.hasNext()) {
                T obj = jsonReader.readObject(clz);
                list.add(obj);
            }
            jsonReader.endArray();
            jsonReader.close();
        } catch (IOException e) {
            LogUtils.e(TAG, "", e);
        }
        return list;
    }

    /**
     * Parse long safe long.
     *
     * @param longStr   the long str
     * @param safeValue the safe value
     * @return the long
     */
    public static long parseLongSafe(String longStr, long safeValue) {
        if (longStr == null) {
            return safeValue;
        }
        try {
            return Long.parseLong(longStr);
        } catch (NumberFormatException e) {
            return safeValue;
        }
    }

    /**
     * Parse int safe int.
     *
     * @param intStr the int str
     * @return the int
     */
    public static int parseIntSafe(String intStr) {
        return parseIntSafe(intStr, 0);
    }

    /**
     * Parse int safe int.
     *
     * @param intStr       the int str
     * @param defaultValue the default value
     * @return the int
     */
    public static int parseIntSafe(String intStr, int defaultValue) {
        try {
            return Integer.parseInt(intStr);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Create qr image bitmap.
     *
     * @param url  the url
     * @param size the size
     * @return the bitmap
     */
    public static Bitmap createQRImage(String url, int size) {
        try {
            if (url == null || "".equals(url) || url.length() < 1) {
                return null;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");

            BitMatrix bitMatrix = new QRCodeWriter().encode(url,
                    BarcodeFormat.QR_CODE, size, size, hints);
            int[] pixels = new int[size * size];

            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * size + x] = 0xff000000;
                    } else {
                        pixels[y * size + x] = 0xffffffff;
                    }
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size,
                    Bitmap.Config.RGB_565);
            bitmap.setPixels(pixels, 0, size, 0, 0, size, size);
            return bitmap;
        } catch (WriterException e) {
            LogUtils.e("log", "", e);
        }
        return null;
    }

    /**
     * Create qr image bitmap.
     *
     * @param content the content
     * @param logo    the logo
     * @param size    the size
     * @return the bitmap
     */
    public static Bitmap createQRImage(String content, Bitmap logo, int size) {
        Bitmap logoBitmap = modifyLogo(logo);
        if (logoBitmap == null) {
            return null;
        }

        int logoSize = logoBitmap.getWidth();
        int logoHaleSize = logoSize >= size ? size / 10 : size / 5;
        Matrix m = new Matrix();
        float s = (float) logoHaleSize / logoSize;
        m.setScale(s, s);

        Bitmap newLogoBitmap = Bitmap.createBitmap(logoBitmap, 0, 0, logoSize,
                logoSize, m, false);
        int newLogoWidth = newLogoBitmap.getWidth();
        int newLogoHeight = newLogoBitmap.getHeight();
        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        BitMatrix matrix = null;
        try {
            matrix = new MultiFormatWriter().encode(content,
                    BarcodeFormat.QR_CODE, size, size, hints);
        } catch (WriterException e) {
            LogUtils.e("log", "", e);
            return null;
        }

        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int halfW = width / 2;
        int halfH = height / 2;

        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x > halfW - newLogoWidth / 2 && x < halfW + newLogoWidth / 2
                        && y > halfH - newLogoHeight / 2 && y < halfH + newLogoHeight / 2) {
                    pixels[y * width + x] = newLogoBitmap.getPixel(
                            x - halfW + newLogoWidth / 2, y - halfH + newLogoHeight / 2);
                } else {
                    pixels[y * width + x] = matrix.get(x, y) ? BLACK : WHITE;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.RGB_565);

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private static Bitmap modifyLogo(Bitmap logoBitmap) {
//        Bitmap bgBitmap = Bitmap.createBitmap(logoBitmap.getWidth(), logoBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Bitmap bgBitmap = Bitmap.createBitmap(logoBitmap.getWidth(), logoBitmap.getHeight(), Bitmap.Config.RGB_565);
        bgBitmap.eraseColor(WHITE);

        int bgWidth = bgBitmap.getWidth();
        int bgHeigh = bgBitmap.getHeight();
        //通过ThumbnailUtils压缩原图片，并指定宽高为背景图的3/4
        logoBitmap = ThumbnailUtils.extractThumbnail(logoBitmap, bgWidth * 3 / 4, bgHeigh * 3 / 4, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
//        Bitmap cvBitmap = Bitmap.createBitmap(bgWidth, bgHeigh, Bitmap.Config.ARGB_8888);
        Bitmap cvBitmap = Bitmap.createBitmap(bgWidth, bgHeigh, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(cvBitmap);
        // 开始合成图片
        canvas.drawBitmap(bgBitmap, 0, 0, null);
        canvas.drawBitmap(logoBitmap, (bgWidth - logoBitmap.getWidth()) / 2, (bgHeigh - logoBitmap.getHeight()) / 2, null);
        canvas.save();
        canvas.restore();
        return cvBitmap;
    }

    /**
     * Need bt print boolean.
     *
     * @return the boolean
     */
    public static boolean needBtPrint() {
        for (String s : BT_PRINT_DEVICE) {
            if (s.equals(Build.MODEL)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Jump activity.
     *
     * @param context the context
     * @param target  the target
     * @param bundle  the bundle
     */
    public static void jumpActivity(Context context, Class<? extends Activity> target, Bundle bundle) {
        Intent intent = new Intent(context, target);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    /**
     * Gets color.
     *
     * @param colorId the color id
     * @return the color
     */
    public static int getColor(int colorId) {
        return ContextCompat.getColor(FinancialApplication.getApp(), colorId);
    }

    /**
     * Gets mutable list.
     *
     * @param id the id
     * @return the mutable list
     */
    public static List<String> getMutableList(int id) {
        return Arrays.asList(FinancialApplication.getApp().getResources().getStringArray(id));
    }

    /**
     * Get print type key.
     *
     * @return the string
     */
    public static String getPrintTypeServiceKey() {
        String key = Constant.PRINT_BUILD_IN;
        String printerType = SysParam.getInstance().getString(R.string.EDC_PRINTER_TYPE);
        String[] printerTypes = FinancialApplication.getApp().getResources().getStringArray(R.array.edc_printer_type_entries);
        if (printerTypes[1].equals(printerType)) {
            key = Constant.PRINT_BUILD_BE_C2;
        }
        return key;
    }

    /**
     * Get print type string.
     *
     * @return the string
     */
    public static String getPrintType() {
        IDeviceInfo deviceInfo = FinancialApplication.getDal().getDeviceInfo();
        IDeviceInfo.ESupported moduleSupported = deviceInfo.getModuleSupported(IDeviceInfo.MODULE_PRINTER);
        int result = moduleSupported.compareTo(IDeviceInfo.ESupported.YES);
        String key = FinancialApplication.getApp().getResources().getStringArray(R.array.edc_printer_type_entries)[0];
        if (result != 0) {
            key = FinancialApplication.getApp().getResources().getStringArray(R.array.edc_printer_type_entries)[1];
        }
        return key;
    }

    /**
     * get supported printer type by device
     *
     * @return supported printer type
     */
    public static List<String> getSupportedPrintType() {
        IDeviceInfo deviceInfo = FinancialApplication.getDal().getDeviceInfo();
        IDeviceInfo.ESupported moduleSupported = deviceInfo.getModuleSupported(IDeviceInfo.MODULE_PRINTER);
        int result = moduleSupported.compareTo(IDeviceInfo.ESupported.YES);
        List<String> mutableList = new LinkedList<>(Utils.getMutableList(R.array.edc_printer_type_entries));
        if (result != 0) {
            mutableList.remove(0);
        }
        return mutableList;
    }

    public static Date addDays(final Date date, final int days)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return cal.getTime();
    }

    public static Date addSeconds(final Date date, final int seconds)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.SECOND, seconds); //minus number would decrement the seconds
        return cal.getTime();
    }

    public static Date getStartOfDay(final Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
        String dateStr = formatter.format(date);
        dateStr += " 00:00:01";
        formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.US);
        Date out = new Date();
        try {
            out = formatter.parse(dateStr);
        } catch (ParseException e) {
            LogUtils.e(TAG, e);
        }
        return out;
    }

    public static Date addTimeToDate(final Date date, final String time, final String timeFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
        String dateStr = formatter.format(date);
        dateStr = dateStr + " " + time;
        formatter = new SimpleDateFormat("dd.MM.yyyy " + timeFormat, Locale.US);
        Date out = new Date();
        try {
            out = formatter.parse(dateStr);
        } catch (ParseException e) {
            LogUtils.e(TAG, e);
        }
        return out;
    }
}
