package com.evp.appstore;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.evp.commonlib.utils.LogUtils;
import com.evp.commonlib.utils.ThreadPoolManager;
import com.pax.market.android.app.sdk.NotificationUtils;
import com.pax.market.android.app.sdk.StoreSdk;
import com.pax.market.api.sdk.java.base.dto.DownloadResultObject;
import com.pax.market.api.sdk.java.base.exception.NotInitException;

import java.io.File;


public class DownloadParamService extends Service {
    private static final String TAG = DownloadParamService.class.getSimpleName();
    private DownloadResultObject result;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationUtils.showForeGround(this, "Downloading params");
        LogUtils.d(TAG, "PaxStore Download Parameter Service started ");
        getParamList();
        return super.onStartCommand(intent, flags, startId);
    }

    public void getParamList() {
        final String saveFilePath = DownloadManager.getInstance().getFilePath();
        LogUtils.d(TAG, "saveFilePath = " + saveFilePath);
        ThreadPoolManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    deleteOldFiles();
                    result = StoreSdk.getInstance().paramApi().downloadParamToPath(getApplication().getPackageName(), getVersion(), saveFilePath);
                    LogUtils.d(TAG, "StoreSdk downloadParamToPath: code = " + result.getBusinessCode());
                    LogUtils.d(TAG, "StoreSdk downloadParamToPath: msg = " + result.getMessage());
                    LogUtils.d(TAG, "StoreSdk downloadParamToPath: path = " + result.getParamSavePath());
                } catch (NotInitException e) {
                    LogUtils.e(TAG, "StoreSdk downloadParamToPath: exception: " + e);
                }
            }
        });
    }

    private void deleteOldFiles() {
        try {
            File dir = new File(DownloadManager.getInstance().getFilePath());
            File[] files = dir.listFiles();

            for (File file : files) {
                if (file.getName().endsWith(".p")) {
                    if (file.delete()) {
                        LogUtils.i(TAG, "File p deleted");
                    } else {
                        LogUtils.e(TAG, "File p not deleted!");
                    }
                }
                if (file.getName().endsWith(".zip")) {
                    if (file.delete()) {
                        LogUtils.i(TAG, "File zip deleted");
                    } else {
                        LogUtils.e(TAG, "File zip deleted");
                    }
                }
                if (file.getName().endsWith(".json")) {
                    if (file.delete()) {
                        LogUtils.i(TAG, "File json deleted");
                    } else {
                        LogUtils.e(TAG, "File json deleted");
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, e);
        }
    }

    private int getVersion() {
        try {
            PackageManager manager = getPackageManager();
            PackageInfo packageInfo = manager.getPackageInfo(getPackageName(), 0);
            if (packageInfo != null) {
                return packageInfo.versionCode;
            }
        } catch (Exception e) {
            LogUtils.w(TAG, e);
        }
        return 0;
    }

}
