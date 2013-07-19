package com.tdispatch.passenger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.tdispatch.passenger.common.Const;
import com.tdispatch.passenger.core.TDActivity;
import com.tdispatch.passenger.fragment.SearchAddressFragment;
import com.tdispatch.passenger.fragment.SearchStationsFragment;
import com.tdispatch.passenger.host.AddressSearchHostInterface;
import com.tdispatch.passenger.host.AddressSearchModuleInterface;
import com.tdispatch.passenger.model.LocationData;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TitlePageIndicator;
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
public class SearchActivity extends TDActivity implements AddressSearchHostInterface
{
	public static final int TYPE_UNKNOWN 		= 0;
	public static final int TYPE_PICKUP 		= 1;
	public static final int TYPE_DROPOFF 		= 2;


	protected static final String KEY_PAGE = "page";

	protected PageFragmentAdapter mAdapter;
	protected ViewPager mPager;
	protected PageIndicator mIndicator;

	@Override
	public void onCreate( Bundle savedInstanceState ) {

		super.onCreate(savedInstanceState);

		setContentView( R.layout.search_activity );

		initModules();

		mAdapter = new PageFragmentAdapter(mFragmentManager);

		mPager = (ViewPager)findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);

		mIndicator = (TitlePageIndicator)findViewById(R.id.indicator);
		mIndicator.setViewPager(mPager);
		mIndicator.setCurrentItem(mAdapter.getCount() - 1);

	    mIndicator.setOnPageChangeListener( mOnPageChangeListener );

		setCustomFonts();
	}


	@Override
	public void doSearchOk( int type, LocationData location ) {

		// build the result bundle
		Intent intent = new Intent();
		intent.putExtra( Const.Bundle.TYPE, type );
		intent.putExtra( Const.Bundle.LOCATION, location );

		setResult( Activity.RESULT_OK, intent );
		finish();
	}

	@Override
	public void doSearchCancel() {
		setResult( Activity.RESULT_CANCELED );
		finish();
	}


	@Override
	public void onPause() {
		super.onPause();

		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putInt(KEY_PAGE, mPager.getCurrentItem());
		editor.commit();
	}

	@Override
	public void onResume() {
		super.onResume();

		mIndicator.setCurrentItem( mPrefs.getInt(KEY_PAGE, 0));
	}


	protected String[] mPageTitles;
	protected void initModules() {

		if( getResources().getBoolean(R.bool.caboffice_settings_enable_location_search_modules) ) {
			mPageTitles = new String[] {
					getString(R.string.address_search_page_search),
					getString(R.string.address_search_page_stations)
				};
		} else {
			mPageTitles = new String[] {
					getString(R.string.address_search_page_search)
				};

			WebnetTools.setVisibility( this, R.id.indicator, View.GONE);
		}

	};

	protected class PageFragmentAdapter extends FragmentPagerAdapter
	{

		public PageFragmentAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem( int position ) {

			AddressSearchModuleInterface frag = null;

			switch( position ) {
				case 0: {
					frag = new SearchAddressFragment();
				}
				break;

				case 1: {
					frag = new SearchStationsFragment();
				}
				break;
			}

			if( frag != null ) {
				((Fragment)frag).setArguments( getIntent().getExtras() );
			}
			return (Fragment)frag;
		}

		@Override
		public int getCount() {
			return mPageTitles.length;
		}

		@Override
		public CharSequence getPageTitle( int position ) {
			return mPageTitles[position];
		}
	}


	protected int mLastPosition = -1;
	protected ViewPager.OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener()
	{
		@Override
		public void onPageSelected(int position) {
			if( mLastPosition != -1 ) {
				((AddressSearchModuleInterface)mAdapter.getItem(mLastPosition)).doLeavePage();
			}
			((AddressSearchModuleInterface)mAdapter.getItem(position)).doEnterPage();
			mLastPosition = position;

			View v = getWindow().getDecorView().findViewById(android.R.id.content);
			InputMethodManager imm = (InputMethodManager)mContext.getSystemService( Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			// dummy
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			// dummy
		}

	};


} // end of class
