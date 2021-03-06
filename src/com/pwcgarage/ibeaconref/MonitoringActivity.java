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

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.BeaconManager;

import com.pwcgarage.ibeaconref.R;
import com.pwcgarage.ibeaconref.eventbus.EventBus;
import com.pwcgarage.ibeaconref.eventbus.EventHubCallStatusEvent;
import com.pwcgarage.ibeaconref.pushmessaging.Constants;
import com.pwcgarage.ibeaconref.pushmessaging.GooglePushMessageHandler;
import com.pwcgarage.ibeaconref.restclients.EventHubRestClient;
import com.squareup.otto.Subscribe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.google.android.gms.gcm.*;
import com.microsoft.windowsazure.messaging.*;
import com.microsoft.windowsazure.notifications.NotificationsManager;
/**
 * @author asksven
 */
public class MonitoringActivity extends Activity
{
	protected static final String TAG = "MonitoringActivity";
	private BeaconManager beaconManager = BeaconManager
			.getInstanceForApplication(this);
	
	// GCM using Azure Notification Hubs stuff
	// see http://azure.microsoft.com/en-us/documentation/articles/notification-hubs-android-get-started/
	private GoogleCloudMessaging gcm;
	private NotificationHub hub;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.d(TAG, "onCreate");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitoring);
		verifyBluetooth();
		logToDisplay("Application just launched");

		// register with GCM
		NotificationsManager.handleNotifications(this, Constants.GCM_SENDER_ID, GooglePushMessageHandler.class);
		gcm = GoogleCloudMessaging.getInstance(this);
		hub = new NotificationHub(Constants.NOTIFICATION_HUB_NAME, Constants.NOTIFICATION_HUB_CS, this);
		GooglePushMessageHandler.registerWithNotificationHubs(gcm, hub, this);
	}

	public void onRangingClicked(View view)
	{
		Intent myIntent = new Intent(this, RangingActivity.class);
		this.startActivity(myIntent);
	}

	public void onTestClicked(View view)
	{
		// send a test event
		EventHubRestClient.getInstance().sendEvent(this, "",
				BeaconReferenceApplication.ACTION_TEST);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		// register to the event bus
		EventBus.getInstance().register(this);

		((BeaconReferenceApplication) this.getApplicationContext())
				.setMonitoringActivity(this);
	}

	@Override
	public void onPause()
	{
		// unregister from the event bus
		EventBus.getInstance().unregister(this);

		super.onPause();
		((BeaconReferenceApplication) this.getApplicationContext())
				.setMonitoringActivity(null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle item selection
		switch (item.getItemId())
		{
		case R.id.settings:

			this.startActivity(new Intent(this, SettingsActivity.class));
			break;

		default:
			return super.onOptionsItemSelected(item);
		}
		return false;
	}

	@Subscribe
	public void handleSomeEvent(EventHubCallStatusEvent event)
	{
		Toast.makeText(this, String.valueOf(event.getResultCode()),
				Toast.LENGTH_LONG).show();
	}

	private void verifyBluetooth()
	{
		try
		{
			if (!BeaconManager.getInstanceForApplication(this)
					.checkAvailability())
			{
				final AlertDialog.Builder builder = new AlertDialog.Builder(
						this);
				builder.setTitle("Bluetooth not enabled");
				builder.setMessage("Please enable bluetooth in settings and restart this application.");
				builder.setPositiveButton(android.R.string.ok, null);
				builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog)
					{
						// finish();
						// System.exit(0);
					}
				});
				builder.show();
			}
		} catch (RuntimeException e)
		{
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Bluetooth LE not available");
			builder.setMessage("Sorry, this device does not support Bluetooth LE.");
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog)
				{
					finish();
					// System.exit(0);
				}
			});
			builder.show();
		}
	}

	public void logToDisplay(final String line)
	{
		runOnUiThread(new Runnable() {
			public void run()
			{
				TextView tv = (TextView) MonitoringActivity.this
						.findViewById(R.id.monitoringText);
				tv.setText(line + "\n" + tv.getText());
			}
		});
	}
}