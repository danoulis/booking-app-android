package com.tdispatch.passenger.fragment;

import android.view.View;
import android.view.View.OnClickListener;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.StartActivity;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.core.TDFragment;
import com.webnetmobile.tools.Redirector;
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
public class MenuFragment extends TDFragment
{
	@Override
	protected int getLayoutId() {
		return R.layout.menu_fragment;
	}

	@Override
	protected void onPostCreateView() {

		WebnetTools.setText(mFragmentView, R.id.version, TDApplication.getAppVersion() );

		int ids[] = { 	R.id.button_account, R.id.button_cab_office, R.id.button_logout, R.id.button_tour,
						R.id.left_menu_drag_handle, R.id.right_menu_drag_handle };
		for( int id : ids ) {
			View v = mFragmentView.findViewById(id);
			if( v != null ) {
				v.setOnClickListener(mMenuClickListener);
			}
		}

	}

	protected OnClickListener mMenuClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick( View v ) {

			switch( v.getId() ) {

				case R.id.button_account: {
					((MenuFragmentContainer)getTargetFragment()).showDriverProfile();
				}
				break;

				case R.id.button_cab_office: {
					((MenuFragmentContainer)getTargetFragment()).showCabOfficeInfo();
				}
				break;

				case R.id.button_tour: {
					((MenuFragmentContainer)getTargetFragment()).showTour();
				}
				break;

				case R.id.button_logout: {
					TDApplication.getSessionManager().doLogout();
					Redirector.showActivity(mContext, StartActivity.class);
					getActivity().finish();
				}
				break;
			}
		}
	};

// end of class
}
