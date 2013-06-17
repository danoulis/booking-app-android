package com.tdispatch.passenger.fragment.dialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
					newFragment.show( getActivity().getSupportFragmentManager(), "datePicker");
				}
				break;

				case R.id.button_pickup_time: {
					DialogFragment newFragment = new TimePickerFragment();
					newFragment.show( getActivity().getSupportFragmentManager(), "timePicker");
				}
				break;

				case R.id.button_ok: {
					Long pickupMillis = null;

					if( (mPickupHour == 0) && (mPickupMinute == 0) &&
						(mPickupYear == 0) && (mPickupMonth == 0) && (mPickupDay == 0) ) {
						pickupMillis = null;
					} else {
						Calendar c = Calendar.getInstance();
						c.set(mPickupYear, mPickupMonth, mPickupDay, mPickupHour, mPickupMinute );
						pickupMillis = c.getTimeInMillis();
					}

					mHostFragment.bookingConfirmed(mPickup, mDropoff, pickupMillis);
					dismiss();
				}
				break;

				case R.id.button_cancel: {
					dismiss();
				}
				break;
			}
		}
	};



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
			pickupTime = String.format("%02d:%02d", mPickupHour, mPickupMinute);
			pickupDateVisibility = View.VISIBLE;
		}
		WebnetTools.setText(mFragmentView, R.id.button_pickup_time, pickupTime);
		WebnetTools.setVisibility( mFragmentView,  R.id.pickup_date_container, pickupDateVisibility);

		String pickupDate = "";
		if( ((mPickupYear == 0) && (mPickupMonth == 0) && (mPickupDay == 0)) == false ) {
			SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd yyyy");
			sdf.setTimeZone(TimeZone.getDefault());
			pickupDate = sdf.format( new Date(mPickupYear-1900, mPickupMonth, mPickupDay, mPickupHour, mPickupMinute) );
		}
		WebnetTools.setText(mFragmentView, R.id.button_pickup_date, pickupDate );
	}



	protected int mPickupHour = 0;
	protected int mPickupMinute = 0;
	protected void initPickupTimeOnce() {
		if( (mPickupHour == 0) && (mPickupMinute == 0) ) {
			Calendar c = Calendar.getInstance();
			mPickupHour = c.get(Calendar.HOUR_OF_DAY);
			mPickupMinute = c.get(Calendar.MINUTE);
		}
	}

	public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener
	{
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			int hour = mPickupHour;
			int minute = mPickupMinute;

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
			mPickupHour = hourOfDay;
			mPickupMinute = minute;

			initPickupDateOnce();
			updateDisplay();
		}
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
	public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener
	{
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			int y = mPickupYear;
			int m = mPickupMonth;
			int d = mPickupDay;

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
			mPickupYear = year;
			mPickupMonth = month;
			mPickupDay = day;

			initPickupTimeOnce();
			updateDisplay();
		}
	}



}	// end of class
