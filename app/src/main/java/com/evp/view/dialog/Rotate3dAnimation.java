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
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.evp.payment.evpscb.R;

/**
 * rotate 3d  Animation
 */
public class Rotate3dAnimation extends Animation {
    private int mPivotXType = ABSOLUTE;
    private int mPivotYType = ABSOLUTE;
    private float mPivotXValue = 0.0f;
    private float mPivotYValue = 0.0f;

    private float mFromDegrees;
    private float mToDegrees;
    private float mPivotX;
    private float mPivotY;
    private Camera mCamera;
    private int mRollType;

    private static final int ROLL_BY_X = 0;
    private static final int ROLL_BY_Y = 1;
    private static final int ROLL_BY_Z = 2;

    /**
     * Instantiates a new Rotate 3 d animation.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public Rotate3dAnimation(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Rotate3dAnimation);

        mFromDegrees = a.getFloat(R.styleable.Rotate3dAnimation_fromDeg, 0.0f);
        mToDegrees = a.getFloat(R.styleable.Rotate3dAnimation_toDeg, 0.0f);
        mRollType = a.getInt(R.styleable.Rotate3dAnimation_rollType, ROLL_BY_X);
        Description d = parseValue(a.peekValue(R.styleable.Rotate3dAnimation_pivotX));
        mPivotXType = d.type;
        mPivotXValue = d.value;

        d = parseValue(a.peekValue(R.styleable.Rotate3dAnimation_pivotY));
        mPivotYType = d.type;
        mPivotYValue = d.value;

        a.recycle();

        initializePivotPoint();
    }

    /**
     * Instantiates a new Rotate 3 d animation.
     *
     * @param rollType    the roll type
     * @param fromDegrees the from degrees
     * @param toDegrees   the to degrees
     */
    public Rotate3dAnimation(int rollType, float fromDegrees, float toDegrees) {
        mRollType = rollType;
        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;
        mPivotX = 0.0f;
        mPivotY = 0.0f;
    }

    /**
     * Instantiates a new Rotate 3 d animation.
     *
     * @param rollType    the roll type
     * @param fromDegrees the from degrees
     * @param toDegrees   the to degrees
     * @param pivotX      the pivot x
     * @param pivotY      the pivot y
     */
    public Rotate3dAnimation(int rollType, float fromDegrees, float toDegrees, float pivotX, float pivotY) {
        mRollType = rollType;
        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;

        mPivotXType = ABSOLUTE;
        mPivotYType = ABSOLUTE;
        mPivotXValue = pivotX;
        mPivotYValue = pivotY;
        initializePivotPoint();
    }

    /**
     * Instantiates a new Rotate 3 d animation.
     *
     * @param rollType    the roll type
     * @param fromDegrees the from degrees
     * @param toDegrees   the to degrees
     * @param pivotXType  the pivot x type
     * @param pivotXValue the pivot x value
     * @param pivotYType  the pivot y type
     * @param pivotYValue the pivot y value
     */
    public Rotate3dAnimation(int rollType, float fromDegrees, float toDegrees, int pivotXType, float pivotXValue,
                             int pivotYType, float pivotYValue) {
        mRollType = rollType;
        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;

        mPivotXValue = pivotXValue;
        mPivotXType = pivotXType;
        mPivotYValue = pivotYValue;
        mPivotYType = pivotYType;
        initializePivotPoint();
    }

    /**
     * The type Description.
     */
    protected static class Description {
        /**
         * The Type.
         */
        int type;
        /**
         * The Value.
         */
        float value;
    }

    private Description parseValue(TypedValue value) {
        Description d = new Description();
        if (value == null) {
            d.type = ABSOLUTE;
            d.value = 0;
        } else {
            if (value.type == TypedValue.TYPE_FRACTION) {
                d.type = (value.data & TypedValue.COMPLEX_UNIT_MASK) == TypedValue.COMPLEX_UNIT_FRACTION_PARENT ? RELATIVE_TO_PARENT
                        : RELATIVE_TO_SELF;
                d.value = TypedValue.complexToFloat(value.data);
                return d;
            } else if (value.type == TypedValue.TYPE_FLOAT) {
                d.type = ABSOLUTE;
                d.value = value.getFloat();
                return d;
            } else if (value.type >= TypedValue.TYPE_FIRST_INT && value.type <= TypedValue.TYPE_LAST_INT) {
                d.type = ABSOLUTE;
                d.value = value.data;
                return d;
            }
        }

        d.type = ABSOLUTE;
        d.value = 0.0f;

        return d;
    }

    private void initializePivotPoint() {
        if (mPivotXType == ABSOLUTE) {
            mPivotX = mPivotXValue;
        }
        if (mPivotYType == ABSOLUTE) {
            mPivotY = mPivotYValue;
        }
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mCamera = new Camera();
        mPivotX = resolveSize(mPivotXType, mPivotXValue, width, parentWidth);
        mPivotY = resolveSize(mPivotYType, mPivotYValue, height, parentHeight);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        final float fromDegrees = mFromDegrees;
        float degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime);

        final Matrix matrix = t.getMatrix();

        mCamera.save();
        switch (mRollType) {
            case ROLL_BY_X:
                mCamera.rotateX(degrees);
                break;
            case ROLL_BY_Y:
                mCamera.rotateY(degrees);
                break;
            case ROLL_BY_Z:
                mCamera.rotateZ(degrees);
                break;
            default:
                break;
        }
        mCamera.getMatrix(matrix);
        mCamera.restore();

        matrix.preTranslate(-mPivotX, -mPivotY);
        matrix.postTranslate(mPivotX, mPivotY);
    }
}