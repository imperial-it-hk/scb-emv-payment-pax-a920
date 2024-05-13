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
 * 20190108  	         Joshua Huang            Create
 * ===========================================================================================
 */
package com.evp.pay.utils;

import android.util.Xml;

import com.evp.commonlib.utils.LogUtils;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Xml utils.
 */
public class XmlUtils {
    /**
     * Parse XML file to POJO
     *
     * @param is          input stream
     * @param clazz       POJO class
     * @param fields      字段集合一一对应节点集合
     * @param elements    节点集合一一对应字段集合
     * @param itemElement 每一项的节点标签
     * @return list
     */
    public static List<Object> parse(InputStream is, Class<?> clazz,
                                     List<String> fields, List<String> elements, String itemElement) {
        LogUtils.v("rss", "开始解析XML.");
        List<Object> list = new ArrayList<Object>();
        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(is, "UTF-8");
            int event = xmlPullParser.getEventType();

            Object obj = null;

            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if (itemElement.equals(xmlPullParser.getName())) {
                            obj = clazz.newInstance();
                        }
                        if (obj != null
                                && elements.contains(xmlPullParser.getName())) {
                            setFieldValue(obj, fields.get(elements
                                            .indexOf(xmlPullParser.getName())),
                                    xmlPullParser.nextText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (itemElement.equals(xmlPullParser.getName())) {
                            list.add(obj);
                            obj = null;
                        }
                        break;
                    default:
                        break;
                }
                event = xmlPullParser.next();
            }
        } catch (Exception e) {
            LogUtils.e("rss", "解析XML异常：" + e.getMessage());
            throw new RuntimeException("解析XML异常：" + e.getMessage());
        }
        return list;
    }

    /**
     * 设置字段值
     *
     * @param obj          实例对象
     * @param propertyName 字段名
     * @param value        新的字段值
     * @return
     */
    public static void setFieldValue(Object obj, String propertyName,
                                     Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(propertyName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception ex) {
            throw new RuntimeException();
        }
    }
}
