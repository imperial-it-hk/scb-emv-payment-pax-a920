package com.evp.appstore;

public interface IUpdater {
    boolean loadConfigFile();
    boolean loadResourceFiles();
    boolean loadEmvParameters();
    boolean loadNonEmvParameters();
}
