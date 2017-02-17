/*
 *  Copyright (c) 2002-2004 ASP Converters pty ltd
 *
 *  www.aspconverters.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.memory.selftest.deadlock;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 *  Test the wrapper classes for Trans header, record and data
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED self test unit</i>
 *
 *  @author      Nigel Leck
 *  @since       27 October 2002
 */
public class TestDeadlock extends TestCase
{
    /**
     * Constructor for the test unit.
     * @param testName The name of the test unit
     */
    public TestDeadlock(String testName)
    {
        super(testName);
    }

    /**
     * The main for the program
     * 
     * @param args The command line arguments
     */
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }

    /**
     * Creates the test suite.
     * 
     * @return The test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestDeadlock.class);
        return suite;
    }
    
    /**
     * 
     * @throws Exception a serious problem
     */
    public void testNoBlockInRelease() throws Exception
    {
        final SlowMemoryHandler smh = new SlowMemoryHandler();
        
        Runnable r1 = new SlowReleaseRunner( smh);

        Runnable r2 = new ClearMemoryRunner();
        
        Thread t1 = new Thread( r1, "Slow release");
        Thread t2 = new Thread( r2, "Memory clear");
        
        t1.start();
        
        Thread.sleep( 1000);
        t2.start();
        
        t2.join( 60000);
        
        if( t2.isAlive())
        {
            fail( "MemoryManager.deregister()");
        }
    }
}
