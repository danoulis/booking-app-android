package com.tdispatch.passenger.data.manager;

import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.model.AccountData;

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
final public class SessionManager
{
	protected TDApplication mContext;
	protected SharedPreferences mPrefs;

	protected final String KEY_ACCESS_TOKEN					= "access_token";
	protected final String KEY_ACCESS_TOKEN_EXPIRATION_MILLIS	= "access_token_expiration_millis";
	protected final String KEY_REFRESH_TOKEN					= "refresh_token";
	protected final String KEY_ACCOUNT_DATA					= "account_data";

	public SessionManager( TDApplication context ) {
		mContext = context;

		mPrefs = mContext.getSharedPreferences("session_manager", Context.MODE_PRIVATE);;
	}

	protected static SessionManager mInstance = null;
	public static SessionManager getInstance( TDApplication context ) {
		if( mInstance == null ) {
			mInstance = new SessionManager(context);
		}

		return mInstance;
	}


	public String getAccessToken() {
		return mPrefs.getString(KEY_ACCESS_TOKEN, null);
	}


	public SessionManager setAccessToken( String accessToken ) {
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putString( KEY_ACCESS_TOKEN, accessToken );
		editor.commit();

		return this;
	}

	public SessionManager setAccessTokenExpirationMillis( long millis ) {
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putLong( KEY_ACCESS_TOKEN_EXPIRATION_MILLIS, millis );
		editor.commit();

		return this;
	}
	public long getAccessTokenExpirationMillis() {
		return mPrefs.getLong(KEY_ACCESS_TOKEN_EXPIRATION_MILLIS, 0);
	}


	public String getRefreshToken() {
		return mPrefs.getString(KEY_REFRESH_TOKEN, null);
	}
	public SessionManager setRefreshToken( String refreshToken ) {
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putString( KEY_REFRESH_TOKEN, refreshToken );
		editor.commit();

		return this;
	}



	public SessionManager putAccountData( AccountData data ) {
		SharedPreferences.Editor editor = mPrefs.edit();
		if( data != null ) {
			editor.putString( KEY_ACCOUNT_DATA, data.toJSON().toString() );
		} else {
			editor.putString( KEY_ACCOUNT_DATA, null );
		}
		editor.commit();

		return this;
	}


	public AccountData getAccountData() {
		AccountData profile = null;

		String jsonStr = mPrefs.getString(KEY_ACCOUNT_DATA, null);
		if( jsonStr != null ) {
			try {
				profile = new AccountData( new JSONObject( jsonStr ) );
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}

		return profile;
	}


	public SessionManager doLogout() {
		setAccessToken( null );
		setRefreshToken( null );
		putAccountData( null );

		return this;
	}

	// end of class
}