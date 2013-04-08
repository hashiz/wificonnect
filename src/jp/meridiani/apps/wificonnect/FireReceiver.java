package jp.meridiani.apps.wificonnect;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.Toast;

public class FireReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!com.twofortyfouram.locale.Intent.ACTION_FIRE_SETTING.equals(intent.getAction())) {
        	return;
        }
        Bundle bundle = intent.getBundleExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE);
        if (bundle == null) {
        	return;
        }
        String ssid = bundle.getString(Constants.BUNDLE_AP_SSID);
        if (ssid == null || ssid.length() < 1) {
        	return;
        }
        WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled()) {
    		Toast.makeText(context, "Wifi disabled", Toast.LENGTH_LONG).show();
    		return;
        }
        List<WifiConfiguration>wifiList = wifi.getConfiguredNetworks();
        WifiConfiguration desireWifiConf = null;
        for (WifiConfiguration wifiConf : wifiList) {
        	if (ssid.equals(wifiConf.SSID)) {
        		if (wifiConf.status == WifiConfiguration.Status.CURRENT) {
            		Toast.makeText(context, "Already connect " + ssid, Toast.LENGTH_LONG).show();
            		return;
        		}
        		desireWifiConf = wifiConf;
        		break;
        	}
        }
		Toast.makeText(context, "Connecting " + desireWifiConf.SSID, Toast.LENGTH_LONG).show();

        context.registerReceiver(
        		new ConnectivityReceiver(
        				System.currentTimeMillis(),
        				10*1000,
        				desireWifiConf.networkId,
        				desireWifiConf.SSID),
				new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        // disable other networks
        wifi.enableNetwork(desireWifiConf.networkId, true);

        // re-enable all networks
        for (WifiConfiguration wifiConf : wifiList) {
       		wifi.enableNetwork(wifiConf.networkId, false);
        }

        // fire
        wifi.saveConfiguration();
	}
}
