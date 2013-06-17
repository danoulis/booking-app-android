package com.webnetmobile.tools;

import android.os.Bundle;

public class WebnetToolsMillisDiffResult {

	Bundle mBundle;

	public WebnetToolsMillisDiffResult( Bundle bundle ) {
		mBundle = bundle;
	}

	public int getSign()			{ return mBundle.getInt(WebnetTools.MD_SIGN); }
	public long getYears() 			{ return mBundle.getLong(WebnetTools.MD_YEARS); }
	public long getMonths()			{ return mBundle.getLong(WebnetTools.MD_MONTHS); }
	public long getMonthsTotal()	{ return mBundle.getLong(WebnetTools.MD_MONTHS_TOTAL);}
	public long getWeeks()			{ return mBundle.getLong(WebnetTools.MD_WEEKS);}
	public long getWeeksTotal()		{ return mBundle.getLong(WebnetTools.MD_WEEKS_TOTAL); }
	public long getDays()			{ return mBundle.getLong(WebnetTools.MD_DAYS); }
	public long getHours()			{ return mBundle.getLong(WebnetTools.MD_HOURS); }
	public long getHoursTotal()		{ return mBundle.getLong(WebnetTools.MD_HOURS_TOTAL); }
	public long getMinutes()		{ return mBundle.getLong(WebnetTools.MD_MINUTES); }
	public long getMinutesTotal()	{ return mBundle.getLong(WebnetTools.MD_MINUTES_TOTAL); }
	public long getSeconds()		{ return mBundle.getLong(WebnetTools.MD_SECONDS); }
	public long getSecondsTotal()	{ return mBundle.getLong(WebnetTools.MD_SECONDS_TOTAL); }

}	// end of class
