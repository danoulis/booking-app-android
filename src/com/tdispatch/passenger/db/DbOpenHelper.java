package com.tdispatch.passenger.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
public class DbOpenHelper extends SQLiteOpenHelper
{
	private static final String DATABASE_NAME = "passenger";
	private static final int DATABASE_VERSION = 1;

	private static DbOpenHelper mSystemDbOpenHelper = null;

	public static DbOpenHelper getInstance( TDApplication context ) {
		if( mSystemDbOpenHelper == null ) {
			mSystemDbOpenHelper = new DbOpenHelper(context);
		}

		return mSystemDbOpenHelper;
	}

	public DbOpenHelper(TDApplication context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}


	@Override
	public void onCreate( SQLiteDatabase db ) {
		createAllTables(db);
	}

	@Override
	public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {
		// no need for upgrade now
	}


	public void createAllTables( SQLiteDatabase db ) {
//		BalanceDbAdapter.createTable(db);
	}

	public void deleteAllTables(SQLiteDatabase db) {
//		BalanceDbAdapter.deleteTable(db);
	}


	public void recreateAllTables() {
		SQLiteDatabase db = getWritableDatabase();

		deleteAllTables(db);
		createAllTables(db);

		db.close();
	}
}
