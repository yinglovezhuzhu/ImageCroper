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
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.opensource.imagecroper.util.LogUtil;

/**
 * CropView,
 *
 * Create by yinglovezhuzhu@gmail.com on 2014-09-18
 */
public class CropView extends FrameLayout {

    private static final String TAG = "CropView";

	/** 拖拽模式 */
	private static final int MODE_DRAG = 1;
	/** 缩放模式 */
	private static final int MODE_ZOOM = 2;
	/** 没有模式 */
	private static final int MODE_NONE = 3;
    /** 显示图片的控件 **/
	private ImageView mImageView = null;
	/** 选取框控件 **/
	private FocusView mFocusView = null;


    /** 图片的Matrix，用来移动，缩放图片 **/
	private Matrix mImageMatrix = new Matrix();
    /** 用来保存Matrix状态的 **/
	private Matrix mSavedMatrix = new Matrix();
    /** 临时的Matrix，用来校正缩放中间点计算 **/
    private Matrix mTempMatrix = new Matrix();

    /** Pointer的落点，这个用于MODE_DRAG **/
	private PointF mStartPoint = new PointF();
    /** 图像缩放的中心点，通常是两点的中间点，但是图片超出FocusView的范围时会进行矫正，矫正后的不一定是中间点 **/
	private PointF mZoomPoint = new PointF();
    /** 两指移动过程中的上个状态的距离 **/
	private float mOldDist = 0f;

    /** 图片的Matrix的数据值 **/
	private float [] mImageMatrixValues = new float[9];
    /** 图片临时Matrix数据值 **/
    private float [] mTempMatrixValues = new float[9];

    /** 图片Bitmap对象 **/
	private Bitmap mBitmap = null;
    /** 图片的最小缩放比例，这个用来保证图片最小能够铺满FocusView的中间窗口 **/
	private float mMiniScale = 1f;

    private RectF mFocusRect = new RectF();

    private int mBitmapWidth = 0;
    private int mBitmapHeight = 0;

    /** 当前模式 **/
	private int mMode = MODE_NONE;

    private boolean mSaving = false;

//	Matrix的Value是一个3x3的矩阵，文档中的Matrix获取数据的方法是void getValues(float[] values)，对于Matrix内字段的顺序
//	并没有很明确的说明，经过测试发现他的顺序是这样的
//	MSCALE_X	MSKEW_X		MTRANS_X
//	MSKEW_Y		MSCALE_Y	MTRANS_Y
//	MPERSP_0	MPERSP_1	MPERSP_2	
	
	public CropView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	public CropView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public CropView(Context context) {
		super(context);
		initView(context);
	}

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mSaving || null == mBitmap) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                actionDown(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                actionPointerDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                actionMove(event);
                break;
            case MotionEvent.ACTION_UP:
                mMode = MODE_NONE;
                mImageMatrix.getValues(mImageMatrixValues);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mMode = MODE_NONE;
                mImageMatrix.getValues(mImageMatrixValues);
                break;
        }
        mImageView.setImageMatrix(mImageMatrix);
        return true;
    }

    /**
     * 设置需要裁剪的Bitmap
     *
     * @param bitmap
     * @throws java.lang.IllegalStateException
     */
    public void setImageBitmap(Bitmap bitmap) {
        if(null == bitmap) {
            throw new IllegalStateException("The bitmap sets to should not be null");
        }
        this.mBitmap = bitmap;
        if(!mBitmap.isRecycled()) {
            mImageView.setScaleType(ScaleType.FIT_CENTER);
            mImageView.setImageBitmap(bitmap);
            mBitmapWidth = mBitmap.getWidth();
            mBitmapHeight = mBitmap.getHeight();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        LogUtil.w(TAG, changed + "<>" + left + "<>" + top + "<>" + right + "<>" + bottom);
        if(changed) {
            int viewWidth = right - left;
            int viewHeight = bottom - top;
            mFocusRect = mFocusView.getFocusRect();
            mImageView.setScaleType(ScaleType.MATRIX);
            mImageMatrix.set(mImageView.getImageMatrix());
            mSavedMatrix.set(mImageMatrix);
            //适配焦点框的缩放比例（图片的最小边不小于焦点框的最小边）
            float fitFocusScale = getScale(mBitmapWidth, mBitmapHeight,
                    mFocusView.getFocusWidth(), mFocusView.getFocusHeight(), true);
            mMiniScale = fitFocusScale;//最小的缩放比例就是适配焦点框的缩放比例
            //适配显示图片的ImageView的缩放比例（图片至少有一边是铺满屏幕的显示的情形）
            float fitViewScale = getScale(mBitmapWidth, mBitmapHeight, viewWidth, viewHeight, false);
            //确定最终的缩放比例
            // 在适配焦点框的前提下适配显示图片的ImageView，
            //方案：首先满足适配焦点框，如果还能适配显示图片的ImageView，则适配它，即取缩放比例的最大值。
            //采取这种方案的原因：有可能图片很长或者很高，适配了ImageView的时候可能会宽/高已经小于焦点框的
            //宽/高，所以采取这种方案。
            float scale = fitViewScale > fitFocusScale ? fitViewScale : fitFocusScale;
            //图像中点为中心进行缩放
            mImageMatrix.setScale(scale, scale, mBitmapWidth / 2, mBitmapHeight / 2);
            mImageMatrix.getValues(mImageMatrixValues); //获取缩放后的mImageMatrix的值
            PointF focusMinPoint = mFocusView.getFocusMidPoint(); //获取焦点框中间点的坐标（相对于CropView）
            //X轴方向的位移
            float transX = focusMinPoint.x - (mImageMatrixValues[2] + mBitmapWidth * mImageMatrixValues[0] / 2);
            //Y轴方向的位移
            float transY = focusMinPoint.y - (mImageMatrixValues[5] + mBitmapHeight * mImageMatrixValues[4] / 2);

//            mImageMatrix.setTranslate(transX, transY);
            //这里很奇怪，如果直接用mImageMatrix.setTranslate(transX, transY),mImageMatrix的值会重置，
            //所以只能采用下面这种赋值的方法来进行位移
            //注意：上面的缩放最好用setScale的方法，因为缩放同时会改变位移，使用setScale可以用getValue这个方法
            //轻松获取缩放后的位移等值。
            mImageMatrixValues[2] = mImageMatrixValues[2] + transX;
            mImageMatrixValues[5] = mImageMatrixValues[5] + transY;
            mImageMatrix.setValues(mImageMatrixValues);
            mImageView.setImageMatrix(mImageMatrix);
        }
    }

    /**
     * Gets the bitmap crop rect
     *
     * @return
     */
    public Rect getCropRect() {
        Rect rect = new Rect(0, 0, mBitmapWidth, mBitmapHeight);
        if(!mBitmap.isRecycled() && null != mImageView.getDrawable()) {
            mImageView.setScaleType(ScaleType.MATRIX);
            mImageMatrix.set(mImageView.getImageMatrix());
            mImageMatrix.getValues(mImageMatrixValues);
            mFocusRect.set(mFocusView.getFocusRect());
            //根据缩放比例和移动的位置，计算出焦点框在mBitmap中的真实位置
            int left = (int)((mFocusRect.left - mImageMatrixValues[2]) / mImageMatrixValues[0]);
            int top = (int)((mFocusRect.top - mImageMatrixValues[5]) / mImageMatrixValues[4]);
            int right = (int) ((mFocusRect.right - mImageMatrixValues[2]) / mImageMatrixValues[0]);
            int bottom = (int) ((mFocusRect.bottom - mImageMatrixValues[5]) / mImageMatrixValues[4]);
            //进行一次矫正，防止范围超出mBitmap（虽然拖动和缩放都进行了控制，但计算过程中可能会出现误差）
            left = left < 0 ? 0 : left;
            top = top < 0 ? 0 : top;
            right = right > mBitmapWidth ? mBitmapWidth : right;
            bottom = bottom > mBitmapHeight ? mBitmapHeight : bottom;
            rect.set(left, top, right, bottom);
        }
        return rect;
    }

    /**
     * Sets it is saving
     * @param isSaving
     */
    public void setSaving(boolean isSaving) {
        this.mSaving = isSaving;
    }

    /**
     * Gets it is saving or not.
     * @return
     */
    public boolean isSaving() {
        return mSaving;
    }

    /**
     * Sets circle crop
     *
     * @param circleCrop
     */
    public void setCircleCrop(boolean circleCrop) {
        mFocusView.setFocusStyle(circleCrop ? FocusView.Style.CIRCLE : FocusView.Style.RECTANGLE);
    }

    /**
     * onTouch中的MotionEvent.ACTION_DOWN执行的操作
     * @param event
     */
	private void actionDown( MotionEvent event) {
		mImageView.setScaleType(ScaleType.MATRIX);
		mImageMatrix.set(mImageView.getImageMatrix());
		mSavedMatrix.set(mImageMatrix);
		mStartPoint.set(event.getX(), event.getY());
        mFocusRect.set(mFocusView.getFocusRect());
		mMode = MODE_DRAG;
	}

    /**
     * onTouch中的MotionEvent.ACTION_POINTER_DOWN执行的操作
     * @param event
     */
	private void actionPointerDown(MotionEvent event) {
		mOldDist = spacing(event);
		if (mOldDist > 0f) {
			mSavedMatrix.set(mImageMatrix);
			midPoint(mZoomPoint, event);
			mMiniScale = (float) mFocusView.getFocusWidth() / Math.min(mBitmapWidth, mBitmapHeight);
			mMode = MODE_ZOOM;
		}
	}

    /**
     * onTouch中的MotionEvent.ACTION_MOVE执行的操作
     * @param event
     */
	private void actionMove(MotionEvent event) {
        switch (mMode) {
            case MODE_DRAG: //拖拽模式
                mImageMatrix.set(mSavedMatrix);
                mImageMatrix.getValues(mImageMatrixValues);
                float transX = event.getX() - mStartPoint.x;
                float transY = event.getY() - mStartPoint.y;
                float leftLimit = mFocusRect.left - mImageMatrixValues[2];
                float topLimit = mFocusRect.top - mImageMatrixValues[5];
                float rightLimit = mFocusRect.right
                        - (mBitmapWidth * mImageMatrixValues[0] + mImageMatrixValues[2]);
                float bottomLimit = mFocusRect.bottom
                        - (mBitmapHeight * mImageMatrixValues[0] + mImageMatrixValues[5]);
                //这里进行移动的位置矫正，保证焦点框选中的区域全部在图片上。
                //因为已经计算好了最小的缩放比例，保证了图片的任何一边不会比焦点框小，所以只需要确保各边的位置即可
                transX = transX > leftLimit ? leftLimit : transX;
                transY = transY > topLimit ? topLimit : transY;
                transX = transX < rightLimit ? rightLimit : transX;
                transY = transY < bottomLimit ? bottomLimit : transY;
                mImageMatrix.postTranslate(transX, transY);
                break;
            case MODE_ZOOM: //缩放模式
                mImageMatrix.set(mSavedMatrix);
                mImageMatrix.getValues(mImageMatrixValues);
                float newDist = spacing(event);
                if (newDist > 0f) { //距离大于0才进行操作
                    mTempMatrix.setValues(mImageMatrixValues);
                    float scale = newDist / mOldDist;
                    //这里是矫正缩放比例，保证缩放后的缩放比例不小于最小缩放比例
                    if(mImageMatrixValues[0] * scale < mMiniScale) {
                        scale = mMiniScale / mImageMatrixValues[0];
                    }
                    //用mTempMatrix来模拟缩放，然后计算得出矫正的数据（主要是计算矫正后的缩放点的坐标）
                    mTempMatrix.postScale(scale, scale, mZoomPoint.x, mZoomPoint.y);
                    mTempMatrix.getValues(mTempMatrixValues);
                    if(mTempMatrixValues[2] > mFocusRect.left) {
                        Log.w(TAG, "Out of left");
                        mZoomPoint.x = (mFocusRect.left - mImageMatrixValues[2] * scale) / (1 - scale);
                    }
                    if(mTempMatrixValues[5] > mFocusRect.top) {
                        Log.w(TAG, "Out of top");
                        mZoomPoint.y = (mFocusRect.top - mImageMatrixValues[5] * scale) / (1 - scale);
                    }
                    if(mTempMatrixValues[2] + mBitmapWidth * mTempMatrixValues[0] < mFocusRect.right) {
                        Log.w(TAG, "Out of right");
                        mZoomPoint.x = (mFocusRect.right - (mImageMatrixValues[2]
                                + mBitmapWidth * mImageMatrixValues[0]) * scale) / (1 - scale);
                    }

                    if(mTempMatrixValues[5] + mBitmapHeight * mTempMatrixValues[4] < mFocusRect.bottom) {
                        Log.w(TAG, "Out of bottom");
                        mZoomPoint.y = (mFocusRect.bottom - (mImageMatrixValues[5]
                                + mBitmapHeight * mImageMatrixValues[4]) * scale) / (1 - scale);
                    }

                    mImageMatrix.postScale(scale, scale, mZoomPoint.x, mZoomPoint.y);

                }
                break;
            case MODE_NONE:
                // Do nothing
                break;
            default:
                break;
        }
	}

    /**
     * 初始化View
     * @param context
     */
    private void initView(Context context) {

        mImageView = new ImageView(context);
        addView(mImageView, new LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        mFocusView = new FocusView(context);
        addView(mFocusView, new LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
//        mFocusRect = mFocusView.getFocusRect();
    }

    /**
     * 计算边界缩放比例
     *
     * @param bitmapWidth
     * @param bitmapHeight
     * @param minWidth
     * @param minHeight
     * @param isMinScale 是否最小比例，true 最小缩放比例， false 最大缩放比例
     * @return
     */
    private float getScale(int bitmapWidth, int bitmapHeight, int minWidth, int minHeight, boolean isMinScale) {
        float scale = 1f;
        float scaleX = (float) minWidth / bitmapWidth;
        float scaleY = (float) minHeight / bitmapHeight;
        if(isMinScale) {
            scale = scaleX > scaleY ? scaleX : scaleY;
        } else {
            scale = scaleX < scaleY ? scaleX : scaleY;
        }
        return scale;
    }

    /**
     * 计算两点之间的距离
     * @param event
     * @return
     */
	private float spacing(MotionEvent event) {
        if(event.getPointerCount() < 2) {
            return 0f; //如果触摸点小于2，那么直接返回
        }
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
	}

    /**
     * 计算Touch事件中两个手指触摸的情况下，两个手指的中点
     * @param point
     * @param event
     */
	private void midPoint(PointF point, MotionEvent event) {
        if(event.getPointerCount() < 2) {
            return; //如果触摸点小于2，那么直接返回
        }
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

}