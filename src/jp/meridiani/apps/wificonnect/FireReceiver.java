package jp.meridiani.apps.wificonnect;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.Toast;
import jp.meridiani.apps.wificonnect.Constants;

public class FireReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		long starttime;

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
        List<WifiConfiguration>wifiList = wifi.getConfiguredNetworks();
        int currentId = wifi.getConnectionInfo().getNetworkId();
        for (WifiConfiguration wifiConf : wifiList) {
        	if (ssid.equals(wifiConf.SSID)) {
        		if (wifiConf.status == WifiConfiguration.Status.CURRENT) {
            		Toast.makeText(context, "Already connect " + ssid, Toast.LENGTH_LONG).show();
            		return;
        		}
        		currentId = wifiConf.networkId;
        		break;
        	}
        }
		Toast.makeText(context, "Connect to " + ssid, Toast.LENGTH_LONG).show();

		// disconnect current network
		wifi.disconnect();

		// wait disconnect
        starttime = System.currentTimeMillis();
        while (wifi.getConnectionInfo().getSupplicantState() != SupplicantState.DISCONNECTED) {
        	if (System.currentTimeMillis() - starttime > 10 * 1000) {
        		// time out
        		break;
        	}
        	try {
				Thread.sleep(500);
			}
        	catch (InterruptedException e) {
			}
        }

        // disable other networks
        wifi.enableNetwork(currentId, true);

        wifi.reconnect();

        // wait connect
        starttime = System.currentTimeMillis();
        while (wifi.getConnectionInfo().getSupplicantState() != SupplicantState.COMPLETED) {
        	if (System.currentTimeMillis() - starttime > 10 * 1000) {
        		break;
        	}
        }

        if (wifi.getConnectionInfo().getNetworkId() == currentId) {
	        // success
			Toast.makeText(context, "Connected " + ssid, Toast.LENGTH_LONG).show();
        }
        else {
			// failure
			Toast.makeText(context, "Can't connect " + ssid, Toast.LENGTH_LONG).show();
        }
        // set all enable
        for (WifiConfiguration wifiConf : wifiList) {
        	wifi.enableNetwork(wifiConf.networkId, false);
        }
	}
}
