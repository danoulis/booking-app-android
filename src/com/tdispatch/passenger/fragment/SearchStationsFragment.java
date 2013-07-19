package com.tdispatch.passenger.fragment;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.SearchActivity;
import com.tdispatch.passenger.common.Const;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.core.TDFragment;
import com.tdispatch.passenger.host.AddressSearchHostInterface;
import com.tdispatch.passenger.host.AddressSearchModuleInterface;
import com.tdispatch.passenger.model.LocationData;
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
public class SearchStationsFragment extends TDFragment implements AddressSearchModuleInterface
{
	protected final ArrayList<Station> mItems = new ArrayList<Station>();
	protected ListAdapter mAdapter;
	protected int mType = SearchActivity.TYPE_UNKNOWN;

	protected AddressSearchHostInterface mAddressSearchHost;

	@Override
	protected int getLayoutId() {
		return R.layout.search_stations_fragment;
	}


	protected AddressSearchHostInterface mHostActivity;
	@Override
	public void onAttach( Activity activity ) {
		super.onAttach( activity );
		mHostActivity = (AddressSearchHostInterface)activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if( args != null ) {
			mType = args.getInt(Const.Bundle.TYPE);
		} else {
			throw new IllegalArgumentException("Arguments not passed");
		}

		mItems.add( new Station(Station.TYPE_TRAIN, new LocationData("Bury St Edmonds Station", "IP32 6AQ", 52.253708, 0.712454) ) );
		mItems.add( new Station(Station.TYPE_TRAIN, new LocationData("Chelmsford Station", "CM1 1HT", 51.736465, 0.468708) ) );
		mItems.add( new Station(Station.TYPE_TRAIN, new LocationData("Colchester Station", "CO4 5EY", 51.901230, 0.893736) ) );
		mItems.add( new Station(Station.TYPE_TRAIN, new LocationData("Ely Station", "CB7 4DJ", 52.391209, 0.265048) ) );
		mItems.add( new Station(Station.TYPE_TRAIN, new LocationData("Ipswich Station", "IP2 8AL", 52.050720, 1.144216) ) );
		mItems.add( new Station(Station.TYPE_TRAIN, new LocationData("Norwich Station", "NR1 1EH", 52.627151, 1.306835) ) );
		mItems.add( new Station(Station.TYPE_TRAIN, new LocationData("Ingatestone Station", "CM4 0BW", 51.666916, 0.383777) ) );
	}

	@Override
	protected void onPostCreateView() {
		ListView lv = (ListView)mFragmentView.findViewById( R.id.list );
		mAdapter = new ListAdapter( mParentActivity, 0, mItems );
		lv.setAdapter( mAdapter );
	}


	@Override
	protected Boolean isBusyOverlayPresent() {
		return false;
	}



	/**[ adapter ]***********************************************************************************************************/


	protected class ListAdapter extends ArrayAdapter<Station>
	{
		protected Context mContext;

		public ListAdapter(Activity activity, int textViewResourceId, ArrayList<Station> objects) {
			super(activity, textViewResourceId, objects);
			mContext = activity;
		}

		@Override
		public int getItemViewType(int position) {
			return 0;
	    }

		@Override
		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View view = convertView;
			if( view == null) {
				int layoutId = R.layout.search_stations_row;

				LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = li.inflate(layoutId, null);

				WebnetTools.setCustomFonts( TDApplication.getAppContext(), (ViewGroup)view );
			}

			ImageView iv = (ImageView)view.findViewById(R.id.item_icon);
			iv.setImageResource( mItems.get(position).getIconId());

			WebnetTools.setText( view, R.id.item_address, mItems.get(position).getLocation().getAddress() );
			view.setTag( R.id.tag_key_position, position );
			view.setOnClickListener( mOnClickListener );

			return( view );
		}

		protected View.OnClickListener mOnClickListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int position = (Integer) v.getTag(R.id.tag_key_position);
				mHostActivity.doSearchOk( mType, mItems.get(position).getLocation() );
			}
		};
	}


	protected class Station {

		protected static final int TYPE_TRAIN	= 0;
		protected static final int TYPE_AIRPORT	= 1;
		protected static final int TYPE_HARBOUR	= 2;
		protected static final int TYPE_BUS		= 3;

		protected int mIconId;
		protected LocationData mLocation;

		public Station( int type, LocationData location ) {
			mIconId = type;
			mLocation = location;
		}

		public int getIconId() {

			int iconId = R.drawable.station_type_default;

			switch( mIconId ) {
				case TYPE_TRAIN:
					iconId = R.drawable.station_type_train;
					break;

				case TYPE_AIRPORT:
					iconId = R.drawable.station_type_airport;
					break;

				case TYPE_HARBOUR:
					iconId = R.drawable.station_type_ship;
					break;

				case TYPE_BUS:
					iconId = R.drawable.station_type_bus;
					break;

			}

			return iconId;
		}
		public LocationData getLocation() {
			return mLocation;
		}
	}



	// AddressSearchModuleInterface
	@Override
	public void doEnterPage() {
		// dummy
	}

	@Override
	public void doLeavePage() {
		// dummy
	}


	// end of class
}
