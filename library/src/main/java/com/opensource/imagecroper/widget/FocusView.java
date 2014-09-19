package com.opensource.imagecroper.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Region;
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

        initData();
	}

	public FocusView(Context context, AttributeSet attrs) {
		super(context, attrs);

        initData();
	}

	public FocusView(Context context) {
		super(context);

        initData();
	}


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

        canvas.save();
        canvas.clipRect(getLeft(), getTop(), getRight(), getBottom());
        canvas.clipRect(mFocusLeft, mFocusTop, mFocusRight, mFocusBottom, Region.Op.DIFFERENCE);

        canvas.drawColor(Color.argb(0xAA, 0x0, 0x0, 0x0));
        canvas.restore();

		mPaint.setColor(mFocusColor);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(mStrokWidth);
		canvas.drawRect(mFocusLeft, mFocusTop, mFocusRight, mFocusBottom, mPaint);	//绘制焦点框



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
