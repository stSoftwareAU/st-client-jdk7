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
package com.aspc.remote.memory.impl;

import com.aspc.remote.memory.HashLongMap;
import com.aspc.remote.memory.HashLongMapFactory;
import com.aspc.remote.memory.internal.MemoryUtil;
import com.aspc.developer.ThreadCop;
import com.aspc.developer.ThreadCop.MODE;
import com.aspc.developer.errors.ThreadCopError;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.SyncBlock;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import org.apache.commons.logging.Log;

/**
 *  This is a hacked version of HashMap for longs only keys. It prevents us from having to
 *  create objects for keys.
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED modifications are externally synchronized</i>
 *
 *
 *  @author      Nigel Leck
 *  @since       5 May 2010
 */
public final class HashLongMapV7 implements HashLongMap, Cloneable
{
     /**  <I> MEMORY USAGE: The number of bytes for one instance of a HashLongMap </I>*/
    public static final int SIZE_OF=164;

    private Entry table[]; // default 11 * 8 bytes + 8 bytes

    private int count;     // 4 bytes

    private int threshold; // 4 bytes
    private int ensureCapcity; // 4 bytes
    private Object lasyLoadValue[];// 8 bytes
    private int nonSortedCallCount;// 4 bytes
    private long keyArray[]; // default 0 + 8 bytes
    private long overflowKey[];// default 0 + 8 bytes

    private final AtomicInteger status=new AtomicInteger( MASK_ARRAY_NULL );// 8 bytes

    private long keyArrayBrief[]; // 8 bytes

    private final SyncBlock lock = new SyncBlock("Hash Long Map lock");

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.memory.impl.HashLongMapV7");//#LOGGER-NOPMD

    private static final int MASK_SORTED=1;
    private static final int MASK_FULL_LIST=1<<1;
    private static final int MASK_LAZY_VALUE=1<<2;
    private static final int MASK_OVERFLOW=(1<<3 | MASK_LAZY_VALUE);
    private static final int MASK_LOADED=1<<4;
    private static final int MASK_ARRAY_NULL=1<<5;
    private static final int MASK_UNKNOWN_SORT=1<<6;
    private static final int MASK_OVERFLOW_LARGER=1<<7;
    private static final int MASK_OVERFLOW_SMALLER=1<<8;
    private static final int MASK_ARRAY_SHARED=1<<9;

    /**
     * Constructs a new, empty map with the specified initial capacity
     * and default load factor, which is <tt>0.75</tt>.
     *
     * @param initialCapacity the initial capacity of the HashMap.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public HashLongMapV7(final int initialCapacity)
    {
        assert ThreadCop.monitor(this, MODE.EXTERNAL_SYNCHRONIZED);
        if (initialCapacity < 0)
        {
            throw new IllegalArgumentException(
                "Illegal Initial Capacity: " + initialCapacity
            );
        }

        int tempCapacity= initialCapacity;
        if (tempCapacity < 11)
        {
            tempCapacity = 11;
        }
        ensureCapcity=tempCapacity;

        assert validate();
    }

    /**
     * Constructs a new, empty map with a default capacity and load
     * factor, which is <tt>0.75</tt>.
     */
    public HashLongMapV7()
    {
        this(11);
    }

    private void initialize()
    {
        if( table == null)
        {
            int tempCapcity=ensureCapcity;
            if( keyArray != null && keyArray.length > 0)
            {
                int t = keyArray.length;
                if( t > tempCapcity) tempCapcity=t;
            }

            int capacity = 1;
            while (capacity < tempCapcity)
            {
                capacity <<= 1;
            }

            table = new Entry[capacity];
            int tmpState = status.get();
            tmpState = ( tmpState | MASK_LOADED);
            status.set(tmpState);

            threshold = (int)(capacity * HashLongMapFactory.LOAD_FACTOR) + 1;
            if( count > 0)
            {
                count=0;
                long tmpOverflowKeys[] = overflowKey;
                Object value = lasyLoadValue[0];
                lasyLoadValue=null;
                putMultiRows(keyArray, value, (tmpState & MASK_SORTED) == MASK_SORTED ? State.UNIQUE_SORTED: State.UNIQUE);
                if( tmpOverflowKeys != null)
                {
                    putMultiRows(tmpOverflowKeys, value, State.UNIQUE_SORTED);
                    overflowKey=null;
                }
            }
        }
        else
        {
            throw new Error( "multiple calls to initialize");
        }
        assert validate();
    }

    /**
     * is this hash map initialized.
     * @return true if initialized.
     */
    @Override
    public boolean isInitialized()
    {
        assert ThreadCop.access(this);
        return table != null;
    }

    /**
     * clone the memory handler group.
     * @throws CloneNotSupportedException doesn't happen.
     * @return the new group.
     */
    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public Object clone() throws CloneNotSupportedException//NOPMD
    {
        lock.take();

        assert ThreadCop.enter(this, ThreadCop.ACCESS.READ);
        try
        {
            // lazy load of array.
            if( table == null)
            {
                HashLongMapV7 tempMap = new HashLongMapV7(ensureCapcity);
                tempMap.keyArray=keyArray;
                tempMap.overflowKey=overflowKey;
                tempMap.count=count;
                tempMap.lasyLoadValue=lasyLoadValue;

                int tmpStatus = status.get();
                int nextStatus = tmpStatus;
                if( keyArray != null)
                {
                    nextStatus = nextStatus | MASK_ARRAY_SHARED;

                    if( status.compareAndSet(tmpStatus, nextStatus) == false)
                    {
                        throw new ThreadCopError( "Concurrent modification exepected " + tmpStatus + " was " + status);
                    }
                }

                tempMap.status.set(nextStatus);

                assert tempMap.validate();

                return tempMap;
            }

            HashLongMapV7 tempMap = new HashLongMapV7(table.length);

            Entry tab[] = table;
            for (int i = tab.length ; i-- > 0 ;)
            {
                for (Entry e = tab[i] ; e != null ; e = e.next)
                {
                    tempMap.put( e.key, e.value);
                }
            }

            assert tempMap.validate();

            return tempMap;
        }
        finally
        {
            assert validate();
            lock.release();
            assert ThreadCop.leave(this);
        }
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map.
     */
    @Override
    public int size()
    {
        assert ThreadCop.read(this);
        return count;
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings.
     */
    @Override
    public boolean isEmpty()
    {
        assert ThreadCop.read(this);
        return count == 0;
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value.
     *
     * @param value value whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value.
     */
    @Override
    public boolean containsValue(final Object value)
    {
        lock.take();

        assert ThreadCop.enter(this, ThreadCop.ACCESS.READ);

        try
        {
            if( count == 0) return false;

            if( table == null)
            {
                Object tempValue = lasyLoadValue[0];
                if( value == tempValue)
                {
                    return true;
                }
                else if( value != null)
                {
                    if( value.equals(tempValue)) return true;
                }

                return false;
            }

            if (
                value==null
            )
            {
                for (int i = table.length ; i-- > 0 ;)
                {
                    for (Entry e = table[i] ; e != null ; e = e.next)
                    {
                        if (e.value==null) return true;
                    }
                }
            }
            else
            {
                for (int i = table.length ; i-- > 0 ;)
                {
                    for (Entry e = table[i] ; e != null ; e = e.next)
                    {
                        if (value.equals(e.value)) return true;
                    }
                }
            }

            return false;
        }
        finally
        {
            lock.release();
            assert ThreadCop.leave(this);
        }
    }

    /**
     * create an array of the first 200 odd keys
     *
     * @return the array
     */
    @Override
    public long[] briefKeyArray()
    {
        lock.take();

        assert ThreadCop.enter(this, ThreadCop.ACCESS.READ);

        try
        {
            long list[] = keyArrayBrief;

            if( list == null)
            {
                int briefSize=size();

                if( briefSize > 200)
                {
                    briefSize = 200;
                }
                else
                {
                    return getKeyArray();
                }

                list = new long[briefSize];
                if( keyArray != null)
                {
                    System.arraycopy(keyArray, 0, list, 0, briefSize);
                }
                else
                {
                    if( briefSize > 0)
                    {
                        int j = 0;
                        Entry tab[] = table;
                        for (int i = tab.length ; i-- > 0 && j < list.length ;)
                        {
                            for (Entry e = tab[i] ; e != null && j < list.length ; e = e.next)
                            {
                                list[j] = e.key;
                                j++;
                            }
                        }
                    }
                }

                keyArrayBrief=list;
            }

            return list;
        }
        finally
        {
            assert validate();
            lock.release();
            assert ThreadCop.leave(this);
        }
    }

    /**
     * create an array of the keys
     *
     *
     * @return the array
     */
    @Override
    public long[] getKeyArray()
    {
        assert ThreadCop.enter(this, ThreadCop.ACCESS.READ);

        try
        {
            int tmpStatus = status.get();
            if(
                (tmpStatus & MASK_FULL_LIST) == MASK_FULL_LIST &&
                (tmpStatus & MASK_ARRAY_SHARED) == MASK_ARRAY_SHARED
            )
            {
                return keyArray;
            }

            lock.take();

            try
            {
                long keys[] = noLockGetKeyArray();
                tmpStatus = status.get();
                int nextStatus = tmpStatus;

                nextStatus = nextStatus | MASK_ARRAY_SHARED;

                if( status.compareAndSet(tmpStatus, nextStatus) == false)
                {
                    throw new ThreadCopError( "Concurrent modification exepected " + tmpStatus + " was " + status);
                }

                return keys;
            }
            finally
            {
                assert validate();
                lock.release();
            }
        }
        finally
        {
            assert ThreadCop.leave(this);
        }
    }

    private long[] noLockGetKeyArray()
    {
        int tmpStatus = status.get();

        if( keyArray == null )
        {
            if( overflowKey != null)
            {
                return overflowKey;
            }
            keyArray = new long[ count];
            boolean sorted=true;

            if( count > 0)
            {
                int j = 0;
                long previous = Long.MIN_VALUE;

                Entry tab[] = table;
                for (Entry tab1 : tab) {
                    for (Entry e = tab1; e != null; e = e.next) {
                        if( sorted && previous > e.key)
                        {
                            sorted = false;
                        }
                        previous = e.key;
                        keyArray[j] = previous;
                        j++;
                    }
                }

                int nextStatus;
                if( sorted)
                {
                    nextStatus= ( tmpStatus | MASK_FULL_LIST | MASK_LOADED | MASK_SORTED ) & ( 0xffff ^ MASK_UNKNOWN_SORT)& ( 0xffff ^ MASK_ARRAY_NULL);
                }
                else
                {
                    nextStatus= ( tmpStatus | MASK_FULL_LIST | MASK_LOADED  ) & ( 0xffff ^ MASK_UNKNOWN_SORT) & ( 0xffff ^ MASK_SORTED)& ( 0xffff ^ MASK_ARRAY_NULL);
                }

                if( status.compareAndSet(tmpStatus, nextStatus) == false)
                {
                    throw new ThreadCopError( "Concurrent modification exepected " + tmpStatus + " was " + status);
                }
            }
            else
            {
                int nextStatus = ( tmpStatus | MASK_FULL_LIST | MASK_SORTED ) & ( 0xffff ^ MASK_UNKNOWN_SORT)& ( 0xffff ^ MASK_ARRAY_NULL);

                if( status.compareAndSet(tmpStatus, nextStatus) == false)
                {
                    throw new ThreadCopError( "Concurrent modification exepected " + tmpStatus + " was " + status);
                }
            }
        }
        else if( overflowKey != null)
        {
            long tmpOverFlowKey[] = overflowKey;
            long tmpKeyArray[] = keyArray;

            long temp[]=new long[tmpKeyArray.length + tmpOverFlowKey.length];
            System.arraycopy(tmpKeyArray, 0, temp, 0, tmpKeyArray.length);
            System.arraycopy(tmpOverFlowKey, 0, temp, tmpKeyArray.length, tmpOverFlowKey.length);

            int nextStatus = tmpStatus;

            keyArray=temp;
            nextStatus = (nextStatus | MASK_FULL_LIST);

            overflowKey=null;
            nextStatus = nextStatus & ( 0xffff ^ MASK_OVERFLOW) & ( 0xffff ^ MASK_OVERFLOW_SMALLER) & ( 0xffff ^ MASK_OVERFLOW_LARGER)& ( 0xffff ^ MASK_ARRAY_NULL)& ( 0xffff ^ MASK_ARRAY_SHARED);

            if( (tmpStatus & MASK_SORTED) == MASK_SORTED)
            {
                if(
                    tmpKeyArray.length > 0 &&
                    tmpKeyArray[tmpKeyArray.length -1] >= tmpOverFlowKey[0]
                )
                {
                    nextStatus = nextStatus & ( 0xffff ^ MASK_SORTED) & ( 0xffff ^ MASK_UNKNOWN_SORT);
                }
            }

            if( status.compareAndSet(tmpStatus, nextStatus) == false)
            {
                throw new ThreadCopError( "Concurrent modification exepected " + tmpStatus + " was " + status);
            }
        }

        return keyArray;
    }

    /**
     * Is the key array sorted ?
     * @return true if sorted
     */
    @Override
    public boolean isKeyArraySorted()
    {
        assert ThreadCop.enter(this, ThreadCop.ACCESS.READ);
        try
        {
            int tmpStatus = status.get();

            if( ( tmpStatus & MASK_UNKNOWN_SORT) != MASK_UNKNOWN_SORT)
            {
                if( ( tmpStatus & MASK_SORTED) == MASK_SORTED )
                {
                    if( ( tmpStatus & MASK_OVERFLOW) == MASK_OVERFLOW )
                    {
                        if( ( tmpStatus & MASK_OVERFLOW_LARGER) == MASK_OVERFLOW_LARGER )
                        {
                            return true;
                        }
                        return false;
                    }

                    return true;
                }
            }

            lock.take();

            try
            {
                return noLockIsKeyArraySorted();
            }
            finally
            {
                assert validate();
                lock.release();
            }
        }
        finally
        {
            assert ThreadCop.leave(this);
        }
    }

    /**
     * Is the key array sorted ?
     *
     * THREAD MODE: MUST BE LOCKED HERE
     *
     * @return true if sorted
     */
    private boolean noLockIsKeyArraySorted()
    {
        if( count < 2) return true;

        int tmpStatus = status.get();
        if( (tmpStatus & MASK_UNKNOWN_SORT) == MASK_UNKNOWN_SORT)
        {
            boolean sorted = true;
            if( keyArray != null)
            {
                long last=Long.MIN_VALUE;
                for( long key:keyArray)
                {
                    if( last > key)
                    {
                        sorted = false;
                        break;
                    }
                    last = key;
                }

                int nextStatus=tmpStatus;
                if( overflowKey != null)
                {
                    if( last > overflowKey[0])
                    {
                        sorted = false;

                        nextStatus = nextStatus & ( 0xffff ^ MASK_OVERFLOW_LARGER);
                    }
                    else
                    {
                        nextStatus = nextStatus | MASK_OVERFLOW_LARGER;
                    }
                }

                if( sorted )
                {
                    nextStatus = nextStatus | MASK_SORTED;
                }
                else
                {
                    nextStatus = nextStatus & ( 0xffff ^ MASK_SORTED);
                }

                nextStatus = nextStatus & ( 0xffff ^ MASK_UNKNOWN_SORT);

                if( status.compareAndSet(tmpStatus, nextStatus) == false)
                {
                    throw new ThreadCopError( "Concurrent modification exepected " + tmpStatus + " was " + status);
                }
            }

            return sorted;
        }
        else if((tmpStatus & MASK_SORTED) == MASK_SORTED)
        {
            if( ( tmpStatus & MASK_OVERFLOW) == MASK_OVERFLOW)
            {
                if( ( tmpStatus & MASK_OVERFLOW_LARGER) == MASK_OVERFLOW_LARGER)
                {
                    return true;
                }
                else if( ( tmpStatus & MASK_OVERFLOW_SMALLER) == MASK_OVERFLOW_SMALLER)
                {
                    return false;
                }
                else
                {
                    int nextStatus;
                    if( keyArray[ keyArray.length -1 ] < overflowKey[0])
                    {
                        nextStatus = tmpStatus | MASK_OVERFLOW_LARGER;
                        if( status.compareAndSet(tmpStatus, nextStatus) == false)
                        {
                            throw new ThreadCopError( "Concurrent modification exepected " + tmpStatus + " was " + status);
                        }
                        return true;
                    }
                    else
                    {
                        nextStatus = tmpStatus | MASK_OVERFLOW_SMALLER;
                        if( status.compareAndSet(tmpStatus, nextStatus) == false)
                        {
                            throw new ThreadCopError( "Concurrent modification exepected " + tmpStatus + " was " + status);
                        }
                        return false;
                    }
                }
            }
            return true;
        }
        else
        {
            return false;
        }

    }

    /**
     * create an array of the keys
     *
     * SORTED array.
     *
     * @return the array
     */
    @Override
    public long[] getSortedKeyArray()
    {
        lock.take();

        assert ThreadCop.enter(this, ThreadCop.ACCESS.READ);// we would need to clone the list if read only

        try
        {
            long list[] = noLockGetSortedKeyArray();
            int tmpStatus = status.get();
            int nextStatus;
            nextStatus = tmpStatus | MASK_ARRAY_SHARED;

            if( status.compareAndSet(tmpStatus, nextStatus) == false)
            {
                throw new ThreadCopError( "Concurrent modification exepected " + tmpStatus + " was " + status);
            }

            return list;
        }
        finally
        {
            assert validate();
            lock.release();
            assert ThreadCop.leave(this);
        }

    }
    /**
     * create an array of the keys
     *
     * SORTED array.
     *
     * @return the array
     */
    private long[] noLockGetSortedKeyArray()
    {
        long list[] = noLockGetKeyArray();

        int tmpStatus = status.get();
        if((tmpStatus & MASK_SORTED) != MASK_SORTED)
        {
            int nextStatus=tmpStatus;
            if( (nextStatus & MASK_ARRAY_SHARED) == MASK_ARRAY_SHARED)
            {
                list = list.clone();
                nextStatus= nextStatus & ( 0xffff ^ MASK_ARRAY_SHARED);
            }

            Arrays.sort(list);

            keyArray = list;

            nextStatus = (nextStatus | MASK_SORTED) & ( 0xffff ^ MASK_UNKNOWN_SORT);

            if( status.compareAndSet(tmpStatus, nextStatus) == false)
            {
                throw new ThreadCopError( "Concurrent modification exepected " + tmpStatus + " was " + status);
            }
        }

        return list;
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified
     * key.
     *
     * @return <tt>true</tt> if this map contains a mapping for the specified
     * key.
     * @param key key whose presence in this Map is to be tested.
     */
    @Override
    public boolean containsKey(final long key)
    {
        lock.take();

        assert ThreadCop.enter(this, ThreadCop.ACCESS.READ);

        try
        {
            if( count == 0) return false;

            if( table == null)
            {
                if( overflowKey != null && overflowKey[0] == key)
                {
                    return true;
                }

                int tmpStatus = status.get();
                if((tmpStatus & MASK_SORTED) != MASK_SORTED)
                {
                    sortIfNeeded();
                    tmpStatus = status.get();
                }

                if( (tmpStatus & MASK_SORTED) == MASK_SORTED)
                {
                    if( Arrays.binarySearch(keyArray, key) < 0)
                    {
                        return false;
                    }

                    return true;
                }
                else
                {
                    for( long tmp: keyArray)
                    {
                        if( tmp == key) return true;
                    }

                    return false;
                }
            }
            else
            {
                int hash = (int)key;
                int index = (hash & 0x7FFFFFFF) % table.length;

                for (Entry e = table[index]; e != null; e = e.next)
                {
                    if( key == e.key)
                    {
                        return true;
                    }
                }
            }

            return false;
        }
        finally
        {
            assert validate();
            lock.release();
            assert ThreadCop.leave(this);
        }
    }

    /**
     * Returns the value to which this map maps the specified key.  Returns
     * <tt>null</tt> if the map contains no mapping for this key.  A return
     * value of <tt>null</tt> does not <i>necessarily</i> indicate that the
     * map contains no mapping for the key; it's also possible that the map
     * explicitly maps the key to <tt>null</tt>.  The <tt>containsKey</tt>
     * operation may be used to distinguish these two cases.
     *
     * THREAD MODE: No external changes are expected.
     *
     * @return the value to which this map maps the specified key.
     * @param key key whose associated value is to be returned.
     */
    @Override
    public Object get(final long key)
    {
        assert ThreadCop.enter(this, ThreadCop.ACCESS.READ);

        try
        {
            if( count == 0) // no concurrent changes expected. We are just handling the lazy sort
            {
                return null;
            }

            int tmpStatus = status.get();

            if( (tmpStatus & MASK_LOADED) == MASK_LOADED)
            {
                int hash = (int)key;
                int index = (hash & 0x7FFFFFFF) % table.length;

                for (Entry e = table[index]; e != null; e = e.next)
                {
                    if( key == e.key)
                    {
                        return e.value;
                    }
                }

                return null;
            }

            if(
                (tmpStatus & MASK_FULL_LIST) == MASK_FULL_LIST &&
                (tmpStatus & MASK_SORTED) == MASK_SORTED
            )
            {
                int pos =  Arrays.binarySearch(keyArray, key);

                if( pos >= 0)
                {
                    return lasyLoadValue[0];
                }

                return null;
            }

            lock.take();

            try
            {
                if( overflowKey != null && overflowKey[0] == key)
                {
                    return lasyLoadValue[0];
                }

                if( keyArray != null)
                {
                    boolean sorted=false;

                    if( (tmpStatus & MASK_SORTED) == MASK_SORTED)
                    {
                        sorted=true;
                    }

                    if( sorted == false)
                    {
                        sorted = sortIfNeeded();
                    }

                    if( sorted)
                    {
                        int pos =  Arrays.binarySearch(keyArray, key);

                        if( pos >= 0)
                        {
                            return lasyLoadValue[0];
                        }
                    }
                    else
                    {
                        for( long tmpKey: keyArray)
                        {
                            if( tmpKey == key)
                            {
                                return lasyLoadValue[0];
                            }
                        }
                    }
                }

                return null;
            }
            finally
            {
                lock.release();
            }
        }
        finally
        {
            assert ThreadCop.leave(this);
        }
    }

    /**
     * Rehashes the contents of this map into a new <tt>HashMap</tt> instance
     * with a larger capacity. This method is called automatically when the
     * number of keys in this map exceeds its capacity and load factor.
     */
    private void rehash(int adjustCount)
    {
        int oldCapacity = table.length;
        Entry oldMap[] = table;

        int newCapacity = (oldCapacity+adjustCount) * 2 + 1;
        Entry newMap[] = new Entry[newCapacity];

        threshold = (int)(newCapacity * HashLongMapFactory.LOAD_FACTOR) + 1;
        table = newMap;

        for (int i = oldCapacity ; i-- > 0 ;)
        {
            for (Entry old = oldMap[i] ; old != null ; )
            {
                Entry e = old;
                old = old.next;

                int index = (e.hash & 0x7FFFFFFF) % newCapacity;
                e.next = newMap[index];
                newMap[index] = e;
            }
        }
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for this key, the old
     * value is replaced.
     *
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key.  A <tt>null</tt> return can
     *         also indicate that the HashMap previously associated
     *         <tt>null</tt> with the specified key.
     * @param value The object to place into the table.
     * @param key key with which the specified value is to be associated.
     */
    @Override
    public Object put(final long key, final Object value)
    {
        assert ThreadCop.enter(this, ThreadCop.ACCESS.MODIFY);

        try
        {
            if( table == null)
            {
                if( count != 0)
                {
                    Object tmpValue = lasyLoadValue[0];
                    if( value == tmpValue)
                    {
                        if( containsKey(key))
                        {
                            return tmpValue;
                        }

                        if( overflowKey== null)
                        {
                            overflowKey=new long[]{key};

                            int tmpStatus = status.get();
                            int nextStatus = tmpStatus;
                            nextStatus = nextStatus | MASK_OVERFLOW;
                            nextStatus = nextStatus & ( 0xffff ^ MASK_FULL_LIST);
                            if( (tmpStatus & MASK_SORTED) == MASK_SORTED)
                            {
                                if( keyArray[ keyArray.length -1 ] < key)
                                {
                                    nextStatus = nextStatus | MASK_OVERFLOW_LARGER;
                                }
                                else
                                {
                                    nextStatus = nextStatus | MASK_OVERFLOW_SMALLER;
                                }
                            }

                            if( status.compareAndSet(tmpStatus, nextStatus) == false)
                            {
                                throw new ThreadCopError( "Concurrent modification exepected " + tmpStatus + " was " + status);
                            }

                            count++;
                            return null;
                        }
                    }
                }
                else
                {
                    lasyLoadValue=new Object[]{value};
                    keyArray=new long[]{key};
                    count=1;
                    int tmpStatus = status.get();
                    int nextStatus = tmpStatus;
                    nextStatus = (nextStatus | MASK_SORTED | MASK_FULL_LIST | MASK_LAZY_VALUE )  & ( 0xffff ^ MASK_ARRAY_NULL);

                    if( status.compareAndSet(tmpStatus, nextStatus) == false)
                    {
                        throw new ThreadCopError( "Concurrent modification exepected " + tmpStatus + " was " + status);
                    }

                    return null;
                }

                initialize();
            }
            // Makes sure the key is not already in the HashMap.

            if (count >= threshold)
            {
                // Rehash the table if the threshold is exceeded
                rehash(1);
            }

            int index;

            int hash = (int)key;

            index = (hash & 0x7FFFFFFF) % table.length;
            Entry startEntry = table[index];
            for (Entry e = startEntry; e != null ; e = e.next)
            {
                if( key == e.key)
                {
                    Object old = e.value;
                    e.value = value;
                    return old;
                }
            }

            // Creates the new entry.
            Entry e = new Entry(hash, key, value, startEntry);
            table[index] = e;
            count++;

            clearLazyLoad();

            return null;
        }
        finally
        {
            assert validate();

            assert ThreadCop.leave(this);
        }
    }

    private String alert( String message)
    {
        LOGGER.error( message);

        return message;
    }

    /**
     * put multiple rows.
     * @param rows the rows
     * @param value the value
     * @param stateOfRows are these rows known to be sorted ?
     */
    @Override
    public void putMultiRows( final long rows[], final Object value, final State stateOfRows)
    {
        assert ThreadCop.enter(this, ThreadCop.ACCESS.MODIFY);

        try
        {
            /**
             * we get a null from the possible rows
             */
            if( rows == null || rows.length == 0) return;

            assert stateOfRows == State.UNKNOWN || rows.length < 2 || rows[0] != rows[1]: alert( "non-unique rows passed into putMultiRows " + rows[0]);

            if( table == null)
            {
                if( rows.length == 1)
                {
                    put( rows[0], value);
                    return;
                }

                if( stateOfRows != State.UNKNOWN && keyArray == null)
                {
                    keyArray = rows;

                    lasyLoadValue=new Object[]{value};
                    int tmpStatus = status.get();
                    int nextStatus = tmpStatus |MASK_FULL_LIST | MASK_LAZY_VALUE | MASK_ARRAY_SHARED;
                    nextStatus = nextStatus & ( 0xffff ^ MASK_SORTED) & ( 0xffff ^ MASK_UNKNOWN_SORT) & ( 0xffff ^ MASK_ARRAY_NULL);

                    count=keyArray.length;

                    if( count < 2)
                    {
                        nextStatus = ( nextStatus | MASK_SORTED);
                    }
                    else
                    {
                        if( stateOfRows == State.UNIQUE_SORTED)
                        {
                            nextStatus = ( nextStatus | MASK_SORTED);
                        }
                        else
                        {
                            nextStatus = ( nextStatus | MASK_UNKNOWN_SORT);
                        }
                    }

                    if( status.compareAndSet(tmpStatus, nextStatus) == false)
                    {
                        throw new ThreadCopError( "Concurrent modification exepected " + tmpStatus + " was " + status);
                    }

                    return;
                }

                initialize();
            }

            if (count + rows.length >= threshold)
            {
                // Rehash the table if the threshold is exceeded
                rehash(rows.length);
            }

            for( long row: rows)
            {
                int index;

                int hash = (int)row;

                index = (hash & 0x7FFFFFFF) % table.length;

                Entry startEntry=table[index];
                boolean found=false;

                for (Entry e = startEntry; e != null ; e = e.next)
                {
                    if( row == e.key)
                    {
                        e.value = value;
                        found=true;
                        break;
                    }
                }

                if( found ) continue;

                // Creates the new entry.
                Entry e = new Entry(hash, row, value, startEntry);
                table[index] = e;
                count++;
            }

            clearLazyLoad();
        }
        finally
        {
            assert validate();

            assert ThreadCop.leave(this);
        }
    }

    /**
     * Removes the mapping for this key from this map if present.
     *
     * @param key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key.  A <tt>null</tt> return can
     *         also indicate that the map previously associated <tt>null</tt>
     *         with the specified key.
     */
    @Override
    public Object remove(final long key)
    {
        assert ThreadCop.enter(this, ThreadCop.ACCESS.MODIFY);

        try
        {
            if( count == 0) return null;

            if( table == null)
            {
                if( containsKey(key) == false) return null;

                if( overflowKey != null && overflowKey[0] == key)
                {
                    overflowKey=null;
                    count--;

                    int tmpStatus = status.get();
                    int nextStatus = tmpStatus & ( 0xffff ^ MASK_OVERFLOW) & ( 0xffff ^ MASK_OVERFLOW_LARGER) & ( 0xffff ^ MASK_OVERFLOW_SMALLER);
                    if( status.compareAndSet(tmpStatus, nextStatus) == false)
                    {
                        throw new ThreadCopError( "Concurrent modification exepected " + tmpStatus + " was " + status);
                    }
                    return lasyLoadValue[0];
                }

                initialize();
            }

            int hash = (int)key;
            int index = (hash & 0x7FFFFFFF) % table.length;

            for(
                Entry e = table[index], prev = null;
                e != null;
                prev = e, e = e.next
            )
            {
                if( key == e.key)
                {
                    if (prev != null)
                    {
                        prev.next = e.next;
                    }
                    else
                    {
                        table[index] = e.next;
                    }

                    count--;
                    Object oldValue = e.value;
                    e.value = null;

                    clearLazyLoad();

                    return oldValue;
                }
            }

            return null;
        }
        finally
        {
            assert validate();
            assert ThreadCop.leave(this);
        }
    }

    private void clearLazyLoad()
    {
        keyArray = null;
        keyArrayBrief=null;
        overflowKey=null;
        lasyLoadValue=null;
        nonSortedCallCount=0;
        int tmpStatus = status.get();
        int nextStatus=tmpStatus;
        nextStatus = ( nextStatus | MASK_ARRAY_NULL) &
                     ( 0xffff ^ MASK_OVERFLOW )&
                     ( 0xffff ^ MASK_OVERFLOW_LARGER )&
                     ( 0xffff ^ MASK_UNKNOWN_SORT )&
                     ( 0xffff ^ MASK_OVERFLOW_SMALLER )&
                     ( 0xffff ^ MASK_LAZY_VALUE ) &
                     ( 0xffff ^ MASK_FULL_LIST) &
                     ( 0xffff ^ MASK_ARRAY_SHARED) &
                     ( 0xffff ^ MASK_SORTED);

        if( status.compareAndSet(tmpStatus, nextStatus) == false)
        {
            throw new ThreadCopError( "Concurrent modification exepected " + tmpStatus + " was " + status);
        }

        assert validate();
    }

    /**
     * Removes all mappings from this map.
     */
    @Override
    public void clear()
    {
        assert ThreadCop.enter(this, ThreadCop.ACCESS.MODIFY);

        try
        {
            status.set( MASK_ARRAY_NULL);
            table = null;

            count = 0;
            clearLazyLoad();
        }
        finally
        {
            assert validate();

            assert ThreadCop.leave(this);
        }
    }

    /** {@inheritDoc} */
    @Override
    public long sizeOf()
    {
        lock.take();

        assert ThreadCop.enter(this, ThreadCop.ACCESS.READ);

        try
        {
            long size = SIZE_OF; // this object
            size += MemoryUtil.sizeOf(table);
            size += MemoryUtil.sizeOf(keyArray);
            size += count * 16; // every Entry object is 16 bytes
            return size;
        }
        finally
        {
            lock.release();
            assert ThreadCop.leave(this);
        }
    }

    @Override
    public long[][] getKeyData() {
        long[][] data={
            getKeyArray()
        };
        
        return data;
    }

    /**
     * HashMap collision list entry.
     */
    private static class Entry
    {
        int hash;
        long key;
        Object value;
        Entry next;

        Entry(int hash, long key, Object value, Entry next)
        {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        /**
         * @return the value
         */
        public long getKey()
        {
            return key;
        }

        /**
         * @return the value
         */
        public Object getValue()
        {
            return value;
        }

        /**
         * @return the value
         */
        @Override @CheckReturnValue @Nonnull
        public String toString()
        {
            return key+"="+value;
        }
    }

    private boolean sortIfNeeded()
    {
        if( noLockIsKeyArraySorted() == false)
        {
            if( nonSortedCallCount > 2)
            {
                noLockGetSortedKeyArray();
                return true;
            }
            else
            {
                nonSortedCallCount++;
                return false;
            }
        }
        else
        {
            return true;
        }
    }

    private boolean validate( )
    {
        int tmpStatus = status.get();
        if( (tmpStatus & MASK_ARRAY_NULL) == MASK_ARRAY_NULL)
        {
            if( keyArray != null)
            {
                throw new ThreadCopError( "should have a null keyArray");
            }

            if( (tmpStatus & MASK_ARRAY_SHARED) == MASK_ARRAY_SHARED)
            {
                throw new ThreadCopError( "can't be sharing a key array that is null");
            }

            if( (tmpStatus & MASK_SORTED) == MASK_SORTED)
            {
                throw new ThreadCopError( "can't be sorted if no array");
            }

            if( (tmpStatus & MASK_UNKNOWN_SORT) == MASK_UNKNOWN_SORT)
            {
                throw new ThreadCopError( "can't be unknown sorted if no array");
            }
        }
        else
        {
            if( keyArray == null)
            {
                throw new ThreadCopError( "should keyArray not be null");
            }

            if( (tmpStatus & MASK_SORTED) == MASK_SORTED)
            {
                if( (tmpStatus & MASK_UNKNOWN_SORT) == MASK_UNKNOWN_SORT)
                {
                    throw new ThreadCopError( "can't be unknown AND sorted");
                }

                if( keyArray.length >1)
                {
                    if( keyArray[0] >= keyArray[ keyArray.length -1])
                    {
                        throw new ThreadCopError( "1) " + keyArray[0] + " last) " + keyArray[ keyArray.length -1] );
                    }
                }
            }
        }

        if( (tmpStatus & MASK_FULL_LIST) == MASK_FULL_LIST)
        {
            if( keyArray == null)
            {
                 throw new ThreadCopError( "should keyArray not be null");
            }

            if( overflowKey != null)
            {
                throw new ThreadCopError( "should overflowKey be null");
            }

            if( ( tmpStatus & MASK_OVERFLOW_LARGER) == MASK_OVERFLOW_LARGER)
            {
                throw new ThreadCopError( "should can not be sort if there is none");
            }

            if( ( tmpStatus & MASK_OVERFLOW_SMALLER) == MASK_OVERFLOW_SMALLER)
            {
                throw new ThreadCopError( "should can not be sort if there is none");
            }
        }

        if( (tmpStatus & MASK_LOADED) == MASK_LOADED)
        {
            if( table == null)
            {
                 throw new ThreadCopError( "should table not be null");
            }
        }
        else
        {
            if( table != null)
            {
                throw new ThreadCopError( "should table be null");
            }
        }

        if( (tmpStatus & MASK_OVERFLOW) == MASK_OVERFLOW)
        {
            if( overflowKey == null)
            {
                 throw new ThreadCopError( "should overflowKey not be null");
            }

            if(
                ( tmpStatus & MASK_OVERFLOW_LARGER) == MASK_OVERFLOW_LARGER &&
                ( tmpStatus & MASK_OVERFLOW_SMALLER) == MASK_OVERFLOW_SMALLER
            )
            {
                throw new ThreadCopError( "Can't be larger and smaller");
            }
        }
        else
        {
            if( overflowKey != null)
            {
                throw new ThreadCopError( "should overflowKey be null");
            }

            if( ( tmpStatus & MASK_OVERFLOW_LARGER) == MASK_OVERFLOW_LARGER)
            {
                throw new ThreadCopError( "should can not be sort if there is none");
            }

            if( ( tmpStatus & MASK_OVERFLOW_SMALLER) == MASK_OVERFLOW_SMALLER)
            {
                throw new ThreadCopError( "should can not be sort if there is none");
            }
        }

        return true;
    }
}
