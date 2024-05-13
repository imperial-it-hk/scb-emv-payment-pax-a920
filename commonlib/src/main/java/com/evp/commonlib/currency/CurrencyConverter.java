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
package com.evp.commonlib.currency;


import com.evp.commonlib.utils.LogUtils;

import org.greenrobot.greendao.annotation.NotNull;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

public class CurrencyConverter {

    private static final String TAG = "CurrencyConv";
    private static final List<Locale> locales = new ArrayList<>();

    private static Locale defLocale = Locale.US;

    private static boolean isUsingDefaultCurrencyStrategy = true;

    static {
        Locale[] tempLocales = Locale.getAvailableLocales();
        for (Locale i : tempLocales) {
            try {
                CountryCode country = CountryCode.getByCode(i.getISO3Country());
                Currency.getInstance(i); // just for filtering
                if (country != null) {
                    locales.add(i);
                }
            } catch (IllegalArgumentException | MissingResourceException e) {
                //LogUtils.d(TAG, "", e);
            }
        }
    }

    private CurrencyConverter() {
        //do nothing
    }

    public static List<Locale> getSupportedLocale() {
        return locales;
    }

    public static void getSupportedLocaleList(List<String> contentList) {
        List<Locale> locales = CurrencyConverter.getSupportedLocale();
        for (Locale locale : locales) {
            contentList.add(locale.getDisplayName(Locale.US));
        }
        Collections.sort(contentList);
    }

    /**
     * @param countryName : {@see Locale#getDisplayName(Locale)}
     */
    public static Locale setDefCurrency(String countryName) {
        for (Locale i : locales) {
            if (i.getDisplayName(Locale.US).equals(countryName)) {
                if (!i.equals(defLocale)) {
                    defLocale = i;
                    Locale.setDefault(defLocale);
                }
                return defLocale;
            }
        }
        return defLocale;
    }

    public static Locale getDefCurrency() {
        return defLocale;
    }

    private static String modifyCurrencyFormat(String currencyCode, String result) {
        return result;
    }

    private static String recoverCurrencyFormat(String currencyCode, String result) {
        return result;
    }

    public static int getDigitsNum(Currency currency) {
        int digits = currency.getDefaultFractionDigits();
        return digits;
    }

    public static int getDigitsNum() {
        Currency currency = Currency.getInstance(defLocale);
        return getDigitsNum(currency);
    }

    public static NumberFormat getFormatter(Locale locale, Currency currency, int digits, boolean isCurrency) {
        NumberFormat formatter;
        if (isCurrency) {
            formatter = NumberFormat.getCurrencyInstance(locale);
        } else {
            formatter = NumberFormat.getNumberInstance(locale);
        }
        formatter.setMinimumFractionDigits(digits);
        formatter.setMaximumFractionDigits(digits);
        return formatter;
    }

    public static String convertAmount(String amount) {
        if (amount == null) { return "null"; }
        try {
            double amt = Double.parseDouble(amount)/100;
            NumberFormat formatter = NumberFormat.getNumberInstance();
            formatter.setMinimumFractionDigits(2);
            formatter.setMaximumFractionDigits(2);
            return formatter.format(amt);
        } catch (NumberFormatException e) {
            return "null";
        }
    }

    public static String convertTag30Amount(String amount) {
        if (amount == null) { return "null"; }
        try {
            double amt = Double.parseDouble(amount);
            NumberFormat formatter = NumberFormat.getNumberInstance();
            formatter.setMinimumFractionDigits(2);
            formatter.setMaximumFractionDigits(2);
            return formatter.format(amt);
        } catch (NumberFormatException e) {
            return "null";
        }
    }

    public static boolean isIsUsingDefaultCurrencyStrategy() {
        return isUsingDefaultCurrencyStrategy;
    }

    public static void setIsUsingDefaultCurrencyStrategy(boolean isUsingDefaultCurrencyStrategy) {
        CurrencyConverter.isUsingDefaultCurrencyStrategy = isUsingDefaultCurrencyStrategy;
    }

    /**
     * @param amount
     * @return
     */
    public static String convert(long amount) {
        return convert(amount, defLocale);
    }

    /**
     * @param amount
     * @param locale
     * @return
     */
    public static String convert(long amount, Locale locale) {
        return convert(amount, locale, isUsingDefaultCurrencyStrategy, false);
    }

    public static String convert(long amount, boolean noDollarSign) {
        return convert(amount, defLocale, false, noDollarSign);
    }

    public static String convert(long amount, Locale locale, boolean isCurrency, boolean noDollarSign) {
        String result;
        Currency currency = Currency.getInstance(locale);
        if (currency != null) {
            int digits = getDigitsNum(currency);
            long newAmount = amount < 0 ? -amount : amount; // AET-58
            String prefix = amount < 0 ? "-" : "";
            try {
                double amt = (double) newAmount / (Math.pow(10, digits));
                String currencyCode = currency.getCurrencyCode();
                if (noDollarSign) {
                    NumberFormat formatter = getFormatter(locale, currency, digits, false);
                    result = prefix + formatter.format(amt);
                } else {
                    NumberFormat formatter = getFormatter(locale, currency, digits, isCurrency);
                    if (isCurrency) {
                        result = prefix + formatter.format(amt);
                    } else {
                        currencyCode = currencyCode.equalsIgnoreCase("CNY") ? "RMB" : currencyCode;
                        result = prefix + currencyCode + formatter.format(amt);
                    }
                }
                return modifyCurrencyFormat(currencyCode, result);
            } catch (IllegalArgumentException e) {
                LogUtils.e(TAG, "", e);
            }
        }
        return "";
    }

    public static Long parse(String formatterAmount) {
        return parse(formatterAmount, defLocale);
    }

    public static Long parse(String formatterAmount, Locale locale) {
        return parse(formatterAmount, locale, isUsingDefaultCurrencyStrategy);
    }

    public static Long parse(String formatterAmount, Locale locale, boolean isCurrency) {
        String amount;
        Currency currency = Currency.getInstance(locale);
        int digits = getDigitsNum(currency);
        String newFormatterAmount = formatterAmount;

        String currencyCode = currency.getCurrencyCode();
        if (isCurrency) {
            amount = newFormatterAmount;
        } else {
            currencyCode = currency.getCurrencyCode().equals("CNY") ? "RMB" : currency.getCurrencyCode();
            amount = dealSignSymbol(formatterAmount, newFormatterAmount, currencyCode);
        }

        amount = recoverCurrencyFormat(currencyCode, amount);

        NumberFormat formatter = getFormatter(locale, currency, digits, isCurrency);
        try {
            Number num = formatter.parse(amount);

            return Math.round(num.doubleValue() * Math.pow(10, digits));
        } catch (ParseException | NumberFormatException e) {
            LogUtils.e(TAG, "", e);
        }
        return 0L;
    }

    public static Long parseParam(String formatterAmount) {
        Currency currency = Currency.getInstance(defLocale);
        int digits = getDigitsNum(currency);
        try {
            double num = Double.parseDouble(formatterAmount);
            return Math.round(num * Math.pow(10, digits));
        } catch (NumberFormatException e) {
            LogUtils.e(TAG, "", e);
        }
        return 0L;
    }

    @NotNull
    private static String dealSignSymbol(String formatterAmount, String newFormatterAmount, String currencyCode) {
        String amount;
        if ("-".equals(formatterAmount.substring(0, 1))) {
            newFormatterAmount = formatterAmount.substring(1);
        }
        if (currencyCode.equals(newFormatterAmount.substring(0, currencyCode.length()))) {
            amount = newFormatterAmount.substring(currencyCode.length());
        } else {
            amount = newFormatterAmount;
        }
        return amount;
    }

    public static Locale getLocaleFromCountryCode(String countryCode) {
        int countryCodeInt = 0;
        try {
            countryCodeInt = Integer.parseInt(countryCode);
        } catch(NumberFormatException e) {
            LogUtils.d(TAG, "", e);
            return defLocale;
        }
        for (Locale i : locales) {
            try {
                int code = CountryCode.getByCode(i.getCountry()).getNumeric();
                if(code == countryCodeInt) {
                    return i;
                }
            } catch (Exception e) {
                //LogUtils.d(TAG, "", e);
            }
        }
        return defLocale;
    }

    public static String getCurrencySymbol(String countryCode) {
        Locale locale = getLocaleFromCountryCode(countryCode);
        Currency currency = Currency.getInstance(locale);
        return currency.getCurrencyCode();
    }
}
