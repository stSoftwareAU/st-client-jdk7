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
package com.aspc.remote.performance.internal;

import com.aspc.remote.soap.LoginContext;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.HttpUtil;
import com.aspc.remote.util.misc.HttpUtilException;
import com.aspc.remote.util.misc.StringUtilities;
import com.aspc.remote.util.misc.internal.HttpCookieMgr;
import com.aspc.remote.util.net.NetUrl;
import java.net.URL;
import org.apache.commons.logging.Log;


/**
 *  HTTP Client for HTTP class
 * 
 * @author Nigel Leck
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @since       3 December 2001, 12:02
 */
public class WebClient
{
    /**
     * The raw transport layer.
     * @param host The host name
     */
    public WebClient(String host)
    {
       remoteUrl = new NetUrl( host);
    }
    
        
   /**
     * Login using the defaults
     *
     * @param loginContext The context.
     * @throws Exception Failed to login.
     */
    public void login(final LoginContext loginContext) throws Exception
    {
        String loginUID;
        
        URL url = new URL( remoteUrl.getHost() );

        loginUID = cookieMgr.getCookie( url.getHost(), "LOGIN_UID");
        if( StringUtilities.isBlank( loginUID ))
        {
            getResponse( remoteUrl.getHost() + "/home/" + remoteUrl.getLayer());    

            StringBuilder sb = new StringBuilder("/login?");
            sb.append( "USERNAME=");
            sb.append( remoteUrl.getLogin());
            sb.append( "&PASSWORD=");
            sb.append( remoteUrl.getPassword());
            sb.append( "&TZOBJ=");
            sb.append( loginContext.getClientTimeZoneName());
            if (loginContext.getJavaVersion() != null) 
            {
                sb.append( "&JAVAVERSION=");
                sb.append( loginContext.getJavaVersion());
            }
            if (loginContext.getLanguage() != null ) 
            {
                sb.append( "&LANGUAGE=");
                sb.append( loginContext.getLanguage());
            }
            sb.append( "&TIMEZONE=");
            sb.append( loginContext.getClientTimeZoneName());

            getResponse( remoteUrl.getHost() + sb.toString()); 


            loginUID = cookieMgr.getCookie( url.getHost(), "LOGIN_UID");
            if( StringUtilities.isBlank( loginUID ))
            {
                throw new Exception( "Could not login");
            }
        }
    }
    
    /**
     *
     * @return the value
     */
    public String getHost()
    {
        return remoteUrl.getHost();
    }
    
    /**
     * If the moveTo tag in the header has any URL, then
     * it will be passed to the client.
     * @param url 
     * @return List of URL
     * @throws com.aspc.remote.util.misc.HttpUtilException 
     */
    public String getResponse( String url) throws HttpUtilException
    {
        return HttpUtil.get( url, null, cookieMgr);
    }
    
    /** Log handler */
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.performance.internal.WebClient");//#LOGGER-NOPMD
    
    private final HttpCookieMgr cookieMgr = new HttpCookieMgr( );
    private final NetUrl remoteUrl;
}
