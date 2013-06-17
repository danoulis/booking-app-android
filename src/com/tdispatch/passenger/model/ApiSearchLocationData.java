package com.tdispatch.passenger.model;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

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
public class ApiSearchLocationData implements Parcelable
{
	/**[ members ]*************************************************************************************************/

	protected String mTown;
	protected String mCounty;
	protected String mCountry;
	protected String mPostCode;
	protected String mAddress;

	protected double mLatitude;
	protected double mLongitude;


	/**[ constructors ]********************************************************************************************/

	public ApiSearchLocationData() {
		// dummy
	}
	public ApiSearchLocationData( JSONObject json ) {
		set( json );
	}
	public ApiSearchLocationData( LatLng loc ) {
		setLocation( loc );
	}


	/**[ setters/getters ]*****************************************************************************************/

	public ApiSearchLocationData setTown( String data ) {
		mTown = data;
		return this;
	}
	public String getTown() {
		return mTown;
	}

	public ApiSearchLocationData setCountry( String data ) {
		mCountry = data;
		return this;
	}
	public String getCountry() {
		return mCountry;
	}

	public ApiSearchLocationData setPostCode( String data ) {
		mPostCode = data;
		return this;
	}
	public String getPostCode() {
		return mPostCode;
	}

	public ApiSearchLocationData setAddress( String data ) {
		mAddress = data;
		return this;
	}
	public String getAddress() {
		return mAddress;
	}

	public ApiSearchLocationData setLatitude(double latitude) {
		mLatitude = latitude;
		return this;
	}
	public double getLatitude() {
		return mLatitude;
	}

	public ApiSearchLocationData setLongitude(double longitude) {
		mLongitude = longitude;
		return this;
	}
	public double getLongitude() {
		return mLongitude;
	}

	public ApiSearchLocationData setLocation( LatLng loc ) {
		setLatitude( loc.latitude );
		setLongitude( loc.longitude );
		return this;
	}

	public ApiSearchLocationData setCounty(String data) {
		mCounty = data;
		return this;
	}
	public String getCounty() {
		return mCounty;
	}

	/**[ helpers ]*************************************************************************************************/

	public ApiSearchLocationData set( LocationData loc ) {
		setTown(null);
		setCounty(null);
		setCountry(null);

		setAddress( loc.getAddress() );
		setPostCode( loc.getPostCode() );
		setLatitude( loc.getLatitude() );
		setLongitude( loc.getLatitude() );

		return this;
	}

	public ApiSearchLocationData set( JSONObject json ) {
		setTown( JsonTools.getString(json, "town", "") );
		setCountry( JsonTools.getString(json, "country", ""));
		setPostCode(JsonTools.getString(json, "postcode",""));
		setAddress(JsonTools.getString(json,"address",""));

		JSONObject locationObj = JsonTools.getJSONObject(json, "location");
			setLatitude( JsonTools.getDouble(locationObj, "lat"));
			setLongitude( JsonTools.getDouble(locationObj, "lng"));

		return this;
	}

	public LatLng getLatLng() {

		return new LatLng(mLatitude, mLongitude);
	}

	public String getFullAddress() {
		return getFullAddress( true );
	}

	public String getFullAddress( Boolean includeCountry ) {
		String result = getAddress();

		if( TextUtils.isEmpty( getPostCode()) == false ) {
			if( result.length() > 0 ) {
				result += ", ";
			}
			result += getPostCode();
		}
		if( TextUtils.isEmpty(getTown()) == false ) {
			if( result.length() > 0 ) {
				result += ", ";
			}

			result += getTown();
		}

		if( includeCountry ) {
			if( TextUtils.isEmpty(getCountry()) == false ) {
				if( result.length() > 0 ) {
					result += ", ";
				}

				result += getCountry();
			}
		}

		return result;
	}

	/**[ parcelable ]**********************************************************************************************/

    @Override
	public int describeContents() {
        return 0;
    }

    @Override
	public void writeToParcel(Parcel out, int flags) {
    	out.writeString( getTown() );
    	out.writeString( getCountry() );
    	out.writeString( getPostCode() );
    	out.writeString( getAddress() );
    	out.writeString( getCounty() );

    	out.writeDouble( getLatitude() );
    	out.writeDouble( getLongitude() );
    }

    private ApiSearchLocationData(Parcel in) {
    	setTown( in.readString() );
    	setCountry( in.readString() );
    	setPostCode( in.readString() );
    	setAddress( in.readString() );
    	setCounty( in.readString() );

    	setLatitude(in.readDouble());
    	setLongitude(in.readDouble());
    }

    public static final Parcelable.Creator<ApiSearchLocationData> CREATOR = new Parcelable.Creator<ApiSearchLocationData>() {
        @Override
		public ApiSearchLocationData createFromParcel(Parcel in) {
            return new ApiSearchLocationData(in);
        }

        @Override
		public ApiSearchLocationData[] newArray(int size) {
            return new ApiSearchLocationData[size];
        }
    };

    /**************************************************************************************************************/

}
