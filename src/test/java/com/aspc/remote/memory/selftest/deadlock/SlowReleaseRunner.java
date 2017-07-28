/*
 *  Copyright (c) 2002-2004 ASP Converters pty ltd
 *
 *  www.stSoftware.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.memory.selftest.deadlock;

/**
 *  Test the wrapper classes for Trans header, record and data
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED self test unit</i>
 *
 *  @author      Nigel Leck
 *  @since       27 October 2002
 */
public class SlowReleaseRunner implements Runnable
{
    /**
     * 
     * @param smh the handler
     */
    public SlowReleaseRunner( SlowMemoryHandler smh)
    {
        this.smh = smh;
    }

    /**
     * run
     */
    @Override
    public void run()
    {
        smh.remove( "ABC");
    }
    
    private final SlowMemoryHandler smh;
}
