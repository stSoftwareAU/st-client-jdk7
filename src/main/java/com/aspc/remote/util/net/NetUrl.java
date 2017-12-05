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
package com.aspc.remote.util.net;

import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.StringUtilities;
import java.net.MalformedURLException;
import java.net.URL;
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
 *  Soap transport.
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 *  @author      Jason McGrath
 *  @since       25 September 2006, 12:02
 */
public class NetUrl
{
    /** HTTP protocol definition */
    public static final String HTTP_PROTOCOL = "http://";
    /** HTTPS protocol definition */
    public static final String HTTPS_PROTOCOL = "https://";
    /** Default URL */
    private static final String DEFUALT_URL = "http://localhost";
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.net.NetUrl");//#LOGGER-NOPMD
    private static final String VALID_PATTERN="((file|ftp|sftp|http|https|s3):/|)/.+";
               
    public static final boolean asserValidURL( final @Nonnull String url)
    {        
        String msg = validateCharactersInURL( url);

        if( msg!=null) 
        {
            assert false: msg;
            return false;
        }

        if( url.matches(VALID_PATTERN)==false)
        {
            assert false: "should match " + VALID_PATTERN +" was: "+ StringUtilities.stripPasswordFromURL(url);
            return false;
        }
        return true;
    }
    
    public static String correctURL( final String url)
    {
        StringBuilder sb=new StringBuilder( url.length());
        boolean changed=false;
        for( char c: url.toCharArray())
        {
            if( StringUtilities.isCharacterValidURL(c))
            {
                sb.append(c);
            }
            else
            {
                sb.append(StringUtilities.encode("" + c));
                changed=true;
            }
        }

        if( changed)
        {
            LOGGER.warn( "Invalid URL: " + url);
            try {               
                
                URL tmpURL=new URL( sb.toString());
                return tmpURL.toString();
            } catch (MalformedURLException ex) {
               throw new RuntimeException( sb.toString(), ex);
            }
        }
        
        return url;
    }
    public static String validateCharactersInURL( final String checkURL)
    {
        if( checkURL==null || checkURL.isEmpty()) return "blank URL";

        String templateMSG="Invalid character '%1s' found in the URL. The valid characters are defined in RFC 7230 and RFC 3986";

        for( char c: checkURL.toCharArray())
        {
            if( StringUtilities.isCharacterValidURL(c)==false)
            {
                return String.format( templateMSG, c);
            }
        }

        return null;
    }
    
    /**
     * The default constructor to parse a supplied url
     * @param theURL the URL to connect to
     */
    public NetUrl(final String theURL)
    {
        boolean isHttpsProtocol = false;
        String url = theURL;
        if( url == null )
        {
            url = DEFUALT_URL;
        }

        if (url.startsWith( HTTPS_PROTOCOL))
        {
            isHttpsProtocol = true;
        }

        url = removeProtocolFromUrl(url);

        String tempHost=url;
        String tempLogin = "guest";
        String tempPassword = "guest";
        String tempLayer = "";

        int pos = tempHost.indexOf( "@");

        if( pos != -1)
        {
            tempLogin = tempHost.substring( 0, pos);
            tempHost = tempHost.substring( pos + 1);

            pos = tempLogin.indexOf( ":");

            if( pos != -1)
            {
                tempPassword = StringUtilities.decode(tempLogin.substring( pos + 1));
                tempLogin = StringUtilities.decode(tempLogin.substring( 0, pos));
            }
        }

        pos = tempHost.indexOf( "/");

        if( pos != -1)
        {
            tempLayer=tempHost.substring( pos + 1);
            tempHost = tempHost.substring( 0, pos);
        }

        if (isHttpsProtocol)
        {
            tempHost = HTTPS_PROTOCOL + tempHost;
        }
        else
        {
            tempHost = HTTP_PROTOCOL + tempHost;
        }

        host = tempHost;
        login=tempLogin;
        password=tempPassword;
        layer=tempLayer;
    }

    
    /**
     * Just accept all SSL certs.
     * @param conn the connection to relax the cert checking. 
     */
    public static void relaxSSLConnection(final URLConnection conn)
    {
        if( conn instanceof HttpsURLConnection)
        {
            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            } };
            
            SSLContext sc;
            try
            {
                /** Install the all-trusting trust manager */
                sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
            }
            catch( NoSuchAlgorithmException | KeyManagementException e)
            {
                throw CLogger.rethrowRuntimeExcepton(e);
            }
        }
    }

//
//    private static void httpsCert(final HttpsURLConnection con) {
//
//        if (con != null) {
//
//            try {
//
//                LOGGER.info("Response Code : " + con.getResponseCode());
//                LOGGER.info("Cipher Suite : " + con.getCipherSuite());
//                LOGGER.info("");
//
//                Certificate[] certs = con.getServerCertificates();
//                for (Certificate cert : certs) {
//                    LOGGER.info("Cert Type : " + cert.getType());
//                    LOGGER.info("Cert Hash Code : " + cert.hashCode());
//                    LOGGER.info("Cert Public Key Algorithm : "
//                            + cert.getPublicKey().getAlgorithm());
//                    LOGGER.info("Cert Public Key Format : "
//                            + cert.getPublicKey().getFormat());
//                    LOGGER.info("");
//                }
//
//            } catch (SSLPeerUnverifiedException e) {
//                LOGGER.warn(con.getURL(), e);
//            } catch (IOException e) {
//                LOGGER.warn(con.getURL(), e);
//            }
//
//        }
//
//    }

    /**
     * to string
     * @return the string
     */
    @Override @CheckReturnValue @Nonnull
    public String toString()
    {
        return login + "@" + host + "/" + layer;
    }

    /**
     * The host.
     * @return the host
     */
    public String getHost()
    {
        return host;
    }

    /**
     * The login.
     * @return The login
     */
    public String getLogin()
    {
        return login;
    }

    /**
     * The password
     * @return the password.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * The layer
     * @return the layer
     */
    public String getLayer()
    {
        return layer;
    }

    /**
     * To remove the protocol from the URL, if it passed URL
     * has one.
     * @param _url URL to stripe the protocol
     * @return String without the protocol.
     */
    public static String removeProtocolFromUrl( final String _url )
    {
        if( _url.startsWith( HTTPS_PROTOCOL ) || _url.startsWith( HTTP_PROTOCOL ) )
        {
            if( _url.startsWith( HTTPS_PROTOCOL) )
            {
                return _url.substring( HTTPS_PROTOCOL.length() );
            }
            else
            {
                // HTTP Protocol
                return _url.substring( HTTP_PROTOCOL.length() );
            }
        }

        return _url;
    }


     /** Host name */
    private final String  host;

    /** Default login id */
    protected String      login;//NOPMD

    /** Default Password */
    protected String      password = "";
    /** Default Layer */
    private String        layer = "";//NOPMD
}
