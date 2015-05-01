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
package com.pwcgarage.ibeaconref.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;
import android.util.Log;

/**
 * Generates a Service Bus / Event Hub Service Access Signature (see spec here: https://msdn.microsoft.com/library/azure/dn170477.aspx)
 * @author asksven
 */
public class SaSignatureTokenGenerator
{
	private final static String TAG = "SaSignatureGenerator";
	
	/**
	 * Generates a Shared Access Signature Authentication Token for accessing an Event Hub on MS Azure
	 * see also the same algorithm in node http://hypernephelist.com/2014/09/16/sending-data-to-azure-event-hubs-from-nodejs.html
	 *  and a website written in C# http://eventhubsas.azurewebsites.net/ 
	 * @param uri the resource uri in the format http://<namespace>.servicebus.windows.net/<entityPath>
	 * @param policyName from the Azure portal configuration dialog
	 * @param policyKey from the Azure portal configuration dialog
	 * @param ttlMinutes the time to live in minutes for this token
	 * @return a SAS Token valid for the given time
	 */
	public static String generateSaSignatureToken(String uri, String policyName, String policyKey, long ttlMinutes)
    {

	    // Token expires in one hour
		long nowMs = System.currentTimeMillis();
		
		String encodedUri = "";
		String encodedSignature = "";
		long expirySeconds = (nowMs / 1000L) + (ttlMinutes * 60);
		
		try
		{
			encodedUri = URLEncoder.encode(uri, "UTF-8");
		
			// create the string to digest
			
		    String stringToSign = encodedUri + '\n' + String.valueOf(expirySeconds);
		    
		    // encrypt it with the key, as Base64 encoded
		    Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
		    SecretKeySpec secret_key = new SecretKeySpec(policyKey.getBytes(), "HmacSHA256");
		    sha256_HMAC.init(secret_key);

		    byte[] encryptedBytes = sha256_HMAC.doFinal(stringToSign.getBytes());
		    
		    // encode it	    
		    encodedSignature = URLEncoder.encode(new String(Base64.encode(encryptedBytes, Base64.NO_WRAP), "UTF-8"), "UTF-8");
		    
		}
		catch (UnsupportedEncodingException e)
		{
			Log.e(TAG, "URL encoding failed:" + e.getMessage());
		}
		catch (NoSuchAlgorithmException e)
		{
			Log.e(TAG, "Algorithm does not exist:" + e.getMessage());
		}
		catch (InvalidKeyException e)
		{
			Log.e(TAG, "The key is invalid:" + e.getMessage());
		}

	    String token = "SharedAccessSignature sr=" 
	    		+ encodedUri 
	    		+ "&sig=" + encodedSignature 
	    		+ "&se=" + expirySeconds
	    		+ "&skn=" + policyName;
	    return token;

	    
    }
}
