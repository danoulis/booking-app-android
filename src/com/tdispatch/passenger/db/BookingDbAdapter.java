package com.tdispatch.passenger.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.model.BookingData;
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
public class BookingDbAdapter extends DbAdapter
{
	public static final String DB_TABLE					= "booking";

	public static final String KEY_LOCAL_ID 			= "_id";

	public static final String KEY_BOOKING_PK			= "booking_pk";
	public static final String KEY_PICKUP_ADDRESS		= "pickup_address";
	public static final String KEY_DROPOFF_ADDRESS		= "dropoff_address";
	public static final String KEY_PICKUP_DATE			= "date";
	public static final String KEY_TYPE					= "type";

	public static final String KEY_JSON					= "json";

	protected String[] mMapping = { KEY_LOCAL_ID, KEY_JSON, KEY_BOOKING_PK, KEY_TYPE, KEY_PICKUP_DATE, KEY_PICKUP_ADDRESS, KEY_DROPOFF_ADDRESS };

	public BookingDbAdapter( TDApplication context ) {
		super( context );
	}

	@Override
	public String getTableName() {
		return DB_TABLE;
	}


	// called by SystemDbOpenHelper() to create DB
	public static void createDbTable(SQLiteDatabase db) {

		// create user profile table
		String query = "CREATE TABLE " + DB_TABLE + "("
				+ KEY_LOCAL_ID + " INTEGER PRIMARY KEY"

				+ "," + KEY_BOOKING_PK + " TEXT"

				+ "," + KEY_PICKUP_ADDRESS + " TEXT"
				+ "," + KEY_DROPOFF_ADDRESS + " TEXT"
				+ "," + KEY_PICKUP_DATE + " DATE KEY"

				+ "," + KEY_TYPE + " INTEGER KEY"

				+ "," + KEY_JSON + " TEXT"
				+ ")";

		db.execSQL( query );
	}

	public static void deleteDbTable( SQLiteDatabase db ) {
		String query = "DROP TABLE " + DB_TABLE;
		db.execSQL( query );
	}


	// these tables are not for permanent cache, so it's fine to drop and create
	// them on upgrade, as these will be re-populated on driver's login
	public static void upgradeDbTable( SQLiteDatabase db, int oldVersion, int newVersion ) {
		deleteDbTable( db );
		createDbTable( db );
	}

	@Override
	public BookingDbAdapter open() throws SQLException {
		if( mDb == null ) {
			mDbHelper = DbOpenHelper.getInstance( mContext );
			mDb = mDbHelper.getReadableDatabase();
		}
		return this;
	}
	@Override
	public void close() {
		if( mDb != null ) {
			mDb.close();
			mDb = null;
		}
		if( mDbHelper != null ) {
			mDbHelper.close();
			mDbHelper = null;
		}
	}

	public long insert( BookingData booking ) {

		ContentValues values = new ContentValues();
		values.put( KEY_BOOKING_PK, booking.getPk() );
		values.put( KEY_PICKUP_ADDRESS, booking.getPickupLocation().getAddress() );
		values.put( KEY_PICKUP_DATE, booking.getPickupDate().getTime() / WebnetTools.MILLIS_PER_SECOND );
		values.put( KEY_DROPOFF_ADDRESS, booking.getPickupLocation().getAddress() );
		values.put( KEY_TYPE, booking.getType());
		values.put( KEY_JSON, booking.getJson().toString() );

		open();
		long id = mDb.insert( DB_TABLE, null, values );
		close();

		return id;
	}

//	public BookingData get( Long id ) {
//
//		BookingData result = null;
//
//		String whereClause = String.format( "%s=?", KEY_LOCAL_ID  );
//		String[] whereArgs = { String.valueOf(id) };
//
//		open();
//
//		try {
//			Cursor cursor = mDb.query( true, DB_TABLE, mMapping, whereClause, whereArgs, null, null, null, null );
//			if( cursor != null ) {
//				if( cursor.getCount() == 1 ) {
//					cursor.moveToFirst();
//					result = new BookingData( cursor );
//				} else {
//					WebnetLog.e("Wrong number of rows for id: " + id + " cnt: " + cursor.getCount() + " (needs 1)");
//				}
//
//				cursor.close();
//
//			} else {
//				WebnetLog.e("Query failed for id: " + id);
//			}
//		} catch ( Exception e ) {
//			e.printStackTrace();
//		}
//
//		close();
//
//		return result;
//	}

//	// type is BookingData.TYPE_xxxx and it can be OR'ed!
//	public ArrayList<BookingData> getAllByType( int type ) {
//
//		ArrayList<BookingData> profiles = new ArrayList<BookingData>();
//
//		String whereClause = "";
//		ArrayList<String> args = new ArrayList<String>();
//
//		String mergeGlue = "";
//
//		if( (type & BookingData.TYPE_COMPLETED) != 0 ) {
//			whereClause += mergeGlue + BookingDbAdapter.KEY_TYPE + "=? ";
//			args.add( String.valueOf(BookingData.TYPE_COMPLETED));
//			mergeGlue = " OR ";
//		}
//
//		if( (type & BookingData.TYPE_DISPATCHED) != 0 ) {
//			whereClause += mergeGlue + BookingDbAdapter.KEY_TYPE + "=? ";
//			args.add( String.valueOf(BookingData.TYPE_DISPATCHED));
//			mergeGlue = " OR ";
//		}
//
//		if( (type & BookingData.TYPE_CONFIRMED) != 0 ) {
//			whereClause += mergeGlue + BookingDbAdapter.KEY_TYPE + "=? ";
//			args.add( String.valueOf(BookingData.TYPE_CONFIRMED));
//			mergeGlue = " OR ";
//		}
//
//		if( (type & BookingData.TYPE_ACTIVE) != 0 ) {
//			whereClause += mergeGlue + BookingDbAdapter.KEY_TYPE + "=? ";
//			args.add( String.valueOf(BookingData.TYPE_ACTIVE));
//			mergeGlue = " OR ";
//		}
//
//		if( (type & BookingData.TYPE_CANCELLED) != 0 ) {
//			whereClause += mergeGlue + BookingDbAdapter.KEY_TYPE + "=? ";
//			args.add( String.valueOf(BookingData.TYPE_CANCELLED));
//			mergeGlue = " OR ";
//		}
//
//
//
//		open();
//
//		String orderBy = KEY_DATE + " ASC";
//		Cursor cursor = mDb.query(DB_TABLE, mMapping, whereClause, (String[])args.toArray(new String[args.size()]), null, null, orderBy);
//
//		if( cursor != null ) {
//			while( cursor.moveToNext() ) {
//				BookingData b = new BookingData( cursor );
//				profiles.add( b );
//			}
//		}
//
//		close();
//
//		return profiles;
//	}


	public boolean update( BookingData booking ) {

		ContentValues values = new ContentValues();
		values.put( KEY_TYPE, booking.getType() );

		String whereClause = BookingDbAdapter.KEY_LOCAL_ID + "=?";
		String[] whereArgs = { String.valueOf( booking.getPk() ) };

		open();
		int affectedRows = mDb.update( BookingDbAdapter.DB_TABLE, values, whereClause, whereArgs);
		close();

		return (affectedRows == 1) ? true :  false;
	}


	public boolean remove( BookingData booking ) {
		return remove( booking.getPk() );
	}
	public boolean remove( String bookingPk ) {
		String whereClause = String.format( "%s=?", KEY_BOOKING_PK );
		String whereArgs[] = { String.valueOf( bookingPk ) };

		return removeByWhereClause(whereClause, whereArgs);
	}

	protected boolean removeByWhereClause( String whereClause, String[] whereArgs ) {
		open();
		boolean result = (mDb.delete( DB_TABLE, whereClause, whereArgs) > 0);
		close();

		return result;
	}

	public boolean removeAll() {
		return removeAllByType( null );
	}
	public boolean removeAllByType( Integer type ) {

		boolean result = false;

		open();

		if( type == null ) {
			result = (mDb.delete( DB_TABLE, null, null ) > 0);
		} else {
			String whereClause = String.format( "%s=?", KEY_TYPE );
			String[] whereArgs = { String.valueOf(type) };
			result = (mDb.delete( DB_TABLE, whereClause, whereArgs) > 0);
		}

		close();

		return result;
	}


} // end of class