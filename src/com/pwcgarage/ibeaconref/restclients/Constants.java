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

/**
 * @author asksven
 */
public class Constants
{
	protected static final String EVENTHUB_URL = "http://<namespace>.servicebus.windows.net/<entityPath>";
	protected static final String EVENTHUB_QUEUE_PATH = "publishers/<device-name>/messages";
	protected static final String EVENTHUB_POLICY_NAME = "<policy-name>";
	protected static final String EVENTHUB_POLICY_KEY= "<policy-key>";	
	protected static final int EVENTHUB_TTL_MINUTES = 60;

}
