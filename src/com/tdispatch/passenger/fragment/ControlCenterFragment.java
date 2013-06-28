package com.tdispatch.passenger.fragment;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.tdispatch.passenger.R;
import com.tdispatch.passenger.SearchActivity;
import com.tdispatch.passenger.api.ApiHelper;
import com.tdispatch.passenger.api.ApiResponse;
import com.tdispatch.passenger.common.Const;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.core.TDFragment;
import com.tdispatch.passenger.fragment.dialog.BookingConfirmationDialogFragment;
import com.tdispatch.passenger.fragment.dialog.BookingConfirmationDialogFragment.BookingConfirmationDialogClickListener;
import com.tdispatch.passenger.fragment.dialog.GenericDialogFragment;
import com.tdispatch.passenger.fragment.dialog.NoLocationDialogFragment;
import com.tdispatch.passenger.host.BookingListHostInterface;
import com.tdispatch.passenger.host.CommonHostInterface;
import com.tdispatch.passenger.host.SlideMenuHostInterface;
import com.tdispatch.passenger.model.AccountData;
import com.tdispatch.passenger.model.ApiSearchLocationData;
import com.tdispatch.passenger.model.BookingData;
import com.tdispatch.passenger.model.LocationData;
import com.tdispatch.passenger.model.PickupAndDropoff;
import com.webnetmobile.tools.GoogleMapRouteHelper;
import com.webnetmobile.tools.JsonTools;
import com.webnetmobile.tools.WebnetLog;
import com.webnetmobile.tools.WebnetTools;

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
public class ControlCenterFragment extends TDFragment implements BookingConfirmationDialogClickListener
{
	protected static final String PREFS_KEY_LAST_LOCATION_LAT = "prefs_last_location_lat";
	protected static final String PREFS_KEY_LAST_LOCATION_LNG = "prefs_last_location_lng";


	protected Handler mHandler = new Handler();

	protected LocationData mAddressMapPointsTo = null;

	protected LocationData mPickupAddress 	= null;
	protected LocationData mDropoffAddress	= null;

	protected LocationManager mLocationManager = null;

	@Override
	protected int getLayoutId() {
		return R.layout.control_center_fragment;
	}

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate(savedInstanceState);

		mLocationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
	}

	protected Boolean mIitialMapLocationSet = false;

	@Override
	public void onResume() {
		super.onResume();

		startCabTracking();
		startBookingTracking();

		if( mIitialMapLocationSet == false ) {
			Location tmp = getMyLocation();
			LatLng loc = null;
			if( tmp != null ) {
				loc = new LatLng( tmp.getLatitude(), tmp.getLongitude() );
				mIitialMapLocationSet = true;
			} else {
				double defaultLat = Double.valueOf( getString(R.string.caboffice_default_location_latitude).replace(",", ".") );
				double defaultLng = Double.valueOf( getString(R.string.caboffice_default_location_longitude).replace(",", ".") );

				String latTmp = mPrefs.getString(PREFS_KEY_LAST_LOCATION_LAT, null);
				String lngTmp = mPrefs.getString(PREFS_KEY_LAST_LOCATION_LNG, null);

				double lat = (latTmp == null) ? defaultLat : Double.valueOf(latTmp);
				double lng = (lngTmp == null) ? defaultLng : Double.valueOf(lngTmp);

				loc = new LatLng(lat, lng);
			}

			moveMapToLocation(loc, true, true);
		}
	}

	@Override
	public void onPause() {
		stopBookingTracking();
		stopCabTracking();

		Location loc = getMyLocation();
		if( loc != null ) {
			SharedPreferences.Editor editor = mPrefs.edit();
			editor.putString(PREFS_KEY_LAST_LOCATION_LAT, "" + loc.getLatitude() );
			editor.putString(PREFS_KEY_LAST_LOCATION_LNG, "" + loc.getLongitude() );
			editor.commit();
		}

		super.onPause();
	}


	protected BookingListHostInterface mBookingListHostActivity;
	protected CommonHostInterface mCommonHostActivity;
	protected SlideMenuHostInterface mSlideMenuHostInterface;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mBookingListHostActivity = (BookingListHostInterface)activity;
		mSlideMenuHostInterface = (SlideMenuHostInterface)activity;
		mCommonHostActivity = (CommonHostInterface)activity;
	}


	@Override
	protected void onPostCreateView() {

		initBusyIndicators();
		showAimPoint( true );

		// try to fix another GMaps v2 related issue
		// http://code.google.com/p/gmaps-api-issues/issues/detail?id=4639
		ViewGroup mapHost = (ViewGroup)mFragmentView.findViewById(R.id.map_container);
		mapHost.requestTransparentRegion(mapHost);

		updateAddresses();
		setUIControlsVisibility(true);

		// disable debug controls
		WebnetTools.setVisibility(mFragmentView, R.id.debug_container, TDApplication.isDebuggable() ? View.INVISIBLE : View.INVISIBLE);

		MapFragment mapFragment = (MapFragment)mFragmentManager.findFragmentById(R.id.map_fragment);
		if( mapFragment != null ) {
			CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo( 15f );

			GoogleMap map = mapFragment.getMap();

			Location currentPosition = map.getMyLocation();
			if( currentPosition != null ) {
				LatLng latLng = new LatLng(currentPosition.getLatitude(), currentPosition.getLongitude());
				cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f);
			}
			map.moveCamera(cameraUpdate);

			map.setOnCameraChangeListener(mMapCameraListener);
			map.setOnMapClickListener( mOnMapClickListener );
			map.setOnMyLocationChangeListener( mOnMyLocationChangeListener );
		} else {
			throw new IllegalStateException("Map is not ready");
		}


	    WebnetTools.setVisibility( mFragmentView, R.id.price_box_container, View.INVISIBLE );


	    int ids[] = {
        				R.id.pickup_location, R.id.dropoff_location,

        				R.id.button_mylocation, R.id.button_book,
        				R.id.button_start_new_booking,

        				R.id.left_menu_drag_handle, R.id.right_menu_drag_handle,

        				R.id.button_set_as_pickup, R.id.button_set_as_dropoff,
        		};
        for( int id : ids ) {
        	View v = mFragmentView.findViewById( id );
        	if( v != null ) {
        		v.setOnClickListener( mOnClickListener );
        	}
        }

        ids = new int[] { R.id.button_set_as_pickup, R.id.button_set_as_dropoff, R.id.button_mylocation };
        for( int id : ids ) {
        	View v = mFragmentView.findViewById( id );
        	if( v != null ) {
        		v.setOnLongClickListener( mOnLongClickListener );
        	}
        }


        // unveil map
		View mapCurtain = mFragmentView.findViewById(R.id.map_curtain);
		mapCurtain.startAnimation( AnimationUtils.loadAnimation(TDApplication.getAppContext(), R.anim.map_curtain_fade_out));
	}


	/**[ map related listeners ]**************************************************************************************/

    protected OnCameraChangeListener mMapCameraListener = new OnCameraChangeListener()
	{
		@Override
		public void onCameraChange( CameraPosition position ) {
			mIitialMapLocationSet = true;

			showAimPoint();
			doReverseGeoCoding( position.target );
		}
	};

	protected GoogleMap.OnMapClickListener mOnMapClickListener = new OnMapClickListener()
	{
		@Override
		public void onMapClick( LatLng point ) {
			mIitialMapLocationSet = true;
			showAimPoint(true);
		}
	};

	protected GoogleMap.OnMyLocationChangeListener mOnMyLocationChangeListener = new GoogleMap.OnMyLocationChangeListener()
	{
		@Override
		public void onMyLocationChange( Location location ) {

			if( mIitialMapLocationSet == false ) {
				LatLng loc = new LatLng( location.getLatitude(), location.getLongitude() );
				moveMapToLocation(loc, false, false);
				mIitialMapLocationSet = true;
			}
		}
	};


    /**[ reverse geocoder ]*******************************************************************************************/

	protected AtomicBoolean mReverseGeoCodingRunning = new AtomicBoolean();
	protected ConcurrentLinkedQueue<LatLng> mReverseQueue = new ConcurrentLinkedQueue<LatLng>();

	protected Boolean isReverseGeoCoderRunning() {
		return mReverseGeoCodingRunning.get();
	}

	protected void doReverseGeoCoding( Location pos ) {
		if( pos != null ) {
			doReverseGeoCoding( new LatLng(pos.getLatitude(), pos.getLongitude()) );
		}
	}

	protected void doReverseGeoCoding( LatLng pos ) {
		mReverseQueue.add(pos);

		if( mReverseGeoCodingRunning.compareAndSet(false, true) ) {
			mAddressMapPointsTo = null;
			WebnetTools.executeAsyncTask( new ReverseGeoAsyncTask() );
		}
	}

	public class ReverseGeoAsyncTask extends AsyncTask<Void, Void, LocationData> {

		@Override
		protected void onPreExecute() {
			showBusy(BUSY_GETTING_MAP_ADDRESS);
			WebnetTools.setText( mFragmentView, R.id.debug_current_address, "ReverseGeoCoding in progress" );
		}

		@Override
		protected LocationData doInBackground(Void ... params) {

			LocationData result = null;

			do {
				LatLng lastLocation = null;

				while( mReverseQueue.isEmpty() == false ) {
					lastLocation = mReverseQueue.poll();
				}

				result = getReverseUsingGoogleApis( lastLocation );

			} while (mReverseQueue.isEmpty() == false);

			return result;
		}

		@Override
		protected void onPostExecute(LocationData addr) {

			setAddressMapPointsTo( addr );

			if( addr != null ) {
				WebnetTools.setText( mFragmentView, R.id.debug_current_address, addr.getAddress() );
			} else {
				WebnetTools.setText( mFragmentView, R.id.debug_current_address, "*** Failed to get address of location ***" );
			}

			mReverseGeoCodingRunning.set(false);

			hideBusy(BUSY_GETTING_MAP_ADDRESS);
		}

		protected LocationData getReverseUsingGoogleApis( LatLng loc ) {

			LocationData result = null;

			String address1 = "", address2 = "", city = "", state = "", country = "", county = "", postCode = "";
			try {
				String url = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + loc.latitude +  "," + loc.longitude + "&sensor=true";

				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost( url );
				HttpResponse response;

				response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				InputStream is = null;

				is = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"), 8);
				StringBuilder sb = new StringBuilder();
				sb.append(reader.readLine() + "\n");
				String line = "0";
				while( (line = reader.readLine()) != null ) {
					sb.append(line + "\n");
				}
				is.close();
				reader.close();

				JSONObject jsonObject = new JSONObject( sb.toString() );

				String Status = jsonObject.getString("status");
				if(Status.equalsIgnoreCase("OK")) {
					JSONArray Results = jsonObject.getJSONArray("results");
					JSONObject zero = Results.getJSONObject(0);
					JSONArray addressComponents = zero.getJSONArray("address_components");

					for(int i=0; i<addressComponents.length(); i++) {
						JSONObject zero2 = addressComponents.getJSONObject(i);

						String longName = zero2.getString("long_name");
						JSONArray typesArray = zero2.getJSONArray("types");

						for( int typeIdx=0; typeIdx<typesArray.length(); typeIdx++ ) {
							String singleType = typesArray.getString(typeIdx);

							if( TextUtils.isEmpty(longName) == false ) {

								if(singleType.equalsIgnoreCase("street_number")) {
									address1 = longName + " ";
								}
								else if(singleType.equalsIgnoreCase("route")) {
										address1 += longName;
								}
								else if(singleType.equalsIgnoreCase("sublocality")) {
									address2 = longName;
								}
								else if(singleType.equalsIgnoreCase("locality")) {
									city = longName;
								}
								else if(singleType.equalsIgnoreCase("postal_town")) {
									city = longName;
								}
								else if(singleType.equalsIgnoreCase("administrative_area_level_2")) {
									county = longName;
								}
								else if(singleType.equalsIgnoreCase("administrative_area_level_1")) {
									state = longName;
								}
								else if(singleType.equalsIgnoreCase("country")) {
									country = longName;
								}
								else if(singleType.equalsIgnoreCase("postal_code")) {
									postCode = longName;
								}
							}
						}
					}

					ApiSearchLocationData tmp = new ApiSearchLocationData();

					tmp.setAddress( address1 );
					tmp.setTown( city );
					tmp.setPostCode(postCode);
					tmp.setCountry(country);
					tmp.setCounty(county);
					tmp.setLocation( loc );

					result = new LocationData( tmp );
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return result;
		}
	}


	/**[ busy indicators ]********************************************************************************************/

	protected static final int BUSY_GETTING_ROUTE_AND_PRICE 	= 1;
	protected static final int BUSY_GETTING_MAP_ADDRESS		= 2;

	public void showBusy(int what) {
		doShowHideBusy(what, +1);
	}
	public void hideBusy(int what) {
		doShowHideBusy(what, -1);
	}

	protected int mBookBusyCount = 0;
	protected int mPickupDropoffBusyCount = 0;
	protected void initBusyIndicators() {
		doShowHideBusy(0, 0);
	}
	protected void doShowHideBusy(int what, int step) {

		switch( what ) {
			case BUSY_GETTING_MAP_ADDRESS: {
				mPickupDropoffBusyCount += step;
			}
			break;

			case BUSY_GETTING_ROUTE_AND_PRICE: {
				mPickupDropoffBusyCount += step;
				mBookBusyCount += step;
			}
			break;
		}


		View v = mFragmentView.findViewById( R.id.busy_container );
		if( v != null ) {
			AnimationDrawable bookBusyAnim = (AnimationDrawable)((ImageView)mFragmentView.findViewById(R.id.busy)).getBackground();

			if( (mBookBusyCount > 0) ) {
				bookBusyAnim.start();
				v.setVisibility(View.VISIBLE);
			} else {
				v.setVisibility(View.GONE);
				bookBusyAnim.stop();
			}
		}


		v = mFragmentView.findViewById( R.id.map_aim_point_busy_container );
		if( v != null ) {
			AnimationDrawable mapAimBusyAnim = (AnimationDrawable)((ImageView)mFragmentView.findViewById(R.id.map_aim_point_busy)).getBackground();
			if( (mPickupDropoffBusyCount > 0) ) {
				mapAimBusyAnim.start();
				v.setVisibility(View.VISIBLE);
			} else {
				v.setVisibility(View.GONE);
				mapAimBusyAnim.stop();
			}
		}

	}

	/**[ Get directions ]*********************************************************************************************/

	protected AtomicBoolean mRouteAndFeeRunning = new AtomicBoolean();
	protected ConcurrentLinkedQueue<PickupAndDropoff> mRouteAndFeeQueue = new ConcurrentLinkedQueue<PickupAndDropoff>();

	protected List<LatLng> mRoutePointList = null;
	protected AtomicBoolean mBookingFeeCalculated = new AtomicBoolean();
	protected String mBookingFeePriceFormatted 	= "";
	protected String mBookingFeeDistanceFormatted 	= "";

	protected void getRouteAndBookingPrice( LocationData pickup, LocationData dropoff ) {
		getRouteAndBookingPrice(
								(pickup != null) ? pickup.getLatLng() : null,
								(dropoff != null) ? dropoff.getLatLng() : null
								);
	}

	protected void getRouteAndBookingPrice( LatLng pickup, LatLng dropoff ) {
		if( (pickup != null) && (dropoff != null) ) {
			mRouteAndFeeQueue.add( new PickupAndDropoff(pickup, dropoff));
			if( mRouteAndFeeRunning.compareAndSet(false, true) ) {
				WebnetTools.executeAsyncTask( new GetRouteAndFeeAsyncTask() );
			}
		} else {
			refreshMapOverlays();
		}
	}

	protected class GetRouteAndFeeAsyncTask extends AsyncTask<Void, Void, List<LatLng>> {

		@Override
		protected void onPreExecute() {
			showBusy(BUSY_GETTING_ROUTE_AND_PRICE);
			WebnetTools.setVisibility(mFragmentView, R.id.price_container, View.INVISIBLE);
			WebnetTools.setVisibility(mFragmentView, R.id.price_box_container, View.VISIBLE);

			// clear old route, set new markers
			mRoutePointList = null;
			refreshMapOverlays();
		}

		@Override
		protected List<LatLng> doInBackground( Void ... params ) {

			List<LatLng> routePointList = null;
			PickupAndDropoff lastLocation = null;

			mBookingFeeCalculated.set(false);

			ApiHelper api = ApiHelper.getInstance(TDApplication.getAppContext());

			do {
				while( mRouteAndFeeQueue.isEmpty() == false ) {
					lastLocation = mRouteAndFeeQueue.poll();
				}

				try {
					ApiResponse feeResult = api.locationFare( lastLocation.getPickup(), lastLocation.getDropoff() );
					if( feeResult.getErrorCode() == Const.ErrorCode.OK ) {

						JSONObject feeJson = JsonTools.getJSONObject( feeResult.getJSONObject(), "fare");
						mBookingFeePriceFormatted = JsonTools.getString(feeJson, "formatted_total_cost");

						JSONObject distJson = JsonTools.getJSONObject(feeJson, "distance");
						if( WebnetTools.useMetricUnits() ) {
							mBookingFeeDistanceFormatted = String.format( getString(R.string.journey_distance_metrics_fmt), JsonTools.getString(distJson, "km") );
						} else {
							mBookingFeeDistanceFormatted = String.format( getString(R.string.journey_distance_imperial_fmt), JsonTools.getString(distJson, "miles") );
						}

						mBookingFeeCalculated.set(true);
					} else {
						WebnetLog.e("Failed to get fare from API");
					}

				} catch ( Exception e ) {
					e.printStackTrace();
				}

			} while (mRouteAndFeeQueue.isEmpty() == false);

			if( mBookingFeeCalculated.get() ) {
				GoogleMapRouteHelper gm = new GoogleMapRouteHelper( lastLocation.getPickup(), lastLocation.getDropoff() );
				routePointList = gm.getDirections();
			}

			return routePointList;
		}

		@Override
		protected void onPostExecute( List<LatLng> result ) {

			hideBusy(BUSY_GETTING_ROUTE_AND_PRICE);
			WebnetTools.setVisibility(mFragmentView, R.id.price_container, View.VISIBLE);

			mRoutePointList = result;
			refreshMapOverlays();

			if( result == null ) {
				WebnetLog.d("Failed to download directions");
			}

			mRouteAndFeeRunning.set(false);
		}
	}


	/*****************************************************************************************************************/

	protected boolean doPlaceBooking( LocationData pickup, LocationData dropoff, Long pickupMillis ) {

		int threshold = getResources().getInteger(R.integer.caboffice_settings_new_bookings_max_days_ahead);

		Boolean result = false;
		Boolean pickupMillisInvalid = false;
		String pickupMillisBodyId = "";

		if( pickupMillis != null ) {
			Long diff = (pickupMillis - System.currentTimeMillis());

			if( diff > 0 ) {

				if( diff > (WebnetTools.MILLIS_PER_MINUTE * 5) ) {
					if( diff < (WebnetTools.MILLIS_PER_DAY * threshold) ) {
						// 	keep it
					} else {
						pickupMillisBodyId  = getString(R.string.new_booking_pickup_date_too_ahead_body_fmt, threshold);
						pickupMillisInvalid = true;
					}
				} else {
					pickupMillis = null;
				}

			} else {
				pickupMillisBodyId  = getString(R.string.new_booking_pickup_date_already_passed);
				pickupMillisInvalid = true;
			}

		}


		if( pickupMillisInvalid == false ) {

			if( mPickupAddress != null ) {

				mCommonHostActivity.lockUI();

				// prepare booking
				try {
					AccountData user = TDApplication.getSessionManager().getAccountData();

					JSONObject json = new JSONObject();

					JSONObject passenger = new JSONObject();
						passenger.put("name", user.getFullName());
						passenger.put("phone", user.getPhone());
						passenger.put("email", user.getEmail());
						json.put("passenger", passenger);

					// pickup_time

					// pickup location
					json.put( "pickup_location", pickup.toJSON() );
					WebnetLog.d("pickup: " + pickup.toJSON());

					WebnetLog.d("pickuptime: " + pickupMillis);
					if( pickupMillis != null ) {
						Time t = new Time();
						t.set( pickupMillis );

						String timeStr = t.format3339(false).replace(".000+", "+");		// FIXME API BUG
						json.put("pickup_time", timeStr);
					}

					// dropoff
					if( mDropoffAddress != null ) {
						json.put( "dropoff_location", dropoff.toJSON() );
						WebnetLog.d("dropoff: " + dropoff.toJSON());
					}


					json.put("passengers", 1);
					json.put("status", "incoming");

					WebnetTools.executeAsyncTask( new NewBookingAsyncTask(), json);

					result = true;

				} catch ( Exception e ) {
					e.printStackTrace();
				}

			} else {
				showDialog( GenericDialogFragment.DIALOG_TYPE_ERROR,
						R.string.dialog_error_title, R.string.new_booking_no_pickup_location_body );
			}

		} else {
			showDialog( GenericDialogFragment.DIALOG_TYPE_ERROR, getString(R.string.dialog_error_title), pickupMillisBodyId );
		}

		if( result == false ) {
			mCommonHostActivity.unlockUI();
		}

		return result;
	}



	protected class NewBookingAsyncTask extends AsyncTask<JSONObject, Void, ApiResponse> {

		@Override
		protected void onPreExecute() {
			showBusy(BUSY_GETTING_ROUTE_AND_PRICE);
		}

		@Override
		protected ApiResponse doInBackground( JSONObject ... params ) {
			JSONObject newBooking = params[0];

			ApiResponse response = new ApiResponse();

			ApiHelper api = ApiHelper.getInstance( TDApplication.getAppContext() );
			try {
				response = api.bookingsNewBooking(newBooking);

			} catch( Exception e) {
				e.printStackTrace();
			}

			return response;
		}

		@Override
		protected void onPostExecute(ApiResponse result) {

			if( result.getErrorCode() == Const.ErrorCode.OK ) {

				WebnetLog.e("OK OK. Booked: " + result.getJSONObject());

				BookingData placedBooking = new BookingData( JsonTools.getJSONObject( result.getJSONObject(), "booking") );

				// update booking list
				mBookingListHostActivity.addBooking( placedBooking );

				// place booking
				String msg = String.format( getString(R.string.new_booking_body_fmt), placedBooking.getPickupLocation().getAddress());
				showDialog( GenericDialogFragment.DIALOG_TYPE_OK, getString(R.string.new_booking_title), msg );

				// reset UI (addresses)
				mRoutePointList = null;

				setPickupAddress(null);
				setDropoffAddress(null);

				updateAddresses();
				setUIControlsVisibility(false);

				refreshMapOverlays();
			} else {
				showDialog( GenericDialogFragment.DIALOG_TYPE_ERROR, getString(R.string.dialog_error_title),
						String.format(getString(R.string.new_booking_failed_body_fmt), result.getErrorMessage())
						);
			}

			hideBusy(BUSY_GETTING_ROUTE_AND_PRICE);
			mCommonHostActivity.unlockUI();
		}
	}


	protected Location getMyLocation() {

		Location result = null;

		MapFragment mapFragment = (MapFragment)mFragmentManager.findFragmentById(R.id.map_fragment);
		if( mapFragment != null ) {
			GoogleMap map = mapFragment.getMap();
			if( map != null ) {
				result = map.getMyLocation();
			}
		}

		return result;
	}

	protected void showCurrentLocation() {
		showCurrentLocation(false);
	}
	protected void showCurrentLocation( Boolean disableAnimation ) {
		moveMapToLocation(null, false, false);
	}
	protected void moveMapToLocation( ApiSearchLocationData addr ) {
		if( addr != null ) {
			moveMapToLocation(new LatLng(addr.getLatitude(), addr.getLongitude()), true, false );
		}
	}
	public void moveMapToLocation( LocationData location ) {
		moveMapToLocation(location, true);
	}
	public void moveMapToLocation( LocationData location, Boolean disableAnimation ) {
		if( location != null ) {
			moveMapToLocation( new LatLng( location.getLatitude(), location.getLongitude()), disableAnimation, false );
		}
	}
	protected void moveMapToLocation( LatLng location, Boolean disableAnimation, Boolean resetCamera ) {

		MapFragment mapFragment = (MapFragment)mFragmentManager.findFragmentById(R.id.map_fragment);
		if( mapFragment != null ) {
			GoogleMap map = mapFragment.getMap();
			if( map != null ) {

				if( location == null ) {
					Location tmp = map.getMyLocation();
					if( tmp != null ) {
						location = new LatLng( tmp.getLatitude(), tmp.getLongitude() );
					}
				}

				if( location == null ) {
					Location lastKnownLoc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					if( lastKnownLoc == null ) {
						lastKnownLoc = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					}

					if (lastKnownLoc != null ) {
						location = new LatLng( lastKnownLoc.getLatitude(), lastKnownLoc.getLongitude() );
					}
				}

				if( location != null ) {
					CameraUpdate cameraUpdate;
					if( resetCamera ) {
						CameraPosition cameraPosition = new CameraPosition.Builder()
							.target( location )
							.zoom(15f)
							.bearing(0f)
							.tilt(0)
							.build();
						cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
					} else {
						cameraUpdate = CameraUpdateFactory.newLatLng(location);
					}

					if( disableAnimation ) {
						map.moveCamera(cameraUpdate);
					} else {
						map.animateCamera(cameraUpdate);
					}
				} else {
					NoLocationDialogFragment frag = NoLocationDialogFragment.newInstance( GenericDialogFragment.DIALOG_TYPE_ERROR, getString(R.string.unable_to_get_current_location_body), getString( R.string.unable_to_get_current_location_button_show_settings) );
					frag.setTargetFragment(mMe, 0);
					frag.show(((FragmentActivity)mParentActivity).getSupportFragmentManager(), "nolocationdialog");
				}
			}
		}
	}

	/*****************************************************************************************************************/

	protected View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener()
	{
		@Override
		public boolean onLongClick( View v ) {

			Boolean result = false;

			switch( v.getId() ) {

				case R.id.button_mylocation: {
					moveMapToLocation(null, false, true);
				}
				break;

				case R.id.button_set_as_pickup: {
					if( getPickupAddress() != null ) {
						setPickupAddress(null);
						updateAddresses();
					}

					result = true;
				}
				break;

				case R.id.button_set_as_dropoff: {
					if( getDropoffAddress() != null ) {
						setDropoffAddress(null);
						updateAddresses();
					}

					result = true;
				}
				break;
			}

			return result;
		}
	};


	protected View.OnClickListener mOnClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick( View v ) {

			int id = v.getId();

			switch( id ) {

				case R.id.button_set_as_pickup: {
					LocationData tmp = getAddressMapPointsTo();
					if( tmp != null ) {
						hideAimPoint();
						setPickupAddress( tmp );
						updateAddresses();
					} else {
						showDialog(GenericDialogFragment.DIALOG_TYPE_ERROR, R.string.dialog_error_title, R.string.map_aim_location_unknown_body);
//						TDApplication.playSound(Const.Sound.BUZZ);
					}
				}
				break;

				case R.id.button_set_as_dropoff: {
					LocationData tmp = getAddressMapPointsTo();
					if( tmp != null ) {
						hideAimPoint();
						setDropoffAddress( tmp );
						updateAddresses();
					} else {
						showDialog(GenericDialogFragment.DIALOG_TYPE_ERROR, R.string.dialog_error_title, R.string.map_aim_location_unknown_body);
//						TDApplication.playSound(Const.Sound.BUZZ);
					}
				}
				break;

				case R.id.left_menu_drag_handle: {
					mSlideMenuHostInterface.showLeftMenu();
				}
				break;

				case R.id.right_menu_drag_handle: {
					mSlideMenuHostInterface.showRightMenu();
				}
				break;

				case R.id.button_mylocation: {
					showCurrentLocation(false);
				}
				break;

				case R.id.button_book: {
					Boolean proceed = false;
					Integer errorMsgId = null;

					if( mContext.getResources().getBoolean(R.bool.caboffice_settings_dropoff_location_is_mandatory) ) {
						if( (getPickupAddress() != null) && (getDropoffAddress() != null) ) {
							proceed = true;
						} else {
							errorMsgId = R.string.new_booking_both_locations_required_to_place_booking;
						}
					} else {
						if( getPickupAddress() != null ) {
							proceed = true;
						} else {
							errorMsgId = R.string.new_booking_no_pickup_location_body;
						}
					}

					if( proceed ) {
						BookingConfirmationDialogFragment frag = BookingConfirmationDialogFragment.newInstance(getPickupAddress(), getDropoffAddress());
						frag.setTargetFragment(ControlCenterFragment.this, 0);
						frag.show(((FragmentActivity)mParentActivity).getSupportFragmentManager(), "newbookingconfirmation");
					} else {
						showDialog(GenericDialogFragment.DIALOG_TYPE_ERROR, R.string.dialog_error_title, errorMsgId);
					}
				}
				break;

				case R.id.button_start_new_booking: {

					if( getAddressMapPointsTo() != null ) {
						setPickupAddress( getAddressMapPointsTo() );
						updateAddresses();
					}

					setUIControlsVisibility(true);
				}
				break;

				case R.id.pickup_location: {
					doAddressSearch( SearchActivity.TYPE_PICKUP, getPickupAddress() );
				}
				break;
				case R.id.dropoff_location: {
					doAddressSearch( SearchActivity.TYPE_DROPOFF, getDropoffAddress() );
				}

			}
		}
	};


	/**[ BookingConfirmationDialogClickListener ]**********************************************************************/

	@Override
	public void bookingConfirmed( LocationData pickup, LocationData dropoff, Long pickupMillis ) {
		doPlaceBooking( pickup, dropoff, pickupMillis );
	}



	/*****************************************************************************************************************/

	protected Boolean mAimPointVisible = false;

	protected void hideAimPoint() {
		if( mAimPointVisible ) {
			mAimPointVisible = false;

			View v = mFragmentView.findViewById(R.id.map_aim_point_container);
			v.clearAnimation();
			v.startAnimation( AnimationUtils.loadAnimation(mContext, R.anim.fade_out));

			WebnetTools.setVisibility(mFragmentView, R.id.map_aim_point_minimal_container, View.VISIBLE);

			int[] ids = { R.id.button_set_as_pickup, R.id.button_set_as_dropoff };
			for( int id : ids ) {
				v = mFragmentView.findViewById( id );
				v.setClickable(false);
				v.setLongClickable(false);
			}
		}
	}

	protected void showAimPoint() {
		if(  isUIControlsVisibile() ) {
			showAimPoint(false);
		}
	}
	protected void showAimPoint(Boolean animate) {

		if( mAimPointVisible == false ) {
			mAimPointVisible = true;

			View v = mFragmentView.findViewById(R.id.map_aim_point_container);
			v.clearAnimation();

			v.startAnimation( AnimationUtils.loadAnimation(mContext,
												( animate ) ? R.anim.fade_in : R.anim.fade_in_instant
											));

			WebnetTools.setVisibility(mFragmentView, R.id.map_aim_point_minimal_container, View.INVISIBLE);

			int[] ids = { R.id.button_set_as_pickup, R.id.button_set_as_dropoff };
			for( int id : ids ) {
				v = mFragmentView.findViewById( id );
				v.setClickable(true);
				v.setLongClickable(true);
			}
		}
	}


	/*****************************************************************************************************************/

	protected void setAddressMapPointsTo( LocationData addr ) {
		mAddressMapPointsTo = addr;
	}
	protected LocationData getAddressMapPointsTo() {
		return mAddressMapPointsTo;
	}

	public void setPickupAddress( LocationData addr ) {
		if( addr != null ) {
			mIitialMapLocationSet = true;
		}
		mPickupAddress = addr;
	}
	public LocationData getPickupAddress() {
		return mPickupAddress;
	}
	public void setDropoffAddress( LocationData addr ) {
		if( addr != null ) {
			mIitialMapLocationSet = true;
		}
		mDropoffAddress = addr;
	}
	public LocationData getDropoffAddress() {
		return mDropoffAddress;
	}

	public void updateAddresses() {

		LocationData pickup = getPickupAddress();
		LocationData dropoff = getDropoffAddress();

		if( pickup != null ) {
			WebnetTools.setText(mFragmentView, R.id.pickup_location, pickup.getAddress() );
		} else {
			WebnetTools.setText(mFragmentView, R.id.pickup_location, R.string.pickup_line_default);
		}

		if( dropoff != null ) {
			WebnetTools.setText(mFragmentView, R.id.dropoff_location, dropoff.getAddress());
		} else {

			int labelId = R.string.dropoff_line_default;
			if( mContext.getResources().getBoolean(R.bool.caboffice_settings_use_alternative_dropoff_label) ) {
				labelId = R.string.dropoff_line_alternative;
			}

			WebnetTools.setText(mFragmentView, R.id.dropoff_location, labelId );
		}

		// calc route if we can
		getRouteAndBookingPrice(pickup, dropoff);

		if( (pickup != null) || (dropoff != null) ) {
			if( isUIControlsVisibile() == false ) {
				setUIControlsVisibility(true);
			}
		}
	}


	/**[ booking tracking ]*********************************************************************************/

	protected HashMap<String, LatLng> mTrackableBookings = new HashMap<String, LatLng>();

	protected AtomicBoolean mBookingTrackingEnabled = new AtomicBoolean();
	protected void startBookingTracking() {

		if( mContext.getResources().getBoolean(R.bool.caboffice_settings_track_bookings ) ) {
			if( mBookingTrackingEnabled.compareAndSet(false, true) ) {
				mHandler.post(mUpdateBookingTrackingRunnable);
			}
		}
	}
	protected void stopBookingTracking() {
		mHandler.removeCallbacks(mUpdateBookingTrackingRunnable);
		mBookingTrackingEnabled.compareAndSet(true, false);
	}

	protected Runnable mUpdateBookingTrackingRunnable = new Runnable()
	{
		@Override
		public void run() {
			if( mBookingTrackingEnabled.get() ) {
				updateBookingTracking();

				mHandler.postDelayed(this,  WebnetTools.MILLIS_PER_WEEK * 15);
			} else {
				WebnetLog.d("Booking tracking disabled");
			}
		}
	};

	protected void updateBookingTracking() {
		if( mTrackableBookings.size() > 0 ) {
			WebnetTools.executeAsyncTask( new UpdateBookingTrackingAsyncTask(mTrackableBookings) );
		}
	}

	protected class UpdateBookingTrackingAsyncTask extends AsyncTask<Void, Void, ApiResponse> {

		protected HashMap<String, LatLng> mBookingsToTrack = null;
		protected HashMap<String, LatLng> mTrackedBookings = null;

		public UpdateBookingTrackingAsyncTask( HashMap<String, LatLng> bookingsToTrack ) {
			mBookingsToTrack = bookingsToTrack;
		}

		@Override
		protected ApiResponse doInBackground( Void ... params ) {

			mTrackedBookings = new HashMap<String, LatLng>();

			ApiResponse response = new ApiResponse();

			try {
				ApiHelper api = ApiHelper.getInstance( TDApplication.getAppContext() );

				Iterator<String> iter = (Iterator<String>)mBookingsToTrack.keySet();
				while( iter.hasNext() ) {
					String bookingPk = iter.next();

					ApiResponse r = api.bookingsTrackBooking(bookingPk);
					if( r.getErrorCode() == Const.ErrorCode.OK ) {

					}
				}

			} catch ( Exception e ) {
				e.printStackTrace();
			}

			return response;
		}

	}

	/**[ nearby taxis ]*************************************************************************************/

	protected AtomicBoolean mCabTrackingEnabled = new AtomicBoolean();
	protected void startCabTracking() {

		if( mContext.getResources().getBoolean(R.bool.caboffice_settings_track_nearby_cabs) ) {
			if( mCabTrackingEnabled.compareAndSet(false, true) ) {
				mHandler.post(mUpdateNearbyCabsRunnable);
			}
		}
	}
	protected void stopCabTracking() {
		mHandler.removeCallbacks(mUpdateNearbyCabsRunnable);
		mCabTrackingEnabled.compareAndSet(true, false);
	}

	protected ArrayList<LatLng> mNearbyTaxis = null;

	protected Runnable mUpdateNearbyCabsRunnable = new Runnable()
	{
		@Override
		public void run() {

			if( mCabTrackingEnabled.get() ) {
				updateNarbyCabs();

		    	mHandler.postDelayed(this, WebnetTools.MILLIS_PER_SECOND * 30);
			} else {
				WebnetLog.d("Cab Update disabled");
			}
		}
	};

	protected void updateNarbyCabs() {
    	Location pos = getMyLocation();
    	if( pos != null ) {
    		UpdateNearbyCabsAsyncTask task = new UpdateNearbyCabsAsyncTask( pos );
    		WebnetTools.executeAsyncTask( task );
    	}
	}

	protected class UpdateNearbyCabsAsyncTask extends AsyncTask<Void, Void, ApiResponse> {

    	protected ArrayList<LatLng> mTaxis = null;
    	protected LatLng mPos;

    	public UpdateNearbyCabsAsyncTask( Location position ) {
    		mPos = new LatLng( position.getLatitude(), position.getLongitude() );
    	}
    	public UpdateNearbyCabsAsyncTask( LatLng position ) {
    		mPos = position;
    	}

		@Override
		protected ApiResponse doInBackground( Void ... params ) {

			ApiResponse response = new ApiResponse();

			try {
				ApiHelper api = ApiHelper.getInstance(TDApplication.getAppContext());
				response = api.getNearbyDrivers( mPos );

				if( response.getErrorCode() == Const.ErrorCode.OK ) {

					mTaxis = new ArrayList<LatLng>();

					JSONArray tmp = response.getJSONObject().getJSONArray("drivers");

					for( int i=0; i<tmp.length(); i++ ) {
						JSONObject item = (JSONObject)tmp.get(i);
						mTaxis.add( new LatLng(item.getDouble("lat"), item.getDouble("lng")) );
					}
				}

			} catch ( Exception e ) {
				e.printStackTrace();
				response.setErrorCode(Const.ErrorCode.EXCEPTION_ERROR);
				response.setException(e);
			}


			return response;
		}

		@Override
		protected void onPostExecute(ApiResponse response) {
			if( response.getErrorCode() == Const.ErrorCode.OK ) {
				mNearbyTaxis = mTaxis;
				refreshMapOverlays();
			}
		}
    }


	/**[ Map overlays ]*************************************************************************************/

	protected void refreshMapOverlays() {

		WebnetLog.d("Refreshing overlays");

		MapFragment mapFragment = (MapFragment)mFragmentManager.findFragmentById(R.id.map_fragment);
		if( mapFragment != null ) {
			GoogleMap map = mapFragment.getMap();
			if( map != null ) {
				map.clear();

				// nearby taxis
				if( mNearbyTaxis != null ) {
					for( int i=0; i<mNearbyTaxis.size(); i++ ) {
						map.addMarker(new MarkerOptions()
								.position( mNearbyTaxis.get(i) )
								.icon( BitmapDescriptorFactory.fromResource(R.drawable.map_marker_nearby_cab))
						);
					}
				}

				// pickup-dropoff route
				if(    (getPickupAddress() != null) && (getDropoffAddress() != null)
					&& (mRoutePointList != null) && (mRoutePointList.size() >= 2) )
				{
					LatLng cabPickupLocation = mRoutePointList.get(0);
					LatLng cabDropoffLocation = mRoutePointList.get( mRoutePointList.size()-1 );

					MarkerOptions mPickup = new MarkerOptions()
							.position(cabPickupLocation)
							.anchor(0.5f,0.5f)
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_cab_pickup));
					map.addMarker( mPickup );

					MarkerOptions mDestination = new MarkerOptions()
							.position(cabDropoffLocation)
							.anchor(0.5f,0.5f)
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_cab_dropoff));
					map.addMarker( mDestination );

					PolylineOptions rectLine = new PolylineOptions().width(5).color( getResources().getColor(R.color.map_route_fg));
					for (int i = 0; i < mRoutePointList.size(); i++) {
						rectLine.add(mRoutePointList.get(i));
					}
					map.addPolyline(rectLine);

					createDashedLine(map, getPickupAddress().getLatLng(), mRoutePointList.get(0), getResources().getColor(R.color.pickup_location) );
					createDashedLine(map, getDropoffAddress().getLatLng(), mRoutePointList.get( mRoutePointList.size()-1 ), getResources().getColor(R.color.dropoff_location) );
				}


				if( mPickupAddress != null ) {
					MarkerOptions mPickup = new MarkerOptions()
						.position( mPickupAddress.getLatLng() )
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_pickup_big));
						map.addMarker( mPickup );
				}

				if( mDropoffAddress != null ) {
						MarkerOptions mDestination = new MarkerOptions()
						.position( mDropoffAddress.getLatLng() )
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_dropoff));
						map.addMarker( mDestination );
				}


				// booking fee
				if( (mPickupAddress == null) || (mDropoffAddress == null)) {
					WebnetTools.setVisibility( mFragmentView, R.id.price_box_container, View.INVISIBLE );
				} else {
					if( mBookingFeeCalculated.get() == true ) {
						WebnetTools.setText( mFragmentView, R.id.price, mBookingFeePriceFormatted );
						WebnetTools.setText( mFragmentView, R.id.distance, mBookingFeeDistanceFormatted );
						WebnetTools.setVisibility( mFragmentView, R.id.price_box_container, View.VISIBLE );
					} else {
						WebnetTools.setVisibility( mFragmentView, R.id.price_box_container, View.INVISIBLE );
					}
				}
			}
		}
	}

	public void createDashedLine(GoogleMap map, LatLng begin, LatLng end, int color) {
		double diffLat = (end.latitude - begin.latitude);
		double diffLng = (end.longitude - begin.longitude);

		double zoom = (map.getCameraPosition().zoom) * 2;

		double divLat = diffLat / zoom;
		double divLng = diffLng / zoom;

		LatLng tmpLat = begin;

		for(int i = 0; i < zoom; i++) {
			LatLng loopLatLng = tmpLat;

			if( i == (zoom - 1) ) {
				loopLatLng = end;
			} else {
				if(i > 0) {
					loopLatLng = new LatLng(tmpLat.latitude + (divLat * 0.25f), tmpLat.longitude + (divLng * 0.25f));
				}
			}

			map.addPolyline(new PolylineOptions()
				.add(loopLatLng)
				.add(new LatLng(tmpLat.latitude + divLat, tmpLat.longitude + divLng))
				.color(color)
				.width(5f));

			tmpLat = new LatLng(tmpLat.latitude + divLat, tmpLat.longitude + divLng);
	    }
	}

	/*******************************************/

	protected Boolean mUIControlsVisibile = false;
	protected void setUIControlsVisibility( Boolean visible ) {

		mUIControlsVisibile = visible;

		if( visible ) {
			disableBookingControls(false);
		} else {
			disableBookingControls(true);
		}
	}
	protected Boolean isUIControlsVisibile() {
		return mUIControlsVisibile;
	}

	protected void disableBookingControls( Boolean disabled ) {

		WebnetTools.setVisibility(mFragmentView, R.id.booking_addresses_container, (disabled) ? View.INVISIBLE : View.VISIBLE);
		WebnetTools.setVisibility(mFragmentView, R.id.bottom_container, (disabled) ? View.INVISIBLE : View.VISIBLE);

		WebnetTools.setVisibility(mFragmentView, R.id.map_aim_point_container, (disabled) ? View.INVISIBLE : View.VISIBLE);

		WebnetTools.setVisibility(mFragmentView, R.id.bottom_start_new_booking_container, (disabled) ? View.VISIBLE : View.INVISIBLE);

		if( disabled ) {
			WebnetTools.setVisibility( mFragmentView, R.id.price_box_container, View.INVISIBLE );
			hideAimPoint();
		} else {
			showAimPoint(true);
		}
	}

	/**[ address search wrapper ]***************************/

	protected void doAddressSearch( int type, LocationData address ) {
		Intent intent = new Intent();
		intent.putExtra(Const.Bundle.TYPE, type);
		intent.putExtra(Const.Bundle.LOCATION, address);
		intent.putExtra(Const.Bundle.REQUEST_CODE, Const.RequestCode.ADDRESS_SEARCH );
		intent.setComponent( new ComponentName( mContext.getPackageName(), SearchActivity.class.getName() ) );
		startActivityForResult(intent, Const.RequestCode.ADDRESS_SEARCH);
	}

	@Override
	public void onActivityResult( int requestCode, int resultCode, Intent intent ) {

		switch( requestCode ) {

			case Const.RequestCode.ADDRESS_SEARCH: {
				if( resultCode == Activity.RESULT_OK ) {

					int type = intent.getExtras().getInt(Const.Bundle.TYPE);
					LocationData location = intent.getExtras().getParcelable( Const.Bundle.LOCATION );

					switch( type ) {
						case SearchActivity.TYPE_PICKUP:
							setPickupAddress( location );
							break;
						case SearchActivity.TYPE_DROPOFF:
							setDropoffAddress( location );
							break;
					}

					updateAddresses();
					moveMapToLocation(location);
				}
			}
			break;

			default: {
				super.onActivityResult(requestCode, resultCode, intent);
			}
			break;
		}
	}

} // end of class

