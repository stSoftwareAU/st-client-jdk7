/*
 *  Copyright (c) 2000-2004 ASP Converters pty ltd
 *
 *  www.stSoftware.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.memory.selftest;

import org.apache.commons.logging.Log;
import com.aspc.developer.ThreadCop;
import com.aspc.developer.ThreadCop.MODE;
import com.aspc.remote.memory.HashLongMap;
import com.aspc.remote.memory.HashLongMapFactory;
import com.aspc.remote.util.misc.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * check HashLongMap logic threading
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED self test unit</i>
 *
 *  @author      Nigel Leck
 *  @since       17 October 2009
 */
public class TestHashLongMapThreading extends TestCase
{
    /**
     * Constructor for the test unit.
     * @param testName The name of the test unit
     */
    public TestHashLongMapThreading(String testName)
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
       Test test = suite();
       //test = TestSuite.createTest(TestHashLongMapThreading.class, "testCloneAndKeyArray");

       TestRunner.run(test);
    }

    /**
     * Creates the test suite.
     *
     * @return The test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestHashLongMapThreading.class);
        return suite;
    }

    /**
     * check that we can concurrently call getKeyArray from many threads.
     *
     * @throws Exception a serious problem
     */
    public void testKeyArray() throws Exception
    {
        //big array so the copy takes time.
        long list[]=new long[1000000];
        for( int l=0;l < list.length;l++)
        {
            list[l]=l;
        }

        for( int i = 0; i < 100; i++)
        {
            check(list, "getKeyArray");
        }
    }

    /**
     * check that we can concurrently call getKeyArray from many threads.
     *
     * @throws Exception a serious problem
     */
    public void testContainsKey() throws Exception
    {
        //big array so the copy takes time.
        long list[]=new long[1000000];
        for( int l=0;l < list.length;l++)
        {
            list[l]=l;
        }

        for( int i = 0; i < 100; i++)
        {
            check(list, "containsKey");
        }
    }

    /**
     * check that we can concurrently call Clone
     *
     * @throws Exception a serious problem
     */
    public void testClone() throws Exception
    {
        //big array so the copy takes time.
        long list[]=new long[1000000];
        for( int l=0;l < list.length;l++)
        {
            list[l]=l;
        }

        for( int i = 0; i < 100; i++)
        {
            check(list, "clone");
        }
    }

    /**
     * check that we can concurrently call Clone & keyArray
     *
     * @throws Exception a serious problem
     */
    public void testCloneAndKeyArray() throws Exception
    {
        //big array so the copy takes time.
        long list[]=new long[1000000];
        for( int l=0;l < list.length;l++)
        {
            list[l]=l;
        }

        for( int i = 0; i < 100; i++)
        {
            check(list, "clone & keyArray");
        }
    }

    /**
     * check that we can concurrently call containsValue
     *
     * @throws Exception a serious problem
     */
    public void testContainsValue() throws Exception
    {
        //big array so the copy takes time.
        long list[]=new long[1000000];
        for( int l=0;l < list.length;l++)
        {
            list[l]=l;
        }

        for( int i = 0; i < 100; i++)
        {
            check(list, "containsValue");
        }
    }

    @SuppressWarnings("SleepWhileInLoop")
    private void check(long list[], final String requestMethod) throws Exception
    {
        final HashLongMap lm = HashLongMapFactory.create( );


        lm.putMultiRows(list, "ABC", HashLongMap.State.UNIQUE_SORTED);

        lm.put(-1, "ABC");
        ThreadCop.monitor(lm, MODE.READONLY);

        final Throwable e[]=new Throwable[1];
        final AtomicLong readyCount = new AtomicLong();
        Runnable r = () -> {
            try
            {
                int seed = (int)(10 * Math.random());
                String method = requestMethod;
                if( requestMethod.equals("clone & keyArray"))
                {
                    if( seed < 5)
                    {
                        method="getKeyArray";
                    }
                    else
                    {
                        method="clone";
                    }
                }
                synchronized( lm)
                {
                    readyCount.incrementAndGet();
                    lm.wait( 120000);
                }
                if( "getKeyArray".equals(method))
                {
                    lm.getKeyArray();
                }
                else if( "containsKey".equals(method))
                {
                    lm.containsKey(5);
                }
                else if( "containsValue".equals(method))
                {
                    if(lm.containsValue("ABC") == false)
                    {
                        throw new Exception( "doesn't contain ABC");
                    }
                    if(lm.containsValue("XYZ") == true)
                    {
                        throw new Exception( "should not contain XYZ");
                    }
                }
                else if( "clone".equals(method))
                {
                    HashLongMap lm2 = (HashLongMap) lm.clone();

                    if( lm2.containsKey(-1) == false)
                    {
                        lm2.containsKey(-1);
                        throw new Exception( "could not find -1");
                    }
                    if( lm2.containsKey(1234) == false)
                    {
                        throw new Exception( "could not find 1234");
                    }
                    if(lm2.containsValue("ABC") == false)
                    {
                        throw new Exception( "doesn't contain ABC");
                    }
                    if(lm2.containsValue("XYZ") == true)
                    {
                        throw new Exception( "should not contain XYZ");
                    }
                }
                else
                {
                    throw new Exception( "unknow method " + method);
                }
            }
            catch( Throwable t2)
            {
                LOGGER.warn( "problem", t2);
                e[0]=t2;
            }
        };

        ArrayList<Thread> threads=new ArrayList<>(100);
        for( int i = 0; i < 100;i++)
        {
            Thread t=new Thread( r, "t" + 1);
            t.start();
            threads.add(t);
        }
        Thread.sleep(10);
        for( int i = 0; i < 120; i++)
        {
            if( readyCount.get() >= 100) break;
            LOGGER.info( requestMethod + " only " + readyCount + " ready");
            Thread.sleep(1000);
        }
        synchronized( lm)
        {
            lm.notifyAll( );
        }

        for( Thread t : threads)
        {
            t.join(60000);
        }

        if( e[0] != null)
        {
            throw new Exception( "concurrent read thread error", e[0]);
        }
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.memory.selftest.TestHashLongMapThreading");//#LOGGER-NOPMD
}
