package com.webnetmobile.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

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
public class GoogleMapRouteHelper
{
	protected LatLng mPickup, mDropoff;
	protected List<LatLng> mRoutePoints = null;

	public GoogleMapRouteHelper(LatLng pickup, LatLng dropoff) {
		mPickup = pickup;
		mDropoff = dropoff;
	}

	public List<LatLng> getDirections() {
		if( (mPickup != null) || (mDropoff != null) ) {

			HttpClient httpclient = new DefaultHttpClient();

			StringBuilder urlBuilder = new StringBuilder();

			urlBuilder.append("http://maps.googleapis.com/maps/api/directions/json");

			urlBuilder.append("?origin=");
			urlBuilder.append(Double.toString((double)mPickup.latitude));
			urlBuilder.append(",");
			urlBuilder.append(Double.toString((double)mPickup.longitude));

			urlBuilder.append("&destination=");
			urlBuilder.append(Double.toString((double)mDropoff.latitude));
			urlBuilder.append(",");
			urlBuilder.append(Double.toString((double)mDropoff.longitude));
			urlBuilder.append("&sensor=false&units=metric&mode=driving");

			HttpPost httppost = new HttpPost(urlBuilder.toString());
			HttpResponse response;
			try {
				response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				InputStream is = null;

				is = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
				StringBuilder sb = new StringBuilder();
				sb.append(reader.readLine() + "\n");
				String line = "0";
				while( (line = reader.readLine()) != null ) {
					sb.append(line + "\n");
				}
				is.close();
				reader.close();

				String result = sb.toString();

				JSONObject jsonObject = new JSONObject(result);

				JSONArray routeArray = jsonObject.getJSONArray("routes");
				if( (routeArray != null) && (routeArray.length() > 0) ) {
					JSONObject routes = routeArray.getJSONObject(0);
					JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
					String encodedString = overviewPolylines.getString("points");
					mRoutePoints = decodePoly(encodedString);
				} else {
					mRoutePoints = null;
				}

			} catch( ClientProtocolException e ) {
				e.printStackTrace();
			} catch( IOException e ) {
				e.printStackTrace();
			} catch( Exception e ) {
				e.printStackTrace();
			}

			return mRoutePoints;
		} else {
			throw new NullPointerException("Pickup or Dropoff cannot be null.");
		}
	}

	private List<LatLng> decodePoly( String poly ) {

		List<LatLng> points = new LinkedList<LatLng>();

		int len = poly.length();
		int index = 0;
		int lat = 0;
		int lng = 0;

		while( index < len ) {
			int b;
			int shift = 0;
			int result = 0;

			do {
				b = poly.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while( b >= 0x20 );

			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;

			do {
				b = poly.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while( b >= 0x20 );

			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			points.add(new LatLng((lat / 1E5), (lng / 1E5)));
		}

		return points;
	}

}
