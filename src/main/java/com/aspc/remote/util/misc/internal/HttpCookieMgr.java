/**
 *  STS Remote library
 *
 *  Copyright (C) 2006  stSoftware Pty Ltd
 *
 *  www.stsoftware.com.au
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
package com.aspc.remote.util.misc.internal;

import com.aspc.remote.memory.HashMapFactory;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.DateUtil;
import com.aspc.remote.util.misc.StringUtilities;
import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import org.apache.commons.logging.Log;


/**
 *  HttpCookieMgr - utility class for handling cookies for Http calls
 *
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 * @author Jason McGrath
 * @since 7 September 2006
 *
 * @original author <a href="mailto:spam@hccp.org">Ian Brown</a>
 */
public final class HttpCookieMgr
{

    private final Map store;

    private TimeZone timeZone;//NOPMD

    private static final String SET_COOKIE = "Set-Cookie";
    private static final String COOKIE_VALUE_DELIMITER = ";";
    private static final String PATH = "path";
    private static final String EXPIRES = "expires";
    private static final String SET_COOKIE_SEPARATOR="; ";
    private static final String COOKIE = "Cookie";


    private static final char NAME_VALUE_SEPARATOR = '=';
    private static final char DOT = '.';

    private DateFormat dateFormat = null;

    /**
     * Constructor
     * Sets the default timezone and sets up a store for the cookie values
     */
    public HttpCookieMgr()
    {
        store = HashMapFactory.create();
        setTimeZone( DateUtil.GMT_ZONE);
    }


    /**
     * Sets the timezone used when comparing if cookies have expired
     *
     * @param tz - timezone
     */
    public void setTimeZone( TimeZone tz)
    {
        this.timeZone = tz;

        dateFormat = new SimpleDateFormat( "EEE, dd-MMM-yyyy hh:mm:ss z", Locale.ENGLISH);
        dateFormat.setTimeZone( timeZone);
    }

    /**
     * Retrieves and stores cookies returned by the host on the other side
     * of the the open java.net.URLConnection.
     *
     * The connection MUST have been opened using the <i>connect()</i>
     * method or a IOException will be thrown.
     *
     * @param conn a java.net.URLConnection - must be open, or IOException will be thrown
     * @throws java.io.IOException Thrown if <i>conn</i> is not open.
     */
    public void retrieveCookies( final URLConnection conn) throws IOException
    {

        // let's determine the domain from where these cookies are being sent
        String host = conn.getURL().getHost();

        // OK, now we are ready to get the cookies out of the URLConnection

        String headerName;
        for (int i=1; (headerName = conn.getHeaderFieldKey(i)) != null; i++)
        {
            if (headerName.equalsIgnoreCase(SET_COOKIE))
            {
                StringTokenizer st = new StringTokenizer(conn.getHeaderField(i), COOKIE_VALUE_DELIMITER);

                // the specification dictates that the first name/value pair
                // in the string is the cookie name and value, so let's handle
                // them as a special case:
                //String cookieName = "";

                while (st.hasMoreTokens())
                {
                    String token  = st.nextToken();
                    if( StringUtilities.isBlank(token)) continue;
                    int pos = token.indexOf(NAME_VALUE_SEPARATOR);
                    if( pos == -1) continue;
                    
                    String cookieName = token.substring(0, pos);
                    String value = token.substring(pos + 1, token.length());
                    addCookie( host, cookieName, value);

                }
                /*
                while (st.hasMoreTokens())
                {
                    String token  = st.nextToken();
                    addCookie( host,
                        cookieName,
                        token.substring(0, token.indexOf(NAME_VALUE_SEPARATOR)).toLowerCase(),
                        token.substring(token.indexOf(NAME_VALUE_SEPARATOR) + 1, token.length()));
                }*/
            }
        }
    }

    /**
     * Retrieves the value of the cookie
     * @param host - host that the cookie is related to
     * @param cookieName - the name of the cookie
     * @return - the value of the specified cookie or null if none was found
     */
    public String getCookie( String host, String cookieName)
    {
        String domain = getDomainFromHost(host);

        Map domainStore = (Map)store.get(domain);

        if ( domainStore != null)
        {
            Map cookie = (Map)domainStore.get(cookieName);
            if ( cookie != null)
            {
                return (String)cookie.get( cookieName);
            }
        }
        return null;
    }

    /**
     * Sets the value of a cookie
     * @param host - the host that the cookie is related to
     * @param cookieName - the name of the cookie
     * @param paramValue - the value to set the cookie to
     */
    public void addCookie( String host, String cookieName, String paramValue)
    {
        addCookie( host, cookieName, cookieName, paramValue);
    }

    /**
     *
     * Sets the value of a cookie parameter
     * @param host - the host that the cookie is related to
     * @param cookieName - the name of the cookie
     * @param paramName - parameter name
     * @param paramValue - the value to set the cookie to
     */
    public void addCookie( String host, String cookieName, String paramName, String paramValue)
    {
        String domain = getDomainFromHost(host);

        Map domainStore = (Map)store.get(domain);

        if ( domainStore == null)
        {
            // we don't, so let's create it and put it in the store
            domainStore = HashMapFactory.create();
            store.put(domain, domainStore);
        }

        Map cookie = (Map)domainStore.get(cookieName);
        if ( cookie == null)
        {
            // we don't, so let's create it and put it in the store
            cookie = HashMapFactory.create();
            domainStore.put(cookieName, cookie);
        }

        cookie.put( paramName, paramValue);

    }

    /**
     * Prior to opening a URLConnection, calling this method will set all
     * unexpired cookies that match the path or subpaths for thi underlying URL
     *
     * The connection MUST NOT have been opened or an IOException will be thrown.
     *
     * @param conn a java.net.URLConnection - must NOT be open, or IOException will be thrown
     * @throws java.io.IOException Thrown if <i>conn</i> has already been opened.
     */
    public void applyCookies(URLConnection conn) throws IOException
    {

        // let's determine the domain and path to retrieve the appropriate cookies
        URL url = conn.getURL();
        String domain = getDomainFromHost(url.getHost());
        String path = url.getPath();

        Map domainStore = (Map)store.get(domain);
        if (domainStore == null) return;
        StringBuilder cookieStringBuilder = new StringBuilder();

        Iterator cookieNames = domainStore.keySet().iterator();
        while(cookieNames.hasNext())
        {
            String cookieName = (String)cookieNames.next();
            Map cookie = (Map)domainStore.get(cookieName);

            // check cookie to ensure path matches  and cookie is not expired
            // if all is cool, add cookie to header string
            if (comparePaths((String)cookie.get(PATH), path) &&
                isNotExpired((String)cookie.get(EXPIRES)))
            {
                cookieStringBuilder.append(cookieName);
                cookieStringBuilder.append("=");
                cookieStringBuilder.append((String)cookie.get(cookieName));
                if (cookieNames.hasNext())
                    cookieStringBuilder.append(SET_COOKIE_SEPARATOR);
            }
        }
        try
        {
            conn.setRequestProperty(COOKIE, cookieStringBuilder.toString());
        }
        catch (java.lang.IllegalStateException ise)
        {
            LOGGER.error( "Illegal State!", ise);
            IOException ioe = new IOException(
                "Illegal State! Cookies cannot be set on a URLConnection that is already connected. " +
                "Only call setCookies(java.net.URLConnection) AFTER calling java.net.URLConnection.connect()."

            );
            throw ioe; //NOPMD
        }
    }

    private String getDomainFromHost(String host)
    {
        if (host.indexOf(DOT) != host.lastIndexOf(DOT))
        {
            return host.substring(host.indexOf(DOT) + 1);
        }
        else
        {
            return host;
        }
    }

    private boolean isNotExpired(String cookieExpires)
    {
        if (cookieExpires == null) return true;

        Date now = new Date();
        try
        {
            return (now.compareTo(dateFormat.parse(cookieExpires))) <= 0;
        }
        catch (java.text.ParseException pe)
        {
            LOGGER.warn( "Could not parse cookie expiry date of " + cookieExpires, pe);
            return false;
        }
    }

    private boolean comparePaths(String cookiePath, String targetPath)
    {
        if (cookiePath == null)
        {
            return true;
        }
        else if (cookiePath.equals("/"))
        {
            return true;
        }
        else if (targetPath.regionMatches(0, cookiePath, 0, cookiePath.length()))
        {
            return true;
        }

        return false;

    }

    /**
     * Returns a string representation of stored cookies organized by domain.
     * @return the string representation
     */
    @Override @CheckReturnValue @Nonnull
    public String toString()
    {
        return store.toString();
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.internal.HttpCookieMgr");//#LOGGER-NOPMD
}
