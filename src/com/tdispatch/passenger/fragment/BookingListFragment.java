package com.tdispatch.passenger.fragment;

import java.util.ArrayList;

import org.json.JSONArray;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.api.ApiHelper;
import com.tdispatch.passenger.api.ApiResponse;
import com.tdispatch.passenger.common.Const;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.core.TDFragment;
import com.tdispatch.passenger.fragment.dialog.BookingCancelConfirmationDialogFragment;
import com.tdispatch.passenger.fragment.dialog.BookingCancelConfirmationDialogFragment.BookingCancelConfirmationDialogClickListener;
import com.tdispatch.passenger.host.MapHostInterface;
import com.tdispatch.passenger.model.BookingData;
import com.tdispatch.passenger.model.ListDataContainer;
import com.tdispatch.passenger.model.LocationData;
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
public class BookingListFragment extends TDFragment implements BookingCancelConfirmationDialogClickListener
{
	protected Handler mHandler = new Handler();
	protected MapHostInterface mMapHostActivity;

	@Override
	protected int getLayoutId() {
		return R.layout.booking_list_fragment;
	}

	@Override
	protected void onPostCreateView() {

		ListView lv = (ListView)mFragmentView.findViewById( R.id.list );
		mAdapter = new ListAdapter( mParentActivity, 0, mBookings );
		lv.setAdapter( mAdapter );

		int[] ids = { R.id.button_retry };
		for( int id : ids ) {
			View v = mFragmentView.findViewById( id );
			v.setOnClickListener( mOnClickListener );
		}

		WebnetTools.setVisibility(mFragmentView, R.id.booking_list_container, View.GONE );
		WebnetTools.setVisibility(mFragmentView, R.id.booking_list_empty_container, View.GONE);
		WebnetTools.setVisibility(mFragmentView, R.id.booking_error_container, View.GONE);

		downloadBookings();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mMapHostActivity = (MapHostInterface)activity;
		} catch( ClassCastException e ) {
			throw new ClassCastException("Host Activity needs to implement MapHostInterface");
		}
	}

	@Override
	protected Integer getOverlayBackgroundResourceId() {
		return R.color.background;
	}


	protected ArrayList<ListDataContainer> mBookings = new ArrayList<ListDataContainer>();
	protected ListAdapter mAdapter;


	/**[ listener ]**********************************************************************************************************/

	protected View.OnClickListener mOnClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick( View v ) {
			switch( v.getId() ) {
				case R.id.button_retry: {
					downloadBookings();
				}
				break;
			}

		}
	};


	/**[ adapter ]***********************************************************************************************************/

	protected class ListAdapter extends ArrayAdapter<ListDataContainer>
	{
		protected TDApplication mApp;
		protected Context mContext;

		public ListAdapter(Activity activity, int textViewResourceId, ArrayList<ListDataContainer> objects) {
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

			ListDataContainer ldc = mBookings.get(position);
			BookingData item = (BookingData)ldc.getData();

			View view = convertView;
			if( view == null) {
				int layoutId = R.layout.booking_list_row;

				LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = li.inflate(layoutId, null);

				WebnetTools.setCustomFonts( TDApplication.getAppContext(), (ViewGroup)view );
			}

			WebnetTools.setVisibility(view, R.id.label, View.GONE);

			WebnetTools.setText( view, R.id.pickup_location, item.getPickupLocation().getAddress() );
			LocationData dropoffLocationData = item.getDropoffLocation();
			if( dropoffLocationData != null ) {
				WebnetTools.setText( view, R.id.dropoff_location, dropoffLocationData.getAddress() );
				WebnetTools.setVisibility(view, R.id.button_dropoff, View.VISIBLE);
				WebnetTools.setVisibility(view, R.id.button_pickup_and_dropoff, View.VISIBLE);
			} else {
				WebnetTools.setText( view, R.id.dropoff_location, "---");
				WebnetTools.setVisibility(view, R.id.button_dropoff, View.INVISIBLE);
				WebnetTools.setVisibility(view, R.id.button_pickup_and_dropoff, View.INVISIBLE);
			}

			WebnetTools.setText( view, R.id.pickup_date, WebnetTools.dateDiffToString( item.getPickupDate() ) );

			Boolean canCancelBooking = false;
			switch( item.getType() ) {
				case BookingData.TYPE_INCOMING:
				case BookingData.TYPE_FROM_PARTNER:
				case BookingData.TYPE_DISPATCHED:
				case BookingData.TYPE_CONFIRMED:
					canCancelBooking = true;
					break;

				case BookingData.TYPE_ACTIVE:
				case BookingData.TYPE_QUOTING:
				case BookingData.TYPE_DRAFT:
				case BookingData.TYPE_COMPLETED:
				case BookingData.TYPE_REJECTED:
				case BookingData.TYPE_CANCELLED:
				default:
					canCancelBooking = false;
					break;
			}

			WebnetLog.d("#" + position + ", Type: " + item.getTypeName() + ", pickup: " + item.getPickupLocation().getAddress());
			WebnetTools.setVisibility(mFragmentView, R.id.button_cancel_booking, (canCancelBooking) ? View.VISIBLE : View.GONE);


			int[] ids = { 	R.id.row_info_container,
							R.id.button_pickup, R.id.button_dropoff, R.id.button_pickup_and_dropoff,
							R.id.button_cancel_booking };
			for( int id : ids ) {
				View v = view.findViewById(id);
				v.setOnClickListener( mOnClickListener );
				v.setOnLongClickListener( mOnLongClickListener );
				v.setTag( R.id.tag_key_position, position );
			}


			WebnetTools.setVisibility(view, R.id.row_action_menu_container, ldc.isActionBarFolded() ? View.GONE : View.VISIBLE);

			int bgResourceId = ((position % 2) == 0) ? R.color.booking_list_bg_even : R.color.booking_list_bg_odd;;
			if( ldc.isActionBarFolded() == false ) {
				bgResourceId = R.color.list_row_bg_highlight;
			}
			view.setBackgroundResource( bgResourceId );


			return( view );
		}

		protected View.OnLongClickListener mOnLongClickListener = new View. OnLongClickListener()
		{
			@Override
			public boolean onLongClick( View v ) {
				Boolean result = false;

				int position = (Integer) v.getTag(R.id.tag_key_position);
				ListDataContainer ldc = (ListDataContainer)mBookings.get(position);
				BookingData booking = (BookingData)ldc.getData();

				switch( v.getId() ) {
					case R.id.button_pickup: {
						mMapHostActivity.moveMapToLocation( booking.getPickupLocation() );
						result = true;
					}
					break;

					case R.id.button_dropoff: {
						if( booking.getDropoffLocation() != null ) {
							mMapHostActivity.moveMapToLocation( booking.getDropoffLocation() );
							result = true;
						}
					}
					break;
				}

				return result;
			}
		};

		protected View.OnClickListener mOnClickListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				int position = (Integer) v.getTag(R.id.tag_key_position);
				ListDataContainer ldc = (ListDataContainer)mBookings.get(position);
				BookingData booking = (BookingData)ldc.getData();

				switch( v.getId() ) {
					case R.id.row_info_container: {
						toggleRowActionBar(position);
					}
					break;

					case R.id.button_pickup: {
						mMapHostActivity.setPickupLocation( booking.getPickupLocation() );
					}
					break;

					case R.id.button_dropoff: {
						if( booking.getDropoffLocation() != null ) {
							mMapHostActivity.setDropoffLocation( booking.getDropoffLocation() );
						}
					}
					break;

					case R.id.button_pickup_and_dropoff: {
						mMapHostActivity.setLocation( booking.getPickupLocation(), booking.getDropoffLocation() );
					}
					break;

					case R.id.button_cancel_booking: {
						switch( booking.getType() ) {
							case BookingData.TYPE_QUOTING:
							case BookingData.TYPE_INCOMING:
							case BookingData.TYPE_FROM_PARTNER:
							case BookingData.TYPE_DISPATCHED:
							case BookingData.TYPE_CONFIRMED:
							case BookingData.TYPE_ACTIVE:
							case BookingData.TYPE_COMPLETED:
							case BookingData.TYPE_DRAFT: {
								BookingCancelConfirmationDialogFragment frag = BookingCancelConfirmationDialogFragment.newInstance(booking);
								frag.setTargetFragment(BookingListFragment.this, 0);
								frag.show(((FragmentActivity)mParentActivity).getSupportFragmentManager(), "bookingcancelconfirmation");
							}
							break;
						}
					}
					break;
				}
			}
		};


		public int toggleRowActionBar( int position ) {

			int foldedRows = 0;

			for( int i=0; i<mBookings.size(); i++ ) {
				if( i != position ) {
					ListDataContainer ldc = mBookings.get(i);

					if( ldc.isActionBarFolded() == false) {
						ldc.setActionBarFolded( true );
						foldedRows++;
					}
				}
			}


			if( position != -1 ) {
				ListDataContainer ldc = (ListDataContainer) mBookings.get( position );
				ldc.toogleActionBarFold();
			}

			mAdapter.notifyDataSetChanged();

			return foldedRows;
		}

	} // end of class


	/**[ fetching bookings ]*************************************************************************************************/

	public void addBooking( BookingData booking ) {
		mBookings.add( 0, new ListDataContainer(booking) );
		mAdapter.notifyDataSetChanged();
	}

	public void downloadBookings() {
		WebnetTools.executeAsyncTask( new GetBookingsAsyncTask() );
	}

	public class GetBookingsAsyncTask extends AsyncTask<Void, Void, ApiResponse> {

		@Override
		protected void onPreExecute() {

			showDownloadErrorMessage( false );

			lockUI(true);

			mBookings = new ArrayList<ListDataContainer>();
		}

		@Override
		protected ApiResponse doInBackground( Void ... args ) {
			return fetchBookings("incoming,completed,confirmed,active");
		}

		@Override
		protected void onPostExecute(ApiResponse response) {

			Boolean downloadFailed = true;

			lockUI(false);

			if( response != null ) {
				if( response.getErrorCode() == Const.ErrorCode.OK) {
					showListEmptyMessage( (mBookings.size() == 0) );

					ListView lv = (ListView)mFragmentView.findViewById( R.id.list );
					mAdapter = new ListAdapter( mParentActivity, 0, mBookings );
					lv.setAdapter( mAdapter );

					downloadFailed = false;
				}
			}

			if( downloadFailed ) {
				showDownloadErrorMessage(true);
			}
		}

		protected ApiResponse fetchBookings( String type ) {

			ApiResponse response = new ApiResponse();

			try {
				ApiHelper api = ApiHelper.getInstance( mApp );
				response = api.bookingsGetAll( type );

				if( response.getErrorCode() == Const.ErrorCode.OK ) {

					JSONArray bookingArray = response.getJSONObject().getJSONArray("bookings");
					int bookingCount = bookingArray.length();
					if( bookingCount > 0 ) {
						for( int i=0; i<bookingCount; i++ ) {
							BookingData booking = new BookingData( bookingArray.getJSONObject(i) );
							mBookings.add( new ListDataContainer(booking) );
						}
					}

				} else {
					WebnetLog.e("Failed to get bookings: " + response.getErrorCode() );
				}

			} catch ( Exception e ) {
				e.printStackTrace();
			}

			return response;
		}
	}


	/**[ helpers ]***********************************************************************************************************/

	protected void showDownloadErrorMessage( Boolean showMessage ) {
		WebnetTools.setVisibility(mFragmentView, R.id.booking_list_container, (showMessage) ? View.GONE : View.VISIBLE);
		WebnetTools.setVisibility(mFragmentView, R.id.booking_error_container, (showMessage) ? View.VISIBLE : View.GONE );
	}

	protected void showListEmptyMessage( Boolean isListEmpty ) {
		WebnetTools.setVisibility(mFragmentView, R.id.booking_list_container, (isListEmpty) ? View.GONE : View.VISIBLE);
		WebnetTools.setVisibility(mFragmentView, R.id.booking_list_empty_container, (isListEmpty) ? View.VISIBLE : View.GONE);
	}

	/**[ BookingCancelConfirmationDialogClickListener ]**********************************************************************/

	@Override
	public void doBookingCancel(BookingData booking) {
		WebnetTools.executeAsyncTask( new CancelBookingAsyncTask( booking ) );
	}

	public class CancelBookingAsyncTask extends AsyncTask<Void, Void, ApiResponse> {

		protected BookingData mBooking;
		public CancelBookingAsyncTask( BookingData booking ) {
			mBooking = booking;
		}

		@Override
		protected void onPreExecute() {
			lockUI(true);
		}

		@Override
		protected ApiResponse doInBackground( Void ... args ) {

			ApiResponse response = new ApiResponse();

			try {
				ApiHelper api = ApiHelper.getInstance( mApp );
				response = api.bookingsCancelBooking( mBooking.getPk() );

			} catch ( Exception e ) {
				e.printStackTrace();
			}

			return response;
		}

		@Override
		protected void onPostExecute(ApiResponse response) {

			if( response != null ) {
				if( response.getErrorCode() == Const.ErrorCode.OK ) {
					WebnetLog.d("Booking cancelled");
					doCancel( mBooking );
				} else {
					WebnetLog.e("Failed to cancel booking: " + response.getErrorCode() );
				}
			}

			lockUI(false);
		}
	}


	protected void doCancel( BookingData booking ) {
		for( int i=0; i<mBookings.size(); i++ ) {
			ListDataContainer ldc = mBookings.get(i);

			WebnetLog.d("#" + i + ", type: " + ldc.getType());

			if( ldc.getType() == ListDataContainer.TYPE_BOOKING ) {
				if( ((BookingData)ldc.getData()).getPk().equals( booking.getPk() ) ) {
					WebnetLog.d("cancelled @#" + i);
					mBookings.remove(i);

					ListView lv = (ListView)mFragmentView.findViewById( R.id.list );
					mAdapter = new ListAdapter( mParentActivity, 0, mBookings );
					lv.setAdapter( mAdapter );

					showListEmptyMessage( (mBookings.size() == 0) );

					break;
				}
			}
		}
	}


// end of class
}
