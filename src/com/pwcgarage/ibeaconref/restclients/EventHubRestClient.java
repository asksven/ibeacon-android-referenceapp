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
package com.pwcgarage.ibeaconref.restclients;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.pwcgarage.ibeaconref.BeaconReferenceApplication;
import com.pwcgarage.ibeaconref.eventbus.EventBus;
import com.pwcgarage.ibeaconref.eventbus.EventHubCallStatusEvent;
import com.pwcgarage.ibeaconref.utils.DeviceUuidFactory;

/**
 * @author asksven
 */
public class EventHubRestClient
{

	private static AsyncHttpClient m_httpClient = null;
	private static EventHubRestClient m_client = null;
	private static final String TAG = "EventHubRestClient";

	private void EventHubRestClient()
	{

	}

	public static EventHubRestClient getInstance()
	{
		if (m_client == null)
		{
			m_client = new EventHubRestClient();
			m_httpClient = new AsyncHttpClient();
			m_httpClient.addHeader("Authorization", Constants.EVENTHUB_SA_SIG);

		}

		return m_client;
	}

	public void sendEvent(Context ctx, String region, String action)
	{
		if (!(action.equals(BeaconReferenceApplication.ACTION_ENTER)
				|| action.equals(BeaconReferenceApplication.ACTION_LEAVE) || action
					.equals(BeaconReferenceApplication.ACTION_TEST)))
		{
			action = BeaconReferenceApplication.ACTION_UNKNOWN;
		}

		JSONObject params = new JSONObject();

		try
		{
			params.put("DeviceId", new DeviceUuidFactory(ctx).getDeviceUuid());
			params.put("TimeUTC", getCurrentTimeUtc());
			params.put("Region", region);
			params.put("Action", action);
			StringEntity entity = new StringEntity(params.toString());
			m_httpClient.post(ctx, Constants.EVENTHUB_URL, entity, "application/atom+xml;type=entry;charset=utf-8",
					new AsyncHttpResponseHandler() {

						@Override
						public void onFailure(int arg0, Header[] arg1,
								byte[] arg2, Throwable arg3)
						{
							// TODO Auto-generated method stub
							Log.d(TAG, "Request failed");
							// post event to eventbus
							EventBus.getInstance().post(new EventHubCallStatusEvent(EventHubCallStatusEvent.Type.COMPLETED,0));
							
						}

						@Override
						public void onSuccess(int arg0, Header[] arg1,
								byte[] arg2)
						{
							// TODO Auto-generated method stub
							Log.d(TAG, "Request succeeded");
							// post event to eventbus
							EventBus.getInstance().post(new EventHubCallStatusEvent(EventHubCallStatusEvent.Type.COMPLETED,1));
						}
					});
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	protected static String getCurrentTimeUtc()
	{
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		final String utcTime = sdf.format(new Date());
		return utcTime;

	}
}