/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pwcgarage.ibeaconref;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.altbeacon.beacon.startup.BootstrapNotifier;


import com.pwcgarage.ibeaconref.R;
import com.pwcgarage.ibeaconref.R.drawable;
import com.pwcgarage.ibeaconref.restclients.EventHubRestClient;
import com.pwcgarage.ibeaconref.utils.DeviceUuidFactory;

/**
 * @author asksven
 */
public class BeaconReferenceApplication extends Application implements BootstrapNotifier
{
	private static final String TAG = "BeaconReferenceApplication";
	private RegionBootstrap m_regionBootstrap;
	private BackgroundPowerSaver m_backgroundPowerSaver;
	private boolean m_haveDetectedBeaconsSinceBoot = false;
	private MonitoringActivity m_monitoringActivity = null;
	public static final String ACTION_ENTER = "enter-beacon-proximity";
	public static final String ACTION_LEAVE = "leave-beacon-proximity";
	public static final String ACTION_TEST = "test-action";
	public static final String ACTION_UNKNOWN = "unknown-action";
	
	public void onCreate()
	{
		super.onCreate();
		
		// make sure that the device has a UUID
		Log.d(TAG, "Device UUID=" + new DeviceUuidFactory(this).getDeviceUuid());
		
		BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);

		// beaconinside specific parser
		beaconManager.getBeaconParsers().add(
				new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

		Log.d(TAG, "setting up background monitoring for beacons and power saving");
		
		// wake up the app when any beacon is seen
		//Region region = new Region("backgroundRegion", null, null, null);
		//m_regionBootstrap = new RegionBootstrap(this, region);
		
		// Building a region for a specific beacon
		// see also https://altbeacon.github.io/android-beacon-library/javadoc/org/altbeacon/beacon/Region.html
		// Region region = new Region("some-unique-id", Identifier.parse(Constants.BT_UUID), 
		//  Identifier.fromInt(Constants.BT_MAJOR), Identifier.fromInt(Constants.BT_MINOR));

		Region regionHome = new Region("region-home", Identifier.parse("F0018B9B-7509-4C31-A905-1A27D39C003C"), 
				Identifier.fromInt(31452), Identifier.fromInt(25352));
		m_regionBootstrap = new RegionBootstrap(this, regionHome);
		
		// simply constructing this class and holding a reference to it in your
		// custom Application
		// class will automatically cause the BeaconLibrary to save battery
		// whenever the application
		// is not visible. This reduces bluetooth power usage by about 60%
		m_backgroundPowerSaver = new BackgroundPowerSaver(this);
		
		// Use for testing with simulator
		// BeaconManager.setBeaconSimulator(new TimedBeaconSimulator() );
		// ((TimedBeaconSimulator)
		// BeaconManager.getBeaconSimulator()).createTimedSimulatedBeacons();
	}

	@Override
	public void didEnterRegion(Region arg0)
	{
		// In this example, this class sends a notification to the user whenever
		// a Beacon matching a Region (defined above) are first seen.
		Log.d(TAG, "did enter region " + arg0.getUniqueId());
		if (!m_haveDetectedBeaconsSinceBoot)
		{
			Log.d(TAG, "auto launching MainActivity");
			
			// The very first time since boot that we detect an beacon, we launch the MainActivity
			Intent intent = new Intent(this, MonitoringActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			
			// Important: make sure to add android:launchMode="singleInstance"
			// in the manifest to keep multiple copies of this activity from getting created if
			// the user has already manually launched the app.
			this.startActivity(intent);
			m_haveDetectedBeaconsSinceBoot = true;
		}
		else
		{
			if (m_monitoringActivity != null)
			{
				// If the Monitoring Activity is visible, we log info about the
				// beacons we have
				// seen on its display
				m_monitoringActivity.logToDisplay("I see a beacon: " + arg0.getUniqueId());
			}
			else
			{
				// If we have already seen beacons before, but the monitoring
				// activity is not in
				// the foreground, we send a notification to the user on
				// subsequent detections.
				Log.d(TAG, "Sending notification.");
				sendNotification(arg0);
			}
		}
		
		// Send the event to the event hub
		EventHubRestClient.getInstance().sendEvent(this, arg0.getUniqueId(), ACTION_ENTER);
	}

	@Override
	public void didExitRegion(Region region)
	{
		if (m_monitoringActivity != null)
		{
			m_monitoringActivity.logToDisplay("I no longer see a beacon.");
		}
		
		// Send the event to the event hub
		EventHubRestClient.getInstance().sendEvent(this, region.getUniqueId(), ACTION_LEAVE);

	}

	@Override
	public void didDetermineStateForRegion(int state, Region region)
	{
		if (m_monitoringActivity != null)
		{
			m_monitoringActivity
					.logToDisplay("I have just switched from seeing/not seeing beacons: "
							+ state);
		}
	}

	private void sendNotification(Region region)
	{
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				this).setContentTitle(getResources().getString(R.string.app_name))
				.setContentText(getResources().getString(R.string.notification_entered_region, region.getUniqueId()))
				.setSmallIcon(R.drawable.ic_stat_ic_action_location_found);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addNextIntent(new Intent(this, MonitoringActivity.class));
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(resultPendingIntent);
		NotificationManager notificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(1, builder.build());
	}

	public void setMonitoringActivity(MonitoringActivity activity)
	{
		this.m_monitoringActivity = activity;
	}
	
}