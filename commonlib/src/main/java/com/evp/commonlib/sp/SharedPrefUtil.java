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
 * 20190725  	         xieYb                  Create
 * ===========================================================================================
 */

package com.evp.commonlib.sp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;

import java.util.Map;

/**
 * SharedPreference Utils
 */
public class SharedPrefUtil {
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    /**
     * constructor to create a default sp
     * even if more than one instance created, they refer to the same sp, i.e. default sp
     *
     * @param context context
     */
    public SharedPrefUtil(Context context) {
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sp.edit();
        editor.apply();
    }

    /**
     * constructor to create an sp by the name of @param spName
     *
     * @param context context
     * @param spName  the name of sp
     */
    public SharedPrefUtil(Context context, String spName) {
        sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        editor = sp.edit();
        editor.apply();
    }

    /**
     * Set a String value in the sp
     */
    public void putString(String key, String value) {
        editor.putString(key, value).apply();
    }

    /**
     * Retrieve a String value from the sp
     *
     * @param key key
     * @return Returns the value if it exists, or {@code null} if not
     */
    public String getString(String key) {
        return getString(key, null);
    }

    /**
     * Retrieve a String value from the sp
     *
     * @param key key
     * @return Returns the value if it exists, or {@code defaultValue} if not
     */
    public String getString(String key, String defaultValue) {
        return sp.getString(key, defaultValue);
    }

    /**
     * Set an int value in the sp
     */
    public void putInt(String key, int value) {
        editor.putInt(key, value).apply();
    }

    /**
     * Retrieve an int value from the sp
     *
     * @param key key
     * @return Returns the value if it exists, or -1 if not
     */
    public int getInt(String key) {
        return getInt(key, -1);
    }

    /**
     * Retrieve an int value from the sp
     *
     * @param key
     * @return Returns the value if it exists, or {@code defaultValue} if not
     */
    public int getInt(String key, int defaultValue) {
        return sp.getInt(key, defaultValue);
    }

    /**
     * Set a long value in the sp
     */
    public void putLong(String key, long value) {
        editor.putLong(key, value).apply();
    }

    /**
     * Retrieve a long value from the sp
     *
     * @param key key
     * @return Returns the value if it exists, or -1L if not
     */
    public long getLong(String key) {
        return getLong(key, -1L);
    }

    /**
     * Retrieve a long value from the sp
     *
     * @param key
     * @return Returns the value if it exists, or {@code defaultValue} if not
     */
    public long getLong(String key, long defaultValue) {
        return sp.getLong(key, defaultValue);
    }

    /**
     * Set a float value in the sp
     */
    public void putFloat(String key, float value) {
        editor.putFloat(key, value).apply();
    }

    /**
     * Retrieve a float value from the sp
     *
     * @param key key
     * @return Returns the value if it exists, or -1f if not
     */
    public float getFloat(String key) {
        return getFloat(key, -1f);
    }

    /**
     * Retrieve a float value from the sp
     *
     * @param key key
     * @return Returns the value if it exists, or {@code defaultValue} if not
     */
    public float getFloat(String key, float defaultValue) {
        return sp.getFloat(key, defaultValue);
    }

    /**
     * Set a float value in the sp
     */
    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value).apply();
    }

    /**
     * Retrieve a boolean value from the sp
     *
     * @param key key
     * @return Returns the value if it exists, or false if not
     */
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    /**
     * Retrieve a boolean value from the sp
     *
     * @param key key
     * @return Returns the value if it exists, or {@code defaultValue} if not
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return sp.getBoolean(key, defaultValue);
    }

    /**
     * Retrieve all values from the sp
     *
     * @return Returns a map containing a list of pairs key/value representing
     * the preferences.
     */
    public Map<String, ?> getAll() {
        return sp.getAll();
    }

    /**
     * remove a preference from sp
     *
     * @param key The name of the preference to remove.
     */
    public void remove(String key) {
        editor.remove(key).apply();
    }

    /**
     * Checks whether the sp contains a preference.
     *
     * @param key The name of the preference to check.
     * @return Returns true if the preference exists in the preferences,
     * otherwise false.
     */
    public boolean contains(String key) {
        return sp.contains(key);
    }

    /**
     * remove all preferences
     */
    public void clear() {
        editor.clear().apply();
    }

    public void putByteArr(String key, byte[] value) {
        String str = Base64.encodeToString(value,Base64.DEFAULT);
        putString(key, str);
    }

    public byte[] getByteArr(String key, byte[] defaultValue) {
        String str = getString(key);
        if (TextUtils.isEmpty(str)) {
            return defaultValue;
        }
        return Base64.decode(str,Base64.DEFAULT);
    }

    public byte[] getByteArr(String key) {
        return getByteArr(key, new byte[0]);
    }
}
