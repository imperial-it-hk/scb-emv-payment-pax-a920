package com.evp.pay.trans.transmit;

import android.text.format.DateFormat;
import android.util.Base64;

import com.evp.bizlib.AppConstants;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.bizlib.ped.PedHelper;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.KeyUtils;
import com.evp.commonlib.utils.LogUtils;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.trans.component.Component;
import com.evp.pay.trans.model.AcqManager;
import com.evp.pay.utils.AES;
import com.evp.pay.utils.Promptpay;
import com.evp.pay.utils.RSA;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.poslib.gl.comm.ICommHelper;
import com.evp.poslib.gl.impl.GL;
import com.pax.gl.commhelper.IHttpsClient;
import com.pax.gl.commhelper.ISslKeyStore;
import com.pax.gl.commhelper.exception.CommException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class Digio {
    private static final String TAG = "Digio";

    private static String API_URL = "https://smartedcgw-sit.se.scb.co.th:443/jsongateway";
    private static String ENCRYPTION_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApGoLMZiQHz4g4fucb1sPrR5z14pokIlSkbUzmnBGoETnJcrSTENwjIYfJ8kgz25CVAHknKiPDMsRLJrk/tIPzoA6n0Hzk47a4vk5h2AKtTd/66xZxJbjIp1v4Yjr5Nre0FSBDH2dv/EEAF+SUV5ul4PZmvvM7PLdlfUvgro17iPqFOoO1d++nt58DwKLoIXeZHVrvw3rheMxwsGQveoBroDq0ktQD26hCWexksa5FgTDc8s/TfmsqjlWCwwekYjlMrDheRFe8ylBeWIYzKqum6u4Bx/rfB7Ud3ZwUahCmy/WF6Uz2X3PHAz2GLApHBkyVb1tpLhdTcMoyX3UUgDP7wIDAQAB";
    private static int HOST_TIMEOUT = 60;

    private ICommHelper commHelper = GL.getGL().getCommHelper();
    private ISslKeyStore keyStore = commHelper.createSslKeyStore();
    private IHttpsClient httpsClient = commHelper.createHttpsClient(keyStore);

    private TransProcessListener listener;

    public Digio() {
        Acquirer acquirer = FinancialApplication.getAcqManager().findAcquirer(AppConstants.QR_ACQUIRER);
        API_URL = acquirer.getApiUrl();
        ENCRYPTION_KEY = acquirer.getApiPublicKey();
        HOST_TIMEOUT = acquirer.getHostTimeout();

        httpsClient.setConnTimeout(HOST_TIMEOUT * 1000);
        httpsClient.setSoTimeout(HOST_TIMEOUT * 1000);

        httpsClient.setHeaders(new HashMap<String, String>() {
            {
                put("Content-Type", "application/json");
            }
        });
    }

    public ArrayList<Object> digio(TransData transData, TransProcessListener listener) {
        this.listener = listener;

        Acquirer acquirer = transData.getAcquirer();
        ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());

        ArrayList<Object> result;
        int ret = 0;
        Object data = null;

        //Mark this transaction as online
        transData.setOnlineTrans(true);

        switch (transType) {
            case TMK_DOWNLOAD:
                byte[] sessionKeyBytes = Digio.generateSessionKey(32);

                ret = loadTmk(transData, sessionKeyBytes);
                if (ret != TransResult.SUCC) {
                    return new ArrayList<Object>(Arrays.asList(ret, null));
                }
                break;

            case TWK_DOWNLOAD:
                ret = loadTwk(transData);
                if (ret != TransResult.SUCC) {
                    return new ArrayList<Object>(Arrays.asList(ret, null));
                }
                break;

            case QR_REGISTER_TAG30:
                transData.setFundingSource("promptpay");
                result = register(transData);
                ret = (int) result.get(0);
                if (ret != TransResult.SUCC) {
                    return new ArrayList<Object>(Arrays.asList(ret, null));
                }
                break;

            case QR_REGISTER_QRCS:
                transData.setFundingSource("qrcs");
                result = register(transData);
                ret = (int) result.get(0);
                if (ret != TransResult.SUCC) {
                    return new ArrayList<Object>(Arrays.asList(ret, null));
                }
                break;

            case SALE:
                ArrayList<Object> saleResult;
                if (transData.getIsBSC()) {
                    saleResult = saleBSC(transData);
                } else {
                    saleResult = saleCSB(transData);
                }
                ret = (int) saleResult.get(0);

                if (ret != TransResult.SUCC) {
                    return saleResult;
                }

                data = saleResult.get(1);
                break;

            case VOID:
                ArrayList<Object> voidResult = saleVoid(transData);
                ret = (int) voidResult.get(0);

                if (ret != TransResult.SUCC) {
                    return voidResult;
                }
                break;

            case REFUND:
                ArrayList<Object> refundResult = refund(transData);
                ret = (int) refundResult.get(0);

                if (ret != TransResult.SUCC) {
                    return refundResult;
                }
                break;

            case QR_INQUIRY:
                ArrayList<Object> inquiryResult = inquiry(transData);
                ret = (int) inquiryResult.get(0);

                data = inquiryResult.get(1);

                if (ret != TransResult.SUCC) {
                    return inquiryResult;
                }
                break;

            case QR_CANCEL:
                ArrayList<Object> cancelResult = cancel(transData);
                ret = (int) cancelResult.get(0);

                if (ret != TransResult.SUCC) {
                    return cancelResult;
                }
                break;
            case PULL_SLIP:
                ArrayList<Object> pullSlipResult = pullSlip(transData);
                ret = (int) pullSlipResult.get(0);

                if (ret != TransResult.SUCC) {
                    return pullSlipResult;
                }
                break;
            case SETTLE:
            case SETTLE_END:
                // do nothing
                ret = TransResult.SUCC;
                break;

            default:
                if (listener != null) {
                    listener.onShowErrMessage(Utils.getString(R.string.err_unsupported_trans), Constants.FAILED_DIALOG_SHOW_TIME, false);
                }
                return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));

        }

        //Increase STAN number
        Component.incStanNo();

        increaseTraceNo(transData);

        return new ArrayList<Object>(Arrays.asList(TransResult.SUCC, data));
    }

    private void increaseTraceNo(TransData transData) {
        ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
        if (transData.getReversalStatus() != TransData.ReversalStatus.REVERSAL
                && transType != ETransType.SALE
                && transType != ETransType.VOID
                && transType != ETransType.REFUND
                && transType != ETransType.QR_INQUIRY
                && transType != ETransType.QR_CANCEL
                && transType != ETransType.TWK_DOWNLOAD
                && transType != ETransType.TMK_DOWNLOAD
                && transType != ETransType.PULL_SLIP) {
            Component.incTransNo();
        }
    }

    private int checkRspCode(String responseCode, String message) {
        if (responseCode != null && !"000".equals(responseCode)) {
            if (listener != null) {
                listener.onHideProgress();
                listener.onShowErrMessage(message,
                        Constants.FAILED_DIALOG_SHOW_TIME, false);
            }
            return TransResult.ERR_HOST_REJECT;
        }
        return TransResult.SUCC;
    }

    private int checkEncrpytedJsonRspCode(String responseCode, String message) {
        if (responseCode != null && !"0000".equals(responseCode)) {
            if (listener != null) {
                listener.onHideProgress();
                listener.onShowErrMessage("Code: " + responseCode + " " + message,
                        Constants.FAILED_DIALOG_SHOW_TIME, false);
            }
            return TransResult.ERR_HOST_REJECT;
        }
        return TransResult.SUCC;
    }

    public static byte[] generateSessionKey(int sessionKeyLength) {
        final String VALID_SESSION_CHAR = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz+-()=@*";

        Random Random = new Random();
        StringBuilder sessionKey = new StringBuilder();
        for (int i = 0; i < sessionKeyLength; i++) {
            sessionKey.append(VALID_SESSION_CHAR.charAt(Random.nextInt(VALID_SESSION_CHAR.length())));
        }
        return sessionKey.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String getMinOfYear() {
        LocalDateTime ld = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            ld = LocalDateTime.now();

            int dayOfYear = ld.getDayOfYear();
            int minOfYear = (dayOfYear - 1) * 1440 + ld.getHour() * 60 + ld.getMinute();
            return ConvertUtils.getPaddedNumber(minOfYear, 6);
        }
        return "";
    }

    public int loadTmk(TransData transData, byte[] sessionKeyBytes) {
        int ret = TransResult.ERR_RECV;

        HttpResponse response = null;
        try {
            Acquirer acquirer = AcqManager.getInstance().findAcquirer(AppConstants.QR_ACQUIRER);
            String venderId = "EVP";
            String terminalId = acquirer.getTerminalId();

            LogUtils.d(TAG, "sessionKey: " + new String(Base64.encode(sessionKeyBytes, Base64.NO_WRAP), "UTF-8"));

            PublicKey publicKey = RSA.getPublicKey(Base64.decode(ENCRYPTION_KEY, Base64.DEFAULT));
            byte[] encryptedSessionKeyBytes = RSA.encrypt(sessionKeyBytes, publicKey);
            JSONObject requestBody = new JSONObject();
            requestBody.put("vendor_id", venderId);
            requestBody.put("tid", terminalId);
            requestBody.put("data", new String(Base64.encode(encryptedSessionKeyBytes, Base64.NO_WRAP), "UTF-8"));

            LogUtils.d(TAG, "loadTMK - request: " + requestBody.toString());
            response = httpsClient.post(API_URL + "/keySign/loadTmk", requestBody.toString().getBytes());

            LogUtils.d(TAG, "loadTMK - response status: " + response.getStatusLine().getStatusCode());
            int responseStatusCode = response.getStatusLine().getStatusCode();
            if (responseStatusCode != 200) {
                return TransResult.ERR_CONNECT;
            }

            LogUtils.d(TAG, "loadTMK - parsing TMK response...");

            HttpEntity tmkResponseEntity = response.getEntity();
            String responseText = EntityUtils.toString(tmkResponseEntity, "UTF-8");

            JSONObject responseJson = new JSONObject(responseText);
            String code = responseJson.getString("code");
            String message = responseJson.getString("message");
            String data = responseJson.getString("data");

            LogUtils.d(TAG, "loadTMK - TMK response: " + code + ", " + message);

            transData.setResponseCode(code);

            ret = checkRspCode(code, message);
            if (ret != TransResult.SUCC) {
                return TransResult.ERR_RECV;
            }
            JSONObject responseJsonData = new JSONObject(data);
            String encryptedTmkKey = responseJsonData.getString("key");
            String tmkKeyIndex = responseJsonData.getString("key_index");
            String kcv = responseJsonData.getString("kcv");

            SecretKeySpec secretSessionKey = new SecretKeySpec(sessionKeyBytes, "AES");
            byte[] decryptedTmkBytes = AES.decrypt(Base64.decode(encryptedTmkKey, Base64.DEFAULT), secretSessionKey);
            LogUtils.d(TAG, "TMK: " + new String(Base64.encode(decryptedTmkBytes, Base64.NO_WRAP), "UTF-8"));

            transData.setTmkKey(decryptedTmkBytes);
            transData.setTmkKeyIndex(tmkKeyIndex);
            transData.setTmkKcv(kcv);

            return TransResult.SUCC;
        } catch (JSONException | NoSuchAlgorithmException | BadPaddingException | InvalidAlgorithmParameterException | CommException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | IOException e) {
            e.printStackTrace();
            return TransResult.ERR_RECV;
        }
    }

    public int loadTwk(TransData transData) {
        int ret = TransResult.ERR_RECV;

        String setId = transData.getAcquirer().getTleKeySetId();

        HttpResponse response = null;
        try {
            Acquirer acquirer = AcqManager.getInstance().findAcquirer(AppConstants.QR_ACQUIRER);
            String venderId = "EVP";
            String terminalId = acquirer.getTerminalId();

            JSONObject requestBody = new JSONObject();
            requestBody.put("vendor_id", venderId);
            requestBody.put("tid", terminalId);
            requestBody.put("key_index", acquirer.getDigioTmkKeyIndex());

            LogUtils.d(TAG, "loadTWK - request: " + requestBody.toString());
            response = httpsClient.post(API_URL + "/keySign/loadTwk", requestBody.toString().getBytes());

            LogUtils.d(TAG, "loadTWK - response status: " + response.getStatusLine().getStatusCode());
            int responseStatusCode = response.getStatusLine().getStatusCode();
            if (responseStatusCode != 200) {
                return TransResult.ERR_CONNECT;
            }

            HttpEntity tmkResponseEntity = response.getEntity();
            String responseText = EntityUtils.toString(tmkResponseEntity, "UTF-8");

            JSONObject responseJson = new JSONObject(responseText);
            String code = responseJson.getString("code");
            String message = responseJson.getString("message");
            String data = responseJson.getString("data");

            transData.setResponseCode(code);

            ret = checkRspCode(code, message);
            if (ret != TransResult.SUCC) {
                return TransResult.ERR_RECV;
            }

            JSONObject responseJsonData = new JSONObject(data);
            String encryptedTwkKey = responseJsonData.getString("key");
            String twkKeyIndex = responseJsonData.getString("key_index");
            String kcv = responseJsonData.getString("kcv");

            byte[] decryptedTwkKey = PedHelper.decrAesCbc(KeyUtils.getTaeskIndex(setId), Base64.decode(encryptedTwkKey, Base64.DEFAULT), new byte[16]);
            LogUtils.d(TAG, "TWK: " + new String(Base64.encode(decryptedTwkKey, Base64.NO_WRAP), "UTF-8"));

            transData.setDekKey(decryptedTwkKey);
            transData.setDekKeyIndex(twkKeyIndex);
            transData.setDekKcv(kcv);

            return TransResult.SUCC;
        } catch (JSONException | CommException | IOException e) {
            e.printStackTrace();
            return TransResult.ERR_RECV;
        }
    }

    public ArrayList<Object> encryptedJson(TransData transData, String service, String action, JSONObject transactionData) {
        String setId = transData.getAcquirer().getTleKeySetId();

        HttpResponse response = null;
        try {
            Acquirer acquirer = AcqManager.getInstance().findAcquirer(AppConstants.QR_ACQUIRER);
            String venderId = "EVP";
            String terminalId = acquirer.getTerminalId();
            JSONObject requestBody = new JSONObject();
            requestBody.put("vendor_id", venderId);
            requestBody.put("tid", terminalId);
            requestBody.put("key_index", acquirer.getDigioTwkKeyIndex());

            JSONObject transaction = new JSONObject();
            transaction.put("service", service);
            transaction.put("action", action);
            transaction.put("vendor_id", venderId);
            transaction.put("sub_vendor_id", "DIGIO");
            transaction.put("api_version", "1.0");
            transaction.put("client_serial_no", "A11000000");
            transaction.put("client_type", "edc");
            transaction.put("data", transactionData);

            final String msg = transaction.toString();
            LogUtils.i(TAG, String.format("%s %s", "Sending message:", msg));

            byte[] encryptedRequest = PedHelper.encAesCbc(KeyUtils.getTsaeskIndex(setId), AES.zeroPadding(msg.getBytes()), new byte[16]);
            requestBody.put("data", new String(Base64.encode(encryptedRequest, Base64.NO_WRAP), "UTF-8"));

            LogUtils.d(TAG, "encryptedJson - request: " + requestBody.toString());
            LogUtils.d(TAG, "encryptedJson - request transaction: " + transaction.toString());

            response = httpsClient.post(API_URL + "/gateway/encryptedJson", requestBody.toString().getBytes());

            LogUtils.d(TAG, "encryptedJson - response status: " + response.getStatusLine().getStatusCode());
            int responseStatusCode = response.getStatusLine().getStatusCode();
            if (responseStatusCode != 200) {
                return null;
            }

            HttpEntity tmkResponseEntity = response.getEntity();
            String responseText = EntityUtils.toString(tmkResponseEntity, "UTF-8");
            LogUtils.d(TAG, "encryptedJson - response body: " + responseText);

            JSONObject responseJson = new JSONObject(responseText);

            // decrypted by chunks (every 1024 bytes)
            byte[] encryptedData = Base64.decode(responseJson.getString("data"), Base64.DEFAULT);

            ByteArrayOutputStream decryptedDataStream = new ByteArrayOutputStream();
            int byteIndex = 0;
            while (byteIndex < encryptedData.length - 1) {
                int byteEndIndex = byteIndex + 1024;
                if (byteEndIndex > encryptedData.length) {
                    byteEndIndex = encryptedData.length;
                }

                byte[] iv = new byte[16];
                if (byteIndex > 0) {
                    iv = Arrays.copyOfRange(encryptedData, byteIndex - 16, byteIndex);
                }

                byte[] decryptedDataChunk = PedHelper.decrAesCbc(KeyUtils.getTsaeskIndex(setId), Arrays.copyOfRange(encryptedData, byteIndex, byteEndIndex), iv);
                decryptedDataStream.write(decryptedDataChunk, 0, decryptedDataChunk.length);

                byteIndex = byteEndIndex;
            }
            byte[] decryptedData = AES.clean(decryptedDataStream.toByteArray());
            String decryptedDataText = new String(decryptedData, "UTF-8");
            LogUtils.i(TAG, String.format("%s %s", "Received message:", decryptedDataText));

            JSONObject responseJsonData = new JSONObject(decryptedDataText);
            String code = responseJsonData.getString("code");
            String message = responseJsonData.optString("message");

            transData.setResponseCode(code);

            JSONObject dataObject = responseJsonData.optJSONObject("data");
            JSONArray dataArray = responseJsonData.optJSONArray("data");

            String responseLogToPrint = responseJsonData.toString();
            if (service == "qrcs" && action == "generate") {
                responseLogToPrint = responseLogToPrint.replaceAll("\"qrCode\":\"[^\"]+\"", "\"qrCode\":\"...\"");
            }
            LogUtils.d(TAG, "encryptedJson - response body transaction: " + responseLogToPrint);

            return new ArrayList<Object>(Arrays.asList(code, message, dataObject, dataArray, decryptedDataText));
        } catch (JSONException | CommException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * mainly for TAG30 & QRCS
     */
    public ArrayList<Object> register(TransData transData) {
        int ret = 0;

        try {
            if (Component.isDemo()) {
                LogUtils.d(TAG, "Demo Mode");
                return new ArrayList<Object>(Arrays.asList(TransResult.SUCC, null));
            }

            Acquirer acquirer = AcqManager.getInstance().findAcquirer(AppConstants.QR_ACQUIRER);
            String fundingSource = transData.getFundingSource();

            JSONObject transactionData = new JSONObject();
            String service = "";
            String action = "";

            switch (fundingSource) {
                case "promptpay":
                case "qrcs":
                    if ("promptpay".equals(fundingSource)) {
                        service = "tag30";
                    } else {
                        service = "qrcs";
                    }
                    action = "register";

                    transactionData.put("terminalType", "32");      // SDO + EDC
                    if ("promptpay".equals(fundingSource)) {
                        transactionData.put("mid", acquirer.getTag30MerchantId());
                        transactionData.put("tid", acquirer.getTag30TerminalId());
                        transactionData.put("partnerCode", acquirer.getTag30PartnerCode());
                    } else {
                        transactionData.put("mid", acquirer.getQrcsMerchantId());
                        transactionData.put("tid", acquirer.getQrcsTerminalId());
                        transactionData.put("partnerCode", acquirer.getQrcsPartnerCode());
                    }
                    break;
                case "alipay":
                case "wechat":
                case "linepay":
                case "true_money":
                case "shopee":
                case "dolfin":
                default:
                    return new ArrayList<Object>(Arrays.asList(TransResult.SUCC, null));
            }

            ArrayList<Object> responseResult = encryptedJson(transData, service, action, transactionData);
            if (responseResult == null) {
                if (listener != null) {
                    listener.onShowErrMessage(Utils.getString(R.string.err_connect), Constants.FAILED_DIALOG_SHOW_TIME, false);
                }
                return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
            }

            String code = (String) responseResult.get(0);
            String message = (String) responseResult.get(1);

            switch (code) {
                case "0000":
                    return new ArrayList<Object>(Arrays.asList(TransResult.SUCC, null));
                case "0502":            // MID/TID is already registered
                default:
                    if (listener != null) {
                        listener.onHideProgress();
                        listener.onShowErrMessage(message,
                                Constants.FAILED_DIALOG_SHOW_TIME, false);
                    }

                    return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
        }
    }

    public ArrayList<Object> saleBSC(TransData transData) {
        int ret = 0;

        try {
            if (Component.isDemo()) {
                LogUtils.d(TAG, "Demo Mode");
                transData.setAmountCNY(transData.getAmount());
                transData.setExchangeRate("0.19");
                transData.setPaymentId("412615100000052021130074940");
                transData.setBillerId("EVP");
                Acquirer acquirer = AcqManager.getInstance().findAcquirer(AppConstants.QR_ACQUIRER);
                if (acquirer != null) {
                    transData.setBillPaymentRef1(acquirer.getTag30TerminalId() + getMinOfYear() + ConvertUtils.getPaddedNumber(transData.getTraceNo(), 6));
                } else {
                    transData.setBillPaymentRef1("asd8451as2d54a2as23das12");
                }
                transData.setQrCodeId("asda32d1");
                transData.setRefNo("13212a1sd2132as1das1d31a5");
                transData.setAuthCode("000");
                transData.setTransactionId("5432215678951212312");
                transData.setMerchantPan("1234xxxxxxxx1234");
                transData.setConsumerPan("1234154212231234");
                transData.setCurrencyCode("THB");
                transData.setPaymentChannel("VISA");
                return new ArrayList<Object>(Arrays.asList(TransResult.SUCC, null));
            }

            Acquirer acquirer = AcqManager.getInstance().findAcquirer(AppConstants.QR_ACQUIRER);
            String fundingSource = transData.getFundingSource();
            String qrCode = transData.getQrCode();

            JSONObject transactionData = new JSONObject();
            String service = "";
            String action = "";

            ArrayList<Object> responseData = null;

            switch (fundingSource) {
                case "alipay":
                    service = "alipay";
                    action = "sale";

                    transactionData.put("terminal_id", acquirer.getAlipayTerminalId());
                    transactionData.put("merchant_id", acquirer.getAlipayMerchantId());
                    transactionData.put("amount", transData.getAmount());
                    transactionData.put("funding_source", fundingSource.toUpperCase());
                    transactionData.put("currency", "THB");
                    transactionData.put("partner_transaction_id", transData.getRefNo());
                    transactionData.put("acquirer", acquirer.getAlipayAcquirer());
                    transactionData.put("auth_code", qrCode);
                    break;
                case "wechat":
                    service = "wechat";
                    action = "sale";

                    transactionData.put("terminal_id", acquirer.getWechatTerminalId());
                    transactionData.put("merchant_id", acquirer.getWechatMerchantId());
                    transactionData.put("amount", transData.getAmount());
                    transactionData.put("funding_source", fundingSource.toUpperCase());
                    transactionData.put("currency", "THB");
                    transactionData.put("partner_transaction_id", transData.getRefNo());
                    transactionData.put("acquirer", acquirer.getWechatAcquirer());
                    transactionData.put("auth_code", qrCode);
                    break;
                case "promptpay":
                    service = "tag30";
                    action = "sale";

                    String billPaymentRef1 = acquirer.getTag30TerminalId() + getMinOfYear() + ConvertUtils.getPaddedNumber(transData.getTraceNo(), 6);
                    String billPaymentRef3 = "EVO" + acquirer.getTag30TerminalId();
                    String billerId = acquirer.getTag30BillerId();

                    transData.setBillPaymentRef1(billPaymentRef1);
                    transData.setBillPaymentRef3(billPaymentRef3);
                    transData.setBillerId(billerId);

                    transactionData.put("rowQRCode", qrCode);
                    transactionData.put("mid", acquirer.getTag30MerchantId());
                    transactionData.put("tid", acquirer.getTag30TerminalId());
                    transactionData.put("billerId", billerId);
                    transactionData.put("amount", String.format("%.2f", (float) ConvertUtils.parseLongSafe(transData.getAmount(), -1) / 100));
                    transactionData.put("ref1", billPaymentRef1);
                    transactionData.put("ref2", acquirer.getTag30Ref2());
                    transactionData.put("ref3", billPaymentRef3);
                    transactionData.put("currency", "THB");
                    transactionData.put("transactionId", transData.getRefNo());
                    break;
                case "linepay":
                case "true_money":
                case "shopee":
                case "dolfin":
                    break;
                default:
                    if (listener != null) {
                        listener.onShowErrMessage(Utils.getString(R.string.err_unsupported_trans), Constants.FAILED_DIALOG_SHOW_TIME, false);
                    }
                    return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
            }

            ArrayList<Object> responseResult = encryptedJson(transData, service, action, transactionData);
            if (responseResult == null) {
                if (listener != null) {
                    listener.onShowErrMessage(Utils.getString(R.string.err_connect), Constants.FAILED_DIALOG_SHOW_TIME, false);
                }
                return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
            }

            String code = (String) responseResult.get(0);
            String message = (String) responseResult.get(1);
            JSONObject responseJSON = (JSONObject) responseResult.get(2);

            parseTransData(transData, responseJSON);

            // silent register for Tag30 & QRCS
            switch (fundingSource) {
                case "promptpay":
                    if (code.equals("0502")) {           // MID/TID is already registered
                        int registerRet = new TransDigio().registerTag30(listener);
                        if (registerRet != TransResult.SUCC) {
                            return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
                        }

                        // retry request
                        responseResult = encryptedJson(transData, service, action, transactionData);
                        if (responseResult == null) {
                            if (listener != null) {
                                listener.onShowErrMessage(Utils.getString(R.string.err_connect), Constants.FAILED_DIALOG_SHOW_TIME, false);
                            }
                            return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
                        }

                        code = (String) responseResult.get(0);
                        message = (String) responseResult.get(1);
                        responseJSON = (JSONObject) responseResult.get(2);
                    }
                    break;
                case "qrcs":
                    if (code.equals("0502")) {           // MID/TID is already registered
                        int registerRet = new TransDigio().registerQrcs(listener);
                        if (registerRet != TransResult.SUCC) {
                            return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
                        }

                        // retry request
                        responseResult = encryptedJson(transData, service, action, transactionData);
                        if (responseResult == null) {
                            if (listener != null) {
                                listener.onShowErrMessage(Utils.getString(R.string.err_connect), Constants.FAILED_DIALOG_SHOW_TIME, false);
                            }
                            return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
                        }

                        code = (String) responseResult.get(0);
                        message = (String) responseResult.get(1);
                        responseJSON = (JSONObject) responseResult.get(2);
                    }
                    break;
            }

            if (checkEncrpytedJsonRspCode(code, message) != TransResult.SUCC) {
                return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
            }

            switch (fundingSource) {
                case "alipay":
                case "wechat":
                    transData.setAmountCNY(responseJSON.optString("amount_cny"));
                    transData.setExchangeRate(responseJSON.optString("exchange_rate"));

                    transData.setPaymentId(responseJSON.optString("payment_id"));
                    transData.setCurrencyCode(responseJSON.optString("currency"));
                    break;
                case "promptpay":
                    String billPaymentRef1 = responseJSON.optString("billPaymentRef1");
                    String billPaymentRef2 = responseJSON.optString("billPaymentRef2");
                    String billPaymentRef3 = responseJSON.optString("billPaymentRef3");

                    transData.setCurrencyCode(responseJSON.optString("currency"));
                    transData.setPayeeProxyId(responseJSON.optString("payeeProxyId"));
                    transData.setPayeeProxyType(responseJSON.optString("payeeProxyType"));
                    transData.setPayeeAccountNumber(responseJSON.optString("payeeAccountNumber"));
                    transData.setPayerProxyId(responseJSON.optString("payerProxyId"));
                    transData.setPayerProxyType(responseJSON.optString("payerProxyType"));
                    transData.setPayerAccountNumber(responseJSON.optString("payerAccountNumber"));
                    transData.setReceivingBankCode(responseJSON.optString("receivingBankCode"));
                    transData.setThaiQRTag(responseJSON.optString("thaiQRTag"));
                    transData.setIsPullSlip(responseJSON.optInt("isPullSlip") == 1);

                    responseData = new ArrayList<Object>(Arrays.asList(
                            billPaymentRef1,
                            billPaymentRef2,
                            billPaymentRef3
                    ));
                    break;
                case "linepay":
                case "true_money":
                case "shopee":
                case "dolfin":
                    break;
            }

            return new ArrayList<Object>(Arrays.asList(TransResult.SUCC, responseData));
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
        }
    }

    public ArrayList<Object> saleCSB(TransData transData) {
        int ret = 0;

        try {
            transData.setTransType(ETransType.SALE.toString());
            if (Component.isDemo()) {
                LogUtils.d(TAG, "Demo Mode");
                transData.setAmountCNY(transData.getAmount());
                transData.setExchangeRate("0.19");
                transData.setPaymentId("412615100000052021130074940");
                transData.setBillerId("EVP");
                Acquirer acquirer = AcqManager.getInstance().findAcquirer(AppConstants.QR_ACQUIRER);
                if (acquirer != null) {
                    transData.setBillPaymentRef1(acquirer.getTag30TerminalId() + getMinOfYear() + ConvertUtils.getPaddedNumber(transData.getTraceNo(), 6));
                } else {
                    transData.setBillPaymentRef1("asd8451as2d54a2as23das12");
                }
                transData.setQrCodeId("asda32d1");
                transData.setRefNo("13212a1sd2132as1das1d31a5");
                transData.setAuthCode("000");
                transData.setTransactionId("5432215678951212312");
                transData.setMerchantPan("1234xxxxxxxx1234");
                transData.setConsumerPan("1234154212231234");
                transData.setCurrencyCode("THB");
                transData.setPaymentChannel("VISA");
                return new ArrayList<Object>(Arrays.asList(TransResult.SUCC, "testing"));
            }

            Acquirer acquirer = AcqManager.getInstance().findAcquirer(AppConstants.QR_ACQUIRER);
            String fundingSource = transData.getFundingSource();

            String qrCode = "";

            // promptpay generate offline
            if ("promptpay".equals(fundingSource)) {
                String traceNo = ConvertUtils.getPaddedNumber(transData.getTraceNo(), 6);
                String amount = String.format("%.2f", (float) ConvertUtils.parseLongSafe(transData.getAmount(), -1) / 100);
                String merchantName = acquirer.getTag30MerchantName();
                String billerId = acquirer.getTag30BillerId();

                String ref1 = acquirer.getTag30TerminalId() + getMinOfYear() + traceNo;

                transData.setBillPaymentRef1(ref1);
                transData.setBillPaymentRef2(acquirer.getTag30Ref2());
                transData.setBillPaymentRef3("EVO" + acquirer.getTag30TerminalId());
                transData.setBillerId(billerId);

                qrCode = Promptpay.generate("A000000677010112", billerId, acquirer.getTag30TerminalId(), traceNo, amount, merchantName, ref1);
                transData.setQrCode(qrCode);

            } else {
                JSONObject transactionData = new JSONObject();
                String service = "";
                String action = "";

                switch (fundingSource) {
                    case "alipay":
                        service = "alipay";
                        action = "order";

                        transactionData.put("terminal_id", acquirer.getAlipayTerminalId());
                        transactionData.put("merchant_id", acquirer.getAlipayMerchantId());
                        transactionData.put("amount", transData.getAmount());
                        transactionData.put("funding_source", fundingSource.toUpperCase());
                        transactionData.put("currency", "THB");
                        transactionData.put("partner_transaction_id", transData.getRefNo());
                        transactionData.put("acquirer", acquirer.getAlipayAcquirer());
                        break;
                    case "wechat":
                        service = "wechat";
                        action = "order";

                        transactionData.put("terminal_id", acquirer.getWechatTerminalId());
                        transactionData.put("merchant_id", acquirer.getWechatMerchantId());
                        transactionData.put("amount", transData.getAmount());
                        transactionData.put("funding_source", fundingSource.toUpperCase());
                        transactionData.put("currency", "THB");
                        transactionData.put("partner_transaction_id", transData.getRefNo());
                        transactionData.put("acquirer", acquirer.getWechatAcquirer());
                        break;
                    case "linepay":
                    case "true_money":
                    case "shopee":
                    case "dolfin":
                        break;
                    case "qrcs":
                        service = "qrcs";
                        action = "generate";

                        String amount = String.format("%.2f", (float) ConvertUtils.parseLongSafe(transData.getAmount(), -1) / 100);

                        DateFormat df = new DateFormat();
                        String invoice = ConvertUtils.getPaddedNumber(transData.getTraceNo(), 6) + df.format("yyMMddhhmmss", new Date()).toString();
                        // invoice = traceNo

                        transactionData.put("mid", acquirer.getQrcsMerchantId());
                        transactionData.put("tid", acquirer.getQrcsTerminalId());
                        transactionData.put("amount", amount);
                        transactionData.put("invoice", invoice);
                        break;
                    default:
                        if (listener != null) {
                            listener.onShowErrMessage(Utils.getString(R.string.err_unsupported_trans), Constants.FAILED_DIALOG_SHOW_TIME, false);
                        }
                        return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
                }

                ArrayList<Object> responseResult = encryptedJson(transData, service, action, transactionData);
                if (responseResult == null) {
                    if (listener != null) {
                        listener.onShowErrMessage(Utils.getString(R.string.err_connect), Constants.FAILED_DIALOG_SHOW_TIME, false);
                    }
                    return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
                }

                String code = (String) responseResult.get(0);
                String message = (String) responseResult.get(1);
                JSONObject responseJSON = (JSONObject) responseResult.get(2);

                parseTransData(transData, responseJSON);

                // silent register for Tag30 & QRCS
                switch (fundingSource) {
                    case "qrcs":
                        if (code.equals("0502")) {           // MID/TID is already registered
                            int registerRet = new TransDigio().registerQrcs(listener);
                            if (registerRet != TransResult.SUCC) {
                                return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
                            }

                            // retry request
                            responseResult = encryptedJson(transData, service, action, transactionData);
                            if (responseResult == null) {
                                if (listener != null) {
                                    listener.onShowErrMessage(Utils.getString(R.string.err_connect), Constants.FAILED_DIALOG_SHOW_TIME, false);
                                }
                                return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
                            }

                            code = (String) responseResult.get(0);
                            message = (String) responseResult.get(1);
                            responseJSON = (JSONObject) responseResult.get(2);
                        }
                        break;
                }

                if (checkEncrpytedJsonRspCode(code, message) != TransResult.SUCC) {
                    return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
                }

                switch (fundingSource) {
                    case "alipay":
                        qrCode = responseJSON.getString("qr_code");
                        transData.setQrCode(qrCode);
                        // others will be updated in inquiry
                        transData.setPaymentId(responseJSON.optString("payment_id"));
                        transData.setCurrencyCode(responseJSON.optString("currency"));
                        break;
                    case "wechat":
                        qrCode = responseJSON.getString("qr_code_url");
                        transData.setQrCode(qrCode);
                        // others will be updated in inquiry
                        transData.setPaymentId(responseJSON.optString("payment_id"));
                        transData.setCurrencyCode(responseJSON.optString("currency"));
                        break;
                    case "linepay":
                    case "true_money":
                    case "shopee":
                    case "dolfin":
                        break;
                    case "qrcs":
                        qrCode = responseJSON.getString("rawQRCode");

                        transData.setQrCode(qrCode);
                        transData.setCurrencyCode(responseJSON.optString("currencyCode"));

                        int qrCodeId = responseJSON.optInt("qrCodeID");
                        transData.setQrCodeId(Integer.toString(qrCodeId));
                        break;
                }

            }

            transData.setReversalStatus(TransData.ReversalStatus.NORMAL);
            transData.setTransState(TransData.ETransStatus.SUSPENDED);
            GreendaoHelper.getTransDataHelper().insert(transData);

            return new ArrayList<Object>(Arrays.asList(TransResult.SUCC, qrCode));
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
        }
    }

    public ArrayList<Object> inquiry(TransData transData) {
        int ret = 0;

        try {
            if (Component.isDemo()) {
                LogUtils.d(TAG, "Demo Mode");
                return new ArrayList<Object>(Arrays.asList(TransResult.SUCC, null));
            }

            Acquirer acquirer = AcqManager.getInstance().findAcquirer(AppConstants.QR_ACQUIRER);
            String fundingSource = transData.getFundingSource();

            JSONObject transactionData = new JSONObject();
            String service = "";
            String action = "";

            switch (fundingSource) {
                case "alipay":
                    service = "alipay";
                    action = "inquiry";

                    transactionData.put("terminal_id", acquirer.getAlipayTerminalId());
                    transactionData.put("merchant_id", acquirer.getAlipayMerchantId());
                    transactionData.put("partner_transaction_id", transData.getRefNo());
                    transactionData.put("funding_source", fundingSource.toUpperCase());
                    transactionData.put("acquirer", acquirer.getAlipayAcquirer());
                    break;
                case "wechat":
                    service = "wechat";
                    action = "inquiry";

                    transactionData.put("terminal_id", acquirer.getWechatTerminalId());
                    transactionData.put("merchant_id", acquirer.getWechatMerchantId());
                    transactionData.put("partner_transaction_id", transData.getRefNo());
                    transactionData.put("funding_source", fundingSource.toUpperCase());
                    transactionData.put("acquirer", acquirer.getWechatAcquirer());
                    break;
                case "promptpay":
                    service = "tag30";
                    action = "inquiry";

                    transactionData.put("mid", acquirer.getTag30MerchantId());
                    transactionData.put("tid", acquirer.getTag30TerminalId());
                    transactionData.put("reference1", transData.getBillPaymentRef1());
                    transactionData.put("reference2", transData.getBillPaymentRef2());
                    transactionData.put("reference3", transData.getBillPaymentRef3());
                    break;
                case "linepay":
                case "true_money":
                case "shopee":
                case "dolfin":
                    break;
                case "qrcs":
                    service = "qrcs";
                    action = "inquiry";

                    transactionData.put("mid", acquirer.getQrcsMerchantId());
                    transactionData.put("tid", acquirer.getQrcsTerminalId());
                    transactionData.put("qrid", transData.getQrCodeId());
                    break;
                default:
                    if (listener != null) {
                        listener.onShowErrMessage(Utils.getString(R.string.err_unsupported_trans), Constants.FAILED_DIALOG_SHOW_TIME, false);
                    }
                    return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
            }

            ArrayList<Object> responseResult = encryptedJson(transData, service, action, transactionData);

            if (responseResult == null) {
                if (listener != null) {
                    listener.onShowErrMessage(Utils.getString(R.string.err_connect), Constants.FAILED_DIALOG_SHOW_TIME, false);
                }
                return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
            }

            String code = (String) responseResult.get(0);
            String message = (String) responseResult.get(1);

            switch (fundingSource) {
                case "promptpay":
                case "qrcs":
                    switch (code) {
                        case "0404":            // Transaction is not found
                        case "0637":            // Transaction reference not found
                            return new ArrayList<Object>(Arrays.asList(TransResult.ERR_TRANS_NOT_FOUND, null));
                    }
                    break;
            }

            if (checkEncrpytedJsonRspCode(code, message) != TransResult.SUCC) {
                return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
            }

            JSONArray responseJSONArray = (JSONArray) responseResult.get(3);
            String transactionType;
            JSONObject responseJSON = (JSONObject) responseResult.get(2);
            if(responseJSON == null) {
                responseJSON = (JSONObject) responseJSONArray.get(0);
            }

            parseTransData(transData, responseJSON);

            switch (fundingSource) {
                case "alipay":
                case "wechat":
                    if ("UNKNOWN".equals(responseJSON.optString("status"))) {
                        return new ArrayList<Object>(Arrays.asList(TransResult.ERR_TRANS_NOT_FOUND, null));
                    }

                    transData.setAmountCNY(responseJSON.optString("amount_cny"));
                    transData.setExchangeRate(responseJSON.optString("exchange_rate"));

                    Double amountCnyFixed = Double.parseDouble(responseJSON.optString("amount_cny")) / 100;
                    Double exchangeRateFixed = Double.parseDouble(responseJSON.optString("exchange_rate")) / 100000000;
                    if ("wechat".equals(fundingSource)) {
                        transData.setAmountCNY(new DecimalFormat("0.00").format(amountCnyFixed));
                        transData.setExchangeRate(new DecimalFormat("0.00000000").format(exchangeRateFixed));
                    }

                    switch (responseJSON.optString("status")) {
                        case "APPROVED":
                            transData.setTransState(TransData.ETransStatus.NORMAL);
                            break;
                        case "VOIDED":
                        case "REVERSED":
                        case "REFUND":
                            transData.setTransState(TransData.ETransStatus.VOIDED);
                            break;
                    }
                    break;
                case "promptpay":
                    responseJSON = (JSONObject) responseJSONArray.get(0);
                    transData.setTransactionId(responseJSON.optString("transactionId"));
                    transData.setBillerId(responseJSON.optString("payeeProxyId"));
                    transData.setSendingBankCode(responseJSON.optString("sendingBankCode"));
                    transData.setTransState(TransData.ETransStatus.NORMAL);
                    transData.setPayeeProxyId(responseJSON.optString("payeeProxyId"));
                    transData.setPayeeProxyType(responseJSON.optString("payeeProxyType"));
                    transData.setPayeeAccountNumber(responseJSON.optString("payeeAccountNumber"));
                    transData.setPayerProxyId(responseJSON.optString("payerProxyId"));
                    transData.setPayerProxyType(responseJSON.optString("payerProxyType"));
                    transData.setPayerAccountNumber(responseJSON.optString("payerAccountNumber"));
                    transData.setReceivingBankCode(responseJSON.optString("receivingBankCode"));
                    transData.setThaiQRTag(responseJSON.optString("thaiQRTag"));
                    transData.setIsPullSlip(responseJSON.optInt("isPullSlip") == 1);
                    break;
                case "linepay":
                case "true_money":
                case "shopee":
                case "dolfin":
                    break;
                case "qrcs":
                    responseJSON = (JSONObject) responseJSONArray.get(0);

                    transData.setTransactionId(responseJSON.optString("transactionId"));
                    transData.setMerchantPan(responseJSON.optString("merchantPAN"));
                    transData.setConsumerPan(responseJSON.optString("consumerPAN"));
                    transData.setPaymentChannel(responseJSON.optString("paymentChannel"));
                    transData.setAuthCode(responseJSON.optString("authorizeCode"));
                    transData.setQrcsTraceNo(responseJSON.optString("traceNo"));

                    transactionType = responseJSON.optString("transactionType");

                    switch (transactionType) {
                        case "APPROVED":
                        case "Settled":
                            transData.setTransState(TransData.ETransStatus.NORMAL);
                            break;
                        case "Refund":
                        case "Refund Settled":
                            transData.setTransState(TransData.ETransStatus.REFUNDED);
                            break;
                        case "Refund Failed":
                        case "Canceled":
                        case "Failed":
                            if (listener != null) {
                                listener.onShowErrMessage(Utils.getString(R.string.err_no_succ_trans), Constants.FAILED_DIALOG_SHOW_TIME, false);
                            }
                            return new ArrayList<Object>(Arrays.asList(TransResult.ERR_NO_TRANS, null));
                    }
                    break;
            }

            if (transData.getId() != null) {
                GreendaoHelper.getTransDataHelper().update(transData);
            }

            return new ArrayList<Object>(Arrays.asList(TransResult.SUCC, transData));
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
        }
    }

    public ArrayList<Object> saleVoid(TransData transData) {
        int ret = 0;

        try {
            if (Component.isDemo()) {
                LogUtils.d(TAG, "Demo Mode");
                return new ArrayList<Object>(Arrays.asList(TransResult.SUCC, null));
            }

            Acquirer acquirer = AcqManager.getInstance().findAcquirer(AppConstants.QR_ACQUIRER);
            String fundingSource = transData.getFundingSource();

            JSONObject transactionData = new JSONObject();
            String service = "";
            String action = "";

            switch (fundingSource) {
                case "alipay":
                    service = "alipay";
                    action = "cancel";

                    transactionData.put("terminal_id", acquirer.getAlipayTerminalId());
                    transactionData.put("merchant_id", acquirer.getAlipayMerchantId());
                    transactionData.put("partner_transaction_id", transData.getOrigRefNo());
                    transactionData.put("funding_source", fundingSource.toUpperCase());
                    transactionData.put("acquirer", acquirer.getAlipayAcquirer());
                    break;
                case "wechat":
                    service = "wechat";
                    action = "cancel";

                    transactionData.put("terminal_id", acquirer.getWechatTerminalId());
                    transactionData.put("merchant_id", acquirer.getWechatMerchantId());
                    transactionData.put("partner_transaction_id", transData.getOrigRefNo());
                    transactionData.put("funding_source", fundingSource.toUpperCase());
                    transactionData.put("acquirer", acquirer.getWechatAcquirer());
                    break;
                case "promptpay":
                    service = "tag30";
                    action = "void";

                    transactionData.put("mid", acquirer.getTag30MerchantId());
                    transactionData.put("tid", acquirer.getTag30TerminalId());
                    transactionData.put("transactionId", transData.getTransactionId());
                    break;
                case "linepay":
                case "true_money":
                case "shopee":
                case "dolfin":
                    break;
                case "qrcs":
                    service = "qrcs";
                    action = "refund";

                    String refundReferenceId = acquirer.getQrcsTerminalId() + transData.getTransactionId();

                    transactionData.put("mid", acquirer.getQrcsMerchantId());
                    transactionData.put("tid", acquirer.getQrcsTerminalId());
                    transactionData.put("transactionId", transData.getTransactionId());
                    transactionData.put("refundReferenceId", refundReferenceId);
                    String amount = String.format("%.2f", (float) ConvertUtils.parseLongSafe(transData.getAmount(), -1) / 100);
                    transactionData.put("amount", amount);
                    break;
                default:
                    if (listener != null) {
                        listener.onShowErrMessage(Utils.getString(R.string.err_unsupported_trans), Constants.FAILED_DIALOG_SHOW_TIME, false);
                    }
                    return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
            }

            ArrayList<Object> responseResult = encryptedJson(transData, service, action, transactionData);
            if (responseResult == null) {
                if (listener != null) {
                    listener.onShowErrMessage(Utils.getString(R.string.err_connect), Constants.FAILED_DIALOG_SHOW_TIME, false);
                }
                return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
            }

            String code = (String) responseResult.get(0);
            String message = (String) responseResult.get(1);
            JSONObject responseJSON = (JSONObject) responseResult.get(2);

            if (checkEncrpytedJsonRspCode(code, message) != TransResult.SUCC) {
                return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
            }

            parseTransData(transData, responseJSON);

            return new ArrayList<Object>(Arrays.asList(TransResult.SUCC, null));
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
        }
    }

    public ArrayList<Object> refund(TransData transData) {
        int ret = 0;
        try {
            if (Component.isDemo()) {
                LogUtils.d(TAG, "Demo Mode");
                return new ArrayList<Object>(Arrays.asList(TransResult.SUCC, null));
            }

            Acquirer acquirer = AcqManager.getInstance().findAcquirer(AppConstants.QR_ACQUIRER);
            String fundingSource = transData.getFundingSource();

            JSONObject transactionData = new JSONObject();
            String service = "";
            String action = "";

            switch (fundingSource) {
                case "alipay":
                    // Alipay & Wechat also use "Cacnel" api for both refund & void
                    service = "alipay";
                    action = "cancel";

                    transactionData.put("terminal_id", acquirer.getAlipayTerminalId());
                    transactionData.put("merchant_id", acquirer.getAlipayMerchantId());
                    transactionData.put("partner_transaction_id", transData.getRefNo());
                    transactionData.put("funding_source", fundingSource.toUpperCase());
                    transactionData.put("acquirer", acquirer.getAlipayAcquirer());
                    break;
                case "wechat":
                    // Alipay & Wechat also use "Cacnel" api for both refund & void
                    service = "wechat";
                    action = "cancel";

                    transactionData.put("terminal_id", acquirer.getWechatTerminalId());
                    transactionData.put("merchant_id", acquirer.getWechatMerchantId());
                    transactionData.put("partner_transaction_id", transData.getRefNo());
                    transactionData.put("funding_source", fundingSource.toUpperCase());
                    transactionData.put("acquirer", acquirer.getWechatAcquirer());
                    break;
                case "linepay":
                case "true_money":
                case "shopee":
                case "dolfin":
                    break;
                case "qrcs":
                    service = "qrcs";
                    // Qrcs both use refund for void & refund
                    action = "refund";

                    String refundReferenceId = acquirer.getQrcsTerminalId() + transData.getTransactionId();

                    transactionData.put("mid", acquirer.getQrcsMerchantId());
                    transactionData.put("tid", acquirer.getQrcsTerminalId());
                    transactionData.put("transactionId", transData.getTransactionId());
                    transactionData.put("refundReferenceId", refundReferenceId);
                    String amount = String.format("%.2f", (float) ConvertUtils.parseLongSafe(transData.getAmount(), -1) / 100);
                    transactionData.put("amount", amount);
                    break;
                case "promptpay":
                default:
                    if (listener != null) {
                        listener.onShowErrMessage(Utils.getString(R.string.err_unsupported_trans), Constants.FAILED_DIALOG_SHOW_TIME, false);
                    }
                    return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
            }

            ArrayList<Object> responseResult = encryptedJson(transData, service, action, transactionData);
            if (responseResult == null) {
                if (listener != null) {
                    listener.onShowErrMessage(Utils.getString(R.string.err_connect), Constants.FAILED_DIALOG_SHOW_TIME, false);
                }
                return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
            }

            String code = (String) responseResult.get(0);
            String message = (String) responseResult.get(1);
            JSONObject responseJSON = (JSONObject) responseResult.get(2);

            if (checkEncrpytedJsonRspCode(code, message) != TransResult.SUCC) {
                return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
            }

            parseTransData(transData, responseJSON);

            switch (fundingSource) {
                case "alipay":
                case "wechat":
                    break;
                case "promptpay":
                    // Prompt Pay no Refund Function
                    break;
                case "linepay":
                case "true_money":
                case "shopee":
                case "dolfin":
                    break;
                case "qrcs":
                    ArrayList<Object> qrcsInquiryResult = qrcsRefundQuery(transData);
                    if ((int) qrcsInquiryResult.get(0) != TransResult.SUCC) {
                        if (listener != null) {
                            listener.onShowErrMessage(Utils.getString(R.string.err_host_reject), Constants.FAILED_DIALOG_SHOW_TIME, false);
                        }
                        return qrcsInquiryResult;
                    }
                    break;
            }

            return new ArrayList<Object>(Arrays.asList(TransResult.SUCC, null));
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
        }
    }


    private ArrayList<Object> qrcsRefundQuery(TransData transData) {
        try {
            String service = "qrcs";
            String action = "inquiryRefund";

            Acquirer acquirer = transData.getAcquirer();

            JSONObject transactionData = new JSONObject();
            String refundReferenceId = acquirer.getQrcsTerminalId() + transData.getTransactionId();

            transactionData.put("mid", acquirer.getQrcsMerchantId());
            transactionData.put("tid", acquirer.getQrcsTerminalId());
            transactionData.put("transactionId", transData.getTransactionId());
            transactionData.put("refundReferenceId", refundReferenceId);

            ArrayList<Object> responseResult = encryptedJson(transData, service, action, transactionData);
            if (responseResult == null) {
                return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
            }

            String code = (String) responseResult.get(0);
            String message = (String) responseResult.get(1);
            JSONArray responseJSONArray = (JSONArray) responseResult.get(3);
            JSONObject responseJSON = (JSONObject) responseJSONArray.get(responseJSONArray.length() - 1);

            if (checkEncrpytedJsonRspCode(code, message) != TransResult.SUCC) {
                return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
            }

            parseTransData(transData, responseJSON);

            // check if refunded
            String transactionType = responseJSON.optString("transactionType");
            if (!"REFUND".equals(transactionType)) {
                return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
            }

            transData.setAuthCode(responseJSON.getString("authorizeCode"));
            transData.setPan(responseJSON.getString("consumerPAN"));
            String paymentChannel = responseJSON.getString("paymentChannel");

            return new ArrayList<Object>(Arrays.asList(TransResult.SUCC, null));
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
        }
    }

    public ArrayList<Object> cancel(TransData transData) {
        int ret = 0;

        try {
            if (Component.isDemo()) {
                LogUtils.d(TAG, "Demo Mode");
                return new ArrayList<Object>(Arrays.asList(TransResult.SUCC, null));
            }

            Acquirer acquirer = AcqManager.getInstance().findAcquirer(AppConstants.QR_ACQUIRER);
            String fundingSource = transData.getFundingSource();

            JSONObject transactionData = new JSONObject();
            String service = "";
            String action = "";

            switch (fundingSource) {
                case "alipay":
                    service = "alipay";
                    action = "reverse";

                    transactionData.put("terminal_id", acquirer.getAlipayTerminalId());
                    transactionData.put("merchant_id", acquirer.getAlipayMerchantId());
                    transactionData.put("partner_transaction_id", transData.getRefNo());
                    transactionData.put("funding_source", fundingSource.toUpperCase());
                    transactionData.put("acquirer", acquirer.getAlipayAcquirer());
                    break;
                case "wechat":
                    service = "wechat";
                    action = "reverse";

                    transactionData.put("terminal_id", acquirer.getWechatTerminalId());
                    transactionData.put("merchant_id", acquirer.getWechatMerchantId());
                    transactionData.put("partner_transaction_id", transData.getRefNo());
                    transactionData.put("funding_source", fundingSource.toUpperCase());
                    transactionData.put("acquirer", acquirer.getWechatAcquirer());
                    break;
                case "promptpay":
                    service = "tag30";
                    action = "void";

                    transactionData.put("mid", acquirer.getTag30MerchantId());
                    transactionData.put("tid", acquirer.getTag30TerminalId());
                    transactionData.put("transactionId", transData.getRefNo());
                    break;
                case "linepay":
                case "true_money":
                case "shopee":
                case "dolfin":
                    break;
                case "qrcs":
                    service = "qrcs";
                    action = "refund";

                    String refundReferenceId = acquirer.getQrcsTerminalId() + transData.getBillPaymentRef1();

                    transactionData.put("mid", acquirer.getQrcsMerchantId());
                    transactionData.put("tid", acquirer.getQrcsTerminalId());
                    transactionData.put("transactionId", transData.getTransactionId());
                    transactionData.put("refundReferenceId", refundReferenceId);
                    String amount = String.format("%.2f", (float) ConvertUtils.parseLongSafe(transData.getAmount(), -1) / 100);
                    transactionData.put("amount", amount);
                    break;
                default:
                    if (listener != null) {
                        listener.onShowErrMessage(Utils.getString(R.string.err_unsupported_trans), Constants.FAILED_DIALOG_SHOW_TIME, false);
                    }
                    return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
            }

            ArrayList<Object> responseResult = encryptedJson(transData, service, action, transactionData);
            if (responseResult == null) {
                if (listener != null) {
                    listener.onShowErrMessage(Utils.getString(R.string.err_connect), Constants.FAILED_DIALOG_SHOW_TIME, false);
                }
                return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
            }

            String code = (String) responseResult.get(0);
            String message = (String) responseResult.get(1);
            JSONObject responseJSON = (JSONObject) responseResult.get(2);

            if (checkEncrpytedJsonRspCode(code, message) != TransResult.SUCC) {
                return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
            }

            parseTransData(transData, responseJSON);

            return new ArrayList<Object>(Arrays.asList(TransResult.SUCC, null));
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
        }
    }

    public ArrayList<Object> pullSlip(TransData transData) {
        int ret = 0;

        try {
            if (Component.isDemo()) {
                LogUtils.d(TAG, "Demo Mode");
                return new ArrayList<Object>(Arrays.asList(TransResult.SUCC, null));
            }

            Acquirer acquirer = AcqManager.getInstance().findAcquirer(AppConstants.QR_ACQUIRER);
            String fundingSource = transData.getFundingSource();

            JSONObject transactionData = new JSONObject();
            String service = "";
            String action = "";
            switch (fundingSource) {
                case "promptpay":
                    service = "tag30";
                    action = "pullslip";

                    transactionData.put("mid", acquirer.getTag30MerchantId());
                    transactionData.put("tid", acquirer.getTag30TerminalId());
                    transactionData.put("sendingBank", transData.getSendingBankCode());
                    transactionData.put("reference1", transData.getBillPaymentRef1());
                    transactionData.put("reference2", transData.getBillPaymentRef2());
                    transactionData.put("reference3", transData.getBillPaymentRef3());
                    transactionData.put("transRef", transData.getRefNo());
                    break;
                case "alipay":
                case "wechat":
                case "linepay":
                case "true_money":
                case "shopee":
                case "dolfin":
                case "qrcs":
                default:
                    if (listener != null) {
                        listener.onShowErrMessage(Utils.getString(R.string.err_unsupported_trans), Constants.FAILED_DIALOG_SHOW_TIME, false);
                    }
                    return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
            }

            ArrayList<Object> responseResult = encryptedJson(transData, service, action, transactionData);
            if (responseResult == null) {
                if (listener != null) {
                    listener.onShowErrMessage(Utils.getString(R.string.err_connect), Constants.FAILED_DIALOG_SHOW_TIME, false);
                }
                return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
            }

            String code = (String) responseResult.get(0);
            String message = (String) responseResult.get(1);
            JSONObject responseJSON = (JSONObject) responseResult.get(2);
            JSONArray responseJSONArray = (JSONArray) responseResult.get(3);

            if (responseJSONArray != null) {
                responseJSON = (JSONObject) responseJSONArray.get(0);
            }

            switch (fundingSource) {
                case "promptpay":
                    switch (code) {
                        case "0404":            // Transaction is not found
                        case "0637":            // Transaction reference not found
                            return new ArrayList<Object>(Arrays.asList(TransResult.ERR_TRANS_NOT_FOUND, null));
                    }
                    break;
            }

            if (checkEncrpytedJsonRspCode(code, message) != TransResult.SUCC) {
                return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
            }

            switch (fundingSource) {
                case "promptpay":
                    transData.setAmount(CurrencyConverter.convertTag30Amount(responseJSON.optString("amount")).replace(".", ""));
                    transData.setBillerId(responseJSON.optString("payeeProxyId"));
                    transData.setBillPaymentRef1(responseJSON.optString("billPaymentRef1"));
                    transData.setBillPaymentRef2(responseJSON.optString("billPaymentRef2"));
                    transData.setBillPaymentRef3(responseJSON.optString("billPaymentRef3"));
                    break;
            }

            return new ArrayList<Object>(Arrays.asList(TransResult.SUCC, null));
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<Object>(Arrays.asList(TransResult.ERR_ABORTED, null));
        }
    }

    private void parseTransData(final TransData transData, final JSONObject responseJSON) {
        if(responseJSON == null) {
            LogUtils.e(TAG, "Json object is NULL!");
            return;
        }
        transData.setQrTransStatus(responseJSON.optString("status"));
        transData.setQrBuyerUserId(responseJSON.optString("buyer_user_id"));
        transData.setQrBuyerLoginId(responseJSON.optString("buyer_login_id"));
        transData.setQrRefTransId(responseJSON.optString("ref_transaction_id"));
        transData.setQrSlipNo(responseJSON.optString("fastEasySlipNumber"));
        transData.setQrTrxDateTime(responseJSON.optString("transactionDateandTime"));
        transData.setQrPaymentMethod(responseJSON.optString("paymentMethod"));
        transData.setQrTransactionType(responseJSON.optString("transactionType"));
        transData.setQrRefReference(responseJSON.optString("refundReferenceId"));
        transData.setQrRefTrxId(responseJSON.optString("refundTransactionId"));
    }
}
