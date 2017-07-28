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

import com.aspc.remote.memory.HashLongMap;
import com.aspc.remote.memory.HashLongMapFactory;
import com.aspc.remote.util.misc.*;
import java.util.HashSet;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.commons.logging.Log;

/**
 * check HashLongMap logic
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED self test unit</i>
 *
 *  @author      Nigel Leck
 *  @since       17 October 2009
 */
public class TestHashLongMapKeyData extends TestCase
{
    /**
     * Constructor for the test unit.
     * @param testName The name of the test unit
     */
    public TestHashLongMapKeyData(String testName)
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
       //test = TestSuite.createTest(TestHashLongMap.class, "testGetSortKey");

       TestRunner.run(test);
    }

    /**
     * Creates the test suite.
     *
     * @return The test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestHashLongMapKeyData.class);
        return suite;
    }

    /**
     * Simple add/remove
     *
     * @throws Exception a serious problem
     */
    public void testKeyData() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        lm.put(1, "ABC");
        lm.put(2, "ABC");

        checkListAndData( lm);

        lm.put(2, "XYZ");
        checkListAndData( lm);
        lm.put(3, "XYZ");
        checkListAndData( lm);
        lm.remove(3);
        checkListAndData( lm);
        lm.remove(1);
        lm.remove(2);
        checkListAndData( lm);
    }

    /**
     * Simple add/remove
     *
     * @throws Exception a serious problem
     */
    public void testKeyData2() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        lm.put(1, "ABC");
        lm.put(2, "ABC");

        long[][] data=lm.getKeyData();
        long[] list=lm.getSortedKeyArray();
        checkListAndData(list, data);
    }

    /**
     * Check when empty
     *
     * @throws Exception a serious problem
     */
    public void testEmpty() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        checkListAndData( lm);
    }
    /**
     * Check shared.
     *
     * @throws Exception a serious problem
     */
    public void testShared() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );
        lm.put(1, "");
        long[][] data1 = lm.getKeyData();
        long[][] data2 = lm.getKeyData();
        assertTrue( "Should be the same array", data1==data2);

        lm.put(1, "A");
        data1 = lm.getKeyData();
        data2 = lm.getKeyData();
        assertTrue( "Should be the same array", data1==data2);

        lm.put(2, "B");
        data1 = lm.getKeyData();
        lm.put(2, "B");
        data2 = lm.getKeyData();
        assertTrue( "Should be the same array", data1==data2);

        data1 = lm.getKeyData();
        lm.remove(3);
        data2 = lm.getKeyData();
        assertTrue( "Should be the same array", data1==data2);


        data1 = lm.getKeyData();
        lm.remove(2);
        data2 = lm.getKeyData();
        assertFalse( "Should NOT be the same array", data1==data2);


    }

    /**
     * Lots of random numbers.
     *
     * @throws Exception a serious problem
     */
    public void testBig() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        for( int loop=0;loop<1234;loop++)
        {
            lm.put((long)(Math.random()*Long.MAX_VALUE), "");
        }
        checkListAndData( lm);

        lm.clear();
        checkListAndData( lm);
    }

    /**
     * Lots of random numbers.
     *
     * @throws Exception a serious problem
     */
    public void testBulk() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        for( int loop=0;loop<1234;loop++)
        {
            lm.put((long)(Math.random()*Long.MAX_VALUE), "");
        }

        checkListAndData( lm);
        HashLongMap lm2 = HashLongMapFactory.create( );
        lm2.putMultiRows(lm.getKeyArray(), "", HashLongMap.State.UNKNOWN);
        checkListAndData( lm2);

    }
    private void checkListAndData( final HashLongMap lm)
    {
        long[] list;
        long[][]data;

        int check=(int) (Math.random() * 5);
//        LOGGER.info( "check: " + check);
        switch(check)
        {
            case 0:
                list=lm.getSortedKeyArray();
                data=lm.getKeyData();
                break;
            case 1:
                data=lm.getKeyData();
                list=lm.getKeyArray();
                break;
            case 2:
                data=lm.getKeyData();
                list=lm.getSortedKeyArray();
                break;
            default:
                list=lm.getKeyArray();
                data=lm.getKeyData();
        }

        assertEquals( "size", lm.size(), list.length);

        checkListAndData(list, data);

        long[][] data2 = lm.getKeyData();
        assertTrue( "should be the same array", data==data2);
    }
    private void checkListAndData( final long[] list, final long[][]data)
    {
        HashSet<Long> unique=new HashSet<>();
        for( long row: list)
        {
            if( unique.add(row)==false)
            {
                fail( "duplicate value: " + row);
            }
        }

        for( long[]rows: data)
        {
            for( long row: rows)
            {
                if( unique.remove(row)==false)
                {
                    fail( "value missing: " + row);
                }
            }
        }

        if( unique.isEmpty()==false)
        {
            fail( "extra values: " + unique);
        }
    }


    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.memory.selftest.TestHashLongMapKeyData");//#LOGGER-NOPMD
}
