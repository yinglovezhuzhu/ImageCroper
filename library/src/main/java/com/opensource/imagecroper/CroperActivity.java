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
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Window;

import com.opensource.imagecroper.widget.CropView;

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

//        Intent intent = getIntent();
//        Bundle extras = intent.getExtras();
//
//        if (extras != null) {
//            if (extras.getString(CroperConfig.EXTRA_CIRCLE_CROP) != null) {
//                mCircleCrop = true;
//                mAspectX = 1;
//                mAspectY = 1;
//                //TODO Add some code to make fomat is png when crop a circle image.
//            }
//            mSaveUri = extras.getParcelable(CroperConfig.EXTRA_OUTPUT);
//            if (mSaveUri != null) {
//                String outputFormatString = extras.getString(CroperConfig.EXTRA_OUTPUT_FORMAT);
//                if (outputFormatString != null) {
//                    mOutputFormat = Bitmap.CompressFormat.valueOf(outputFormatString);
//                }
//            }
//            mBitmap = extras.getParcelable(CroperConfig.EXTRA_DATA);
//            mAspectX = extras.getInt(CroperConfig.EXTRA_ASPECT_X);
//            mAspectY = extras.getInt(CroperConfig.EXTRA_ASPECT_Y);
//            mOutputX = extras.getInt(CroperConfig.EXTRA_OUTPUT_X);
//            mOutputY = extras.getInt(CroperConfig.EXTRA_OUTPUT_Y);
//            mScale = extras.getBoolean(CroperConfig.EXTRA_SCALE, true);
//            mScaleUp = extras.getBoolean(CroperConfig.EXTRA_SCALE_UP_IF_NEEDED, true);
//        }
//
//        if (mBitmap == null) {
//            // Create a MediaItem representing the URI.
//            mInputUri = intent.getData();
//
//            File imageFile = Util.parseUriToFile(this, mInputUri);
//
//
//            if(null != imageFile) {
//                String imagePath = imageFile.getPath();
//                Log.i(TAG, "Parse Uri to file, file path : " + imagePath);
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inJustDecodeBounds = true;
//                options.inJustDecodeBounds = false;
//                options.inSampleSize = 4;
//                mBitmap = BitmapFactory.decodeFile(imagePath, options);
//
//            }
//        }
//
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
	

}
