package jp.meridiani.apps.wificonnect.receiver;

import java.util.List;

import jp.meridiani.apps.wificonnect.Constants;
import jp.meridiani.apps.wificonnect.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

public class TimeoutReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(this.getClass().getName(), "onReceive");
    	String action = intent.getAction();
    	if (!Constants.ACTION_TIMEOUT.equals(action)) {
    		return;
    	}
    	String desireSSID = intent.getStringExtra(Constants.BUNDLE_SSID);
		boolean showToast = intent.getBooleanExtra(Constants.BUNDLE_SHOWTOAST, true);
    	if (desireSSID == null) return;
    	int desireNetworkId = intent.getIntExtra(Constants.BUNDLE_NETWORKID, -1);
    	if (desireNetworkId < 0) return;

    	WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
    	WifiInfo info = wifi.getConnectionInfo();
    	if (info == null || info.getNetworkId() != desireNetworkId) {
			Log.d(this.getClass().getName(), "Failed connect");
    		if (showToast)
				Toast.makeText(context, context.getString(R.string.msg_failed_to_connect, desireSSID), Toast.LENGTH_LONG).show();
    	}
    	enableNetworks(context);
	}

	private void enableNetworks(Context context) {
		Log.d(this.getClass().getName(), "enableNetworks");
        WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled()) {
	        List<WifiConfiguration> wifiList = wifi.getConfiguredNetworks();
	        if (wifiList != null) {
		        for (WifiConfiguration wifiConf : wifiList) {
		       		wifi.enableNetwork(wifiConf.networkId, false);
		        }
	        }
        }
	}
}
