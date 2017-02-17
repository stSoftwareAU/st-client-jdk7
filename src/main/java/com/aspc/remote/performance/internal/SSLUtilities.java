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

package com.aspc.remote.performance.internal;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/**
 * SSLUtililties
 *
 * <br>
 * <i>THREAD MODE: SINGLETON MULTI-THREADED </i>
 *  @author      luke
 *  @since       10 February 2007
 */
public final class SSLUtilities 
{
    private static final HostnameVerifier verifier = new BenchMarkVerifier();
    private static final TrustManager[] trust = new TrustManager[]{
        new BenchMarkX509TrustManager() 
    };
    
    /**
     *
     */
    public static void trustAllHostNames()
    {
        HttpsURLConnection.setDefaultHostnameVerifier( verifier );
    }
    
    /**
     *
     */
    public static void trustAllCertificates()
    {
        SSLContext context;
        
        try
        {
            context = SSLContext.getInstance( "SSL" );
            context.init( null, trust, new SecureRandom() );
        }
        catch( GeneralSecurityException gse )
        {
            throw new IllegalStateException( gse.getMessage() );//NOPMD
        }
        
        HttpsURLConnection.setDefaultSSLSocketFactory( context.getSocketFactory() );
    }
    
    @SuppressWarnings("empty-statement")
    private SSLUtilities()
    {
        ;// no one can create 
    }
}
