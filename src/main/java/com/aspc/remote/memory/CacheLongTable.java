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
package com.aspc.remote.memory;

import com.aspc.remote.database.*;
import com.aspc.remote.util.misc.*;
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
 * @author Nigel Leck
 * @param <V> the class.
 * @since 31 December 1998
 */
public class CacheLongTable<V> extends CacheTableTemplate<V>
{
    /**
     * Creates a new Cache Long table.
     *
     * @param description The description of this cache table.
     */
    public CacheLongTable(final @Nonnull String description)
    {
        this( description, Cost.MEDIUM);
    }

    /**
     * Creates a new Cache Long table.
     *
     * @param description The description of this cache table.
     * @param cost The relative cost.
     */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public CacheLongTable(final @Nonnull String description, final @Nonnull Cost cost)
    {
        super( description, cost);
        data = createArray( INITIAL_CAPACITY);//NOPMD
    }

    /**
     * creates a array of the keys.
     *
     * @return The array keys
     */
    @CheckReturnValue @Nonnull
    public long[] getKeyArray()
    {
        Lock l = rwLock.readLock();
        l.lock();
        try
        {
            long list[] = new long[ count];

            if( data == null) return list;

            int j = 0;
            for (int i = data.length ; i-- > 0 ;)
            {
                for (InterfaceEntry e = (InterfaceEntry)data[i] ; e != null ; e = e.next())
                {
                    InterfaceEntryLong temp = (InterfaceEntryLong)e;

                    list[j] = temp.key();

                    j++;
                }
            }

            return list;
        }
        finally
        {
            l.unlock();
        }
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified
     * key.
     *
     * @return <tt>true</tt> if this map contains a mapping for the specified key.
     * @param key key whose presence in this Map is to be tested.
     */
    @CheckReturnValue
    public boolean containsKey(final long key)
    {
        Lock l = rwLock.readLock();
        l.lock();
        try
        {
            if( data == null) return false;

            int hash = (int)key;

            int index = (hash & 0x7FFFFFFF) % data.length;

            for (InterfaceEntry e = (InterfaceEntry)data[index]; e != null; e = e.next())
            {
                if (key == ((InterfaceEntryLong)e).key())
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
     * Returns <tt>true</tt> if the entry is locked into memory
     *
     * @return <tt>true</tt> if locked.
     * @param key key whose presence in this Map is to be tested.
     */
    @CheckReturnValue
    public boolean isLocked(final long key)
    {
        Lock l = rwLock.readLock();
        l.lock();
        try
        {
            if( data == null) return false;

            int hash = (int)key;

            int index = (hash & 0x7FFFFFFF) % data.length;

            for (InterfaceEntry e = (InterfaceEntry)data[index]; e != null; e = e.next())
            {
                if (key == ((InterfaceEntryLong)e).key())
                {
                    return e instanceof LockedEntryLong;
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
     *
     * @param key the key to lock
     * @return true if locked.
     */
    public boolean lock( final long key)
    {
        Lock l = rwLock.writeLock();
        l.lock();
        try
        {
            if( data == null) return false;

            int hash = (int)key;

            int index = (hash & 0x7FFFFFFF) % data.length;

            InterfaceEntry prev = null;
            for (InterfaceEntry e = (InterfaceEntry)data[index]; e != null; e = e.next())
            {
                if (key == ((InterfaceEntryLong)e).key())
                {
                    if( e instanceof CacheEntryLong)
                    {
                        CacheEntryLong ce = (CacheEntryLong)e;

                        Object obj = ce.get();

                        if( obj != null)
                        {
                            LockedEntryLong lel = new LockedEntryLong(ce.key(), ce.hashCode(), obj, ce.next());

                            if( prev != null)
                            {
                                prev.setNext( lel);
                            }
                            else
                            {
                                data[index]=lel;
                            }
                            ce.clear();
                            return true;
                        }
                    }
                    return false;
                }

                prev = e;
            }

            return false;
        }
        finally
        {
            l.unlock();
        }
    }

    /**
     *
     * @param key the key to unlock
     * @return true if unlocked.
     */
    public boolean unlock( final long key)
    {
        Lock l = rwLock.writeLock();
        l.lock();
        try
        {
            if( data == null) return false;

            int hash = (int)key;

            int index = (hash & 0x7FFFFFFF) % data.length;

            InterfaceEntry prev = null;
            for (InterfaceEntry e = (InterfaceEntry)data[index]; e != null; e = e.next())
            {
                if (key == ((InterfaceEntryLong)e).key())
                {
                    if( e instanceof LockedEntryLong)
                    {
                        LockedEntryLong lel = (LockedEntryLong)e;

                        CacheEntryLong ce = new CacheEntryLong(lel.key(), lel.hashCode(), lel.get(), lel.next());

                        if( prev != null)
                        {
                            prev.setNext( ce);
                        }
                        else
                        {
                            data[index]=ce;
                        }

                        return true;
                    }
                    return false;
                }

                prev = e;
            }

            return false;
        }
        finally
        {
            l.unlock();
        }
    }

    /**
     * Release the hard link to the object.
     *
     * Returns <tt>true</tt> if this map contains a mapping for the specified
     * key.
     *
     * @return <tt>true</tt> if this map contains a mapping for the specified key.
     * @param key key whose presence in this Map is to be tested.
     */    
    public boolean release(long key)
    {
        Lock l = rwLock.writeLock();
        l.lock();
        try
        {
            if( data == null) return false;

            int hash = (int)key;

            int index = (hash & 0x7FFFFFFF) % data.length;

            for (InterfaceEntry e = (InterfaceEntry)data[index]; e != null; e = e.next())
            {
                if (key == ((InterfaceEntryLong)e).key())
                {
                    if( e.hasHardReference())
                    {
                        e.clearHardReference();
                        return true;
                    }
                    else
                    {
                        return false;
                    }
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
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key.  A <tt>null</tt> return can
     *         also indicate that the map previously associated <tt>null</tt>
     *         with the specified key.
     */
    @Nullable
    public V remove(final long key)
    {
        Lock l = rwLock.writeLock();
        l.lock();
        try
        {
            if( data == null) return null;

            int hash = (int)key;
            int index = (hash & 0x7FFFFFFF) % data.length;

            InterfaceEntry prev = null;

            for (
                InterfaceEntry e = (InterfaceEntry)data[index];
                e != null;
                e = e.next()
            )
            {
                if( key == ((InterfaceEntryLong)e).key())
                {
                    V oldValue = (V)e.get();

                    removeElement( e, index, prev, oldValue == null);

                    checkRegister();

                    return oldValue;
                }

                prev = e;
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
     * @param key The key
     * @return The object if found
     */
    @Nullable
    public V get( final long key)
    {
        Lock l = rwLock.readLock();
        l.lock();
        try
        {
            if( data == null) return null;

            int hash = (int)key;
            int index = (hash & 0x7FFFFFFF) % data.length;

            for (InterfaceEntry e = (InterfaceEntry)data[index]; e != null; e = e.next())
            {
                if( key == ((InterfaceEntryLong)e).key())
                {
                    V value;

                    value = (V)e.get();

                    if( value == null)
                    {
                        /* OK remove this entry */
                        estEmptyCount++;

                        checkRegister();
                    }
                    else
                    {
                        touch( e);
                    }

                    return value;
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
     * find the key and throw NotFound if known.
     *
     * @param key the key
     * @throws Exception NotFoundException if the object is known not to exist.
     * @return The found object.
     */
    @Nullable
    public V find( final long key) throws Exception
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
     * @param orginal The object to put into the cache table.
     * @param key The key of this object
     * @return the object that has been replaced.
     */
    @Nullable
    public V put(final long key, final @Nullable V orginal)
    {
        if( orginal == null)
        {
            return remove( key);
        }

        Object value;
        value = CommonData.recycle( orginal);

        /** Makes sure the key is not already in the HashMap.*/
        int hash= (int)key;

        CacheEntryLong addedEntry;

        Lock l = rwLock.writeLock();
        l.lock();
        try
        {
            int index = (hash & 0x7FFFFFFF) % data.length;

            InterfaceEntry prev = null;

            for (InterfaceEntry e = (InterfaceEntry)data[index] ; e != null ; e = e.next())
            {
                /**
                 * Replacing an existing entry.
                 */
                if( key == ((InterfaceEntryLong)e).key())
                {
                    Object old = e.get();

                    InterfaceEntryLong insert;

                    if( e instanceof LockedEntryLong)
                    {
                        insert = new LockedEntryLong( key, hash, value, e.next());
                    }
                    else
                    {
                        insert = makeCacheEntryLong( key, hash, value, e.next());
                    }

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

                prev = e;
            }

            index = rehash( index, hash);

            /* Creates the new entry. */
            addedEntry = makeCacheEntryLong(key, hash, value, (InterfaceEntry)data[index]);
            data[index] = addedEntry;
            count++;

            checkRegister();

            touch( addedEntry);

            return null;
        }
        finally
        {
            l.unlock();
        }
    }

    /**
     * make a cache entry for objects with a long for a key.
     *
     * @param key the key
     * @param hashCode the hash number
     * @param referent the object that we want to cache
     * @param next the next entry
     * @return the entry
     */
    protected CacheEntryLong makeCacheEntryLong( final long key, final int hashCode, final Object referent, final InterfaceEntry next)
    {
        return new CacheEntryLong(key, hashCode, referent, next);
    }

    /**
     * Returns the current object which is in this cache table.
     *
     * @param key The key of the object to place
     * @param value The object to place.
     * @return The actual object stored.
     */    
    public @Nonnull V placeIfAbsent(final long key, final @Nonnull V value)
    {
        if( value==null) throw new IllegalArgumentException("value must not be null");
        V v=place(key, value, true);
        assert v!=null;
        return v;
    }
    
    /**
     * Returns the current object which is in this cache table.
     *
     * @param key The key of the object to place
     * @param orginal The object to place.
     * @param keepFoundObject Should we keep the original object if found ( are they the same)
     * @return The actual object stored.
     */
    @Nullable
    public V place(final long key, final @Nullable V orginal, boolean keepFoundObject)
    {
        if( orginal == null)
        {
            return remove( key);
        }

        Object value;
        value = CommonData.recycle( orginal);

        /** Makes sure the key is not already in the HashMap.*/
        int hash= (int)key;

        InterfaceEntryLong addedEntry;

        Lock l = rwLock.writeLock();
        l.lock();
        try
        {
            int index = (hash & 0x7FFFFFFF) % data.length;

            InterfaceEntry prev = null;

            for (InterfaceEntry e = (InterfaceEntry)data[index] ; e != null ; e = e.next())
            {
                /**
                 * Replacing an existing entry.
                 */
                if( key == ((InterfaceEntryLong)e).key())
                {
                    Object old = e.get();

                    /**
                     * If we should keep the original object then just return the original version.
                     * this cuts down on the two call overhead in VirtualDB and DBData
                     */
                    if(
                        old == null ||
                        keepFoundObject == false
                    )
                    {
                        CacheEntryLong insert = makeCacheEntryLong( key, hash, value, e.next());

                        if( prev != null)
                        {
                            prev.setNext( insert);
                        }
                        else
                        {
                            data[index] = insert;
                        }

                        return (V)value;
                    }
                    else
                    {
                        return (V)old;
                    }
                }

                prev = e;
            }

            index = rehash( index, hash);

            /* Creates the new entry. */
            addedEntry = makeCacheEntryLong(key, hash, value, (InterfaceEntry)data[index]);

            data[index] = addedEntry;

            count++;

            checkRegister();
        }
        finally
        {
            l.unlock();
        }

        touch( addedEntry);

        return (V)value;
    }

    /**
     * create a new array
     * @param size the new size of the array
     * @return the new array
     */
    @Override @Nonnull
    protected InterfaceEntry[] createArray( final int size)
    {
        return new InterfaceEntryLong[size];
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.memory.CacheLongTable");//#LOGGER-NOPMD
}
