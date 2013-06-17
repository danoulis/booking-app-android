package com.tdispatch.passenger.api;

import com.tdispatch.passenger.common.Const;
import com.tdispatch.passenger.core.TDApplication;
import com.webnetmobile.tools.WebnetLog;

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
public abstract class ApiHelperCore
{
	protected static TDApplication mApplication = null;

	protected int lastErrorCode = Const.ErrorCode.OK;
	protected String lastErrorMessage = "";

	public ApiHelperCore() {
		// dummy
	}

	public ApiHelperCore(TDApplication app) {
		super();
		setApplication(app);
	}

	public void setApplication( TDApplication app ) {
		mApplication = app;
	}

	public int getLastErrorCode() {
		return lastErrorCode;
	}

	public void setLastErrorCode( int lastErrorCode ) {
		this.lastErrorCode = lastErrorCode;
	}

	public String getLastErrorMessage() {
		return lastErrorMessage;
	}

	public void setLastErrorMessage( String lastErrorMessage ) {
		this.lastErrorMessage = lastErrorMessage;
	}

	protected void cleanErrors() {
		setLastErrorCode(Const.ErrorCode.OK);
		setLastErrorMessage("");
	}

	protected final static int TYPE_GET = 0;
	protected final static int TYPE_POST = 1;

	protected ApiResponse doGetRequest( ApiRequest req ) {
		return doRequest(TYPE_GET, req);
	}

	protected ApiResponse doPostRequest( ApiRequest req ) {
		return doRequest(TYPE_POST, req );
	}

	protected ApiResponse doRequest( int type, ApiRequest request ) {

		ApiNetworker apiNetworker = ApiNetworker.getInstance(mApplication);
		ApiResponse response = new ApiResponse();

		switch( type ){
			case TYPE_GET:
				response = apiNetworker.sendGet(request);
				break;

			case TYPE_POST:
				response = apiNetworker.sendPost(request);
				break;
		}

		WebnetLog.i("response: " + response.getErrorCode());
		return response;
	}

}
