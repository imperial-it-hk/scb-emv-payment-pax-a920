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
package com.evp.pay.app;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Handler;
import android.util.Log;

import com.evp.appstore.DownloadManager;
import com.evp.appstore.IDownloadListener;
import com.evp.bizlib.data.local.db.helper.DaoManager;
import com.evp.bizlib.data.local.db.helper.TransDataDbHelper;
import com.evp.bizlib.data.model.ETransType;
import com.evp.commonlib.application.AppInfo;
import com.evp.commonlib.application.BaseApplication;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.utils.LogUtils;
import com.evp.commonlib.utils.ThreadPoolManager;
import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.device.GeneralParam;
import com.evp.eemv.entity.AidParam;
import com.evp.eemv.entity.Capk;
import com.evp.eventbus.Event;
import com.evp.paxprinter.init.BP60A_BE_C2Print;
import com.evp.paxprinter.init.InternalPrint;
import com.evp.pay.trans.BaseTrans;
import com.evp.pay.trans.SaleTrans;
import com.evp.pay.trans.model.AcqManager;
import com.evp.pay.trans.model.Controller;
import com.evp.update.Updater;
import com.evp.pay.utils.InjectKeyUtil;
import com.evp.pay.utils.ResponseCode;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.poslib.gl.IGL;
import com.evp.poslib.gl.IPacker;
import com.evp.poslib.gl.convert.ConvertHelper;
import com.evp.poslib.gl.convert.IConvert;
import com.evp.poslib.gl.impl.GL;
import com.evp.poslib.neptune.Sdk;
import com.evp.settings.SysParam;
import com.pax.dal.IDAL;
import com.pax.dal.entity.ETermInfoKey;
import com.sankuai.waimai.router.service.ServiceLoader;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

/**
 * customized application
 */
public class FinancialApplication extends BaseApplication {
    /**
     * The constant TAG.
     */
    public static final String TAG = FinancialApplication.class.getSimpleName();
    private static FinancialApplication mApp;
    private static Controller controller;
    private static ResponseCode rspCode;
    private static GeneralParam generalParam;
    private static AcqManager acqManager;

    private static TransDataDbHelper transDataDbHelper;

    // Neptune interface
    private static IDAL dal;
    private static IGL gl;
    private static IConvert convert;
    private static IPacker packer;

    //App Store
    private static DownloadManager downloadManager;

    // app version
    private static String appName;
    private static String version;

    private static boolean isJapanese;

    private static Handler handler;
    private ExecutorService backgroundExecutor;
    private static ETransType currentETransType;
    private static List<Capk> capkList;
    private static List<AidParam> aidParamList;

    @Override
    public void onCreate() {
        super.onCreate();
        DaoManager.getInstance().init();
        ServiceLoader.lazyInit();
        InternalPrint.init();
        //auto select BP60A_C2 or BP60A_BE,if you only want use BP60A_C2 or BP60A_BE,just init BpPrint or BePrint instead
        BP60A_BE_C2Print.init();
        ConvertHelper.init(true);
        AppInfo.getInstance().init(this);
        registerActivityLifecycleCallbacks(new AppActivityLifecycleCallbacks());
        mApp = this;
        version = updateVersion();
        // app name
        ApplicationInfo applicationInfo = getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        appName = stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : getString(stringId);
        init();
        handler = new Handler();
        backgroundExecutor = ThreadPoolManager.getInstance().getExecutor();
        initData();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //AET-149
        Utils.changeAppLanguage(mApp, CurrencyConverter.setDefCurrency(SysParam.getInstance().getString(R.string.EDC_CURRENCY_LIST)));
    }

    /**
     * Init.
     */
    public static void init() {

        //Init singletons
        dal = Sdk.getInstance().getDal(mApp);
        GL.init(mApp);
        gl = GL.getGL();
        convert = getGl().getConvert();
        packer = getGl().getPacker();
        transDataDbHelper = TransDataDbHelper.getInstance();
        controller = new Controller();
        generalParam = new GeneralParam();
        acqManager = AcqManager.getInstance();

        //PAX store & download manager
        downloadManager = DownloadManager.getInstance();
        downloadManager.setAppKey(AppInfo.getInstance().getAppKey());
        downloadManager.setAppSecret(AppInfo.getInstance().getAppSecret());
        downloadManager.setSn(dal.getSys().getTermInfo().get(ETermInfoKey.SN));
        downloadManager.setFilePath(ConfigConst.PATH_TO_UPDATE_FOLDER);
        downloadManager.init(mApp, new Updater(), new IDownloadListener() {
            @Override
            public boolean isReadyUpdate() {
                boolean result = !SaleTrans.isTransRunning() && getTransDataDbHelper().countOf() == 0;
                Log.i(TAG, "isReadyUpdate result : " + result);
                BaseTrans.setUpdating(result);
                return result;
            }

            @Override
            public void setErrorMessage(String message) {
                SysParam.getInstance().set(Utils.getString(R.string.IS_PAXSTORE_UPDATE_PARAM_EXCEPTION), true);
                SysParam.getInstance().set(Utils.getString(R.string.PAXSTORE_UPDATE_PARAM_EXCEPTION), message);
            }
        });
        downloadManager.addConfigFile(ConfigConst.APP_CONFIG_FILE);
        downloadManager.addResourceFile(ConfigConst.APP_RESOURCE_FILE);

        //Key injector but only for DEMO mode
        InjectKeyUtil.injectMKSK(SysParam.getInstance().getString(R.string.COMM_TYPE));

        if (Locale.getDefault().getLanguage().equals("ja")) {
            isJapanese = true;
        }

        //Mr. crash handler
        CrashHandler.getInstance();

        //App configuration
        ConfigUtils.getInstance().loadConfiguration();
    }

    /**
     * Init data.
     */
    public static void initData() {
        mApp.runInBackground(() -> {
            // init response code
            rspCode = ResponseCode.getInstance();
            rspCode.init();
        });
    }

    /**
     * get app version
     */
    private String updateVersion() {
        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            LogUtils.w(TAG, e);
            return null;
        }
    }

    /**
     * Gets app.
     *
     * @return the app
     */
// getter
    public static FinancialApplication getApp() {
        return mApp;
    }

    public static TransDataDbHelper getTransDataDbHelper() {
        return transDataDbHelper;
    }

    /**
     * Gets controller.
     *
     * @return the controller
     */
    public static Controller getController() {
        return controller;
    }

    /**
     * Gets rsp code.
     *
     * @return the rsp code
     */
    public static ResponseCode getRspCode() {
        return rspCode;
    }

    /**
     * Gets general param.
     *
     * @return the general param
     */
    public static GeneralParam getGeneralParam() {
        return generalParam;
    }

    /**
     * Gets acq manager.
     *
     * @return the acq manager
     */
    public static AcqManager getAcqManager() {
        return acqManager;
    }

    /**
     * Gets dal.
     *
     * @return the dal
     */
    public static IDAL getDal() {
        return dal;
    }

    /**
     * Gets gl.
     *
     * @return the gl
     */
    public static IGL getGl() {
        return gl;
    }

    /**
     * Gets convert.
     *
     * @return the convert
     */
    public static IConvert getConvert() {
        return convert;
    }

    /**
     * Gets packer.
     *
     * @return the packer
     */
    public static IPacker getPacker() {
        return packer;
    }

    /**
     * Gets download manager.
     *
     * @return the download manager
     */
    public static DownloadManager getDownloadManager() {
        return downloadManager;
    }

    public static String getAppName() {
        return appName;
    }

    /**
     * Gets version.
     *
     * @return the version
     */
    public static String getVersion() {
        return version;
    }

    /**
     * Run in background.
     *
     * @param runnable the runnable
     */
// merge handles from all activities
    public void runInBackground(final Runnable runnable) {
        backgroundExecutor.execute(runnable);
    }

    /**
     * Run on ui thread.
     *
     * @param runnable the runnable
     */
    public void runOnUiThread(final Runnable runnable) {
        handler.post(runnable);
    }

    /**
     * Run on ui thread delay.
     *
     * @param runnable    the runnable
     * @param delayMillis the delay millis
     */
    public void runOnUiThreadDelay(final Runnable runnable, long delayMillis) {
        handler.postDelayed(runnable, delayMillis);
    }

    /**
     * Register.
     *
     * @param obj the obj
     */
// eventbus helper
    public void register(Object obj) {
        EventBus.getDefault().register(obj);
    }

    /**
     * Unregister.
     *
     * @param obj the obj
     */
    public void unregister(Object obj) {
        EventBus.getDefault().unregister(obj);
    }

    /**
     * Do event.
     *
     * @param event the event
     */
    public void doEvent(Event event) {
        EventBus.getDefault().post(event);
    }

    /**
     * Do event.
     *
     * @param event the event
     */
    public void doEvent(Object event) {
        EventBus.getDefault().post(event);
    }

    /**
     * Gets background executor.
     *
     * @return the background executor
     */
    public ExecutorService getBackgroundExecutor() {
        return backgroundExecutor;
    }

    /**
     * Is japanese boolean.
     *
     * @return the boolean
     */
    public static boolean isJapanese() {
        return isJapanese;
    }

    /**
     * Gets current e trans type.
     *
     * @return the current e trans type
     */
    public static ETransType getCurrentETransType() {
        return currentETransType;
    }

    /**
     * Sets current e trans type.
     *
     * @param currentETransType the current e trans type
     */
    public static void setCurrentETransType(ETransType currentETransType) {
        FinancialApplication.currentETransType = currentETransType;
    }

    /**
     * Gets capk list.
     *
     * @return the capk list
     */
    public static List<Capk> getCapkList() {
        return capkList;
    }

    /**
     * Sets capk list.
     *
     * @param capkList the capk list
     */
    public static void setCapkList(List<Capk> capkList) {
        FinancialApplication.capkList = capkList;
    }

    /**
     * Gets aid param list.
     *
     * @return the aid param list
     */
    public static List<AidParam> getAidParamList() {
        return aidParamList;
    }

    /**
     * Sets aid param list.
     *
     * @param aidParamList the aid param list
     */
    public static void setAidParamList(List<AidParam> aidParamList) {
        FinancialApplication.aidParamList = aidParamList;
    }

}
