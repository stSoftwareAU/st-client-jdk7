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
package com.aspc.remote.rest;

import com.aspc.remote.rest.internal.RestTransport;

import com.aspc.remote.database.InvalidDataException;
import com.aspc.remote.rest.errors.ReSTException;
import com.aspc.remote.rest.internal.Friend;
import com.aspc.remote.rest.internal.HttpRestTransport;
import com.aspc.remote.rest.internal.ReSTAuthorization;
import com.aspc.remote.rest.internal.ReSTAuthorizationInterface;
import com.aspc.remote.rest.internal.ReSTTask;
import com.aspc.remote.util.misc.DocumentUtil;

import com.aspc.remote.rest.internal.ReSTUtil;
import com.aspc.remote.rest.internal.RestCall;
import com.aspc.remote.rest.internal.Trace;
import com.aspc.remote.util.misc.CLogger;
import java.net.URL;
import com.aspc.remote.util.misc.FileUtil;
import com.aspc.remote.util.misc.StringUtilities;
import com.aspc.remote.util.misc.TimeUtil;
import com.aspc.remote.util.net.NetUrl;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import javax.annotation.*;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLProtocolException;
import org.apache.commons.logging.Log;
import org.w3c.dom.Document;

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
public final class ReST
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.rest.ReST");//#LOGGER-NOPMD
    private static final ConcurrentHashMap<File, com.aspc.remote.rest.internal.RestCall>PREFETCH=new ConcurrentHashMap();
    private static final Friend FRIEND=(File propertiesFile, RestCall call) -> {
        PREFETCH.remove(propertiesFile, call );        
    };
    
    private static final long DEFAULT_BLOCK_TIMEOUT=60L*60L*1000L;
    
    /**
     * http://en.wikipedia.org/wiki/Query_string
     */
    private static final Pattern QUERY_PATTERN=Pattern.compile("(([a-z0-9\\+\\._\\-~,\\*]|%[0-9a-f]{2})+((=([a-z0-9\\+\\._\\-~,\\*]|%[0-9a-f]{2})*)|&|))*", Pattern.CASE_INSENSITIVE);

    /**
     * Build a new ReST call for the past URL.
     * @param url the URL to call
     * @return the builder for this URL
     * @throws MalformedURLException the URL is not valid
     * @throws InvalidDataException the URL is not valid
     */
    @CheckReturnValue @Nonnull
    public static Builder builder(final @Nonnull String url) throws MalformedURLException, InvalidDataException
    {
        return new Builder(url, new HttpRestTransport());
    }

    /**
     * Build a new ReST call for the past URL.
     * @param url the URL to call
     * @return the builder for this URL
     * @throws InvalidDataException the URL is not valid
     */
    @CheckReturnValue @Nonnull
    public static Builder builder(final @Nonnull URL url) throws InvalidDataException
    {
        return new Builder(url, new HttpRestTransport());
    }

    @SuppressWarnings("PublicInnerClass") 
    public static class Builder 
    {
        private final URL url;
        @Nullable
        private LinkedHashMap<String, String[]> args;
        private long minCacheTimeToLiveMs;
        private long errorCacheTimeToLiveMs;        
        private int timeoutMs=-1;
        private int maxBlockMs=-1;
        private int staleBlockMs=-1;
        private String agent;
        private RestTransport transport;
        private Method method=null;
        private File body = null;
        private Document bodyXML=null;
        private boolean methodInUrl;
        private boolean disableURLLengthCheck = false;
        private boolean enableValidateCharactersInURL;
        private boolean disableGZIP = false;
        private ContentType contentType;
        private DispositionType dispositionType;
        
        private ReSTAuthorizationInterface auth;
        private Builder(final @Nonnull URL url, final @Nonnull RestTransport transport) throws InvalidDataException
        {
            if( url == null) throw new IllegalArgumentException( "URL is mandatory");

            if( ReSTUtil.validateURL(url, false) == false)
            {
                throw new InvalidDataException( StringUtilities.stripPasswordFromURL(url.toString()) + " not a valid ReST call");                
            }
            
            URL tmpURL=url;
            try
            {
                tmpURL=stripAuth(tmpURL);
            }
            catch( MalformedURLException me)
            {
                throw new InvalidDataException( StringUtilities.stripPasswordFromURL(url.toString()) + " not a valid ReST call",me);
            }
            
            scanForMethod(tmpURL);

            try
            {
                tmpURL=parseParameters(tmpURL);
            }
            catch( MalformedURLException me)
            {
                throw new InvalidDataException( StringUtilities.stripPasswordFromURL(url.toString()) + " not a valid ReST call",me);
            }

            this.url=tmpURL;
            this.transport=transport;
        }

        private Builder(final @Nonnull String url, final @Nonnull RestTransport transport) throws InvalidDataException
        {
            if( StringUtilities.isBlank(url)) throw new IllegalArgumentException( "URL is mandatory");
            
            if( ReSTUtil.validateURL(url, false) == false)
            {
                throw new InvalidDataException( StringUtilities.stripPasswordFromURL(url) + " not a valid ReST call");                
            }
            
            URL tmpURL;
            try
            {
                tmpURL=new URL(url);
                tmpURL=stripAuth(tmpURL);
            }
            catch( MalformedURLException me)
            {
                throw new InvalidDataException( StringUtilities.stripPasswordFromURL(url) + " not a valid ReST call",me);
            }
            
            scanForMethod(tmpURL);
            
            try
            {
                tmpURL=parseParameters(tmpURL);
            }
            catch( MalformedURLException me)
            {
                throw new InvalidDataException( StringUtilities.stripPasswordFromURL(url) + " not a valid ReST call",me);
            }
            
            this.url = tmpURL;
            this.transport=transport;
        }
        
        @CheckReturnValue @Nonnull
        private URL parseParameters( final @Nonnull URL url) throws MalformedURLException
        {
            String q=url.getQuery();
            
            if( q == null )
            {
                return url;
            }
            
            args=new LinkedHashMap<>();
            
            for( String parameter: q.split("&"))
            {
                String name;
                String[] valueList={""};
                int pos=parameter.indexOf("=");
                
                if( pos != -1)
                {
                    name=StringUtilities.decode(parameter.substring(0, pos));
                    String tmpEncodedValues=parameter.substring(pos + 1);
                    ArrayList<String> list=new ArrayList();
                    for( String encodedValue:tmpEncodedValues.split(","))
                    {
                        list.add(StringUtilities.decode(encodedValue));
                    }
                    valueList=new String[list.size()];
                    list.toArray(valueList);
                }
                else
                {
                    name=StringUtilities.decode(parameter);
                }
                
                String fullValues[]=args.get(name);
                if( fullValues == null)
                {
                    fullValues= valueList;                
                }
                else
                {
                    List<String> list=new ArrayList<>(Arrays.asList(fullValues));
                    for( String tmpValue:valueList)
                    {
                        if( tmpValue != null && tmpValue.length()!=0)
                        {
                            list.add(tmpValue);                
                        }
                    }
                    fullValues = new String[list.size()];
                    list.toArray(fullValues);
                }
                
                args.put(name, fullValues);
            }
            
            String tmp=url.toString();
            int pos = tmp.indexOf("?");
            URL tmpURL=new URL( tmp.substring(0, pos));
            
            return tmpURL;
        }
        
        @CheckReturnValue @Nonnull
        private URL stripAuth( final @Nonnull URL url) throws MalformedURLException
        {
            String authority=url.getAuthority();
            int pos = authority.indexOf('@');
           
            if( pos != -1)
            {
                String temp=authority.substring(0, pos);
                String user;
                String passwd="";
                String domain=null;
                pos=temp.indexOf(':');
                if( pos != -1)
                {
                    user=StringUtilities.decode(temp.substring(0, pos));
                    passwd=StringUtilities.decode(temp.substring(pos + 1));
                }
                else
                {
                    user=StringUtilities.decode(temp);
                }
                pos=user.indexOf('\\');
                if( pos != -1)
                {
                    domain=user.substring(0, pos);
                    user=user.substring(pos + 1);
                }
                
                auth=new ReSTAuthorization(user, passwd, domain);
                
                URL realURL=new URL( url.getProtocol(), url.getHost(), url.getPort(), url.getFile());
                
                return realURL;
            }
            return url;
        }
        
        @CheckReturnValue @Nonnull
        private void scanForMethod( final @Nonnull URL url)
        {
            String q=url.getQuery();

            if( q!=null)
            {
//                String q2=q;
                int lastPos=-1;
                while(true)
                {
                    int pos=q.indexOf(METHOD_PARAMETER,lastPos);
                    if( pos ==-1) break;
                    lastPos=pos+8;
                    
                    if( pos > 0)
                    {
                        if( q.charAt(pos -1) != '&')
                        {
                            continue;
                        }
                    }
                    
                    int end=q.indexOf("&", pos);
                    
                    if( end==-1)
                    {
                        method=Method.valueOf(q.substring(pos + METHOD_PARAMETER.length()));
                        methodInUrl=true;
                        break;
                    }
                    else
                    {
                        method=Method.valueOf(q.substring(pos + METHOD_PARAMETER.length(),end));
                        methodInUrl=true;
                    }
                }
                    
            }
        }
        
        private static final String METHOD_PARAMETER = "_method=";
        
        /**
         * sets the user/password to use for this call.
         *
         * @param user the user
         * @param passwd
         * @return this builder
         * @throws InvalidDataException the duration is not valid
         */
        public @Nonnull Builder setAuthorization( final @Nonnull String user, final @Nonnull String passwd) throws InvalidDataException
        {
            auth=new ReSTAuthorization(user, passwd, null);

            return this;
        }

        /**
         * sets the authorization token to use for this call.
         *
         * @param token the authorization token
         * @return this builder
         * @throws InvalidDataException the duration is not valid
         */
        public @Nonnull Builder setAuthorization( final @Nonnull String token) throws InvalidDataException
        {
            auth=new ReSTAuthorization(token);

            return this;
        }

        /**
         * sets the user/password to use for this call.
         *
         * @param user the user
         * @param passwd
         * @param domain the domain
         * @return this builder
         * @throws InvalidDataException the duration is not valid
         */
        public @Nonnull Builder setAuthorization( final @Nonnull String user, final @Nonnull String passwd, final @Nullable String domain) throws InvalidDataException
        {
            auth=new ReSTAuthorization(user, passwd, domain);

            return this;
        }
        
        /**
         * sets the customized authorization to use for this call.
         *
         * @param auth the customized authorization
         * @return this builder
         * @throws InvalidDataException the duration is not valid
         */
        public @Nonnull Builder setAuthorization( final @Nonnull ReSTAuthorizationInterface auth) throws InvalidDataException
        {
            this.auth = auth;

            return this;
        }
        
        /**
         * Set the body of this POST/PUT request.
         * @param body the body to send.
         * @return this
         * @throws InvalidDataException The method must not be GET
         */
        public @Nonnull Builder setBody( final @Nullable File body) throws InvalidDataException
        {
            if( body == null)
            {
                this.body=null;
                return this;
            }
            else
            {
                return setBody(body, ContentType.APPLICATION_OCTET_STREAM, DispositionType.FORM_DATA);
            }
        }

        /**
         * Set the body of this POST/PUT request.
         * @param body the body to send.
         * @param contentType the content type to send
         * @param dispositionType the disposition type. 
         * @return this
         * @throws InvalidDataException The method must not be GET
         */
        public @Nonnull Builder setBody( 
            final @Nonnull File body, 
            final @Nonnull ContentType contentType,
            final @Nonnull DispositionType dispositionType 
        ) throws InvalidDataException
        {
            if( body==null) throw new IllegalArgumentException("body file is mandatory");
            if( contentType==null) throw new IllegalArgumentException("Content Type is mandatory");
            if( dispositionType==null) throw new IllegalArgumentException("Disposition Type is mandatory");
            
            if(method == Method.GET)
            {
                throw new InvalidDataException("body is not allowed for method GET");
            }
            
            this.body = body;
            this.contentType=contentType;
            this.dispositionType=dispositionType;
            return this;
        }
        
        /**
         * Set the body of this POST/PUT request.
         * @param bodyXML the body to send.
         * @return this
         * @throws InvalidDataException The method must not be GET
         */
        public @Nonnull Builder setBody( final @Nullable Document bodyXML) throws InvalidDataException
        {
            if(method == Method.GET && bodyXML != null)
            {
                throw new InvalidDataException("body is not allowed for method GET");
            }
            this.bodyXML = bodyXML;
            return this;
        }
       
        @CheckReturnValue 
        private boolean hasBody()
        {
            return body!=null || bodyXML!=null;
        }
        
        public @Nonnull Builder setDisableURLLenghCheck( final boolean disable)
        {
            this.disableURLLengthCheck = disable;
            return this;
        }
        
        public @Nonnull Builder setEnableValidateCharactersInURL( final boolean enable)
        {
            this.enableValidateCharactersInURL = enable;
            return this;
        }
        /**
         * true to NOT accept GZIP encoding, default is false
         * @param disableGZIP
         * @return 
         */
        public @Nonnull Builder setDisableGZIP(final boolean disableGZIP)
        {
            this.disableGZIP = disableGZIP;
            return this;
        }
        
        /**
         * The maximum time to wait. If the request takes too long, a response with Status C504_TIMED_OUT_GATEWAY will return.
         *
         * @param timeoutPeriod the maximum time to block
         * @return this builder
         * @throws InvalidDataException the duration is not valid
         */
        public @Nonnull Builder setTimeout( final @Nullable String timeoutPeriod) throws InvalidDataException
        {
            if(StringUtilities.isBlank(timeoutPeriod))
            {
                timeoutMs=-1;
                return this;
            }
            long tmpMS=TimeUtil.convertDurationToMs(timeoutPeriod);
            if( tmpMS > Integer.MAX_VALUE)
            {
                throw new InvalidDataException("block period greater than max allowed");
            }

            if( tmpMS>0 &&staleBlockMs>0)
            {
                throw new IllegalStateException("can not set timeout & stale block period");
            }
                        
            if( tmpMS>0 &&maxBlockMs>0)
            {
                throw new IllegalStateException("can not set timeout & max block period");
            }
                        
            timeoutMs = (int)tmpMS;

            return this;
        }
        
        /**
         * Set the maximum time we are prepared to wait for a NEW result. If the call times out the previous result will be used. 
         * 
         * The request will block forever(max request time ~60 mins) if there is no old cache file. The max block period only has effect when there is a cached version available.
         *
         * @param maxBlockPeriod the maximum time to block
         * @return this builder
         * @throws InvalidDataException the duration is not valid
         */
        public @Nonnull Builder setMaxBlockPeriod( final @Nullable String maxBlockPeriod) throws InvalidDataException
        {
            if( method != null && method != Method.GET)
            {
                throw new InvalidDataException("block period only allowed for method GET");
            }
            
            long tmpMS=TimeUtil.convertDurationToMs(maxBlockPeriod);
            if( tmpMS > Integer.MAX_VALUE)
            {
                throw new InvalidDataException("block period greater than max allowed");
            }
                        
            if( staleBlockMs>0 && tmpMS>staleBlockMs)
            {
                throw new InvalidDataException("MAX block " + maxBlockPeriod + " is greater than the stale block " + TimeUtil.getDiff(0, staleBlockMs));
            }
            
            if( timeoutMs>0 && tmpMS>0)
            {
                throw new IllegalStateException("can not set timeout & max block period");
            }
            
            maxBlockMs = (int)tmpMS;

            return this;
        }
        
        /**
         * The block time before we will use a stale version ( previously cached) of the request response. The stale block time must be greater or equal to the max block period.
         * 
         * If there is no cache avaliable for the request then a response with C504_TIMED_OUT_GATEWAY will be returned.
         * 
         * @param staleBlockPeriod the maximum time to block.
         * @return this builder
         * @throws InvalidDataException the duration is not valid
         */
        public @Nonnull Builder setStaleBlockPeriod( final @Nullable String staleBlockPeriod) throws InvalidDataException
        {
            if( method != null && method != Method.GET)
            {
                throw new InvalidDataException("stale block period only allowed for method GET");
            }

            long tmpMS=TimeUtil.convertDurationToMs(staleBlockPeriod);
            if( tmpMS > Integer.MAX_VALUE)
            {
                throw new InvalidDataException("block period greater than max allowed");
            }
            
            if( maxBlockMs>0 && tmpMS<maxBlockMs && tmpMS>0)
            {
                throw new InvalidDataException("stale block " + staleBlockPeriod + " less than the MAX block " + TimeUtil.getDiff(0, maxBlockMs));
            }
                        
            if( timeoutMs>0 && tmpMS>0)
            {
                throw new IllegalStateException("can not set timeout & stale block period");
            }
            
            staleBlockMs = (int)tmpMS;

            return this;
        }
        
        /**
         * Set the method to use
         *
         * @param method the method.
         * 
         * @return this builder
         */
        public @Nonnull Builder setMethod( final @Nullable String method) throws IllegalArgumentException
        {
            return setMethod(Method.valueOf(method));
        }    
        
        /**
         * Get the call method
         *
         * @return the method
         */
        public @Nonnull Method getMethod( )
        {
            return method;
        }
        
        /**
         * Set the method to use
         *
         * @param method the method.
         * 
         * @return this builder
         */
        public @Nonnull Builder setMethod( final @Nullable Method method) throws IllegalArgumentException
        {
            if( methodInUrl && method != this.method && method!=Method.POST)
            {
                throw new IllegalArgumentException("conflicting method (" + this.method + ") set in the URL");
            }
            
            if(method == Method.GET && hasBody())
            {
                throw new IllegalArgumentException("body is not allowed for method GET");
            }

            if( method != Method.GET)
            {
                if( errorCacheTimeToLiveMs >0 || maxBlockMs > 0 || minCacheTimeToLiveMs > 0 || staleBlockMs > 0)
                {
                    throw new IllegalArgumentException("block/caching periods only allowed for method GET");
                }
            }

            this.method = method;

            return this;
        }
        
        /**
         * Set the agent to use
         *
         * @param agent the agent.
         * 
         * @return this builder
         */
        public @Nonnull Builder setAgent( final @Nullable String agent)
        {
            this.agent = agent;

            return this;
        }

        /**
         * A ampersand(&) separated list of encoded name/value pairs. 
         * @param query the <b>encoded</b> name and value pairs to be added to the call. 
         * @return this
         */
        @SuppressWarnings("null")
        public @Nonnull Builder addQuery( final @Nonnull String query) throws IllegalArgumentException
        {
            if( query == null || QUERY_PATTERN.matcher(query).matches()==false)
            {
                throw new IllegalArgumentException( "not correctly encoded query: " + query);
            }
            
            for( String pair:query.split("&"))
            {
                int pos = pair.indexOf('=');
                String ename=pair;
                String evalue="";

                if( pos != -1)
                {
                    ename=pair.substring(0, pos);
                    evalue=pair.substring( pos + 1);
                }

                addParameter( StringUtilities.decode(ename), StringUtilities.decode(evalue) );
            }
            
            return this;
        }
        
        /**
         * Add a parameter 
         * @param name the name of the parameter
         * @param value the long value
         * @return this
         */
        public @Nonnull Builder addParameter( final @Nonnull String name, final long value)
        {
            return addParameter(name, Long.toString(value));
        }
        
        /**
         * set a parameter (decoded)
         * @param name the name of the parameter
         * @param value the value
         * @return this
         */
        public @Nonnull Builder setParameter( final @Nonnull String name, final @Nullable String value)
        {
            if( args == null)
            {
                args=new LinkedHashMap<>();
            }
            String values[]= new String[]{value};                
            
            args.put(name, values);
            
            return this;
        }
        
        /**
         * set a boolean parameter
         * @param name the name of the parameter
         * @param value the value
         * @return this
         */
        public @Nonnull Builder setParameter( final @Nonnull String name, final boolean value)
        {
            if( args == null)
            {
                args=new LinkedHashMap<>();
            }
            String values[]= new String[]{Boolean.toString(value)};                
            
            args.put(name, values);
            
            return this;
        }
        
        /**
         * set a parameter
         * @param name the name of the parameter
         * @param value the value
         * @return this
         */
        public @Nonnull Builder setParameter( final @Nonnull String name, final long value)
        {
            return setParameter(name, Long.toString(value));
        }
        
        /** 
         * Return the current parameter values.
         * @param name the parameter name
         * @return the values or NULL if not set.
         */
        public @Nullable String[] getParameterValues( final @Nonnull String name)
        {
            if( args==null)
            {
                return null;
            }
            
            String values[]=args.get( name);
            if( values != null)
            {
                return values.clone();
            }
            
            return null;
        }
        
        /**
         * Add a parameter (decoded)
         * @param name the name of the parameter
         * @param value the value
         * @return this
         */
        public @Nonnull Builder addParameter( final @Nonnull String name, final @Nullable String value)
        {
            if( args == null)
            {
                args=new LinkedHashMap<>();
            }
            String values[]=args.get( name);
            if( values == null)
            {
                if( value==null || value.length()==0)
                {
                    values=new String[]{};
                }
                else
                {
                    values= new String[]{value};                
                }                
            }
            else
            {
                List<String> list=new ArrayList<>(Arrays.asList(values));
                if( value != null && value.length()!=0)
                {
                    list.add(value);                
                }
                
                values = new String[list.size()];
                list.toArray(values);
            }
            
            args.put(name, values);
            
            return this;
        }
        
        /**
         * Set the minimum cache period for this request.
         * 
         * The server cache period sent will <b>extend</b> this minimum cache period if sent. 
         * 
         * @param cachePeriod how long to remember the response for this URL.
         * @return this builder
         * @throws InvalidDataException the duration is not valid
         */
        public @Nonnull Builder setMinCachePeriod( final @Nullable String cachePeriod) throws InvalidDataException
        {
            long cachePeriodMS=TimeUtil.convertDurationToMs(cachePeriod);
            if( cachePeriodMS>0 && method != null && method != Method.GET)
            {
                throw new InvalidDataException("cache period only allowed for method GET");
            }

            minCacheTimeToLiveMs = cachePeriodMS;

            return this;
        }
        
        /**
         * Set the ERROR cache period for this request
         * @param cachePeriod how long to remember this URL returns an error.
         * @return this builder
         * @throws InvalidDataException the duration is not valid
         */
        public @Nonnull Builder setErrorCachePeriod( final @Nullable String cachePeriod) throws InvalidDataException
        {
            if( method != null && method != Method.GET)
            {
                throw new InvalidDataException("error cache period only allowed for method GET");
            }

            errorCacheTimeToLiveMs = TimeUtil.convertDurationToMs(cachePeriod);
            return this;
        }
        
        /**
         * Sets the underlying transport for this ReST call. 
         * 
         * @param transport the transport to use.
         * @return this builder
         */
        public @Nonnull Builder setTransport(final @Nullable RestTransport transport)
        {
            this.transport = transport;
            return this;
        }

       /**
        * Fetch a ReST request
        * @return the resulting data
        * @throws IOException a serious problem.
        */
        @CheckReturnValue
        public @Nonnull String getContentAsString() throws IOException
        {           
            Response rr = getResponse();

            return rr.getContentAsString();
        }

        /**
         * make the cache file name.
         * @return the file name.
         */
        @CheckReturnValue
        public @Nonnull String makeFileName()
        {
            return ReSTUtil.makeFileName(makeRealURL(), auth, agent);
        }
        
        public static URL correctURL(final URL url)
        {
            URL realURL=url;
            String checkURL =realURL.toString();
            StringBuilder sb=new StringBuilder( checkURL.length());
            boolean changed=false;
            for( char c: checkURL.toCharArray())
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
                LOGGER.warn( "Invalid URL: " + checkURL);
                try {                
                    realURL=new URL( sb.toString());
                } catch (MalformedURLException ex) {
                   throw new RuntimeException( sb.toString(), ex);
                }
            }
            return realURL;
        }
       
        private URL makeRealURL()
        {
            URL realURL=url;
            if(enableValidateCharactersInURL==false)
            {
                realURL = correctURL(url);
            }
            
            if( args != null)
            {
                StringBuilder sb=new StringBuilder(realURL.toString());
                
                boolean started=sb.indexOf("?") != -1;
                for( String name: args.keySet())
                {
                    if( started)
                    {
                        sb.append("&");
                    }
                    else
                    {
                        started=true;
                        sb.append( "?");
                    }

                    String values[]=args.get(name);

                    if( values.length==0)
                    {
                        sb.append(StringUtilities.encode( name));
                    }
                    else
                    {
                        sb.append(StringUtilities.encode( name));
                        sb.append("=");
                        boolean startedValue=false;
                        for( String value:values)
                        {
                            if( startedValue)
                            {
                                sb.append(",");
                            }
                            else
                            {
                                startedValue=true;
                            }
                            sb.append(StringUtilities.encode( value));
                        }
                    }
                }
                try {                
                    realURL=new URL( sb.toString());
                } catch (MalformedURLException ex) {
                   throw new RuntimeException( sb.toString(), ex);
                }
            }
            if( LOGGER.isDebugEnabled())
            {
                LOGGER.debug( "Real URL: " + realURL);
            }
            return realURL;
        }

        @Override @CheckReturnValue @Nonnull
        public String toString() {
            StringBuilder sb;
            sb = new StringBuilder("Builder{").append( StringUtilities.stripPasswordFromURL(url.toString()) );
            
            if( args!=null)
            {
                
                for( String key: args.keySet())
                {
                    sb.append( ", " );
                    sb.append(key);
                    boolean started=false;
                    for( String value:args.get(key))
                    {
                        if( started==false)
                        {
                            sb.append("=");
                            started=true;
                        }
                        else
                        {
                            sb.append("|");
                        }
                        sb.append(value);
                    }                
                }
            }
            if(method != Method.GET && method !=null)
            {
                sb.append(", _method=");
                sb.append(method);
            }
            sb.append('}');
            
            return sb.toString();
        }

        /**
         * Call the server and return the response. Note you must call checkStatus() determine if the call actually worked. 
         * 
         * This is the heart of the ReST calls. Before calling the remote server it first checks the local disk cache for the matching request property file. 
         * The rest call property file contains the checksum of the results file plus a history of results files for later clean up. If the result file is 
         * present and it matches the checksum of when it was cached and the cache period hasn't expired then the previous result can be returned without even 
         * calling the server.
         * 
         * If the server is to be called then a future task is scheduled for this URL. Other call for the same URL that is made while the server is processing 
         * this call will be joined together as one.
         * 
         * Previous result files will be cleaned removed only after a period of time has expired to handle possible race conditions when there are multiple 
         * calls for the same resource. 
         * 
         * @return the response object.
         */        
        @CheckReturnValue @Nonnull
        public Response getResponse()
        {
            Thread ct=Thread.currentThread();
            String tn=ct.getName();
            try{
                ct.setName(toString());
                if(method == null)
                {
                    method = Method.GET;
                }
                if( method==Method.GET)
                {
                    return readResponse();
                }
                else
                {
                    return writeResponse();
                }
            }
            finally
            {
                ct.setName(tn);
            }
        }
        
        /**
         * Short hand method to call getResponse() & checkStatus()
         * @return the response
         * @throws FileNotFoundException
         * @throws ReSTException 
         */
        @CheckReturnValue @Nonnull
        public Response getResponseAndCheck() throws FileNotFoundException, ReSTException
        {
            Response r =getResponse();
            
            r.checkStatus();
            
            return r;
        }
        
        @CheckReturnValue
        private Response makeCacheResponse(final @Nonnull File propertiesFile, final @Nonnull AtomicLong TTL)
        {
//            long tmpCacheTimeToLiveMs=minCacheTimeToLiveMs;
            if( propertiesFile.exists()) {
                Properties p = new Properties();
                try {
                    try (FileReader fr = new FileReader(propertiesFile)) {
                        p.load(fr);
                    }
                    String cacheControl = p.getProperty("cache-control", "");
                    if (errorCacheTimeToLiveMs > 0 || minCacheTimeToLiveMs > 0 || cacheControl.contains("max-age")||maxBlockMs>0||staleBlockMs>0) {
                        String fileList[] = p.getProperty(RestTransport.FILE_LIST, "").split(",");
                        String cacheFileName;
                        cacheFileName = fileList[fileList.length - 1];
                        if (StringUtilities.notBlank(cacheFileName)) {
                            String sha1 = p.getProperty(RestTransport.RESULTS_SHA1);

                            if (StringUtilities.notBlank(sha1)) {
                                File cachedFile = new File(propertiesFile.getParentFile(), cacheFileName);

                                if (cachedFile.isFile()) {
                                    String tmpCS = new String(StringUtilities.encodeBase64(FileUtil.generateSHA1(cachedFile)));
                                    if (tmpCS.equals(sha1)) {
                                        String mimetype = p.getProperty(RestTransport.MIME_TYPE);
                                        Status status = Status.find(Integer.parseInt(p.getProperty(RestTransport.STATUS, "200")));
//                                        String cacheControl=null;
                                        if (status.isError()) {
                                            TTL.set(errorCacheTimeToLiveMs);
                                        } else {
                                            TTL.set(minCacheTimeToLiveMs);
//                                            cacheControl=p.getProperty("cache-control");
                                            if (StringUtilities.notBlank(cacheControl)) {
                                                if (cacheControl.contains("max-age")) {
                                                    int equalsPos = cacheControl.indexOf("=");
                                                    if (equalsPos != -1) {
                                                        String maxAge = cacheControl.substring(equalsPos + 1);
                                                        int commaPos = maxAge.indexOf(",");
                                                        if (commaPos != -1) {
                                                            maxAge = maxAge.substring(0, commaPos);
                                                        }
                                                        maxAge = maxAge.trim();
                                                        if (maxAge.matches("[0-9]+")) {
                                                            long serverCacheTime = Long.parseLong(maxAge) * 1000L;

                                                            if (minCacheTimeToLiveMs < serverCacheTime) {
                                                                TTL.set( serverCacheTime);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        Response.Builder rb = Response.builder(status, mimetype, cachedFile).setTrace(Trace.CACHED);
                                        switch (status) {
                                            case C301_REDIRECT_MOVED_PERMANENTLY:
                                            case C302_REDIRECT_FOUND:
                                            case C303_REDIRECT_SEE_OTHER:
                                                rb.setRedirection(p.getProperty(RestCall.HEADER_LOCATION));
                                        }
                                        if (cacheControl != null) {
                                            rb.setCacheControl(cacheControl);
                                        }
                                        return rb.make();

                                    } else {
//                                        tmpCacheTimeToLiveMs = -1;
                                        LOGGER.warn("Cache SHA1 was: " + tmpCS + "expected: " + sha1);
                                    }
                                } else {
//                                    tmpCacheTimeToLiveMs = -1;
                                    LOGGER.warn("No cache file missing " + cachedFile);
                                }
                            } else {
//                                tmpCacheTimeToLiveMs = -1;
                                LOGGER.warn("No cache file specified ");
                            }
                        } else {
//                            tmpCacheTimeToLiveMs = -1;
                            LOGGER.warn("No SHA1");
                        }
                    }
                } catch (IOException io) {
                    LOGGER.warn("could not load ", io);
//                    tmpCacheTimeToLiveMs = -1;
                }
            }
            
            return null;
        }
        
        @SuppressWarnings("ThrowableResultIgnored") @CheckReturnValue @Nonnull
        private Response readResponse()
        {
            long nowTime = System.currentTimeMillis();

            URL realURL=makeRealURL();
            
            if(enableValidateCharactersInURL)
            {
                String validationMessage=validateCharactersInURL(realURL);
                if( StringUtilities.notBlank(validationMessage))
                {
                    return Response.builder(Status.C400_ERROR_BAD_REQUEST,"text/plan", validationMessage ).make();                
                }
            }
            
            if(disableURLLengthCheck == false)
            {
                int urlLen=realURL.toString().length();
                if( urlLen>ReSTUtil.MAX_SAFE_URL_LENGTH)
                {
                    return Response.builder(Status.C414_REQUEST_URI_TOO_LARGE,"text/plan", "URL over the safe limit of " + ReSTUtil.MAX_SAFE_URL_LENGTH + " was " + urlLen ).make();                
                }
            }
            String feedPath = FileUtil.getCachePath() + "/rest/" + transport.getRootFolderName();
            String fileName = ReSTUtil.makeFileName( realURL, auth, agent);
            int pos = fileName.lastIndexOf('.');
            File propertiesFile = new File(feedPath + fileName.substring(0, pos) + ".properties");

            Response rr;
            
            AtomicLong tmpCacheTimeToLiveMS=new AtomicLong(-1);
            rr=makeCacheResponse( propertiesFile, tmpCacheTimeToLiveMS);
//            if( rr!=null) return rr;

            long modTS=propertiesFile.lastModified();
            long expireTS=modTS+tmpCacheTimeToLiveMS.get();
            if( rr==null || tmpCacheTimeToLiveMS.get()<=0 || propertiesFile.exists() == false || (expireTS - ( tmpCacheTimeToLiveMS.get()/2)) < nowTime )
            {
                assert timeoutMs<0||timeoutMs>0&&staleBlockMs<1&&maxBlockMs<1:"Should not set timeout and other timeouts";
                int callTimeout=Math.max(Math.max(timeoutMs, Math.max( staleBlockMs, (rr!=null ?maxBlockMs:-1))),0);
                
                callTimeout*=2;
                
                com.aspc.remote.rest.internal.RestCall call = transport.makeRestCall(
                    method,
                    realURL, 
                    auth, 
                    agent, 
                    propertiesFile,
                    null,
                    callTimeout,
                    disableGZIP,
                    FRIEND,
                    contentType,
                    dispositionType
                );

                /** Are we already processing this GET request ? */
                com.aspc.remote.rest.internal.RestCall call2 = PREFETCH.putIfAbsent(propertiesFile, call);

                try
                {
                    com.aspc.remote.rest.internal.RestCall currentCall;

                    if( call2 ==null)
                    {
                        currentCall=call;
                        ReSTTask fjt = ReSTTask.submit(call, DEFAULT_BLOCK_TIMEOUT);
                        
                        call.fjt=fjt;
                    }
                    else
                    {
                        currentCall=call2;
                        if( call2.timeoutMS> 0 && call2.timeoutMS < call.timeoutMS)
                        {
                            if( call2.fjt != null )
                            {
                                try
                                {
                                    call2.fjt.get(call.timeoutMS).status.check();
                                }
                                catch( InterruptedException | ExecutionException | FileNotFoundException | ReSTException e)
                                {
                                    LOGGER.warn("switched to longer timeout", e);
                                    currentCall=call;
                                }                                            
                            }
                        }

                        if( currentCall.fjt==null)
                        {
                            /* Switched to longer timeout */
                            if( currentCall != call)
                            {
                                /** The other thread should be setting it. */
                                Thread.sleep(100);
                            }
                            
                            if( currentCall.fjt==null)
                            {
                                ReSTTask fjt = ReSTTask.submit(currentCall, DEFAULT_BLOCK_TIMEOUT);
                                currentCall.fjt=fjt;
                            }
                        }
                    }

                    if( propertiesFile.exists() && expireTS > nowTime && rr != null )
                    {
                        Response.Builder rb = Response.builder(rr.status, rr.mimeType, rr.getContentAsFile()).setTrace( Trace.PREFETCH);
                        switch( rr.status)
                        {
                            case C301_REDIRECT_MOVED_PERMANENTLY:
                            case C302_REDIRECT_FOUND:
                            case C303_REDIRECT_SEE_OTHER:
                                rb.setRedirection(rr.redirection);
                        }
                        rr = rb.make();
                        LOGGER.info( "prefetch next version due in " + TimeUtil.getDiff(nowTime, expireTS) + " for " + realURL  );
                    }
                    else
                    {
                        if( timeoutMs>0)
                        {
                            try
                            {
                                rr=currentCall.fjt.get(timeoutMs);
                            }
                            catch( TimeoutException toe)
                            {
                                LOGGER.warn( realURL.toString(), toe);
                    
                                rr=Response.builder(Status.C504_TIMED_OUT_GATEWAY, "text/plan",toe.toString()).make();
                            }
                        }
                        else if( rr != null && rr.getContentAsFile().exists() )
                        {
                            try
                            {
                                if( staleBlockMs > 0)
                                {
                                    rr=currentCall.fjt.get(staleBlockMs);
                                }
                                else if( maxBlockMs > 0)
                                {
                                    rr=currentCall.fjt.get(maxBlockMs);
                                }
                                else
                                {
                                    rr=currentCall.fjt.get(DEFAULT_BLOCK_TIMEOUT);
                                }
                            }
                            catch( TimeoutException toe)
                            {
                                rr = Response.builder(rr.status, rr.mimeType, rr.getContentAsFile()).setRedirection(rr.redirection).setTrace( Trace.STALE).make();                                

                                LOGGER.warn( "Old (" + TimeUtil.getDiff(modTS) + ") version used of " + realURL, toe);
                            }
                        }
                        else
                        {
                            if( staleBlockMs > 0)
                            {
                                rr=currentCall.fjt.get(staleBlockMs);
                            }
                            else
                            {
                                rr=currentCall.fjt.get(DEFAULT_BLOCK_TIMEOUT);
                            }
                        }
                    }                    
                }
                catch( TimeoutException | InterruptedException to)
                {
                    LOGGER.warn( realURL.toString(), to);
                    
                    rr=Response.builder(Status.C504_TIMED_OUT_GATEWAY, "text/plan", to.toString() ).make();
                }
                catch( IOException e)
                {
                    LOGGER.warn( realURL.toString(), e);
                    
                    rr=Response.builder(Status.C502_BAD_GATEWAY, "text/plan", e.toString()).make();
                } 
                catch( ExecutionException e)
                {
                    Status currentStatus=Status.C520_UNKNOWN_ERROR;
                    Throwable cause=e;
                    for( int loop=0;loop<100;loop++)
                    {
                        Throwable tmpCause=cause.getCause();
                        if( tmpCause == null) break;
                        
                        currentStatus=moreExplicitStatus( currentStatus, tmpCause);
                        cause=tmpCause;
                    }

                    if( currentStatus == Status.C520_UNKNOWN_ERROR)
                    {
                        LOGGER.error( realURL.toString(), cause);

                        rr=Response.builder(Status.C520_UNKNOWN_ERROR, "text/plan", cause.toString()).make();                               
                    }
                    else
                    {
                        LOGGER.warn( realURL.toString(), cause);

                        rr=Response.builder( currentStatus, "text/plan", cause.toString()).make();
                    }                    
                }
            }
            assert rr!=null: "No response";
            
            return rr;
        }
        
        private Status moreExplicitStatus( Status status, Throwable cause)
        {
            Status explicitStatus=status;
            
            if( cause instanceof UnknownHostException)
            {
                explicitStatus=Status.C503_SERVICE_UNAVAILABLE;
            }
            else if( cause instanceof CertificateException)
            {
                explicitStatus=Status.C526_SSL_INVALID_CERTIFICATE;
            }
            else if( cause instanceof SocketException)
            {
                explicitStatus=Status.C521_WEB_SERVER_IS_DOWN;
            }
            else if( cause instanceof SSLProtocolException)
            {
                explicitStatus=Status.C526_SSL_INVALID_CERTIFICATE;
            }
            else if( cause instanceof SSLException)
            {
                explicitStatus=Status.C525_SSL_HANDSHAKE_FAILED;
            }
            else if( cause instanceof CertificateException)
            {
                explicitStatus=Status.C526_SSL_INVALID_CERTIFICATE;
            }
            else if( cause instanceof SocketTimeoutException)
            {
                explicitStatus=Status.C598_TIMED_OUT_SERVER_NETWORK_READ;
            }                    

            return explicitStatus;
        }
        
        private String validateCharactersInURL( final @Nonnull URL checkURL)
        {
            return NetUrl.validateCharactersInURL(checkURL.toString());
        }

        @CheckReturnValue @Nonnull
        private Response writeResponse()
        {           
            URL realURL=makeRealURL();

            if(enableValidateCharactersInURL)
            {
                String validationMessage=validateCharactersInURL(realURL);
                if( StringUtilities.notBlank(validationMessage))
                {
                    return Response.builder(Status.C400_ERROR_BAD_REQUEST,"text/plan", validationMessage ).make();                
                }
            }
            File tempFile=null;

            try
            {
                File tmpBody=body;
                if( bodyXML!=null)
                {
                    tempFile=File.createTempFile("rest",".xml", FileUtil.makeQuarantineDirectory());
                    DocumentUtil.writeDocument(bodyXML, tempFile);
                    tmpBody=tempFile;
                }
                int callTimeout=Math.max(Math.max(timeoutMs, Math.max( staleBlockMs, maxBlockMs)),0);
                
                com.aspc.remote.rest.internal.RestCall r = transport.makeRestCall(
                    method,
                    realURL, 
                    auth, 
                    agent, 
                    null,
                    tmpBody,
                    callTimeout,
                    disableGZIP,
                    FRIEND,
                    contentType,
                    dispositionType
                );
                return r.call();
            }
            catch( TimeoutException to)
            {
                LOGGER.warn( realURL.toString(), to);

                return Response.builder( Status.C504_TIMED_OUT_GATEWAY, "text/plan", to.toString() ).make();
            }
            catch( Exception e)
            {
                LOGGER.warn( realURL.toString(), e);

                return Response.builder(Status.C500_SERVER_INTERNAL_ERROR, "text/plan", e.toString()).make();
            } 
            finally{
                if( tempFile !=null) tempFile.delete();
            }
        }                
    }    
}
