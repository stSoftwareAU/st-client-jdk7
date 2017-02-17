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
package com.aspc.remote.util.misc;

import com.aspc.remote.util.misc.internal.HttpCookieMgr;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import javax.servlet.http.Cookie;
import org.apache.commons.logging.Log;

/**
 *  HttpUtil - utility class for processing HTTP requests
 * 
 * @version: $Revision: 1.27 $
 * @author Nigel Leck
 * @since 29 September 2006
 * 
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 */
public final class HttpUtil
{

    /**
     * check if the cookies are the same.
     * @param c1
     * @param c2
     * @return the value
     */
    public static boolean cookieSame( final Cookie c1, final Cookie c2)
    {
        String name=c1.getName();
        if( name.equals(c2.getName()))
        {
            String cookieValue=c1.getValue();
            if( cookieValue.equals(c2.getValue()))
            {
                String domain=c1.getDomain();
                if( 
                    (
                        StringUtilities.isBlank(domain) &&
                        StringUtilities.isBlank(c2.getDomain())
                    ) || 
                    ( 
                        domain != null && 
                        domain.equals(c2.getDomain())
                    )                            
                )
                {
                    int maxAge=c1.getMaxAge();
                    if( maxAge == c2.getMaxAge())
                    {
                        String path = c1.getPath();
                        if( 
                            (
                                StringUtilities.isBlank(path) &&
                                StringUtilities.isBlank(c2.getPath())
                            ) || 
                            ( 
                                path != null && 
                                path.equals(c2.getPath())
                            )                            
                        )
                        {
                            if( c1.getSecure() == c2.getSecure())
                            {
                                if( c1.getVersion() == c2.getVersion())
                                {
                                    if( c1.isHttpOnly() == c2.isHttpOnly())
                                    {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
   /**
   * Perform an HTTP GET to the supplied urlString
   * @param urlString the URL to call
   * @param requestHeaders a Map of the request header names and values to
   * be placed into the request
    * @param cookieMgr
     * @return the value
    * @throws HttpUtilException
   */
    public static String get(  
        final String urlString,
        final HashMap requestHeaders,
        final HttpCookieMgr cookieMgr
    ) throws HttpUtilException
    {        
        return post( urlString, requestHeaders, cookieMgr, null );
    }

    
   /**
   * Perform an HTTP POST to the supplied urlString with the supplied
   * requestHeaders and formParameters
   * @return String the response contents
   * @param urlString the URL to post to
   * @param requestHeaders a Map of the request header names and values to
   * be placed into the request
    * @param cookieMgr
    * @param formParameters a Map of form parameters and values to be placed
   * into the request
    * @throws HttpUtilException
   */
    public static String postContents(  
        final String urlString,
        final HashMap requestHeaders,
        final HttpCookieMgr cookieMgr,
        final HashMap formParameters 
            ) throws HttpUtilException
    {
        String requestContent = null;
        
        if( formParameters != null ) 
        {
            Set parameters = formParameters.keySet();
            Iterator it = parameters.iterator();
            StringBuilder buf = new StringBuilder();

            int paramCount =0;
            while( it.hasNext()) 
            {
                String parameterName = (String) it.next();
                String parameterValue = (String) formParameters.get( parameterName );

                if( parameterValue != null ) 
                {
                    try
                    {
                        parameterValue = URLEncoder.encode( parameterValue,"UTF-8");
                    }
                    catch( UnsupportedEncodingException ue)
                    {
                        throw new HttpUtilException( "Could not encode parameter " + parameterValue, ue);
                    }
                    
                    if( paramCount > 0 ) 
                    {
                        buf.append( "&" );
                    }
                    buf.append( parameterName );
                    buf.append( "=" );
                    buf.append( parameterValue );
                    ++paramCount;
                }                
            }
            requestContent = buf.toString();
        }
      
        return post( urlString, requestHeaders, cookieMgr, requestContent );
    }

  
    
    
  /**
   * Perform an POST to the supplied urlString with the supplied
   * requestHeaders and contents
   * @return String the response contents
   * @param urlString the URL to post to
   * @param requestHeaders a Map of the request header names and values to
   * be placed into the request
   * @param cookieMgr
   * @param requestContents the contents of the HTTP request
   * @throws HttpUtilException
   */
    public static String postContents( 
        final String urlString,
        final HashMap requestHeaders,
        final HttpCookieMgr cookieMgr,
        final String requestContents 
    ) throws HttpUtilException
    {
        return post( urlString, requestHeaders, cookieMgr, requestContents );
    }

  /**
   * Perform an HTTP POST to the supplied urlString with the supplied
   * requestHeaders and formParameters
   * @return String the response contents
   * @param urlString the URL to post to
   * @param requestHeaders a Map of the request header names and values to
   * be placed into the request
   * @param cookieMgr
   * @param requestContents the contents of the HTTP request
   * @throws HttpUtilException
   */
    public static String post( 
        final String urlString,
        final HashMap requestHeaders,
        final HttpCookieMgr cookieMgr,
        final String requestContents 
    ) throws HttpUtilException
    {
        // open url connection
        URL url = null;
        
        try
        {
            url = new URL( urlString );
        }
        catch( MalformedURLException mf)
        {
            throw new HttpUtilException( "Invalid url.", mf);
        }
        URLConnection connection = null;
        try
        {
            // Create connection
            connection = url.openConnection();
            HttpURLConnection httpConnection;
            if (connection instanceof HttpURLConnection == false) 
            {
                throw new HttpUtilException( "post - could not cast to HttpURLConnection", null);
            }
            
            httpConnection = (HttpURLConnection) connection;
            httpConnection.setInstanceFollowRedirects(true);
            // add all the request headers
            if( requestHeaders != null ) 
            {
                Set headers = requestHeaders.keySet();
                for( Iterator it = headers.iterator(); it.hasNext(); ) 
                {
                    String headerName = (String) it.next();
                    String headerValue = (String) requestHeaders.get( headerName );
                    connection.setRequestProperty( headerName, headerValue );
                }
            }
            if( cookieMgr != null)
            {
                cookieMgr.applyCookies( connection);
            }
            
            if( StringUtilities.isBlank( requestContents) == false)
            {
                setupConnectionForPost( httpConnection, requestContents);
            }

            InputStream is = null;

            try
            {    
                is = getInputStream(httpConnection);

                if( cookieMgr != null)
                {
                    cookieMgr.retrieveCookies( httpConnection);
                }
                
                return getContent( httpConnection, is);
            }
            finally
            {
                if( is != null)
                {
                    try
                    {                
                        is.close();
                    }
                    catch( IOException oe)
                    {
                        LOGGER.debug( "Could not close input stream", oe);
                        // Carry on any way
                    }
                }
            }
        }
        catch (IOException ioe) 
        {
            LOGGER.error( "IO error", ioe);
            throw new HttpUtilException( "IO error", ioe);
        }
        finally
        {
            if( connection instanceof HttpURLConnection) ((HttpURLConnection)connection).disconnect();
        }
    }
    
    private static void setupConnectionForPost(HttpURLConnection httpConnection, String requestContents ) throws IOException
    {
        // set up url connection to post information and
        // retrieve information back
        httpConnection.setRequestMethod( "POST" );
        httpConnection.setDoInput( true );
        httpConnection.setDoOutput( true );

        // add url form parameters
        DataOutputStream ostream = null;
        try 
        {
            ostream = new DataOutputStream( httpConnection.getOutputStream() );      
            if( requestContents != null ) 
            {
                ostream.writeBytes( requestContents );
            }
        }
        finally 
        {

            if( ostream != null ) 
            {
                try
                {                
                    ostream.flush();
                    ostream.close();
                }
                catch( IOException oe)
                {

                    LOGGER.debug( "Could not close output stream", oe);
                    // Carry on any way
                }
            }
        }
    }
    
    private static String getContent( final HttpURLConnection httpConnection, 
                                      final InputStream is) throws IOException
    {
         final int blockSize = 64;
            
         OutputStream out = null;
         
         try
         {
            out=new ByteArrayOutputStream(blockSize * 1024);
            httpConnection.setInstanceFollowRedirects(true);
            int contentLength=httpConnection.getHeaderFieldInt("Content-Length",-1);
            int bytesRead=0;

            byte[] buffer = new byte[blockSize * 1024];
            int length = is.read(buffer);

            while ((length) >= 0 && 
                (contentLength==-1 || bytesRead<contentLength)) 
            {
                bytesRead += length;
                out.write(buffer, 0, length);
                length = is.read(buffer);
            }

            String failureString=null;
            if(contentLength>-1 && bytesRead!=contentLength) 
            {
                failureString="Incomplete download -Expected "+contentLength
                                +"received "+bytesRead+" bytes";
            }
            else 
            {
               // if (httpConnection != null) 
               // {
                    int statusCode=httpConnection.getResponseCode();
                    if( statusCode == 302)
                    {
                        LOGGER.info(  "Redirect from " + httpConnection.getURL() );
                    }
                    else if( statusCode <200 || statusCode >299) 
                    {
                         failureString="Server error code "+statusCode+" received";
                    }
               // }
            }

            //check for an error message
            if( StringUtilities.isBlank( failureString) == false)
            {
                LOGGER.error( failureString);
                throw new IOException( failureString);
            }
            return out.toString();
        }
        finally
        {
            if( out != null)
            {
                try
                {                
                    out.flush();
                    out.close();
                }
                catch( IOException oe)
                {

                    LOGGER.debug( "Could not close output stream", oe);
                    // Carry on any way
                }
            }
        }
    }
    
    /** get an input stream from a connection
     * This code tries to fix a problem found in HttpURLConnection, that
     * any attempt to get the response code would trigger a FileNotFound
     */
    private static InputStream getInputStream(final HttpURLConnection httpConnection) throws IOException, HttpUtilException  
    {
        IOException lastException=null;
        InputStream instream=null;
        for (int attempts = 0; attempts < 5; attempts++) 
        {
            try 
            {
                instream = httpConnection.getInputStream();
                break;
            }
            catch (FileNotFoundException ex) 
            {
                LOGGER.debug( "Skipping FileNotFoundException in getInputStream", ex);
                lastException=ex;
            }
        }
        
        // Can't go any further if no result returned
        if (instream == null) 
        {
            int responseCode = -1;
            try
            {
                responseCode = getResponseCode(httpConnection);
            }
            catch( IOException e)
            {
                LOGGER.debug("Could not get response code when no input stream.", e);
            }
            String message = "URL could not be reached. HTTP " + responseCode + "-" + (lastException != null ?lastException.getMessage():"");
            LOGGER.error(message, lastException);
            throw new HttpUtilException( message, lastException);
        }
        
        return instream;
    }

    /* Get a response from a connection request.
     * This code fixes a problem found in HttpURLConnection, that
     * any attempt to get the response code would trigger a FileNotFound*/
    private static int getResponseCode(HttpURLConnection connection) throws IOException  
    {
        IOException lastException = null;
        boolean caught=false;
        int response=0;
        for (int attempts = 0; attempts < 5; attempts++) 
        {
            try 
            {
                response = connection.getResponseCode();
                caught=true;
                break;
            }
            catch (FileNotFoundException ex) 
            {
                LOGGER.debug( "Skipping FileNotFoundException in getResponseCode", ex);
                lastException=ex;
            }
        }
        if(!caught && lastException!=null) 
        {
            throw lastException;
        }
        return response;
    }

   /**
     * Private constructor
     */
    private HttpUtil()
    {        
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.HttpUtil");//#LOGGER-NOPMD
}
