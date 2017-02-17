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

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED</i>
 *  @author      luke
 *  @since       10 February 2007
 */
public class BenchMarkVerifier implements HostnameVerifier 
{
    /**
     * 
     * @param hostname 
     * @param session 
     * @return the value
     */
    @Override
    public boolean verify( String hostname, SSLSession session )
    {
        return true;
    }
}
