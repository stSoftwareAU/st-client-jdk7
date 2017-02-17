/*
 *  Copyright (c) 1998-2004 ASP Converters pty ltd
 *
 *  www.aspconverters.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.memory;//NOPMD

import com.aspc.remote.database.*;
import com.aspc.remote.util.misc.*;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.concurrent.locks.Lock;
import org.apache.commons.logging.Log;
import com.aspc.remote.memory.internal.*;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *  Cache Table is used to hold data cache which may be cleared independently by of the Memory
 *  Manager. The data held by these cache tables will not by default release elements that
 *  are referenced.
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED memory management</i>
 *
 *  @author      Nigel Leck
 * @param <K> key type
 * @param <V> value type
 *  @since       31 December 1998
 */
public final class CacheTable<K,V> extends CacheTableTemplate<V>
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.memory.CacheTable");//#LOGGER-NOPMD

    /**
     * Creates a new cache table
     *
     * @param description The description of this cache table.
     */
    public CacheTable(final @Nonnull String description)
    {
        this( description, Cost.LOW);
    }

    /**
     * creates a new cache table
     *
     * @param description the description of this cache table
     * @param cost The relative cost of this cache table.
     */
    public CacheTable(final @Nonnull String description, final @Nonnull Cost cost)
    {
        super( description, cost);
        data = createArray( INITIAL_CAPACITY);
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified
     * key.
     *
     * @param key key whose presence in this Map is to be tested.
     *
     * @return <tt>true</tt> if this map contains a mapping for the specified key.
     */
    @CheckReturnValue
    public boolean containsKey(final @Nonnull K key)
    {
        Lock l = rwLock.readLock();
        l.lock();
        try
        {
            if( data == null) return false;

            int hash = key.hashCode();

            int index = (hash & 0x7FFFFFFF) % data.length;

            for (InterfaceEntry e = (InterfaceEntry)data[index]; e != null; e = e.next())
            {
                if (e.hashCode()==hash && key.equals(((CacheEntryObject)e).key()))
                {
                    return true;
                }
            }

            return false;
        }
        finally
        {
            l.unlock();
        }
    }

    /**
     * Removes the mapping for this key from this map if present.
     *
     * @param key key whose mapping is to be removed from the map.
     *
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key.  A <tt>null</tt> return can
     *         also indicate that the map previously associated <tt>null</tt>
     *         with the specified key.
     */
    @Nullable
    public V remove(final @Nonnull K key)
    {
        Lock l = rwLock.writeLock();
        l.lock();
        try
        {
            if( data == null) return null;

            int hash = key.hashCode();
            int index = (hash & 0x7FFFFFFF) % data.length;

            for (
                InterfaceEntry e = (InterfaceEntry)data[index], prev = null;
                e != null;
                prev = e, e = e.next()
            )
            {
                if ((e.hashCode() == hash))
                {
                    if( key.equals(((CacheEntryObject)e).key()))
                    {
                        Object oldValue = e.get();

                        removeElement( e, index, prev, oldValue == null);

                        checkRegister();

                        return (V)oldValue;
                    }
                }
            }

            return null;
        }
        finally
        {
            l.unlock();
        }
    }

    /**
     * Retrieve the data for the key.
     *
     * Only check the weak table if it contains keys
     * that are not in hard map table. We do this because
     * weak references are slow on Solaris as of 2.8
     *
     * @param key The key to get.
     *
     * @return The object if found.
     */
    @CheckReturnValue @Nullable
    public V get( final @Nonnull K key)
    {
        Lock l = rwLock.readLock();
        l.lock();
        try
        {
            if( data == null) return null;

            int hash = key.hashCode();
            int index = (hash & 0x7FFFFFFF) % data.length;

            for (InterfaceEntry e = (InterfaceEntry)data[index]; e != null; e = e.next())
            {
                if ((e.hashCode() == hash))
                {
                    if( key.equals(((CacheEntryObject)e).key()))
                    {
                        Object value;

                        value = e.get();

                        if( value == null)
                        {
                            estEmptyCount++;

                            checkRegister();
                        }
                        else
                        {
                            touch( e);
                        }

                        return (V)value;
                    }
                }
            }

            return null;
        }
        finally
        {
            l.unlock();
        }
    }

    /**
     * a short hand method for getting an object and if that object is a
     * NotFoundException re-throwing the exception.
     *
     * @param key The key to find.
     * @throws Exception Not Found if the object is known not to exist.
     *
     * @return the Object
     */
    @Nullable @CheckReturnValue
    public V find( final @Nonnull K key) throws Exception
    {
        Object o;

        o = get(key);

        if( o instanceof Exception)
        {
            if( LOGGER.isDebugEnabled())
            {
                if( o instanceof NotFoundException)
                {
                    throw new NotFoundException( description + " {" + key + "}");
                }
                if( o instanceof NullValueException)
                {
                    throw new NullValueException( description + " {" + key + "}");
                }
            }
            throw (Exception)o;
        }

        return (V)o;
    }

    /**
     * Puts data into the cache table.
     * Common data is replaced to save memory
     *
     * @param orginal The object to put into the table.
     * @param key The key to be used.
     *
     * @return the object that has been replaced.
     */
    @Nullable
    public V put(final @Nonnull K key, final @Nullable V orginal)
    {
        assert key instanceof AbstractMap == false && key instanceof AbstractList==false: "don't use map/list as a key as it will not do what you think it does";

        if( orginal == null)
        {
            return remove( key);
        }

        Object value;
        value = CommonData.recycle( orginal);

        /* Makes sure the key is not already in the HashMap. */
        int hash;

        hash = key.hashCode();

        CacheEntryObject addedEntry;
        Lock l = rwLock.writeLock();
        l.lock();
        try
        {
            int index;

            index = (hash & 0x7FFFFFFF) % data.length;

            InterfaceEntry prev = null;

            for (InterfaceEntry e = (InterfaceEntry)data[index] ; e != null ; e = e.next())
            {
                if ((e.hashCode() == hash))
                {
                    /**
                     * Replacing an existing entry.
                     */
                    if( key.equals(((CacheEntryObject)e).key()))
                    {
                        Object old = e.get();

                        CacheEntryObject insert = new CacheEntryObject( key, hash, value, e.next());

                        if( prev != null)
                        {
                            prev.setNext( insert);
                        }
                        else
                        {
                            data[index] = insert;
                        }

                        return (V)old;
                    }
                }

                prev = e;
            }

            index = rehash( index, hash);

            // Creates the new entry.
            addedEntry = new CacheEntryObject(key, hash, value, (InterfaceEntry)data[index]);
            data[index] = addedEntry;
            count++;

            checkRegister();
        }
        finally
        {
            l.unlock();
        }
        touch( addedEntry);

        return null;
    }

    /**
     * create a new array
     * @param size the new size of the array
     * @return the new array
     */
    @Override @CheckReturnValue @Nonnull
    protected InterfaceEntry[] createArray( int size)
    {
        return new InterfaceEntryObject[size];
    }
}
