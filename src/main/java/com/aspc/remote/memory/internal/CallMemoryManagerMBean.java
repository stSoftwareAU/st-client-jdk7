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

import java.util.Date;

/**
 *  Auto Generate constants with java programs.
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED command line application</i>
 *
 *  @author      Nigel Leck
 *  @since       29 May 2002
 */
public interface CallMemoryManagerMBean
{           
    /**
     * Is the memory full ?
     *
     * @return True if with the upper limit
     */
    public boolean isFull();

    /**
     * Clear memory ASAP                                                        <br>
     *                                                                          <br>
     * Steps                                                                    <br>
     * <ol>
     * <li> Clear rainy day fund to give us some space to work
     * <li> Call each registered memory handler with the current clear level.
     * </ol>
     *
     * Don't manually run the system.gc() as it will lock every thing up
     * while it runs.
     *
     * @param level Clear memory to this cost level
     * @return The amount cleared
     */
    public long clearMemory(int level);
    
    /**
     * Get the JVM max memory ( when will we get a out of memory error)               <br>
     * 
     * The currently is a bug in JDK 1.4 which adds 64m to the number returned by the maxMemory() method
     * of the runtime. We adjust for this. This maybe an issue when we go to JDK1.5, we 
     * will need to test see <a href="http://forum.java.sun.com/thread.jspa?threadID=253517&messageID=951032">Runtime.maxMemory() and -Xmx</a>
     *
     * @return the "real" JVM max memory
     */
    public long jvmMaxMemory();
    
    /**
     * Manually set the maximum memory to use.
     *
     * @param maximum The max memory.
     */
    public void setMaxMemory( String maximum);

    /**
     * Used to set the safe zone parameter from system properties
     *
     * @param percentage The safe zone lower amount
     */
    public void setSafeZoneLower(String percentage);

    /**
     * What is the safe zone lower currently set to ?
     *
     * @return The safe zone lower amount
     */
    public String getSafeZoneLower();

    /**
     * Used to set the safe zone parameter from system properties
     *
     * @param percentage The minimum amount to free.
     */
    public void setMinFreePercent(String percentage);

//    /**
//     * what is the minimum percentage to free if we detected that we are below the safe zone limit ?
//     *
//     * @return The minimum amount to free.
//     */
//    public String getMinFreePercent();

    /**
     * Used to set the safe zone parameter from system properties
     *
     * @param percentage The safe zone upper limit
     */
    public void setSafeZoneUpper(String percentage);

    /**
     * At what percentage free will we say that we are full ?
     *
     * @return The safe zone upper limit
     */
    public String getSafeZoneUpper();

    /**
     * Number of times GC has been called.
     *
     * @return The number of times GC has been called. 
     */
    public long getCountGC();

    /**
     * Total GC time.
     *
     * @return The total time we have spent in GC
     */
    public long getGCtime();

    /**
     * The total memory limited to the MAX_MEMORY defined by the command line.
     * if MAX_MEMORY was not set and the total memory is greater than the MAX_MEMORY then
     * set MAX_MEMORY
     *
     * @return The total number of bytes used.
     */
    public long getTotalMemory();

    /**
     * The percentage free.
     *
     * @return 0-1 percent free.
     */
    public double getFreePercent();

    /**
     * The upper safety zone as a percentage 0-1
     *
     * @return The percentage
     */
    public double getSafeUpperPercent();

    /**
     * The free Memory, if we have defined the MAX_MEMORY to be more than what is
     * currently reserved then add this to the free memory. If the reserved is more than the
     * amount defined on the command line ignore the extra amount. For sensibility reasons
     * never return a negative amount.
     *
     * @return the free memory
     */
    public long getFreeMemory();

    /**
     * Call System.gc() regardless of when it has been called last.
     * @return Was it called ?
     */
    public boolean gc();//NOPMD
    
    /**
     * The amount of memory used
     *
     * @return The number of bytes used.
     */
    public long getUsedMemory();

    /**
     * The last level cleared
     *
     * @return The last level cleared.
     */
    public int getLastClearedLevel();

    /**
     * How many times have the memory been cleared.
     *
     * @return The number of times.
     */
    public int getTotalClearedCount();
        
    /**
     * how much time has been taken clearing memory.
     *
     * @return The number of times.
     */
    public long getTotalClearedTimeTaken();
    
    /**
     * The maximum level cleared.
     *
     * @return The maximum level cleared.
     */
    public int getHighWaterMark();

    /**
     * Last time the maximum level was cleared.
     *
     * @return The maximum level cleared.
     */
    public Date getHighWaterMarkTime();

    /**
     * The last time memory was cleared.
     *
     * @return The system time last cleared.
     */
    public long getLastClearedTime();

    /**
     * The estimated size of the data held at this cost level
     *
     * @param level The cost level
     * @return The estimated amount held.
     */
    public long estimateLevel( int level);
}
