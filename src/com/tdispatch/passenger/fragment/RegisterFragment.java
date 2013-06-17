package com.tdispatch.passenger.fragment;

import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.api.ApiHelper;
import com.tdispatch.passenger.api.ApiResponse;
import com.tdispatch.passenger.common.Const;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.core.TDFragment;
import com.tdispatch.passenger.fragment.dialog.GenericDialogFragment;
import com.tdispatch.passenger.host.RegisterHostInterface;
import com.tdispatch.passenger.model.AccountData;
import com.tdispatch.passenger.model.OfficeData;
import com.webnetmobile.tools.JsonTools;
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
public class RegisterFragment extends TDFragment
{
	protected Handler mHandler = new Handler();

	@Override
	protected int getLayoutId() {
		return R.layout.register_fragment;
	}

	protected RegisterHostInterface mHostActivity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mHostActivity = (RegisterHostInterface)activity;
		} catch( ClassCastException e ) {
			throw new ClassCastException("Host Activity needs to implement RegisterHostInterface");
		}
	}

	@Override
	protected void onPostCreateView() {

		int ids[] = { R.id.button_register };

		for( int id : ids ) {
			View v = mFragmentView.findViewById( id );
			v.setOnClickListener(mOnClickListener);
		}
	}

	protected View.OnClickListener mOnClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick( View v ) {

			switch( v.getId() ) {

				case R.id.button_register: {

					String firstName = getEditTextContent( R.id.et_first_name).trim();
					String lastName = getEditTextContent( R.id.et_last_name).trim();
					String email = getEditTextContent( R.id.et_email).trim();
					String phone = getEditTextContent( R.id.et_phone).trim();
					String password1 = getEditTextContent( R.id.et_password1);
					String password2 = getEditTextContent( R.id.et_password2);

					int errorMsgId = 0;

					if( errorMsgId == 0 ) {
						if( (firstName.length() < 2) || (firstName.length() > 50) ) {
							errorMsgId = R.string.register_form_dialog_error_first_name;
						}
					}
					if( errorMsgId == 0 ) {
						if( (lastName.length() < 2) || (lastName.length() > 50) ) {
							errorMsgId = R.string.register_form_dialog_error_last_name;
						}
					}
					if( errorMsgId == 0 ) {
						if( (email.length() < 2) || (email.length() > 128) ) {
							errorMsgId = R.string.register_form_dialog_error_email;
						}
					}

					if( errorMsgId == 0 ) {
						if( (phone.length() < 2) || (phone.length() > 20) ) {
							errorMsgId = R.string.register_form_dialog_error_phone;
						}
					}

					if( errorMsgId == 0 ) {
						if( (password1.length() < 1) || (password1.length() > 32) ) {
							errorMsgId = R.string.register_form_dialog_error_password;
						} else {
							if( password1.equals( password2) == false ) {
								errorMsgId = R.string.register_form_dialog_error_password_verification;
							}
						}
					}

					if( errorMsgId == 0 ) {
						AccountData account = new AccountData();

						account.setFirstName(firstName);
						account.setLastName(lastName);
						account.setEmail(email);
						account.setPhone(phone);
						account.setPassword(password1);

						WebnetTools.executeAsyncTask( new CreateAccountAsyncTask(), account );
					} else {
						String msg = String.format(getString( errorMsgId ));
						showDialog(GenericDialogFragment.DIALOG_TYPE_ERROR, getString(R.string.dialog_error_title), msg);
					}
				}
				break;
			}
		}
	};

	protected String getEditTextContent( int editTextId ) {
		String result = "";

		EditText et = (EditText)mFragmentView.findViewById( editTextId );
		if( et != null ) {
			result = et.getText().toString();
		}

		return result;
	}

	protected class CreateAccountAsyncTask extends AsyncTask<AccountData, Void, ApiResponse> {

		@Override
		protected void onPreExecute() {
			lockUI(true);
		}

		@Override
		protected ApiResponse doInBackground( AccountData ... params ) {

			AccountData account = params[0];

			ApiResponse response = new ApiResponse();
			ApiHelper api = ApiHelper.getInstance(TDApplication.getAppContext());

			try {
				response = api.accountCreate(account);

				if( response.getErrorCode() == Const.ErrorCode.OK ) {

					JSONObject tmpJson = JsonTools.getJSONObject(response.getJSONObject(), "passenger");
						TDApplication.getSessionManager().setAccessToken( JsonTools.getString(tmpJson, "access_token") );
						TDApplication.getSessionManager().setAccessTokenExpirationMillis( JsonTools.getInt(tmpJson, "expires_in", 0) + System.currentTimeMillis() );
						TDApplication.getSessionManager().setRefreshToken( JsonTools.getString(tmpJson, "refresh_token") );

					ApiResponse profileResponse = api.getAccountProfile();
					if( profileResponse.getErrorCode() == Const.ErrorCode.OK ) {
						JSONObject tmp = profileResponse.getJSONObject();
						TDApplication.getSessionManager().putAccountData( new AccountData( tmp.getJSONObject("preferences") ));
					} else {
						TDApplication.getSessionManager().doLogout();
						response = profileResponse;
					}

					ApiResponse fleetDataResponse = api.getAccountFleetData();
					if( fleetDataResponse.getErrorCode() == Const.ErrorCode.OK ) {
						JSONObject fleetJson = JsonTools.getJSONObject( fleetDataResponse.getJSONObject(), "data" );
						OfficeData office = new OfficeData();
						office.set( fleetJson );
					}

				}
			} catch ( Exception e ) {
				e.printStackTrace();
			}


			return response;
		}

		@Override
		protected void onPostExecute(ApiResponse response) {
			if( response.getErrorCode() == Const.ErrorCode.OK ) {
				mHandler.postDelayed(MyRunnable, 500);
			} else {
				lockUI(false);
				showDialog(GenericDialogFragment.DIALOG_TYPE_ERROR, getString(R.string.dialog_error_title), response.getErrorMessage() );
			}
		}
	}

	protected Runnable MyRunnable = new Runnable()
	{
		@Override
		public void run() {
			lockUI(false);
			mHostActivity.registerCompleted();
		}
	};

} // end of class
