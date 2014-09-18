package com.opensource.imagecroper.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import com.opensource.imagecroper.util.LogUtil;

public class FocusView extends View {
	private String tag = FocusView.class.getSimpleName();
	
	private int mFocusLeft = 0;
	private int mFocusTop = 0;
	private int mFocusRight = 0;
	private int mFocusBottom = 0;
	
	private int mHideColor = Color.argb(0xAF, 0x00, 0x00, 0x00);
	
	private int mFocusColor = Color.argb(0xFF, 0x80, 0x80, 0x80);
	
	private Paint mPaint = new Paint();
	
	private int mFocusWidth = 400;
	
	private float mStrokWidth = 3.0f;
	
	private PointF mFocusMidPoint = new PointF();
	
	public FocusView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public FocusView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FocusView(Context context) {
		super(context);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		initData();
		mPaint.setColor(mFocusColor);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(mStrokWidth);
		canvas.drawRect(mFocusLeft, mFocusTop, mFocusRight, mFocusBottom, mPaint);	//绘制焦点框
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(mHideColor);
		canvas.drawRect(getLeft(), getTop(), getRight(), mFocusTop, mPaint);	//绘制焦点框上边阴影
		canvas.drawRect(getLeft(), mFocusTop, mFocusLeft, mFocusBottom + mStrokWidth / 2, mPaint);	//绘制焦点框左边阴影
		canvas.drawRect(mFocusRight + mStrokWidth / 2, mFocusTop, getRight(), mFocusBottom + mStrokWidth / 2, mPaint);	//绘制焦点框右边边阴影
		canvas.drawRect(getLeft(), mFocusBottom + mStrokWidth / 2, getRight(), getBottom(), mPaint);	//绘制焦点框下边阴影
//		mPaint.setStyle(Paint.Style.FILL);
//		mPaint.setColor(mHideColor);
//        canvas.drawRect(getLeft(), getTop(), getRight(), getBottom(), mPaint);
//		mPaint.setColor(mFocusColor);
//		mPaint.setStyle(Paint.Style.STROKE);
//		mPaint.setStrokeWidth(mStrokWidth);
//		canvas.drawRect(mFocusLeft, mFocusTop, mFocusRight, mFocusBottom, mPaint);	//绘制焦点框
//
//        mPaint.setColor(Color.argb(0xFF, 0xFF, 0, 0));
//		mPaint.setStyle(Paint.Style.FILL);
////        canvas.drawRect(mFocusLeft + mStrokWidth / 2, mFocusTop + mStrokWidth / 2, mFocusRight - mStrokWidth / 2, mFocusBottom - mStrokWidth / 2, mPaint);
//
//        Path p = new Path();
//        p.addCircle(mFocusMidPoint.x, mFocusMidPoint.y, mFocusWidth / 2, Path.Direction.CCW );
//        canvas.drawPath(p, mPaint);


	}
	
	private void initData() {
		LogUtil.i(tag, "View content+++++(" + getLeft() + ", " + getTop() + ", " 
				+ getRight() + ", " + getBottom() + ")");
		mFocusMidPoint.set((getRight() - getLeft()) / 2, (getBottom() - getTop()) / 2);
		mFocusLeft = (int) (mFocusMidPoint.x - mFocusWidth / 2);
		mFocusTop = (int) (mFocusMidPoint.y - mFocusWidth / 2);
		mFocusRight = (int) (mFocusMidPoint.x + mFocusWidth / 2);
		mFocusBottom = (int) (mFocusMidPoint.y + mFocusWidth / 2);
		LogUtil.i(tag, "Focus content=====(" + getFocusLeft() + ", " + getFocusTop() + ", "
				+ getFocusRight() + ", " + getFocusBottom() + ")");
	}

	/**
	 * 返回焦点框左边位置
	 * @return
	 */
	public int getFocusLeft() {
		return mFocusLeft;
	}

	/**
	 * 返回焦点框上边位置
	 * @return
	 */
	public int getFocusTop() {
		return mFocusTop;
	}

	/**
	 * 返回焦点框右边位置
	 * @return
	 */
	public int getFocusRight() {
		return mFocusRight;
	}

	/**
	 * 返回焦点框下边位置
	 * @return
	 */
	public int getFocusBottom() {
		return mFocusBottom;
	}

	/**
	 * 返回焦点框中间点坐标
	 * @return
	 */
	public PointF getFocusMidPoint() {
		return mFocusMidPoint;
	}

	/**
	 * 返回焦点框宽度
	 * @return
	 */
	public int getFocusWidth() {
		return mFocusWidth;
	}
	
	/**
	 * 设置焦点框的宽度
	 * @param width
	 */
	public void setFocusWidth(int width) {
		this.mFocusWidth = width;
		postInvalidate();
	}

	/**
	 * 返回阴影颜色
	 * @return
	 */
	public int getHideColor() {
		return mHideColor;
	}
	
	/**
	 * 设置阴影颜色
	 * @param color
	 */
	public void setHidColor(int color) {
		this.mHideColor = color;
		postInvalidate();
	}

	/**
	 * 返回焦点框边框颜色
	 * @return
	 */
	public int getFocusColor() {
		return mFocusColor;
	}
	
	/**
	 * 设置焦点框边框颜色
	 * @param color
	 */
	public void setFocusColor(int color) {
		this.mFocusColor = color;
		postInvalidate();
	}

	/**
	 * 返回焦点框边框绘制宽度
	 * @return
	 */
	public float getStrokWidth() {
		return mStrokWidth;
	}
	
	/**
	 * 设置焦点边框宽度
	 * @param width
	 */
	public void setStrokWidth(float width) {
		this.mStrokWidth = width;
		postInvalidate();
	}
	
}
