package com.webnetmobile.tools;

import org.json.JSONArray;
import org.json.JSONObject;

/*
 ******************************************************************************
 *
 * Copyright 2013 Webnet Marcin Orłowski
 *
 * Licensed under the GPL License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/gpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************
 *
 * @author Marcin Orlowski <marcin.orlowski@webnet.pl>
 *
 ******************************************************************************
 */
public final class JsonTools
{
	/**
	 * Tries to read JSON object's boolean property stored with given key. If such mapping does not exist, or there is
	 * value type mismatch, defaultValue will be returned
	 *
	 * @param json JSON object to get the property from
	 * @param key property key
	 * @param defaultValue value to be returned if no key mapping exists
	 * @return
	 */
	public static Boolean getBoolean( JSONObject json, String key, Boolean defaultValue ) {

		Boolean result = defaultValue;

		if( json != null ) {
			try {
				result = json.getBoolean( key );
			} catch( Exception e ) {}
		}

		return result;
	}
	/**
	 * Tries to read JSON object's String property stored with given key. If such mapping does not exist, or there is
	 * value type mismatch, defaultValue will be returned
	 *
 	 * @param json JSON object to get the property from
	 * @param key property key
	 * @param defaultValue value to be returned if no key mapping exists
	 * @return
	 */
	public static String getString( JSONObject json, String key ) {
		return getString( json, key, null );
	}
	public static String getString( JSONObject json, String key, String defaultValue ) {
		String result = defaultValue;

		try {
			if( json.isNull(key) == false ) {
				result = json.getString( key );
			} else {
				result = null;
			}
		} catch( Exception e ) {
			e.printStackTrace();
		}

		//WebnetLog.d(key + " = " + result + " (" + defaultValue +")");

		return result;
	}

	/**
	 * Tries to read JSON object String property given with given key. Will return <code>null</code> if no mapping found
	 * Will also return <code>null</code> if mapping exists but the value is empty string (length of 0).
	 *
	 * @param json
	 * @param key
	 * @return
	 */
	public static String getStringOrNull( JSONObject json, String key ) {
		String val = getString(json, key, null);
		if( (val != null) && (val.length() == 0) ) {
			val = null;
		}

		return val;
	}

	/**
	 * Tries to read JSON object's Integer property stored with given key. If such mapping does not exist, or there is
	 * value type mismatch, defaultValue will be returned
	 *
 	 * @param json JSON object to get the property from
	 * @param key property key
	 * @param defaultValue value to be returned if no key mapping exists
	 * @return
	 */
	public static Integer getInt( JSONObject json, String key, Integer defaultValue ) {

		Integer result = defaultValue;

		if( json != null ) {
			try {
				result = json.getInt( key );
			} catch( Exception e ) {}
		}

		return result;
	}

	public static Double getDouble( JSONObject json, String key ) {
		return getDouble( json, key, 0.0d );
	}
	public static Double getDouble( JSONObject json, String key, Double defaultValue ) {

		double result = defaultValue;

		if( json != null ) {
			try {
				result = json.getDouble(key);
			} catch( Exception e ) {}
		}

		return result;
	}


	/**
	 * Tries to read JSON object's embeded object property stored with given key. If such mapping does not exist, or there is
	 * value type mismatch, defaultValue will be returned
	 *
 	 * @param json JSON object to get the property from
	 * @param key property key
	 * @return returns found {@link JSONObject} or <code>null</code> if no key found
	 */
	public static JSONObject getJSONObject( JSONObject json, String key ) {
		return getJSONObject( json, key, null );
	}

	/**
	 * Tries to read JSON object's embeded object property stored with given key. If such mapping does not exist, or there is
	 * value type mismatch, defaultValue will be returned
	 *
 	 * @param json JSON object to get the property from
	 * @param key property key
	 * @param defaultValue value to be returned if no key mapping exists
	 * @return returns found {@link JSONObject} or defaultValue if no key found
	 */
	public static JSONObject getJSONObject( JSONObject json, String key, JSONObject defaultValue ) {

		JSONObject result = defaultValue;

		if( json != null ) {
			try {
				result = json.getJSONObject( key );
			} catch( Exception e ) {}
		}

		return result;
	}

	/**
	 * Tries to read JSON object's array property stored with given key. If such mapping does not exist, or there is
	 * value type mismatch, defaultValue will be returned
	 *
 	 * @param json JSON object to get the property from
	 * @param key property key
	 * @return returns {@link JSONArray} or empty {@link JSONArray} if no key found
	 */
	public static JSONArray getJSONArray( JSONObject json, String key ) {
		return getJSONArray(json, key, new JSONArray());
	}

	/**
	 * Tries to read JSON object's array property stored with given key. If such mapping does not exist, or there is
	 * value type mismatch, defaultValue will be returned
	 *
 	 * @param json JSON object to get the property from
	 * @param key property key
	 * @param defaultValue value to be returned if no key mapping exists
	 * @return returns {@link JSONArray} or defaultValue if no key found
	 */
	public static JSONArray getJSONArray( JSONObject json, String key, JSONArray defaultValue ) {

		JSONArray result = defaultValue;

		if( json != null ) {
			try {
				result = json.getJSONArray( key );
			} catch( Exception e ) {}
		}

		return result;
	}


// end of class
}