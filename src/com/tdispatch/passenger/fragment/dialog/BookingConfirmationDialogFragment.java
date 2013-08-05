package com.tdispatch.passenger.fragment.dialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.core.TDDialogFragment;
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
public class BookingConfirmationDialogFragment extends TDDialogFragment
{
	public interface BookingConfirmationDialogClickListener
	{
		public void bookingConfirmed(LocationData pickup, LocationData dropoff, Long pickupMillis);
	}


	public static final int DIALOG_TYPE_OK 		= 0;
	public static final int DIALOG_TYPE_ERROR		= 1;

	protected static final String KEY_PICKUP 		= "pickup";
	protected static final String KEY_DROPOFF	 	= "dropoff";

	public static BookingConfirmationDialogFragment newInstance(LocationData pickup, LocationData dropoff) {

		BookingConfirmationDialogFragment frag = new BookingConfirmationDialogFragment();

		if( pickup == null ) {
			throw new NullPointerException("Pickup location cannot be null");
		}

		Bundle args = new Bundle();
		args.putParcelable(KEY_PICKUP, pickup);
		args.putParcelable(KEY_DROPOFF, dropoff);
		frag.setArguments(args);

		return frag;
	}

	protected BookingConfirmationDialogClickListener mHostFragment;

	protected LocationData mPickup;
	protected LocationData mDropoff;
	protected Long mPickupMillis = 0L;			// 0 == now

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate(savedInstanceState);

		try {
			mHostFragment = (BookingConfirmationDialogClickListener)getTargetFragment();
		} catch( ClassCastException e ) {
			throw new ClassCastException("Host Fragment needs to implement BookingConfirmationDialogClickListener");
		}

		Bundle args = getArguments();
		mPickup = args.getParcelable(KEY_PICKUP);
		mDropoff = args.getParcelable(KEY_DROPOFF);


		int shortestPickupTime = getResources().getInteger(R.integer.caboffice_minimum_allowed_pickup_time_offset_in_minutes);
		if( shortestPickupTime > 0 ) {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis( c.getTimeInMillis() + (shortestPickupTime * (WebnetTools.MILLIS_PER_MINUTE)) );

			mPickupYear = c.get(Calendar.YEAR);
			mPickupMonth = c.get(Calendar.MONTH);
			mPickupDay = c.get(Calendar.DAY_OF_MONTH);

			mPickupHour = c.get(Calendar.HOUR_OF_DAY);
			mPickupMinute = c.get(Calendar.MINUTE);
		}

	}

	@Override
	protected int getLayoutId() {
		return R.layout.booking_confirmation_dialog_fragment;
	}

	@Override
	protected void onPostCreateView() {

		int[] ids = { R.id.button_ok, R.id.button_cancel, R.id.button_pickup_date, R.id.button_pickup_time};
		for( int id : ids ) {
			View button = mFragmentView.findViewById( id );
			button.setOnClickListener( listener );
		}

		updateDisplay();
	}

	protected View.OnClickListener listener = new View.OnClickListener()
	{
		@Override
		public void onClick( View v ) {

			switch( v.getId() ) {

				case R.id.button_pickup_date: {
					DialogFragment newFragment = new DatePickerFragment();

					Bundle args = new Bundle();
					args.putInt( DatePickerFragment.ARGS_YEAR, mPickupYear );
					args.putInt( DatePickerFragment.ARGS_MONTH, mPickupMonth );
					args.putInt( DatePickerFragment.ARGS_DAY, mPickupDay );
					newFragment.setArguments(args);

					newFragment.setTargetFragment( BookingConfirmationDialogFragment.this, 0 );

					newFragment.show( getActivity().getSupportFragmentManager(), "datePicker");
				}
				break;

				case R.id.button_pickup_time: {
					DialogFragment newFragment = new TimePickerFragment();

					Bundle args = new Bundle();
					args.putInt(TimePickerFragment.ARGS_HOURS, mPickupHour);
					args.putInt(TimePickerFragment.ARGS_MINUTES, mPickupMinute);
					newFragment.setArguments(args);

					newFragment.setTargetFragment(BookingConfirmationDialogFragment.this, 0);

					newFragment.show( getActivity().getSupportFragmentManager(), "timePicker");
				}
				break;

				case R.id.button_ok: {
					if( validatePickupTimeAndShowMessage() ){
						mHostFragment.bookingConfirmed(mPickup, mDropoff, getPickupTimeMillis());
						dismiss();
					}
				}
				break;

				case R.id.button_cancel: {
					dismiss();
				}
				break;
			}
		}
	};



	protected Long getPickupTimeMillis() {
		Long pickupMillis;

		if( (mPickupHour == 0) && (mPickupMinute == 0) &&
				(mPickupYear == 0) && (mPickupMonth == 0) && (mPickupDay == 0) ) {
				pickupMillis = null;
			} else {
				Calendar c = Calendar.getInstance();
				c.set(mPickupYear, mPickupMonth, mPickupDay, mPickupHour, mPickupMinute );
				pickupMillis = c.getTimeInMillis();
			}

		return pickupMillis;
	}


	protected Boolean validatePickupTimeAndShowMessage() {

		Long pickupMillis = getPickupTimeMillis();

		Boolean result = true;

		if( result ) {
			int maxDaysAhead = getResources().getInteger(R.integer.caboffice_settings_new_bookings_max_days_ahead);

			String pickupMillisBody = "";

			if( pickupMillis != null ) {
				Long diff = (pickupMillis - System.currentTimeMillis());

				if( diff > 0 ) {
					if( diff > (WebnetTools.MILLIS_PER_MINUTE * 5) ) {
						if( diff > (WebnetTools.MILLIS_PER_DAY * maxDaysAhead) ) {
							pickupMillisBody = getString(R.string.new_booking_pickup_date_too_ahead_body_fmt, maxDaysAhead);
							result = false;
						}
					}
				} else {
					pickupMillisBody = getString(R.string.new_booking_pickup_date_already_passed);
					result = false;
				}

				if (!result ) {
					showDialog( GenericDialogFragment.DIALOG_TYPE_ERROR, getString(R.string.dialog_error_title), pickupMillisBody );
				}
			}
		}


		if( result ) {
			int shortestPickupTimeOffset = getResources().getInteger(R.integer.caboffice_minimum_allowed_pickup_time_offset_in_minutes);

			if( (mPickupHour == 0) && (mPickupMinute == 0) &&
				(mPickupYear == 0) && (mPickupMonth == 0) && (mPickupDay == 0) ) {
				pickupMillis = null;
			} else {
				Calendar c = Calendar.getInstance();
				c.set(mPickupYear, mPickupMonth, mPickupDay, mPickupHour, mPickupMinute );
				pickupMillis = c.getTimeInMillis();
			}

			Boolean pickupTimeTooEarly = false;
			if( shortestPickupTimeOffset > 0 ) {
				if( pickupMillis != null ) {
					if( (pickupMillis - System.currentTimeMillis()) < (shortestPickupTimeOffset*WebnetTools.MILLIS_PER_MINUTE) ) {
						pickupTimeTooEarly = true;
					}
				}
			}


			if( pickupTimeTooEarly ) {
				String tooEarly = getString(R.string.new_booking_pickup_date_too_early_fmt, shortestPickupTimeOffset);
				showDialog( GenericDialogFragment.DIALOG_TYPE_ERROR, getString(R.string.dialog_error_title), tooEarly );

				result = false;
			}
		}


		return result;
	}


	protected void updateDisplay() {
		WebnetTools.setText(mFragmentView, R.id.pickup_address, mPickup.getAddress());
		if( mDropoff != null ) {
			WebnetTools.setText(mFragmentView, R.id.dropoff_address, mDropoff.getAddress());
		} else {
			WebnetTools.setVisibility(mFragmentView, R.id.dropoff_container, View.GONE);
		}

		String pickupTime = getString(R.string.new_booking_dialog_pickup_time_now);
		int pickupDateVisibility = View.GONE;
		if( ((mPickupHour == 0) && (mPickupMinute == 0)) == false ) {
			pickupTime = String.format(Locale.US, "%02d:%02d", mPickupHour, mPickupMinute);
			pickupDateVisibility = View.VISIBLE;
		}
		WebnetTools.setText(mFragmentView, R.id.button_pickup_time, pickupTime);
		WebnetTools.setVisibility( mFragmentView,  R.id.pickup_date_container, pickupDateVisibility);

		String pickupDate = "";
		if( ((mPickupYear == 0) && (mPickupMonth == 0) && (mPickupDay == 0)) == false ) {
			SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd yyyy", Locale.US);
			sdf.setTimeZone(TimeZone.getDefault());
			pickupDate = sdf.format( new Date(mPickupYear-1900, mPickupMonth, mPickupDay, mPickupHour, mPickupMinute) );
		}
		WebnetTools.setText(mFragmentView, R.id.button_pickup_date, pickupDate );
	}



	protected int mPickupHour = 0;
	protected int mPickupMinute = 0;

	public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener
	{
		public static final String ARGS_HOURS		= "hours";
		public static final String ARGS_MINUTES	= "minutes";

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			Bundle args = getArguments();

			int hour = args.getInt("hour", 0);
			int minute = args.getInt("minute", 0);

			if( (hour == 0) && (minute == 0)) {
				Calendar c = Calendar.getInstance();

				c.add(Calendar.MINUTE, 10);
				hour = c.get(Calendar.HOUR_OF_DAY);
				minute = c.get(Calendar.MINUTE);
			}

			return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
		}

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			((BookingConfirmationDialogFragment)getTargetFragment()).onPostTimeSet( hourOfDay, minute );
		}
	}


	public void onPostTimeSet( int hourOfDay, int minute ) {
		mPickupHour = hourOfDay;
		mPickupMinute = minute;

		initPickupDateOnce();
		updateDisplay();

		validatePickupTimeAndShowMessage();
	}



	protected int mPickupYear = 0;
	protected int mPickupMonth = 0;
	protected int mPickupDay = 0;
	protected void initPickupDateOnce() {
		if( (mPickupYear == 0) && (mPickupMonth == 0) && (mPickupDay == 0) ) {
			Calendar c = Calendar.getInstance();
			mPickupYear = c.get(Calendar.YEAR);
			mPickupMonth = c.get(Calendar.MONTH);
			mPickupDay = c.get(Calendar.DAY_OF_MONTH);
		}
	}

	public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener
	{
		public static final String ARGS_YEAR	= "year";
		public static final String ARGS_MONTH	= "month";
		public static final String ARGS_DAY	= "day";

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			Bundle args = getArguments();

			int y = args.getInt( ARGS_YEAR, 0);
			int m = args.getInt( ARGS_MONTH, 0);
			int d = args.getInt( ARGS_DAY, 0);

			if( (y+m+d) == 0 ) {
				Calendar c = Calendar.getInstance();
				y = c.get(Calendar.YEAR);
				m = c.get(Calendar.MONTH);
				d = c.get(Calendar.DAY_OF_MONTH);
			}

			return new DatePickerDialog(getActivity(), this, y, m, d );
		}

		@Override
		public void onDateSet(DatePicker view, int year, int month, int day) {
			((BookingConfirmationDialogFragment)getTargetFragment()).onPostDateSet(year, month, day);
		}
	}

	public void onPostDateSet( int year, int month, int day ) {
		mPickupYear = year;
		mPickupMonth = month;
		mPickupDay = day;

		updateDisplay();

		validatePickupTimeAndShowMessage();
	}


}	// end of class
