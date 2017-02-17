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
package com.aspc.remote.memory.impl;

import com.aspc.remote.memory.HashLongMap;
import com.aspc.developer.ThreadCop;
import com.aspc.remote.memory.HashMapFactory;
import java.util.HashMap;

/**
 *  This is a hacked version of HashMap for longs only keys. It prevents us from having to
 *  create objects for keys.
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 *
 *  @author      Nigel Leck
 *  @since       29 March 2002
 */
public final class HashLongMapCompare implements HashLongMap, Cloneable
{
    private HashLongMapV6 v6;
    private HashLongMapV7 v7;

    /**
     * Constructs a new, empty map with the specified initial capacity
     * and default load factor, which is <tt>0.75</tt>.
     *
     * @param initialCapacity the initial capacity of the HashMap.
     */
    public HashLongMapCompare(final int initialCapacity)
    {
        v6=new HashLongMapV6(initialCapacity);
        v7=new HashLongMapV7(initialCapacity);
    }

    /**
     * Constructs a new, empty map with a default capacity and load
     * factor, which is <tt>0.75</tt>.
     */
    public HashLongMapCompare()
    {
        this(11);
    }

    /**
     * is this hash map initialized.
     * @return true if initialized.
     */
    @Override
    public boolean isInitialized()
    {
        return v7.isInitialized();
    }

    /**
     * clone the memory handler group.
     * @throws CloneNotSupportedException doesn't happen.
     * @return the new group.
     */
    @Override
    public Object clone() throws CloneNotSupportedException//NOPMD
    {
        HashLongMapV7 c7 = (HashLongMapV7)v7.clone();
        HashLongMapV6 c6 = (HashLongMapV6)v6.clone();

        HashLongMapCompare c = new HashLongMapCompare();
        c.v6=c6;
        c.v7=c7;

        return c;
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map.
     */
    @Override
    public int size()
    {
        int size2 = v6.size();
        int size3 = v7.size();

        if( size2 != size3)
        {
            assert size2 == size3: "v6 size=" + size2 + " v7 size=" + size3;
        }

        return size2;
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings.
     */
    @Override
    public boolean isEmpty()
    {
        boolean isEmpty2 = v6.isEmpty();
        boolean isEmpty3 = v7.isEmpty();

        assert isEmpty2 == isEmpty3: "v6.isEmpty()=" + isEmpty2 + " v7.isEmpty()=" + isEmpty3;

        return isEmpty3;
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
        boolean containsValue2 = v6.containsValue(value);
        boolean containsValue3 = v7.containsValue(value);

        assert( containsValue2 == containsValue3);

        return containsValue3;
    }

    /**
     * create an array of the first 200 odd keys
     *
     * @return the array
     */
    @Override
    public long[] briefKeyArray()
    {
        long list2[] = v6.briefKeyArray();
        long list3[] = v7.briefKeyArray();

        assert list2.length == list3.length: "v6 length=" + list2.length + " v7 length=" + list3.length;

        return list3;
    }

    private void compare( long list2[], long list3[], final boolean sorted)
    {
        assert list2.length == list3.length: "v6 length=" + list2.length + " v7 length=" + list3.length;

        String msg=null;

        for( int pos=0; pos < list2.length; pos++)
        {
            if( list2[pos] != list3[pos])
            {
                msg = pos + ") v6: " + list2[pos] + " v7: " + list3[pos];
                break;
            }
        }

        if( msg == null) return;

        if( sorted == false)
        {
            HashMap<Long, String> map=HashMapFactory.create(ThreadCop.MODE.ACCESS_ONLY_BY_CREATING_THREAD, list2.length);

            for( int pos=0; pos < list2.length; pos++)
            {
                map.put(list2[pos], "");
            }
            for( int pos=0; pos < list3.length; pos++)
            {
                if( map.remove(list3[pos]) == null)
                {
                    throw new Error( "version 3 contains " + list3[pos]);
                }
            }
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
        long list2[] = v6.getKeyArray();
        long list3[] = v7.getKeyArray();

        compare( list2, list3, false);
        return list3;
    }

    /**
     * Is the key array sorted ?
     * @return true if sorted
     */
    @Override
    public boolean isKeyArraySorted()
    {
        //boolean isKeyArraySorted2 = v6.isKeyArraySorted();
        boolean isKeyArraySorted3 = v7.isKeyArraySorted();

        return isKeyArraySorted3;
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
        long list2[] = v6.getSortedKeyArray();
        long list3[] = v7.getSortedKeyArray();

        compare( list2, list3, true);
        return list3;
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
        boolean containsKey2 = v6.containsKey(key);
        boolean containsKey3 = v7.containsKey(key);

        assert( containsKey2 == containsKey3);

        return containsKey3;
    }

    /**
     * Returns the value to which this map maps the specified key.  Returns
     * <tt>null</tt> if the map contains no mapping for this key.  A return
     * value of <tt>null</tt> does not <i>necessarily</i> indicate that the
     * map contains no mapping for the key; it's also possible that the map
     * explicitly maps the key to <tt>null</tt>.  The <tt>containsKey</tt>
     * operation may be used to distinguish these two cases.
     *
     * @return the value to which this map maps the specified key.
     * @param key key whose associated value is to be returned.
     */
    @Override
    public Object get(final long key)
    {
        Object o2 = v6.get(key);
        Object o3 = v7.get(key);
        assert o2 == o3: "get( " + key + ") v6=" + o2 + " v7=" + o3;

        return o3;
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
        Object o2 = v6.put( key, value);
        Object o3 = v7.put( key, value);

        assert o2 == o3: "put( " + key + ", " + value + ") v6:" + o2 + " v7:" + o3;

        return o3;
    }

    /**
     * put multiple rows.
     * @param rows the rows
     * @param value the value
     * @param stateOfRows are these rows known to be sorted ? MAYBE null if you don't know
     */
    @Override
    public void putMultiRows( final long rows[], final Object value, final State stateOfRows)
    {

        v6.putMultiRows(rows, value, stateOfRows);
        v7.putMultiRows(rows, value, stateOfRows);
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
        Object o2 = v6.remove( key);
        Object o3 = v7.remove( key);

        assert( o2 == o3);

        return o3;
    }

    /**
     * Removes all mappings from this map.
     */
    @Override
    public void clear()
    {
        v6.clear();
        v7.clear();
    }

    /** {@inheritDoc} */
    @Override
    public long sizeOf()
    {
        long size = v6.sizeOf();
        size += v7.sizeOf();

        return size;
    }
}
