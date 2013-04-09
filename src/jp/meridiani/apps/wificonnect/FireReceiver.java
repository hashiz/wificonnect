package jp.meridiani.apps.wificonnect;

import java.util.List;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class FireReceiver extends BroadcastReceiver {

	private static final String ACTION_TIMEOUT = "jp.meridiani.apps.wificonnnect.TIMEOUT";

	private BroadcastReceiver mReceiver;
	private IntentFilter      mFilter;
	private boolean           mReset;
	private WifiConfiguration mDesireWifiConf;

    public FireReceiver() {
    	mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onStateChange(context, intent);
            }
        };
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(ACTION_TIMEOUT);
        mReset = false;
    }

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
        mDesireWifiConf = null;
        int lastPriority = 0;
        for (WifiConfiguration wifiConf : wifiList) {
        	if (ssid.equals(wifiConf.SSID)) {
        		if (wifiConf.status == WifiConfiguration.Status.CURRENT) {
            		Toast.makeText(context, "Already connect " + ssid, Toast.LENGTH_LONG).show();
            		return;
        		}
        		mDesireWifiConf = wifiConf;
        	}
        	if (wifiConf.priority > lastPriority) {
        		lastPriority = wifiConf.priority;
        	}
        }
        if (mDesireWifiConf == null) {
        	return;
        }
		Toast.makeText(context, "Connecting " + mDesireWifiConf.SSID, Toast.LENGTH_LONG).show();

		// set priority to top
		mDesireWifiConf.priority = lastPriority + 1;
		wifi.updateNetwork(mDesireWifiConf);
		wifi.saveConfiguration();
		
		// disable other networks
        wifi.enableNetwork(mDesireWifiConf.networkId, true);
        mReset = true;

		// register
		context.getApplicationContext().registerReceiver(mReceiver,mFilter);

        wifi.reconnect();

        // timer start
        AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        
        alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 15 * 1000, operation)
    }

	private void onStateChange(Context context, Intent intent) {
    	NetworkInfo info = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
    	if (info == null) {
    		Log.e("onStateChange", WifiManager.EXTRA_NETWORK_INFO);
    		return;
    	}

        WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

        switch (info.getState()) {
    	case CONNECTED:
	    	{
	    		if (info.getType() == ConnectivityManager.TYPE_WIFI &&
	    				wifi.getConnectionInfo().getNetworkId() == mDesireWifiConf.networkId) {
	    			Toast.makeText(context, "Connected " + mDesireWifiConf.SSID, Toast.LENGTH_LONG).show();
		        	// re-enable all networks
		    		if (mReset) {
			            List<WifiConfiguration> wifiList = wifi.getConfiguredNetworks();
			            for (WifiConfiguration wifiConf : wifiList) {
			           		wifi.enableNetwork(wifiConf.networkId, false);
			            }
			            mReset = false;
		    		}
		            context.getApplicationContext().unregisterReceiver(this);
	    		}
	    	}
	    	break;
		default:
			break;
    	}
    }
}
