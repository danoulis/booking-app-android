package com.tdispatch.passenger.fragment;

import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.core.TDFragment;
import com.tdispatch.passenger.model.OfficeData;
import com.webnetmobile.tools.Redirector;
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
public class OfficeFragment extends TDFragment
{
	protected OfficeData mOfficeData;

	@Override
	protected int getLayoutId() {
		return R.layout.office_fragment;
	}

	@Override
	protected void onPostCreateView() {

		mOfficeData = TDApplication.getOfficeManager().get();

		WebnetTools.setText(mFragmentView, R.id.office_name,  mOfficeData.getName() );

		WebnetTools.setText(mFragmentView, R.id.office_phone, (mOfficeData.hasPhone()) ? mOfficeData.getPhone() : "---");
		WebnetTools.setVisibility(mFragmentView, R.id.phone_container, (mOfficeData.hasPhone() ? View.VISIBLE : View.GONE));

		WebnetTools.setText(mFragmentView, R.id.office_email, (mOfficeData.hasEmail()) ? mOfficeData.getEmail() : "---");
		WebnetTools.setVisibility(mFragmentView, R.id.email_container, (mOfficeData.hasEmail() ? View.VISIBLE : View.GONE));

		int ids[] = { R.id.office_phone, R.id.office_email };
		for( int id : ids ) {
			View v = mFragmentView.findViewById(id);
			v.setOnClickListener(mOnClickListener);
		}

	}

	protected View.OnClickListener mOnClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick( View v ) {

			switch( v.getId() ){

				case R.id.office_phone: {
					if( mOfficeData.hasPhone() ) {
						Intent intent = new Intent();
						intent.setAction(Intent.ACTION_DIAL);
						intent.setData(Uri.parse("tel:" + mOfficeData.getPhone()));
						Redirector.showActivity(mContext, intent);
					}
				}
				break;

				case R.id.office_email: {
					if( mOfficeData.hasEmail() ) {
						final Intent intent = new Intent( Intent.ACTION_SEND );
						intent.setType("text/plain");
						intent.putExtra(Intent.EXTRA_EMAIL, mOfficeData.getEmail());
						intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.office_mail_title));
						intent.putExtra(Intent.EXTRA_TEXT, "");

						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						try{
							startActivity( Intent.createChooser(intent, getString(R.string.office_chooser_select_mua) ) );
						} catch (Exception e) {
							WebnetTools.showErrorToast(mContext, R.string.office_failed_to_launch_mua);
						}
					}
				}
				break;
			}
		}
	};
}
