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

import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

/**
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED </i>
 *  @author      luke
 *  @since       10 February 2007
 */
public class BenchMarkX509TrustManager implements X509TrustManager
{
    static final X509Certificate[] issuers = new X509Certificate[]{};
    
    /**
     * 
     * @param chain 
     * @param authType 
     */
    @SuppressWarnings("empty-statement")
    @Override
    public void checkClientTrusted( X509Certificate[] chain, String authType )
    {
        ;// allow all
    }
    
    /**
     * 
     * @param chain 
     * @param authType 
     */
    @SuppressWarnings("empty-statement")
    @Override
    public void checkServerTrusted( X509Certificate[] chain, String authType )
    {
        ;// allow all
    }
    
    /**
     * 
     * @return the value
     */
    @Override
    public X509Certificate[] getAcceptedIssuers()
    {
        return issuers;
    }
}
