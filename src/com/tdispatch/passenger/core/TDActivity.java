package com.tdispatch.passenger.core;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import com.crittercism.app.Crittercism;
import com.tdispatch.passenger.R;
import com.tdispatch.passenger.common.Const;
import com.tdispatch.passenger.host.CommonHostInterface;
import com.tdispatch.passenger.host.RedirectorInterface;
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
public abstract class TDActivity extends android.support.v4.app.FragmentActivity implements RedirectorInterface, CommonHostInterface
{
	protected SharedPreferences mPrefs;

	protected Context mContext;
	protected TDActivity mMe;
	protected TDApplication mApp;
	protected FragmentManager mFragmentManager = null;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {

		super.onCreate(savedInstanceState);

		String critAppId = getString(R.string.crittercism_app_id);
		if( (critAppId != null) && (critAppId.length() == 24) ) {
			JSONObject crittercismConfig = new JSONObject();
			try {
				crittercismConfig.put("delaySendingAppLoad", false);
				crittercismConfig.put("shouldCollectLogcat", true);
				crittercismConfig.put("includeVersionCode", true);
				crittercismConfig.put("customVersionName", TDApplication.getAppVersion() + " (" + TDApplication.getAppVersionCode() + ")" );
			} catch (JSONException e) {
				e.printStackTrace();
			}

			Crittercism.init(getApplicationContext(), critAppId, crittercismConfig);
			Crittercism.setMetadata( TDApplication.getEnvInfoAsJson() );
		}

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		mApp = TDApplication.getAppContext();
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mApp);
		mFragmentManager = getSupportFragmentManager();

		mContext = this;
		mMe = this;
	}



	/**[ ui lock ]***********************************************************************************************************/

	protected Boolean useContainerWithLockUIOverlay() {
		return true;
	}

	@Override
	public void setContentView( int layoutId ) {

		if( useContainerWithLockUIOverlay() == true ) {
			LayoutInflater inflater = getLayoutInflater();

			ViewGroup activityLayout = (ViewGroup)inflater.inflate( R.layout.tdactivity, null );
			ViewGroup contentContainer = (ViewGroup)activityLayout.findViewById(R.id.tdactivity_content_container);

			inflater.inflate( layoutId, contentContainer );

			super.setContentView( activityLayout );

			doHandleUiLock(0);
		} else {
			super.setContentView( layoutId );
		}

		// set custom fonts
		setCustomFonts();
	}


	/**[ common host interface ]*********************************************************************************************/

	@Override
	public void lockUI() {
		doHandleUiLock(+1);
	}
	@Override
	public void unlockUI() {
		doHandleUiLock(-1);
	}

	protected int mUiLockCount = 0;
	protected void doHandleUiLock(int step) {

		View v = findViewById( R.id.tdactivity_busy_overlay_container );
		if( v != null ) {
			mUiLockCount += step;
			if( (mUiLockCount > 0) ) {
				InputMethodManager imm = (InputMethodManager)getSystemService( Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			}
			v.setVisibility( (mUiLockCount > 0 ) ? View.VISIBLE : View.GONE);
		}

	}


	/**[ custom fonts ]******************************************************************************************************/

	protected void setCustomFonts() {
		setCustomFonts((ViewGroup)(((ViewGroup)findViewById(android.R.id.content)).getChildAt(0)));
	}

	protected void setCustomFonts( ViewGroup viewGroup ) {
		WebnetTools.setCustomFonts(mApp, viewGroup);
	}




	// Redirector Interface - FIXME should not be here...
	@Override
	public void showLogin() {
		WebnetLog.e("Not implemented!");
	}

	@Override
	public void showRegister() {
		WebnetLog.e("Not implemented!");
	}

	@Override
	public void showBooking() {
		WebnetLog.e("Not implemented!");
	}

	@Override
	public void showStart() {
		WebnetLog.e("Not implemented!");
	}

	@Override
	public void showOAuth() {
		WebnetLog.e("Not implemented!");
	}



	protected void setFragment( TDFragment fragment ) {
		setFragment( fragment, true );
	}
	protected void setFragment( TDFragment fragment, Boolean addToBackStack ) {
		setFragment(fragment, addToBackStack, R.id.fragment_container, Const.Tag.FRAGMENT);
	}

	protected void setFragment( TDFragment fragment, Boolean addToBackStack, int fragmentContainer, String tag ) {

		FragmentTransaction ft = mFragmentManager.beginTransaction();

		ft.replace( fragmentContainer, fragment, tag );

		if( addToBackStack ) {
			ft.addToBackStack(null);
		}

		ft.commit();

		// flush all pending actions. We do not want to wait for async task who knows how long...
		mFragmentManager.executePendingTransactions();
	}


} // end of class
