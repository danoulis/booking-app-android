package com.tdispatch.passenger.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.SearchActivity;
import com.tdispatch.passenger.api.ApiHelper;
import com.tdispatch.passenger.api.ApiResponse;
import com.tdispatch.passenger.common.Const;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.core.TDFragment;
import com.tdispatch.passenger.host.AddressSearchHostInterface;
import com.tdispatch.passenger.host.AddressSearchModuleInterface;
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
public class SearchAddressFragment extends TDFragment implements AddressSearchModuleInterface
{
	protected ArrayList<LocationData> mItems = new ArrayList<LocationData>();
	protected ListAdapter mAdapter;

	protected int mType = SearchActivity.TYPE_UNKNOWN;
	protected LocationData mAddress;

	protected AddressSearchHostInterface mAddressSearchHost;


	protected Boolean mVoiceSearchAvailable = false;

	@Override
	protected int getLayoutId() {
		return R.layout.search_address_fragment;
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


		// Check to see if a voice recognition activity is present on device
		PackageManager pm = mContext.getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities( new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		mVoiceSearchAvailable = (activities.size() != 0);
	}

	@Override
	protected void onPostCreateView() {

		ImageView iv = (ImageView)mFragmentView.findViewById(R.id.icon);
		EditText et = (EditText)mFragmentView.findViewById(R.id.address);

		int imgId = R.drawable.ic_launcher;
		int hintId = R.string.address_search_generic_hint;
		switch( mType ) {
			case SearchActivity.TYPE_PICKUP:
				imgId = R.drawable.map_marker_pickup;
				hintId = R.string.address_search_pickup_hint;
				break;
			case SearchActivity.TYPE_DROPOFF:
				imgId = R.drawable.map_marker_dropoff;
				hintId = R.string.address_search_dropoff_hint;
				break;
		}
		iv.setImageResource( imgId );

		et.setText( ( mAddress != null ) ? mAddress.getAddress() : "");
		et.setHint( hintId );

		et.setOnEditorActionListener( mOnEditorActionListener );
		et.addTextChangedListener( mTextWatcher );

		WebnetTools.setVisibility(mFragmentView, R.id.button_voice_search, mVoiceSearchAvailable ? View.VISIBLE : View.GONE);

		int[] ids = { R.id.button_voice_search, R.id.button_clear };
		for( int id : ids ) {
			View v = mFragmentView.findViewById(id);
			if( v != null ) {
				v.setOnClickListener(mOnClickListener);
			}
		}

		WebnetTools.setVisibility(mFragmentView, R.id.button_clear, View.GONE);

		ListView lv = (ListView)mFragmentView.findViewById( R.id.list );
		mAdapter = new ListAdapter( mParentActivity, 0, mItems );
		lv.setAdapter( mAdapter );
	}



	protected View.OnClickListener mOnClickListener = new OnClickListener()
	{
		@Override
		public void onClick( View v ) {
			switch( v.getId() ) {
				case R.id.button_voice_search: {
					if( mVoiceSearchAvailable ) {
						Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
						intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM );
						try {
							startActivityForResult(intent, Const.RequestCode.VOICE_RECOGNITION);
						} catch ( Exception e ) {
							e.printStackTrace();
						}
					}
				}
				break;

				case R.id.button_clear: {
					EditText et = (EditText)mFragmentView.findViewById(R.id.address);
					et.setText("");
				}
				break;
			}
		}
	};


	@Override
	protected Boolean isBusyOverlayPresent() {
		return false;
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		if( (requestCode == Const.RequestCode.VOICE_RECOGNITION) && (resultCode == Activity.RESULT_OK) ) {
			ArrayList<String> matches = intent.getStringArrayListExtra( RecognizerIntent.EXTRA_RESULTS );
			WebnetLog.d("size: " + matches.size());
			if( matches.size() > 0 ) {
				WebnetLog.d( "1st: " + matches.get(0) );
				EditText et = (EditText)mFragmentView.findViewById(R.id.address);
				et.setText( matches.get(0) );
			}
		}
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
				WebnetTools.setVisibility(mFragmentView, R.id.button_clear, View.VISIBLE);
			} else {
				WebnetTools.setVisibility(mFragmentView, R.id.button_clear, View.GONE);
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
				int layoutId = R.layout.search_address_row;

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
		protected Integer doInBackground(String ... params) {

			String queryString = params[0];

			try {
				ApiHelper api = ApiHelper.getInstance(TDApplication.getAppContext());

				ApiResponse response = api.locationSearch(queryString, 10, false);
				if( response.getErrorCode() == Const.ErrorCode.OK ) {
					mPredictionsArray = new ArrayList<LocationData>();

					JSONArray locations = JsonTools.getJSONArray( response.getJSONObject(), "locations" );
					for( int i=0; i<locations.length(); i++ ) {
						LocationData loc = new LocationData( (JSONObject)locations.get(i));
						mPredictionsArray.add( loc );
					}
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
