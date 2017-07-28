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

import com.aspc.remote.memory.LargeLongArray;
import com.aspc.remote.memory.LargeLongArray.SanityArrayCounter;
import com.aspc.remote.util.misc.*;
import com.aspc.remote.util.timer.StopWatch;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.commons.logging.Log;

/**
 * Check long array sorting.
 *
 *  @author      Nigel Leck
 *  @since       21 Oct 2015
 */
public class TestLargeLongArray extends TestCase
{
    /**
     * Constructor for the test unit.
     * @param testName The name of the test unit
     */
    public TestLargeLongArray(String testName)
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
        Test test=suite();
//        test=TestSuite.createTest(TestLargeLongArray.class, "testSortSegment");
        TestRunner.run(test);
    }

    /**
     * Creates the test suite.
     *
     * @return The test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestLargeLongArray.class);
        return suite;
    }
    private int randomInt( final int min, final int max)
    {
        if( min >= max ) throw new IllegalArgumentException("the minimum value (" + min +") must be less than the max value (" + max + ")");
        int range=(max + 1)-min;
        
        int randomDelta=(int)( range * Math.random());
        randomDelta=Math.abs(randomDelta);
        int value=min + randomDelta;
        
        assert value >= min: "the calculated random number "+ value +" is less than the minimum " + min;
        assert value <= max: "the calculated random number "+ value +" is greater than the maximum " + max;
        
        return value;
    }
    
    public void testValidateZeroAndExpectedCapacity()
    {
        int vars[][]={
            {
                123,
                0
            },
            {
                123,
                0
            },
            {
                1234,
                1234
            },
            {
                2049,
                123
            },
            {
                randomInt(1, 4096),
                randomInt(1, 4096)
            }
        };
        
        for( int flags=0;flags<=0b00111111;flags++)
        {
            for( int var[]: vars)
            {
                LargeLongArray.Builder b = LargeLongArray
                    .factory()
                    .setExpectedCapacity(var[0])
                    .assertNonZero(   (flags&0b00000001)!=0)
                    .setOutputShared( (flags&0b00000010)!=0)
                    .setInputShared(  (flags&0b00000100)!=0)
                    .validateUnique(  (flags&0b00001000)!=0)
                    .assertUnique(    (flags&0b00010000)!=0)
                    .validateNonZero( (flags&0b00100000)!=0);
                
                LargeLongArray lla = b.build();
                HashSet<Long> unique=new HashSet();

                for( int pos=0;pos< var[1];pos++)
                {
                    int key=randomInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
                    if( key == 0) continue;
                    if( unique.add((long)key))
                    {
                        lla.append(key);
                    }
                }

                long last=Long.MIN_VALUE;
                long data[][]=lla.sort();
                for( long[] values: data)
                {
                    for( long value: values)
                    {
                        boolean found=unique.remove(value);
                        assertTrue( b + " remove: " + value, found);
                        
                        assertTrue( last +"->" + value, value>last);
                        last=value;
                    }
                }

                assertTrue( b + " should be now empty", unique.isEmpty());
            }
        }
    }
    public void testReplaceValidate()
    {
        LargeLongArray lla = LargeLongArray.factory().validateUnique(true).build();
        
        for( int step=0;step< 100;step++)
        {
            lla.append(step * 10);
        }
        
        int count=lla.replace(10, 11);
        assertEquals( "Check replace count",1, count);
        assertEquals( "Should have been replaced",11, lla.get(1));
        try{
            lla.replace(11, 21);
            fail( "should have detected issue");
        }
        catch(IllegalArgumentException iae)
        {
            // GOOD
        }
    }

    public void testReplace()
    {
        LargeLongArray lla = LargeLongArray.factory().build();
        
        for( int step=0;step< 100;step++)
        {
            lla.append(step * 10);
        }
        
        int count=lla.replace(10, 11);
        assertEquals( "Check replace count",1, count);
        assertEquals( "Should have been replaced",11, lla.get(1));
        try{
            lla.replace(11, 21);
        }
        catch(IllegalArgumentException iae)
        {
            fail( "should NOT have detected issue");
        }
    }
        
    public void testRemoveReAllocate()
    {
        final AtomicLong counter=new AtomicLong();
        SanityArrayCounter sac=() -> {
            counter.incrementAndGet();            
        };
        
        LargeLongArray lla = LargeLongArray.factory().setSanity(sac).build();
        
        for( long c=-10000;c < 10000;c++)
        {
            lla.append(c);
        }
        
        for( long line[]: lla.repack())
        {
            for(long c: line)
            {
                if( lla.remove(c)==false)
                {
                    LOGGER.info( "did not find: " + c);
                }
            }
        }
        
        assertEquals("should be zero remaining", 0, lla.size());
        
        if( counter.get() < 1)
        {
            fail( "should have created at least one array was: " + counter);
        }
//        
//        if( counter.get() > 100)
//        {
//            fail( "should not have created as many arrays: " + counter);
//        }        
    }

    public void testCapacityMin()
    {
        LargeLongArray.Builder factory = LargeLongArray.factory();
        try{
            factory.setExpectedCapacity(-5);
            fail( "should not be able to set capacity to a negative");
        }
        catch( IllegalArgumentException iae)
        {
        }
        final AtomicLong counter=new AtomicLong();
        SanityArrayCounter sac=() -> {
            counter.incrementAndGet();            
        };
        factory.setSanity(sac);
        factory.setExpectedCapacity(5);
        
        LargeLongArray lla = factory.build();
        
        for( int pos=0;pos<1000;pos++)
        {
            lla.append(pos);
        }
        
        assertEquals( "Allocations", 1l, counter.get());
        for( int pos=0;pos<2000;pos++)
        {
            lla.append(pos * -1);
        }
        
        assertEquals( "Allocations", 3l, counter.get());
    }
    
    public void testAppendAndSort5()
    {
        LargeLongArray lla = LargeLongArray.factory().build();
         
        lla.append(-2);
        lla.append(-1);
        long[][] data = lla.sort();
        
        assertEquals( "data length", 1, data.length);
        assertEquals( "row length", 2, data[0].length);
        assertEquals( "value", -2, data[0][0]);
    }
    
    public void testAppendAndSet()
    {
        LargeLongArray lla = LargeLongArray.factory().build();
         
        lla.append(1);
        lla.set(0, 2);
        long[][] data = lla.repack();
        
        assertEquals( "data length", 1, data.length);
        assertEquals( "row length", 1, data[0].length);
        assertEquals( "value", 2, data[0][0]);
    }
    public void testAppendAndSetInvalid()
    {
        LargeLongArray lla = LargeLongArray.factory().build();
         
        lla.append(1);
        try{
            lla.set(1,2);
            fail( "should not be able to set past the end of the array");
         }
         catch( IllegalArgumentException iae)
         {
             
         }
        try{
            lla.set(-1,2);
            fail( "should not be able to set before the start of the array");
         }
         catch( IllegalArgumentException iae)
         {
             
         }
        lla.set(0,2);
        
        long[][] data = lla.repack();
        
        assertEquals( "data length", 1, data.length);
        assertEquals( "row length", 1, data[0].length);
        assertEquals( "value", 2, data[0][0]);
    }
        
    public void testAppendAndSort()
    {
        LargeLongArray lla = LargeLongArray.factory().build();
         
        lla.append(1);
         
        long[][] data = lla.sort();
        
        assertEquals( "data length", 1, data.length);
        assertEquals( "row length", 1, data[0].length);
        assertEquals( "value", 1, data[0][0]);
        
    }
    
    public void testAppendAndSort2()
    {
        LargeLongArray lla = LargeLongArray.factory().build();
         
        lla.append(1);
        lla.append(0);
        lla.remove(0);
        
        long[][] data = lla.sort();
        
        assertEquals( "data length", 1, data.length);
        assertEquals( "row length", 1, data[0].length);
        assertEquals( "value", 1, data[0][0]);
        
    }
    
    public void testAppendAndSort3()
    {
        LargeLongArray lla = LargeLongArray.factory().build();
         
        lla.append(1);
        lla.append(2);
        lla.remove(2);
        
        long[][] data = lla.sort();
        
        assertEquals( "data length", 1, data.length);
        assertEquals( "row length", 1, data[0].length);
        assertEquals( "value", 1, data[0][0]);
        
    }
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void testAppendGet()
    {
         LargeLongArray lla = LargeLongArray.factory().build();
         
         lla.append(1);
         
         assertEquals( "position 0", 1, lla.get( 0));
         
         try{
            lla.get(1);
            fail( "should not be able to get past the end of the array");
         }
         catch( IllegalArgumentException iae)
         {
             
         }
         
        try{
            lla.get(-1);
            fail( "should not be able to get before the start of the array");
         }
         catch( IllegalArgumentException iae)
         {
             
         }
    }
    
    public void testAppendHasPadding()
    {
        final AtomicLong counter=new AtomicLong();
        SanityArrayCounter sac=() -> {
            counter.incrementAndGet();            
        };
        
        LargeLongArray lla = LargeLongArray.factory().setSanity(sac).build();
        HashSet<Long> numbers=new HashSet<>();
        for( long c=-1000;c < 1000;c++)
        {
            lla.append(c);
            numbers.add(c);
        }
        
        for( long line[]: lla.repack())
        {
            for(long c: line)
            {
                if( numbers.remove(c)==false)
                {
                    LOGGER.info( "did not find: " + c);
                }
            }
        }
        
        assertEquals("should be zero remaining", 0, numbers.size());
        
        if( counter.get() < 1)
        {
            fail( "should have created at least one array was: " + counter);
        }
        
        if( counter.get() > 100)
        {
            fail( "should not have created as many arrays: " + counter);
        }        
    }
    
    /**
     * If already sorted we STILL need to copy.
     */    
    public void testOutputSharedSorted()
    {
        long[][] data={{1,2,3},{11,13,15},{},{20}};
        long row0[]=data[0];
        LargeLongArray lla = LargeLongArray.factory(data).build();
        
        long data2[][]=lla.sort();
        if( data == data2)
        {
            fail( "should NOT be the same data object");
        }
        if( row0==data2[0])
        {
            fail( "The ROW object should have been changed");
        }
        
        lla.append(21);
        long data3[][]=lla.repack();
        if( data == data3)
        {
            fail( "should NOT be the same data object");
        }
        
        if( row0==data3[0])
        {
            fail( "The ROW object should be the same");
        }
        
        assertEquals("Out array should have been changed", 8, data3[0].length);
    }
    
    /**
     * Output of the sort method should be a new object.
     */    
    public void testOutputSharedSorted1()
    {
        long[][] data={{1}};
        long row0[]=data[0];
        LargeLongArray lla = LargeLongArray.factory(data).build();
        
        long data2[][]=lla.sort();
        if( data == data2)
        {
            fail( "should NOT be the same data object");
        }
        
        if( row0==data2[0])
        {
            fail( "The ROW object should have been changed");
        }
    }
    /**
     * If already sorted then don't copy on sort.
     */    
    public void testOutputNotSharedSorted2()
    {
        long[][] data={{1}};
        long row0[]=data[0];
        LargeLongArray lla = LargeLongArray.factory(data).setInputShared(false).setOutputShared(false).build();
        
        long data2[][]=lla.sort();
        if( data != data2)
        {
            fail( "should be the same data object");
        }
        if( row0!=data2[0])
        {
            fail( "The ROW object should not have been changed");
        }
        
        lla.append(21);
        long data3[][]=lla.repack();
        if( data != data3)
        {
            fail( "should be the same data object");
        }
        
        if( row0==data3[0])
        {
            fail( "The ROW object should NOT be the same");
        }
    }
    
    /**
     * If already sorted then don't copy on sort.
     */    
    public void testOutputNotSharedSorted3()
    {
        long[][] data={{}};
        long row0[]=data[0];
        LargeLongArray lla = LargeLongArray.factory(data).setOutputShared(false).setInputShared(false).build();
        
        long data2[][]=lla.sort();
        if( data != data2)
        {
            fail( "should be the same data object");
        }
        if( row0!=data2[0])
        {
            fail( "The ROW object should not have been changed");
        }
        
        lla.append(21);
        long data3[][]=lla.repack();
        if( data != data3)
        {
            fail( "should be the same data object");
        }
        
        if( row0==data3[0])
        {
            fail( "The ROW object should be the same");
        }
        
//        assertEquals("Out array should have been changed", 2, data3[3].length);
    }
    
    /**
     * If the output is NOT shared then we don't need to copy the arrays.
     */    
    public void testOutputNotSharedNoCopy()
    {
        long[][] data={{1,2,3},{11,13,15},{},{20}};
        long row0[]=data[0];
        LargeLongArray lla = LargeLongArray.factory(data).setOutputShared(false).build();
        
        long data2[][]=lla.repack();
        if( data != data2)
        {
            fail( "should be the same data object");
        }
        if( row0!=data2[0])
        {
            fail( "The ROW object should not have been changed");
        }
        
        lla.append(21);
        long data3[][]=lla.repack();
        if( data == data3)
        {
            fail( "should NOT be the same data object");
        }
        
        if( row0!=data3[0])
        {
            fail( "The ROW object should be the same");
        }
        
        assertEquals("Out array should have been changed", 2, data3[3].length);
    }
    
    /**
     * The original array should NOT be changed. Even if the output is not shared.  
     */    
    public void testNonMutableOutputNotShared()
    {
        long[][] data={{1,2,3},{11,13,15},{},{20}};
        
        LargeLongArray lla = LargeLongArray.factory(data).setOutputShared(false).build();
        
        lla.remove(2);
        
        assertEquals("original should be unchanged", 3, data[0].length);
        
        lla.append(21);

        assertEquals("Array should not change", 1, data[3].length);
    }
    
    /**
     * The original array should NOT be changed.  
     */    
    public void testNonMutable()
    {
        long[][] data={{1,2,3},{11,13,15},{},{20}};
        
        LargeLongArray lla = LargeLongArray.factory(data).build();
        
        lla.set(0, 5);
        
        assertEquals("original should be unchanged", 1, data[0][0]);
        
        lla.append(21);

        assertEquals("Array should not change", 1, data[3].length);
    }
    
    /**
     * No need to copy if not shared.
     */    
    public void testDoNotCopyIfNotShared()
    {
        long[][] data={{1,2,3},{11,13,15},{},{20}};
        
        LargeLongArray lla = LargeLongArray.factory(data).setInputShared(false).build();
        
        lla.set(0, 5);
        
        assertEquals("original should be changed", 5, data[0][0]);
        
        lla.append(21);

        assertEquals("Size", 8, lla.size());
    }
    
    /**
     * Don't allow a value to be set if it will make the array unsorted.
     */
    public void testSetToUnsorted()
    {
        long[][] data={{1,2,3},{11,13,15},{},{20}};
        
        LargeLongArray lla = LargeLongArray.factory(data).validateUnique(true).build();        
        
        try
        {
            lla.set(0, 5);
            fail( "should not allow non sorted values to be set");
        }
        catch( IllegalArgumentException e)
        {
            // Expected. 
        }
        
        lla.set(3, 5);
    }
    
    public void testRemoveZero()
    {
        int counter=0;
        long data[][]=new long[123][];
        int pos=1;
        for( int j=0;j<data.length;j++)
        {
            long rows[]=new long[2345];
            data[j]=rows;
            counter+=rows.length;
            rows[pos]=counter;
            pos+=3;
            rows[pos]=counter;
        }

        LargeLongArray lla = LargeLongArray.factory(data).build();

        if( lla.remove(0)==false)
        {
            fail( "did not remove zero");
        }

        assertEquals( "size", 123 * 2, lla.size());
        
        long data2[][]=lla.repack();
        pos=0;
        for( long rows2[]:data2)
        {
            for( long value2: rows2)
            {
                if( value2==0)
                {
                    fail( pos + ") found zero");
                }
                pos++;
            }
        }
    }
    
    public void testRemoveZero2()
    {
        int counter=0;
        long data[][]=new long[123][];
        int pos=0;
        for( int j=0;j<data.length;j++)
        {
            long rows[]=new long[2345];
            data[j]=rows;
            counter+=rows.length;
            rows[pos]=counter;
            pos++;
            rows[pos]=counter;
        }

        LargeLongArray lla = LargeLongArray.factory(data).build();

        if( lla.remove(0)==false)
        {
            fail( "did not remove zero");
        }

        assertEquals( "size", 123 * 2, lla.size());
        
        long data2[][]=lla.repack();
        pos=0;
        for( long rows2[]:data2)
        {
            for( long value2: rows2)
            {
                if( value2==0)
                {
                    fail( pos + ") found zero");
                }
                pos++;
            }
        }
    }
    
    public void testRemoveZero3()
    {
        long data[][]=new long[123][];
        
        for( int j=0;j<data.length;j++)
        {
            long rows[]=new long[2345];
            data[j]=rows;
        }

        LargeLongArray lla = LargeLongArray.factory(data).build();

        if( lla.remove(0)==false)
        {
            fail( "did not remove zero");
        }

        assertEquals( "size", 0, lla.size());
        
        long data2[][]=lla.repack();
        int pos=0;
        for( long rows2[]:data2)
        {
            for( long value2: rows2)
            {
                if( value2==0)
                {
                    fail( pos + ") found zero");
                }
                pos++;
            }
        }
    }
  
    public void testSizeCache()
    {
        long data[][]={
            {1,2,3},
            {4,5}
        };
        LargeLongArray lla = LargeLongArray.factory(data).build();
        lla.append(6);
        assertEquals( "Size after append", 6, lla.size());
    }
    
    public void testRemove()
    {
        long value=Long.MAX_VALUE;
        int counter=0;
        long data[][]=new long[123][];
        for( int j=0;j<data.length;j++)
        {
            long rows[]=new long[2345];
            data[j]=rows;
            counter+=rows.length;
            for( int k=0;k< rows.length;k++)
            {
                rows[k]=value;
                value--;
            }
        }

        LargeLongArray lla = LargeLongArray.factory(data).assertNonZero(true).assertUnique(true).build();
        for( int i=0;i<10000;i++)
        {
            lla.remove(Long.MAX_VALUE - i);
        }

        assertEquals( "size", counter - 10000, lla.size());
        
        long data2[][]=lla.repack();
        
        int counter2=10000;
        for( long rows2[]:data2)
        {
            for( long value2: rows2)
            {
                long expectValue=Long.MAX_VALUE-counter2;
                assertEquals( "expected", expectValue, value2);
                counter2++;
            }
        }
    }
    
    public void testAppendSorted()
    {
        long rows[]={1,3,5};

        LargeLongArray lla = LargeLongArray.factory(rows).validateUnique(true).build();
        
        try
        {
            lla.append(2);
            fail( "should not allow non sorted values to be appended");
        }
        catch( IllegalArgumentException e)
        {
            // Expected. 
        }
        
        lla.append(7);
        
        assertEquals( "size", 4, lla.size());
    }
    
    public void testRemove3()
    {
        long rows[]={1,3,2};

        LargeLongArray lla = LargeLongArray.factory(rows).build();
        assertEquals( "size", 3, lla.size());
        lla.remove(0);
        assertEquals( "size", 3, lla.size());        
        lla.remove(2);
        
        assertEquals( "size", 2, lla.size());
        
    } 
    
    public void testRemove2()
    {
//        StopWatch sw=new StopWatch();

        LargeLongArray lla = LargeLongArray.factory().assertNonZero(true).build();
        for( int i=0;i<10000;i++)
        {
            lla.append(Integer.MAX_VALUE - i);
        }
        
        for( int i=0;i<10000;i++)
        {
            lla.remove(Integer.MAX_VALUE - i);
            lla.remove(Integer.MAX_VALUE - i);
        }
        
        assertEquals( "should be zero", 0, lla.size());
        
    } 
    
    public void testAppend()
    {
//        StopWatch sw=new StopWatch();

        LargeLongArray lla = LargeLongArray.factory().assertNonZero(true).build();
        for( int i=0;i<10000;i++)
        {
            lla.append(Integer.MAX_VALUE - i);
        }

        for( int i=0;i<10000;i++)
        {
            long expectedValue=Integer.MAX_VALUE - i;
            long actualValue=lla.get(i);
            
            assertEquals("pos: " + i, expectedValue, actualValue);
        }
        
        long data[][]=lla.repack();
        long counter=0;
        
        for( long rows[]: data)
        {
            for( long value: rows)
            {
                long expectedValue=Integer.MAX_VALUE - counter;
                assertEquals("pos: " + counter, expectedValue, value);
                counter++;
            }
        }
        
        assertEquals("Expected count", 10000, counter);
    }
    
    private void swap( final int pos, long a[], long b[])
    {
        long tmp = a[pos];
        a[pos]=b[pos];
        b[pos]=tmp;
    }
    
    public void testSortSegment()
    {
        StopWatch sw=new StopWatch();

        long max=0;
        for( int attempts=0;attempts<30;attempts++)
        {
            long seg1[]=new long[10000];
            long seg2[]=new long[seg1.length];
            long seg3[]=new long[seg1.length];
            for( int pos=0;pos<seg1.length;pos++)
            {
                seg1[pos] = pos + 1;
                seg2[pos] = seg1.length + pos + 1;
                max=seg1.length + seg2.length + pos + 1;
                seg3[pos] = max;
            }
            
            for( int pos=0;pos<seg1.length;pos++)
            {
                int rand=(int)(Math.random()*10);
                switch( rand)
                {
                    case 0:
                        swap( pos, seg1, seg2);
                        break;
                    case 1:
                        swap( pos, seg2, seg3);
                        break;
                    case 2:
                        swap( pos, seg1, seg3);
                        break;
                    case 3:
                        swap( pos, seg1, seg3);
                        swap( pos, seg2, seg1);
                        break;
                }
            }
            long data[][]={
                seg1,
                seg2,
                seg3
            };

            int rand=(int)(Math.random()*10);

            LargeLongArray lla = LargeLongArray
                    .factory(data)
                    .setSegmentSize(rand < 7 ? seg1.length: rand==8?3333:12345)
                    .assertNonZero(true)
                    .assertUnique(true)
                    .setOutputShared(rand %2==0)
                    .setInputShared(rand %3==0)
                    .build();
            
            switch( rand)
            {
                case 0:
                    lla.append(++max);
                    break;
                case 1:
                    lla.append(++max);                   
                    lla.append(++max);                   
                    break;
                case 7:
                    lla.remove(max--);
                    break;
            }
            sw.start();

            long sortedData[][]=lla.sort();
            sw.stop();

            long last=0; 
            for( long rows[]:sortedData)
            {
                for( long row: rows)
                {
                    if( row<=last)
                    {
                        fail( row +"<=" + last);
                    }
                    last++;
                }
            }
            
            assertEquals( "Last == MAX", max, last);
        }

        LOGGER.info( sw.summary("sorting of segments"));
    }
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void testSortLarge()
    {
        long value=Long.MAX_VALUE;
        int counter=0;
        long data[][]=new long[123][];
        for( int j=0;j<data.length;j++)
        {
            long rows[]=new long[2345];
            data[j]=rows;
            counter+=rows.length;
            for( int k=0;k< rows.length;k++)
            {
                rows[k]=value;
                value--;
            }
        }
        long largeArray[]=new long[counter];
        for( int j=0;j<counter;j++)
        {
            largeArray[j]=value--;
        }
        StopWatch sw=new StopWatch();
        StopWatch swBase=new StopWatch();

        for( int i=0;i<30;i++)
        {
            LOGGER.info( "loop " + i );
            sw.start();
            LargeLongArray.factory(data).build().sort();
            sw.stop();
            swBase.start();
            long tmp[][]={largeArray};
            LargeLongArray.factory(tmp).build().sort();
            //Arrays.sort(largeArray.clone());
            swBase.stop();
        }

        LOGGER.info( sw.summary("sorting of arrays of arrays"));
        LOGGER.info( swBase.summary("sorting of large arrays"));
//
//        if( sw.median()> swBase.median()* 4)
//        {
//            fail( "too slow " + sw.formatNano(swBase.avg()) +" -> " + sw.formatNano(sw.avg()));
//        }
    }

    /**
     * Detect a zero value.
     * @throws Exception a serious problem
     */
    public void testRepackRemoveEmpties() throws Exception
    {
        long data[][]={
            {10,9,8},
            {7,6,5,4,3,2,1},
            {},
            {},
            {11}
        };

        long repackedData[][]=LargeLongArray.factory(data).build().repack();

        assert repackedData!=data;

        for( long rows[]: repackedData)
        {
            if( rows.length==0)
            {
                fail( "rows should not be zero");
            }
        }
    }

    /**
     * Detect a zero value.
     * @throws Exception a serious problem
     */
    public void testRepackIntoSegments() throws Exception
    {
        long rows[]=new long[12345];
        for( int pos=0;pos<rows.length;pos++)
        {
            rows[pos]=pos+1;
        }

        long data[][]={
            {-10,-9},rows,{-11,-12,-13,-15},{-16,-17},{-18}
        };
        ArrayList<Long>list=new ArrayList<>();
        for( long tmpRows[]:data)
        {
            for( long row: tmpRows)
            {
                list.add(row);
            }
        }

        repack( 3, list, data);
        repack( 2, list, data);
        repack( 1, list, data);
        repack( 4, list, data);
        repack( 100, list, data);
        repack( 101, list, data);
        repack( 12343, list, data);
        repack( 12345, list, data);
        repack( 12346, list, data);
        repack( list.size()-1, list, data);
        repack( list.size(), list, data);
        repack( list.size()+1, list, data);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void repack( final int segmentSize, final ArrayList<Long>list, final long data[][])
    {
        long repackedData[][]=LargeLongArray.factory(data).setSegmentSize(segmentSize).build().repack();

        assert repackedData!=data;

        int scan=0;
        for( long scanRows[]: repackedData)
        {
            if( scanRows.length>segmentSize)
            {
                fail( "didn't resize");
            }
            for( long scanRow: scanRows)
            {
                long check=list.get(scan);
                assertEquals("check the row " + scan, check, scanRow);
                scan++;
            }
        }

        LargeLongArray.factory(repackedData).assertNonZero(true).assertUnique(true).build().sort();
    }

    /**
     * Detect a zero value.
     * @throws Exception a serious problem
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void testDetectInvalid() throws Exception
    {
        long data[][]={
            {10,9,8},
            {7,6,5,4,3,2,1}
        };

        long sortedData[][]=LargeLongArray.factory(data).validateNonZero(true).validateUnique(true).build().sort();

        int expect=1;
        for( long rows[]: sortedData)
        {
            for( long actual:rows)
            {
                assertEquals( "expected", expect, actual);
                expect++;
            }
        }
//        long keep=data[1][3];
        data[1][3]=0;

        try{
            LargeLongArray.factory(data).validateNonZero(true).validateUnique(true).build().sort();
            fail( "did not detect a zero");
        }
        catch( IllegalArgumentException iae)
        {

        }
        data[1][3]=8;

        try{
            LargeLongArray.factory(data).validateNonZero(true).validateUnique(true).build().sort();
            fail( "did not detect duplicate");
        }
        catch( IllegalArgumentException iae)
        {

        }
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.memory.selftest.TestLargeLongArray");//#LOGGER-NOPMD
}
