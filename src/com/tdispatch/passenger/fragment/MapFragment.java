package com.tdispatch.passenger.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
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
public class MapFragment extends SupportMapFragment
{
	protected GoogleMap mMap;

	public MapFragment() {
		super();
	}

	public static MapFragment newInstance() {
		return new MapFragment();
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
		View v = super.onCreateView(inflater, container, savedInstanceState);

		fixGLSurfaceBug((ViewGroup)v);

		initMap();

		return v;
	}

	protected Boolean initMap() {

		Boolean result = false;

		mMap = super.getMap();

		if( mMap != null ) {

			mMap.setTrafficEnabled( false );
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			mMap.setIndoorEnabled( false );
			mMap.setMyLocationEnabled(true);

			UiSettings settings = mMap.getUiSettings();
				settings.setCompassEnabled( true );
				settings.setMyLocationButtonEnabled( false );
				settings.setRotateGesturesEnabled( true );
				settings.setScrollGesturesEnabled( true );
				settings.setTiltGesturesEnabled( true );
				settings.setZoomControlsEnabled( false );
				settings.setZoomGesturesEnabled( true );

			result = true;
		} else {
			WebnetLog.e("map is null!");
		}


		return result;
	}



	// Google Maps v2 is buggy. This is hack to make it not wrecking
	// the rest of the UI by overlaying other elements by its GLView
	//
	// http://code.google.com/p/gmaps-api-issues/issues/detail?id=4659
	// http://code.google.com/p/gmaps-api-issues/issues/detail?id=4639

	protected void fixGLSurfaceBug( ViewGroup group ) {
		int childCount = group.getChildCount();

		for( int i = 0; i < childCount; i++ ) {
			View child = group.getChildAt(i);

			if( child instanceof ViewGroup ) {
				fixGLSurfaceBug((ViewGroup)child);
			} else {
				if( child instanceof SurfaceView ) {
					child.setBackgroundColor(0x00000000);
				}
			}
		}
	}

}
