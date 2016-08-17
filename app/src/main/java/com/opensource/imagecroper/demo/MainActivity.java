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

package com.opensource.imagecroper.demo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.opensource.imagecroper.CroperActivity;
import com.opensource.imagecroper.CroperConfig;
import com.opensource.imagecroper.demo.util.Util;
import com.opensource.imagecroper.util.LogUtil;

import java.io.File;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_PICK_PHOTO = 0x100;
    private static final int REQUEST_TAKE_PHOTO = 0x101;
    private static final int REQUEST_CUT_PHOTO = 0x102;


    private ImageView mIvImage;

    private File mPicFolder;
    private File mPicFile;
    private File mAvatarFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_crop_from_gallery).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                pickPhoto();
            }
        });

        findViewById(R.id.btn_crop_from_camera).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        mIvImage = (ImageView) findViewById(R.id.iv_image);

        try {
            mPicFolder = new File(Util.getApplicationFolder("ImageCroper"));
            mPicFolder.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "创建目录失败", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_PHOTO: //选取图片
                if(resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    if(uri != null) {
                        cutPhoto(uri, 200);
                    }
                }
                break;
            case REQUEST_TAKE_PHOTO: //拍照
                if(resultCode == RESULT_OK) {
                    if(mPicFile != null && mPicFile.exists()) {
                        cutPhoto(Uri.fromFile(mPicFile), 200);
                    }
                }
                break;
            case REQUEST_CUT_PHOTO: //剪切图片
                if(resultCode == RESULT_OK) {
                    Bitmap bm = data.getExtras().getParcelable("data");
                    if(null != bm) {
//                        mIvImage.setImageBitmap(BitmapUtils.toRoundCorner(bm, 2));
                        mIvImage.setImageBitmap(bm);
                    }
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 从图库中选取照片
     */
    private void pickPhoto() {
        Intent i = new Intent(Intent.ACTION_PICK);
//		i.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        i.setType("image/*");
        startActivityForResult(i, REQUEST_PICK_PHOTO);
    }

    /**
     * 调用相机拍照
     */
    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(null == mPicFile) {
            mPicFile = new File(Util.createImageFilename(mPicFolder.getPath()));
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPicFile));
        LogUtil.i(TAG, mPicFile.getAbsolutePath());
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    /**
     * 调用系统的图片编辑工具剪切图片
     * @param uri source data
     * @param size
     */
    private void cutPhoto(Uri uri, int size) {
//        Intent intent = new Intent("com.android.camera.action.CROP");
        Intent intent = new Intent(this, CroperActivity.class);
        intent.setDataAndType(uri, "image/*");
        intent.putExtra(CroperConfig.EXTRA_CIRCLE_CROP, true);
        intent.putExtra(CroperConfig.EXTRA_SCALE_UP_IF_NEEDED, true);

        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", size);
        intent.putExtra("outputY", size);
        //设置是否返回data数据
        intent.putExtra("return-data", true);
        if(null == mAvatarFile) {
            mAvatarFile = new File(Util.createImageFilename(mPicFolder.getPath()));
        }
        //设置输出文件
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mAvatarFile));
        startActivityForResult(intent, REQUEST_CUT_PHOTO);
    }

}
