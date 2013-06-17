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
public class OfficeData
{
	protected String mName;
	protected String mEmail;
	protected String mPhone;

	// -------------------------------------------------------------------------------------------------------------

	public OfficeData() {
		// dummy
	}

	public OfficeData(String name, String phone, String email) {
		mName = name;
		mPhone = phone;
		mEmail = email;
	}

	// -------------------------------------------------------------------------------------------------------------

	public OfficeData setName( String name ) {
		mName = name;
		put();
		return this;
	}

	public String getName() {
		return mName;
	}

	public OfficeData setEmail( String email ) {
		mEmail = email;
		put();
		return this;
	}

	public String getEmail() {
		return mEmail;
	}

	public OfficeData setPhone( String phone ) {
		mPhone = phone;
		put();
		return this;
	}

	public String getPhone() {
		return mPhone;
	}


	// -------------------------------------------------------------------------------------------------------------

	public Boolean hasEmail() {
		return ( (mEmail != null) && (mEmail.length() > 6) );
	}
	public Boolean hasPhone() {
		return ( (mPhone != null) && (mPhone.length() > 4) );
	}

	// -------------------------------------------------------------------------------------------------------------

	public OfficeData set( JSONObject json ) {

		setName(JsonTools.getString(json, "name"));
		setEmail(JsonTools.getString(json, "email"));
		setPhone(JsonTools.getString(json, "phone"));

		put();

		return this;
	}

	// storage

	public static OfficeData get() {
		return TDApplication.getOfficeManager().get();
	}

	public OfficeData put() {
		TDApplication.getOfficeManager().put(this);
		return this;
	}

}
