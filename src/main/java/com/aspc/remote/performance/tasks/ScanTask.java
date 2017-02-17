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
package com.aspc.remote.performance.tasks;

import com.aspc.remote.performance.BenchMarkClient;
import com.aspc.remote.performance.Task;

import com.aspc.remote.jdbc.SoapResultSet;

/**
 *  App
 *
 * @author Nigel Leck
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @since       7 April 2001
 */
public class ScanTask extends Task
{
    /**
     * 
     * @param name 
     * @param bmClient 
     */
    public ScanTask(String name, BenchMarkClient bmClient)
    {
        super( name, bmClient);
    }

    /**
     * 
     * @throws Exception a serious problem
     */
    @Override
    @SuppressWarnings("empty-statement")
    protected void process() throws Exception
    {
        SoapResultSet r = bmClient.getRemoteClient().fetch( "SELECT * FROM " + getData());

        while( r.next())
        {
            ;
        }
    }
}
