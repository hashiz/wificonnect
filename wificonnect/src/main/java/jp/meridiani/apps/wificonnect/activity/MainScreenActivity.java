package jp.meridiani.apps.wificonnect.activity;

import jp.meridiani.apps.wificonnect.R;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.content.pm.PackageInfo;

public class MainScreenActivity extends Activity {
	Button mCloseButton;
	String mVersionName = "";
	int mVersionCode = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main_screen);
		try {
			PackageInfo pkgInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			mVersionName = pkgInfo.versionName;
			mVersionCode = pkgInfo.versionCode;
		}
		catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		mCloseButton = (Button)findViewById(R.id.close_button);
		mCloseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		TextView version = (TextView)findViewById(R.id.versionName);
		version.setText(String.format("%s(%d)", mVersionName, mVersionCode));
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}
}
