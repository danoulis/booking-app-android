package com.webnetmobile.tools;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import ch.boye.httpclientandroidlib.conn.ssl.SSLSocketFactory;
import ch.boye.httpclientandroidlib.conn.ssl.TrustStrategy;
import ch.boye.httpclientandroidlib.conn.ssl.X509HostnameVerifier;

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
public final class WebnetSslSocketFactory extends SSLSocketFactory
{
	public WebnetSslSocketFactory() throws NoSuchAlgorithmException, KeyManagementException, UnrecoverableKeyException,
			KeyStoreException {
		super(trustStrategy, hostnameVerifier);
	}

	private static final X509HostnameVerifier hostnameVerifier = new X509HostnameVerifier()
	{

		@Override
		public boolean verify( String hostname, SSLSession session ) {
			return true;
		}

		@Override
		public void verify( String arg0, String[] arg1, String[] arg2 ) throws SSLException {
			// do nothing
		}

		@Override
		public void verify( String arg0, X509Certificate arg1 ) throws SSLException {
			// do nothing
		}

		@Override
		public void verify( String arg0, SSLSocket arg1 ) throws IOException {
			// do nothing
		}
	};

	private static final TrustStrategy trustStrategy = new TrustStrategy()
	{

		@Override
		public boolean isTrusted( X509Certificate[] arg0, String arg1 ) throws CertificateException {
			return true;
		}
	};

}
