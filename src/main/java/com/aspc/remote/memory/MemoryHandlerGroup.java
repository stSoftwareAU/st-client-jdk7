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
package com.aspc.remote.memory;

import com.aspc.remote.util.misc.CLogger;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;
import org.apache.commons.logging.Log;
import com.aspc.remote.memory.internal.*;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;

/**
 *  MemoryHandler
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED memory management</i>
 *
 *  @author      Nigel Leck
 *  @since       15 November 1998
 */
public class MemoryHandlerGroup implements MemoryHandler
{
    /**
     * Create a new memory group.
     * @param description The description
     * @param cost the cost
     */
    public MemoryHandlerGroup(final @Nonnull String description, final @Nonnull Cost cost)
    {
        this.description = description;
        this.cost = cost;
        handlers = new ArrayList();
    }

    /**
     * The description
     * @return  The description
     */
    @Override @Nonnull @CheckReturnValue
    public String toString()
    {
        return description;
    }

    /**
     * The description
     * @return the description
     */
    @Nonnull @CheckReturnValue
    public String getDescription()
    {
        return description;
    }

    /**
     * register a memory handler.
     * @param handler The memory handler to register.
     */
    public void add( final @Nonnull MemoryHandler handler)
    {
        if( handler instanceof CacheTableTemplate)
        {
            CacheTableTemplate ctt = (CacheTableTemplate)handler;

            ctt.setThreshold( Integer.MAX_VALUE);
        }

        synchronized( handlers)
        {
            handlers.add( handler);
        }

        if( registered.get()==false)
        {
            MemoryManager.register( this);
            registered.set(true);
        }
    }

    /**
     * The cost level for this MemoryHandler.
     * @return The cost
     */
    @Override @Nonnull @CheckReturnValue
    public Cost getCost()
    {
        return cost;
    }

    /**
     * Free memory at this level and below. <br>
     * To prevent a possible deadlock this method should not be synchronized. It should call
     * MemoryManager.callFreeMemory which in turn calls queuedFreeMemory()
     *
     * @param percentage to be freed
     *
     * @return the approximate amount freed
     */
    @Override
    public long freeMemory(final @Nonnegative double percentage)
    {
        MemoryHandler list[] = list();

        long total = 0;
        for (MemoryHandler list1 : list) {
            if (list1.getEstimatedSize() > 0) {
                total += list1.freeMemory(percentage);
            }
        }

        return total;
    }

    /**
     * How many items do we have a hard link on ?
     * @return The count.
     */
    @CheckReturnValue
    public int getHardLinkCount()
    {
        MemoryHandler list[] = list();

        int count = 0;
        for (MemoryHandler h : list) {
            if( h instanceof CacheTableTemplate )
            {
                CacheTableTemplate ct = (CacheTableTemplate)h;

                count += ct.getHardLinkCount();
            }
        }

        return count;
    }

    /**
     * The estimated number of items held.
     * @return The count of the items held.
     */
    @CheckReturnValue
    public int size()
    {
        MemoryHandler list[] = list();

        int count = 0;
        for (MemoryHandler h : list) {
            if( h instanceof CacheTableTemplate )
            {
                CacheTableTemplate ct = (CacheTableTemplate)h;

                count += ct.size();
            }
        }

        return count;
    }

    /**
     * List of memory handlers that have been registered.
     * @return The list of handlers.
     */
    @Nonnull @CheckReturnValue
    public MemoryHandler[] list()
    {
        MemoryHandler list[];
        synchronized( handlers)
        {
            list = new MemoryHandler[ handlers.size()];

            handlers.toArray(list);
        }

        return list;
    }

    /**
     * Tidy up the memory handler
     * @return The total bytes cleared.
     */
    @Override
    public long tidyUp()
    {
        MemoryHandler list[] = list();

        long total = 0;
        for (MemoryHandler list1 : list) {
            if (list1.getEstimatedSize() > 0) {
                total += list1.tidyUp();
            }
        }

        return total;
    }

    /**
     * Clear all memory ASAP.
     *
     * @return The estimated total number of bytes released.
     */
    @Override
    public long panicFreeMemory()
    {
        return queuedFreeMemory(1);
    }

    /**
     * This method should implement the freeing of the method and maybe synchronized.
     *
     * @param percentage The percentage to be cleared.
     * @return the estimated amount cleared.
     */
    @Override
    public long queuedFreeMemory( final @Nonnegative double percentage)
    {
        MemoryHandler list[] = list();

        long total = 0;
        for (MemoryHandler mh : list) {
            if( mh.getEstimatedSize() > 0)
            {
                total += mh.queuedFreeMemory( percentage);
            }
        }

        return total;
    }

    /**
     * The estimated size of data held by this memory handler
     *
     * @return The estimated number of bytes held.
     */
    @Override @CheckReturnValue @Nonnegative 
    public long getEstimatedSize()
    {
        MemoryHandler list[] = list();

        long estSize = 0;
        for (MemoryHandler mh : list) {
            long mhSize=mh.getEstimatedSize();
            assert mhSize>=0:mh +" estimated size should be nonnegative" + mhSize;
            estSize += mh.getEstimatedSize();
        }
        assert estSize>=0:"Estimated size should be nonnegative" + estSize;
        return estSize;
    }

    /**
     * The last time this memory handle was accessed
     *
     * @return The last time in seconds this memory handler was accessed.
     */
    @Override @CheckReturnValue
    public long getLastAccessed()
    {
        MemoryHandler list[] = list();

        long max = 0;
        for (MemoryHandler list1 : list) {
            long temp = list1.getLastAccessed();
            if( temp > max)
            {
                max = temp;
            }
        }

        return max;
    }

    private final ArrayList handlers;
    private final Cost cost;
    private final String description;
    private final AtomicBoolean registered=new AtomicBoolean();
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.memory.MemoryHandlerGroup");//#LOGGER-NOPMD
}
