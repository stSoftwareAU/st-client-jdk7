/*
 *  Copyright (c) 1998-2004 ASP Converters pty ltd
 *
 *  www.stSoftware.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.memory;//NOPMD

import com.aspc.remote.util.misc.*;
import java.util.concurrent.locks.Lock;
import org.apache.commons.logging.Log;
import com.aspc.remote.memory.internal.*;

/**
 *  Cache Table is used to hold data cache which may be cleared independently by of the Memory
 *  Manager. The data held by these cache tables will not by default release elements that
 *  are referenced.
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED memory management</i>
 *
 * @author Nigel Leck
 * @param <V> the type. 
 * @since 31 December 1998
 */
public final class ResetableCacheLongTable<V> extends CacheLongTable<V>
{
    /** Recheck any object that doesn't match the read check */
    private int                readCheck;

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.memory.ResetableCacheLongTable");//#LOGGER-NOPMD


    /**
     * Creates a new Cache Long table.
     *
     * @param description The description of this cache table.
     * @param cost The relative cost.
     */
    public ResetableCacheLongTable(final String description, final Cost cost)
    {
        super( description, cost);
        data = createArray( INITIAL_CAPACITY);
    }

    /**
     * Retrieve the entry for the key.
     *
     * @param key The key
     * @return The entry if found
     */
    public ResetableCacheEntryLong getCacheEntry( final long key)
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
                    touch( e);

                    return ((ResetableCacheEntryLong)e);
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
     * make a cache entry for objects with a long for a key.
     *
     * @param key the key
     * @param hashCode the hash number
     * @param referent the object that we want to cache
     * @param next the next entry
     * @return the entry
     */
    @Override
    protected CacheEntryLong makeCacheEntryLong( final long key, final int hashCode, final Object referent, final InterfaceEntry next)
    {
        ResetableCacheEntryLong entry = new ResetableCacheEntryLong(key, hashCode, referent, next, getReadCheck());

        return entry;
    }

    /**
     * create a new array
     * @param size the new size of the array
     * @return the new array
     */
    @Override
    protected InterfaceEntry[] createArray( int size)
    {
        return new ResetableCacheEntryLong[size];
    }

    /**
     *
     * @return the read check
     */
    public int getReadCheck()
    {
        Lock l = rwLock.readLock();
        l.lock();
        try
        {
            return readCheck;
        }
        finally
        {
            l.unlock();
        }
    }

    /**
     * update the read check
     */
    public void incrementReadCheck()
    {
        Lock l = rwLock.writeLock();
        l.lock();
        try
        {
            readCheck++;
        }
        finally
        {
            l.unlock();
        }
    }
}
