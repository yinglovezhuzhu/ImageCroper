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
import android.view.View;

public class FocusView extends View {

	private String tag = FocusView.class.getSimpleName();
	
//	private int mFocusLeft = 0;
//	private int mFocusTop = 0;
//	private int mFocusRight = 0;
//	private int mFocusBottom = 0;

    private RectF mFocusRect = new RectF();
	
	private int mHideColor = Color.argb(0xAF, 0x00, 0x00, 0x00);
	
	private int mFocusColor = Color.argb(0xFF, 0x80, 0x80, 0x80);
	
	private Paint mPaint = new Paint();
	
	private int mFocusWidth = 400;
	
	private float mStrokWidth = 3.0f;
	
	private PointF mFocusMidPoint = new PointF();

    private Style mStyle = Style.CIRCLE;
	
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

        //计算出焦点框的中点的坐标和上、下、左、右边的x或y的值
        mFocusMidPoint.set((getRight() - getLeft()) / 2, (getBottom() - getTop()) / 2);
        mFocusRect.left = mFocusMidPoint.x - mFocusWidth / 2;
        mFocusRect.top = mFocusMidPoint.y - mFocusWidth / 2;
        mFocusRect.right = mFocusMidPoint.x + mFocusWidth / 2;
        mFocusRect.bottom = mFocusMidPoint.y + mFocusWidth / 2;

        mPaint.setColor(mFocusColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokWidth);
        mPaint.setAntiAlias(true);
        Path focusPath = new Path();
        if(Style.RECTANGLE == mStyle) {
            focusPath.addRect(mFocusRect, Path.Direction.CCW);

            canvas.save();
            canvas.clipRect(getLeft(), getTop(), getRight(), getBottom());
            canvas.clipPath(focusPath, Region.Op.DIFFERENCE);
            canvas.drawColor(Color.argb(0xAA, 0x0, 0x0, 0x0));
            canvas.restore();

        } else if(Style.CIRCLE == mStyle) {
            float radius = Math.min((mFocusRect.right - mFocusRect.left) / 2,
                    (mFocusRect.bottom - mFocusRect.top) / 2);
            focusPath.addCircle(mFocusMidPoint.x, mFocusMidPoint.y, radius, Path.Direction.CCW);

            canvas.save();
            canvas.clipRect(getLeft(), getTop(), getRight(), getBottom());
            canvas.clipPath(focusPath, Region.Op.DIFFERENCE);
            canvas.drawColor(Color.argb(0xAA, 0x0, 0x0, 0x0));
            canvas.restore();

        }
        canvas.drawPath(focusPath, mPaint); //绘制焦点框
        focusPath.reset();
	}

    /**
     * 获取焦点框的位置信息
     * @return
     */
    public RectF getFocusRect() {
        return mFocusRect;
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
	public int getDarkColor() {
		return mHideColor;
	}
	
	/**
	 * 设置阴影颜色
	 * @param color
	 */
	public void setDarkColor(int color) {
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

    /**
     * Sets focus style <br/><br/>
     * <p/>Sets the style of focus view, you can sets it as rectangle or circle.
     *
     * @see com.opensource.imagecroper.widget.FocusView.Style
     *
     * @param style
     */
    public void setFocusStyle(Style style) {
        this.mStyle = style;
        postInvalidate();
    }

    /**
     * Gets the style of focus view.
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
}
