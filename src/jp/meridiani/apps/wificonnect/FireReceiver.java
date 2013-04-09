package jp.meridiani.apps.wificonnect;

import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class FireReceiver extends BroadcastReceiver {

	private static final String TIMEOUT_ACTION = "jp.meridiani.apps.wificonnnect.TIMEOUT";

	private IntentFilter      mFilter;
	private boolean           mReset;
	private WifiConfiguration mDesireWifiConf;

	public class StateReceiver extends BroadcastReceiver {
		public StateReceiver() {
			Debug.waitForDebugger();
			Log.d(this.getClass().getName(), "constractor");
		}
        @Override
        public void onReceive(Context context, Intent intent) {
        	String action = intent.getAction();
        	if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            	NetworkInfo info = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            	if (info == null) {
            		return;
            	}

                switch (info.getState()) {
            	case CONNECTED:
        	    	{
        	            WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

        	    		if (info.getType() == ConnectivityManager.TYPE_WIFI &&
        	    				wifi.getConnectionInfo().getNetworkId() == mDesireWifiConf.networkId) {
        	    			Toast.makeText(context, "Connected " + mDesireWifiConf.SSID, Toast.LENGTH_LONG).show();
        	    			
        	    	        context.getApplicationContext().unregisterReceiver(this);
        	    			cancelTimer(context);
        	    			enableNetworks(context);
        	    		}
        	    	}
        	    	break;
        		default:
        			break;
            	}
        	}
        }
	}

	public class AlarmReceiver extends BroadcastReceiver {
		public AlarmReceiver() {
			Debug.waitForDebugger();
			Log.d(this.getClass().getName(), "constractor");
		}

		@Override
        public void onReceive(Context context, Intent intent) {
        	String action = intent.getAction();
        	if (TIMEOUT_ACTION.equals(action)) {
        		cancelTimer(context);
        		enableNetworks(context);
        	}
        }
	}

	public FireReceiver() {
    	Debug.waitForDebugger();

        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(TIMEOUT_ACTION);
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
		context.getApplicationContext().registerReceiver(new StateReceiver(), mFilter);

        wifi.reconnect();

        // timer start
        AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 15000, makePendingIntent(context));
    }

    private PendingIntent makePendingIntent(Context context) {
	    Intent i = new Intent();
	    i.setAction(TIMEOUT_ACTION);
	    i.setClass(context.getApplicationContext(), AlarmReceiver.class);
	    return PendingIntent.getBroadcast(context.getApplicationContext(), 0, i, 0);
    }

	private void enableNetworks(Context context) {
		if (mReset) {
	        WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
	        List<WifiConfiguration> wifiList = wifi.getConfiguredNetworks();
	        for (WifiConfiguration wifiConf : wifiList) {
	       		wifi.enableNetwork(wifiConf.networkId, false);
	        }
	        mReset = false;
		}
	}

	private void cancelTimer(Context context) {
		AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(makePendingIntent(context));
	}
}
