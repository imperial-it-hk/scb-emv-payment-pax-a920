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
package com.evp.poslib.print.impl;

import android.content.Context;
import android.graphics.Bitmap;

import com.evp.commonlib.utils.LogUtils;
import com.evp.poslib.print.entity.PrinterInfo;
import com.evp.poslib.print.exception.PrinterException;
import com.pax.gl.commhelper.ICommBt;
import com.pax.gl.commhelper.impl.PaxGLComm;
import com.pax.gl.extprinter.entity.BitmapLine;
import com.pax.gl.extprinter.entity.TextLine;
import com.pax.gl.extprinter.exception.CommException;
import com.pax.gl.extprinter.exception.PrintException;
import com.pax.gl.extprinter.impl.GLExtPrinter;
import com.pax.gl.extprinter.inf.ICommListener;
import com.pax.gl.extprinter.inf.IExtPrinter;
import com.pax.gl.extprinter.inf.ILine;

import java.util.ArrayList;
import java.util.List;

public class BpPrinter extends ABtPrinter {
    private PaxGLComm paxGLComm;
    private ICommBt mIomm;
    private ICommListener btlistener = new ICommListener() {

        //send data
        @Override
        public void onSend(byte[] data) throws CommException {
            if (mIomm != null) {
                try {
                    mIomm.send(data);
                } catch (com.pax.gl.commhelper.exception.CommException e) {
                    throw new CommException(CommException.ERR_RECV);
                }
            }
        }

        @Override
        public byte[] onRecv(int expLen) throws CommException {
            byte[] data;
            if (mIomm != null) {
                try {
                    data = mIomm.recv(1);
                } catch (com.pax.gl.commhelper.exception.CommException e) {
                    throw new CommException(CommException.ERR_RECV);
                }

                return data;
            } else {
                return new byte[0];
            }
        }

        @Override
        public void onReset() {
            if (mIomm != null) {
                mIomm.reset();
            }
        }
    };

    public BpPrinter(Context context, PrinterInfo printerInfo) {
        this.context = context;
        this.printerInfo = printerInfo;
        paxGLComm = PaxGLComm.getInstance(context);
        mIomm = paxGLComm.createBt(printerInfo.getIdentifier());
    }

    /**
     * print bitmap
     * @param bitmap bitmap
     * @return print status
     * @throws PrinterException exception
     */
    @Override
    public int print(Bitmap bitmap) throws PrinterException {
        int result = -1;
        IExtPrinter mPrinter = GLExtPrinter.createEscPosPrinter(btlistener, 383);
        try {
            mPrinter.reset();
            mPrinter.setGrayLevel(2);
            mPrinter.print(new BitmapLine(bitmap));
            mPrinter.feedPaper(200);//Paper feed
            mPrinter.cutPaper(1);//cut paper
            result = 0;
        } catch (CommException e) {
            throw new PrinterException(e.getErrCode(), e.getErrMsg());
        } catch (PrintException e) {
            throw castPrinterException(e);
        }
        return result;
    }

    /**
     * print single line
     * @param line single line
     * @return print status
     * @throws PrinterException exception
     */
    @Override
    public int print(String line) throws PrinterException {
        List<ILine> lines = new ArrayList<ILine>();
        lines.add(new TextLine().addUnit(line, null));
        return print(lines);
    }

    @Override
    public boolean connect() {
        String identifier = printerInfo.getIdentifier();
        long start = System.currentTimeMillis();
        long end = 0;
        LogUtils.d(TAG, "start connect to BpPrinter:" + identifier + ",start time:" + start);
        try {
            //if connect when printer powered off, set timeout for shortest wait.
            mIomm.setConnectTimeout(8000);
            mIomm.connect();
        } catch (Exception e) {
            LogUtils.e(TAG, e);
            end = System.currentTimeMillis();
            LogUtils.d(TAG, "finish connect to BpPrinter:" + identifier + ",end time:" + end);
            LogUtils.d(TAG, "total time:" + (end - start));
            return false;
        }
        end = System.currentTimeMillis();
        LogUtils.d(TAG, "finish connect to BpPrinter:" + identifier + ",end time:" + end);
        LogUtils.d(TAG, "total time:" + (end - start));
        return true;
    }

    @Override
    public void disConnect() {
        if (mIomm == null){
            return;
        }
        mIomm.cancelRecv();
        try {
            mIomm.disconnect();
        } catch (com.pax.gl.commhelper.exception.CommException e) {
            LogUtils.e(TAG, e);
        }
        mIomm = null;
    }

    public int print(List<ILine> lines) throws PrinterException {
        int result = -1;
        IExtPrinter mPrinter = GLExtPrinter.createEscPosPrinter(btlistener, 383);
        try {
            mPrinter.reset();
            mPrinter.setGrayLevel(2);
            mPrinter.print(lines);
            mPrinter.feedPaper(200);//Paper feed
            mPrinter.cutPaper(1);//cut paper
            result = 0;
        } catch (CommException e) {
            throw new PrinterException(e.getErrCode(), e.getErrMsg());
        } catch (PrintException e) {
            throw castPrinterException(e);
        }
        return result;
    }

    /**
     * GLExtPrinter problem cause we can't get right errorCode(from e.getErrCode())
     * and errorMsg(from e.getErrMsg())
     *
     * @param e PrintException
     * @return PrinterException
     */
    private PrinterException castPrinterException(PrintException e) {
        PrinterException printerException = new PrinterException(e.getErrCode(), e.getErrMsg());
        String message = e.getMessage();
        if (message != null) {
            int codeBegin = message.indexOf("#");
            int codeEnd = message.indexOf("(");
            int msgEnd = message.indexOf(")");
            try {
                int errorCode = Integer.parseInt(message.substring(codeBegin + 1, codeEnd));
                String errorMsg = message.substring(codeEnd + 1, msgEnd);
                printerException = new PrinterException(errorCode, errorMsg);
            } catch (NumberFormatException e1) {
                return printerException;
            }

        }
        return printerException;
    }
}
