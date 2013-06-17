package com.tdispatch.passenger.model;

import org.json.JSONObject;

import com.tdispatch.passenger.core.TDApplication;
import com.webnetmobile.tools.JsonTools;

/*
 ******************************************************************************
 *
 * Copyright (C) 2013 T Dispatch Ltd
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
public class AccountData
{
	// birthdate
	protected String		mFirstName;
	protected String		mLastName;
	protected String 		mEmail;
	protected String 		mPhone;
	protected LocationData	mLocation;
	protected Boolean 		mUseLocationAsPickup;
	// receive email notifications bool
	// receive sms notifications bool
	// username String


	protected String		mPassword;		// this is NOT stored in the app. We got it here to use AccountData for new account registration only


	// -------------------------------------------------------------------------------------------------------------

	public AccountData() {
		// dummy
	}


	public AccountData( JSONObject json ) {
		set( json );
	}

	// -------------------------------------------------------------------------------------------------------------

	public AccountData setFirstName( String name ) {
		mFirstName = name;
		return this;
	}
	public String getFirstName() {
		return mFirstName;
	}


	public AccountData setLastName( String name ) {
		mLastName = name;
		return this;
	}
	public String getLastName() {
		return mLastName;
	}


	public AccountData setEmail( String email ) {
		mEmail = email;
		return this;
	}
	public String getEmail() {
		return mEmail;
	}


	public AccountData setPhone( String phone ) {
		mPhone = phone;
		return this;
	}
	public String getPhone() {
		return mPhone;
	}


	public AccountData setLocation( LocationData data ) {
		mLocation = data;
		return this;
	}
	public LocationData getLocation() {
		return mLocation;
	}


	public AccountData setUseMyLocationAsPickup( Boolean val ) {
		mUseLocationAsPickup = val;
		return this;
	}
	public Boolean getUseMyLocationAsPickup() {
		return mUseLocationAsPickup;
	}
	public Boolean useMyLocationAsPickup() {
		return getUseMyLocationAsPickup();
	}




	public AccountData setPassword( String password ) {
		mPassword = password;
		return this;
	}

	public String getPassword() {
		return mPassword;
	}


	// -------------------------------------------------------------------------------------------------------------

	public String getFullName() {
		String fullName = "";

		if( getFirstName() != null ) {
			fullName += getFirstName().trim() + " ";
		}

		if( getLastName() != null ) {
			fullName += getLastName().trim();
		}

		return fullName.trim();
	}


	// -------------------------------------------------------------------------------------------------------------

	public AccountData set( JSONObject json ) {

		setFirstName( JsonTools.getString(json, "first_name") );
		setLastName( JsonTools.getString(json, "last_name") );
		setLocation( new LocationData( JsonTools.getJSONObject(json, "location") ) );
		setEmail( JsonTools.getString(json, "email") );
		setPhone( JsonTools.getString(json, "phone") );

		JSONObject prefs = JsonTools.getJSONObject(json, "preferences");
			setUseMyLocationAsPickup( JsonTools.getBoolean(prefs, "use_account_location_as_pickup", false) );

		return this;
	}


	// -------------------------------------------------------------------------------------------------------------

	public JSONObject toJSON() {

		JSONObject json = new JSONObject();

		try {
			json.put("email", getEmail() );
			json.put("first_name", getFirstName());
			json.put("last_name", getLastName());
			json.put("phone", getPhone());
			json.put("location", mLocation.toJSON() );

			JSONObject prefs = new JSONObject();
				prefs.put("use_account_location_as_pickup", getUseMyLocationAsPickup() );
				json.put("preferences", prefs);

		} catch ( Exception e ) {
			e.printStackTrace();
		}

		return json;
	}



	// storage

	public static AccountData get() {
		return TDApplication.getSessionManager().getAccountData();
	}

	public AccountData put() {
		TDApplication.getSessionManager().putAccountData( this );
		return this;
	}

}
