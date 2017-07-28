/*
 *  Copyright (c) 1999-2004 ASP Converters pty ltd
 *
 *  www.stSoftware.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.memory;

import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.NumUtil;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;

/**
 * The secondary cache.
 * <br>
 * <i>THREAD MODE: MULTI-THREADED memory management</i>
 *
 * @author dk67933
 *
 * @since 12 April 2011
 */
public final class SecondaryCache
{
    private static final ReferenceQueue<SecondaryCacheKey> RELEASED_QUEUE=new ReferenceQueue<>();

    private static final ConcurrentMap<SecondaryKey, ConcurrentMap<String, Object> > CACHE_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentMap<SecondaryKey, ConcurrentMap<SecondaryKey, ConcurrentMap<String, Object>>> CACHE_GROUP_MAP = new ConcurrentHashMap<>();
    private static final AtomicLong LAST_ACCESSED=new AtomicLong();
    private static final SecondaryCacheHandler MEMORY_HANDLER;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.memory.SecondaryCache");//#LOGGER-NOPMD

    /**
     * How many caches do we have in memory ?
     * @return the count.
     */
    @CheckReturnValue
    public static int countOfCaches()
    {
        int counter=CACHE_MAP.size();
        for( ConcurrentMap subMap: CACHE_GROUP_MAP.values())
        {
            counter+=subMap.size();
        }

        return counter;
    }
    
    /**
     * get the secondary cache
     *
     * @param source the key
     * @return the cache table
     */
    @CheckReturnValue @Nonnull
    public static ConcurrentMap<String, Object> getSecondaryCache(final @Nonnull Object source)
    {
        LAST_ACCESSED.set((int)(MemoryManager.lastTick()/1000L));

        ConcurrentMap<SecondaryKey, ConcurrentMap<String, Object>> sourceMap;
        if (source instanceof SecondaryCacheGroup)
        {
            Object group = ((SecondaryCacheGroup) source).getSecondaryCacheGroupKey();
            assert group != null;

            SecondarySearchKey readKey=new SecondarySearchKey(source);

            sourceMap = CACHE_GROUP_MAP.get(readKey);

            if( sourceMap == null)
            {
                SecondaryCacheKey writeKey=new SecondaryCacheKey(group, CACHE_GROUP_MAP);
                sourceMap = new ConcurrentHashMap<>();

                ConcurrentMap<SecondaryKey, ConcurrentMap<String, Object>> temp = CACHE_GROUP_MAP.putIfAbsent(writeKey, sourceMap);
                if (temp != null)
                {
                    sourceMap = temp;
                }
            }
        }
        else
        {
            sourceMap=CACHE_MAP;
        }

        SecondarySearchKey readKey=new SecondarySearchKey(source);

        ConcurrentMap<String, Object> registeredCache;

        registeredCache = sourceMap.get(readKey);

        if (registeredCache == null)
        {
            registeredCache = new ConcurrentHashMap<>();
            SecondaryCacheKey writeKey=new SecondaryCacheKey(source, sourceMap);
            ConcurrentMap<String, Object> temp = sourceMap.putIfAbsent(writeKey, registeredCache);
            if (temp != null)
            {
                registeredCache = temp;
            }
        }
        
        assert registeredCache!=null;
        
        return registeredCache;
    }

    /**
     * clear the cache by source or group.
     *
     * @param source the key
     */
    public static void clearSecondaryCache(final @Nullable Object source)
    {
        if( source != null)
        {
            if (source instanceof SecondaryCacheGroup)
            {
                Object group = ((SecondaryCacheGroup) source).getSecondaryCacheGroupKey();
                if( group != null)
                {
                    CACHE_GROUP_MAP.remove(new SecondarySearchKey( group));
                }
            }
            else
            {
                CACHE_MAP.remove(new SecondarySearchKey( source));
            }
        }

        MEMORY_HANDLER.tidyUp();
    }

    /**
     * clear all
     */
    public static void clearAllCaches()
    {
        long size=MEMORY_HANDLER.getEstimatedSize();
        CACHE_MAP.clear();
        CACHE_GROUP_MAP.clear();

        MEMORY_HANDLER.tidyUp();

        LOGGER.info( "Secondary cached cleared " +  NumUtil.convertMemoryToHumanReadable(size));
    }

    private SecondaryCache()
    {
    }

    /** clear secondary cache when needed */
    private static class SecondaryCacheHandler implements MemoryHandler
    {
        /** {@inheritDoc }*/
        @Override @CheckReturnValue
        public Cost getCost()
        {
            return MemoryHandler.Cost.MEDIUM_LOW;
        }

        /** {@inheritDoc }*/
        @Override
        public long freeMemory(final @Nonnull double percentage)
        {
            long estimate=getEstimatedSize();
            clearAllCaches();
            assert estimate>=0:"negative freeMemory( " + percentage + "): " + estimate;
            return estimate;
        }

        /** {@inheritDoc }*/
        @Override
        public long tidyUp()
        {
            int count =0;
            while( true)
            {
                SecondaryCacheKey sck=(SecondaryCacheKey) RELEASED_QUEUE.poll();
                if( sck==null) break;
                sck.callbackMap.remove(sck);
                count++;
            }

            return count * 1024;
        }

        /** {@inheritDoc }*/
        @Override
        public long queuedFreeMemory(final @Nonnegative double percentage)
        {
            return freeMemory( percentage);
        }

        /** {@inheritDoc }*/
        @Override
        public long panicFreeMemory()
        {
            return freeMemory(1);
        }

        /** {@inheritDoc }*/
        @Override @CheckReturnValue @Nonnegative 
        public long getEstimatedSize()
        {
            long gmCount= CACHE_GROUP_MAP.size();
            long gmSize=1024 * 1024;
            long mCount= CACHE_MAP.size();
            long mSize=1024;
            long size =gmCount * gmSize  + mCount * mSize;
            assert size>=0:"negative getEstimatedSize(): " + size + "( group count=" + gmCount + ", group element size=" + gmSize + ", map count=" + mCount + ", map element size=" + mSize + ")";

            return size;
        }

        /** {@inheritDoc }*/
        @Override @CheckReturnValue
        public long getLastAccessed()
        {
            return LAST_ACCESSED.get() * 1000;
        }
    }

    @SuppressWarnings("EqualsAndHashcode")
    private static interface SecondaryKey<K>
    {
        K get();
    }

    @SuppressWarnings("EqualsAndHashcode")
    private static class SecondarySearchKey<K> implements SecondaryKey<K>
    {
        private final K key;
        SecondarySearchKey( final K key)
        {
            this.key=key;
        }

        @Override @CheckReturnValue
        public int hashCode() {
            return key.hashCode();
        }

        @Override @CheckReturnValue
        public boolean equals(final Object o2) {

            if( this == o2) return true;

            if ( o2 instanceof SecondaryKey )
            {
                SecondaryKey sk2=(SecondaryKey)o2;

                Object key2=sk2.get();

                return key.equals(key2);
            }

            return false;
        }

        @Override
        public K get() {
            return key;
        }
    }

    @SuppressWarnings("EqualsAndHashcode")
    private static class SecondaryCacheKey extends WeakReference implements SecondaryKey
    {
        private final int hashCode;
        public final ConcurrentMap<SecondaryKey, Object > callbackMap;

        SecondaryCacheKey( final Object key, final ConcurrentMap callbackMap)
        {
            super(key, RELEASED_QUEUE);
            this.callbackMap=callbackMap;

            hashCode=key.hashCode();
        }

        @Override @CheckReturnValue
        public int hashCode() {
            return hashCode;
        }

        @Override @CheckReturnValue
        public boolean equals(final Object o2) {

            if( this == o2) return true;

            if ( o2 instanceof SecondaryKey )
            {
                SecondaryKey sk2=(SecondaryKey)o2;

                Object key1=get();
                Object key2=sk2.get();

                if( key1 != null )
                {
                    return key1.equals(key2);
                }
                else if( key2==null)
                {
                    return true;
                }
            }

            return false;
        }
    }

    static
    {
        MEMORY_HANDLER = new SecondaryCacheHandler();
        MemoryManager.register(MEMORY_HANDLER);
    }
}
