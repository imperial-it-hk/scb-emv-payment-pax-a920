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
 * 20190108  	         Steven.W                Create
 * ===========================================================================================
 */
package com.evp.view.dialog;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import com.evp.commonlib.utils.LogUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * The type Opt animation loader.
 */
public class OptAnimationLoader {

    private OptAnimationLoader() {
        //do nothing
    }

    /**
     * Load animation animation.
     *
     * @param context the context
     * @param id      the id
     * @return the animation
     * @throws NotFoundException the not found exception
     */
    public static Animation loadAnimation(Context context, int id) throws Resources.NotFoundException {

        XmlResourceParser parser = null;
        try {
            parser = context.getResources().getAnimation(id);
            return createAnimationFromXml(context, parser);
        } catch (XmlPullParserException | IOException ex) {
            Resources.NotFoundException rnf = new Resources.NotFoundException("Can't load animation resource ID #0x"
                    + Integer.toHexString(id));
            rnf.initCause(ex);
            throw rnf;
        } finally {
            if (parser != null)
                parser.close();
        }
    }

    private static Animation createAnimationFromXml(Context c, XmlPullParser parser) throws XmlPullParserException,
            IOException {

        return createAnimationFromXml(c, parser, null, Xml.asAttributeSet(parser));
    }

    private static Animation createAnimationFromXml(Context c, XmlPullParser parser, AnimationSet parent,
                                                    AttributeSet attrs) throws XmlPullParserException, IOException {

        Animation anim = null;

        // Make sure we are on a start tag.
        int type;
        int depth = parser.getDepth();

        final String set = "set";
        final String alpha = "alpha";
        final String scale = "scale";
        final String rotate = "rotate";
        final String translate = "translate";

        while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth)
                && type != XmlPullParser.END_DOCUMENT) {

            if (type != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();

            switch (name) {
                case set:
                    anim = new AnimationSet(c, attrs);
                    createAnimationFromXml(c, parser, (AnimationSet) anim, attrs);
                    break;
                case alpha:
                    anim = new AlphaAnimation(c, attrs);
                    break;
                case scale:
                    anim = new ScaleAnimation(c, attrs);
                    break;
                case rotate:
                    anim = new RotateAnimation(c, attrs);
                    break;
                case translate:
                    anim = new TranslateAnimation(c, attrs);
                    break;
                default:
                    anim = genAnimFromClass(c, parser, attrs);
                    break;
            }

            if (parent != null && anim != null) {
                parent.addAnimation(anim);
            }
        }

        return anim;

    }

    private static Animation genAnimFromClass(Context c, XmlPullParser parser, AttributeSet attrs) {
        Animation anim = null;
        try {
            anim = (Animation) Class.forName(parser.getName()).getConstructor(Context.class, AttributeSet.class)
                    .newInstance(c, attrs);
        } catch (Exception te) {
            LogUtils.w("OptAnimationLoader", "", te);
        }
        return anim;
    }
}
