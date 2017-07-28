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
package com.aspc.remote.memory.internal;

import com.aspc.remote.memory.MemoryHandler.Cost;
import com.aspc.remote.memory.MemoryManager;
import com.aspc.remote.util.misc.CLogger;
import java.util.Date;
import org.apache.commons.logging.Log;

/**
 *  Auto Generate constants with java programs.
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED command line application</i>
 *
 *  @author      Nigel Leck
 *  @since       29 May 2002
 */
public class CallMemoryManager implements CallMemoryManagerMBean
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.memory.internal.CallMemoryManager");//#LOGGER-NOPMD

    /**
     * Is the memory full ?
     *
     * @return True if with the upper limit
     */
    @Override
    public boolean isFull()
    {
        return MemoryManager.isFull();
    }

    /**
     * Clear memory ASAP                                                        <br>
     *                                                                          <br>
     * Steps                                                                    <br>
     * <ul>
     * <li> Clear rainy day fund to give us some space to work
     * <li> Call each registered memory handler with the current clear level.
     * </ul>
     *
     * Don't manually run the system.gc() as it will lock every thing up
     * while it runs.
     *
     * @param level Clear memory to this cost level
     * @return The amount cleared
     */
    @Override
    public long clearMemory(int level)
    {
        return MemoryManager.clearMemory( Cost.find(level));
    }

    /**
     * Get the JVM max memory ( when will we get a out of memory error)               <br>
     *
     * The currently is a bug in JDK 1.4 which adds 64m to the number returned by the maxMemory() method
     * of the runtime. We adjust for this. This maybe an issue when we go to JDK1.5, we
     * will need to test see <a href="http://forum.java.sun.com/thread.jspa?threadID=253517&messageID=951032">Runtime.maxMemory() and -Xmx</a>
     *
     * @return the "real" JVM max memory
     */
    @Override
    public long jvmMaxMemory()
    {
        return MemoryManager.jvmMaxMemory();
    }

    /**
     * Manually set the maximum memory to use.
     *
     * @param maximum The max memory.
     */
    @Override
    public void setMaxMemory( String maximum)
    {
        MemoryManager.setMaxMemory(maximum);
    }

    /**
     * Used to set the safe zone parameter from system properties
     *
     * @param percentage The safe zone lower amount
     */
    @Override
    public void setSafeZoneLower(String percentage)
    {
        MemoryManager.setSafeZoneLower( percentage);
    }

    /**
     * What is the safe zone lower currently set to ?
     *
     * @return The safe zone lower amount
     */
    @Override
    public String getSafeZoneLower()
    {
        return "" + MemoryManager.getSafeZoneLower();
    }

    /**
     * Used to set the safe zone parameter from system properties
     *
     * @param percentage The minimum amount to free.
     */
    @Override
    public void setMinFreePercent(String percentage)
    {
        MemoryManager.setMinFreePercent(percentage);
    }

//    /**
//     * what is the minimum percentage to free if we detected that we are below the safe zone limit ?
//     *
//     * @return The minimum amount to free.
//     */
//    @Override
//    public String getMinFreePercent()
//    {
//        return "" + MemoryManager.getMinFreePercent();
//    }

    /**
     * Used to set the safe zone parameter from system properties
     *
     * @param percentage The safe zone upper limit
     */
    @Override
    public void setSafeZoneUpper(String percentage)
    {
        MemoryManager.setSafeZoneUpper(percentage);
    }

    /**
     * At what percentage free will we say that we are full ?
     *
     * @return The safe zone upper limit
     */
    @Override
    public String getSafeZoneUpper()
    {
        return "" + MemoryManager.getSafeZoneUpper();
    }

    /**
     * Number of times GC has been called.
     *
     * @return The number of times GC has been called.
     */
    @Override
    public long getCountGC()
    {
        return MemoryManager.getCountGC();
    }

    /**
     * Total GC time.
     *
     * @return The total time we have spent in GC
     */
    @Override
    public long getGCtime()
    {
        return MemoryManager.getGCtime();
    }

    /**
     * The total memory limited to the MAX_MEMORY defined by the command line.
     * if MAX_MEMORY was not set and the total memory is greater than the MAX_MEMORY then
     * set MAX_MEMORY
     *
     * @return The total number of bytes used.
     */
    @Override
    public long getTotalMemory()
    {
        return MemoryManager.getTotalMemory();
    }

    /**
     * The percentage free.
     *
     * @return 0-1 percent free.
     */
    @Override
    public double getFreePercent()
    {
        return MemoryManager.getFreePercent();
    }

    /**
     * The upper safety zone as a percentage 0-1
     *
     * @return The percentage
     */
    @Override
    public double getSafeUpperPercent()
    {
        return MemoryManager.getSafeUpperPercent();
    }

    /**
     * The free Memory, if we have defined the MAX_MEMORY to be more than what is
     * currently reserved then add this to the free memory. If the reserved is more than the
     * amount defined on the command line ignore the extra amount. For sensibility reasons
     * never return a negative amount.
     *
     * @return the free memory
     */
    @Override
    public long getFreeMemory()
    {
        return MemoryManager.getFreeMemory();
    }

    /**
     * Call System.gc() regardless of when it has been called last.
     * @return Was it called ?
     */
    @Override
    public boolean gc()//NOPMD
    {
        return MemoryManager.gc();
    }

    /**
     * The amount of memory used
     *
     * @return The number of bytes used.
     */
    @Override
    public long getUsedMemory()
    {
        return MemoryManager.getTotalUsed();
    }

    /**
     * The last level cleared
     *
     * @return The last level cleared.
     */
    @Override
    public int getLastClearedLevel()
    {
        return MemoryManager.getLastClearedLevel().level;
    }

    /**
     * How many times have the memory been cleared.
     *
     * @return The number of times.
     */
    @Override
    public int getTotalClearedCount()
    {
        return MemoryManager.getTotalClearedCount();
    }

    /**
     * how much time has been taken clearing memory.
     *
     * @return The number of times.
     */
    @Override
    public long getTotalClearedTimeTaken()
    {
        return MemoryManager.getTotalClearedTimeTaken();
    }

    /**
     * The maximum level cleared.
     *
     * @return The maximum level cleared.
     */
    @Override
    public int getHighWaterMark()
    {
        return MemoryManager.getHighWaterMark().level;
    }

    /**
     * Last time the maximum level was cleared.
     *
     * @return The maximum level cleared.
     */
    @Override
    public Date getHighWaterMarkTime()
    {
        return MemoryManager.getHighWaterMarkTime();
    }

    /**
     * The last time memory was cleared.
     *
     * @return The system time last cleared.
     */
    @Override
    public long getLastClearedTime()
    {
        return MemoryManager.getLastClearedTime();
    }

    /**
     * The estimated size of the data held at this cost level
     *
     * @param level The cost level
     * @return The estimated amount held.
     */
    @Override
    public long estimateLevel( final int level)
    {
        try
        {
            return MemoryManager.estimateLevel( Cost.find(level));
        }
        catch( InterruptedException ie)
        {
            LOGGER.warn( "estimate level", ie);
            Thread.currentThread().interrupt();
            return -1;
        }
    }
}
