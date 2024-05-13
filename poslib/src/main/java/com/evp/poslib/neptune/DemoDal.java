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
 * 20190108  	         Kim.L                   Create
 * ===========================================================================================
 */
package com.evp.poslib.neptune;

import android.content.Context;

import com.pax.dal.IBase;
import com.pax.dal.ICardReaderHelper;
import com.pax.dal.ICashDrawer;
import com.pax.dal.IDAL;
import com.pax.dal.IDalCommManager;
import com.pax.dal.IDeviceInfo;
import com.pax.dal.IFaceDetector;
import com.pax.dal.IFingerprintReader;
import com.pax.dal.IIDReader;
import com.pax.dal.IIDReaderEx;
import com.pax.dal.IIcc;
import com.pax.dal.IKeyBoard;
import com.pax.dal.IMag;
import com.pax.dal.IOCR;
import com.pax.dal.IPed;
import com.pax.dal.IPedBg;
import com.pax.dal.IPedNp;
import com.pax.dal.IPedTrSys;
import com.pax.dal.IPhoneManager;
import com.pax.dal.IPicc;
import com.pax.dal.IPrinter;
import com.pax.dal.IPuk;
import com.pax.dal.IScanCodec;
import com.pax.dal.IScanner;
import com.pax.dal.IScannerHw;
import com.pax.dal.ISignPad;
import com.pax.dal.ISys;
import com.pax.dal.IWifiProbe;
import com.pax.dal.entity.EPedType;
import com.pax.dal.entity.EPiccType;
import com.pax.dal.entity.EScannerType;
import com.pax.dal.pedkeyisolation.IPedKeyIsolation;
/**
 * neptune IDAL
 */
class DemoDal implements IDAL {
    private IMag mag;
    private IIcc icc;
    private IPicc picc;
    private IPed ped;
    private IPedKeyIsolation pedKeyIsolation;
    private ICardReaderHelper cardReaderHelper;
    private IScanner scanner;
    private ISignPad signPad;
    private IKeyBoard keyBoard;
    private IPrinter printer;
    private IDeviceInfo deviceInfo;
    private IPuk puk;
    private ISys sys;
    private IDalCommManager commManager;
    private IIDReader idReader;

    private IPedNp pedNp;
    private IPedBg pedBg;
    private IPhoneManager phoneManager;
    private IIDReaderEx readerEx;

    DemoDal(Context context) {
        mag = new DemoMag();
        icc = new DemoIcc();
        picc = new DemoPicc();
        ped = new DemoPed();
        pedKeyIsolation = new DemoPedIsolation();
        cardReaderHelper = new DemoCardReaderHelper();
        scanner = new DemoScanner();
        signPad = new DemoSignPad();
        keyBoard = new DemoKeyBoard();
        printer = new DemoPrinter();
        deviceInfo = new DemoDeviceInfo();
        puk = new DemoPuk();
        sys = new DemoSys(context);
        commManager = new DemoCommManager();
        idReader = new DemoIDReader();

        pedNp = new DemoPedNp();
        pedBg = new DemoPedBg();
        phoneManager = new DemoPhoneManager();
        readerEx = new DemoReaderEx();
    }

    @Override
    public IMag getMag() {
        return mag;
    }

    @Override
    public IIcc getIcc() {
        return icc;
    }

    @Override
    public IPicc getPicc(EPiccType ePiccType) {
        return picc;
    }

    @Override
    public IPed getPed(EPedType ePedType) {
        return ped;
    }

    @Override
    public IPedTrSys getPedTrSys() {
        return null;
    }

    @Override
    public IPedNp getPedNp() {
        return pedNp;
    }

    @Override
    public IPedBg getPedBg() {
        return pedBg;
    }

    @Override
    public ICardReaderHelper getCardReaderHelper() {
        return cardReaderHelper;
    }

    @Override
    public IScanner getScanner(EScannerType eScannerType) {
        return scanner;
    }

    @Override
    public IScannerHw getScannerHw() {
        return null;
    }

    @Override
    public ISignPad getSignPad() {
        return signPad;
    }

    @Override
    public IKeyBoard getKeyBoard() {
        return keyBoard;
    }

    @Override
    public IPrinter getPrinter() {
        return printer;
    }

    @Override
    public IDeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    @Override
    public IPuk getPuk() {
        return puk;
    }

    @Override
    public ISys getSys() {
        return sys;
    }

    @Override
    public IDalCommManager getCommManager() {
        return commManager;
    }

    @Override
    public IIDReader getIDReader() {
        return idReader;
    }

    @Override
    public IPedKeyIsolation getPedKeyIsolation(EPedType var1){
        return pedKeyIsolation;
    }

    @Override
    public ICashDrawer getCashDrawer(){
        return null;
    }

    @Override
    public IScanCodec getScanCodec() {
        return null;
    }

    @Override
    public IWifiProbe getWifiProbe() {
        return null;
    }

    @Override
    public IPhoneManager getPhoneManager() {
        return phoneManager;
    }

    @Override
    public IIDReaderEx getIDReaderEx() {
        return readerEx;
    }

    @Override
    public IFingerprintReader getFingerprintReader() {
        return null;
    }

    @Override
    public IBase getBase() {
        return null;
    }

    @Override
    public IFaceDetector getFaceDetector() {
        return null;
    }

    @Override
    public IOCR getOCR() {
        return null;
    }
}
