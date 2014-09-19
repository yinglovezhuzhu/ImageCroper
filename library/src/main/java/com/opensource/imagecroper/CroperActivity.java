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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;

import com.opensource.imagecroper.util.Util;
import com.opensource.imagecroper.widget.CropView;

import java.io.File;

public class CroperActivity extends Activity {

	private static final String TAG = "CroperActivity";

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
    private Bitmap.CompressFormat mOutputFormat = Bitmap.CompressFormat.JPEG; // only
    // used
    // with
    // mSaveUri

    boolean mSaving; // Whether the "save" button is already clicked.

    private ContentResolver mContentResolver;

    private Bitmap mBitmap;
    private Uri mInputUri;


	private CropView mCropView = null;
	
	private DisplayMetrics mMetrics = new DisplayMetrics();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_croper);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {
            if (extras.getString(CroperConfig.EXTRA_CIRCLE_CROP) != null) {
                mCircleCrop = true;
                mAspectX = 1;
                mAspectY = 1;
                //TODO Add some code to make fomat is png when crop a circle image.
            }
            mSaveUri = extras.getParcelable(CroperConfig.EXTRA_OUTPUT);
            if (mSaveUri != null) {
                String outputFormatString = extras.getString(CroperConfig.EXTRA_OUTPUT_FORMAT);
                if (outputFormatString != null) {
                    mOutputFormat = Bitmap.CompressFormat.valueOf(outputFormatString);
                }
            }
            mBitmap = extras.getParcelable(CroperConfig.EXTRA_DATA);
            mAspectX = extras.getInt(CroperConfig.EXTRA_ASPECT_X);
            mAspectY = extras.getInt(CroperConfig.EXTRA_ASPECT_Y);
            mOutputX = extras.getInt(CroperConfig.EXTRA_OUTPUT_X);
            mOutputY = extras.getInt(CroperConfig.EXTRA_OUTPUT_Y);
            mScale = extras.getBoolean(CroperConfig.EXTRA_SCALE, true);
            mScaleUp = extras.getBoolean(CroperConfig.EXTRA_SCALE_UP_IF_NEEDED, true);
        }

        if (mBitmap == null) {
            // Create a MediaItem representing the URI.
            mInputUri = intent.getData();

            File imageFile = Util.parseUriToFile(this, mInputUri);


            if(null != imageFile) {
                String imagePath = imageFile.getPath();
                Log.i(TAG, "Parse Uri to file, file path : " + imagePath);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                options.inJustDecodeBounds = false;
                options.inSampleSize = 4;
                mBitmap = BitmapFactory.decodeFile(imagePath, options);

            }
        }

//        if (mBitmap == null) {
//            Log.e(TAG, "Cannot load bitmap, exiting.");
//            finish();
//            return;
//        }
//
//        // Make UI fullscreen.
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        findViewById(R.id.discard).setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                setResult(RESULT_CANCELED);
//                finish();
//            }
//        });
//
//        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                onSaveClicked();
//            }
//        });
//
//
//        mImageView.setImageBitmapResetBase(mBitmap, true);
//
//        makeCropView();
	}

    /**
     * Save button clicked
     */
    private void onSaveClicked() {
//        if (mSaving)
//            return;
//
//        if (mImageView.getCropView() == null) {
//            return;
//        }
//
//        mSaving = true;
//        mImageView.setSaving(true);
//
//        Rect r = mImageView.getCropRect();
//
//        int width = r.width(); // CR: final == happy panda!
//        int height = r.height();
//
//        // If we are circle cropping, we want alpha channel, which is the
//        // third param here.
//        Bitmap croppedImage = Bitmap.createBitmap(width, height, mCircleCrop ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
//        {
//            Canvas canvas = new Canvas(croppedImage);
//            Rect dstRect = new Rect(0, 0, width, height);
//            canvas.drawBitmap(mBitmap, r, dstRect, null);
//        }
//
//        if (mCircleCrop) {
//            // OK, so what's all this about?
//            // Bitmaps are inherently rectangular but we want to return
//            // something that's basically a circle. So we fill in the
//            // area around the circle with alpha. Note the all important
//            // PortDuff.Mode.CLEARes.
//            Canvas c = new Canvas(croppedImage);
//            Path p = new Path();
//            p.addCircle(width / 2F, height / 2F, width / 2F, Path.Direction.CW);
//            c.clipPath(p, Region.Op.DIFFERENCE);
//            c.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
//        }
//
//        // If the output is required to a specific size then scale or fill.
//        if (mOutputX != 0 && mOutputY != 0) {
//            if (mScale) {
//                // Scale the image to the required dimensions.
//                Bitmap old = croppedImage;
//                croppedImage = Util.transform(new Matrix(), croppedImage, mOutputX, mOutputY, mScaleUp);
//                if (old != croppedImage) {
//                    old.recycle();
//                }
//            } else {
//
//                /*
//                 * Don't scale the image crop it to the size requested. Create
//                 * an new image with the cropped image in the center and the
//                 * extra space filled.
//                 */
//
//                // Don't scale the image but instead fill it so it's the
//                // required dimension
//                Bitmap b = Bitmap.createBitmap(mOutputX, mOutputY, Bitmap.Config.RGB_565);
//                Canvas canvas = new Canvas(b);
//
//                Rect srcRect = mImageView.getCropRect();
//                Rect dstRect = new Rect(0, 0, mOutputX, mOutputY);
//
//                int dx = (srcRect.width() - dstRect.width()) / 2;
//                int dy = (srcRect.height() - dstRect.height()) / 2;
//
//                // If the srcRect is too big, use the center part of it.
//                srcRect.inset(Math.max(0, dx), Math.max(0, dy));
//
//                // If the dstRect is too big, use the center part of it.
//                dstRect.inset(Math.max(0, -dx), Math.max(0, -dy));
//
//                // Draw the cropped bitmap in the center.
//                canvas.drawBitmap(mBitmap, srcRect, dstRect, null);
//
//                // Set the cropped bitmap as the new bitmap.
//                croppedImage.recycle();
//                croppedImage = b;
//            }
//        }
//
//        // Return the cropped image directly or save it to the specified URI.
//        Bundle myExtras = getIntent().getExtras();
//        if (myExtras != null && (myExtras.getParcelable(CropConfig.EXTRA_DATA) != null || myExtras.getBoolean(CropConfig.EXTRA_RETURN_DATA))) {
//            Bundle extras = new Bundle();
//            extras.putParcelable(CropConfig.EXTRA_DATA, croppedImage);
//            setResult(RESULT_OK, (new Intent()).setAction(CropConfig.ACTION_INLINE_DATA).putExtras(extras));
//            finish();
//        } else {
//            final Bitmap b = croppedImage;
//            final Runnable save = new Runnable() {
//                public void run() {
//                    saveOutput(b);
//                }
//            };
//            Util.startBackgroundJob(this, null, getResources().getString(R.string.saving_image), save, mHandler);
//        }
    }

    /**
     * Save the cropped image to file<br/>
     * <br/><p/>If the output file has been set, it won't insert the image message inout ContentProvider<br/>
     * @param croppedImage
     */
    private void saveOutput(Bitmap croppedImage) {
//        if (mSaveUri != null) {
//            OutputStream outputStream = null;
//            try {
//                outputStream = mContentResolver.openOutputStream(mSaveUri);
//                if (outputStream != null) {
//                    croppedImage.compress(mOutputFormat, 75, outputStream);
//                }
//                // TODO ExifInterface write
//            } catch (IOException ex) {
//                Log.e(TAG, "Cannot open file: " + mSaveUri, ex);
//            } finally {
//                Util.closeSilently(outputStream);
//            }
//            Bundle extras = new Bundle();
//            setResult(RESULT_OK, new Intent(mSaveUri.toString()).putExtras(extras));
//        } else {
//            Bundle extras = new Bundle();
//            extras.putString(CropConfig.EXTRA_RECT, mImageView.getCropRect().toString());
//            File oldFile = Util.parseUriToFile(this, mInputUri);
//            File directory = new File(oldFile.getParent());
//            int x = 0;
//            String fileName = oldFile.getName();
//            fileName = fileName.substring(0, fileName.lastIndexOf("."));
//
//            // Try file-1.jpg, file-2.jpg, ... until we find a filename
//            // which
//            // does not exist yet.
//            while (true) {
//                x += 1;
//                String candidate = directory.toString() + "/" + fileName + "-" + x + ".jpg";
//                boolean exists = (new File(candidate)).exists();
//                if (!exists) { // CR: inline the expression for exists
//                    // here--it's clear enough.
//                    break;
//                }
//            }
//
//            String title = fileName + "-" + x;
//            String finalFileName = title + ".jpg";
//            int[] degree = new int[1];
//            Double latitude = null;
//            Double longitude = null;
//            Uri newUri = Util.addImage(mContentResolver, title,
//                    System.currentTimeMillis() / 1000, System.currentTimeMillis(), latitude,
//                    longitude, directory.toString(), finalFileName,
//                    croppedImage, null, degree);
//            if (newUri != null) {
//                setResult(RESULT_OK, new Intent().setAction(newUri.toString()).putExtras(extras));
//            } else {
//                setResult(RESULT_OK, new Intent().setAction(null));
//            }
//        }
//        croppedImage.recycle();
//        finish();
    }

    /**
     * Create a HightlightView to show how to crop the image.
     */
    private void makeCropView() {
//        HighlightView hv = new HighlightView(mImageView);
//
//        int width = mBitmap.getWidth();
//        int height = mBitmap.getHeight();
//
//        Rect imageRect = new Rect(0, 0, width, height);
//
//        // CR: sentences!
//        // make the default size about 4/5 of the width or height
//        int cropWidth = Math.min(width, height) * 4 / 5;
//        int cropHeight = cropWidth;
//
//        if (mAspectX != 0 && mAspectY != 0) {
//            if (mAspectX > mAspectY) {
//                cropHeight = cropWidth * mAspectY / mAspectX;
//            } else {
//                cropWidth = cropHeight * mAspectX / mAspectY;
//            }
//        }
//
//        int x = (width - cropWidth) / 2;
//        int y = (height - cropHeight) / 2;
//
//        RectF cropRect = new RectF(x, y, x + cropWidth, y + cropHeight);
//        hv.setup(mImageView.getImageMatrix(), imageRect, cropRect, mCircleCrop, mAspectX != 0 && mAspectY != 0);
//        hv.setFocus(true);
//        mImageView.setCropView(hv);
    }

}
