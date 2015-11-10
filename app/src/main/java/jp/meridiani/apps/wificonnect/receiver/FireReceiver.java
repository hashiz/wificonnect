package jp.meridiani.apps.wificonnect.receiver;

import java.util.List;

import jp.meridiani.apps.wificonnect.Constants;
import jp.meridiani.apps.wificonnect.R;

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
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class FireReceiver extends BroadcastReceiver {

	private IntentFilter      mFilter;
	private boolean           mReset;
	private WifiConfiguration mDesireWifiConf;

	public class StateReceiver extends BroadcastReceiver {
		public StateReceiver() {
			Log.d(this.getClass().getName(), "StateReceiver");
		}

		@Override
        public void onReceive(Context context, Intent intent) {
			Log.d(this.getClass().getName(), "onReceive");
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
        	    			Toast.makeText(context, context.getString(R.string.msg_connected, mDesireWifiConf.SSID), Toast.LENGTH_LONG).show();
        	    			
        	    			Log.d(this.getClass().getName(), "Connected");

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

	public FireReceiver() {
		Log.d(this.getClass().getName(), "FireReceiver");
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(Constants.ACTION_TIMEOUT);
    }

    @Override
	public void onReceive(Context context, Intent intent) {
		Log.d(this.getClass().getName(), "onReceive");
		if (!com.twofortyfouram.locale.api.Intent.ACTION_FIRE_SETTING.equals(intent.getAction())) {
        	return;
        }
        Bundle bundle = intent.getBundleExtra(com.twofortyfouram.locale.api.Intent.EXTRA_BUNDLE);
        if (bundle == null) {
        	return;
        }
        String ssid = bundle.getString(Constants.BUNDLE_AP_SSID);
        if (ssid == null || ssid.length() < 1) {
        	return;
        }

        mReset = false;

        WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled()) {
    		Toast.makeText(context, context.getString(R.string.msg_wifi_disable), Toast.LENGTH_LONG).show();
    		return;
        }
        List<WifiConfiguration>wifiList = wifi.getConfiguredNetworks();
        if (wifiList == null) {
    		Toast.makeText(context, context.getString(R.string.msg_wifi_disable), Toast.LENGTH_LONG).show();
    		return;
        }
        mDesireWifiConf = null;
        int lastPriority = 0;
        for (WifiConfiguration wifiConf : wifiList) {
        	if (ssid.equals(wifiConf.SSID)) {
        		if (wifiConf.status == WifiConfiguration.Status.CURRENT) {
            		Toast.makeText(context, context.getString(R.string.msg_already_connect, ssid), Toast.LENGTH_LONG).show();
            		return;
        		}
        		mDesireWifiConf = wifiConf;
        	}
        	if (wifiConf.priority > lastPriority) {
        		lastPriority = wifiConf.priority;
        	}
        }
        if (mDesireWifiConf == null) {
    		Toast.makeText(context, context.getString(R.string.msg_not_configured, ssid), Toast.LENGTH_LONG).show();
        	return;
        }
		Toast.makeText(context, context.getString(R.string.msg_connecting, mDesireWifiConf.SSID), Toast.LENGTH_LONG).show();

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
		Log.d(this.getClass().getName(), "makePendingIntent");
	    Intent i = new Intent();
	    i.setAction(Constants.ACTION_TIMEOUT);
	    i.setClass(context.getApplicationContext(), TimeoutReceiver.class);
	    i.putExtra(Constants.BUNDLE_SSID, mDesireWifiConf.SSID);
	    i.putExtra(Constants.BUNDLE_NETWORKID, mDesireWifiConf.networkId);
	    return PendingIntent.getBroadcast(context.getApplicationContext(), 0, i, 0);
    }

	private void enableNetworks(Context context) {
		Log.d(this.getClass().getName(), "enableNetworks");
		if (mReset) {
	        WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
	        if (wifi.isWifiEnabled()) {
		        List<WifiConfiguration> wifiList = wifi.getConfiguredNetworks();
		        if (wifiList != null) {
			        for (WifiConfiguration wifiConf : wifiList) {
			       		wifi.enableNetwork(wifiConf.networkId, false);
			        }
		        }
	        }
	        mReset = false;
		}
	}

	private void cancelTimer(Context context) {
		Log.d(this.getClass().getName(), "cancelTimer");
		AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(makePendingIntent(context));
	}
}
