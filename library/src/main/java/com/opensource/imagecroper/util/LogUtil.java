package com.opensource.imagecroper.util;

import android.util.Log;

public class LogUtil {
	private static final boolean mIsPrint = true;
	
	public static void i(String tag, String msg) {
		if(mIsPrint) {
			Log.i(tag, msg);
		}
	}
	
	public static void e(String tag, String msg) {
		if(mIsPrint) {
			Log.e(tag, msg);
		}
	}
	
	public static void w(String tag, String msg) {
		if(mIsPrint) {
			Log.w(tag, msg);
		}
	}
}
