package com.tdispatch.passenger.common;

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
public final class Const
{
	/* PUT YOUR API ACCESS DATA HERE */
	public final class Api {

		public static final String BaseDomain		= "api.tdispatch.com";
		public static final String ApiKey			= "API-KEY";
		public static final String ClientId			= "CLIENT-ID@tdispatch.com";
		public static final String ClientSecret	= "SECRET";


		/*****************************************************************************************/

		public static final String	BaseUrl				= "https://" + BaseDomain;
		public static final String	OAuthTokensUrl		= BaseUrl + "/passenger/oauth2/token";
		public static final String	OauthAuthUrl		= BaseUrl + "/passenger/oauth2/auth";

		public static final String	ApiUrl				= BaseUrl + "/passenger/v1/";

		public static final String	AccountNew			= ApiUrl + "accounts";
		public static final String	AccountProfile		= ApiUrl + "accounts/preferences";
		public static final String	AccountFleetData	= ApiUrl + "accounts/fleetdata";

		public static final String	BookingsGetAll		= ApiUrl + "bookings";
		public static final String	BookingsNew			= ApiUrl + "bookings";
		public static final String	BookingsCancelFmt	= ApiUrl + "bookings/%s/cancel";
		public static final String	BookingsTrackFmt	= ApiUrl + "bookings/track";

		public static final String	LocationSearch		= ApiUrl + "locations/search";
		public static final String	LocationFare		= ApiUrl + "locations/fare";

		public static final String	DriversNearby		= ApiUrl + "drivers/nearby";
	}


	// helpers
	public static String getApiKey() {
		return Api.ApiKey;
	}
	public static String getOAuthClientId() {
		return Api.ClientId;
	}
	public static String getOAuthSecret() {
		return Api.ClientSecret;
	}


	public final class Bundle
	{
		public static final String BUNDLE = "bundle";
		public static final String PAGE = "page";
		public static final String CMD = "cmd";
		public static final String MODE = "mode";
		public static final String REQUEST_CODE = "request_code";
		public static final String TYPE = "type";
		public static final String LOCATION = "location";
		public static final String BOOKING = "booking";
	}

	public final class ErrorCode
	{
		public static final int OK = 1;
		public static final int UNKNOWN_ERROR = 0;
		public static final int API_ERROR = -1;
		public static final int EXCEPTION_ERROR = -2;
	}

	public final class RequestCode {
		public static final int ADDRESS_SEARCH = 0;
	}
	public final class Sound
	{
		public static final int CLICK 	= 0;
		public static final int ERROR 	= 1;
		public static final int BUZZ	 	= 2;
		public static final int CONFIRM	= 3;
	}

	public final class Tag
	{
		public static final String FRAGMENT 					= "fragment";
		public static final String MAP_FRAGMENT 				= "map_fragment";
		public static final String ADDRESS_SEARCH_FRAGMENT 	= "address_search_fragment";
		public static final String MENU_FRAGMENT_CONTENT		= "menu_fragment_content";
		public static final String BOOKING_LIST_FRAGMENT		= "booking_list_fragment";
	}

	public final class Font
	{
		public static final String REGULAR	= "fonts/OpenSans-Light.ttf";
		public static final String BOLD	 	= "fonts/OpenSans-Semibold.ttf";
	}

	public final class FleetData {
		public static final String NAME = "fleet_data_name";
		public static final String PHONE = "fleet_data_phone";
		public static final String EMAIL = "fleet_data_email";
	}
}