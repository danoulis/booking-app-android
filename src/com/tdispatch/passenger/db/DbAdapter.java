package com.tdispatch.passenger.db;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.tdispatch.passenger.core.TDApplication;

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
public abstract class DbAdapter
{
	abstract public String getTableName();

	public static final String DB_TABLE = null;
	public static final String KEY_LOCAL_ID = "_id";

	protected TDApplication mContext;
	protected SQLiteDatabase mDb;
	protected DbOpenHelper mDbHelper;

	public DbAdapter( TDApplication context ) {
		mContext = context;
	}

	protected int openCount = 0;
	protected DbAdapter open() throws SQLException {
		if( openCount == 0 ) {
			if( mDb == null ) {
				mDbHelper = DbOpenHelper.getInstance( mContext );
				mDb = mDbHelper.getWritableDatabase();
			}
		}

		openCount++;

		return this;
	}

	protected void close() {

		if( openCount == 0 ) {
			if( mDb != null ) {
				mDb.close();
				mDb = null;
			}
			if( mDbHelper != null ) {
				mDbHelper.close();
				mDbHelper = null;
			}
		}

		if( openCount > 0 ) {
			openCount--;
		}
	}


	public int delete(long id) {
		open();
		int count = mDb.delete(getTableName(), KEY_LOCAL_ID + "=?", new String[] { String.valueOf(id) } );
		close();
		return count;
	}

	public void deleteAll() {
		open();

		mDb.delete(getTableName(), null, null);

		close();
	}


}
