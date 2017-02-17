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
package com.aspc.remote.rest.internal;

import com.aspc.remote.rest.ReST;
import com.aspc.remote.rest.Response;
import com.aspc.remote.rest.errors.ReSTException;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.FileUtil;
import java.net.URL;
import com.aspc.remote.util.misc.StringUtilities;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.regex.Pattern;
import javax.annotation.CheckReturnValue;
import javax.annotation.*;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;
import org.json.JSONObject;

/**
 *  ReST client which will cache requests for a given period.
 *
 *  <h3>Optimizations</h3>
 *  <ul>
 *      <li> Requests that are for data that we have an existing result less than the cache period will return immediately.
 *      <li> Requests that are for data that are half the age of the cache period will result in a "pre-fetch" request being made in the background.
 *      <li> Multiple requests for the same URL will result in only one request to the web server. All requests will get the same result when returned.
 *  </ul>
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Lei Gao
 *  @since       Jan 9, 2014
 */
public final class ReSTUtil
{
    /**
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/MAX_SAFE_INTEGER
     */
    public static final long MAX_SAFE_INTEGER=(((long)Math.pow(2, 53))-1L);
    
    /**
     * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/MIN_SAFE_INTEGER
     */
    public static final long MIN_SAFE_INTEGER=(((long)Math.pow(2, 53))-1L) * -1L;
        
    /**
     * Authorization header
     */
    public static final String HEADER_AUTHORIZATION="Authorization";

    private ReSTUtil()
    {

    }

    /**
     * http://mathiasbynens.be/demo/url-regex
     *
     * Validate ReST URLs
     */
    private static final Pattern HOST_PATTERN=Pattern.compile("^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$");
    
    /**
     * Maximum URL length is 2,083 characters in Internet Explorer.
     * 
     * https://support.microsoft.com/en-us/kb/208427
     */
    public static final int MAX_SAFE_URL_LENGTH=2048;
    
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.rest.internal.ReSTUtil");//#LOGGER-NOPMD
       
    /**
     * Check the URL is valid for a ReST call.
     * @param url the URL to check
     */
    public static void checkURL( final @Nonnull String url) throws IllegalArgumentException
    {
        checkURL(url, true);
    }
    
    /**
     * Check the URL is valid for a ReST call.
     * @param url the URL to check
     * @param checkUrlLength
     */
    public static void checkURL( final @Nonnull String url, final boolean checkUrlLength) throws IllegalArgumentException
    {
     if( url==null){
            throw new IllegalArgumentException( "URL is null");
        }

        if(checkUrlLength && url.length()>MAX_SAFE_URL_LENGTH)
        {
            throw new IllegalArgumentException( "URL londer than the safe limit " + MAX_SAFE_URL_LENGTH + " was " + url.length());
        }
        
        try
        {
            URL tmpURL=new URL( url);
            String protocol=tmpURL.getProtocol();
            
            if( protocol.equals("http")==false && protocol.equals("https")==false )
            {
                throw new IllegalArgumentException( "URL invalid protocol (" + protocol + "): " + StringUtilities.stripPasswordFromURL(url));
            }

            String host=tmpURL.getHost();

            if( StringUtilities.isBlank(host))
            {
                throw new IllegalArgumentException( "URL blank host: " + url);
            }

            if( HOST_PATTERN.matcher(host).matches()==false || host.matches(".*(--).*") || host.startsWith("0."))
            {
                throw new IllegalArgumentException( "URL invalid host (" + host +"): " + StringUtilities.stripPasswordFromURL(url));
            }

            if( host.matches("[0-9]+.*"))
            {
                String ips[]=host.split("\\.");
                if( ips.length != 4)
                {
                    throw new IllegalArgumentException( "URL invalid IP (" + host +"): " + StringUtilities.stripPasswordFromURL(url));
                }

                for( String ip: ips)
                {
                    try{
                        int range =Integer.parseInt(ip);
                        if( range <0||range > 255)
                        {
                            throw new IllegalArgumentException( "ReST URL invalid IP (" + host +"): " + StringUtilities.stripPasswordFromURL(url));
                        }
                    }
                    catch( NumberFormatException nf)
                    {
                        throw new IllegalArgumentException( "ReST URL invalid IP (" + host +"): " + StringUtilities.stripPasswordFromURL(url),nf);
                    }
                }
            }
            
            String path=tmpURL.getFile();
            if( path != null &&path.matches(".*[ ].*"))
            {
                throw new IllegalArgumentException( "ReST URL path should be encoded: " + StringUtilities.stripPasswordFromURL(url));
            }

            String query=tmpURL.getQuery();
            if( query != null &&query.matches(".*[ ].*"))
            {
                throw new IllegalArgumentException( "ReST URL query should be encoded: " + StringUtilities.stripPasswordFromURL(url));
            }

        }
        catch( MalformedURLException me)
        {
            throw new IllegalArgumentException( "ReST invalid URL: " + StringUtilities.stripPasswordFromURL(url), me);
        }
    }
    
    /**
     * Check the URL is valid for a ReST call.
     * @param url the URL to check
     * @return true if valid
     */
    @CheckReturnValue
    public static boolean validateURL( final @Nonnull String url)
    {
        return validateURL(url, true);
    }
    
    /**
     * Check the URL is valid for a ReST call.
     * @param url the URL to check
     * @param checkUrlLength
     * @return true if valid
     */
    @CheckReturnValue
    public static boolean validateURL( final @Nonnull String url, final boolean checkUrlLength)
    {      
        try{
            checkURL(url, checkUrlLength);
            return true;
        }
        catch( IllegalArgumentException iae)
        {
            LOGGER.warn( url, iae);
            return false;
        }        
    }

    /**
     * Check the URL is valid for a ReST call.
     * @param url the URL to check
     * @return true if valid
     */
    @CheckReturnValue
    public static boolean validateURL( final @Nonnull URL url)
    {
        return validateURL(url.toString());
    }

    /**
     * Check the URL is valid for a ReST call.
     * @param url the URL to check
     * @param checkUrlLength
     * @return true if valid
     */
    @CheckReturnValue
    public static boolean validateURL( final @Nonnull URL url, final boolean checkUrlLength)
    {
        return validateURL(url.toString(), checkUrlLength);
    }

    /**
     * Make a file name
     * @param url the URL
     * @param auth the authorization
     * @param agent the browser agent
     * @return a simple name
     */
    @CheckReturnValue @Nonnull
    public static String makeFileName(
        final @Nonnull URL url,
        final @Nullable ReSTAuthorizationInterface auth,
        final @Nullable String agent
    )
    {
        //if( transport == null ) throw new IllegalArgumentException("transport is null");
        if( url == null ) throw new IllegalArgumentException("url is null");
        String tmpURL=url.toString();
        if( StringUtilities.notBlank(agent))
        {
            if( tmpURL.contains("?"))
            {
                tmpURL+="&";
            }
            else
            {
                tmpURL+="?";
            }
            tmpURL+="_agent=" + agent;
        }

        String checkSum;
        String fileName = "/";
        fileName += url.getHost();
        fileName+="/";
        if( auth != null)
        {
            fileName += auth.toShortString().toLowerCase() + "@";
            checkSum=auth.checkSumAdler32(tmpURL);
        }
        else
        {
            checkSum=StringUtilities.checkSumAdler32(tmpURL);
        }

        String path=url.getPath();
        while( path.startsWith("/")) {
            path=path.substring(1);
        }
        fileName += path.trim().toLowerCase();

        String query = url.getQuery();
        if(query != null)
        {
            String[][] obscureList={{"key=","key=xxx"}};
            String[] args = query.split("&");
            StringBuilder sb=new StringBuilder();
            for( String arg:args)
            {
                if( sb.length()==0)
                {
                    sb.append("?");
                }
                else
                {
                    sb.append("&");
                }

                boolean obscure=false;
                for( String check[]: obscureList)
                {
                    if( arg.startsWith(check[0]))
                    {
                        sb.append(check[1]);
                        obscure=true;
                        break;
                    }
                }

                if( obscure==false)
                {
                    sb.append( arg);
                }
            }
            fileName+=sb.toString().toLowerCase();
        }
        fileName = fileName.replaceAll("[\\?&]", "/");

        fileName = fileName.replaceAll("[%$\\*:\\.\"'+\\s<>~]", "_");
        while( fileName.contains("//"))
        {
            fileName=fileName.replace("//", "/");
        }
        while( fileName.contains("__"))
        {
            fileName=fileName.replace("__", "_");
        }
        String[] split = fileName.split("/");
        StringBuilder sb=new StringBuilder( );
        for( String bit:split)
        {
            if(sb.length()>0) sb.append("/");
            if( bit.length()> FileUtil.MAX_FILE_NAME_LENGTH)
            {
                sb.append(bit.substring(0, FileUtil.MAX_FILE_NAME_LENGTH));
            }
            else
            {
                sb.append(bit);
            }
        }
        fileName=sb.toString();

        if( path.startsWith("/feed/"))
        {
            while( fileName.endsWith("_"))
            {
                fileName=fileName.substring(0, fileName.length()-1);
            }
            fileName+="/" +checkSum;
            fileName+=".rss";
        }
        else
        {
            while( fileName.endsWith("_"))
            {
                fileName=fileName.substring(0, fileName.length()-1);
            }
            String ext=".data";

            String knownExt[]={
                ".json",
                ".xml",
                ".html",
                ".htm"
            };
            String tmpFN=fileName.toLowerCase();
            for( String tmpExt: knownExt)
            {
                String t=tmpExt.replace(".", "_");
                if( tmpFN.endsWith(t))
                {
                    ext=tmpExt;
                    fileName=fileName.substring(0, fileName.length() - t.length());
                    break;
                }
            }
            fileName+="/" +checkSum;
            fileName+=ext;
        }
        return fileName;
    }

    /**
     * get json includes locations information for the given ip address
     * @param ip
     * @return json or null
     * @throws Exception 
     */
    @CheckReturnValue @Nullable
    public static JSONObject getGeoIp(final @Nonnull String ip) throws Exception
    {
        if(
            StringUtilities.IP_PATTERN.matcher(ip).matches() &&
            StringUtilities.LOCAL_IP_PATTERN.matcher(ip).matches() == false
        )
        {
            URL url = new URL("http://freegeoip.net/json/" + ip);
            Response res = ReST.builder(url).setMaxBlockPeriod("3 sec").setMinCachePeriod("3 month").getResponse();
            try
            {
                res.checkStatus();
                return res.getContentAsJSON();
            }
            catch(ReSTException | IOException e)
            {
                LOGGER.warn("can not get GEO of " + ip, e);
                return null;
            }
        }
        else
        {
            return null;
        }
    }
    @CheckReturnValue @Nonnull
    public static String getLocationCity(final @Nonnull String ip) throws Exception
    {
        JSONObject json = getGeoIp(ip);
        if(json != null)
        {
            return json.getString("city");
        }
        return "";
    }

    @CheckReturnValue @Nonnull
    public static String getLocationCoordinates(final @Nonnull String ip) throws Exception
    {
        JSONObject json = ReSTUtil.getGeoIp(ip);
        if(json != null)
        {
            return json.getString("latitude") + "," +json.getString("longitude") ;
        }
        return "";
    }
    
    @CheckReturnValue @Nonnull
    public static String getLocationCountryCode(final @Nonnull String ip) throws Exception
    {
        JSONObject json = ReSTUtil.getGeoIp(ip);
        if(json != null)
        {
            return json.getString("country_code");
        }
        return "";
    }
    
    @CheckReturnValue @Nonnull
    public static String getLocationRegion(final @Nonnull String ip) throws Exception
    {
        JSONObject json = ReSTUtil.getGeoIp(ip);
        if(json != null)
        {
            return json.getString("region_name");
        }
        return "";
    }

    @CheckReturnValue @Nonnull
    public static String getLocationZipCode(final @Nonnull String ip) throws Exception
    {
        JSONObject json = ReSTUtil.getGeoIp(ip);
        if(json != null)
        {
            return json.getString("zipcode");
        }
        return "";
    }
}
