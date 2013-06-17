package com.tdispatch.passenger.api;

import org.json.JSONObject;

import android.os.Looper;

import com.google.android.gms.maps.model.LatLng;
import com.tdispatch.passenger.common.Const;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.model.AccountData;
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
final public class ApiHelper extends ApiHelperCore
{
	private static ApiHelper _instance = null;

	public static ApiHelper getInstance( TDApplication app ) {
		if( _instance == null ) {
			_instance = new ApiHelper();
			_instance.setApplication(app);
		}

		if( Looper.getMainLooper().equals(Looper.myLooper()) ) {
			WebnetLog.e("ERROR: instantiated from UI thread!");
		}

		return (_instance);
	}


	/**[ helpers ]*******************************************************************************************/

	public ApiResponse getOAuthTokens( String tmpAuthCode ) {
		ApiRequest req = new ApiRequest( Const.Api.OAuthTokensUrl );
		req.addPostParam("code", tmpAuthCode);
		req.addPostParam("client_id", Const.getOAuthClientId());
		req.addPostParam("client_secret", Const.getOAuthSecret());
		req.addPostParam("redirect_url", "" );
		req.addPostParam("grant_type", "authorization_code");

		return doPostRequest(req );
	}

	public ApiResponse refreshOAuthAccessToken( String refreshToken ) {
		ApiRequest req = new ApiRequest( Const.Api.OAuthTokensUrl );
		req.addPostParam("refresh_token", refreshToken );
		req.addPostParam("client_id", Const.getOAuthClientId());
		req.addPostParam("client_secret", Const.getOAuthSecret());
		req.addPostParam("grant_type", "refresh_token");

		return doPostRequest( req );
	}



	// Accounts
	public ApiResponse accountCreate( AccountData account ) {
		ApiRequest req = new ApiRequest( Const.Api.AccountNew );
		req.addGetParam("key", Const.Api.ApiKey);

		req.addRequestParam("first_name", account.getFirstName());
		req.addRequestParam("last_name", account.getLastName());
		req.addRequestParam("email", account.getEmail());
		req.addRequestParam("phone", account.getPhone());
		req.addRequestParam("password", account.getPassword());

		req.addRequestParam("client_id", Const.Api.ClientId);

		return doPostRequest(req);
	}

	public ApiResponse getAccountProfile() {
		ApiRequest req = new ApiRequest( Const.Api.AccountProfile, TDApplication.getSessionManager().getAccessToken() );
		return doGetRequest( req );
	}

	public ApiResponse getAccountFleetData() {
		ApiRequest req = new ApiRequest( Const.Api.AccountFleetData, TDApplication.getSessionManager().getAccessToken() );
		return doGetRequest( req );
	}


	// Location search
	public ApiResponse locationSearch( String search, int limit, Boolean narrowToPickupOnly ) {
		ApiRequest req = new ApiRequest( Const.Api.LocationSearch, TDApplication.getSessionManager().getAccessToken() );

		req.addGetParam("q", search );
		req.addGetParam("limit", limit);
		if( narrowToPickupOnly ) {
			req.addGetParam("type", "pickup");
		}
//		req.addGetParam("source", "googlemaps");

		return doGetRequest(req);
	}

	// fare calculation
	public ApiResponse locationFare( LatLng from, LatLng to ) {
		ApiRequest req = new ApiRequest( Const.Api.LocationFare, TDApplication.getSessionManager().getAccessToken() );

		try {
			JSONObject pickup = new JSONObject();
				pickup.put("lat", from.latitude);
				pickup.put("lng", from.longitude);
			req.addRequestParam("pickup_location", pickup);

			JSONObject dropoff = new JSONObject();
				dropoff.put("lat", to.latitude);
				dropoff.put("lng", to.longitude);
			req.addRequestParam("dropoff_location", dropoff);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return doPostRequest(req);
	}

	/**[ bookings ]*****************************************************************************************************/

	public ApiResponse bookingsGetAll(String status) {
		ApiRequest req = new ApiRequest( Const.Api.BookingsGetAll, TDApplication.getSessionManager().getAccessToken() );

		req.addGetParam("order_by", "-pickup_time");
		req.addGetParam("status", status);
		req.addGetParam("limit", 20);
		req.addGetParam("offset", 0);

		return doGetRequest(req);
	}

	// new booking
	public ApiResponse bookingsNewBooking( JSONObject newBookingJson ) {
		ApiRequest req = new ApiRequest( Const.Api.BookingsNew, TDApplication.getSessionManager().getAccessToken() );
		req.setRequestParameters(newBookingJson);

		return doPostRequest(req);
	}

	public ApiResponse bookingsCancelBooking( String bookingPk ) {
		String url = String.format(Const.Api.BookingsCancelFmt, bookingPk);
		ApiRequest req = new ApiRequest( url, TDApplication.getSessionManager().getAccessToken() );

		return doPostRequest(req);
	}

	public ApiResponse bookingsTrackBooking( String bookingPk ) {
		String url = String.format(Const.Api.BookingsTrackFmt, bookingPk);
		ApiRequest req = new ApiRequest( url, TDApplication.getSessionManager().getAccessToken() );

		return doPostRequest(req);
	}

	/**[ drivers ]******************************************************************************************************/

	public ApiResponse getNearbyDrivers(LatLng position ) {
		ApiRequest req = new ApiRequest( Const.Api.DriversNearby, TDApplication.getSessionManager().getAccessToken() );

		req.addRequestParam("limit",  15);		// #of cabs
		req.addRequestParam("radius", 10);		// km

		try {
			JSONObject json = new JSONObject();
			json.put("lat", position.latitude);
			json.put("lng", position.longitude);

			req.addRequestParam("location", json);
		} catch( Exception e ) {
			e.printStackTrace();
		}

		return doPostRequest(req);
	}
}