package com.tdispatch.passenger.fragment;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.api.ApiHelper;
import com.tdispatch.passenger.api.ApiRequest;
import com.tdispatch.passenger.api.ApiResponse;
import com.tdispatch.passenger.common.Const;
import com.tdispatch.passenger.core.TDApplication;
import com.tdispatch.passenger.core.TDFragment;
import com.tdispatch.passenger.fragment.dialog.GenericDialogFragment;
import com.tdispatch.passenger.host.OAuthHostInterface;
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
public class OAuthFragment extends TDFragment
{
	protected Handler mHandler = new Handler();
	protected WebView mWebView;

	protected String mOAuthRedirectUrl = Const.Api.BaseUrl + "/passenger/oauth/dummy/redirect";
	protected String mOAuthRedirectToGetTokensUrl = Const.Api.BaseUrl + "/passenger/oauth/dummy/redirect-to-get-tokens";

	@Override
	protected int getLayoutId() {
		return R.layout.oauth_fragment;
	}

	protected View.OnClickListener mOnClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick( View v ) {
			switch( v.getId() ) {
				case R.id.button_cancel: {
					mHostActivity.oAuthCancelled();
				}
				break;
			}
		}
	};

	protected OAuthHostInterface mHostActivity;
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mHostActivity = (OAuthHostInterface)activity;
		} catch( ClassCastException e ) {
			throw new ClassCastException("Host Activity needs to implement OAuthHostInterface");
		}
	}

	@SuppressLint( "SetJavaScriptEnabled" )
	@Override
	protected void onPostCreateView() {

		View v = mFragmentView.findViewById( R.id.button_cancel );
		v.setOnClickListener(mOnClickListener);

		ProgressBar pb = (ProgressBar)mFragmentView.findViewById(R.id.progressbar);
		pb.setVisibility( View.GONE );

		mWebView = (WebView)mFragmentView.findViewById( R.id.webview );
		if( mWebView != null ) {

			mWebView.setWebViewClient( new MyWebViewClient() );
			mWebView.setWebChromeClient( new MyWebchromeClient() );

			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
				mWebView.setOnTouchListener( mOnTouchListener );
			}

			WebSettings webSettings = mWebView.getSettings();
			webSettings.setJavaScriptEnabled(true);
			webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
			webSettings.setAppCacheEnabled(false);
			webSettings.setSavePassword(false);
			webSettings.setSaveFormData(false);

			CookieSyncManager.createInstance( mContext );
			CookieManager cm = CookieManager.getInstance();
			cm.setAcceptCookie(true);
			cm.removeAllCookie();

			try {
				ApiRequest req = new ApiRequest( Const.Api.OauthAuthUrl );
				req.addGetParam("key", Const.getApiKey());
				req.addGetParam("scope", "");
				req.addGetParam("response_type", "code");
				req.addGetParam("client_id", Const.getOAuthClientId());
				req.addGetParam("redirect_uri", mOAuthRedirectUrl);
				req.buildRequest();

				String url = req.getUrl();

				mWebView.loadUrl( url );

			} catch (Exception e ) {
				WebnetLog.e("Failed to load oauth launch page...");
			}

		} else {
			WebnetLog.e("Failed to init WebView. Aborting");
			mHostActivity.oAuthCancelled();
		}
	}


	private class MyWebViewClient extends WebViewClient {

		@TargetApi(Build.VERSION_CODES.FROYO)
		@Override
		public void onReceivedSslError( WebView webView, SslErrorHandler handler, SslError error ) {
			handler.proceed();
		}

		@Override
		public void onReceivedError(WebView webView, int errorCode, String description, String failingUrl ) {
			webView.loadUrl("file:///android_asset/connect_error.html");	// FIXME replace this HTML with native view
		};


		@Override
		public boolean shouldOverrideUrlLoading(WebView webView, String url) {

			Boolean consumed = false;

			if( url.startsWith( mOAuthRedirectUrl ) ) {
				Uri uri = Uri.parse( url );
				String mTemporaryAuthCode = uri.getQueryParameter("code");

				if( mTemporaryAuthCode != null) {
					if (mTemporaryAuthCode.equals("denied") == false) {
						WebnetTools.executeAsyncTask( new GetOAuthAccessTokenAsyncTask(), mTemporaryAuthCode );
					} else {
						mHostActivity.oAuthCancelled();
					}
				}


//				if( (mTemporaryAuthCode != null) && (mTemporaryAuthCode.equals("denied") == false) ) {
//					WebnetTools.executeAsyncTask( new GetOAuthAccessTokenAsyncTask(), mTemporaryAuthCode );
//
//				} else if( url.startsWith( mOAuthRedirectToGetTokensUrl ) ) {
//					// nothing
//				}

				consumed = true;
			}

			return consumed;
		};
	}

	private class MyWebchromeClient extends WebChromeClient {

		@Override
		public void onProgressChanged( WebView webView, int progress ) {

			ProgressBar pb = (ProgressBar)mFragmentView.findViewById(R.id.progressbar);
			if( progress > 0 ) {
				pb.setVisibility(View.VISIBLE);
			}

			pb.setProgress(progress);

			if( progress >= 100 ) {
				pb.setVisibility(View.GONE);
			}
		}
	}

	protected View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_UP:
					if (!v.hasFocus()) {
						v.requestFocus();
					}
					break;
			}
			return false;
		}
	};

	public class GetOAuthAccessTokenAsyncTask extends AsyncTask<String, Void, ApiResponse> {

		@Override
		protected void onPreExecute() {
			lockUI(true);
		}

		@Override
		protected ApiResponse doInBackground( String ... args ) {

			ApiResponse result = new ApiResponse();

			String tmpAccessCode = args[0];
			ApiResponse tokensApiResponse = new ApiResponse();

			ApiHelper api = ApiHelper.getInstance( mApp );
			try {
				tokensApiResponse = api.getOAuthTokens( tmpAccessCode );

				if( tokensApiResponse.getErrorCode() == Const.ErrorCode.OK ) {

					TDApplication.getSessionManager().setAccessToken( JsonTools.getString(tokensApiResponse.getJSONObject(), "access_token") );
					TDApplication.getSessionManager().setRefreshToken( JsonTools.getString( tokensApiResponse.getJSONObject(), "refresh_token") );

					long expiresIn = JsonTools.getInt(tokensApiResponse.getJSONObject(), "expires_in", 0) * WebnetTools.MILLIS_PER_SECOND;
					expiresIn += System.currentTimeMillis();
					TDApplication.getSessionManager().setAccessTokenExpirationMillis( expiresIn + System.currentTimeMillis() );

					ApiResponse fleetDataResponse = api.getAccountFleetData();
					if( fleetDataResponse.getErrorCode() == Const.ErrorCode.OK ) {
						JSONObject fleetJson = JsonTools.getJSONObject( fleetDataResponse.getJSONObject(), "data" );
						OfficeData office = new OfficeData();
						office.set( fleetJson );
					}

					ApiResponse profileResponse = api.getAccountProfile();
					if( profileResponse.getErrorCode() == Const.ErrorCode.OK ) {
						JSONObject tmp = profileResponse.getJSONObject();
						TDApplication.getSessionManager().putAccountData( new AccountData( tmp.getJSONObject("preferences") ));

						result.setErrorCode(Const.ErrorCode.OK);
					} else {
						result = profileResponse;
					}

				} else {
					result = tokensApiResponse;
				}

			} catch( Exception e ) {
				e.printStackTrace();
			}

			return result;
		}

		@Override
		protected void onPostExecute(ApiResponse apiResponse) {
			if( apiResponse.getErrorCode() == Const.ErrorCode.OK ) {
				mHostActivity.oAuthAuthenticated();
			} else {
				lockUI(false);
				showDialog(GenericDialogFragment.DIALOG_TYPE_ERROR, getString(R.string.dialog_error_title), apiResponse.getErrorMessage() );
			}
		}
	}

}
