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
 * 20190108  	         ligq                    Create
 * ===========================================================================================
 */
package com.evp.settings;

import android.util.Xml;

import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.utils.LogUtils;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.settings.spupgrader.SpUpgrader;
import com.evp.settings.spupgrader.Upgrade1To2;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * The type Sys param.
 */
public class SysParam {
    private static class LazyHolder {
        /**
         * The constant INSTANCE.
         */
        public static final SysParam INSTANCE = new SysParam();
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static SysParam getInstance() {
        return LazyHolder.INSTANCE;
    }

    private SysParam() {
        this.mSysSp = new SharedPref(FinancialApplication.getApp());
        load();
    }

    private static final String TAG = "SysParam";
    private static final String UPGRADER_PATH = "com.pax.settings.spupgrader";
    private static final String IS_PARAM_FILE_EXIST = "IS_PARAM_FILE_EXIST";
    private static final String VERSION_TAG = "PARAM_VERSION";
    private static final int VERSION = 1;
    /**
     * The constant KEY_NONE.
     */
    public static final String KEY_NONE = "N";
    private SharedPref mSysSp;

    /**
     * 系统参数加载，如果sp中不存在则添加
     */
    private void load() {
        // 设置默认参数值
        if (isParamFileExist()) {
            try {
                for (int i = getVersion(); i < VERSION; i++) {
                    SpUpgrader.upgrade(mSysSp, i, i + 1, UPGRADER_PATH);
                    mSysSp.putInt(VERSION_TAG, i + 1);
                }
            } catch (IllegalArgumentException e) {
                LogUtils.w(TAG, "", e);
            }
            return;
        }
        PullParseService pps = new PullParseService(mSysSp);
        pps.parse();

        set(IS_PARAM_FILE_EXIST, true);
        set(VERSION_TAG, VERSION);

        //special tags
        String commTimeout = FinancialApplication.getApp().getResources().getStringArray(R.array.edc_connect_time_entries)[0];
        set(R.string.COMM_TIMEOUT, Integer.parseInt(commTimeout));
        // 通讯方式
        set(R.string.COMM_TYPE, Utils.getString(R.string.demo));

        set(R.string.EDC_CURRENCY_LIST, CurrencyConverter.getDefCurrency().getDisplayName(Locale.US));
        set(R.string.EDC_PED_MODE, FinancialApplication.getApp().getResources().getStringArray(R.array.edc_ped_mode_entries)[0]);
        set(R.string.EDC_CLSS_MODE, FinancialApplication.getApp().getResources().getStringArray(R.array.edc_clss_mode_entries)[0]);
        set(R.string.EDC_PRINTER_TYPE, Utils.getPrintType());

        set(R.string.KEY_ALGORITHM, Utils.getString(R.string.keyManage_menu_3des)); // 密钥算法

        if (VERSION >= 2) {
            new Upgrade1To2().upgrade(mSysSp);
        }

        set(R.string.LOG_FILE_INDEX, 1);
    }


    public void setLanguage(String lang) {
        set("language", lang);
    }

    public String getLanguage() {
        String lang = getString("language");
        if (lang != null) {
            return lang;
        }
        return "";
    }

    /**
     * Gets string.
     *
     * @param key the key
     * @return the string
     */
    public final String getString(Object key) {
        return (String) get(key, "");
    }

    /**
     * Gets boolean.
     *
     * @param key the key
     * @return the boolean
     */
    public final boolean getBoolean(Object key) {
        return (Boolean) get(key, false);
    }

    /**
     * Gets int.
     *
     * @param key the key
     * @return the int
     */
    public final int getInt(Object key) {
        return (int) get(key, 0);
    }

    /**
     * Gets int.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the int
     */
    public final int getInt(Object key, int defaultValue) {
        return (int) get(key, defaultValue);
    }

    /**
     * Get object.
     *
     * @param key   the key
     * @param value the value
     * @return the object
     */
    public final Object get(Object key, Object value) {
        return value instanceof String ? (Object) this.mSysSp.getString(this.getKey(key), (String) value) : (value instanceof Boolean ? (Object) this.mSysSp.getBoolean(this.getKey(key), (Boolean) value) : (value instanceof Integer ? (Object) this.mSysSp.getInt(this.getKey(key), ((Number) value).intValue()) : value));
    }

    /**
     * Set.
     *
     * @param key   the key
     * @param value the value
     */
    public final void set(Object key, Object value) {
        if (value instanceof String) {
            mSysSp.putString(getKey(key), (String) value);
        } else if (value instanceof Boolean) {
            mSysSp.putBoolean(getKey(key), (Boolean) value);
        } else if (value instanceof Long) {
            mSysSp.putInt(getKey(key), ((Long) value).intValue());
        } else if (value instanceof Short) {
            mSysSp.putInt(getKey(key), ((Short) value).intValue());
        } else if (value instanceof Byte) {
            mSysSp.putInt(getKey(key), ((Byte) value).intValue());
        } else if (value instanceof Integer) {
            mSysSp.putInt(getKey(key), (Integer) value);
        }

    }

    /**
     * Put byte arr.
     *
     * @param key   the key
     * @param value the value
     */
    public void putByteArr(String key, byte[] value) {
        mSysSp.putByteArr(key, value);
    }

    /**
     * Get byte arr byte [ ].
     *
     * @param key the key
     * @return the byte [ ]
     */
    public byte[] getByteArr(String key) {
        return mSysSp.getByteArr(key);
    }

    /**
     * Gets key.
     *
     * @param key the key
     * @return the key
     */
    public final String getKey(Object key) {
        String result;
        if (key instanceof Integer) {
            result = Utils.getString((Integer) key);
        } else {
            result = String.valueOf(key);
        }
        return result;
    }

    private final boolean isParamFileExist() {
        return getBoolean("IS_PARAM_FILE_EXIST");
    }

    private final int getVersion() {
        return getInt("PARAM_VERSION");
    }

    private static final class PullParseService {
        private final HashMap<String, Integer> intMap;
        private final HashMap<String, Boolean> boolMap;
        private final HashMap<String, String> stringMap;
        private final SharedPref sp;

        /**
         * Instantiates a new Pull parse service.
         *
         * @param sp the sp
         */
        public PullParseService(SharedPref sp) {
            super();
            this.sp = sp;
            this.intMap = new HashMap<>();
            this.boolMap = new HashMap<>();
            this.stringMap = new HashMap<>();
        }

        private void setIntOrString(String tag, String value) {
            stringMap.put(tag, value);
        }

        private void setIntMap(String tag, int value) {
            intMap.put(tag, value);
        }

        private String safeNextText(XmlPullParser parser) throws XmlPullParserException, IOException {
            String result = parser.nextText();
            if (parser.getEventType() != XmlPullParser.END_TAG) {
                parser.nextTag();
            }
            return result;
        }

        private int safeNextInt(XmlPullParser parser) throws XmlPullParserException, IOException {
            String result = parser.nextText();
            if (parser.getEventType() != XmlPullParser.END_TAG) {
                parser.nextTag();
            }

            LogUtils.d("young", result);

            int intResult;
            try {
                intResult = Integer.parseInt(result);
            } catch (Exception e) {
                LogUtils.e("young", "", e);
                intResult = 0;
            }

            return intResult;
        }

        private final void setTag(XmlPullParser parser) throws XmlPullParserException, IOException {
            if ("string".equals(parser.getName())) {//判断开始标签元素是否是string
                setIntOrString(parser.getAttributeValue(0), safeNextText(parser));
            } else if ("boolean".equals(parser.getName())) {
                boolMap.put(parser.getAttributeValue(0), Boolean.valueOf(parser.getAttributeValue(1)));
            } else if ("int".equals(parser.getName())) {
                String tag = parser.getAttributeValue(0);
                LogUtils.d("young", "key:" + tag);
                setIntMap(tag, this.safeNextInt(parser));
            }

        }

        /**
         * Parse.
         */
        public final void parse() {
            try {
                InputStream in = FinancialApplication.getApp().getResources().openRawResource(R.raw.pref);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(in, "UTF-8");
                int event = parser.getEventType();//产生第一个事件
                while (event != XmlPullParser.END_DOCUMENT) {
                    if (XmlPullParser.START_TAG == event) {//判断当前事件是否是标签元素开始事件
                        setTag(parser);
                    }
                    event = parser.next();//进入下一个元素并触发相应事件
                }//end while
            } catch (IOException e) {
                LogUtils.e("SysParam", "", e);
            } catch (XmlPullParserException e) {
                LogUtils.e("SysParam", "", e);
            }


            Iterator it = intMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String key = (String) entry.getKey();
                int value = (int) entry.getValue();
                sp.putInt(key, value);
            }

            it = boolMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String key = (String) entry.getKey();
                boolean value = (boolean) entry.getValue();
                sp.putBoolean(key, value);
            }

            it = stringMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                sp.putString(key, value);
            }
        }
    }

    /**
     * SSL
     */
    public enum CommSslType {
        /**
         * No ssl comm ssl type.
         */
        NO_SSL(Utils.getString(R.string.NO_SSL)),
        /**
         * Ssl comm ssl type.
         */
        SSL(Utils.getString(R.string.SSL)),
        ;

        private final String str;

        CommSslType(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }

    /**
     * The type Comm type.
     */
    public static final class CommType {
        /**
         * The constant LAN.
         */
        public static final String LAN = "LAN";
        /**
         * The constant MOBILE.
         */
        public static final String MOBILE = "MOBILE";
        /**
         * The constant WIFI.
         */
        public static final String WIFI = "WIFI";
        /**
         * The constant DEMO.
         */
        public static final String DEMO = "DEMO";

    }

    /**
     * TLE Key set ID
     */
    public enum TleKeySetIdType {

        SET_1(Utils.getString(R.string.SET_1)),

        SET_2(Utils.getString(R.string.SET_2)),

        SET_3(Utils.getString(R.string.SET_3)),

        SET_4(Utils.getString(R.string.SET_4)),

        SET_5(Utils.getString(R.string.SET_5)),

        SET_6(Utils.getString(R.string.SET_6)),

        SET_7(Utils.getString(R.string.SET_7)),

        SET_8(Utils.getString(R.string.SET_8)),

        SET_9(Utils.getString(R.string.SET_9)),

        SET_10(Utils.getString(R.string.SET_10)),
        ;

        private final String str;

        TleKeySetIdType(String str) {
            this.str = str;
        }

        @Override
        public String toString() {
            return str;
        }
    }
}
