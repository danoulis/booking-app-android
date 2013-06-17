package com.webnetmobile.tools;

import java.io.InputStream;
import java.security.KeyStore;

import android.content.Context;
import ch.boye.httpclientandroidlib.HttpVersion;
import ch.boye.httpclientandroidlib.client.HttpClient;
import ch.boye.httpclientandroidlib.client.params.ClientPNames;
import ch.boye.httpclientandroidlib.client.params.CookiePolicy;
import ch.boye.httpclientandroidlib.conn.scheme.PlainSocketFactory;
import ch.boye.httpclientandroidlib.conn.scheme.Scheme;
import ch.boye.httpclientandroidlib.conn.scheme.SchemeRegistry;
import ch.boye.httpclientandroidlib.conn.ssl.AllowAllHostnameVerifier;
import ch.boye.httpclientandroidlib.conn.ssl.SSLSocketFactory;
import ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient;
import ch.boye.httpclientandroidlib.impl.conn.PoolingClientConnectionManager;
import ch.boye.httpclientandroidlib.params.BasicHttpParams;
import ch.boye.httpclientandroidlib.params.CoreProtocolPNames;
import ch.boye.httpclientandroidlib.params.HttpConnectionParams;
import ch.boye.httpclientandroidlib.params.HttpParams;

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
 *
 * Using Apache httpclient http://code.google.com/p/httpclientandroidlib/
 *
 */
final public class HttpClientFactory
{
    private static HttpClient mDefaultHttpClient;

    public synchronized static HttpClient getThreadSafeHttpClient( Context context ) {
    	if( mDefaultHttpClient == null ) {

    		try {
	    		// as per: http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html
	    		SchemeRegistry schemeRegistry = new SchemeRegistry();
	    		schemeRegistry.register( new Scheme("http" ,  80, PlainSocketFactory.getSocketFactory()) );
	    		schemeRegistry.register( new Scheme("https", 443, new WebnetSslSocketFactory() ) );

	    		PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager( schemeRegistry );
	    		connectionManager.setDefaultMaxPerRoute(25);
	    		connectionManager.setMaxTotal( 200 );

    		HttpParams httpParams = new BasicHttpParams();
	    		httpParams.setParameter( CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
	    		httpParams.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);
	    		HttpConnectionParams.setConnectionTimeout(httpParams, (int)(30 * WebnetTools.MILLIS_PER_SECOND) );
	    		HttpConnectionParams.setSoTimeout(httpParams, (int)(30 * WebnetTools.MILLIS_PER_SECOND) );

	    		mDefaultHttpClient = new DefaultHttpClient(connectionManager, httpParams);
    		} catch( Exception e ) {
    			e.printStackTrace();
    		}
    	}

    	return mDefaultHttpClient;
    }

	protected static String getKeystorePassword() {
		return null;		// we don't use keystore
	}

	protected static int getKeystoreResourceId() {
		return 0;			// set to proper keystore (if we going to use any)
	}

	protected static SSLSocketFactory newSslSocketFactory( Context context ) {
		try {
			// Get an instance of the Bouncy Castle KeyStore format
			KeyStore trusted = KeyStore.getInstance("BKS");
			// Get the raw resource, which contains the keystore with trusted certificates (root and any intermediate certs)
			InputStream in = context.getResources().openRawResource( getKeystoreResourceId() );
			try {
				// Initialize the keystore with the provided trusted certificates and password
				trusted.load(in, getKeystorePassword().toCharArray());
			} finally {
				in.close();
			}
			// Pass the keystore to the SSLSocketFactory. The factory is responsible
			// for the verification of the server certificate.
			SSLSocketFactory sf = new SSLSocketFactory(trusted);
			// Hostname verification from certificate
			// http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d4e506
			//sf.setHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
			sf.setHostnameVerifier( new AllowAllHostnameVerifier() );
			return sf;
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

}
