package com.tdispatch.passenger.api;

import java.net.UnknownHostException;

import org.json.JSONObject;

import ch.boye.httpclientandroidlib.HttpResponse;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.methods.HttpGet;
import ch.boye.httpclientandroidlib.client.methods.HttpPost;
import ch.boye.httpclientandroidlib.util.EntityUtils;

import com.tdispatch.passenger.R;
import com.tdispatch.passenger.common.Const;
import com.tdispatch.passenger.core.TDApplication;
import com.webnetmobile.tools.HttpClientFactory;
import com.webnetmobile.tools.JsonTools;
import com.webnetmobile.tools.WebnetLog;

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
final public class ApiNetworker
{
	private static ApiNetworker _instance = null;

	protected TDApplication mApp;

	protected String USER_AGENT_FMT = "Mozilla/5.0 (compatible; %s %s; Passenger Android)";


	private ApiNetworker( TDApplication app ) {
		mApp = app;
	}

	public static ApiNetworker getInstance( TDApplication context ) {
		if( _instance == null ){
			_instance = new ApiNetworker( context );
		}

		return _instance;
	}


	public ApiResponse sendGet( ApiRequest request ) {

		request.buildRequest();

		String url 					= request.getUrl();
		HttpGet mHttpGet 			= new HttpGet( url );
		HttpResponse mHttpResponse	= null;
		ApiResponse responseData	= new ApiResponse();


		mHttpGet.setHeader( "User-Agent", String.format( USER_AGENT_FMT, mApp.getApplicationInfo().packageName, TDApplication.getAppVersion() ) );

    	WebnetLog.d( "Url: " + url );

		HttpClient httpClient = HttpClientFactory.getThreadSafeHttpClient( mApp );

		try {
			mHttpResponse = httpClient.execute(mHttpGet);

			if(mHttpResponse != null) {

				String tmpResponse = EntityUtils.toString(mHttpResponse.getEntity());
				EntityUtils.consume( mHttpResponse.getEntity() );

//				WebnetLog.d("tmpResponse: " + tmpResponse);

				if( mHttpResponse.getStatusLine().getStatusCode() == 200 ) {

	                JSONObject parsedJSON = null;
	                try {
	                	parsedJSON = new JSONObject(tmpResponse);
	                	responseData.setJSONObject( parsedJSON );

	                	String status = parsedJSON.getString("status");
	                	WebnetLog.d("status= '" + status + "'");

	                	if( status.equals("OK") ) {
	                		responseData.setErrorCode(Const.ErrorCode.OK);
	                	} else {
	                		responseData.setErrorCode(Const.ErrorCode.API_ERROR);

	                		JSONObject errorJson = JsonTools.getJSONObject(parsedJSON, "message");
	                		if( errorJson != null ) {
	                			responseData.setErrorMessage( JsonTools.getString(errorJson, "text"));
	                		}
	                	}

	                } catch (Exception e) {
	                	responseData.setErrorCode( Const.ErrorCode.EXCEPTION_ERROR );
	                	responseData.setErrorMessage( e.getMessage() );
	                	responseData.setException( e );
	                	WebnetLog.e("Failed to convert server response to object. " + e.getMessage() );
	                }

				} else {
					responseData.setErrorCode(Const.ErrorCode.API_ERROR);

					String msg = mApp.getString(R.string.msg_unknown_error_body);

					try {
						JSONObject tmp = new JSONObject(tmpResponse);
						try {
							JSONObject json = new JSONObject( tmp.getString("message") );
							msg = json.getString("text");
						} catch ( Exception e ) {}
					} catch( Exception e ) {
						msg = mApp.getString(R.string.msg_failed_to_parse_api_response);
					}

					msg += " #" + mHttpResponse.getStatusLine().getStatusCode();
					responseData.setErrorMessage( msg );
				}

            } else {
            	responseData.setErrorCode( Const.ErrorCode.EXCEPTION_ERROR );
            	responseData.setErrorMessage( mApp.getString(R.string.msg_no_api_response));
            	responseData.setException( new Exception( mApp.getString(R.string.msg_no_api_response)) );

            	WebnetLog.d("httpResponse is null?");
            }

		} catch (UnknownHostException e) {
        	mHttpGet.abort();
        	responseData.setErrorCode( Const.ErrorCode.EXCEPTION_ERROR );
        	responseData.setErrorMessage( e.getMessage() );

            WebnetLog.e( "HttpUtils: " + e + " msg: " + e.getMessage() );

        } catch (Exception e) {
        	mHttpGet.abort();

        	responseData.setErrorCode( Const.ErrorCode.EXCEPTION_ERROR );
        	responseData.setErrorMessage( e.getMessage() );
        	responseData.setException( e );

            WebnetLog.e( "HttpUtils: " + e + " msg: " + e.getMessage() );
        }


		if( responseData.getErrorCode() != Const.ErrorCode.OK ) {
			if( responseData.getErrorMessage().equals("") ) {
				responseData.setErrorMessage( mApp.getString(R.string.msg_unknown_error_body));
			}
		}

		WebnetLog.d("errorCode: " + responseData.getErrorCode() + " msg: " + responseData.getErrorMessage() );
        return responseData;
	}


	public ApiResponse sendPost( ApiRequest request ) {

		request.buildRequest();

		String url 						= request.getUrl();
		HttpPost mHttpPost 				= new HttpPost( url );
		HttpResponse mHttpResponse		= null;
		ApiResponse responseData	= new ApiResponse();

		mHttpPost.setHeader( "User-Agent", String.format( USER_AGENT_FMT, mApp.getApplicationInfo().packageName, TDApplication.getAppVersion() ) );

		WebnetLog.d("Url: " + url  );
		if ( request.sendPostAsJson() == true ) {
			mHttpPost.setEntity( request.getStringEntityWithRequestParams() );
		} else {
			mHttpPost.setEntity( request.getMultipartEntitiyWithPostParams() );
		}
		HttpClient httpClient = HttpClientFactory.getThreadSafeHttpClient( mApp );

		try {
			mHttpResponse = httpClient.execute(mHttpPost);

			if(mHttpResponse != null) {

				String tmpResponse = EntityUtils.toString(mHttpResponse.getEntity());
				EntityUtils.consume( mHttpResponse.getEntity() );

				WebnetLog.d(  "StatusCode: " + mHttpResponse.getStatusLine().getStatusCode()
							+ ", Response: " + tmpResponse
							+ ", URL " + url);


				if( mHttpResponse.getStatusLine().getStatusCode() == 200 ) {

					JSONObject parsedJSON = null;
					try {
						parsedJSON = new JSONObject(tmpResponse);
						responseData.setJSONObject( parsedJSON );

						String status = parsedJSON.getString("status");
						WebnetLog.d("status= '" + status + "'");

						if( status.equals("OK") ) {
							responseData.setErrorCode(Const.ErrorCode.OK);
						} else {
							responseData.setErrorCode(Const.ErrorCode.API_ERROR);

	                		JSONObject errorJson = JsonTools.getJSONObject(parsedJSON, "message");
	                		if( errorJson != null ) {
	                			responseData.setErrorMessage( JsonTools.getString(errorJson, "text"));
	                		}
	                	}

	                } catch (Exception e) {
	                	responseData.setErrorCode( Const.ErrorCode.EXCEPTION_ERROR );
	                	responseData.setException( e );
	                	WebnetLog.e("Failed to convert server response to object. " + e.getMessage() );
	                }

				} else {
					responseData.setErrorCode(Const.ErrorCode.API_ERROR);

					String msg = mApp.getString(R.string.msg_unknown_error_body);

					try {
						JSONObject tmp = new JSONObject(tmpResponse);
						try {
							JSONObject json = new JSONObject( tmp.getString("message") );
							msg = json.getString("text");
						} catch ( Exception e ) {}
					} catch( Exception e ) {
						msg = mApp.getString(R.string.msg_failed_to_parse_api_response);
					}

					msg += " #" + mHttpResponse.getStatusLine().getStatusCode();
					responseData.setErrorMessage( msg );
				}


            } else {
            	responseData.setErrorCode( Const.ErrorCode.EXCEPTION_ERROR );
            	responseData.setErrorMessage( mApp.getString(R.string.msg_no_api_response) );
            	responseData.setException( new Exception( mApp.getString(R.string.msg_no_api_response) ) );

            	WebnetLog.d("httpResponse is null?");
            }

		} catch (UnknownHostException e) {
        	mHttpPost.abort();

        	responseData.setErrorCode( Const.ErrorCode.EXCEPTION_ERROR );
        	responseData.setErrorMessage( e.getMessage() );
            WebnetLog.e( "HttpUtils: " + e + " msg: " + e.getMessage() );

        } catch (Exception e) {
        	mHttpPost.abort();

        	responseData.setErrorCode( Const.ErrorCode.EXCEPTION_ERROR );
        	responseData.setErrorMessage( e.getMessage() );
        	responseData.setException( e );

        	WebnetLog.e( "HttpUtils: " + e + " msg: " + e.getMessage() );
        }


		if( responseData.getErrorCode() != Const.ErrorCode.OK ) {
			if( responseData.getErrorMessage().equals("") ) {
				responseData.setErrorMessage( mApp.getString(R.string.msg_unknown_error_body));
			}
		}

        return responseData;
    }


} // end of class
