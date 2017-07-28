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
import com.aspc.remote.memory.impl.HashLongMapV6;
import com.aspc.remote.util.misc.*;
import java.util.ArrayList;
import java.util.Arrays;
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
public class TestHashLongMap extends TestCase
{
    /**
     * Constructor for the test unit.
     * @param testName The name of the test unit
     */
    public TestHashLongMap(String testName)
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
        TestSuite suite = new TestSuite(TestHashLongMap.class);
        return suite;
    }

    /**
     * Runs the bare test sequence.
     * @exception Throwable if any exception is thrown
     */
    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void runBare() throws Throwable
    {
        setUp();
        int version = 0;
        // Run SQL commands
        setUp();

        long oldStartMS;
        long oldEndMS;
        try
        {
            version = HashLongMapFactory.setProgramVersion(6);
            oldStartMS=System.currentTimeMillis();
            runTest();
            oldEndMS=System.currentTimeMillis();
        }
        finally
        {
            HashLongMapFactory.setProgramVersion(version);
            tearDown();
        }

        long oldDiffMS=oldEndMS-oldStartMS;

        if( oldDiffMS < 2) oldDiffMS=2;

        while( true)
        {
            // Run SQL commands
            setUp();
            long newStartMS;
            long newEndMS;
            try
            {
                version = HashLongMapFactory.setProgramVersion(0);
                newStartMS=System.currentTimeMillis();
                runTest();
                newEndMS=System.currentTimeMillis();
            }
            finally
            {
                HashLongMapFactory.setProgramVersion(version);
                tearDown();
            }

            long newDiffMS=newEndMS-newStartMS;

            if( newDiffMS<=oldDiffMS) break;

            String name = getName();
            String msg=name + " retry old was " + TimeUtil.getDiff(oldStartMS, oldEndMS) + " new was " + TimeUtil.getDiff(newStartMS, newEndMS);


            /*if( i>50)
            {
                if(
                    name.equalsIgnoreCase("testShouldBeAbleToAddOne") ||
                    name.equalsIgnoreCase("testShouldSortIfNeededContains") ||
                    name.equalsIgnoreCase("testShouldSortIfNeededGet")||

                )
                {
                    LOGGER.warn( msg);
                    break;
                }
                else
                {
                    fail( msg);
                }
            }*/

            LOGGER.info( msg);
            break;
        }
        try
        {
            version = HashLongMapFactory.setProgramVersion(Integer.MAX_VALUE);
            runTest();
        }
        finally
        {
            HashLongMapFactory.setProgramVersion(version);
            tearDown();
        }
    }

    /**
     * getKey is causing an issue
     *
     * @throws Exception a serious problem
     */
    public void testGetSortKey() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );


        //long list[]={1,2};
        //lm.putMultiRows(list, "ABC", HashLongMap.State.UNIQUE_SORTED);
        lm.put(1, "ABC");
        lm.put(2, "ABC");

        long list2[] = lm.getSortedKeyArray();
        //assertTrue( "we can find it", lm.containsKey(-1));

        assertEquals( "check length ", 2, list2.length);
    }

    /**
     * getKey is causing an issue
     *
     * @throws Exception a serious problem
     */
    public void testInitContainsKey() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );


        long list[]={1,2,3,5,6};
        lm.putMultiRows(list, "ABC", HashLongMap.State.UNIQUE_SORTED);
        lm.put(-1, "ABC");

        long list2[] = lm.getKeyArray();
        assertTrue( "we can find it", lm.containsKey(-1));

        assertEquals( "check length ", 6, list2.length);
    }

    /**
     * getKey is causing an issue
     *
     * @throws Exception a serious problem
     */
    public void testInitContainsKey2() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );


        long list[]={1,2,3,5,6};
        lm.putMultiRows(list, "ABC", HashLongMap.State.UNIQUE_SORTED);
        lm.put(-1, "ABC");
        assertTrue( "we can find it", lm.containsKey(-1));

        long list2[] = lm.getKeyArray();
        assertTrue( "we can find it", lm.containsKey(-1));
        HashLongMap lm2 = (HashLongMap) lm.clone();
        assertTrue( "we can find it", lm2.containsKey(-1));

        assertEquals( "check length ", 6, list2.length);

        //assertEquals("first element", -1, list2[0]);
    }

    /**
     * don't give out shared arrays.
     *
     * @throws Exception a serious problem
     */
    public void testShared3() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        if( lm instanceof HashLongMapV6 == false ) return;

        long list[]={1,2,3,4,5};

        lm.putMultiRows(list, "", HashLongMap.State.UNIQUE_SORTED);

        lm.put(-6, "");

        lm.remove(-6);

        lm.put(6, "");

        lm.remove(6);

        long list2[] = lm.getKeyArray();

        if( list != list2)
        {
            fail( "should still be able to share");
        }
    }
    /**
     * don't give out shared arrays.
     *
     * @throws Exception a serious problem
     */
    public void testShared() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        if( lm instanceof HashLongMapV6 == false ) return;

        long list[]={1,2,3,0,5};

        lm.putMultiRows(list, "ABC", HashLongMap.State.UNIQUE);
        HashLongMap lm2 = (HashLongMap) lm.clone();
        long sortedList[] = lm.getSortedKeyArray();
        if( list == sortedList)
        {
            fail( "sorted a shared array");
        }

        long list2[] =lm2.getKeyArray();
        if( list2 == sortedList)
        {
            fail( "should not share");
        }

        lm.put(-6, "");
        long list3[] = lm.getKeyArray();

        long list4[] = lm.getSortedKeyArray();

        if( list3 == list4)
        {
            fail( "the array should NOT be shared and not sorted ");
        }

    }

    /**
     * don't give out shared arrays.
     *
     * @throws Exception a serious problem
     */
    public void testShared2() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        if( lm instanceof HashLongMapV6 == false ) return;

        long list[]={1,2,3,0,5};

        lm.putMultiRows(list, "ABC", HashLongMap.State.UNIQUE);
        long list1[] = lm.getKeyArray();

        HashLongMap lm2 = (HashLongMap) lm.clone();
        long sortedList[] = lm.getSortedKeyArray();
        if( list == sortedList)
        {
            fail( "sorted a shared array");
        }

        long list2[] =lm2.getKeyArray();
        if( list2 == sortedList)
        {
            fail( "should not share");
        }

        if( list1 != list2)
        {
            fail( "should be able to return previous");
        }
    }

    /**
     * check that we can put in duplicates and they get sorted out
     *
     * @throws Exception a serious problem
     */
    public void testPutMultiWithDuplicates() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        long list[]={1,2,3,3,3,0,5};

        lm.putMultiRows(list, "ABC", HashLongMap.State.UNKNOWN);

        long longList[] = lm.getKeyArray();

        if( longList == null || longList.length != 5 )
        {
            fail( "long list failed " + longList.length);
        }

        long sortedList[] = lm.getSortedKeyArray();

        if( sortedList == null || sortedList.length != 5 )
        {
            fail( "sorted list failed " + Arrays.toString(sortedList));
        }

        assertTrue( "Should be sorted", lm.isKeyArraySorted());

        assertTrue( "should contain this value", lm.containsValue("ABC"));

        assertTrue( "should contain this value", lm.containsKey(5));

        assertEquals( "Should be able to get this value without initialize", "ABC", lm.get(0));

        lm.put(999, "ABC");

        assertEquals( "Should be able to get this value without initialize", "ABC", lm.get(999));

        assertNull( "Should be able to remove the key if missing without initialize", lm.remove(-1));

        lm.put(0, "XYZ");

        assertEquals( "Should be able to get this value without initialize", "XYZ", lm.get(0));
        assertTrue( "should be initialized now", lm.isInitialized());
        assertEquals( "Should be able to get this value without initialize", "ABC", lm.get(999));
        assertEquals( "Don't double count", 6, lm.size());
    }

    /**
     * check the return value of the put.
     *
     * @throws Exception a serious problem
     */
    public void testPutReturnValue() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        Object previous;
        previous= lm.put(3, "A");
        assertNull( "no previous", previous);
        previous= lm.put(4, "A");
        assertNull( "no previous", previous);
    }

    /**
     * check that we replace one va
     *
     * @throws Exception a serious problem
     */
    public void testReplace() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        ArrayList a1=new ArrayList();
        Object previous;
        previous= lm.put(3, a1);
        assertNull( "no previous", previous);
        ArrayList a2=new ArrayList();
        previous= lm.put(3, a2);

        if( previous != a1)
        {
            fail( "should have got a1");
        }

        Object value = lm.get( 3);
        if( value != a2)
        {
            fail( "should have got a2");
        }
    }

    /**
     * check that we replace one va
     *
     * @throws Exception a serious problem
     */
    public void testPutAllReplace() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        ArrayList a1=new ArrayList();

        long list[]={1,2};

        Object previous;

        lm.putMultiRows(list, a1, HashLongMap.State.UNIQUE);
        //assertNull( "no previous", previous);

        ArrayList a2=new ArrayList();
        previous= lm.put(3, a2);

        assertNull( "no previous value", previous);

        Object value = lm.get( 3);
        if( value != a2)
        {
            fail( "should have got a2");
        }

        value = lm.get( 1);
        if( value != a1)
        {
            fail( "should have got a1");
        }

        value = lm.get( 2);
        if( value != a1)
        {
            fail( "should have got a1");
        }

        value = lm.remove(2);
        if( value != a1)
        {
            fail( "should have got a1");
        }

        value = lm.get( 3);
        if( value != a2)
        {
            fail( "should have got a2");
        }

        value = lm.get( 1);
        if( value != a1)
        {
            fail( "should have got a1");
        }
    }

    /**
     * put NULL and then put value
     * @throws Exception a serious problem
     */
    public void testPutNullPutValue() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        Object previous;
        previous= lm.put(3, null);
        assertNull( "no previous", previous);

        previous= lm.put(4, "A");
        assertNull( "no previous", previous);

        long list[];
        list=lm.getSortedKeyArray();
        assertEquals("check length", 2, list.length);
        assertEquals( "Size should match array length", list.length, lm.size());

        assertTrue( "should contain key 3", lm.containsKey(3));
        assertTrue( "should contain key 4", lm.containsKey(4));

        assertTrue( lm.getClass() + " should contain NULL", lm.containsValue(null));
        assertTrue( "should contain A", lm.containsValue("A"));

        assertNull("get value for 3",  lm.get(3));
        assertEquals("get value for 4", "A", lm.get(4));
    }

    /**
     * put, getSortedKeyArray, put
     * @throws Exception a serious problem
     */
    public void testPutGetSortedArrayGetKeyArray() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        lm.put(3, "A");
        long list[];
        list=lm.getSortedKeyArray();
        assertEquals("check length", 1, list.length);
        assertEquals( "Size should match array length", list.length, lm.size());

        list=lm.getSortedKeyArray();
        assertEquals("check length", 1, list.length);
        assertEquals( "Size should match array length", list.length, lm.size());
    }

    /**
     * put and then put multiple.
     * @throws Exception a serious problem
     */
    public void testPutThenPutMultiple() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        lm.put(3, "A");
        long list[]={1,2,3};
        lm.putMultiRows(list, "A", HashLongMap.State.UNIQUE);

        long tempList[] = lm.getKeyArray();
        assertEquals("check length", 3, tempList.length);
        assertEquals( "Size should match array length", tempList.length, lm.size());
    }

    /**
     * put multiple. then two puts.
     * @throws Exception a serious problem
     */
    public void testPutMultipleThenTwoPuts() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        long list[]={1,2,3};
        lm.putMultiRows(list, "A", HashLongMap.State.UNIQUE);
        lm.put(4, "A");
        lm.put(5, "A");

        long tempList[] = lm.getKeyArray();
        assertEquals("check length", 5, tempList.length);
        assertEquals( "Size should match array length", tempList.length, lm.size());

        for( long key=1; key < 6;key++)
        {
            assertEquals( "should find", "A", lm.get( key));
        }
    }

    /**
     * put and then put multiple with different values
     * @throws Exception a serious problem
     */
    public void testPutThenPutMultipleDiff() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        lm.put(3, "A");
        long list[]={1,2,3};
        lm.putMultiRows(list, "B", HashLongMap.State.UNIQUE);

        long tempList[] = lm.getKeyArray();
        assertEquals("check length", 3, tempList.length);
        assertEquals( "Size should match array length", tempList.length, lm.size());
    }

    /**
     * first get without any put
     * @throws Exception a serious problem
     */
    public void testFirstGet() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        assertNull( "Should return without error", lm.get(3));

        long list[] = lm.getKeyArray();
        assertEquals("Should be zero", 0, list.length);
        assertEquals( "Size should match array length", list.length, lm.size());
    }

    /**
     * second get with first put
     * @throws Exception a serious problem
     */
    public void testSecondGet() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );
        lm.put( 3, "A");
        assertNotNull( "Should return without error", lm.get(3));

        long list[] = lm.getKeyArray();
        assertEquals("Should be one", 1, list.length);
        assertEquals( "Size should match array length", list.length, lm.size());
    }

    /**
     * NPE from getKeyArray
     * @throws Exception a serious problem
     */
    public void testGetKeyArrayOverflowWithUnkownSort() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        long list[]=new long[0];

        lm.putMultiRows(list, "A", HashLongMap.State.UNIQUE);

        lm.put( 3, "A");

        long tempList[] = lm.getKeyArray();
        assertNotNull( "should be able to get", tempList);

        assertEquals( "Correct length", 1, tempList.length);
        assertEquals( "Size should match array length", tempList.length, lm.size());

        assertEquals( "Correct value", 3, tempList[0]);
    }

    /**
     * should get a the overflow value.
     * @throws Exception a serious problem
     */
    public void testGetOverflow() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        long list[]=new long[1000000];
        for( int i =0;i < 1000000;i++)
        {
            list[i] = i + 5;
        }

        lm.putMultiRows(list, "A", HashLongMap.State.UNIQUE);

        lm.put( 3, "A");

        assertNotNull( "should be able to get", lm.get(3));
    }

    /**
     * should clear the overflow value.
     * @throws Exception a serious problem
     */
    public void testClearOverflow() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        long list[]=new long[1000000];
        for( int i =0;i < 1000000;i++)
        {
            list[i] = i + 5;
        }

        lm.putMultiRows(list, "A", HashLongMap.State.UNIQUE);

        lm.put( 3, "A");

        assertNotNull( "should be able to get", lm.get(3));

        lm.clear();

        lm.putMultiRows(list, "A", HashLongMap.State.UNIQUE);

        assertNull( "should be cleared", lm.get(3));

    }

    /**
     * should not initialize if large and small
     * @throws Exception a serious problem
     */
    public void testLargeSmallPutAll() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        long list[]=new long[1000000];
        for( int i =0;i < 1000000;i++)
        {
            list[i] = i + 5;
        }

        lm.putMultiRows(list, "A", HashLongMap.State.UNIQUE);

        long zero[]=new long[0];

        lm.putMultiRows(zero, null, HashLongMap.State.UNIQUE);

        if( lm instanceof HashLongMapV6)
        {
            assertFalse( "Should not need to initialized", lm.isInitialized());
            assertTrue( "Should be sorted", lm.isKeyArraySorted());
        }

        long one[]=new long[]{list.length * 2};

        lm.putMultiRows(one, "A", HashLongMap.State.UNIQUE);

        if( lm instanceof HashLongMapV6)
        {
            assertFalse( "Should not need to initialized", lm.isInitialized());
            assertTrue( "Should be sorted", lm.isKeyArraySorted());
        }

        assertTrue( "contains set one", lm.containsKey(100));
        assertTrue( "contains set two", lm.containsKey(one[0]));
    }

    /**
     * check that the lazy add handles sort.
     * @throws Exception a serious problem
     */
    public void testLazyAddIsSorted() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        long list[]=new long[1000000];
        for( int i =0;i < 1000000;i++)
        {
            list[i] = i + 5;
        }

        lm.putMultiRows(list, "A", HashLongMap.State.UNIQUE);

        if( lm instanceof HashLongMapV6)
        {
            assertFalse( "Should not need to initialized", lm.isInitialized());
            assertTrue( "Should be sorted", lm.isKeyArraySorted());
        }

        lm.put( 3, "A");
        if( lm instanceof HashLongMapV6)
        {
            assertFalse( "Should not need to initialized", lm.isInitialized());
        }
        assertFalse( "Should not be sorted", lm.isKeyArraySorted());
    }

    /**
     * check that the lazy add handles sort.
     * @throws Exception a serious problem
     */
    public void testLazyAddIsSorted2() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        long list[]=new long[1000000];
        for( int i =0;i < 1000000;i++)
        {
            list[i] = i + 5;
        }

        lm.putMultiRows(list, "A", HashLongMap.State.UNIQUE);

        if( lm instanceof HashLongMapV6)
        {
            assertFalse( "Should not need to initialized", lm.isInitialized());
        }

        lm.put( 3, "A");
        if( lm instanceof HashLongMapV6)
        {
            assertFalse( "Should not need to initialized", lm.isInitialized());
        }
        assertFalse( "Should not be sorted", lm.isKeyArraySorted());
    }

    /**
     * check that we can add one without putting everything into elements.
     * @throws Exception a serious problem
     */
    public void testLazyBrief() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        long list[]=new long[1000000];
        for( int i =0;i < 1000000;i++)
        {
            list[i] = i + 5;
        }

        lm.putMultiRows(list, "A", HashLongMap.State.UNIQUE);

        long[] briefKeyArray = lm.briefKeyArray();
        assertEquals( "brief array should be short", 200, briefKeyArray.length);

        if( lm instanceof HashLongMapV6)
        {
            assertFalse( "Should not need to initialized", lm.isInitialized());
        }
        assertEquals( "Size should be correct", list.length, lm.size());

        for( int j = 0;j < 1000; j++)
        {
            long search=list[list.length - j -1];

            boolean found = lm.containsKey(search);

            if( found )
            {
                assertNotNull("if we contain a key then we should be able to get the value", lm.get(search));
            }
            boolean shouldFind= search == 3 || search > 4;

            if( found != shouldFind)
            {
                fail( "we should " + ( shouldFind ? "" : "NOT ") + "have found " + search);
            }
        }

        if( lm instanceof HashLongMapV6)
        {
            assertTrue( "Should be sorted", lm.isKeyArraySorted());
        }
        assertEquals( "Size should match", lm.size(), lm.getKeyArray().length);
    }

    /**
     * check that we can add one without putting everything into elements.
     * @throws Exception a serious problem
     */
    public void testShouldBeAbleToAddOne() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        long list[]=new long[1000000];
        for( int i =0;i < 1000000;i++)
        {
            list[i] = i + 5;
        }

        lm.putMultiRows(list, "A", HashLongMap.State.UNIQUE);

        lm.put(3, "A");

        if( lm instanceof HashLongMapV6)
        {
            assertFalse( "Should not need to initialized", lm.isInitialized());
        }
        assertEquals( "Size should be correct", list.length + 1, lm.size());

        for( int j = 0;j < 1000; j++)
        {
            long search=list[list.length - j -1];

            boolean found = lm.containsKey(search);
            if( found )
            {
                assertNotNull("if we contain a key then we should be able to get the value", lm.get(search));
            }

            boolean shouldFind= search == 3 || search > 4;

            if( found != shouldFind)
            {
                fail( "we should " + ( shouldFind ? "" : "NOT ") + "have found " + search);
            }
        }

        if( lm instanceof HashLongMapV6)
        {
            lm.getSortedKeyArray();
            assertTrue( "Should be sorted", lm.isKeyArraySorted());
        }
        assertEquals( "Size should match", lm.size(), lm.getKeyArray().length);
    }

    /**
     * check that we can add one without putting everything into elements.
     * @throws Exception a serious problem
     */
    public void testShouldBeAbleToAddOneClone() throws Exception
    {
        HashLongMap lm2 = HashLongMapFactory.create( );

        long list[]=new long[1000000];
        for( int i =0;i < 1000000;i++)
        {
            list[i] = i + 5;
        }

        lm2.putMultiRows(list, "A", HashLongMap.State.UNIQUE);

        lm2.put(3, "A");
        HashLongMap lm= (HashLongMap) lm2.clone();
        if( lm instanceof HashLongMapV6)
        {
            assertFalse( "Should not need to initialized", lm.isInitialized());
        }
        assertEquals( "Size should be correct", list.length + 1, lm.size());

        for( int j = 0;j < 1000; j++)
        {
            long search=list[list.length - j -1];

            boolean found = lm.containsKey(search);

            if( found )
            {
                assertNotNull("if we contain a key then we should be able to get the value", lm.get(search));
            }

            boolean shouldFind= search == 3 || search > 4;

            if( found != shouldFind)
            {
                fail( "we should " + ( shouldFind ? "" : "NOT ") + "have found " + search);
            }
        }

        //assertTrue( "Should be sorted", lm.isKeyArraySorted());

        assertEquals( "Size should match", lm.size(), lm.getKeyArray().length);

    }

    /**
     * check that we can add one without putting everything into elements.
     * @throws Exception a serious problem
     */
    public void testAddAndRemoveOneWithoutInitialize() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        long list[]=new long[1000000];
        for( int i =0;i < 1000000;i++)
        {
            list[i] = i + 5;
        }

        lm.putMultiRows(list, "A", HashLongMap.State.UNIQUE_SORTED);

        lm.put(3, "A");
        lm.remove(3);
        lm.remove(2);
        assertFalse( "should not find", lm.containsKey(3));

        if( lm instanceof HashLongMapV6)
        {
            assertFalse( "Should not need to initialized for one", lm.isInitialized());
        }
        assertEquals( "Size should be correct", list.length, lm.size());

        for( int j = 0;j < 1000; j++)
        {
            long search=list[list.length - j -1];

            boolean found = lm.containsKey(search);
            if( found )
            {
                assertNotNull("if we contain a key then we should be able to get the value", lm.get(search));
            }

            boolean shouldFind= search > 4;

            if( found != shouldFind)
            {
                fail( "we should " + ( shouldFind ? "" : "NOT ") + "have found " + search);
            }
        }

         assertTrue( "Should be sorted", lm.isKeyArraySorted());
    }

    /**
     * check that we can add and remove one without putting everything into elements.
     * @throws Exception a serious problem
     */
    public void testAddAndRemoveOneWithoutInitializeClone() throws Exception
    {
        HashLongMap lm2 = HashLongMapFactory.create( );

        long list[]=new long[1000000];
        for( int i =0;i < 1000000;i++)
        {
            list[i] = i + 5;
        }

        lm2.putMultiRows(list, "A", HashLongMap.State.UNIQUE);

        lm2.put(3, "A");
        HashLongMap lm= (HashLongMap) lm2.clone();
        lm.remove(3);
        lm.remove(2);
//        assertFalse( "Should not need to initialized for one", lm.isInitialized());
        assertEquals( "Size should be correct", list.length, lm.size());
        assertFalse( "should not find", lm.containsKey(3));

        for( int j = 0;j < 1000; j++)
        {
            long search=list[list.length - j -1];

            boolean found = lm.containsKey(search);
            if( found )
            {
                assertNotNull("if we contain a key then we should be able to get the value", lm.get(search));
            }

            boolean shouldFind= search > 4;

            if( found != shouldFind)
            {
                fail( "we should " + ( shouldFind ? "" : "NOT ") + "have found " + search);
            }
        }

//        assertTrue( "Should be sorted", lm.isKeyArraySorted());
    }

    /**
     * check that a number of calls to contains will sort.
     * @throws Exception a serious problem
     */
    public void testShouldSortIfNeededContains() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        long list[]=new long[1000001];
        for( int i =0;i < 1000000;i++)
        {
            list[i] = i + 5;
        }

        list[1000000]=3;

        lm.putMultiRows(list, "A", HashLongMap.State.UNIQUE);

        for( int j = 0;j < 1000; j++)
        {
            long search=list[list.length - j -1];

            boolean found = lm.containsKey(search);
            if( found )
            {
                assertNotNull("if we contain a key then we should be able to get the value " + search, lm.get(search));
            }

            boolean shouldFind= search == 3 || search > 4;

            if( found != shouldFind)
            {
                fail( "we should " + ( shouldFind ? "" : "NOT ") + "have found " + search);
            }
        }

        if( lm instanceof HashLongMapV6)
        {
            assertTrue( "Should be sorted", lm.isKeyArraySorted());
        }
    }

    /**
     * check that a number of calls to get will sort.
     * @throws Exception a serious problem
     */
    public void testShouldSortIfNeededGet() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        long list[]=new long[10000001];
        for( int i =0;i < 10000000;i++)
        {
            list[i] = i + 5;
        }

        list[10000000]=3;

        lm.putMultiRows(list, "A", HashLongMap.State.UNIQUE);

        for( int j = 0;j < 1000; j++)
        {
            long search=list[list.length - j -1];
            boolean found = lm.get(search) != null;

            boolean shouldFind= search == 3 || search > 4;

            if( found != shouldFind)
            {
                fail( "we should " + ( shouldFind ? "" : "NOT ") + "have found " + j);
            }
        }

        if( lm instanceof HashLongMapV6)
        {
            assertTrue( "Should be sorted", lm.isKeyArraySorted());
        }
    }

    /**
     * if it only has < 2 rows then it's is sorted.
     * @throws Exception a serious problem
     */
    public void testMarkSmallAsSorted() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        long list[]=new long[0];

        lm.putMultiRows(list, "A", HashLongMap.State.UNIQUE);

        if( lm instanceof HashLongMapV6)
        {
            assertTrue( "Should be sorted", lm.isKeyArraySorted());
        }

        list=new long[1];
        list[0]=6;
        lm.putMultiRows(list, "B", HashLongMap.State.UNIQUE);

        if( lm instanceof HashLongMapV6)
        {
            assertTrue( "should be sorted", lm.isKeyArraySorted());
        }
    }

    /**
     * check that a number of calls to get will sort.
     * @throws Exception a serious problem
     */
    public void testShouldSortIfNeededRemove() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        long list[]=new long[1000001];
        for( int i =0;i < 1000000;i++)
        {
            list[i] = i + 5;
        }

        list[1000000]=3;

        lm.putMultiRows(list, "A", HashLongMap.State.UNIQUE);

        for( int j = 0;j < 100000; j++)
        {
            boolean found = lm.remove(j * -1) != null;

            if( found != false)
            {
                fail( "we should NOT have found " + j);
            }
        }

        if( lm instanceof HashLongMapV6)
        {
            assertTrue( "Should be sorted", lm.isKeyArraySorted());
        }
        else
        {
            Thread.sleep(1000);// even up the time a bit.
        }
    }

    /**
     * Check that we do not change the orginal list ( maybe used by another thread somewhere else)
     * @throws Exception a serious problem
     */
    public void testLazySort() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        long list[]={3,2,1};
        lm.putMultiRows(list, "A", HashLongMap.State.UNIQUE);

        long sortedList[]=lm.getSortedKeyArray();
        assertEquals( "should be sorted", 1, sortedList[0]);

        assertEquals( "Orginal should be unsorted", 3, list[0]);
    }


    /**
     * Check that we do not change the orginal list ( maybe used by another thread somewhere else)
     * @throws Exception a serious problem
     */
    public void testLazySortClone() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        long list[]={3,2,1};
        lm.putMultiRows(list, "A", HashLongMap.State.UNIQUE);
        HashLongMap lmc = (HashLongMap) lm.clone();
        long sortedList[]=lmc.getSortedKeyArray();
        assertEquals( "should be sorted", 1, sortedList[0]);

        assertEquals( "Orginal should be unsorted", 3, list[0]);
    }

    /**
     * Check the remove.
     * @throws Exception a serious problem
     */
    public void testLazyInitializeRemove() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );
        assertNull( "should not find ( was NPE)", lm.remove(777));
    }

    /**
     * check we can handle very large sets.
     *
     * @throws Exception a serious problem
     */
    public void testVeryLargeUnionUnSorted() throws Exception
    {
        HashLongMap largeMap = HashLongMapFactory.create( );
        HashLongMap smallMap = HashLongMapFactory.create( );
        for( int i = 0; i < 100000;i++)
        {
            long row = (long) (Math.random() * Long.MAX_VALUE);
            if( row > 0)
            {
                if( row % 1000 == 0)
                {
                    smallMap.put(row, "");
                }
                else
                {
                    largeMap.put(row, "");
                }
            }
        }

        HashLongMap fullMap = HashLongMapFactory.create( );
        long largeList[]=largeMap.getKeyArray();
        fullMap.putMultiRows(largeList, "", HashLongMap.State.UNIQUE);
        long smallList[]=smallMap.getKeyArray();
        fullMap.putMultiRows(smallList, "",HashLongMap.State.UNIQUE);
        assertEquals( "Should have merged", fullMap.size(), 100000);
    }


    /**
     * Check that we are not initialized first off.
     * @throws Exception a serious problem
     */
    public void testLazyInitialize() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );
        if( lm instanceof HashLongMapV6)
        {
            assertFalse( "should not be initialized", lm.isInitialized());
        }
        lm.clear();
        if( lm instanceof HashLongMapV6)
        {
            assertFalse( "Should not need to initialized", lm.isInitialized());
        }

        assertFalse( "should not find ( was NPE)", lm.containsKey(888));
        assertNull( "should not find ( was NPE)", lm.get(777));
    }

    /**
     * Check that we detect that the rows are sorted.
     * @throws Exception a serious problem
     */
    public void testPutAllCheckSorted() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );
        long list[] = new long[10000];
        for( int i =0; i < list.length; i++)
        {
            list[i]=i;
        }

        lm.putMultiRows(list, "A",HashLongMap.State.UNIQUE);

        if( lm instanceof HashLongMapV6)
        {
            assertTrue("should be known as sorted", lm.isKeyArraySorted());
        }

        lm.clear();

        if( lm instanceof HashLongMapV6)
        {
            assertTrue("should be known as sorted", lm.isKeyArraySorted());
        }
        list[999]=29;
        lm.putMultiRows(list, "A",HashLongMap.State.UNIQUE);
        assertFalse("should be known as NOT sorted", lm.isKeyArraySorted());
    }

    /**
     * Check that we are not initialized first off.
     * @throws Exception a serious problem
     */
    public void testTwoPutAll() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        long list1[]={1,2,3};
        lm.putMultiRows(list1, "A",HashLongMap.State.UNIQUE);
        long list2[]={4,5,6};
        lm.putMultiRows(list2, "B",HashLongMap.State.UNIQUE);

        for( long key:list1)
        {
            assertEquals( "check keys", "A", lm.get(key));
        }

        for( long key:list2)
        {
            assertEquals( "check keys", "B", lm.get(key));
        }
    }

    /**
     * Check that we are not initialized first off.
     * @throws Exception a serious problem
     */
    public void testLazyInitializePutAll() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );
        long list[] = new long[10000];
        for( int i =0; i < list.length; i++)
        {
            list[i]=i;
        }
        lm.putMultiRows(list, "ABC",HashLongMap.State.UNIQUE);

        check( lm,list.length);
    }

    /**
     * Check that we are not initialized first off.
     * @throws Exception a serious problem
     */
    public void testLazyCloneInitializePutAll() throws Exception
    {
        HashLongMap lm1 = HashLongMapFactory.create( );
        long list[] = new long[10000];
        for( int i =0; i < list.length; i++)
        {
            list[i]=i;
        }
        lm1.putMultiRows(list, "ABC",HashLongMap.State.UNIQUE);

        HashLongMap lm = (HashLongMap) lm1.clone();
        assertEquals( "should copy the size", list.length, lm1.size());

        check( lm,list.length);
    }

    private void check( HashLongMap lm, int size)
    {
        if( lm instanceof HashLongMapV6)
        {
            assertFalse( "should not be initialized yet", lm.isInitialized());
        }
        long shortList[] = lm.briefKeyArray();

        if( shortList == null || shortList.length < 200 )
        {
            fail( "short list failed " + Arrays.toString(shortList));
        }

        if( lm instanceof HashLongMapV6)
        {
            assertFalse( "should not be initialized yet", lm.isInitialized());
        }

        long longList[] = lm.getKeyArray();

        if( longList == null || longList.length != size )
        {
            fail( "long list failed " + Arrays.toString(longList));
        }

        if( lm instanceof HashLongMapV6)
        {
            assertFalse( "Should not need to initialized", lm.isInitialized());
        }
        assertTrue( "Should be sorted", lm.isKeyArraySorted());

        long sortedList[] = lm.getSortedKeyArray();

        if( sortedList == null || sortedList.length != size )
        {
            fail( "sorted list failed " + Arrays.toString(sortedList));
        }

        if( lm instanceof HashLongMapV6)
        {
            assertFalse( "Should not need to initialized", lm.isInitialized());
        }

        assertTrue( "should contain this value", lm.containsValue("ABC"));
        if( lm instanceof HashLongMapV6)
        {
            assertFalse( "Should not need to initialized", lm.isInitialized());
        }

        assertTrue( "should contain this value", lm.containsKey(512));
        if( lm instanceof HashLongMapV6)
        {
            assertFalse( "Should not need to initialized", lm.isInitialized());
        }

        assertEquals( "Should be able to get this value without initialize", "ABC", lm.get(678));
        if( lm instanceof HashLongMapV6)
        {
            assertFalse( "Should not need to initialized", lm.isInitialized());
        }

        lm.put(999, "ABC");

        assertEquals( "Should be able to get this value without initialize", "ABC", lm.get(999));
        if( lm instanceof HashLongMapV6)
        {
            assertFalse( "Should not need to initialized", lm.isInitialized());
        }

        assertNull( "Should be able to remove the key if missing without initialize", lm.remove(-1));
        if( lm instanceof HashLongMapV6)
        {
            assertFalse( "Should not need to initialized", lm.isInitialized());
        }

        lm.put(1234, "XYZ");

        assertEquals( "Should be able to get this value without initialize", "XYZ", lm.get(1234));
        assertTrue( "should be initialized now", lm.isInitialized());
        assertEquals( "Should be able to get this value without initialize", "ABC", lm.get(777));
        assertEquals( "Don't double count", size, lm.size());

        lm.clear();
        if( lm instanceof HashLongMapV6)
        {
            assertFalse( "Should not need to initialized", lm.isInitialized());
        }
        assertEquals( "should clear the size", 0, lm.size());
        assertNull( "Should be able to get this value without initialize", lm.get(777));
        shortList = lm.briefKeyArray();

        if( shortList == null || shortList.length != 0 )
        {
            fail( "short list failed " + Arrays.toString(shortList));
        }

        longList = lm.getKeyArray();

        if( longList == null || longList.length != 0 )
        {
            fail( "long list failed " + Arrays.toString(longList));
        }

        if( lm instanceof HashLongMapV6)
        {
            assertFalse( "Should not need to initialized", lm.isInitialized());
        }
        assertTrue( "Should be sorted", lm.isKeyArraySorted());

        sortedList = lm.getSortedKeyArray();

        if( sortedList == null || sortedList.length != 0 )
        {
            fail( "sorted list failed " + Arrays.toString(sortedList));
        }
    }

    /**
     * check is sorted.
     * @throws Exception a serious problem
     */
    public void testIsSorted() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );

        for( int i = 0; i < 100; i++)
        {
            lm.put(i, i + "");
        }

        long keys[] = lm.getKeyArray();

        boolean sorted = true;

        for( int i = 0; sorted == true && i < 100;i++)
        {
            if( i != keys[i]) sorted = false;
        }

        assertEquals( "Should identify if sorted or not", sorted, lm.isKeyArraySorted());
    }

    /**
     * check is sorted.
     * @throws Exception a serious problem
     */
    public void testKeySortedArray() throws Exception
    {
        HashLongMap lm = HashLongMapFactory.create( );
        int size=18430;
        for( int i = size/2; 0 < i; )
        {
            i--;
            lm.put(i*1000, i + "");
        }
        for( int i = size/2; i < size;i++ )
        {
            lm.put(i*1000, i + "");
        }

        long keys[] = lm.getSortedKeyArray();
        assertEquals( "size of sorted array", size, keys.length);
        boolean sorted = true;

        for( int i = 0; sorted == true && i < size;i++)
        {
            assertEquals( "elements should come out sorted",  i*1000, keys[i]);
        }

        assertTrue( "when we call keyStoredArray it should always be true", lm.isKeyArraySorted());
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.memory.selftest.TestHashLongMap");//#LOGGER-NOPMD
}
