/*
 *  * ===========================================================================================
 *  * = COPYRIGHT
 *  *          PAX Computer Technology(Shenzhen); CO., LTD PROPRIETARY INFORMATION
 *  *   This software is supplied under the terms of a license agreement or nondisclosure
 *  *   agreement with PAX Computer Technology(Shenzhen); CO., LTD and may not be copied or
 *  *   disclosed except in accordance with the terms in that agreement.
 *  *     Copyright (C); 2019-? PAX Computer Technology(Shenzhen); CO., LTD All rights reserved.
 *  * Description: // Detail description about the voidction of this module,
 *  *             // interfaces with the other modules, and dependencies.
 *  * Revision History:
 *  * Date                  Author	                 Action
 *  * 20200713  	         xieYb                   Modify
 *  * ===========================================================================================
 *
 */
package com.evp.paxprinter.btscan;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.evp.commonlib.event.RxBus;
import com.evp.commonlib.utils.LogUtils;
import com.evp.paxprinter.R;
import com.evp.poslib.print.entity.PrinterInfo;
import com.pax.gl.commhelper.IBtScanner;
import com.pax.gl.commhelper.IBtScanner.IBtDevice;
import com.pax.gl.commhelper.IBtScanner.IBtScannerListener;
import com.pax.gl.commhelper.impl.PaxGLComm;

import java.lang.ref.WeakReference;

/**
 * This class works for scanning Bluetooth device
 */
public class BluetoothDeviceListActivity extends AppCompatActivity {
    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    private IBtScanner btScan;
    private Button scanButton;
    private Button cancelButton;
    private boolean correctDevice = false;
    private boolean emptyResultPost = false;
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {

        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Get the device MAC address, which is the last 17 chars in the View
            String selectedDevice = ((TextView) v).getText().toString();
            if (selectedDevice.length() >= 17) {
                correctDevice = true;
                int i = selectedDevice.indexOf("\n");
                RxBus.getInstance().post(new PrinterInfo(selectedDevice.substring(0, i), selectedDevice.substring(selectedDevice.length() - 17)));
            }
            btScan.stop();
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.bt_device_list);
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setTitle(R.string.look_for_printer);
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(true);
        }
        checkPermission();
        // Initialize the button to perform device discovery
        scanButton = (Button) findViewById(R.id.bt_scan);
        cancelButton = (Button) findViewById(R.id.bt_cancel);

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = (ListView) findViewById(R.id.list_available);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // start discovery automatically.
        scanButton.setEnabled(false);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanButton.setEnabled(false);
                mNewDevicesArrayAdapter.clear();
                doDiscovery();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emptyResultPost = true;
                btScan.stop();
                RxBus.getInstance().post(new PrinterInfo());
                finish();
            }
        });
    }

    static class BtScannerListener implements IBtScannerListener{
        private WeakReference<BluetoothDeviceListActivity> activityWeakReference;

        public BtScannerListener(BluetoothDeviceListActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onDiscovered(IBtDevice dev) {
            BluetoothDeviceListActivity activity = activityWeakReference.get();
            if (activity == null){
                return;
            }
            LogUtils.d(TAG, dev.getName() + "\n" + dev.getIdentifier());
            activity.mNewDevicesArrayAdapter.add(dev.getName() + "\n" + dev.getIdentifier());
            activity.mNewDevicesArrayAdapter.notifyDataSetChanged();
        }

        @Override
        public void onFinished() {
            BluetoothDeviceListActivity activity = activityWeakReference.get();
            if (activity == null){
                return;
            }
            activity.scanButton.setEnabled(true);
        }
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        btScan = PaxGLComm.getInstance(this).getBtScanner();
        btScan.start(new BtScannerListener(BluetoothDeviceListActivity.this),(int) 10);
    }

    private void checkPermission() {
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion >= android.os.Build.VERSION_CODES.M) {//API LEVEL 18
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                LogUtils.d(TAG, "checkPermission - requestPermissions");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            } else {
                doDiscovery();
                LogUtils.d(TAG, "checkPermission - no need requestPermissions");
            }
        } else {
            doDiscovery();
            LogUtils.d(TAG, "checkPermission - SDK Version < 23");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1001:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    doDiscovery();
                    LogUtils.d(TAG, "requestPermissions success");
                } else {
                    LogUtils.d(TAG, "requestPermissions fail");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onBackPressed() {
        emptyResultPost = true;
        RxBus.getInstance().post(new PrinterInfo());
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        //other exception condition,we need to push the result
        if (!correctDevice && !emptyResultPost) {
            RxBus.getInstance().post(new PrinterInfo());
        }
        super.onStop();
    }
}
