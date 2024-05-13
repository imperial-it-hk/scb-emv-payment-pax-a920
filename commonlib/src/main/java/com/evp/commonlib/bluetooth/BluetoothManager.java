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
 * Date	                 Author	                Action
 * 20200109  	         xieYb                  Create
 * ===========================================================================================
 */
package com.evp.commonlib.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * bluetooth utils
 */
public class BluetoothManager {
    private static Context context;
    private BluetoothAdapter btAdapter;

    private BluetoothManager() {
        init();
    }

    public static BluetoothManager getInstance(Context mContext) {
        context = mContext.getApplicationContext();
        return LazyHolder.INSTANCE;
    }

    private void init() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            return;
        }
        if (!btAdapter.isEnabled()) {
            btAdapter.enable();
        }
    }

    public boolean isBtEnabled() {
        return btAdapter.isEnabled();
    }

    public boolean openBluetooth() {
        return btAdapter.enable();
    }

    public void enableDetect() {
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        context.startActivity(discoverableIntent);
    }

    /**
     * 获取所有已配对的设备
     */
    public List<BluetoothDevice> getPairedDevices() {
        List<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
        if (btAdapter == null) {
            return deviceList;
        }
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        if (!pairedDevices.isEmpty()) {
            deviceList.addAll(pairedDevices);
        }
        return deviceList;
    }

    /**
     * 获取所有已配对的打印类设备
     */
    public List<BluetoothDevice> getPairedPrinterDevices() {
        return getSpecificDevice(BluetoothClass.Device.Major.IMAGING);
    }

    /**
     * 从已配对设配中，删选出某一特定类型的设备展示
     *
     * @param deviceClass
     * @return
     */
    public List<BluetoothDevice> getSpecificDevice(int deviceClass) {
        List<BluetoothDevice> devices = getPairedDevices();
        List<BluetoothDevice> printerDevices = new ArrayList<BluetoothDevice>();

        for (BluetoothDevice device : devices) {
            BluetoothClass klass = device.getBluetoothClass();
            // 关于蓝牙设备分类参考 http://stackoverflow.com/q/23273355/4242112
            if (klass.getMajorDeviceClass() == deviceClass)
                printerDevices.add(device);
        }
        return printerDevices;
    }

    public boolean isPaired(String address) {
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.isEmpty()) {
            return false;
        }
        for (BluetoothDevice device : pairedDevices) {
            String mac = device.getAddress();
            if (mac.equals(address)) {
                return true;
            }
        }
        return false;
    }

    private static class LazyHolder {
        static final BluetoothManager INSTANCE = new BluetoothManager();
    }

}
