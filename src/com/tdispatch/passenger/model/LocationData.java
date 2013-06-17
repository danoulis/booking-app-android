package com.tdispatch.passenger.model;

import org.json.JSONObject;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
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
public class LocationData implements Parcelable
{
	protected String mAddress;
	protected String mPostCode;

	protected double mLatitude;
	protected double mLongitude;


	/**[ constructors ]***********************************************************************************************/

	public LocationData() {
		// dummy
	}
	public LocationData( String address) {
		setAddress( address );
	}
	public LocationData( JSONObject json ) {
		set( json );
	}
	public LocationData( ApiSearchLocationData data ) {
		setAddress( data.getFullAddress(true) );
		setPostCode( data.getPostCode() );
		setLatitude( data.getLatitude() );
		setLongitude( data.getLongitude() );
	}
	public LocationData( Location loc ) {
		setLocation( loc );
	}
	public LocationData( LatLng loc ) {
		setLocation( loc );
	}

	/**[ setters / getters ]******************************************************************************************/

	public LocationData setAddress(String address) {
		mAddress = address;

		return this;
	}
	public String getAddress() {
		return mAddress;
	}


	public LocationData setPostCode(String postCode) {
		mPostCode = postCode;

		return this;
	}
	public String getPostCode() {
		return mPostCode;
	}

	public LocationData setLatitude(double latitude) {
		mLatitude = latitude;

		return this;
	}
	public double getLatitude() {
		return mLatitude;
	}

	public LocationData setLongitude(double longitude) {
		mLongitude = longitude;

		return this;
	}
	public double getLongitude() {
		return mLongitude;
	}
	public LocationData setLocation( Location loc ) {
		setLatitude( loc.getLatitude() );
		setLongitude( loc.getLongitude() );

		return this;
	}
	public LocationData setLocation( LatLng loc ) {
		setLatitude( loc.latitude );
		setLongitude( loc.longitude );

		return this;
	}

	/**[ helpers ]****************************************************************************************************/

	public LocationData set( JSONObject json ) {
		if( json != null ) {
			setAddress( JsonTools.getString(json,  "address"));
			setPostCode( JsonTools.getString(json, "postcode"));

			JSONObject locationObj = JsonTools.getJSONObject(json, "location");
				setLatitude( JsonTools.getDouble(locationObj, "lat"));
				setLongitude( JsonTools.getDouble(locationObj, "lng"));
		}

		return this;
	}

	public JSONObject toJSON() {

		JSONObject json = new JSONObject();

		try {
			json.put("address", getAddress() );
			json.put("postcode", getPostCode());

			JSONObject location = new JSONObject();
			location.put("lat", getLatitude());
			location.put("lng", getLongitude());

			json.put("location", location);

		} catch ( Exception e ) {
			e.printStackTrace();
		}

		return json;
	}

	public LatLng getLatLng() {
		return new LatLng( getLatitude(), getLongitude() );
	}


	/**[ parcelable ]*************************************************************************************************/

    @Override
	public int describeContents() {
        return 0;
    }

    @Override
	public void writeToParcel(Parcel out, int flags) {
    	out.writeString( getAddress() );
    	out.writeString( getPostCode() );
    	out.writeDouble( getLatitude() );
    	out.writeDouble( getLongitude() );
    }


    private LocationData(Parcel in) {
    	setAddress(in.readString());
    	setPostCode(in.readString());
    	setLatitude(in.readDouble());
    	setLongitude(in.readDouble());
    }


    public static final Parcelable.Creator<LocationData> CREATOR = new Parcelable.Creator<LocationData>() {
        @Override
		public LocationData createFromParcel(Parcel in) {
            return new LocationData(in);
        }

        @Override
		public LocationData[] newArray(int size) {
            return new LocationData[size];
        }
    };



	/**[ end of class ]***********************************************************************************************/
}
