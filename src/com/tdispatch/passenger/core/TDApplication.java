package com.tdispatch.passenger.core;

import java.util.Hashtable;
import java.util.Locale;

import org.json.JSONObject;

import android.app.Application;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.tdispatch.passenger.R;
import com.tdispatch.passenger.data.manager.BookingManager;
import com.tdispatch.passenger.data.manager.SessionManager;
import com.webnetmobile.tools.WebnetLog;

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
final public class TDApplication extends Application
{
	protected static TDApplication mAppContext;
	protected static SharedPreferences mPrefs;
	protected static NotificationManager mNotificationManager;
	protected static SessionManager mSessionManager;
	protected static BookingManager mBookingManager;
	protected static OfficeManager mOfficeManager;

	private static Boolean mIsDebuggable = false;

	@Override
	public void onCreate() {

		// Workaround for Android bug #20915
		// http://code.google.com/p/android/issues/detail?id=20915
		try {
			Class.forName("android.os.AsyncTask");
		} catch (Exception e) {
			e.printStackTrace();
		}

		super.onCreate();

		mAppContext = this;

		mIsDebuggable 			= ( ( getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE ) != 0 );

		mPrefs 					= PreferenceManager.getDefaultSharedPreferences( mAppContext );
		mNotificationManager 	= (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		mSessionManager			= SessionManager.getInstance(mAppContext );
		mBookingManager			= BookingManager.getInstance( mAppContext );
		mOfficeManager			= OfficeManager.getInstance(mAppContext);

		initPackageInfo();
		initEnvInfo();
		checkGoogleServices();
	}


	// **[ typefaces ]***************************************************************************************

	// we cache used typefaces to avoid memory leaks due to framework bug #9904:
	// http://code.google.com/p/android/issues/detail?id=9904
	protected final Hashtable<String, Typeface> mTypefaceCache = new Hashtable<String, Typeface>();

	public Typeface getTypeface( String assetPath ) {

		synchronized( mTypefaceCache ) {

			Typeface typeface = null;

			if( mTypefaceCache.containsKey(assetPath) == false ) {
				try {
					typeface = Typeface.createFromAsset( mAppContext.getAssets(), assetPath);
					mTypefaceCache.put(assetPath, typeface);
					return typeface;
				} catch (Exception e) {
					WebnetLog.e( "Could not get '" + assetPath + "': " + e.getMessage());
					return null;
				}
			} else {
				typeface = mTypefaceCache.get( assetPath );
			}

			return typeface;
		}
	}


	// **[ helpers ]*****************************************************************************************

	public static Boolean isDebuggable() {
		return mIsDebuggable;
	}

	public static TDApplication getAppContext() {
		return mAppContext;
	}

	public static NotificationManager getNotificationManager() {
		return mNotificationManager;
	}
	public static SharedPreferences getSharedPreferences() {
		return mPrefs;
	}
	public static SessionManager getSessionManager() {
		return mSessionManager;
	}
	public static BookingManager getBookingManager() {
		return mBookingManager;
	}
	public static OfficeManager getOfficeManager() {
		return mOfficeManager;
	}


	// **[ Google service checker ]************************************************************************

	// checks if proper version of google services is available so we can use i.e. new
	// Maps API https://developer.android.com/google/play-services/setup.html

	protected static int mGoogleServicesV2AvailableCheckCode = ConnectionResult.SERVICE_MISSING;

	protected void checkGoogleServices() {
		mGoogleServicesV2AvailableCheckCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable( mAppContext );

		switch( mGoogleServicesV2AvailableCheckCode ) {
			case ConnectionResult.SUCCESS:
				break;

			case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
			case ConnectionResult.SERVICE_MISSING:
			case ConnectionResult.SERVICE_DISABLED:
			case ConnectionResult.SERVICE_INVALID:
			default:
//				if( GooglePlayServicesUtil.isUserRecoverableError( errorCode ) ) {
//					GooglePlayServicesUtil.getErrorDialog(errorCode, null, requestCode)
//				}

				break;
		}
	}

	public static int getGoogleServicesCheckReturnCode() {
		return mGoogleServicesV2AvailableCheckCode;
	}
	public static Boolean isGoogleServiceAvailable() {
		return ( mGoogleServicesV2AvailableCheckCode == ConnectionResult.SUCCESS );
	}


	// **********[ APP VERSION STRING HELPER ]*****************************************

	protected static String mAppName = null;
	protected static String mAppVersion = null;
	protected static int mAppVersionCode = 0;
	protected static PackageInfo mPackageInfo = null;

	protected void initPackageInfo() {

		try {
			// read current version information about this package
			PackageManager manager = mAppContext.getPackageManager();
			mPackageInfo = manager.getPackageInfo(mAppContext.getPackageName(), 0);

			mAppVersion  	= "v" + mPackageInfo.versionName;
			mAppVersionCode = mPackageInfo.versionCode;
			mAppName = mPackageInfo.applicationInfo.name;
		} catch (Exception e) {
			WebnetLog.e("Failed to get packageInfo");
		}
	}

	public static PackageInfo getPackageInfo() {
		return mPackageInfo;
	}


	public static String getAppVersion() {
		return (mAppVersion==null) ? "N/A" : mAppVersion;
	}
	public static int getAppVersionCode() {
		return mAppVersionCode;
	}
	public static String getAppName() {
		return mAppName;
	}


	public static Boolean isTablet() {

		Boolean result = false;

		try {
			result = mAppContext.getResources().getBoolean( R.bool.sys_is_tablet );
		} catch ( Exception e ) {
			e.printStackTrace();
		}

		return result;
	}



	protected static JSONObject mEnvInfoJson = new JSONObject();
	@SuppressWarnings( "deprecation" )
	protected void initEnvInfo() {

		DisplayMetrics dm = getResources().getDisplayMetrics();

		String orientation = "???";
		switch( getResources().getConfiguration().orientation ) {
			case Configuration.ORIENTATION_LANDSCAPE:	orientation = "Landscape";	break;
			case Configuration.ORIENTATION_PORTRAIT:	orientation = "Portrait";	break;
			case Configuration.ORIENTATION_SQUARE:		orientation = "Square";		break;
			case Configuration.ORIENTATION_UNDEFINED:	orientation = "Undef";		break;
			default:									orientation = "Unknown";	break;
		}

		try {
			mEnvInfoJson.put("type", isTablet() ? "tablet" : "phone");
			mEnvInfoJson.put("build_manufacturer", Build.MANUFACTURER);
			mEnvInfoJson.put("build_model", Build.MODEL);
			mEnvInfoJson.put("build_board", Build.BOARD);
			mEnvInfoJson.put("build_device", Build.DEVICE);
			mEnvInfoJson.put("build_product", Build.PRODUCT);
			mEnvInfoJson.put("api", Build.VERSION.SDK_INT + " (" + Build.VERSION.RELEASE + ")");
			mEnvInfoJson.put("screen", dm.widthPixels + "x" + dm.heightPixels + " (" + dm.densityDpi + "DPI) " + orientation );

			mEnvInfoJson.put("locale", Locale.getDefault());
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	public static JSONObject getEnvInfoAsJson() {
		return mEnvInfoJson;
	}


} // end of class