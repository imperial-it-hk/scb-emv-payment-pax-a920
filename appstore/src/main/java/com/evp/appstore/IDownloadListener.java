package com.evp.appstore;

/**
 * Created by laiyi on 2018/2/7.
 */

public interface IDownloadListener {
    boolean isReadyUpdate();
    void setErrorMessage(String message);
}
