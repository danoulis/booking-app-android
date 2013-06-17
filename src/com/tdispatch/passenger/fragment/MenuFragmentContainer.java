package com.tdispatch.passenger.fragment;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.StartActivity;
import com.tdispatch.passenger.common.Const;
import com.tdispatch.passenger.core.TDFragment;
import com.webnetmobile.tools.Redirector;

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
public class MenuFragmentContainer extends TDFragment
{
	@Override
	protected int getLayoutId() {
		return R.layout.menu_fragment_container;
	}

	@Override
	protected void onPostCreateView() {
		setFragment( new MenuFragment(), false );
	}

	public void showDriverProfile() {
		setFragment( new ProfileFragment() );
	}
	public void showCabOfficeInfo() {
		setFragment( new OfficeFragment() );
	}
	public void showTour() {
		Intent intent = new Intent();
		intent.putExtra(Const.Bundle.MODE, StartActivity.MODE_TOUR);
		Redirector.showActivity(mContext, StartActivity.class, intent);
	}


	protected void setFragment( Fragment fragment ) {
		setFragment( fragment, true );
	}
	protected void setFragment( Fragment fragment, Boolean addToBackStack ) {

		fragment.setTargetFragment(this, 0);

		FragmentTransaction ft = mFragmentManager.beginTransaction();
			if( addToBackStack ) {
				ft.setCustomAnimations(R.anim.menu_enter, R.anim.menu_exit, R.anim.menu_pop_enter, R.anim.menu_pop_exit);
			}

			ft.replace( R.id.menu_fragment_content_container, fragment, Const.Tag.MENU_FRAGMENT_CONTENT );

			if( addToBackStack ) {
				ft.addToBackStack(null);
			}
		ft.commit();

	}

// end of class
}
