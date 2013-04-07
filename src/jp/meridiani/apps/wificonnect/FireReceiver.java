package jp.meridiani.apps.wificonnect;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import jp.meridiani.apps.wificonnect.Constants;

public class FireReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
        if (!com.twofortyfouram.locale.Intent.ACTION_QUERY_CONDITION.equals(intent.getAction())) {
        	return;
        }
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
        	return;
        }
        String ssid = (String)bundle.get(Constants.BUNDLE_AP_SSID);
        if (ssid == null) {
        	return;
        }
        WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration>wifiList = wifi.getConfiguredNetworks();
        for (WifiConfiguration wifiConf : wifiList) {
        	if (ssid.equals(wifiConf.SSID)) {
        		// connect to ssid
        		if (wifi.enableNetwork(wifiConf.networkId,false)) {
        			// success
        		}
        		else {
        			// failure
        		}
        		break;
        	}
        }
	}
}
