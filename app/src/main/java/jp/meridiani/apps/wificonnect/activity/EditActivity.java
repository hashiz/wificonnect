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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.ToggleButton;

public class EditActivity extends Activity implements OnItemSelectedListener, OnItemClickListener {

	private String mSelectedSSID;
	private ListView mWifiListView;
	private ToggleButton mWifiButton;
	private Button mSelectButton;
	private Button mCancelButton;
	private boolean mCanceled;
	private static final IntentFilter WIFI_STATE_CHANGED = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
	private BroadcastReceiver mBroadcastReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// receive intent and extra data
		Intent intent = getIntent();
		if (!com.twofortyfouram.locale.Intent.ACTION_EDIT_SETTING.equals(intent.getAction())) {
			super.finish();
			return;
		}

		Bundle bundle = getIntent().getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);

		mSelectedSSID = null;
		if (bundle != null) {
			mSelectedSSID = bundle.getString(Constants.BUNDLE_AP_SSID);
		}
		if (mSelectedSSID == null) {
			mSelectedSSID = "";
		}

		mCanceled = false;

		// set view
		setContentView(R.layout.activity_edit);
		mWifiListView = (ListView)findViewById(R.id.SSIDList);
		mWifiButton = (ToggleButton)findViewById(R.id.wifi_button);
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

		mSelectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onSelectClick((Button)v);
			}
		});
		mCancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onCancelClick((Button)v);
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
				onWifiStateChanged(intent);
			}

		};

		getApplicationContext().registerReceiver(mBroadcastReceiver,WIFI_STATE_CHANGED);

		updateWifiList();

	};
	
	@Override
	protected void onPause() {
		super.onPause();
		
		getApplicationContext().unregisterReceiver(mBroadcastReceiver);
	};

	@Override
    public void finish()
    {
		int selPos = -1;
		if (mWifiListView.getCount() > 0) {
			selPos = mWifiListView.getCheckedItemPosition();
		}
		String ssid = null;
		if (selPos >= 0) {
			ssid = (String)mWifiListView.getAdapter().getItem(selPos);
		}

        Intent resultIntent = new Intent();
        if (! mCanceled && ssid != null && ssid.length() > 0) {
            Bundle resultBundle = new Bundle();
            resultBundle.putString(Constants.BUNDLE_AP_SSID, ssid);

            resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB, ssid);
            resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE, resultBundle);

            setResult(RESULT_OK, resultIntent);
        }
        else {
            setResult(RESULT_CANCELED, resultIntent);
        }
    	super.finish();
    }

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
		wifiListView.setOnItemSelectedListener(this);
		wifiListView.setOnItemClickListener(this);
	}

	private void onSelectClick(Button b) {
		mCanceled = false;
		finish();
	}

	private void onCancelClick(Button b) {
		mCanceled = true;
		finish();
	}

	private void onWifiStateChanged(Intent intent) {
		int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
		switch (state) {
		case WifiManager.WIFI_STATE_ENABLED:
		case WifiManager.WIFI_STATE_DISABLED:
			updateWifiList();
		}
	}
}
