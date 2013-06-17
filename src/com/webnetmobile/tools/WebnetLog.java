package com.webnetmobile.tools;

import android.util.Log;

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
final public class WebnetLog
{
	// FIXME - we shall make this class abstract and put TAG in extending class!
	static protected final String TAG = "Passenger";

	static protected final Boolean ENABLED = TDApplication.isDebuggable();

	static protected final Boolean ENABLE_I = ( ENABLED == false ) ? false : true;
	static protected final Boolean ENABLE_W = ( ENABLED == false ) ? false : true;
	static protected final Boolean ENABLE_E = ( ENABLED == false ) ? false : true;
	static protected final Boolean ENABLE_D = ( ENABLED == false ) ? false : true;

	// NOTE: it's important to understand what we got on stack and NOT
	// call this method directly or the output will be WRONG!
	//
	// 0: getCallerTrace() (this)
	// 1: formatMessage()
	// 2: one of local calling methods (i.e. WebnetLog.i())
	// 3: caller class and method
	//
	// FIXME we could work that out by analyzing stack and skipping all invocation from
	// this class but as for now it's not done, so beware!

	protected static String getCallerTrace() {
		return getCallerTrace(4);
	}
	protected static String getCallerTrace(int depth) {
		Throwable throwable = new Throwable();
		StackTraceElement[] stackTrace = throwable.getStackTrace();

		String callerMethod	= stackTrace[depth].getMethodName();
		String callerClass	= stackTrace[depth].getClassName();
		int lineNumber 		= stackTrace[depth].getLineNumber();

		String[] nameParts = callerClass.split("\\.");
		callerClass = nameParts[ nameParts.length - 1 ];

//		Log.i("TA", callerMethod: " + callerMethod + ", callerClass: " + callerClass );

		return( callerClass + "/" + callerMethod + "()[+" + lineNumber + "]" );
	}


	protected static String formatMessage() {
		return( WebnetLog.getCallerTrace() );
	}
	protected static String formatMessage( String message ) {
		return( WebnetLog.getCallerTrace() + ": " + message );
	}

	protected static String formatMessage( String tag, String message ) {
		return( tag + ": " + WebnetLog.getCallerTrace() + ": " + message );
	}


	public static void i() {
		_i( WebnetLog.TAG, WebnetLog.formatMessage() );
	}
	public static void i( int message ) {
		i( "(int): " + message );
	}

	public static void i( String message ) {
		_i( WebnetLog.TAG, WebnetLog.formatMessage(message) );
	}
	public static void i( String tag, String message ) {
		Log.i( WebnetLog.TAG, WebnetLog.formatMessage(tag, message) );
	}
	public static void i( String message, Throwable tr) {
		_i( WebnetLog.TAG, WebnetLog.formatMessage(message + " " + tr.getMessage() ) );
	}
	public static void i( String tag, String message, Throwable tr ) {
		_i( WebnetLog.TAG, WebnetLog.formatMessage(tag, message + " " + tr.getMessage() ) );
	}
	protected static void _i( String tag, String msg ) {
		if( ENABLE_I ) {
			Log.i(tag, msg);
		}
	}

	public static void w() {
		_w( WebnetLog.TAG, WebnetLog.formatMessage() );
	}
	public static void w( String message ) {
		_w( WebnetLog.TAG, WebnetLog.formatMessage(message) );
	}
	public static void w( String tag, String message ) {
		_w( WebnetLog.TAG, WebnetLog.formatMessage(tag, message) );
	}
	public static void w( String message, Throwable tr) {
		_w( WebnetLog.TAG, WebnetLog.formatMessage(message + " " + tr.getMessage() ) );
	}
	public static void w( String tag, String message, Throwable tr ) {
		_w( WebnetLog.TAG, WebnetLog.formatMessage(tag, message + " " + tr.getMessage() ) );
	}
	protected static void _w( String tag, String msg ) {
		if( ENABLE_W ) {
			Log.w(tag, msg);
		}
	}


	public static void e() {
		_e( WebnetLog.TAG, WebnetLog.formatMessage() );
	}
	public static void e( String message ) {
		_e( WebnetLog.TAG, WebnetLog.formatMessage(message) );
	}
	public static void e( String tag, String message ) {
		_e( WebnetLog.TAG, WebnetLog.formatMessage(tag, message) );
	}
	public static void e( String message, Throwable tr) {
		_e( WebnetLog.TAG, WebnetLog.formatMessage(message + " " + tr.getMessage() ) );
	}
	public static void e( String tag, String message, Throwable tr) {
		_e( WebnetLog.TAG, WebnetLog.formatMessage(tag, message + " " + tr.getMessage() ) );
	}
	protected static void _e( String tag, String msg ) {
		if( ENABLE_E ) {
			Log.e(tag, msg);
		}
	}


	public static void d() {
		_d( WebnetLog.TAG, WebnetLog.formatMessage() );
	}
	public static void d( String message ) {
		_d( WebnetLog.TAG, WebnetLog.formatMessage(message) );
	}
	public static void d( String tag, String message ) {
		_d( WebnetLog.TAG, WebnetLog.formatMessage(tag, message) );
	}
	public static void d( String message, Throwable tr) {
		_d( WebnetLog.TAG, WebnetLog.formatMessage(message + " " + tr.getMessage() ) );
	}
	public static void d( String tag, String message, Throwable tr) {
		_d( WebnetLog.TAG, WebnetLog.formatMessage(tag, message + " " + tr.getMessage() ) );
	}
	protected static void _d( String tag, String msg ) {
		if( ENABLE_D ) {
			Log.d(tag, msg);
		}
	}


} // end of class
