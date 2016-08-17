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

package com.opensource.imagecroper;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.opensource.imagecroper.util.CropUtil;
import com.opensource.imagecroper.widget.CropView;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class CropActivity extends MonitoredActivity {

	private static final String TAG = "CropActivity";

    public static final String EXTRA_CIRCLE_CROP = "circleCrop";
    public static final String EXTRA_OUTPUT = "output";
    public static final String EXTRA_OUTPUT_FORMAT = "outputFormat";
    public static final String EXTRA_DATA = "data";
    public static final String EXTRA_RETURN_DATA = "return-data";
    public static final String EXTRA_ASPECT_X = "aspectX";
    public static final String EXTRA_ASPECT_Y = "aspectY";
    public static final String EXTRA_OUTPUT_X = "outputX";
    public static final String EXTRA_OUTPUT_Y = "outputY";
    public static final String EXTRA_SCALE = "scale";
    public static final String EXTRA_SCALE_UP_IF_NEEDED = "scaleUpIfNeeded";
    public static final String EXTRA_RECT = "rect";


    public static final String ACTION_INLINE_DATA = "inline-data";


    public static final int CROP_MSG = 10;
    public static final int CROP_MSG_INTERNAL = 100;


    private final Handler mHandler = new Handler();

    private boolean mCircleCrop = false;
    // These options specifiy the output image size and whether we should
    // scale the output to fit it (or just crop it).
    private int mAspectX;
    private int mAspectY; // CR: two definitions per line == sad panda.
    private int mOutputX;
    private int mOutputY;
    private boolean mScale;
    private boolean mScaleUp = true;
    private Uri mSaveUri = null;
    // These are various options can be specified in the intent.
    private Bitmap.CompressFormat mOutputFormat = Bitmap.CompressFormat.JPEG; // only used with mSaveUri

    boolean mSaving; // Whether the "save" button is already clicked.

    private ContentResolver mContentResolver;

    private Bitmap mBitmap;
    private Uri mInputUri;


	private CropView mCropView = null;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_croper);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {
            mCircleCrop = extras.getBoolean(EXTRA_CIRCLE_CROP, false);
            if (mCircleCrop) {
                mAspectX = 1;
                mAspectY = 1;
                //TODO Add some code to make format is png when crop a circle image.
            }
            mSaveUri = extras.getParcelable(EXTRA_OUTPUT);
            if (mSaveUri != null) {
                String outputFormatString = extras.getString(EXTRA_OUTPUT_FORMAT);
                if (outputFormatString != null) {
                    mOutputFormat = Bitmap.CompressFormat.valueOf(outputFormatString);
                }
            }
            mBitmap = extras.getParcelable(EXTRA_DATA);
            mAspectX = extras.getInt(EXTRA_ASPECT_X);
            mAspectY = extras.getInt(EXTRA_ASPECT_Y);
            mOutputX = extras.getInt(EXTRA_OUTPUT_X);
            mOutputY = extras.getInt(EXTRA_OUTPUT_Y);
            mScale = extras.getBoolean(EXTRA_SCALE, true);
            mScaleUp = extras.getBoolean(EXTRA_SCALE_UP_IF_NEEDED, true);
        }

        if (mBitmap == null) {
            // Create a MediaItem representing the URI.
            mInputUri = intent.getData();

            File imageFile = CropUtil.parseUriToFile(this, mInputUri);


            if(null != imageFile) {
                String imagePath = imageFile.getPath();
                Log.i(TAG, "Parse Uri to file, file path : " + imagePath);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imagePath, options);
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                options.inSampleSize = calculateInSampleSize(options,
                        displayMetrics.widthPixels, displayMetrics.heightPixels);
                options.inJustDecodeBounds = false;
                mBitmap = BitmapFactory.decodeFile(imagePath, options);
            }
        }

        if (mBitmap == null) {
            Log.e(TAG, "Cannot load bitmap, exiting.");
            finish();
            return;
        }

        // Make UI fullscreen.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        findViewById(R.id.btn_croper_cancel).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        findViewById(R.id.btn_croper_save).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onSaveClicked();
            }
        });

        findViewById(R.id.ibtn_rotate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBitmap = CropUtil.rotate(mBitmap, 90);
                mCropView.setImageBitmap(mBitmap);
            }
        });

        initCropView();

        mContentResolver = getContentResolver();
	}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != mBitmap && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    /**
     * Calculate an inSampleSize for use in a {@link android.graphics.BitmapFactory.Options} object when decoding
     * bitmaps using the decode* methods from {@link android.graphics.BitmapFactory}. This implementation calculates
     * the closest inSampleSize that will result in the final decoded bitmap having a width and
     * height equal to or larger than the requested width and height. This implementation does not
     * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
     * results in a larger bitmap which isn't as useful for caching purposes.
     *
     * @param options   An options object with out* params already populated (run through a decode*
     *                  method with inJustDecodeBounds==true
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int width = options.outWidth;
        final int height = options.outHeight;

        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = width / reqWidth;
            } else {
                inSampleSize = height / reqHeight;
            }
        }
        return inSampleSize;
    }

    /**
     * Save button clicked
     */
    private void onSaveClicked() {
        if (mSaving) {
            return;
        }

        if (null == mCropView) {
            return;
        }

        mSaving = true;
        mCropView.setSaving(true);

        Rect rect = mCropView.getCropRect();

        int width = rect.width(); // CR: final == happy panda!
        int height = rect.height();

        // If we are circle cropping, we want alpha channel, which is the
        // third param here.
        Bitmap croppedImage = Bitmap.createBitmap(width, height, mCircleCrop ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        {
            Canvas canvas = new Canvas(croppedImage);
            Rect dstRect = new Rect(0, 0, width, height);
            canvas.drawBitmap(mBitmap, rect, dstRect, null);
        }

        if (mCircleCrop) {
            // OK, so what's all this about?
            // Bitmaps are inherently rectangular but we want to return
            // something that's basically a circle. So we fill in the
            // area around the circle with alpha. Note the all important
            // PortDuff.Mode.CLEARes.
            Canvas c = new Canvas(croppedImage);
            Path p = new Path();
            p.addCircle(width / 2F, height / 2F, width / 2F, Path.Direction.CW);
            c.clipPath(p, Region.Op.DIFFERENCE);
            c.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
        }

        // If the output is required to a specific size then scale or fill.
        if (mOutputX != 0 && mOutputY != 0) {
            if (mScale) {
                // Scale the image to the required dimensions.
                croppedImage = CropUtil.transform(new Matrix(), croppedImage, mOutputX, mOutputY, mScaleUp);
            } else {

                /*
                 * Don't scale the image crop it to the size requested. Create
                 * an new image with the cropped image in the center and the
                 * extra space filled.
                 */

                // Don't scale the image but instead fill it so it's the
                // required dimension
                Bitmap b = Bitmap.createBitmap(mOutputX, mOutputY, Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(b);

                Rect srcRect = mCropView.getCropRect();
                Rect dstRect = new Rect(0, 0, mOutputX, mOutputY);

                int dx = (srcRect.width() - dstRect.width()) / 2;
                int dy = (srcRect.height() - dstRect.height()) / 2;

                // If the srcRect is too big, use the center part of it.
                srcRect.inset(Math.max(0, dx), Math.max(0, dy));

                // If the dstRect is too big, use the center part of it.
                dstRect.inset(Math.max(0, -dx), Math.max(0, -dy));

                // Draw the cropped bitmap in the center.
                canvas.drawBitmap(mBitmap, srcRect, dstRect, null);

                // Set the cropped bitmap as the new bitmap.
                croppedImage = b;
            }
        }

        // Return the cropped image directly or save it to the specified URI.
        Bundle myExtras = getIntent().getExtras();
        if (myExtras != null && (myExtras.getParcelable(EXTRA_DATA) != null || myExtras.getBoolean(EXTRA_RETURN_DATA))) {
            Bundle extras = new Bundle();
            extras.putParcelable(EXTRA_DATA, croppedImage);
            setResult(RESULT_OK, (new Intent()).setAction(ACTION_INLINE_DATA).putExtras(extras));
            finish();
        } else {
            final Bitmap b = croppedImage;
            final Runnable save = new Runnable() {
                public void run() {
                    saveOutput(b);
                }
            };
            CropUtil.startBackgroundJob(this, null, getString(R.string.str_saving_image), save, mHandler);
        }
    }

    /**
     * Save the cropped image to file<br/>
     * <br/><p/>If the output file has been set, it won't insert the image message inout ContentProvider<br/>
     * @param croppedImage The bitmap to save to file
     */
    private void saveOutput(Bitmap croppedImage) {
        Bundle extras = new Bundle();
        extras.putString(EXTRA_RECT, mCropView.getCropRect().toString());
        Intent intent = new Intent();
        intent.putExtras(extras);
        if (mSaveUri == null) {
            File oldFile = CropUtil.parseUriToFile(this, mInputUri);
            File directory = new File(oldFile.getParent());
            int x = 0;
            String fileName = oldFile.getName();
            fileName = fileName.substring(0, fileName.lastIndexOf("."));

            // Try file-1.jpg, file-2.jpg, ... until we find a filename
            // which
            // does not exist yet.
            while (true) {
                x += 1;
                String candidate = directory.toString() + "/" + fileName + "-" + x + ".jpg";
                boolean exists = (new File(candidate)).exists();
                if (!exists) { // CR: inline the expression for exists
                    // here--it's clear enough.
                    break;
                }
            }

            String title = fileName + "-" + x;
            String finalFileName = title + ".jpg";
            int[] degree = new int[1];
            Double latitude = null;
            Double longitude = null;
            mSaveUri = CropUtil.addImage(mContentResolver, title,
                    System.currentTimeMillis() / 1000, System.currentTimeMillis(), latitude,
                    longitude, directory.toString(), finalFileName,
                    croppedImage, null, degree);
        }
        if(null != mSaveUri) {
            OutputStream outputStream = null;
            try {
                outputStream = mContentResolver.openOutputStream(mSaveUri);
                if (outputStream != null) {
                    croppedImage.compress(mOutputFormat, 100, outputStream);
                }
                // TODO ExifInterface write
            } catch (IOException ex) {
                Log.e(TAG, "Cannot open file: " + mSaveUri, ex);
            } finally {
                CropUtil.closeSilently(outputStream);
            }
        }
        if(null != croppedImage) {
            croppedImage.recycle();
        }
        intent.setData(mSaveUri);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Create a mCropView to show how to crop the image.
     */
    private void initCropView() {
        mCropView = (CropView) findViewById(R.id.cv_croper_image);
        mCropView.setCircleCrop(mCircleCrop);
//        mCropView.setCircleCrop(true);
        mCropView.setImageBitmap(mBitmap);
    }

}
