package com.evp.appstore;

import android.content.Context;
import android.os.RemoteException;

import com.evp.commonlib.utils.LogUtils;
import com.pax.market.android.app.sdk.BaseApiService;
import com.pax.market.android.app.sdk.StoreSdk;

import java.io.File;


public class DownloadManager {
    private static final String TAG = DownloadManager.class.getSimpleName();

    private String appKey;
    private String appSecret;
    private String sn;
    private String filePath;
    private static DownloadManager instance;
    private IDownloadListener downloadListener;
    private IUpdater paramUpdater;
    private String configFileName;
    private String resourceFileName;

    public static synchronized DownloadManager getInstance() {
        if (instance == null) {
            instance = new DownloadManager();
        }
        return instance;
    }

    public boolean updateData() {
        LogUtils.i(TAG, "UpdateData START");

        if(!paramUpdater.loadConfigFile()) {
            LogUtils.i(TAG, "loadConfigFile FAILED");
            return false;
        }
        if(!paramUpdater.loadResourceFiles()) {
            LogUtils.i(TAG, "loadResourceFiles FAILED");
        }
        if(!paramUpdater.loadEmvParameters()) {
            LogUtils.i(TAG, "loadEmvParameters FAILED");
        }
        if(!paramUpdater.loadNonEmvParameters()) {
            LogUtils.i(TAG, "loadNonEmvParameters FAILED");
        }

        LogUtils.i(TAG, "UpdateData END");
        return true;
    }

    public boolean isUpdateRequired() {
        LogUtils.i(TAG, "isUpdateRequired START");

        final String JSON_EXTENSION = ".json";
        final String ZIP_EXTENSION = ".zip";
        boolean updateRequired = false;
        try {
            File dir = new File(filePath);
            File[] files = dir.listFiles();
            if(files == null) {
                LogUtils.e(TAG, "Update files does not exists!");
                return false;
            }

            for (File file : files) {
                if(!file.getName().endsWith(JSON_EXTENSION) && !file.getName().endsWith(ZIP_EXTENSION)) {
                    continue;
                }
                if (file.getName().startsWith("config") && file.getName().endsWith(JSON_EXTENSION)) {
                    File newFile = new File(filePath + configFileName);
                    if(!file.renameTo(newFile)) {
                        LogUtils.e(TAG, "Config file rename FAILED!");
                        return false;
                    }
                    updateRequired = true;
                    LogUtils.i(TAG, "Parameter update is REQUIRED.");
                }
                if (file.getName().startsWith("resource") && file.getName().endsWith(ZIP_EXTENSION)) {
                    File newFile = new File(filePath + resourceFileName);
                    if(!file.renameTo(newFile)) {
                        LogUtils.e(TAG, "Resource file rename FAILED!");
                        return false;
                    }
                    updateRequired = true;
                    LogUtils.i(TAG, "Resource update is REQUIRED.");
                }
            }
        } catch (SecurityException | NullPointerException e) {
            LogUtils.e(TAG, e);
            return false;
        }

        LogUtils.i(TAG, "isUpdateRequired END");
        return updateRequired;
    }

    public void init(Context context, IUpdater updater, IDownloadListener listener) {
        LogUtils.i(TAG, "DownloadManager Init START");

        downloadListener = listener;
        paramUpdater = updater;
        StoreSdk.getInstance().init(context, appKey, appSecret, sn, new BaseApiService.Callback() {
            @Override
            public void initSuccess() {
                LogUtils.d(TAG, "PaxStore SDK init success");
                StoreSdk.getInstance().initInquirer(new StoreSdk.Inquirer() {
                    @Override
                    public boolean isReadyUpdate() {
                        return downloadListener.isReadyUpdate();
                    }
                });
            }

            @Override
            public void initFailed(RemoteException e) {
                LogUtils.d(TAG, "PaxStore SDK init fail");
            }
        });

        LogUtils.i(TAG, "DownloadManager Init END");
    }

    public void addConfigFile(String fileName) {
        LogUtils.i(TAG, "Adding config file: " + fileName);
        configFileName = fileName;
    }

    public void addResourceFile(String fileName) {
        LogUtils.i(TAG, "Adding resource file: " + fileName);
        resourceFileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
        LogUtils.d(TAG, "PAXSTORE_APP_KEY: " + appKey);
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
        LogUtils.d(TAG, "PAXSTORE_APP_SECRET: " + appSecret);
    }
}
