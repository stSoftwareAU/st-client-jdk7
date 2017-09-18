/**
 *  STS Remote library
 *
 *  Copyright (C) 2006  stSoftware Pty Ltd
 *
 *  stSoftware.com.au
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *
 *  info AT stsoftware.com.au
 *
 *  or by snail mail to:
 *
 *  stSoftware
 *  building C, level 1,
 *  14 Rodborough Rd
 *  Frenchs Forest 2086
 *  Australia.
 */
package com.aspc.remote.rest.internal;

import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.StringUtilities;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.logging.Log;

/**
 * Allow all SSL certs
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED test case</i>
 *
 * @author Nigel Leck
 * @since 12 August 2017
 */
public final class TrustAllCertsAuthorization implements ReSTAuthorizationInterface
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.rest.internal.TrustAllCertsAuthorization");//#LOGGER-NOPMD

    @Override
    public ReSTAuthorizationInterface setRequestProperty(URLConnection c) {

        if( c instanceof HttpsURLConnection)
        {
            HttpsURLConnection httpsCon=(HttpsURLConnection)c;
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                   @Override
                   public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                     return null;
                   }

                   @Override
                   public void checkClientTrusted(X509Certificate[] certs, String authType) {  }

                   @Override
                   public void checkServerTrusted(X509Certificate[] certs, String authType) {  }

                }
            };

            try{
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());

                httpsCon.setSSLSocketFactory(sc.getSocketFactory());
            }
            catch( KeyManagementException | NoSuchAlgorithmException e)
            {
                LOGGER.warn( "Can not set the socket factory", e);
            }
        }
        else
        {
            LOGGER.warn( "Not a SSL connection: " + c.getURL());
        }
        return this;
    }

    @Override @Nonnull @CheckReturnValue
    public String checkSumAdler32(final @Nonnull String url) {
        return StringUtilities.checkSumAdler32(url + "NO_CERTS");
    }

    @Override @Nonnull @CheckReturnValue
    public String toShortString() {
        return "trust_all";
    }
}
