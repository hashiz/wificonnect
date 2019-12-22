package jp.meridiani.apps.wificonnect.activity;

import java.util.ArrayList;
import java.util.List;

import jp.meridiani.apps.wificonnect.Constants;
import jp.meridiani.apps.wificonnect.R;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class EditActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {
	private static final String SAVE_SELECTED_SSID = "save_selected_ssid";
	private static final String SAVE_SHOW_TOAST = "save_show_toast";
	private static final int REQUEST_PERMISSIONS = 1;
	private static final int PICK_WIFI_NETWORK = 2;

	private String mSelectedSSID;
	private ListView mWifiListView;
	private ToggleButton mWifiButton;
	private CheckBox mShowToastCheckbox;
	private Button mSelectButton;
	private Button mCancelButton;
	private Button mPickSsidButton;
	private EditText mDesireSsidText;
	private boolean mCanceled;
	private static final IntentFilter WIFI_STATE_CHANGED = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
	private BroadcastReceiver mBroadcastReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSelectedSSID = null;
		boolean showToast = true;

		if (savedInstanceState == null) {
			// receive intent and extra data
			Intent intent = getIntent();
			if (!com.twofortyfouram.locale.api.Intent.ACTION_EDIT_SETTING.equals(intent.getAction())) {
				super.finish();
				return;
			}

			Bundle bundle = getIntent().getBundleExtra(com.twofortyfouram.locale.api.Intent.EXTRA_BUNDLE);

			if (bundle != null) {
				mSelectedSSID = bundle.getString(Constants.BUNDLE_AP_SSID);
				showToast = bundle.getBoolean(Constants.BUNDLE_SHOWTOAST, true);
			}
		}
		else {
			mSelectedSSID = savedInstanceState.getString(SAVE_SELECTED_SSID);
			showToast = savedInstanceState.getBoolean(SAVE_SHOW_TOAST, true);
		}
		if (mSelectedSSID == null)
			mSelectedSSID = "";


		mCanceled = false;

		// set view
		setContentView(R.layout.activity_edit);
		mWifiButton = (ToggleButton)findViewById(R.id.wifi_button);
		mShowToastCheckbox = (CheckBox)findViewById(R.id.showtoast);
		mSelectButton = (Button)findViewById(R.id.select_button);
		mCancelButton = (Button)findViewById(R.id.cancel_button);
		mPickSsidButton = (Button)findViewById(R.id.pick_ssid_button);
		mDesireSsidText = (EditText)findViewById(R.id.desire_ssid_text);

		WifiManager wifi = getWifiManager();

		mWifiButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
										 boolean isChecked) {
				setWifiEnable(isChecked);
			}
		});

		mWifiButton.setChecked(wifi.isWifiEnabled());
		mShowToastCheckbox.setChecked(showToast);

		mSelectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCanceled = false;
				finish();
			}
		});
		mCancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCanceled = true;
				finish();
			}
		});

		mPickSsidButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK), PICK_WIFI_NETWORK);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		String[] requirePermissions = {
				Manifest.permission.ACCESS_WIFI_STATE,
				Manifest.permission.CHANGE_WIFI_STATE,
				Manifest.permission.ACCESS_NETWORK_STATE,
				Manifest.permission.ACCESS_COARSE_LOCATION
		};

		ArrayList<String> requestPermissions = new ArrayList<String>();

		for (String permission : requirePermissions) {
			if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
				requestPermissions.add(permission);
			}
		}
		if (!requestPermissions.isEmpty()) {
			ActivityCompat.requestPermissions(
					this,
					requestPermissions.toArray(new String[requestPermissions.size()]),
					REQUEST_PERMISSIONS);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		getApplicationContext().unregisterReceiver(mBroadcastReceiver);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(SAVE_SELECTED_SSID, mSelectedSSID);
		outState.putBoolean(SAVE_SHOW_TOAST, mShowToastCheckbox.isChecked());
	}

	@Override
    public void finish()
    {
        Intent resultIntent = new Intent();
        if (! mCanceled && mSelectedSSID != null && mSelectedSSID.length() > 0) {
            Bundle resultBundle = new Bundle();
            resultBundle.putString(Constants.BUNDLE_AP_SSID, mSelectedSSID);
			resultBundle.putBoolean(Constants.BUNDLE_SHOWTOAST, mShowToastCheckbox.isChecked());
            resultIntent.putExtra(com.twofortyfouram.locale.api.Intent.EXTRA_STRING_BLURB, mSelectedSSID);
            resultIntent.putExtra(com.twofortyfouram.locale.api.Intent.EXTRA_BUNDLE, resultBundle);

            setResult(RESULT_OK, resultIntent);
        }
        else {
            setResult(RESULT_CANCELED, resultIntent);
        }
    	super.finish();
    }


	private void setWifiEnable(boolean enabled) {
		getWifiManager().setWifiEnabled(enabled);
	}

	private WifiManager getWifiManager() {
		return (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grants) {
		switch (requestCode) {
			case REQUEST_PERMISSIONS:
				break;
			default:
				return;
		}
		for (int i = 0; i < permissions.length; i++) {
			if (grants[i] != PackageManager.PERMISSION_GRANTED) {
				Toast.makeText(this, permissions[i]+" require", Toast.LENGTH_SHORT);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case PICK_WIFI_NETWORK:
				break;
			default:
				return;
		}
		if (resultCode == RESULT_OK) {
			Bundle b;
			if ((b = data.getExtras()) != null) {
				for (String key : b.keySet()) {
					Toast.makeText(getApplicationContext(), key, Toast.LENGTH_LONG).show();
				}
			}
		}
	}
}
