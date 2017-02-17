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
package com.aspc.remote.memory.selftest.deadlock;

import com.aspc.remote.memory.MemoryHandler;
import com.aspc.remote.memory.MemoryManager;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;

/**
 *  Test the wrapper classes for Trans header, record and data
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED self test unit</i>
 *
 *  @author      Nigel Leck
 *  @since       27 October 2002
 */
public class SlowMemoryHandler implements MemoryHandler
{
    /**
     * show memory handler
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public SlowMemoryHandler()
    {
        MemoryManager.register( this);
    }
    
    /**
     * 
     * @return the cost
     */
    @Override
    public Cost getCost()
    {
        return Cost.LOWEST;
    }

    /**
     * 
     * @param percentage the percentage
     * @return the amount freed. 
     */
    @Override
    public long freeMemory( double percentage)
    {
        return 0;
    }

    /**
     * 
     * @return the amount freed
     */
    @Override
    public long tidyUp()
    {
        return 0;
    }

    /**
     * 
     * @param key the key to remove
     */
    @SuppressWarnings({"empty-statement", "SleepWhileHoldingLock"})
    public synchronized void remove( String key)
    {
        try
        {
            Thread.sleep( 10000);
            MemoryManager.deregister( this);                
        }
        catch( Exception e)
        {
            ;
        }
        
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
     * 
     * @param percentage the percentage
     * @return the amount freed
     */
    @Override
    public synchronized long queuedFreeMemory( double percentage)
    {
        return 0;
    }
    
    /**
     * 
     * @return the estimated size
     */
    @Override @CheckReturnValue @Nonnegative 
    public long getEstimatedSize()
    {
        return 100000;
    }

    /**
     * 
     * @return when accessed
     */
    @Override
    public long getLastAccessed()
    {
        return System.currentTimeMillis();
    }
}
