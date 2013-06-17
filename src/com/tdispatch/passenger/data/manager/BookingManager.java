package com.tdispatch.passenger.data.manager;

import java.util.ArrayList;

import com.tdispatch.passenger.core.OnBookingManagerStateChangeListener;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.model.BookingData;

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
final public class BookingManager
{
	protected TDApplication mContext;
	protected BookingManager mBookingManager;



	protected ArrayList<BookingData> mBookings = new ArrayList<BookingData>();




	protected static BookingManager _instance = null;
	public static BookingManager getInstance(TDApplication context) {
		if( _instance == null ) {
			_instance = new BookingManager(context);
		}

		return _instance;
	}


	/**[ constructor ]*******************************************************************************************************************/

	public BookingManager( TDApplication context ) {
		mContext = context;
	}





	/**[ booking state listeners ]*******************************************************************************************************/

	public static final int STATE_UNKNOWN				= 0;
	public static final int STATE_LOADING_BOOKINGS	= 1;
	public static final int STATE_BOOKINGS_LOADED		= 2;
	public static final int STATE_BOOKINGS_UPDATED	= 3;


	protected int mCurrentState = STATE_UNKNOWN;

	protected ArrayList<OnBookingManagerStateChangeListener> mOnBookingManagerStateChangeListeners = new ArrayList<OnBookingManagerStateChangeListener>();

	protected void notifyStateChangeListeners( int state ) {
		// notify all listeners
		for( int i=0; i<mOnBookingManagerStateChangeListeners.size(); i++ ) {
			OnBookingManagerStateChangeListener tmp = mOnBookingManagerStateChangeListeners.get( i );
			tmp.onStateChange( state );
		}
	}

	// registers new listener to be notified on each login/logout state change
	// checks for duplicated entries first
	public void setOnStateChangeListener( OnBookingManagerStateChangeListener listener ) {
		setOnStateChangeListener( listener, OnBookingManagerStateChangeListener.FLAG_DEFAULT );
	}
	public void setOnStateChangeListener( OnBookingManagerStateChangeListener listener, int flags ) {
		Boolean listenerAlreadyRegistered = false;

		for( int i=0; i<mOnBookingManagerStateChangeListeners.size(); i++ ) {
			if( mOnBookingManagerStateChangeListeners.get( i ) == listener ) {
				listenerAlreadyRegistered = true;
				break;
			}
		}

		if( listenerAlreadyRegistered == false ) {
			mOnBookingManagerStateChangeListeners.add( listener );

			if( (flags & OnBookingManagerStateChangeListener.FLAG_NOTIFY_WITH_CURRENT_STATE) != 0 ) {
				// notify attached listener on current state
				listener.onStateChange( mCurrentState );
			}
		}
	}

	// removes previously registered listener
	public void unsetOnSignInStateChangeListener( OnBookingManagerStateChangeListener listener ) {
		for( int i=0; i<mOnBookingManagerStateChangeListeners.size(); i++ ) {
			if( mOnBookingManagerStateChangeListeners.get( i ) == listener ) {
				mOnBookingManagerStateChangeListeners.remove( i );
				break;
			}
		}
	}




} // end of class
