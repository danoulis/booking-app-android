package com.tdispatch.passenger.model;

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
final public class ListDataContainer
{
	public static final int				TYPE_UNKNOWN				= 0;
	public static final int				TYPE_BOOKING				= 1;

	public static final int				TYPE_MAX_COUNT				= 2;

	protected BookingData 	mBookingData;

	protected int mType = TYPE_UNKNOWN;

	protected Boolean mActionBarFolded	= true;					// state of action bar (if any)


	public ListDataContainer() {
		// dummy
	}

	public ListDataContainer( BookingData data ) {
		setData( data );
	}

	public ListDataContainer setType( int type ) {
		mType = type;
		return this;
	}
	public int getType() {
		return( mType );
	}

	public Object getData() {
		Object object = null;

		switch( mType ) {
			case TYPE_BOOKING:
				object = mBookingData;
				break;

			case TYPE_UNKNOWN:
			default:
				WebnetLog.e("Seems we miss a case() for mType " + mType );
				object = null;
				break;
		}

		return( object );
	}

	public ListDataContainer clearData() {
		mBookingData = null;

		mType = TYPE_UNKNOWN;

		return this;
	}

	public ListDataContainer setData( BookingData data ) {
		clearData();
		mBookingData = data;
		setType( TYPE_BOOKING );

		return this;
	}



	public ListDataContainer setActionBarFolded( Boolean isFolded ) {
		mActionBarFolded = isFolded;
		return this;
	}
	public Boolean isActionBarFolded() {
		return mActionBarFolded;
	}

	public Boolean toogleActionBarFold() {
		setActionBarFolded( !isActionBarFolded() );
		return isActionBarFolded();
	}


// end of class
}
