package com.tdispatch.passenger.core;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.fragment.dialog.GenericDialogFragment;
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
abstract public class TDFragment extends android.support.v4.app.Fragment
{
	abstract protected int getLayoutId();

	protected void onPostCreateView() {
		// dummy by default
	}

	protected Integer getOverlayBackgroundResourceId() {
		return null;
	}

	protected Boolean isBusyOverlayPresent() {
		return true;
	}

	// ********************** PRIVATE CLASS MEMBERS **********************************************

	/**
	 * {@link Context} to be used within fragment. Please note we set this (in
	 * onAttach()) to point to our Application's Context, not parent Activity
	 * context. If you need parent Activity context, please use mParentActivity
	 * instead
	 */
	protected Context mContext;
	protected TDApplication mApp;
	/**
	 * Our view (this is view we got inflated from our layout XML in
	 * onCreateView() method)
	 */
	protected View mFragmentView;
	/**
	 * Initialized {@link SharedPreferences} object, ready to be used in this
	 * fragment
	 */
	protected SharedPreferences mPrefs;

	/**
	 * reference to parent {@link Activity} (set in {@link
	 * android.app.Fragment.onAttach()})
	 */
	protected TDActivity mParentActivity;
	protected static TDFragment mMe;

	protected FragmentManager mFragmentManager;



	// *********************** FRAGMENT LIFE CYCLE **********************************************

	@Override
	public void onAttach( Activity activity ) {

		super.onAttach(activity);

		mParentActivity = (TDActivity)activity;
		mContext = mParentActivity.getApplicationContext();
		mApp = (TDApplication)mParentActivity.getApplicationContext();
		mFragmentManager = mParentActivity.getSupportFragmentManager();
		mMe = this;

		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
	}

	@Override
	public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

		if( isBusyOverlayPresent() ) {
			ViewGroup baseLayout = (ViewGroup)inflater.inflate( R.layout.tdfragment, container, false );
			ViewGroup contentContainer = (ViewGroup)baseLayout.findViewById(R.id.tdfragment_content_container);
			inflater.inflate( getLayoutId(), contentContainer );

			mFragmentView = baseLayout;
		} else {
			mFragmentView = (ViewGroup)inflater.inflate( getLayoutId(), container, false );
		}

		lockUI(false);
		WebnetTools.setCustomFonts(mApp, (ViewGroup)mFragmentView);
		onPostCreateView();

		return mFragmentView;
	}

	protected void lockUI( Boolean lockUI ) {
		View v = mFragmentView.findViewById( R.id.tdfragment_busy_overlay_container );

		if( v != null ) {

			AnimationDrawable busyAnim = (AnimationDrawable)((ImageView)mFragmentView.findViewById(R.id.tdfragment_busy)).getBackground();

			if( lockUI ) {
//				// hide soft menu
//				InputMethodManager imm = (InputMethodManager)mContext.getSystemService( Context.INPUT_METHOD_SERVICE);
//				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

				if( getOverlayBackgroundResourceId() != null ) {
					v.setBackgroundResource( getOverlayBackgroundResourceId() );
				}

				busyAnim.start();
				v.setVisibility(View.VISIBLE);
			} else {
				v.setVisibility(View.GONE);
				busyAnim.stop();
			}
		} else {
			WebnetLog.d("No lockUI overlay");
		}
	}



	/**[ dialog helpers ]********************************************************************************************/

	protected void showDialog( int type, int titleId, int messageId ) {
		showDialog( type, getString(titleId), getString(messageId));
	}
	protected void showDialog( int type, int titleId, int messageId, int buttonId ) {
		showDialog( type, getString(titleId), getString(messageId), getString(buttonId));
	}
	protected void showDialog( int type, String title, String message ) {
		showDialog(type, title, message, null);
	}
	protected void showDialog( int type, String title, String message, String button ) {
		GenericDialogFragment frag = GenericDialogFragment.newInstance( type, title, message );
		frag.setTargetFragment(mMe, 0);
		frag.show(((FragmentActivity)mParentActivity).getSupportFragmentManager(), "genericdialog");
	}



	// end of class
}
