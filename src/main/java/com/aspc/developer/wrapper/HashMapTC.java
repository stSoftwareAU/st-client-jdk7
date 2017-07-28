/*
 *  Copyright (c) 2001-2004 ASP Converters Pty Ltd.
 *
 *  www.stSoftware.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
*/
package com.aspc.developer.wrapper;

import com.aspc.developer.*;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
//import java.util.concurrent.atomic.AtomicInteger;

/**
 *  Thread Cop is designed to make sure that an object is called in the correct manner, not that the object handles the call correctly.
 *
 *  If an object is single threaded then it is only called by one thread.
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED singleton</i>
 *
 *  @param <K>
 * @param <V>
 * @author      Nigel Leck
 *  @since       11 September 2012
 */
public final class HashMapTC<K,V> extends HashMap<K,V> implements MonitorTC
{
    private Object trackedObject=new Object();

    /**
     * Constructs an empty <tt>HashMap</tt> with the specified initial
     * capacity and load factor.
     *
     * @param  initialCapacity the initial capacity
     * @param  loadFactor      the load factor
     * @throws IllegalArgumentException if the initial capacity is negative
     *         or the load factor is nonpositive
     */
    public HashMapTC(int initialCapacity, float loadFactor)
    {
        super( initialCapacity, loadFactor);
    }
     /**
     * Constructs an empty <tt>HashMap</tt> with the specified initial
     * capacity and load factor.
     *
     * @param  initialCapacity the initial capacity
     * @throws IllegalArgumentException if the initial capacity is negative
     *         or the load factor is nonpositive
     */
    public HashMapTC(int initialCapacity)
    {
        super( initialCapacity);
    }
     /**
     * Constructs an empty <tt>HashMap</tt> with the specified initial
     * capacity and load factor.
     *
     * @throws IllegalArgumentException if the initial capacity is negative
     *         or the load factor is nonpositive
     */
    public HashMapTC()
    {
        super();
    }

    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    @Override
    public Object clone()
    {
        enter( ThreadCop.ACCESS.READ);
        try
        {
            HashMapTC t2=(HashMapTC) super.clone();
            t2.trackedObject=new Object();

            return t2;
        }
        finally
        {
            leave();
        }
    }

    @Override
    public int size()
    {
        enter( ThreadCop.ACCESS.READ);
        try
        {
            return super.size();
        }
        finally
        {
            leave();
        }
    }

    @Override
    public boolean isEmpty()
    {
        enter( ThreadCop.ACCESS.READ);
        try
        {
            return super.isEmpty();
        }
        finally
        {
            leave();
        }
    }

    @Override
    public V get(Object key)
    {
        enter( ThreadCop.ACCESS.READ);
        try
        {
            return super.get(key);
        }
        finally
        {
            leave();
        }
    }

    @Override
    public boolean containsKey(Object key)
    {
        enter( ThreadCop.ACCESS.READ);
        try
        {
            return super.containsKey(key);
        }
        finally
        {
            leave();
        }
    }

    @Override
    public V put(K key, V value)
    {
        assert key instanceof AbstractMap == false && key instanceof AbstractList==false: "don't use map/list as a key as it will not do what you think it does";

        enter( ThreadCop.ACCESS.MODIFY);
        try
        {
            return super.put(key, value);
        }
        finally
        {
            leave();
        }
    }

    @Override
    public void putAll(Map m)
    {
        enter( ThreadCop.ACCESS.MODIFY);
        try
        {
            super.putAll(m);
        }
        finally
        {
            leave();
        }
    }

    @Override
    public V remove(Object key)
    {
        enter( ThreadCop.ACCESS.MODIFY);

        try
        {
            return super.remove(key);
        }
        finally
        {
            leave();
        }
    }

    @Override
    public void clear()
    {
        enter( ThreadCop.ACCESS.MODIFY);

        try
        {
            super.clear();
        }
        finally
        {
            leave();
        }
    }

    @Override
    public boolean containsValue(Object value)
    {
        enter( ThreadCop.ACCESS.READ);

        try
        {
            return super.containsValue(value);
        }
        finally
        {
            leave();
        }
    }

    @Override
    public Set keySet()
    {
        enter( ThreadCop.ACCESS.READ);
        try
        {
            Set s= super.keySet();
            return new SetTC( s, this);
        }
        finally
        {
            leave();
        }
    }

    @Override
    public Collection values()
    {
        enter( ThreadCop.ACCESS.READ);
        try
        {
            return super.values();
        }
        finally
        {
            leave();
        }
    }

    @Override
    public Set entrySet()
    {
        enter( ThreadCop.ACCESS.READ);
        try
        {
            Set s=super.entrySet();

            return new SetTC( s, this);
        }
        finally
        {
            leave();
        }
    }


    @Override
    public void enter( final ThreadCop.ACCESS access)
    {
        ThreadCop.enter(this, access);
    }

    @Override
    public void leave( )
    {
        ThreadCop.leave(this);
    }

    @Override
    public Object target()
    {
        return trackedObject;
    }
}
