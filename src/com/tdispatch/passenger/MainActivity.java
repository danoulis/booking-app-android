package com.tdispatch.passenger;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.slidingmenu.lib.SlidingMenu;
import com.tdispatch.passenger.common.Const;
import com.tdispatch.passenger.core.TDActivity;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.fragment.BookingListFragment;
import com.tdispatch.passenger.fragment.ControlCenterFragment;
import com.tdispatch.passenger.host.BookingListHostInterface;
import com.tdispatch.passenger.host.MapHostInterface;
import com.tdispatch.passenger.host.SlideMenuHostInterface;
import com.tdispatch.passenger.model.BookingData;
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
public class MainActivity extends TDActivity implements MapHostInterface, BookingListHostInterface, SlideMenuHostInterface
{
	protected SlidingMenu mSlidingMenu;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {

		super.onCreate(savedInstanceState);

		if( TDApplication.isTablet() == false ) {

			setContentView( R.layout.main_activity );

			setFragment( new ControlCenterFragment(), false );

			mSlidingMenu = new SlidingMenu( mContext );
			mSlidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
			mSlidingMenu.setFadeDegree(0.35f);
			mSlidingMenu.setBehindOffset( getResources().getDimensionPixelSize( R.dimen.mapViewDragHandleWidth ) );

			mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
			mSlidingMenu.setMenu(R.layout.slide_menu_left);

			mSlidingMenu.setSecondaryMenu(R.layout.slide_menu_right);
		} else {
			setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE );

			setContentView( R.layout.main_activity_tablet );

			setFragment( new ControlCenterFragment(), false );

			mSlidingMenu = new SlidingMenu( mContext );
			mSlidingMenu.setMode(SlidingMenu.LEFT);
			mSlidingMenu.setFadeDegree(0.35f);
			mSlidingMenu.setBehindOffset( getResources().getDimensionPixelSize( R.dimen.mapViewDragHandleWidth ) );
			mSlidingMenu.setBehindWidth( getResources().getDimensionPixelSize( R.dimen.mapViewMenuWidthTablet ));

			mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
			mSlidingMenu.setMenu(R.layout.slide_menu_left);

			setFragment(new BookingListFragment(), false, R.id.booking_list_fragment, Const.Tag.BOOKING_LIST_FRAGMENT );
		}

		setCustomFonts();

		int ids[] = { 	R.id.left_menu_drag_handle, R.id.right_menu_drag_handle };
		for( int id : ids ) {
			View v = findViewById(id);
			if( v != null ) {
				v.setOnClickListener(mMenuClickListener);
			}
		}
	}


	protected OnClickListener mMenuClickListener = new OnClickListener()
	{
		@Override
		public void onClick( View v ) {

			switch( v.getId() ) {
				case R.id.left_menu_drag_handle: {
					mSlidingMenu.showMenu();
				}
				break;

				case R.id.right_menu_drag_handle: {
					mSlidingMenu.showSecondaryMenu();
				}
				break;
			}
		}
	};

	/**[ back key handling ]*********************************************************************************************/

	@Override
	public void onBackPressed()
	{
		Boolean handled = false;

		if( mSlidingMenu.isMenuShowing() ) {
			if( mFragmentManager.getBackStackEntryCount() == 0 ) {
				mSlidingMenu.showContent();
				handled = true;
			}

		} else {
			if( mSlidingMenu.isSecondaryMenuShowing() ) {
				mSlidingMenu.showContent();
				handled = true;
			}
		}

		if( !handled ) {
			super.onBackPressed();
		}
	  }

	/**[ booking list host interface ]***********************************************************************************/


	@Override
	public void addBooking( BookingData b ) {
		BookingListFragment frag = (BookingListFragment)mFragmentManager.findFragmentById( R.id.booking_list_fragment );
		if( frag != null ) {
			frag.addBooking(b);
		}
	}


	/**[ slide menu host interface ]*************************************************************************************/

	@Override
	public void showLeftMenu() {
		mSlidingMenu.showMenu();
	}
	@Override
	public void showRightMenu() {
		mSlidingMenu.showSecondaryMenu();
	}

	@Override
	public void hideDragHandles() {
		WebnetTools.setVisibility(this, R.id.left_menu_drag_handle, View.GONE);
		WebnetTools.setVisibility(this, R.id.right_menu_drag_handle, View.GONE);
	}

	@Override
	public void showDragHandles() {
		WebnetTools.setVisibility(this, R.id.left_menu_drag_handle, View.VISIBLE);
		WebnetTools.setVisibility(this, R.id.right_menu_drag_handle, View.VISIBLE);
	}

	/**[ map host interface ]********************************************************************************************/


	@Override
	public void setPickupLocation( LocationData pickup ) {
		setLocation( pickup, null );
	}
	@Override
	public void setDropoffLocation( LocationData dropoff ) {
		setLocation( null, dropoff );
	}

	@Override
	public void setLocation( LocationData pickup, LocationData dropoff ) {

		if( (pickup != null) || (dropoff != null) ) {
			ControlCenterFragment cc = (ControlCenterFragment)mFragmentManager.findFragmentById( R.id.fragment_container );

			if( pickup != null ) {
				cc.setPickupAddress( pickup );
				cc.moveMapToLocation( pickup, (TDApplication.isTablet() == false) );
			}

			if( dropoff != null ) {
				cc.setDropoffAddress( dropoff );
				if( pickup == null ) {
					cc.moveMapToLocation( dropoff, (TDApplication.isTablet() == false) );
				}
			}

			cc.updateAddresses();
			mSlidingMenu.showContent();
		}
	}


	@Override
	public void moveMapToLocation( LocationData loc ) {
		ControlCenterFragment cc = (ControlCenterFragment)mFragmentManager.findFragmentById( R.id.fragment_container );
		if( cc != null ) {
			cc.moveMapToLocation( loc );
			mSlidingMenu.showContent();
		}
	}



} // end of class