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
package com.pwcgarage.ibeaconref.pushmessaging;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.microsoft.windowsazure.messaging.NotificationHub;
import com.microsoft.windowsazure.notifications.NotificationsHandler;
import com.pwcgarage.ibeaconref.MonitoringActivity;
import com.pwcgarage.ibeaconref.R;

/**
 * GCM Notification Handler
 * 
 * @author asksven
 */
public class GooglePushMessageHandler extends NotificationsHandler
{
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	Context ctx;

	static public MonitoringActivity mainActivity;

	@Override
	public void onReceive(Context context, Bundle bundle)
	{
		ctx = context;
		String nhMessage = bundle.getString("message");

		sendNotification(nhMessage);
	}

	private void sendNotification(String msg)
	{
		mNotificationManager = (NotificationManager) ctx
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
				new Intent(ctx, MonitoringActivity.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				ctx).setSmallIcon(R.drawable.ic_stat_ic_action_location_found)
				.setContentTitle("Notification Hub Demo")
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

	@SuppressWarnings("unchecked")
	public static void registerWithNotificationHubs(final GoogleCloudMessaging gcm,
			final NotificationHub hub, final Context ctx)
	{
		new AsyncTask() {
			@Override
			protected Object doInBackground(Object... params)
			{
				try
				{
					String regid = gcm.register(Constants.GCM_SENDER_ID);
					Toast.makeText(ctx, 
							ctx.getString(R.string.gcm_register_success, hub.register(regid).getRegistrationId()),
							Toast.LENGTH_SHORT).show();
					
				} catch (Exception e)
				{
					Toast.makeText(ctx,
							ctx.getString(R.string.gcm_register_error, e.getMessage()), Toast.LENGTH_SHORT).show();
					return e;
				}
				return null;
			}
		}.execute(null, null, null);
	}
}