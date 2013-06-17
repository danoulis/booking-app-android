package com.tdispatch.passenger.api;

import org.json.JSONObject;

import com.tdispatch.passenger.common.Const;

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
final public class ApiResponse
{
	private int mErrorCode = Const.ErrorCode.UNKNOWN_ERROR;
	private String mErrorMessage = "";
	private Exception mException = null;

	private int mApiErrorCode = 0; // only set if errorCode == API_ERROR
	private String mApiErrorMessage = "";

	private JSONObject mJsonObject = null;

	public void setErrorCode( int errorCode ) {
		mErrorCode = errorCode;
	}

	public int getErrorCode() {
		return (mErrorCode);
	}

	public void setErrorMessage( String errorMessage ) {
		mErrorMessage = errorMessage;
	}

	public String getErrorMessage() {
		return mErrorMessage;
	}

	public void setException( Exception e ) {
		mException = e;
	}

	public Exception getException() {
		return mException;
	}

	public void setJSONObject( JSONObject data ) {
		mJsonObject = data;
	}

	public JSONObject getJSONObject() {
		return (mJsonObject);
	}

	public void setApiErrorCode( int code ) {
		mApiErrorCode = code;
	}

	public int getApiErrorCode() {
		return mApiErrorCode;
	}

	public void setApiErrorMessage( String msg ) {
		mApiErrorMessage = msg;
	}

	public String getApiErrorMessage() {
		return mApiErrorMessage;
	}

}