package com.webnetmobile.tools;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.common.Const;
import com.tdispatch.passenger.core.TDApplication;

/*
 ******************************************************************************
 *
 * Copyright 2013 Webnet Marcin Orłowski
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
public final class WebnetTools
{
	protected static Typeface mFontRegular;
	protected static Typeface mFontBold;


	public static void setCustomFonts( TDApplication app, ViewGroup viewGroup ) {
		mFontRegular	= app.getTypeface( Const.Font.REGULAR );
		mFontBold		= app.getTypeface( Const.Font.BOLD );

		if( app.getResources().getDisplayMetrics().densityDpi >= DisplayMetrics.DENSITY_MEDIUM ) {
			setCustomFontsRaw( viewGroup );
		}
	}
	protected static void setCustomFontsRaw( ViewGroup viewGroup ) {
		for( int i = 0; i < viewGroup.getChildCount(); i++ ) {
			View view = viewGroup.getChildAt(i);

			if( view instanceof ViewGroup ) {
				setCustomFontsRaw( (ViewGroup)view );
			} else {
				Typeface tf 	= mFontRegular;
				Typeface oldTf;

				if( view instanceof TextView ) {
					oldTf = ((TextView)view).getTypeface();
					if( (oldTf != null ) && (oldTf.isBold() == true) ) {
						tf = mFontBold;
					}

					((TextView)view).setTypeface( tf );
				} else if( view instanceof EditText ) {
					oldTf = ((EditText)view).getTypeface();
					if( ((EditText)view).getTypeface().isBold() ) {
						tf = mFontBold;
					}

					((EditText)view).setTypeface( tf );
				} else if( view instanceof Button ) {
					oldTf = ((Button)view).getTypeface();
					if( ((Button)view).getTypeface().isBold() ) {
						tf = mFontBold;
					}

					((Button)view).setTypeface( tf );
				}
			}
		}
	}

	/**
	 * Helper to be used to change View visibility, referenced by Id (within parentView). It's safe to
	 * call this method even if parentView is null or parentView does not contain View referenced by itemId
	 * @param parentView View which shall hold target view referenced by itemId
	 * @param itemId R.id.XXX of view to look for
	 * @param visibility View visibility (View.GONE, View.VISIBLE, View.INVISIBLE)... to be applied
	 */
	public static void setVisibility( View parentView, int itemId, int visibility ) {
		if( (parentView != null) && (itemId != 0) ) {
			setVisibility( parentView.findViewById( itemId), visibility );
		}
	}

	public static void setVisibility( Activity activity, int itemId, int visibility ) {
		View view = activity.getWindow().getDecorView().getRootView();
		setVisibility(view, itemId, visibility);
	}
	/**
	 * Little helper to be used to change View visibility.
	 * @param view View to change visibility of. Can also be <code>null</code> which is the main purpose this method exists
	 * @param visibility View visibility (View.GONE, View.VISIBLE, View.INVISIBLE)...
	 */
	public static void setVisibility( View view, int visibility ) {
		if( view != null ) {
			view.setVisibility( visibility );
		} else {
			WebnetLog.d( String.format("No view found: 0x%X", view) );
		}
	}

	public static void setImageButtonImageResource( View view, int buttonId, int drawableId ) {
		if( view != null ) {
			ImageButton button = (ImageButton) view.findViewById( buttonId );
			if( button != null ) {
				button.setImageResource( drawableId );
			}
		}
	}

	public static void setText( View view, int textViewId, int textId ) {
		if( view != null ) {
			TextView tv = (TextView) view.findViewById( textViewId );
			if( tv != null ) {
				tv.setText( view.getContext().getString(textId) );
			}
		}
	}
	public static void setText( Activity activity, int itemId, int visibility ) {
		View view = activity.getWindow().getDecorView().getRootView();
		setText(view, itemId, visibility);
	}

	public static void setText( View view, int textViewId, String text ) {
		if( view != null ) {
			TextView tv = (TextView) view.findViewById( textViewId );
			if( tv != null ) {
				tv.setText( text );
			}
		}
	}
	public static void setText( Activity view, int textViewId, String text ) {
		if( view != null ) {
			TextView tv = (TextView) view.findViewById( textViewId );
			if( tv != null ) {
				tv.setText( text );
			}
		}
	}


	// returns number of days + hrs that millis represents
	// NOTE: even these are millis we usually would pass *diff* between given mills and now,
	// so in fact millis are since epoch. And this method is to show diff in more human
	// readable form, so pay attention too big value will return error!
	public static final long MILLIS_PER_SECOND	= 1000;
	public static final long MILLIS_PER_MINUTE	= (MILLIS_PER_SECOND * 60);
	public static final long MILLIS_PER_HOUR	= (MILLIS_PER_MINUTE * 60);
	public static final long MILLIS_PER_DAY		= (MILLIS_PER_HOUR * 24);
	public static final long MILLIS_PER_WEEK	= (MILLIS_PER_DAY * 7);
	public static final long MILLIS_PER_MONTH	= (MILLIS_PER_DAY * 30);		// FIXME hardcoded 30!
	// public static final long MILLIS_PER_YEAR	= (MILLIS_PER_MONTH * 12);		// FIXME accumulates error from MILLIS_PER_MONTH!
	public static final long MILLIS_PER_YEAR	= (MILLIS_PER_DAY * ((7*31) + (4*30) + 28));

	public static final String MD_SIGN			= "sign";
	public static final int    MD_SIGN_SMALLER	= -1;
	public static final int    MD_SIGN_EQUAL	=  0;
	public static final int    MD_SIGN_GREATER	= +1;


	public static final String MD_YEARS			= "years";
	public static final String MD_MONTHS		= "months";
	public static final String MD_MONTHS_TOTAL	= "months_total";
	public static final String MD_WEEKS			= "weeks";
	public static final String MD_WEEKS_TOTAL	= "weeks_total";
	public static final String MD_DAYS			= "days";
	public static final String MD_HOURS			= "hours";
	public static final String MD_HOURS_TOTAL	= "hours_total";
	public static final String MD_MINUTES		= "minutes";
	public static final String MD_MINUTES_TOTAL	= "minutes_total";
	public static final String MD_SECONDS		= "seconds";
	public static final String MD_SECONDS_TOTAL	= "seconds_total";


	/**
	 * Checks if there at least one Google account signed in on this device
	 * @param context
	 * @return boolean, <code>true</code> if there's account, <code>false</code> otherwise
	 */
	public static boolean hasGoogleAccount(Context context) {
		Boolean result = false;
		Account[] arrayOfAccount = AccountManager.get(context).getAccounts();
		for(Account account : arrayOfAccount) {
			if( account.type.equals("com.google")) {
				result = true;
				break;
			}
		}

		return result;
	}



	/**
	 * tells how many years, months, weeks, days, hours, minutes, seconds is between given Date object and current timestamp.
	 * returns sign as well
	 *
	 * @param int stamp1 milliseconds of 1st date
	 * @param int stamp2 milliseconds of 2nd date
	 * @return Bundle with result values
	 *
	 * MD_YEARS, MD_MONTHS, MD_WEEKS, MD_DAYS, MD_HOURS, MD_MINUTES, MD_SECONDS
	 * MD_MINUTES_TOTAL, MD_SECONDS_TOTAL, MD_WEEKS_TOTAL, MD_MONTHS_TOTAL
	 */
	public static WebnetToolsMillisDiffResult millisDiff( Date date ) {
		return millisDiff( date.getTime() );
	}
	/**
	 * tells how many years, months, weeks, days, hours, minutes, seconds is between given two Date objects. returns sign as well
	 *
	 * @param int stamp1 milliseconds of 1st date
	 * @param int stamp2 milliseconds of 2nd date
	 * @return Bundle with result values
	 *
	 * MD_YEARS, MD_MONTHS, MD_WEEKS, MD_DAYS, MD_HOURS, MD_MINUTES, MD_SECONDS
	 * MD_MINUTES_TOTAL, MD_SECONDS_TOTAL, MD_WEEKS_TOTAL, MD_MONTHS_TOTAL
	 */
	public static WebnetToolsMillisDiffResult millisDiff( Date date1, Date date2 ) {
		return millisDiff( date1.getTime(), date2.getTime() );
	}
	/**
	 * tells how many years, months, weeks, days, hours, minutes, seconds is between given milliseconds and current time stamp.
	 * returns sign as well
	 *
	 * @param int stamp1 milliseconds of 1st date
	 * @param int stamp2 milliseconds of 2nd date
	 * @return Bundle with result values
	 *
	 * MD_YEARS, MD_MONTHS, MD_WEEKS, MD_DAYS, MD_HOURS, MD_MINUTES, MD_SECONDS
	 * MD_MINUTES_TOTAL, MD_SECONDS_TOTAL, MD_WEEKS_TOTAL, MD_MONTHS_TOTAL
	 */
	public static WebnetToolsMillisDiffResult millisDiff( long stamp1 ) {
		return millisDiff( stamp1, System.currentTimeMillis() );
	}
	/**
	 * tells how many years, months, weeks, days, hours, minutes, seconds is between two given milliseconds. returns sign as well
	 *
	 * @param int stamp1 milliseconds of 1st date
	 * @param int stamp2 milliseconds of 2nd date
	 * @return Bundle with result values
	 *
	 * MD_YEARS, MD_MONTHS, MD_WEEKS, MD_DAYS, MD_HOURS, MD_MINUTES, MD_SECONDS
	 * MD_MINUTES_TOTAL, MD_SECONDS_TOTAL, MD_WEEKS_TOTAL, MD_MONTHS_TOTAL
	 */
	public static WebnetToolsMillisDiffResult millisDiff( long stamp1, long stamp2 ) {

		Bundle result = new Bundle();

		//  1 if stamp1 is bigger (older) than stamp2,
		//  0 if both are equal
		// -1 if stamp2 is bigger (older) than stamp1
		int _sign = MD_SIGN_EQUAL;
		if( stamp1 > stamp2 ) {
			_sign = MD_SIGN_GREATER;
		} else {
			if( stamp1 < stamp2 ) {
				_sign = MD_SIGN_SMALLER;
			}
		}
		result.putInt(MD_SIGN, _sign);

		// NOTE: as our "precission" ends at seconds we need to round millis up
		// to avoid "off by minute" problem that will raise due to dropped millis
		// i.e. 20minutes+some millis - 14minutes+some millis may give 15 minutes
		// difference result due to remaining, not countend millis.
		// long diffMillis	= Math.abs( stamp1 - stamp2 ) + ( MILLIS_PER_MINUTE / 2 ) ;

		long diffMillis	= Math.abs( stamp1 - stamp2 );


		long diff = diffMillis;
		long years = ( diff / MILLIS_PER_YEAR );

		diff = diff - ( years * MILLIS_PER_YEAR );
		long months = ( diff / MILLIS_PER_MONTH );

		diff = diff - ( months * MILLIS_PER_MONTH );
		long weeks = ( diff / MILLIS_PER_WEEK );

		diff = diff - ( weeks * MILLIS_PER_WEEK );
		long days 	= ( diff / MILLIS_PER_DAY );

		diff = diff - (days * MILLIS_PER_DAY);
		long hours	= ( diff / MILLIS_PER_HOUR );

		diff 		 = diff - (hours * MILLIS_PER_HOUR);
		long minutes = ( diff / MILLIS_PER_MINUTE );

		diff 		= diff - (minutes * MILLIS_PER_MINUTE);
		long seconds	= ( diff / MILLIS_PER_SECOND );

		result.putLong(MD_YEARS, years);
		result.putLong(MD_MONTHS, months);
		result.putLong(MD_WEEKS, weeks);
		result.putLong(MD_DAYS, days);
		result.putLong(MD_HOURS, hours);
		result.putLong(MD_MINUTES, minutes);
		result.putLong(MD_SECONDS, seconds);

		// calculating totals (LONG!)
		long minutesTotal = (days * 24 * 60) + (hours * 60) + minutes;
		long secondsTotal = ( minutesTotal * 60 );
		long weeksTotal = ( diffMillis / MILLIS_PER_WEEK );
		long monthsTotal = ( diffMillis / MILLIS_PER_MONTH );
		long hoursTotal = minutesTotal / 60;

		result.putLong(MD_MINUTES_TOTAL, minutesTotal);
		result.putLong(MD_SECONDS_TOTAL, secondsTotal);
		result.putLong(MD_WEEKS_TOTAL, weeksTotal);
		result.putLong(MD_MONTHS_TOTAL, monthsTotal);
		result.putLong(MD_HOURS_TOTAL, hoursTotal);

		// done
		return new WebnetToolsMillisDiffResult( result );
	}


    /**
     * returns string with human readable result of difference between given
     * timestamp and current timestamp
     *
     * NOTE: it does only shows time passed. Settings timestamps to be in future
     * does not affect the way result is build. No matter what time stamp is set
     * it is difference between these two that matters, not real location in time
     *
     * @param then date to compare to current stamp
     * @return String with result value (i.e. "3 mins")
     */
    public static String dateDiffToString( Date then ) {
    	return dateDiffToString( null, then );
    }
    /**
     * returns string with human readable result of difference between given
     * now and then.
     *
     * NOTE: it does only shows time passed. Settings timestamps to be in future
     * does not affect the way result is build. No matter what time stamp is set
     * it is difference between these two that matters, not real location in time
     *
     * @param now first date to compare stamps (current timestamp)
     * @param then second date to compare stamp
     * @return
     */
    public static String dateDiffToString( Date now, Date then ) {

    	Calendar calNow = Calendar.getInstance();
    	Calendar calThen = Calendar.getInstance();

    	long millisNow = 0;
    	long millisThen = 0;

    	if( now != null ) {
    		millisNow = now.getTime();
    		calNow.setTimeInMillis( millisNow );
    	} else {
    		millisNow = calNow.getTimeInMillis();
    	}
    	millisThen = then.getTime();
    	calThen.setTimeInMillis( millisThen );

    	if( millisNow < millisThen ) {
    		long tmp = millisNow;
    		millisNow = millisThen;
    		millisThen = tmp;
    	}

    	WebnetToolsMillisDiffResult	diff = millisDiff( millisNow, millisThen );

    	long diffMinutes 		= diff.getMinutes();
    	long diffHours	 		= diff.getHours();
    	long diffDays	 		= diff.getDays();
    	long diffWeeks 	 		= diff.getWeeks();
    	long diffMonths	 		= diff.getMonths();
    	long diffMonthsTotal 	= diff.getMonthsTotal();
    	long diffYears	 		= diff.getYears();

    	String result = "";
    	String suffix = " ago";
    	String separator = "";

    	// if something is longer than 12 months, we'd show years and months only
    	// as it is quite irrelevant to go into closer details
    	if( diffMonthsTotal >= 12 ) {

    		result += diffYears + " " + ((diffYears == 1) ? "year" : "years");
    		separator = " ";

        	if( diffMonths > 0 ) {
        		result += separator + diffMonths + " months";
        		separator = " ";
        	}

    	} else {
    		// it we are within 1 year time frame, let's' go into some closer details

    		// if something is older than 6 months, we'd show months and days
        	if( diffMonthsTotal >= 6 ) {

        		if( diffMonths > 0 ) {
        			result += separator + diffMonths + " months";
        			separator = " ";
        		}

        		if( diffDays > 0 ) {
        			result = separator + diffDays + " ";
        			if( diffDays == 1 ) {
        				result += "day";
        			} else {
        				result += "days";
        			}
        			separator = " ";
        		}

        	} else {

        		// for anything not older than 1 month (but not older than 6), we'd show
        		// months, weeks and days
        		if( diffMonthsTotal >= 1 ) {

	        		if( diffMonths > 0 ) {
	        			result += separator + diffMonths + " mo.";
	        			separator = " ";
	        		}

	        		if( diffWeeks > 0 ) {
	        			result += separator + diffWeeks + " weeks";
	        			separator = " ";
	        		}

	        		if( diffDays > 0 ) {
	        			result = separator + diffDays + " ";
	        			if( diffDays == 1 ) {
	        				result += "day";
	        			} else {
	        				result += "days";
	        			}
	        			separator = " ";
	        		}

        		} else {
        			// anything withing 1 month goes for more detailed stamp
        			// with weeks, days and hours
        			if( diffWeeks > 0 ) {
        				result += separator + diffWeeks + " weeks";
        				separator = " ";
        			}

        			if( diffDays > 0 ) {
        				result = separator + diffDays + " ";
        				if( diffDays == 1 ) {
        					result += "days";
        				} else {
        					result += "day";
        				}
        				separator = " ";
        			}

        			if( diffHours > 0 ) {
        				result += separator + diffHours + " hrs";
        			} else {
        				if( diffMinutes > 0 ) {
        					result += separator + diffMinutes + " mins";
        				} else {
        					result += separator + "moment ago";
        					suffix = "";
        				}
        			}
        		}
        	}
    	}

    	return (result + suffix);
    }


    /**
     * Formats provided date according to devices' current timezone
     * @param date
     * @return
     */
    public static String formatDate( Date date ) {
    	// 2013-03-07T13:26:04+00:00

    	SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, HH:mm");
    	sdf.setTimeZone(TimeZone.getDefault());
    	return sdf.format(date);
    }


	/**
	 * returns date (YYYY-MM-DD) based on given milliseconds since Epoch
	 *
	 * @param milliseconds since epoch to take date from
	 * @return String date in YYYY-MM-DD format
	 */
    public static String dateFromMillis( long milliseconds ) {
    	return formatMillis( "yyyy-MM-dd", milliseconds);
    }
    /**
     * returns time from given millis. If i.e. millis is dated 1920-05-27 12:33, "12:33" would be returned
     *
     * @param milliseconds milliseconds since epoch to take time from
     * @return String
     */
    public static String timeFromMillis( long milliseconds ) {
    	return formatMillis( "HH:mm", milliseconds);
    }
    public static String stampFromMillis( long milliseconds ) {
    	return formatMillis( "yyyy-MM-dd HH:mm", milliseconds);
    }
    public static String formatMillis( String format, long milliseconds ) {
    	Date date = new java.util.Date( milliseconds );
		SimpleDateFormat formatter = new SimpleDateFormat( "", Locale.ENGLISH );
        formatter.applyPattern( format );
        return formatter.format( date );
    }



	public static boolean isAnyLocationEnabled(Context context) {
		LocationManager mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		boolean isGpsEnabled = false;
		boolean isNetworkEnabled = false;

		isGpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		return isGpsEnabled || isNetworkEnabled;
	}



	public static <P, T extends AsyncTask<P, ?, ?>> void executeAsyncTask(T task) {
		executeAsyncTask(task, (P[]) null);
	}

	@SuppressLint("NewApi")
	public static <P, T extends AsyncTask<P, ?, ?>> void executeAsyncTask(T task, P... params) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		} else {
			task.execute(params);
		}
	}


    // Toast helpers

	// displays custom toast. Needs toast.xml layout + some styles (style_toast.xml)
	public static void showToast( Context context, int messageId ) {
		showToast( context, context.getResources().getString( messageId ) );
	}
	public static void showToast( Context context, String message ) {
		showToast( context, message, Toast.LENGTH_SHORT, R.layout.toast );
	}
	public static void showToast( Context context, String message, int length, int toastLayoutId ) {

		// we use custom layout for Toasts
		LayoutInflater inflater = LayoutInflater.from(context);
		View layout = inflater.inflate( toastLayoutId, null);

		TextView text = (TextView) layout.findViewById(R.id.toast_body);
		if( text != null ) {
			text.setText( message );
		} else {
			WebnetLog.e("Failed to find toast_body!");
		}

		Toast toast = new Toast( context );
		toast.setDuration(length);
		toast.setView(layout);
		toast.show();
	}

	public static void showErrorToast( Context context, int messageId ) {
		showErrorToast( context, context.getResources().getString( messageId ) );
	}
	public static void showErrorToast( Context context, String message ) {
		showToast( context, message, Toast.LENGTH_LONG, R.layout.toast_error );
	}

	public static void showOkToast( Context context, int messageId ) {
		showOkToast( context, context.getResources().getString( messageId ) );
	}
	public static void showOkToast( Context context, String message ) {
		showToast( context, message, Toast.LENGTH_SHORT, R.layout.toast_ok );
	}



	// Method to launch Settings
	public static void showSystemLocationSettings(Context context) {

		Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		try {
			context.startActivity(settingsIntent);
		} catch( Exception e ) {
			e.printStackTrace();
		}

	}


	public static Boolean useMetricUnits() {

		Boolean result = true;

		String countryCode = Locale.getDefault().getCountry();

		if("US".equals(countryCode)) { result = false; }	// USA
		if("UK".equals(countryCode)) { result = false; }	// UK
		if("LR".equals(countryCode)) { result = false; }	// Liberia
		if("MM".equals(countryCode)) { result = false; }	// Birma

		return result;
	}




	// end of class
}