/*
 * Copyright (C) 2014. The Android Open Source Project.
 *
 *         yinglovezhuzhu@gmail.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.opensource.imagecroper.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

public class FocusView extends View {

    private static final String TAG = "FocusView";

    private static final int DEFAULT_FOCUS_SIZE = 400;

    /**  **/
    private Paint mPaint = new Paint();

    /** 暗色 **/
    private int mDarkColor = Color.argb(0xAF, 0x00, 0x00, 0x00);

    /** 焦点框的边框颜色 **/
    private int mFocusColor = Color.argb(0xFF, 0x80, 0x80, 0x80);

    /** 焦点框边框的宽度（画笔宽度） **/
    private float mBorderWidth = 3.0f;

    /** 焦点框的宽度 **/
    private int mFocusWidth = DEFAULT_FOCUS_SIZE;

    /** 焦点框的高度 **/
    private int mFocusHeight = DEFAULT_FOCUS_SIZE;

    /**  **/
    private RectF mFocusRect = new RectF();

    /**  **/
    private PointF mFocusMidPoint = new PointF();

    /**  **/
    private Style mStyle = Style.CIRCLE;

    public FocusView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public FocusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FocusView(Context context) {
        super(context);
        init(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Path focusPath = new Path();
        if (Style.RECTANGLE == mStyle) {
            focusPath.addRect(mFocusRect, Path.Direction.CCW);

            canvas.save();
            canvas.clipRect(getLeft(), getTop(), getRight(), getBottom());
            canvas.clipPath(focusPath, Region.Op.DIFFERENCE);
            canvas.drawColor(Color.argb(0xAA, 0x0, 0x0, 0x0));
            canvas.restore();

        } else if (Style.CIRCLE == mStyle) {
            float radius = Math.min((mFocusRect.right - mFocusRect.left) / 2,
                    (mFocusRect.bottom - mFocusRect.top) / 2);
            focusPath.addCircle(mFocusMidPoint.x, mFocusMidPoint.y, radius, Path.Direction.CCW);

            canvas.save();
            canvas.clipRect(getLeft(), getTop(), getRight(), getBottom());
            canvas.clipPath(focusPath, Region.Op.DIFFERENCE);
            canvas.drawColor(Color.argb(0xAA, 0x0, 0x0, 0x0));
            canvas.restore();

        }
        mPaint.setColor(mFocusColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mBorderWidth);
        mPaint.setAntiAlias(true);
        canvas.drawPath(focusPath, mPaint); //绘制焦点框
        focusPath.reset();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(changed) {
            //计算出焦点框的中点的坐标和上、下、左、右边的x或y的值
            mFocusMidPoint.set((getRight() - getLeft()) / 2, (getBottom() - getTop()) / 2);
            mFocusRect.left = mFocusMidPoint.x - mFocusWidth / 2;
            mFocusRect.right = mFocusMidPoint.x + mFocusWidth / 2;
            mFocusRect.top = mFocusMidPoint.y - mFocusHeight / 2;
            mFocusRect.bottom = mFocusMidPoint.y + mFocusHeight / 2;
        }
    }

    /**
     * 获取焦点框的位置信息
     *
     * @return
     */
    public RectF getFocusRect() {
        return mFocusRect;
    }

    /**
     * 返回焦点框中间点坐标
     *
     * @return
     */
    public PointF getFocusMidPoint() {
        return mFocusMidPoint;
    }

    /**
     * 返回焦点框宽度
     *
     * @return
     */
    public int getFocusWidth() {
        return mFocusWidth;
    }

    /**
     * 设置焦点框的宽度（最大不能超过屏幕的宽度）
     *
     * @param width
     */
    public void setFocusWidth(int width) {
        final int focusWidth = width > getResources().getDisplayMetrics().widthPixels
                ? getResources().getDisplayMetrics().widthPixels : width;
        this.mFocusWidth = focusWidth;
        invalidate();
    }

    /**
     * 设置焦点框的高度
     * @return
     */
    public int getFocusHeight() {
        return mFocusHeight;
    }

    /**
     * 获取焦点框的高度（最大不能超过屏幕的高度）
     * @param height 高度
     */
    public void setFocusHeight(int height) {
        final int focusHeight = height > getResources().getDisplayMetrics().heightPixels
                ? getResources().getDisplayMetrics().heightPixels : height;
        this.mFocusHeight = focusHeight;
        invalidate();
    }

    /**
     * 返回阴影颜色
     *
     * @return
     */
    public int getDarkColor() {
        return mDarkColor;
    }

    /**
     * 设置阴影颜色
     *
     * @param color
     */
    public void setDarkColor(int color) {
        this.mDarkColor = color;
        invalidate();
    }

    /**
     * 返回焦点框边框颜色
     *
     * @return
     */
    public int getFocusColor() {
        return mFocusColor;
    }

    /**
     * 设置焦点框边框颜色
     *
     * @param color
     */
    public void setFocusColor(int color) {
        this.mFocusColor = color;
        invalidate();
    }

    /**
     * 返回焦点框边框绘制宽度
     *
     * @return
     */
    public float getStrokWidth() {
        return mBorderWidth;
    }

    /**
     * 设置焦点边框宽度
     *
     * @param width
     */
    public void setStrokWidth(float width) {
        this.mBorderWidth = width;
        invalidate();
    }

    /**
     * Sets focus style <br/><br/>
     * <p/>Sets the style of focus view, you can sets it as rectangle or circle.
     *
     * @param style
     * @see com.opensource.imagecroper.widget.FocusView.Style
     */
    public void setFocusStyle(Style style) {
        this.mStyle = style;
        invalidate();
    }

    /**
     * Gets the style of focus view.
     *
     * @return
     */
    public Style getFocusStyle() {
        return mStyle;
    }

    /**
     * The style enum of focus view
     */
    public static enum Style {
        RECTANGLE(0), CIRCLE(1);

        private int value = -1;

        private Style(int value) {
            this.value = value;
        }

        public int value() {
            return this.value;
        }

        public Style valueOf(int value) {
            switch (value) {
                case 0:
                    return RECTANGLE;
                case 1:
                    return CIRCLE;
                default:
                    return RECTANGLE;
            }
        }

    }

    /**
     * 初始化
     * @param context Context对象
     */
    private void init(Context context) {
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        final int screenMinSize = dm.widthPixels > dm.heightPixels ? dm.heightPixels : dm.widthPixels;
        mFocusWidth = screenMinSize / 3 * 2;
        mFocusHeight = screenMinSize / 3 * 2;
    }
}
