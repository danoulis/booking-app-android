package com.tdispatch.passenger.core;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.tdispatch.passenger.common.Const;
import com.tdispatch.passenger.model.OfficeData;

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
final public class OfficeManager
{
	protected TDApplication mContext;
	protected OfficeManager mOfficeManager;
	protected SharedPreferences mPrefs;

	protected OfficeData mOfficeData;

	protected static OfficeManager _instance = null;
	public static OfficeManager getInstance(TDApplication context) {
		if( _instance == null ) {
			_instance = new OfficeManager(context);
		}

		return _instance;
	}

	public OfficeManager( TDApplication context ) {
		mContext = context;

		load();
	}

	public OfficeData get() {
		return mOfficeData;
	}

	public OfficeManager put( OfficeData data ) {
		mOfficeData = data;
		save();

		return this;
	}
	public OfficeManager load() {
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		mOfficeData = new OfficeData(	mPrefs.getString( Const.FleetData.NAME, "---" ),
										mPrefs.getString( Const.FleetData.PHONE, "---" ),
										mPrefs.getString( Const.FleetData.EMAIL, "---" )
									);

		return this;
	}
	public OfficeManager save() {

		if( mOfficeData != null ) {
			SharedPreferences.Editor editor = mPrefs.edit();

			editor.putString( Const.FleetData.NAME, mOfficeData.getName() );
			editor.putString( Const.FleetData.PHONE, mOfficeData.getPhone() );
			editor.putString( Const.FleetData.EMAIL, mOfficeData.getEmail() );

			editor.commit();
		}

		return this;
	}


} // end of class
