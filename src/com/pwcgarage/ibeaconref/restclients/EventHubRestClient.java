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
import com.pwcgarage.ibeaconref.utils.SaSignatureTokenGenerator;

/**
 * @author asksven
 */
public class EventHubRestClient
{

	private static AsyncHttpClient m_httpClient = null;
	private static EventHubRestClient m_client = null;
	private static long tokenExpiresAt = 0L;
	private static final String TAG = "EventHubRestClient";

	private void EventHubRestClient()
	{

	}

	public static EventHubRestClient getInstance()
	{
		if (m_client == null)
		{
			m_client = new EventHubRestClient();
			m_httpClient = getRestClient();

		}

		return m_client;
	}
	
	/** 
	 * Returns an http client calid for a given time
	 * @return
	 */
	private static AsyncHttpClient getRestClient()
	{
		AsyncHttpClient ret = new AsyncHttpClient();
				
		String SaSignature = SaSignatureTokenGenerator.generateSaSignatureToken(Constants.EVENTHUB_URL, Constants.EVENTHUB_POLICY_NAME, 
				Constants.EVENTHUB_POLICY_KEY, Constants.EVENTHUB_TTL_MINUTES);
		tokenExpiresAt = System.currentTimeMillis() + (Constants.EVENTHUB_TTL_MINUTES * 60L * 1000L);
		
		ret.addHeader("Authorization", SaSignature);

		return ret;
	}

	public void sendEvent(Context ctx, String region, String action)
	{
		// check if our SAS Token has expired. If yes get a new REST client with a fresh token
		if (System.currentTimeMillis() > tokenExpiresAt)
		{
			m_httpClient = getRestClient();
		}
		
		// Assert parameters
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
			m_httpClient.post(ctx, Constants.EVENTHUB_URL + "/" + Constants.EVENTHUB_QUEUE_PATH, entity, "application/atom+xml;type=entry;charset=utf-8",
					new AsyncHttpResponseHandler() {

						@Override
						public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error)
						{
							Log.d(TAG, "Request failed with statusCode=" + statusCode + " with message=" + error.getMessage());
							// post event to eventbus
							EventBus.getInstance().post(new EventHubCallStatusEvent(EventHubCallStatusEvent.Type.COMPLETED,0));
							
						}

						@Override
						public void onSuccess(int statusCode, Header[] headers, byte[] responseBody)
						{
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