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

/**
 * CropView,
 *
 * Create by yinglovezhuzhu@gmail.com on 2014-09-18
 */
public class CropView extends FrameLayout {

    private static final String TAG = "CropView";

	/** 拖动模式 */
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

    /** 当前模式 **/
	private int mMode = MODE_NONE;

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
		LogUtil.w(TAG, "Fuces width = " + mFocusView.getFocusWidth());
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
                mMode = MODE_NONE;
                mImageMatrix.getValues(mImageMatrixValues);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mMode = MODE_NONE;
                mImageMatrix.getValues(mImageMatrixValues);
//                Log.i(TAG, "Image MSCALE_X = " + mImageMatrixValues[0] + "; MSKEW_X = " + mImageMatrixValues[1] + "; MTRANS_X = " + mImageMatrixValues[2]
//                        + "; \nMSCALE_Y = " + mImageMatrixValues[4] + "; MSKEW_Y = " + mImageMatrixValues[3] + "; MTRANS_Y = " + mImageMatrixValues[5]
//                        + "; \nMPERSP_0 = " + mImageMatrixValues[6] + "; MPERSP_1 = " + mImageMatrixValues[7] + "; MPERSP_2 = " + mImageMatrixValues[8]);
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
//			可以通过0 + (mFocusView.getFocusLeft() - mImageMatrixValues[2])获得真实的坐标（图片是从0开始的），
//			但是这个还是缩放的，别忘了除以缩放比例
			int left = (int)((mFocusView.getFocusLeft() - mImageMatrixValues[2]) / mImageMatrixValues[0]);
			int top = (int)((mFocusView.getFocusTop() - mImageMatrixValues[5]) / mImageMatrixValues[4]);
			int right = (int) ((mFocusView.getFocusRight() - mImageMatrixValues[2]) / mImageMatrixValues[0]);
			int bottom = (int) ((mFocusView.getFocusBottom() - mImageMatrixValues[5]) / mImageMatrixValues[4]);
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
		mImageMatrix.getValues(mImageMatrixValues);
		int bitmapLeft = (int) mImageMatrixValues[2];
		int bitmapTop = (int) mImageMatrixValues[5];
		int bitmapRight = (int) (mBitmap.getWidth() * mImageMatrixValues[0] - mImageMatrixValues[2]);
		int bitmapBottom = (int) (mBitmap.getHeight() * mImageMatrixValues[4] - mImageMatrixValues[5]);
		if(bitmapLeft > mFocusView.getFocusLeft()) {
			left += (bitmapLeft - mFocusView.getFocusLeft()) / mImageMatrixValues[0];
		}
		if(bitmapTop > mFocusView.getFocusTop()) {
			top += (bitmapTop - mFocusView.getFocusTop()) / mImageMatrixValues[4];
		}
		if(bitmapRight < mFocusView.getFocusRight()) {
			right -= (mFocusView.getFocusRight() - bitmapRight) / mImageMatrixValues[0];
		}
		if(bitmapBottom < mFocusView.getFocusBottom()) {
			bottom -= (mFocusView.getFocusBottom() - bitmapBottom) / mImageMatrixValues[4];
		}
	}

    /**
     * onTouch中的MotionEvent.ACTION_DOWN
     * @param event
     */
	private void actionDown( MotionEvent event) {
		mImageView.setScaleType(ScaleType.MATRIX);
		mImageMatrix.set(mImageView.getImageMatrix());
		mSavedMatrix.set(mImageMatrix);
		mStartPoint.set(event.getX(), event.getY());
		mMode = MODE_DRAG;
	}
	
	private void actionPointerDown(MotionEvent event) {
		mOldDist = spacing(event);
		if (mOldDist > 0f) {
			mSavedMatrix.set(mImageMatrix);
			midPoint(mZoomPoint, event);
//			mZoomPoint.set(mFocusView.getFocusMidPoint());
			mMiniScale = (float) mFocusView.getFocusWidth() / Math.min(mBitmap.getWidth(), mBitmap.getHeight());
			mMode = MODE_ZOOM;
		}
	}



	
	private void actionMove(MotionEvent event) {
        switch (mMode) {
            case MODE_DRAG:
                mImageMatrix.set(mSavedMatrix);
                mImageMatrix.getValues(mImageMatrixValues);
                float transX = event.getX() - mStartPoint.x;
                float transY = event.getY() - mStartPoint.y;
                float leftLimit = mFocusView.getFocusLeft() - mImageMatrixValues[2];
                float topLimit = mFocusView.getFocusTop() - mImageMatrixValues[5];
                float rightLimit = mFocusView.getFocusRight() - (mBitmap.getWidth() * mImageMatrixValues[0] + mImageMatrixValues[2]);
                float bottomLimit = mFocusView.getFocusBottom() - (mBitmap.getHeight() * mImageMatrixValues[0] + mImageMatrixValues[5]);
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
                break;
            case MODE_ZOOM:
                mImageMatrix.set(mSavedMatrix);
                mImageMatrix.getValues(mImageMatrixValues);
                float newDist = spacing(event);
                if (newDist > 0f) { //距离大于0才进行操作
                    mTempMatrix.setValues(mImageMatrixValues);
                    float scale = newDist / mOldDist;
                    if(mImageMatrixValues[0] * scale < mMiniScale) {
                        scale = mMiniScale / mImageMatrixValues[0];
                    }
                    mTempMatrix.postScale(scale, scale, mZoomPoint.x, mZoomPoint.y);
                    mTempMatrix.getValues(mTempMatrixValues);
                    if(mTempMatrixValues[2] > mFocusView.getFocusLeft()) {
                        Log.w(TAG, "Out of left");
                        mZoomPoint.x = (mFocusView.getFocusLeft() - mImageMatrixValues[2] * scale) / (1 - scale);
                    }
                    if(mTempMatrixValues[5] > mFocusView.getFocusTop()) {
                        Log.w(TAG, "Out of top");
                        mZoomPoint.y = (mFocusView.getFocusTop() - mImageMatrixValues[5] * scale) / (1 - scale);
                    }
                    if(mTempMatrixValues[2] + mBitmap.getWidth() * mTempMatrixValues[0] < mFocusView.getFocusRight()) {
                        Log.w(TAG, "Out of right");
                        mZoomPoint.x = (mFocusView.getFocusRight() - (mImageMatrixValues[2] + mBitmap.getWidth() * mImageMatrixValues[0]) * scale) / (1 - scale);
                    }

                    if(mTempMatrixValues[5] + mBitmap.getHeight() * mTempMatrixValues[4] < mFocusView.getFocusBottom()) {
                        Log.w(TAG, "Out of bottom");
                        mZoomPoint.y = (mFocusView.getFocusBottom() - (mImageMatrixValues[5] + mBitmap.getHeight() * mImageMatrixValues[4]) * scale) / (1 - scale);
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