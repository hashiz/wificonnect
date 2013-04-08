package jp.meridiani.apps.wificonnect;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.widget.Toast;

public class WifiStateReceiver extends BroadcastReceiver {

	protected long  mStartTime;
	protected long  mMaxWait;
	protected int   mDesireNetworkId;
	protected String mDesireSSID;

	public WifiStateReceiver(long startTime, long maxWait, int desireNetworkId, String desireSSID)	{
		mStartTime = startTime;
		mMaxWait   = maxWait;
		mDesireNetworkId  = desireNetworkId;
		mDesireSSID = desireSSID;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			return;
		}
		try {
			ConnectivityManager conMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
			WifiManager wifiMgr = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
	
			NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
			if (netInfo.isConnected() && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				String ssid = wifiMgr.getConnectionInfo().getSSID();
				int networkId = wifiMgr.getConnectionInfo().getNetworkId();
				if (networkId == mDesireNetworkId) {
					Toast.makeText(context, "Connected " + ssid, Toast.LENGTH_LONG).show();
					return;
				}
			}
			Toast.makeText(context, "Failed connect to " + mDesireSSID, Toast.LENGTH_LONG).show();
		}
		finally {
			// set all enable
			WifiManager wifiMgr = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
	        List<WifiConfiguration>wifiList = wifiMgr.getConfiguredNetworks();
	        for (WifiConfiguration wifiConf : wifiList) {
	        	wifiMgr.enableNetwork(wifiConf.networkId, false);
	        }
	        context.unregisterReceiver(this);
		}
	}
}
