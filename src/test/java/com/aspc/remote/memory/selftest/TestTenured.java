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

import com.aspc.remote.memory.MemoryManager;
import com.aspc.remote.util.misc.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.commons.logging.Log;

/**
 * Check the lock method in the cache tables.
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED self test unit</i>
 *
 *  @author      Nigel Leck
 *  @since       17 December 2007
 */
public class TestTenured extends TestCase
{
    /**
     * Constructor for the test unit.
     * @param testName The name of the test unit
     */
    public TestTenured(String testName)
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
        TestSuite suite = new TestSuite(TestTenured.class);
        return suite;
    }

    @SuppressWarnings("SleepWhileInLoop")
    public void testTenuredVersusMax() throws Exception
    {
        for( int attempts=0;true;attempts++)
        {
            long freeMemory=MemoryManager.getFreeMemory();
            long tenuredFreeMemory=MemoryManager.getTenuredFreeMemory();
            
            String msg=null;
            if( tenuredFreeMemory>freeMemory)
            {
                msg="tenured free memory " + NumUtil.convertMemoryToHumanReadable(tenuredFreeMemory) + " > " + NumUtil.convertMemoryToHumanReadable(freeMemory);
            }
            else
            {
                long tenuredTotalMemory = MemoryManager.getTenuredTotalMemory();
                long totalMemory = MemoryManager.getTotalMemory();
                
                if( tenuredTotalMemory>=totalMemory)
                {
                    msg="tenured total memory " + NumUtil.convertMemoryToHumanReadable(tenuredTotalMemory) + " >= " + NumUtil.convertMemoryToHumanReadable(totalMemory);
                }
                else
                {
                    long tenuredUsed = MemoryManager.getTenuredUsed();
                    long totalUsed = MemoryManager.getTotalUsed();
                    if( tenuredUsed>=totalUsed)
                    {
                        msg="tenured used memory " + NumUtil.convertMemoryToHumanReadable(tenuredUsed) + " >= " + NumUtil.convertMemoryToHumanReadable(totalUsed);
                    }
                }
            }
            
            if( msg!=null)
            {                                
                if( attempts > 3)
                {
                    fail( msg);
                }
                else
                {
                    LOGGER.info( msg);
                    Thread.sleep(50);
                }
            }
            else
            {
                break;
            }
        }
    }
    /**
     * Check that the tenured settings works
     * @throws Exception a serious problem
     */
    public void testSetting() throws Exception
    {
        //assertTrue( "Should be use new memory manager", MemoryManager.isNewMemoryManager());
        long max = 128 * 1024 * 1024;

        MemoryManager.setMaxMemory( max);

        long tempMax = MemoryManager.getTotalMemory();

        assertEquals( "should match the value set", max, tempMax);

        long tenuredSize = 100 * 1024 * 1024;

        MemoryManager.setTenuredSize( tenuredSize);

        long tempTenuredSize = MemoryManager.getTenuredTotalMemory();

        assertEquals( "should match the tenured value set", tenuredSize, tempTenuredSize);

        tenuredSize = max * 2;

        MemoryManager.setTenuredSize( tenuredSize);

        tempMax = MemoryManager.getTotalMemory();

        if( tempMax <= max)
        {
            fail( "If we set the tenured size to be greater than the max then we need to adjust the max");
        }

        MemoryManager.setTenuredSize( "100m");

        tempMax = MemoryManager.getTenuredTotalMemory();

        assertEquals( "should have set " + NumUtil.convertMemoryToHumanReadable(tempMax), 100 * 1024 * 1024, tempMax);

        Runtime rt = Runtime.getRuntime();

        max = rt.maxMemory();

        MemoryManager.setMaxMemory( max * 2);

        tempMax = MemoryManager.getTotalMemory();

        assertEquals( "MAX should be limited to the Actual MAX memory of " + NumUtil.convertMemoryToHumanReadable(max) + " but was " + NumUtil.convertMemoryToHumanReadable(tempMax), max, tempMax);

        MemoryManager.setTenuredSize( max * 2);

        tempMax = MemoryManager.getTenuredTotalMemory();

        if( tempMax > max)
        {
            fail( "Tenured size MUST be less than Actual MAX memory of " + NumUtil.convertMemoryToHumanReadable(max) + " but was " + NumUtil.convertMemoryToHumanReadable(tempMax));
        }

        long tenuredMax = MemoryManager.getTotalMemory();
        /*
        List<MemoryPoolMXBean> list = ManagementFactory.getMemoryPoolMXBeans();

        for( int i=0; i < list.size(); i++)
        {
            MemoryPoolMXBean memoryPool;

            memoryPool = list.get(i);

            //String name = memoryPool.getName();

            if( memoryPool.isCollectionUsageThresholdSupported())
            {
                long poolMax = memoryPool.getUsage().getMax();

                if( poolMax > tenuredMax)
                {
                    tenuredMax=poolMax;
                }
            }
        }

        if( tempMax > tenuredMax)
        {
            fail( "Tenured size MUST be less than MAX tenured pool max of " + MemoryUtil.displaySize(tenuredMax) + " but was " + MemoryUtil.displaySize(tempMax));
        }
        */
        MemoryManager.checkZone();

        MemoryManager.setTenuredSize(-1);
        tempMax = MemoryManager.getTenuredTotalMemory();

        if( MemoryManager.OVERRIDE_OCCUPANCY_FRACTION > 0)
        {
            assertEquals(
                "tenured size if not set should be " +
                MemoryManager.OVERRIDE_OCCUPANCY_FRACTION +
                "% of max memory", max /100 * MemoryManager.OVERRIDE_OCCUPANCY_FRACTION,
                tempMax
            );
        }

        MemoryManager.setTenuredPercent("95");

        tempMax = MemoryManager.getTenuredTotalMemory();

        if( tempMax > tenuredMax)
        {
            fail( "When calculating the tenured size MUST be less than MAX tenured pool max of " + NumUtil.convertMemoryToHumanReadable(tenuredMax) + " but was " + NumUtil.convertMemoryToHumanReadable(tempMax));
        }
        /*
        long startCount = MemoryManager.getCountGC();
        MemoryManager.gc();
        long endCount = MemoryManager.getCountGC();

        if( startCount == endCount )
        {
            fail( "we didn't increament the GC count");
        }*/
    }


    /**
     * @throws java.lang.Exception {@inheritDoc} */
    @Override
    /**
     * Close any resources used by the test case. <B>DO NOT RELY ON THE TEAR DOWN RUNNING</B> when debugging we can
     * stop the test case halfway through. The <code>setUp</code> must handle this condition.
     *
     * @throws java.lang.Exception A serious problem
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();

        MemoryManager.setMaxMemory(maxMemory);
        MemoryManager.setTenuredSize(-1);
        MemoryManager.setTenuredPercent(null);
    }

    /** {@inheritDoc}
     * @throws java.lang.Exception */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        MemoryManager.checkZone();

        maxMemory=MemoryManager.getTotalMemory();
    }

    private long maxMemory;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.memory.selftest.TestTenured");//#LOGGER-NOPMD
}
