package jp.meridiani.apps.wificonnect.activity;

import java.util.List;

import jp.meridiani.apps.wificonnect.Constants;
import jp.meridiani.apps.wificonnect.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
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
import android.widget.ListView;
import android.widget.ToggleButton;

public class EditActivity extends Activity {
	private static final String SAVE_SELECTED_SSID = "save_selected_ssid";
	private static final String SAVE_SHOW_TOAST = "save_show_toast";

	private String mSelectedSSID;
	private ListView mWifiListView;
	private ToggleButton mWifiButton;
	private CheckBox mShowToastCheckbox;
	private Button mSelectButton;
	private Button mCancelButton;
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
		mWifiListView = (ListView)findViewById(R.id.SSIDList);
		mWifiButton = (ToggleButton)findViewById(R.id.wifi_button);
		mShowToastCheckbox = (CheckBox)findViewById(R.id.showtoast);
		mSelectButton = (Button)findViewById(R.id.select_button);
		mCancelButton = (Button)findViewById(R.id.cancel_button);

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

	}

	@Override
	protected void onResume() {
		super.onResume();

		mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (!WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
					return;
				}
				int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
				switch (state) {
					case WifiManager.WIFI_STATE_ENABLED:
					case WifiManager.WIFI_STATE_DISABLED:
						updateWifiList();
				}
			}
		};

		getApplicationContext().registerReceiver(mBroadcastReceiver, WIFI_STATE_CHANGED);

		updateWifiList();

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

	private void updateWifiList() {
		WifiManager wifi = getWifiManager();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_single_choice);

		int selPos = 0;
		if (wifi.isWifiEnabled()) {
			List<WifiConfiguration>wifiConfList = wifi.getConfiguredNetworks();
			if (wifiConfList != null) {
				for ( WifiConfiguration wifiConf : wifiConfList) {
					adapter.add(wifiConf.SSID);
					if (mSelectedSSID.equals(wifiConf.SSID)) {
						selPos = adapter.getCount() - 1;
					}
				}
			}
		}


		ListView wifiListView = (ListView)findViewById(R.id.SSIDList);
		if (wifiListView == null) {
			return;
		}
		wifiListView.setAdapter(adapter);
		wifiListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		wifiListView.setItemChecked(selPos, true);
		wifiListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mSelectedSSID = (String)parent.getAdapter().getItem(position);
			}
		});
	}
}
