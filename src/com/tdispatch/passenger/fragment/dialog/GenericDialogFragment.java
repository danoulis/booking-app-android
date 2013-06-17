package com.tdispatch.passenger.fragment.dialog;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.core.TDDialogFragment;
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
public class GenericDialogFragment extends TDDialogFragment
{
	public static final int DIALOG_TYPE_OK 		= 0;
	public static final int DIALOG_TYPE_ERROR		= 1;

	protected static final String KEY_TYPE 		= "type";
	protected static final String KEY_TITLE	 	= "title";
	protected static final String KEY_MESSAGE		= "msg";
	protected static final String KEY_BUTTON		= "button";

	public static GenericDialogFragment newInstance(int type, String title, String msg) {
		return newInstance(type, title, msg, null);
	}

	public static GenericDialogFragment newInstance(int type, String title, String msg, String button) {

		GenericDialogFragment frag = new GenericDialogFragment();

		Bundle args = new Bundle();
		args.putInt(KEY_TYPE, type);
		args.putString(KEY_TITLE, title);
		args.putString(KEY_MESSAGE, msg);
		args.putString(KEY_BUTTON, button);
		frag.setArguments(args);

		return frag;
	}



	@Override
	protected int getLayoutId() {
		return R.layout.dialog_fragment;
	}

	@Override
	protected void onPostCreateView() {

		View button = mFragmentView.findViewById(R.id.dialog_button_ok);
		button.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View v ) {
				dismiss();
			}
		} );


		Bundle args = getArguments();

		int titleBgColorId;
		int titleFgColorId;

		switch( args.getInt(KEY_TYPE, DIALOG_TYPE_OK) ) {
			case DIALOG_TYPE_OK: {
				titleBgColorId = R.color.dialog_ok_bg;
				titleFgColorId = R.color.dialog_ok_fg;
			}
			break;

			case DIALOG_TYPE_ERROR: {
				titleBgColorId = R.color.dialog_error_bg;
				titleFgColorId = R.color.dialog_error_fg;
			}
			break;

			default: {
				titleBgColorId = R.color.dialog_default_bg;
				titleFgColorId = R.color.dialog_default_bg;
			}
			break;
		}
		TextView titleTv = (TextView)mFragmentView.findViewById(R.id.dialog_title);
		titleTv.setBackgroundColor( getResources().getColor( titleBgColorId ) );
		titleTv.setTextColor( getResources().getColor( titleFgColorId) );

		WebnetTools.setText(mFragmentView, R.id.dialog_title, args.getString(KEY_TITLE) );
		WebnetTools.setText(mFragmentView, R.id.dialog_body, args.getString(KEY_MESSAGE) );

		String buttonLabel = ( args.getString(KEY_BUTTON) != null ) ? args.getString(KEY_BUTTON) : getString(R.string.dialog_button_ok);
		WebnetTools.setText(mFragmentView, R.id.dialog_button_ok, buttonLabel );
	}


	// end of class
}
