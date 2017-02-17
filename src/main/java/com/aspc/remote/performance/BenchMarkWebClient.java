/*
 *  Copyright (c) 2004 ASP Converters pty ltd
 *
 *  www.aspconverters.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.performance;

import com.aspc.remote.soap.LoginContext;
import com.aspc.remote.performance.internal.WebClient;

/**
 *  App
 *
 * @author Nigel Leck
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @since       25 September, 2006
 */
public final class BenchMarkWebClient extends BenchMarkClient
{
    /**
     * Constructor for BenchMarkWebClient
     * Creates a client that can be used by tasks to make http requests
     * 
     * @param connectionUrl - url identifying the user details and database to connect to
     * @param loginContext - details about the users physical environment
     * @param name - a name to uniquely identify the client
     * @throws Exception - an error has occurred instantiating the clients
     */
    public BenchMarkWebClient(final String name, final String connectionUrl, final LoginContext loginContext) throws Exception
    {
        super( name, connectionUrl, loginContext);
        httpClient = createHttpClient( getConnectionUrl(), getLoginContext());
    }
    
    /**
     * The http client used for making http requests
     * @return the http client
     */
    public WebClient getHttpClient()
    {
        return httpClient;
    }

    /**
     * This function creates the client for processing http requests
     * @param remoteURL The remote url
     * @param loginContext The login context
     * @return client HttpUnit client
     * @throws Exception client could not be created or logged in
     */
    private WebClient createHttpClient( final String remoteURL, final LoginContext loginContext ) throws Exception
    {
        WebClient client = new WebClient( remoteURL);        
        
        client.login( loginContext ); 

        return client;
    }
  
    private final WebClient httpClient;
}
