package com.opensource.imagecroper;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.opensource.imagecroper.widget.CropView;

public class CroperActivity extends Activity {

	private String tag = CroperActivity.class.getSimpleName();

	private CropView mCropView = null;
	
	private DisplayMetrics mMetrics = new DisplayMetrics();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_croper);
		
		getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
		
		mCropView = (CropView) findViewById(R.id.cv_photo);
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(getResources().openRawResource(R.raw.pic1), null, options);
		options.inJustDecodeBounds = false;
		options.inSampleSize = options.outWidth / mMetrics.widthPixels;
		Bitmap bitmap = BitmapFactory.decodeStream(getResources().openRawResource(R.raw.pic1), null, options);
		mCropView.setImageBitmap(bitmap);
	}
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case R.id.action_cut:
//			LogUtil.e(tag, "ImageCut");
//			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//				try {
//					String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/image.jpg";
//					mCropView.cutImageBitmap(path);
//					Toast.makeText(this, "已保存至" + path, Toast.LENGTH_SHORT).show();
//				} catch (FileNotFoundException ex){
//					ex.printStackTrace();
//					Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
//				}
//			} else {
//				Toast.makeText(this, "请插入Sdcard", Toast.LENGTH_SHORT).show();
//			}
//			break;
//		default:
//			break;
//		}
//		return super.onOptionsItemSelected(item);
//	}
	
}
