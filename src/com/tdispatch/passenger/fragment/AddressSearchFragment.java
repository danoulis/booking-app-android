package com.tdispatch.passenger.fragment;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.api.ApiHelper;
import com.tdispatch.passenger.api.ApiResponse;
import com.tdispatch.passenger.common.Const;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.core.TDFragment;
import com.tdispatch.passenger.host.AddressSearchHostInterface;
import com.tdispatch.passenger.model.LocationData;
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
public class AddressSearchFragment extends TDFragment
{
	protected static final int TYPE_UNKNOWN 		= 0;
	public static final 	  int TYPE_PICKUP 		= 1;
	public static final 	  int TYPE_DROPOFF 		= 2;


	protected ArrayList<LocationData> mItems = new ArrayList<LocationData>();
	protected ListAdapter mAdapter;

	protected int mType = TYPE_UNKNOWN;
	protected LocationData mAddress;

	protected AddressSearchHostInterface mAddressSearchHost;

	@Override
	protected int getLayoutId() {
		return R.layout.address_search_fragment;
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
			mAddress = args.getParcelable(Const.Bundle.LOCATION);
		} else {
			throw new IllegalArgumentException("Arguments not passed");
		}
	}

	@Override
	protected void onPostCreateView() {
		ListView lv = (ListView)mFragmentView.findViewById( R.id.list );
		mAdapter = new ListAdapter( mParentActivity, 0, mItems );
		lv.setAdapter( mAdapter );

		EditText et = (EditText)mFragmentView.findViewById(R.id.address);
		et.setOnEditorActionListener( mOnEditorActionListener );
		et.addTextChangedListener( mTextWatcher );
	}


	@Override
	protected Boolean isBusyOverlayPresent() {
		return false;
	}


	@Override
	public void onResume() {
		super.onResume();

		ImageView iv = (ImageView)mFragmentView.findViewById(R.id.icon);
		EditText et = (EditText)mFragmentView.findViewById(R.id.address);

		int imgId = R.drawable.ic_launcher;
		int hintId = R.string.address_search_generic_hint;
		switch( mType ) {
			case TYPE_PICKUP:
				imgId = R.drawable.map_marker_pickup;
				hintId = R.string.address_search_pickup_hint;
				break;
			case TYPE_DROPOFF:
				imgId = R.drawable.map_marker_dropoff;
				hintId = R.string.address_search_dropoff_hint;
				break;
		}
		iv.setImageResource( imgId );

		et.setText( ( mAddress != null ) ? mAddress.getAddress() : "");
		et.setHint( hintId );

//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//			((InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(et, InputMethodManager.SHOW_FORCED);
//		}
	}


	protected TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener()
	{
		@Override
		public boolean onEditorAction( TextView v, int actionId, KeyEvent event ) {
			return false;
		}
	};

	protected TextWatcher mTextWatcher = new TextWatcher()
	{
		@Override
		public void onTextChanged( CharSequence s, int start, int before, int count ) {
			mAdapter.clear();
			if( s.length() > 0 ) {
				WebnetTools.executeAsyncTask( new GetPlacesPredictionsAsyncTask(), s.toString());
			}
		}

		@Override
		public void beforeTextChanged( CharSequence s, int start, int count, int after ) {}
		@Override
		public void afterTextChanged( Editable s ) {}
	};



	protected void hideSoftKeyboard() {
		View v = mFragmentView.findViewById(R.id.address);
		InputMethodManager imm = (InputMethodManager)mContext.getSystemService( Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}


	protected void doCancel() {
		hideSoftKeyboard();
    	mHostActivity.doSearchCancel();
	}

	protected void doOk( LocationData loc ) {
		hideSoftKeyboard();
		mHostActivity.doSearchOk( mType, loc );
	}


	/**[ adapter ]***********************************************************************************************************/

	protected class ListAdapter extends ArrayAdapter<LocationData>
	{
		protected Context mContext;

		public ListAdapter(Activity activity, int textViewResourceId, ArrayList<LocationData> objects) {
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
				int layoutId = R.layout.address_search_row;

				LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = li.inflate(layoutId, null);

				WebnetTools.setCustomFonts( TDApplication.getAppContext(), (ViewGroup)view );
			}

			WebnetTools.setText( view, R.id.item_address, mItems.get(position).getAddress() );
			view.setTag( R.id.tag_key_position, position );
			view.setOnClickListener( mOnClickListener );

			return( view );
		}

		protected View.OnClickListener mOnClickListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				int position = (Integer) v.getTag(R.id.tag_key_position);
				LocationData item = mItems.get(position);

				EditText et = (EditText)mFragmentView.findViewById(R.id.address);
				et.setText( item.getAddress() );

				doOk( item );
			}
		};


	}


	/**[ auto complete - helpers ]************************************************************************************/

	protected AtomicBoolean mGetPlacesTaskRunning = new AtomicBoolean();

	protected Boolean getPlacesTaskLock() {
		return mGetPlacesTaskRunning.get();
	}
	protected void setPlacesTaskLock( Boolean state ) {
		mGetPlacesTaskRunning.set(state);
	}
	protected class GetPlacesPredictionsAsyncTask extends AsyncTask<String, Void, Integer> {

		ArrayList<LocationData> mPredictionsArray = null;

		@Override
		protected Integer doInBackground(String ... params)
		{
			String queryString = params[0];
			WebnetLog.d( "Asking for places: '" + queryString + "'");

			try {
				ApiHelper api = ApiHelper.getInstance(TDApplication.getAppContext());

				ApiResponse response = api.locationSearch(queryString, 10, false);
				if( response.getErrorCode() == Const.ErrorCode.OK ) {
					mPredictionsArray = new ArrayList<LocationData>();

					WebnetLog.d( "places search: " + response.getJSONObject() );
					JSONArray locations = JsonTools.getJSONArray( response.getJSONObject(), "locations" );
					for( int i=0; i<locations.length(); i++ ) {
						LocationData loc = new LocationData( (JSONObject)locations.get(i));
						WebnetLog.d("place: " + loc.getAddress() );
						mPredictionsArray.add( loc );
					}

				} else {
					WebnetLog.d("places No results?!");
				}
			} catch ( Exception e ) {
				e.printStackTrace();
			}

			return 0;
		}


		@Override
		protected void onPostExecute(Integer result) {

			if( mPredictionsArray != null ) {
				mItems = mPredictionsArray;

				ListView lv = (ListView)mFragmentView.findViewById( R.id.list );
				mAdapter = new ListAdapter( mParentActivity, 0, mItems );
				lv.setAdapter( mAdapter );
			} else {
				WebnetLog.d("No result");
			}

			setPlacesTaskLock( false );
		}

	}


// end of class
}
