/*
 *  Copyright (c) 2001-2004 ASP Converters pty ltd
 *
 *  www.stSoftware.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.memory.internal.selftest;

import com.aspc.remote.memory.internal.CommonTable;
import com.aspc.remote.memory.MemoryManager;
import com.aspc.remote.util.misc.NumUtil;
import java.util.ArrayList;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 *  Check the re-hash logic of CommonTable
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED self test unit</i>
 *
 *  @author         Nigel Leck
 *  @since          29 September 2006
 *  
 */
public class TestCommonTable extends TestCase
{
    /**
     * Check the simple operations
     *
     * @throws Exception a serious problem
     */
    public void testSimple() throws Exception
    {
        CommonTable table = new CommonTable("dummy common");

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        ArrayList list = new ArrayList();
        for( int i = 1; i <= 100; i++)
        {
            DummyObject dummy = new DummyObject( i, i);

            list.add( dummy);
            table.intern( dummy);
        }
        
        for( int i = 1; i <= 100; i++)
        {
            DummyObject dummy = new DummyObject( i, i);
            DummyObject dummy2;
            dummy2 = (DummyObject)table.intern( dummy);

            if( dummy2 == dummy)
            {
                fail( "Should have recycled the object " + dummy);
            }
            
            assertEquals( "must match values ", dummy.value, dummy2.value);
        }
    }

    /**
     * Check that when objects are released that they do not continue to grow the 
     * size of the common table.
     * @throws java.lang.InterruptedException
     */
    public void testPhantomGrowth() throws InterruptedException
    {
        CommonTable table = new CommonTable("dummy common");

        for( int i = 1; i <= 10000000; i++)
        {
            if( i %1000000 == 0)
            {
                MemoryManager.gc();
                Thread.sleep(100);
            }
            
            DummyObject dummy = new DummyObject( i, i);

            table.intern( dummy);
        }

        long size = table.estimatedMemoryInBytes();

        // we must have not compacted at all. 
        if( size > 10000000 * 8 )
        {
            fail( "There are no live objects so this table should not grow large " + NumUtil.convertMemoryToHumanReadable(size));
        }
    }

    /**
     * Check that the count remains actuate
     *
     * @throws Exception a serious problem
     */
    public void testCount() throws Exception
    {
        CommonTable table = new CommonTable("dummy common");

        /**
         * Load the hash table will many objects with the same hash code, to form a large linked list
         * but only keep a hard reference to some of them.
         */
        ArrayList list = new ArrayList();
        for( int i = 1; i <= 1000; i++)
        {
            DummyObject dummy = new DummyObject( 1, i);

            if( (i % 7 == 0 || i % 5 == 0|| i % 6 == 0) == false)
            {
                list.add( dummy);
            }

            table.intern( dummy);
        }

        MemoryManager.gc();

        table.compact();
        
        assertEquals( "Counts should match", list.size(), table.getCount());
    }

    /**
     * Check that we do not lose entries when a member ( or set ) of the linked 
     * list has been cleared by the GC
     *
     * @throws Exception a serious problem
     */
    public void testReleaedEntryInLinkedList() throws Exception
    {
        CommonTable table = new CommonTable("dummy common");

        /**
         * Load the hash table will many objects with the same hash code, to form a large linked list
         * but only keep a hard reference to some of them.
         */
        ArrayList list = new ArrayList();
        for( int i = 1; i <= 1000; i++)
        {
            DummyObject dummy = new DummyObject( 1, i);

            if( (i % 7 == 0 || i % 5 == 0|| i % 6 == 0) == false)
            {
                list.add( dummy);
            }

            table.intern( dummy);
            if( i % 100 == 0)
            {
                MemoryManager.gc( );
            }
        }

        for (Object list1 : list) {
            DummyObject dummy = (DummyObject) list1;
            DummyObject dummyTemp = new DummyObject( dummy.hashValue, dummy.value);
            DummyObject dummyRecycled = (DummyObject)table.intern( dummyTemp);
            if( dummyRecycled != dummy)
            {
                table.dump();

                fail( "we lost a reference " + dummy);
            }
        }
    }

    /**
     * Check that we can find hashs that don't match
     *
     * @throws Exception a serious problem
     */
    @SuppressWarnings("UnusedAssignment")
    public void testLinkedList() throws Exception
    {
        CommonTable table = new CommonTable("dummy common");

        ArrayList list = new ArrayList();
        for( int i = 1; i <= 100; i++)
        {
            DummyObject dummy = new DummyObject( i%2, i);

            list.add( dummy);
            table.intern( dummy);
        }

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        ArrayList list2 = new ArrayList();
        
        for( int i = 1; i <= 100; i++)
        {
            DummyObject dummy = new DummyObject( i % 2, i + 1000);
            table.intern( dummy);

            list2.add( dummy);
            table.intern( dummy);
        }

        list.clear();
        MemoryManager.gc( );
        list = null;
        MemoryManager.gc( );

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        ArrayList list3 = new ArrayList();
        
        for( int i = 1; i <= 100; i++)
        {
            DummyObject dummy = new DummyObject( i % 2, i + 2000);
            table.intern( dummy);

            list3.add( dummy);
            table.intern( dummy);
        }

        for( int i = 1; i <= 100; i++)
        {
            DummyObject dummy = new DummyObject( i % 2, i + 1000);
            DummyObject dummy2 = (DummyObject)table.intern( dummy);

            if( dummy2 == dummy)
            {
                table.dump();

                fail( "we lost a reference " + dummy);
            }
        }
    }

    /**
     * Check that we swap when missing objects
     *
     * @throws Exception a serious problem
     */
    @SuppressWarnings("UnusedAssignment")
    public void testSwap() throws Exception
    {
        CommonTable table = new CommonTable("dummy common");

        ArrayList list = new ArrayList();
        for( int i = 1; i <= 100; i++)
        {
            DummyObject dummy = new DummyObject( 1, i);

            list.add( dummy);
            table.intern( dummy);
        }

        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        ArrayList list2 = new ArrayList();
        
        for( int i = 1; i <= 100; i++)
        {
            DummyObject dummy = new DummyObject( 101-i, i + 1000);
            table.intern( dummy);

            list2.add( dummy);
            table.intern( dummy);
        }

        list.clear();
        MemoryManager.gc( );
        list = null;
        MemoryManager.gc( );

        // Should cause a big reshuffle.
        DummyObject temp = new DummyObject( 1, 1);

        table.intern( temp);

        for( int i = 1; i <= 100; i++)
        {
            DummyObject dummy = new DummyObject( 101-i, i + 1000);
            DummyObject dummy2 = (DummyObject)table.intern( dummy);

            if( dummy2 == dummy)
            {
                table.dump();

                fail( "we lost a reference " + dummy);
            }
        }
    }

    /**
     * Creates new VirtualDBTestUnit
     * @param name The name of the test unit
     */
    public TestCommonTable(final String name)
    {
        super( name);
    }

    /**
     * @param args  */
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }

    /**
     * @return the suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite( TestCommonTable.class);
                
        return suite;
    }
}
