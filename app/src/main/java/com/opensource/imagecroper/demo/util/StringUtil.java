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

package com.opensource.imagecroper.demo.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Use:
 * Created by yinglovezhuzhu@gmail.com on 2014-06-06.
 */
public class StringUtil {
    private StringUtil() {}

    public static boolean isEmpty(String str) {
        return str == null || "".equals(str.trim());
    }

    public static boolean isEqual(String str1, String str2) {
        if(str1 == null && str2 == null) {
            return true;
        } else if(str1 == null || str2 == null) {
        	return false;
        }
        return str1.equals(str2);
    }

    /**
     * 删除所有在startStr和endStr之间的字符串，包括startStr和endStr,即删除闭区间［startStr，endStr］
     * @param sb
     * @param startStr
     * @param endStr
     */
    public static void deleteAllIn(StringBuilder sb, String startStr, String endStr) {
        int startIndex = 0;
        int endIndex = 0;
        while((startIndex = sb.indexOf(startStr)) >= 0 && (endIndex = sb.indexOf(endStr)) >= 0) {
            sb.delete(startIndex, endIndex + endStr.length());
        }
    }

    /**
     * 根据相对／绝对路径获取文件名
     * @param path
     * @return
     */
    public static String getFileName(String path) {
        return path.substring(path.lastIndexOf("/") + 1, path.length());
    }

    /**
     * 获取字符串两个字符串之间的字符（第一个）
     * @param source
     * @param start
     * @param end
     * @return
     */
    public static String getStringIn(String source, String start, String end) {
        return source.substring(source.indexOf(start) + start.length(), source.indexOf(end));
    }
    
    public static boolean isNumberic(String str) {
        final Pattern p = Pattern.compile("[0-9]*");
        final Matcher m = p.matcher(str);
        return m.matches();
    }
    
    /**
     * 输入的字符串是否为Email
     * @param str
     * @return
     */
    public static boolean isEmail(String str) {
    	if(StringUtil.isEmpty(str)) {
    		return false;
    	}
        String check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        final Pattern p = Pattern.compile(check);
        final Matcher m = p.matcher(str);
        return m.matches();
    }
    
    /**
     * 获取字符串的长度（一个英文字符长度为1,一个中文字符长度为2）
     * @param s
     * @return
     */
    public static int getLength(String s) {
        if(StringUtil.isEmpty(s)) {
        	return 0;
        }
        String chinese = "[\u4e00-\u9fa5]"; //中文
        String emoji = ""; //emoji字符
    	int strLength = 0;
        String [] sArray = s.split(""); //把传入的字符窜分割成单个字符的数组
        for (String string : sArray) { //逐个字符进行计算，每个中文字符长度为2，否则为1
        	if(StringUtil.isEmpty(string)) {
        		continue;
        	}
            if (string.matches(chinese)) { //判断是否为中文字符
                strLength += 2; //中文字符，计数加2
            } else {
                strLength += 1; //其他字符，计数加1
            }
		}
        return strLength;
    }
}
