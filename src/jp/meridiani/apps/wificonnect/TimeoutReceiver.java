package jp.meridiani.apps.wificonnect;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

public class TimeoutReceiver extends BroadcastReceiver {
	public static final String TIMEOUT_ACTION  = "jp.meridiani.apps.wificonnnect.TIMEOUT_ACTION";
	public static final String EXTRA_SSID      = "jp.meridiani.apps.wificonnnect.EXTRA_SSID";
	public static final String EXTRA_NETWORKID = "jp.meridiani.apps.wificonnnect.EXTRA_NETWORKID";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(this.getClass().getName(), "onReceive");
    	String action = intent.getAction();
    	if (!TIMEOUT_ACTION.equals(action)) {
    		return;
    	}
    	String desireSSID = intent.getStringExtra(EXTRA_SSID);
    	if (desireSSID == null) return;
    	int desireNetworkId = intent.getIntExtra(EXTRA_NETWORKID, -1);
    	if (desireNetworkId < 0) return;

    	WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
    	WifiInfo info = wifi.getConnectionInfo();
    	if (info == null || info.getNetworkId() != desireNetworkId) {
			Log.d(this.getClass().getName(), "Failed connect");
    		Toast.makeText(context, "Failed connect to " + desireSSID, Toast.LENGTH_LONG).show();
    	}
    	enableNetworks(context);
	}

	private void enableNetworks(Context context) {
		Log.d(this.getClass().getName(), "enableNetworks");
        WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> wifiList = wifi.getConfiguredNetworks();
        for (WifiConfiguration wifiConf : wifiList) {
       		wifi.enableNetwork(wifiConf.networkId, false);
        }
	}


}
