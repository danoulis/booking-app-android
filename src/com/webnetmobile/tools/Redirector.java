package com.webnetmobile.tools;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;

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
final public class Redirector
{
	// launchers
	public static void showActivity( Context context, Class activityClass ) {
		Intent intent = new Intent();
		showActivity( context, activityClass, intent );
	}
	public static void showActivity( Context context, Class activityClass, Intent intent ) {
		String className = activityClass.getName();

		ComponentName comp = new ComponentName( context.getPackageName(), className );
		intent.setComponent(comp);

		showActivity( context, intent );
	}
	public static void showActivity( Context context, Intent intent ) {

        try {
    		if( context instanceof Application ) {
    			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		} else {
    			if( (context instanceof FragmentActivity) || (context instanceof Activity) ) {
    				// perfect :)
    			} else {
    				WebnetLog.e("Hm, perhaps using wrong context! Needs Activity based, got: " + context );
    			}
    		}

        	context.startActivity( intent );

        } catch ( Exception e ) {
        	e.printStackTrace();
        }
	}

// end of class
}
