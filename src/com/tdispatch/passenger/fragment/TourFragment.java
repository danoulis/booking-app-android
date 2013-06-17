package com.tdispatch.passenger.fragment;

import uk.co.jasonfry.android.tools.ui.PageControl;
import uk.co.jasonfry.android.tools.ui.SwipeView;
import android.app.Activity;
import android.view.View;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.core.TDFragment;
import com.tdispatch.passenger.host.TourHostInterface;

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
public class TourFragment extends TDFragment
{
	@Override
	protected int getLayoutId() {

		int layoutId = R.layout.tour_fragment;

		if( TDApplication.isTablet() ) {
			layoutId = R.layout.tour_fragment_tablet;
		}
		return layoutId;
	}

	@Override
	protected void onPostCreateView() {

		SwipeView swipeView = (SwipeView)mFragmentView.findViewById(R.id.swipe_view);
		swipeView.setPageControl((PageControl)mFragmentView.findViewById(R.id.page_control));

		int ids[] = { R.id.button_ok };
		for( int id : ids ) {
			View v = mFragmentView.findViewById(id);
			v.setOnClickListener( mOnClickListener );
		}
	}

	protected TourHostInterface mHostActivity;
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mHostActivity = (TourHostInterface)activity;
		} catch( ClassCastException e ) {
			throw new ClassCastException("Host Activity needs to implement TourHostInterface");
		}
	}

	protected View.OnClickListener mOnClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick( View v ) {

			switch( v.getId() ) {

				case R.id.button_ok: {
					mHostActivity.tourCompleted();
				}
				break;
			}

		}
	};


}	// end of class
