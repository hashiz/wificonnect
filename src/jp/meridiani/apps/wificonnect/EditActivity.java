package jp.meridiani.apps.wificonnect;

import java.util.List;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import jp.meridiani.apps.wificonnect.Constants;

public class EditActivity extends Activity implements OnItemSelectedListener, OnItemClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_edit);

		Bundle bundle = getIntent().getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);

		String ssid = "";
		if (bundle != null) {
			ssid = bundle.getString(Constants.BUNDLE_AP_SSID);
		}

		WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		List<WifiConfiguration>wifiConfList = wifi.getConfiguredNetworks();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
							android.R.layout.simple_list_item_single_choice);
		int selPos = 0;
		for ( WifiConfiguration wifiConf : wifiConfList) {
			adapter.add(wifiConf.SSID);
			if (ssid.equals(wifiConf.SSID)) {
				selPos = adapter.getCount() - 1;
			}
		}

		ListView wifiListView = (ListView)findViewById(R.id.SSIDList);
		wifiListView.setAdapter(adapter);
		wifiListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		wifiListView.setItemChecked(selPos, true);
		wifiListView.setOnItemSelectedListener(this);
		wifiListView.setOnItemClickListener(this);
	}

	@Override
    public void finish()
    {
		ListView wifiListView = (ListView)findViewById(R.id.SSIDList);
		int selPos = wifiListView.getCheckedItemPosition();
		String ssid = (String)wifiListView.getAdapter().getItem(selPos);

        if (ssid != null && ssid.length() > 0)
        {
            Intent resultIntent = new Intent();
            Bundle resultBundle = new Bundle();
            resultBundle.putString(Constants.BUNDLE_AP_SSID, ssid);

            resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_STRING_BLURB, ssid);
            resultIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE, resultBundle);

            setResult(RESULT_OK, resultIntent);
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
}
