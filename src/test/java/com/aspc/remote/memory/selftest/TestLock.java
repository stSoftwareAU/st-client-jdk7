/*
 *  Copyright (c) 2000-2004 ASP Converters pty ltd
 *
 *  www.aspconverters.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.memory.selftest;

import com.aspc.remote.memory.CacheLongTable;
import com.aspc.remote.memory.MemoryManager;
import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Check the lock method in the cache tables.
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED self test unit</i>
 *
 *  @author      Nigel Leck
 *  @since       17 December 2007
 */
public class TestLock extends TestCase
{
    /**
     * Constructor for the test unit.
     * @param testName The name of the test unit
     */
    public TestLock(String testName)
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
        //parseArgs( args);
        TestRunner.run(suite());
    }

    /**
     * Creates the test suite.
     *
     * @return The test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestLock.class);
        return suite;
    }

    /**
     * Make sure Cache Reference works.
     * @throws Exception a serious problem
     */
    public void testBasic() throws Exception
    {
        CacheLongTable ct = new CacheLongTable("Testing ");

        ct.setThreshold( Integer.MAX_VALUE);

        for( int i = 0; i < 1000; i++)
        {
            ct.put( i, "ROW:" + i);
            if( i % 2 == 0)
            {
                ct.lock(i);
            }
        }

        for( int i = 0; i < 1000; i++)
        {
            Object obj;

            obj = ct.get( i);

            if( obj == null)
            {
                fail( "Everything is needed it be in memory " + i);
            }
        }

        ct.freeMemory( 1);
        MemoryManager.gc();

        for( int i = 0; i < 1000; i++)
        {
            Object obj;

            obj = ct.get( i);

            if( i % 2 == 0)
            {
                if( obj == null)
                {
                    fail( "Should not have freed locked object ");
                }
            }
            else
            {
                if( obj != null)
                {
                    fail( "Should have freed unlocked object " + obj);
                }

            }
        }

        for( int i = 0; i < 1000; i++)
        {
            if( i % 2 == 0)
            {
                ct.unlock(i);
            }
        }

        ct.freeMemory( 1);
        MemoryManager.gc();

        for( int i = 0; i < 1000; i++)
        {
            Object obj;

            obj = ct.get( i);

            if( obj != null)
            {
                fail( "Should have freed unlocked object " + obj);
            }
        }
    }


    /**
     * Make sure Cache Reference works.
     * @throws Exception a serious problem
     */
    public void testReplace() throws Exception
    {
        CacheLongTable ct = new CacheLongTable("Testing ");

        ct.setThreshold( Integer.MAX_VALUE);

        for( int i = 0; i < 1000; i++)
        {
            ct.put( i, "TEMP:" + i);
            if( i % 2 == 0)
            {
                ct.lock(i);
            }
        }

        for( int i = 0; i < 1000; i++)
        {
            ct.put( i, "ROW:" + i);
        }


        for( int i = 0; i < 1000; i++)
        {
            Object obj;

            obj = ct.get( i);

            if( obj == null)
            {
                fail( "Everything is needed it be in memory " + i);
            }

            if( obj.toString().startsWith("ROW") == false)
            {
                fail( "Should have replaced the element " + obj);
            }
        }

        ct.freeMemory( 1);
        MemoryManager.gc();

        for( int i = 0; i < 1000; i++)
        {
            Object obj;

            obj = ct.get( i);

            if( i % 2 == 0)
            {
                if( obj == null)
                {
                    fail( "Should not have freed locked object ");
                }
            }
            else
            {
                if( obj != null)
                {
                    fail( "Should have freed unlocked object " + obj);
                }

            }
        }

        for( int i = 0; i < 1000; i++)
        {
            if( i % 2 == 0)
            {
                ct.unlock(i);
            }
        }

        ct.freeMemory( 1);
        MemoryManager.gc();

        for( int i = 0; i < 1000; i++)
        {
            Object obj;

            obj = ct.get( i);

            if( obj != null)
            {
                fail( "Should have freed unlocked object " + obj);
            }
        }
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.memory.selftest.TestLock");//#LOGGER-NOPMD
}
