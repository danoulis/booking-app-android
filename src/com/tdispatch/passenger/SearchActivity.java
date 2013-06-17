package com.tdispatch.passenger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.tdispatch.passenger.common.Const;
import com.tdispatch.passenger.core.TDActivity;
import com.tdispatch.passenger.fragment.AddressSearchFragment;
import com.tdispatch.passenger.host.AddressSearchHostInterface;
import com.tdispatch.passenger.model.LocationData;

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
public class SearchActivity extends TDActivity implements AddressSearchHostInterface
{
	@Override
	public void onCreate( Bundle savedInstanceState ) {

		super.onCreate(savedInstanceState);

		setContentView( R.layout.search_activity );

		AddressSearchFragment frag = new AddressSearchFragment();
		frag.setArguments( getIntent().getExtras() );

		setFragment(frag, false);
	}


	@Override
	public void doSearchOk( int type, LocationData location ) {

    	// build the result bundle
    	Intent intent = new Intent();
    	intent.putExtra( Const.Bundle.TYPE, type );
    	intent.putExtra( Const.Bundle.LOCATION, location );

    	setResult( Activity.RESULT_OK, intent );
		finish();
	}

	@Override
	public void doSearchCancel() {
		setResult( Activity.RESULT_CANCELED );
		finish();
	}



} // end of class
