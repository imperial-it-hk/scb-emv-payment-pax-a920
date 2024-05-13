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
 * 20190108  	         Steven.S                Create
 * ===========================================================================================
 */

package com.evp.pay.utils.lightscanner;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.evp.commonlib.utils.LogUtils;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.utils.lightscanner.tool.InactivityTimer;
import com.evp.pay.utils.lightscanner.tool.LightScanner;
import com.evp.pay.utils.lightscanner.tool.LightScannerManager;
import com.evp.pay.utils.lightscanner.view.ViewfinderView;
import com.evp.payment.evpscb.R;
import com.pax.dal.entity.DecodeResult;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.IOException;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

/**
 * The type Light scanner activity.
 */
public class LightScannerActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.PreviewCallback, OnClickListener {
    /**
     * The constant CLOSE_SCANNER_INTENT_ACTION.
     */
    public static final String CLOSE_SCANNER_INTENT_ACTION = "android.intent.action.CloseLightScanner";
    private static final int WIDTH = 640;
    private static final int HEIGHT = 480;
    private boolean isOpen = false;
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private Camera camera;
    private byte[] data;
    private ViewfinderView viewfinderView;
    private ImageView ivHeaderBack;
    private TextView tvHeaderTitle;
    private RelativeLayout rlHeaderLayout;
    private CompositeDisposable disposable;
    private RxPermissions rxPermissions;

    private int timeout;
    private InactivityTimer inactivityTimer;

    private BroadcastReceiver mDestroyActivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(CLOSE_SCANNER_INTENT_ACTION)) {
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_scanner);

        loadParameters();
        initViews();
        registerDestroyActivityReceiver();
        requestPermissions();
    }

    private void requestPermissions() {
        disposable = new CompositeDisposable();
        rxPermissions = new RxPermissions(this);
        disposable.add(rxPermissions.request(Manifest.permission.CAMERA).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean granted) throws Exception {
                if (granted) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            openCamera();
                        }
                    }).start();
                } else {
                    Toast.makeText(LightScannerActivity.this, R.string.camera_permission, Toast.LENGTH_LONG).show();
                }
            }
        }));
    }


    private void loadParameters() {
        timeout = getIntent().getIntExtra(LightScanner.TIMEOUT, 2 * 60);
    }

    private void initViews() {
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        tvHeaderTitle = (TextView) findViewById(R.id.header_title);
        tvHeaderTitle.setText(R.string.trans_scanner);
        rlHeaderLayout = (RelativeLayout) findViewById(R.id.id_header_layout);
        rlHeaderLayout.setBackgroundResource(R.color.special_header_layout_color);
        ivHeaderBack = (ImageView) findViewById(R.id.header_back);
        ivHeaderBack.setOnClickListener(this);

        inactivityTimer = new InactivityTimer(this, timeout);
    }

    private void openCamera() {
        if (!isOpen) {
            LightScannerManager.getInstance().init(LightScannerActivity.this, WIDTH, HEIGHT);

            holder = surfaceView.getHolder();
            holder.addCallback(this);
            initCamera();
            camera.addCallbackBuffer(data);
            camera.setPreviewCallbackWithBuffer(this);

            try {
                camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                LogUtils.e(e);
            }
            camera.startPreview();
            //drawViewfinder();
            isOpen = !isOpen;
        } else {
            releaseRes();
            isOpen = !isOpen;
        }
    }

    private void initCamera() {
        camera = Camera.open(0);
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(WIDTH, HEIGHT);
        parameters.setPictureSize(WIDTH, HEIGHT);
        parameters.setZoom(parameters.getZoom());
        camera.setParameters(parameters);
        setCameraDisplayOrientation(this, 0, camera);

        // For formats besides YV12, the size of the buffer is determined by multiplying the preview image width,
        // height, and bytes per pixel. The width and height can be read from Camera.Parameters.getPreviewSize(). Bytes
        // per pixel can be computed from android.graphics.ImageFormat.getBitsPerPixel(int) / 8, using the image format
        // from Camera.Parameters.getPreviewFormat().
        float bytesPerPixel = ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / (float) 8;
        data = new byte[(int) (bytesPerPixel * WIDTH * HEIGHT)];
    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        if (data != null) {
            FinancialApplication.getApp().runInBackground(new Runnable() {
                @Override
                public void run() {
                    DecodeResult decodeResult = LightScannerManager.getInstance().decode(data);
                    camera.addCallbackBuffer(data);
                    if (decodeResult.getContent() != null) {
                        sendSuccessBroadcast(decodeResult.getContent());
                    }
                }
            });
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    protected void onDestroy() {
        disposable.clear();
        inactivityTimer.shutdown();
        if (isOpen) {
            releaseRes();
        }
        unregisterReceiver(mDestroyActivityReceiver);
        super.onDestroy();
    }

    private void releaseRes() {
        LightScannerManager.getInstance().release();
        camera.setPreviewCallbackWithBuffer(null);
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    private void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        //See android.hardware.Camera.setCameraDisplayOrientation for documentation.
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int degrees = getDisplayRotation(activity);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; //compensate the mirror
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private int getDisplayRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
            default:
                return 0;
        }
    }

    /**
     * Draw viewfinder.
     */
    public void drawViewfinder() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewfinderView.drawViewfinder();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.header_back:
                sendCancelBroadcast();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            sendCancelBroadcast();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void registerDestroyActivityReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CLOSE_SCANNER_INTENT_ACTION);
        registerReceiver(mDestroyActivityReceiver, intentFilter);
    }

    private void sendSuccessBroadcast(String qrCodeStr) {
        Intent intent = new Intent(LightScanner.SCAN_INTENT_ACTION);
        intent.putExtra(LightScanner.FLAGS, LightScanner.SUCCESS_FLAG);
        intent.putExtra(LightScanner.QR_CODE_STR, qrCodeStr);
        sendBroadcast(intent);
    }

    private void sendCancelBroadcast() {
        Intent intent = new Intent(LightScanner.SCAN_INTENT_ACTION);
        intent.putExtra(LightScanner.FLAGS, LightScanner.CANCEL_FLAG);
        sendBroadcast(intent);
    }

    /**
     * Send scan error broadcast.
     */
    public void sendScanTimeoutBroadcast() {
        Intent intent = new Intent(LightScanner.SCAN_INTENT_ACTION);
        intent.putExtra(LightScanner.FLAGS, LightScanner.TIMEOUT_FLAG);
        sendBroadcast(intent);
    }
}
