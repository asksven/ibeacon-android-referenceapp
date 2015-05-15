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

/**
 * Cloud to Device Constants
 * @author asksven
 */
public class Constants
{
	// Azure Notification Hubs Constants
	// from azure portal
	public static String NOTIFICATION_HUB_NAME = "ibeaconref-pushnotifications";
	public static String NOTIFICATION_HUB_CS = "Endpoint=sb://ibeaconref-pushnotifications-ns.servicebus.windows.net/;SharedAccessKeyName=DefaultListenSharedAccessSignature;SharedAccessKey=LKFjmWQxWECduZUNAxp6eZ23hcN6D5WJYGxXu2BatMk=";
	
	// GCM Constants
	// from https://console.developers.google.com/
	public static String GCM_SENDER_ID = "1083449393014";
	
}
