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

import com.aspc.remote.memory.MemoryHandler;
import com.aspc.remote.memory.MemoryManager;
import com.aspc.remote.memory.SecondaryCache;
import com.aspc.remote.memory.SecondaryCacheGroup;
import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.*;
import java.util.ArrayList;
import java.util.HashSet;
import javax.annotation.CheckReturnValue;
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
public class TestSecondaryCache extends TestCase
{
    /**
     * Constructor for the test unit.
     * @param testName The name of the test unit
     */
    public TestSecondaryCache(String testName)
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
        TestSuite suite = new TestSuite(TestSecondaryCache.class);
        return suite;
    }

    /**
     * Make sure we release memory.
     * @throws Exception a serious problem
     */
    public void testRelease() throws Exception
    {
        int startCount=SecondaryCache.countOfCaches();

        ArrayList objects=new ArrayList<>();
        HashSet<String>groups=new HashSet<>();
        final int COUNT=1000;
        for( int i=0;i<COUNT;i++)
        {
            String key="KEY" + i;
            String groupKey="GROUP12345";
            //keys.add( key);
            groups.add( groupKey);
            Object obj=load( groupKey, key);
            objects.add(obj);
        }

        int endCount=SecondaryCache.countOfCaches();

        if( endCount< startCount+COUNT)
        {
            fail( "lost caches " + (endCount-startCount));
        }

        for( int j=0;j<COUNT;j++)
        {
            String key="LOST" + j;
            String groupKey="GROUP12345";

            load( groupKey, key);
        }

        SecondaryCache.clearSecondaryCache("XYZ");
        MemoryManager.gc();
        SecondaryCache.clearSecondaryCache("XYZ");

        int endCount2=SecondaryCache.countOfCaches();
        LOGGER.info("2. Caches " + endCount2);
        if( endCount2>= (endCount-startCount)+COUNT)
        {
            LOGGER.warn( "looks like a leak");
            Thread.sleep(5000);
            SecondaryCache.clearSecondaryCache("XYZ1");

            endCount2=SecondaryCache.countOfCaches();
                        
            if( endCount2>= (endCount-startCount)+COUNT)
            {

                fail( "Did not release caches " + (endCount2-(endCount-startCount)));
            }
        }

        if( endCount2< startCount+COUNT)
        {
            fail( "lost caches " + (endCount2-startCount));
        }

        assertEquals("Key size", objects.size(), COUNT);
        assertEquals("Group size", groups.size(), 1);

        MemoryManager.clearMemory(MemoryHandler.Cost.MEDIUM_LOW);
        MemoryManager.gc();
        int endCount3=SecondaryCache.countOfCaches();
        LOGGER.info("3. Caches " + endCount3);

        if( endCount3>=endCount)
        {
            fail( "should have cleared all the caches");
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private Object load( final String group, final String key)
    {
        Object cacheObject=new SecondaryCacheGroup(){

            @Override
            @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
            public boolean equals(Object obj) {
                return key.equals(obj);
            }

            @Override @CheckReturnValue
            public int hashCode() {
                return key.hashCode();
            }

            @Override
            public Object getSecondaryCacheGroupKey() {
                return group;
            }

        };
        SecondaryCache.getSecondaryCache(cacheObject);

        return cacheObject;
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.memory.selftest.TestSecondaryCache");//#LOGGER-NOPMD

}
