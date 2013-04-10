package jp.meridiani.apps.wificonnect.activity;

import jp.meridiani.apps.wificonnect.R;
import android.app.Activity;
import android.os.Bundle;

public class MainScreenActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main_screen);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}
}
