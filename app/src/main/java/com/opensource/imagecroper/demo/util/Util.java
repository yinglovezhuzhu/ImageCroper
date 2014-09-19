/*
 * Copyright (C) 2014 The Android Open Source Project.
 *
 *        yinglovezhuzhu@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.opensource.imagecroper.demo.util;

import android.os.Environment;

import com.opensource.imagecroper.demo.Config;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Use:
 * Created by yinglovezhuzhu@gmail.com on 2014-07-24.
 */
public class Util {

    private Util() {
    }

    /**
     * 存储是否可用
     * @return
     */
    public static boolean hasStorage() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 根据文件夹名称生成一个应用数据目录
     * @param folderName
     * @return
     */
    public static String getApplicationFolder(String folderName) {
        if(StringUtil.isEmpty(folderName)) {
            return null;
        }
        if(hasStorage()) {
            File storage = Environment.getExternalStorageDirectory();
            File file =  new File(storage, folderName);
            if(!file.exists()) {
                file.mkdirs();
            }
            return file.getAbsolutePath();
        }
        return null;
    }

    /**
     * 根据系统时间产生一个在指定目录下的图片文件名
     *
     * @param folder
     * @return
     */
    public static String createImageFilename(String folder) {
        return createFilename(folder, Config.IMAGE_PREFIX, Config.IMAGE_SUFFIX);
    }

    /**
     * 根据系统时间产生一个在指定目录下的视频文件名
     *
     * @param folder
     * @return
     */
    public static String createVideoFilename(String folder) {
        return createFilename(folder, Config.VIDEO_PREFIX, Config.VIDEO_SUFFIX);
    }

    /**
     * 根据系统时间、前缀、后缀产生一个文件名
     *
     * @param folder
     * @param prefix
     * @param suffix
     * @return
     */
    private static String createFilename(String folder, String prefix, String suffix) {
        File file = new File(folder);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
//        String filename = prefix + System.currentTimeMillis() + suffix;
        SimpleDateFormat dateFormat = new SimpleDateFormat(Config.DATE_FORMAT_MILLISECOND);
        String filename = prefix + dateFormat.format(new Date(System.currentTimeMillis())) + suffix;
        return new File(folder, filename).getAbsolutePath();
    }
}
