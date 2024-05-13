package com.evp.update;

import com.evp.appstore.IUpdater;
import com.evp.bizlib.data.entity.AcqIssuerRelation;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.CardRange;
import com.evp.bizlib.data.entity.EmvAid;
import com.evp.bizlib.data.entity.EmvCapk;
import com.evp.bizlib.data.entity.EmvTerminal;
import com.evp.bizlib.data.entity.Issuer;
import com.evp.commonlib.utils.LogUtils;
import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.config.ConfigModel;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.trans.model.AcqManager;
import com.evp.payment.evpscb.R;
import com.evp.settings.SysParam;
import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Updater implements IUpdater {
    private static final String TAG = Updater.class.getSimpleName();

    public boolean loadConfigFile() {
        LogUtils.d(TAG, "loadConfigFile START");

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(ConfigConst.PATH_TO_UPDATE_FOLDER + ConfigConst.APP_CONFIG_FILE))) {
            Gson gson = new Gson();
            ConfigModel configModel = gson.fromJson(reader, ConfigModel.class); //Validate config file with configModel
            if(configModel == null) {
                LogUtils.e(TAG, "Parsing of json failed!");
                return false;
            }
            StringBuilder json = new StringBuilder();
            gson.toJson(configModel, json);
            SysParam.getInstance().set(ConfigConst.ENTIRE_CONFIG, json.toString());
        } catch (Exception e) {
            LogUtils.e(TAG, e);
            return false;
        }

        if(!deleteFile(ConfigConst.PATH_TO_UPDATE_FOLDER + ConfigConst.APP_CONFIG_FILE)) {
            LogUtils.e(TAG, "Can't delete config file!");
            return false;
        }

        //Load new configuration
        ConfigUtils.getInstance().loadConfiguration();
        ConfigUtils.getInstance().reloadCurrency();

        LogUtils.d(TAG, "loadConfigFile END");
        return true;
    }

    public boolean loadResourceFiles() {
        LogUtils.d(TAG, "loadResourceFiles START");
        File file;
        try {
            file = new File(ConfigConst.PATH_TO_UPDATE_FOLDER + ConfigConst.APP_RESOURCE_FILE);
        } catch (NullPointerException e) {
            LogUtils.e(TAG, e);
            return false;
        }

        if (!file.exists() || !file.isFile()) {
            deleteFile(ConfigConst.PATH_TO_UPDATE_FOLDER + ConfigConst.APP_RESOURCE_FILE);
            LogUtils.e(TAG, "Invalid resource file!!!");
            return false;
        }

        if(!unzip(file, new File(ConfigConst.PATH_TO_UPDATE_FOLDER + ConfigConst.RESOURCE_FOLDER))) {
            deleteFile(ConfigConst.PATH_TO_UPDATE_FOLDER + ConfigConst.APP_RESOURCE_FILE);
            LogUtils.e(TAG, String.format("%s %s%s", "Unzip failed for file:", ConfigConst.PATH_TO_UPDATE_FOLDER, ConfigConst.APP_RESOURCE_FILE));
            return false;
        }

        deleteFile(ConfigConst.PATH_TO_UPDATE_FOLDER + ConfigConst.APP_RESOURCE_FILE);
        LogUtils.d(TAG, "loadResourceFiles END");
        return true;
    }

    public boolean loadEmvParameters() {
        boolean result;

        LogUtils.d(TAG, "loadEmvParameters START");

        //CAPK
        List<EmvCapk> capkList = ConfigUtils.getInstance().convert(EmvCapk.class);
        if (!capkList.isEmpty()) {
            result = EmvCapk.deleteAllRecords();
            LogUtils.d(TAG, "Delete EMV CAPK records result is " + result);
            result = EmvCapk.load(capkList);
            LogUtils.d(TAG, "Load EMV CAPK result is " + result);
            if (!result) {
                return false;
            }
        } else {
            LogUtils.d(TAG, "EMV CAPK parameters are not present");
        }

        //AID
        List<EmvAid> aidList = ConfigUtils.getInstance().convert(EmvAid.class);
        if (!aidList.isEmpty()) {
            result = EmvAid.deleteAllRecords();
            LogUtils.d(TAG, "Delete EMV AID records result is " + result);
            result = EmvAid.load(aidList);
            LogUtils.d(TAG, "Load EMV AID result is " + result);
            if (!result) {
                return false;
            }
        } else {
            LogUtils.d(TAG, "EMV AID parameters are not present");
        }

        //EMV Terminal
        List<EmvTerminal> terminalList = ConfigUtils.getInstance().convert(EmvTerminal.class);
        if (!terminalList.isEmpty()) {
            result = EmvTerminal.deleteAllRecords();
            LogUtils.d(TAG, "Delete EMV Terminal records result is " + result);
            result = EmvTerminal.load(terminalList);
            LogUtils.d(TAG, "Load EMV Terminal result is " + result);
            if (!result) {
                return false;
            }
        } else {
            LogUtils.d(TAG, "EMV Terminal parameters are not present");
        }

        LogUtils.d(TAG, "loadEmvParameters END");
        return true;
    }

    public boolean loadNonEmvParameters() {
        AcqManager acqManager = FinancialApplication.getAcqManager();
        boolean result;

        List<Acquirer> acquirers = ConfigUtils.getInstance().convert(Acquirer.class);
        List<Issuer> issuers = ConfigUtils.getInstance().convert(Issuer.class);
        List<CardRange> cardRanges = ConfigUtils.getInstance().convert(CardRange.class);

        if (acquirers.isEmpty() && issuers.isEmpty() && cardRanges.isEmpty()) {
            LogUtils.d(TAG, "Non EMV parameters are not present.");
            return true;
        }

        if (acquirers.isEmpty()) {
            LogUtils.e(TAG, "Acquirer ERROR!");
            return false;
        } else if (issuers.isEmpty()) {
            LogUtils.e(TAG, "Issuer ERROR!");
            return false;
        } else if (cardRanges.isEmpty()) {
            LogUtils.e(TAG, "Card range ERROR!");
            return false;
        }

        //Drop all data
        result = acqManager.deleteAllAcqIssuerRelation();
        LogUtils.d(TAG, "Delete all Acquirer Issuer relations records result is " + result);
        result = acqManager.deleteAllAcquirer();
        LogUtils.d(TAG, "Delete all Acquirer records result is " + result);
        result = acqManager.deleteAllCardRange();
        LogUtils.d(TAG, "Delete all CardRange records result is " + result);
        result = acqManager.deleteAllIssuer();
        LogUtils.d(TAG, "Delete all Issuer records result is " + result);

        result = acqManager.insertAcquirer(acquirers);
        LogUtils.d(TAG, "Load Acquirer result is " + result);
        if (!result) {
            return false;
        }

        SysParam.getInstance().set(R.string.ACQ_NAME, acquirers.get(0).getName());
        acqManager.setCurAcq(acquirers.get(0));

        result = acqManager.insertIssuer(issuers);
        LogUtils.d(TAG, "Load Issuer result is " + result);
        if (!result) {
            return false;
        }

        List<AcqIssuerRelation> acqIssuerRelationList = new ArrayList<>();
        for (Issuer issuer : issuers) {
            for (Acquirer acquirer : acquirers) {
                if (acquirer.getName().equals(issuer.getBindToAcquirer())) {
                    acqIssuerRelationList.add(new AcqIssuerRelation(acquirer, issuer));
                }
            }
            for (CardRange cardRange : cardRanges) {
                if (cardRange.getName().equals(issuer.getName())) {
                    cardRange.setIssuer(issuer);
                }
            }
        }

        result = acqManager.insertCardRange(cardRanges);
        LogUtils.d(TAG, "Load CardRange result is " + result);
        if (!result) {
            return false;
        }

        result = acqManager.insertAcqIssuerRelation(acqIssuerRelationList);
        LogUtils.d(TAG, "Load AcquirerIssuerRelation result is " + result);
        return result;
    }

    private boolean deleteFile(String path) {
        return new File(path).delete();
    }

    private boolean unzip(File zipFile, File targetDirectory) {
        LogUtils.i(TAG, "unzip START");

        boolean result = true;
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                if(file.getName().charAt(0) == '.') { //Unix hidden files workaround
                    continue;
                }
                File dir = ze.isDirectory() ? file : file.getParentFile();

                if (!dir.isDirectory() && !dir.mkdirs()) {
                    LogUtils.e(TAG, String.format("%s %s", "Error - DIR: ", dir.getAbsolutePath()));
                    result = false;
                    break;
                }

                if (ze.isDirectory()) {
                    continue;
                }

                FileOutputStream out = new FileOutputStream(file);
                int count;
                byte[] buffer = new byte[8192];
                while ((count = zis.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }
                out.close();
            }
        } catch (IOException e) {
            LogUtils.e(TAG, e);
            result = false;
        }

        LogUtils.i(TAG, "unzip END");
        return result;
    }
}
