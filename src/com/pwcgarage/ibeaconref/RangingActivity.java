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

import java.util.Collection;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import com.pwcgarage.ibeaconref.R;

/**
 * @author asksven
 */
public class RangingActivity extends Activity implements BeaconConsumer
{
	protected static final String TAG 	= "RangingActivity";
	private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ranging);
		beaconManager.bind(this);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		beaconManager.unbind(this);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		if (beaconManager.isBound(this))
			beaconManager.setBackgroundMode(true);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		if (beaconManager.isBound(this))
			beaconManager.setBackgroundMode(false);
	}

	@Override
	public void onBeaconServiceConnect()
	{
		beaconManager.setRangeNotifier(new RangeNotifier() {
			@Override
			public void didRangeBeaconsInRegion(Collection<Beacon> beacons,
					Region region)
			{
				if (beacons.size() > 0)
				{
					Beacon firstBeacon = beacons.iterator().next();
					logToDisplay(firstBeacon.getBluetoothAddress()
							+ " is " + String.format("%1$,.2f", firstBeacon.getDistance())
							+ " m away.");
				}
			}
		});
		try
		{
			beaconManager.startRangingBeaconsInRegion(new Region(
					"myRangingUniqueId", null, null, null));
		} catch (RemoteException e)
		{
		}
	}

	private void logToDisplay(final String line)
	{
		runOnUiThread(new Runnable() {
			public void run()
			{
				TextView tv = (TextView) RangingActivity.this
						.findViewById(R.id.rangingText);
				tv.setText(line + "\n" + tv.getText());
			}
		});
	}
}