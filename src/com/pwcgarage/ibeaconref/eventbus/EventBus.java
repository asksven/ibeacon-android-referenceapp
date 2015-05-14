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
package com.pwcgarage.ibeaconref.eventbus;

import android.util.Log;

import com.squareup.otto.Bus;

/**
 * A singleton event bus based on Otto for communicating between the different parts of the application.
 * @author asksven
 */
public class EventBus
{
	private static EventBus m_singleton = null;
	private static Bus m_bus = null;
	private static String TAG = "EventBus";
	
	private void EventBus()
	{
		
	}

	public static EventBus getInstance()
	{
		if (m_singleton == null)
		{
			m_bus = new Bus();
			m_singleton = new EventBus();
		}
		
		return m_singleton;
	}
	
	public void register(Object subscriber)
	{
		m_bus.register(subscriber);
	}

	public void unregister(Object subscriber)
	{
		// make sure this won't cause an FC in case subscriber is not registered
		try
		{
			m_bus.unregister(subscriber);
		}
		catch (Exception e)
		{
			Log.e(TAG, "Error in unregister: " + e.getMessage());
		}
	}

	public void post(AbstractEvent event)
	{
		m_bus.post(event);
	}
	
}
