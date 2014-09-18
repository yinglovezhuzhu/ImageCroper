package com.opensource.imagecroper.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import com.opensource.imagecroper.util.LogUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


public class CropView extends FrameLayout {

	/** 拖动模式 */
	private static final int MODE_DRAG = 1;
	/** 缩放模式 */
	private static final int MODE_ZOOM = 2;
	/** 没有模式 */
	private static final int MODE_NONE = 3;

	private int mMode = MODE_NONE;

	private ImageView mImageView = null;
	
	private FocusView mFocusView = null;

	private Bitmap mBitmap = null;

    /** 图片的Matrix，用来移动，缩放图片 **/
	private Matrix mImageMatrix = new Matrix();
    /** 用来保存Matrix状态的 **/
	private Matrix mSavedMatrix = new Matrix();
    /** 临时的Matrix，用来校正缩放中间点计算 **/
    private Matrix mTempMatrix = new Matrix();

	private PointF mStartPoint = new PointF();
	private PointF mZoomPoint = new PointF();
	private float mOldDist = 1f;
	
	private float [] mMatrixValues = new float[9];
	
	private float mMiniScale = 1f;
	
	private String tag = CropView.class.getSimpleName();
	
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

	public void setImageBitmap(Bitmap bitmap) {
		this.mBitmap = bitmap;
		mImageView.setScaleType(ScaleType.FIT_CENTER);
		mImageView.setImageBitmap(bitmap);
	}

	private void initView(Context context) {
		
		mImageView = new ImageView(context);
		addView(mImageView, new LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		mFocusView = new FocusView(context);
		LogUtil.w(tag, "Fuces width = " + mFocusView.getFocusWidth());
		addView(mFocusView, new LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
	}

    @Override
    public boolean onTouchEvent(MotionEvent event) {
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
            case MotionEvent.ACTION_POINTER_UP:
                mMode = MODE_NONE;
                mImageMatrix.getValues(mMatrixValues);
                Log.i(tag, "Image MSCALE_X = " + mMatrixValues[0] + "; MSKEW_X = " + mMatrixValues[1] + "; MTRANS_X = " + mMatrixValues[2]
                        + "; \nMSCALE_Y = " + mMatrixValues[4] + "; MSKEW_Y = " + mMatrixValues[3] + "; MTRANS_Y = " + mMatrixValues[5]
                        + "; \nMPERSP_0 = " + mMatrixValues[6] + "; MPERSP_1 = " + mMatrixValues[7] + "; MPERSP_2 = " + mMatrixValues[8]);

                float [] values = new float[9];
                mTempMatrix.getValues(values);
                Log.i(tag, " temp MSCALE_X = " + values[0] + "; MSKEW_X = " + values[1] + "; MTRANS_X = " + values[2]
                        + "; \nMSCALE_Y = " + values[4] + "; MSKEW_Y = " + values[3] + "; MTRANS_Y = " + values[5]
                        + "; \nMPERSP_0 = " + values[6] + "; MPERSP_1 = " + values[7] + "; MPERSP_2 = " + values[8]);

                break;
        }
        mImageView.setImageMatrix(mImageMatrix);
        return true;
//        return super.onTouchEvent(event);
    }

    /**
	 * 保存剪切的
	 * @param path 图像保存位置
	 * @return 剪切的图像
	 */
	public Bitmap cutImageBitmap(String path) throws FileNotFoundException {
		if(mBitmap != null) {
//			焦点框内的图片为缩放的图片，起始坐标（相对屏幕）有可能小于零，
//			可以通过0 + (mFocusView.getFocusLeft() - mMatrixValues[2])获得真实的坐标（图片是从0开始的），
//			但是这个还是缩放的，别忘了除以缩放比例
			int left = (int)((mFocusView.getFocusLeft() - mMatrixValues[2]) / mMatrixValues[0]);
			int top = (int)((mFocusView.getFocusTop() - mMatrixValues[5]) / mMatrixValues[4]);
			int right = (int) ((mFocusView.getFocusRight() - mMatrixValues[2]) / mMatrixValues[0]);
			int bottom = (int) ((mFocusView.getFocusBottom() - mMatrixValues[5]) / mMatrixValues[4]);
			left = left < 0 ? 0 : left;
			top = top < 0 ? 0 : top;
			right = right > mBitmap.getWidth() ? mBitmap.getWidth() : right;
			bottom = bottom > mBitmap.getHeight() ? mBitmap.getHeight() : bottom;
			
//			correctSize(left, top, right, bottom);
			Bitmap bitmap = Bitmap.createBitmap(mBitmap, left, top, right - left, bottom - top);
			bitmap.compress(CompressFormat.JPEG, 100, new FileOutputStream(new File(path)));
			return bitmap;
		}
		return null;
	}
	
	/**
	 * 修正图片，获得真实的边界（防止焦点框不包含图片外部时出错）
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 */
	private void correctSize(int left, int top, int right, int bottom) {
		mImageMatrix.getValues(mMatrixValues);
		int bitmapLeft = (int) mMatrixValues[2];
		int bitmapTop = (int) mMatrixValues[5];
		int bitmapRight = (int) (mBitmap.getWidth() * mMatrixValues[0] - mMatrixValues[2]);
		int bitmapBottom = (int) (mBitmap.getHeight() * mMatrixValues[4] - mMatrixValues[5]);
		if(bitmapLeft > mFocusView.getFocusLeft()) {
			left += (bitmapLeft - mFocusView.getFocusLeft()) / mMatrixValues[0];
		}
		if(bitmapTop > mFocusView.getFocusTop()) {
			top += (bitmapTop - mFocusView.getFocusTop()) / mMatrixValues[4];
		}
		if(bitmapRight < mFocusView.getFocusRight()) {
			right -= (mFocusView.getFocusRight() - bitmapRight) / mMatrixValues[0];
		}
		if(bitmapBottom < mFocusView.getFocusBottom()) {
			bottom -= (mFocusView.getFocusBottom() - bitmapBottom) / mMatrixValues[4];
		}
	}
	
	private void actionDown( MotionEvent event) {
		mImageView.setScaleType(ScaleType.MATRIX);
		mImageMatrix.set(mImageView.getImageMatrix());
		mSavedMatrix.set(mImageMatrix);
		mStartPoint.set(event.getX(), event.getY());
		mMode = MODE_DRAG;
	}
	
	private void actionPointerDown(MotionEvent event) {
		mOldDist = spacing(event);
		if (mOldDist > 10f) {
			mSavedMatrix.set(mImageMatrix);
			midPoint(mZoomPoint, event);
//			mZoomPoint.set(mFocusView.getFocusMidPoint());
			mMiniScale = (float) mFocusView.getFocusWidth() / Math.min(mBitmap.getWidth(), mBitmap.getHeight());
			mMode = MODE_ZOOM;
		}
	}



	
	private void actionMove(MotionEvent event) {
		if (mMode == MODE_DRAG) {
			mImageMatrix.set(mSavedMatrix);
			float transX = event.getX() - mStartPoint.x;
			float transY = event.getY() - mStartPoint.y;
			mImageMatrix.getValues(mMatrixValues);
			float leftLimit = mFocusView.getFocusLeft() - mMatrixValues[2];
			float topLimit = mFocusView.getFocusTop() - mMatrixValues[5];
			float rightLimit = mFocusView.getFocusRight() - (mBitmap.getWidth() * mMatrixValues[0] + mMatrixValues[2]);
			float bottomLimit = mFocusView.getFocusBottom() - (mBitmap.getHeight() * mMatrixValues[0] + mMatrixValues[5]);
			if(transX > leftLimit) {
				transX = leftLimit;
			}
			if(transY > topLimit) {
				transY = topLimit;
			}
			if(transX < rightLimit) {
				transX = rightLimit;
			}
			if(transY < bottomLimit) {
				transY = bottomLimit;
			}
			mImageMatrix.postTranslate(transX, transY);
		} else if (mMode == MODE_ZOOM) {
			float newDist = spacing(event);
			if (newDist > 10f) {
				mImageMatrix.set(mSavedMatrix);
				mImageMatrix.getValues(mMatrixValues);
                mTempMatrix.setValues(mMatrixValues);
				float scale = newDist / mOldDist;
				if(mMatrixValues[0] * scale < mMiniScale) {
					scale = mMiniScale / mMatrixValues[0];
				}
				mTempMatrix.postScale(scale, scale, mZoomPoint.x, mZoomPoint.y);
                float [] values = new float[9];
                mTempMatrix.getValues(values);
                if(values[2] > mFocusView.getFocusLeft()) {
                    Log.w(tag, "Out of left");
                    mZoomPoint.x = (mFocusView.getFocusLeft() - mMatrixValues[2] * scale) / (1 - scale);
                }
                if(values[5] > mFocusView.getFocusTop()) {
                    Log.w(tag, "Out of top");
                    mZoomPoint.y = (mFocusView.getFocusTop() - mMatrixValues[5] * scale) / (1 - scale);
                }
                if(values[2] + mBitmap.getWidth() * values[0] < mFocusView.getFocusRight()) {
                    Log.w(tag, "Out of right");
                    mZoomPoint.x = (mFocusView.getFocusRight() - (mMatrixValues[2] + mBitmap.getWidth() * mMatrixValues[0]) * scale) / (1 - scale);
                }

                if(values[5] + mBitmap.getHeight() * values[4] < mFocusView.getFocusBottom()) {
                    Log.w(tag, "Out of bottom");
                    mZoomPoint.y = (mFocusView.getFocusBottom() - (mMatrixValues[5] + mBitmap.getHeight() * mMatrixValues[4]) * scale) / (1 - scale);
                }

				mImageMatrix.postScale(scale, scale, mZoomPoint.x, mZoomPoint.y);

			}
		}
	}

	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
//		如果在API8以下的版本使用，采用FloatMath.sqrt()会更快，但是在API8和以上版本，Math.sqrt()更快
//		原文：Use java.lang.Math#sqrt instead of android.util.FloatMath#sqrt() since it is faster as of API 8
//		return FloatMath.sqrt(x * x + y * y);
	}

	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}
}