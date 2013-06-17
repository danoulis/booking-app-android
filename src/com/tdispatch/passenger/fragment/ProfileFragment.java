package com.tdispatch.passenger.fragment;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.core.TDFragment;
import com.tdispatch.passenger.model.AccountData;
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
public class ProfileFragment extends TDFragment
{
	@Override
	protected int getLayoutId() {
		return R.layout.profile_fragment;
	}

	@Override
	protected void onPostCreateView() {

		AccountData ad = TDApplication.getSessionManager().getAccountData();

		if( ad != null ) {
			String name = ad.getFullName();

			String phone = ad.getPhone();
			if( phone.length() == 0 ) {
				phone = "---";
			}

			String email = ad.getEmail();
			if( email.length() == 0 ) {
				email = "---";
			}

			WebnetTools.setText( mFragmentView, R.id.profile_name, name );
			WebnetTools.setText( mFragmentView, R.id.profile_phone, phone );
			WebnetTools.setText( mFragmentView, R.id.profile_email, email );

		}
	}
}
