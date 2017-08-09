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

import com.aspc.remote.memory.MemoryHandler.Cost;
import com.aspc.remote.util.misc.*;
import com.aspc.remote.util.timer.StopWatch;
import java.lang.management.*;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nonnull;
import org.apache.commons.logging.Log;
import com.aspc.remote.memory.internal.*;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nullable;

/**
 *  MemoryManager handles low memory situations.
 *
 *  Each Handler has concept of "cost" so low cost items are cleared first then medium and then high.   <br>
 *                                                                                                      <br>
 *  Examples:                                                                                           <br>
 *   <UL>
 *      <lI> Low cost: Java Object Pooling
 *      <LI> Medium cost: Data loaded from the database.
 *      <LI> High Cost: Temporary sort tables
 *   </UL>                                                                                              <br>
 *                                                                                                      <br>
 * CacheEntry to have a accesTime element.                                                              <br>
 * Change the clearMemory method of CacheTable to take a percentage                                     <br>
 * The CacheTable.get() method will set accessTime to the current time. Actually the integer version of System.currentTimeMills()
 * when the MemoryManager's check loop last ran.
 *
 * <OL>
 *   <LI> When the MemoryManager detects a low memory situation ( < 10%)
 *   <LI> Ask ALL MemoryHandlers for an estimate of the number of elements they contain
 *   <LI> Increment the cost level until at least 20% of the elements are covered.
 *   <LI> For each MemoryHandler under the calculated cost level call clearMemory() with 100 percent. This clears non referenced data so many elements are not deallocated. This is done with WeakReferences
 *   <LI> For each MemoryHandler at the Calculated cost level call clearMemory() with the percentage that adds up to the 20% when taking into account the elements free from the lower cost levels.
 *   <LI> CacheTable is just one type of MemoryHandler. For a CacheTable if the required percentage is less than 100% we know the create time or the last cleared time and the last retrieval time. Find the mid point between these two times and clear all elements lastAccess at or before this time.
 *   <LI> Call System.gc() and check the available memory is > 10% if not increment the cost level and try again
 * </OL>
 * <BR>
 * <br>
 *  Define the system property MAX_MEMORY to set the initial threshold limit. Defaults to 32m.
 *  You may add the suffix of G, M and K.                                                           <br>
 *                                                                                                  <br>
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED memory management</i>
 *
 *  @author      Nigel Leck
 *  @since       January 23, 1999, 2:20 PM
 */
@SuppressWarnings({"BroadCatchBlock", "TooBroadCatch", "UseSpecificCatch"})
public final class MemoryManager
{
    private static Issue lastIssue=null;
    
    /**
     * The minimum TENURED percent environment variable.
     * The percentage of the total memory.
     */
    public static final String TENURED_PERCENT="TENURED_PERCENT";

    /**
     * The TENURED size environment variable.
     */
    public static final String TENURED_SIZE="TENURED_SIZE";

    /**
     * How many times has panic been called.
     */
    private static final AtomicLong PANIC_COUNT=new AtomicLong();

    /**
     * The minimum safe zone percentage default
     */
    private static final int SAFE_ZONE_LOWER_DEFAULT = 20;

    /**
     * The upper safe zone percentage default ( when we should stop loading cache etc)
     */
    private static final int SAFE_ZONE_UPPER_DEFAULT = 40;

    /**
     * The minimum to be freed percentage
     */
    private static final int MIN_FREE_PERCENT_DEFAULT = 15;

    /**
     * The minimum to be freed percentage
     */
    private static int minFreePercent = MIN_FREE_PERCENT_DEFAULT;//MT CHECKED

    /** The last time the memory allocation was checked.  */
    private static final AtomicLong LAST_TICK=new AtomicLong(System.currentTimeMillis());

    /**
     * The minium safe zone percentage
     */
    private static int safeZoneLower=SAFE_ZONE_LOWER_DEFAULT;//MT CHECKED

    /**
     * The upper safe zone percentage ( when we stop loading cache)
     */
    private static int safeZoneUpper = SAFE_ZONE_UPPER_DEFAULT;//MT CHECKED

    /**
     * The last level cleared
     */
    @Nonnull
    private static Cost lastClearedLevel = Cost.LOWEST;

    /**
     * total number of times cleared
     */
    private static int totalClearedCount;//MT CHECKED

    /**
     * total number of times cleared
     */
    private static long totalClearedTimeTaken;//MT CHECKED

    /**
     * The maximum level cleared.
     */
    @Nonnull
    private static Cost highWaterMark=Cost.LOWEST;//MT CHECKED

    /**
     * The high water mark time
     */
    private static long highWaterMarkTime=System.currentTimeMillis();//MT CHECKED

    /**
     * The last time memory was cleared.
     */
    private static final long LAST_CLEARED[];//MT CHECKED

    /**
     * The last time we check the idle time of handles
     */
    private static long lastCheckedIdle;//MT CHECKED

    private static final long MAX_IDLE_TIME[];//MT CHECKED

    private static final String THREAD_NAME_IDLE="Memory Manager: idle";

    /**
     * The default max heap is 64m if we want the MAX to be the
     * maximum memory size ( If I could get this from the command line
     * it would be great). PS. In JDK1.4 & JDK1.4.1 getMaxMemory() doesn't work.
     */
    private static long maxMemory;
    
    /**
     * G1 segments are a maximum of 32 megs.
     */
    private static final int PADDING_MAX_SEGMENT=24*1024*1024;

    /** 
     * The maximum padding size.
     */
    private static final long MAX_PADDING_SIZE=PADDING_MAX_SEGMENT * 100;

    /** the size of the rainy day fund */
    private static final int RAINY_DAY_SIZE=512 * 1024;

    /** Was it defined on the command line ? if so don't change it */
    private static boolean      maxDefined;//MT CHECKED

    /** The MemoryHandlers at each level */
    private static final WeakHashMap<MemoryHandler, String>[] LEVELS = new WeakHashMap[ MemoryHandler.Cost.values().length];

    /**
     * The register lock
     */
    private static final ReadWriteLock REGISTER_LOCK = new ReentrantReadWriteLock();

    /** A list of objects to be notified when the memory has been cleared */
    private static final CopyOnWriteArrayList<WeakReference<MemoryListener>> LISTENERS = new CopyOnWriteArrayList<>();

    /** Gives us some room if we are clearing up. */
    @SuppressWarnings("VolatileArrayField")
    private static volatile byte[] rainyDayFund;//MT CHECKED
    /** weak handle to the padding of the memory */
    private static final WeakReference WEAK_PADDING[];
    /** hard handle to the padding of the memory */
    private static final byte HARD_PADDING[][];
    private static final Object GC_WATCH[];

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.memory.MemoryManager");//#LOGGER-NOPMD

    /**
     * The last time GC was called.
     */
    private static long lastGCtime;//MT CHECKED

    /**
     * The MAX time GC took.
     */
    private static long maxGCperiod;//MT CHECKED

    private static final boolean DISABLE_GC;

    private static final MemoryManagerRunner RUNNER;
    
    /**
     * The reserve percentage.
     */
    private static final int RESERVE_PERCENT;
    public static final Collector COLLECTOR;
    
    private static final ConcurrentDecimalFormat PF=new ConcurrentDecimalFormat("0.#%");
    
    /**
     * http://blog.takipi.com/garbage-collectors-serial-vs-parallel-vs-cms-vs-the-g1-and-whats-new-in-java-8/
     */
    @SuppressWarnings("PublicInnerClass")
    public static enum Collector{
        SERIAL("Serial", "Tenured Gen",50, "", "",0),
        PS("Parallel scavenge mark-sweep", "PS Old Gen",50,"", "",0),
        CMS("Concurrent mark-sweep", "CMS Old Gen", 60, "CMSInitiatingOccupancyFraction", "",0),
        G1("Garbage first", "G1 Old Gen", 45, "InitiatingHeapOccupancyPercent", "G1ReservePercent",10);
        
        /**
         * The collector name.
         */
        public final String collectorName;
        
        /**
         * The old generation pool name. 
         */
        public final String oldGenerationPoolName;
        
        /**
         * The default initiating faction of the heap. 
         */
        public final int defaultInitiatingOccupancyFaction;
        
        public final String initiatingHeapOccupancyPercentArgument;
        
        /** 
         * The command line argument to define the reserve percent. 
         */
        public final String reservePercentArgument;
        /**
         * G1 has a reserve percent of 10 by default.
         */
        public final int defaultResercePercent;
        
        private Collector( 
            final String collectorName, 
            final String oldGenerationName,
            final int defaultInitiatingOccupancyFaction,
            final String initiatingHeapOccupancyPercentArgumentPattern,
            final String reservePercentArgument,
            final int defaultResercePercent
        )
        {
            this.collectorName=collectorName;
            this.oldGenerationPoolName=oldGenerationName;
            this.defaultInitiatingOccupancyFaction=defaultInitiatingOccupancyFaction;
            this.initiatingHeapOccupancyPercentArgument=initiatingHeapOccupancyPercentArgumentPattern;
            this.reservePercentArgument=reservePercentArgument;
            this.defaultResercePercent=defaultResercePercent;
        }
    }
    
    /**
     * The initiating faction set from command line.
     */
    public static final int OVERRIDE_OCCUPANCY_FRACTION;

    private static final MemoryPoolMXBean TENURED_GENERATION_POOL;
    private static final GarbageCollectorMXBean TENURED_GENERATION_GARBAGE_COLLECTOR;
    private static final MemoryMXBean MEMORY_MX_BEAN;
    private static long lastTenuredThreadholdCount;//MT CHECKED
    private static long lastLowGCCount;//MT CHECKED
    private static long lastLowClearTime;//MT CHECKED
    private static long lastMediumGCCount;//MT CHECKED
    private static long lastMediumClearTime;//MT CHECKED
    private static long lastPanicGCCount=-1;//MT CHECKED
    
    private static final long MIN_INCREMENTAL_CLEAR_TIME;
    
    /**
     * the min incremental clear time environment variable
     */
    public static final String ENV_MIN_INCREMENTAL_CLEAR_TIME="MIN_INCREMENTAL_CLEAR_TIME";

    /**
     * The padding margin
     */
    public static final String ENV_PADDING_MARGIN="PADDING_MARGIN";
    private static final int PADDING_MARGIN;

    /**
     * The padding max growth percent
     */
    public static final String ENV_PADDING_MAX_GROWTH_PERCENT="PADDING_MAX_GROWTH_PERCENT";
    private static final int PADDING_MAX_GROWTH_PERCENT;

    /**
     * The minimum tenured percentage
     */
    private static int tenuredMinPercent=50;

    private static long tenuredMemorySize = -1;//MT CHECKED

    private static final SyncBlock LOCK=new SyncBlock( "Memory Manager class lock");
    private static final SyncBlock GC_LOCK=new SyncBlock( "Memory Manager GC lock");

    /**
     * Is the memory full ?
     *
     * @return True if with the upper limit
     */
    @CheckReturnValue
    public static boolean isFull()
    {
        long used= getTenuredUsed();

        long upperThreadhold = calculatedTenuredThreshold();

        return used >= upperThreadhold;
    }

    /**
     * Register a Memory Handler. The Handler will be call
     * to free up memory once a day or when low on memory
     *
     * @param handler The memory handler to watch.
     */
    public static void register( final @Nonnull MemoryHandler handler)
    {
        int pos = handler.getCost().level;

        WeakHashMap<MemoryHandler, String> map=LEVELS[pos];
        Lock wl=REGISTER_LOCK.writeLock();
        wl.lock();
        try
        {
            map.put( handler, "");
        }
        finally
        {
            wl.unlock();
        }
    }

    /**
     * Register a Memory listener. The listener will be call
     * after memory has been cleared and the low memory situation has been resolved.
     * <br><br>
     * The listener will be held as a weak reference.
     *
     * @param listener The memory listener.
     */
    public static void addListener( final @Nonnull MemoryListener listener)
    {
        for( WeakReference<MemoryListener> r: LISTENERS)
        {
            MemoryListener compareListener = r.get();
            if( compareListener==null)
            {
                LISTENERS.remove(r);
            }
            else if( compareListener==listener)
            {
                return;
            }
        }

        WeakReference<MemoryListener> wr = new WeakReference( listener);

        LISTENERS.add(wr);
    }

    /**
     * Removes the memory handler from the watch list.
     *
     * @param handler The memory handler to remove
     */
    public static void deregister( final @Nonnull MemoryHandler handler)
    {
        int level = handler.getCost().level;
        WeakHashMap<MemoryHandler, String> map = LEVELS[ level];

        /**
         * No references for this cost level
         */
        if( map == null )
        {
            assert false: "no memory handler for level " + level;
            return;
        }

        Lock wl=REGISTER_LOCK.writeLock();
        wl.lock();
        try
        {
            map.remove(handler);
        }
        finally
        {
            wl.unlock();
        }
    }

    /**
     * Collect the stats of the memory handlers
     *
     * @return The memory stats
     * @throws java.lang.InterruptedException the request was interrupted.
     */
    @SuppressWarnings("null") @CheckReturnValue @Nonnull
    public static ArrayList collectStats() throws InterruptedException
    {
        ArrayList stats = new ArrayList();
        for (Cost cost: Cost.values())
        {
            MemoryHandler list[]=listHandlers(cost);

            /*
            * Just clear the WeakReferences as when we are clearing memory
            * I copy it from one vector to another ( I may miss something if you do a remove)
            */
            for( MemoryHandler h: list)
            {
                assert h!=null: "null handler";
                if( h == null) continue;

                MemoryStats stat;

//                Cost cost = h.getCost();
                long size = h.getEstimatedSize();
                String name = h.toString();
                int totalCount = -1;
                int hardCount = -1;

                long lastAccessed = 0;

                if( h instanceof CacheTableTemplate)
                {
                    CacheTableTemplate t = (CacheTableTemplate)h;

                    name = t.getDescription();
                    hardCount = t.getHardLinkCount();
                    totalCount = t.size();

                    lastAccessed = t.getLastAccessed();
                }
                else if( h instanceof MemoryHandlerGroup)
                {
                    MemoryHandlerGroup g = (MemoryHandlerGroup)h;

                    name = g.getDescription();
                    hardCount = g.getHardLinkCount();
                    totalCount = g.size();

                    lastAccessed = g.getLastAccessed();
                }

                stat = new MemoryStats(name, cost, size, totalCount, hardCount, lastAccessed);

                stats.add( stat);
            }
        }

        return stats;
    }

    /**
     * To prevent a deadlock found on 25 Oct 2003 between freeMemory() and deregister()
     *
     * @param handler The memory handler to call
     * @param percentage The required amount to be freed
     * @return The estimated number of bytes freed.
     */
    public static long callFreeMemory( final @Nonnull MemoryHandler handler, final @Nonnegative double percentage)
    {
        assert percentage>0 && percentage<=1: "invalid percentage " + percentage;
        LOCK.take();
        try
        {
            return handler.queuedFreeMemory( percentage);
        }
        finally
        {
            LOCK.release();
        }
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
     * @param cost Clear memory to this cost level
     * @return The amount cleared
     */
    @Nonnegative
    public static long clearMemory(final @Nonnull Cost cost)
    {
        assert cost!=null;
        long cleared = 0;

        try
        {
            cleared = iClearMemory( cost);
        }
        catch( InterruptedException ie)
        {
            String msg="MemoryManager.clearMemory(" + cost + ")";
            lastError(msg, ie);
            LOGGER.error( msg,ie);
            throw CLogger.rethrowRuntimeExcepton(ie);
        }
        assert cleared >=0: "cleared memory must be NON negative: " + cleared;
        return cleared;
    }

    @SuppressWarnings("null")
    private static long iClearMemory(final @Nonnull Cost cost) throws InterruptedException
    {
        /* Clear rainy day fund to give us some space to work   */
        releaseRainyDayFund();

        long        oTotal,
                    cleared = 0,
                    oFree;

        oTotal = getTotalMemory();
        oFree  = getFreeMemory();

        long used = oTotal - oFree;
        long max = getTotalMemory();
        if( oTotal < max)
        {
            oFree += max - oTotal;
        }

        StopWatch sw=new StopWatch(true);

        for( int i = 0; i <= cost.level && i < LEVELS.length; i++)
        {
            Cost clearCost=Cost.find(i);
            MemoryHandler list[]=listHandlers(clearCost);

            for( MemoryHandler h: list)
            {
                assert h!=null: "null handler";
                if( h == null) continue;

                if( clearCost == MemoryHandler.Cost.PANIC)
                {
                    cleared += h.panicFreeMemory();
                }
                else
                {
                    /* The freeMemory routine should be fast!!! */
                    cleared += h.queuedFreeMemory(1);
                }
            }

            lastClearedLevel = clearCost;
            LAST_CLEARED[i] = System.currentTimeMillis();
        }

        if( cost.level >= highWaterMark.level)
        {
            highWaterMark = cost;
            highWaterMarkTime = System.currentTimeMillis();
        }

        long actualCleared;
        actualCleared = showDetails(sw, used,null, cost, LOGGER);

        if( actualCleared > cleared)
        {
            cleared = actualCleared;
        }

        callListeners( cost);
        sw.stop();
        long diff = sw.durationMS();
        totalClearedTimeTaken += diff;

        return cleared;
    }

    /**
     * Get the JVM max memory ( when will we get a out of memory error)
     *
     * @return the "real" JVM max memory
     */
    @CheckReturnValue
    public static long jvmMaxMemory()
    {
        Runtime rt = Runtime.getRuntime();
        long max;
        max = rt.maxMemory();

        return max;
    }

    /**
     * Manually set the maximum memory to use.
     *
     * @param maximum The max memory.
     */
    public static void setMaxMemory( final @Nonnegative long maximum)
    {
        if( maximum <=0) throw new IllegalArgumentException("max memory must be positive was: " + maximum);
        
        long tempMax = maximum;

        long max;
        max = jvmMaxMemory();

        if( max < tempMax || maximum < 64 * 1024 * 1024)
        {
            LOGGER.warn( "Adjusting MAX MEMORY " + NumUtil.convertMemoryToHumanReadable( tempMax) + " -> " + NumUtil.convertMemoryToHumanReadable( max));
            tempMax = max;
        }

        maxMemory = tempMax;

        maxDefined = true;
    }

    /**
     * Manually set the maximum memory to use.
     *
     * @param maximum The max memory.
     */
    public static void setMaxMemory( final @Nullable String maximum)
    {
        if( StringUtilities.isBlank(maximum)) return;

        long tempMax = MemoryUtil.parseSize( maximum);
        setMaxMemory( tempMax);
    }

    /**
     * Used to set the safe zone parameter from system properties
     *
     * @param percentage The safe zone lower amount
     */
    public static void setSafeZoneLower(final @Nullable String percentage)
    {
        if( StringUtilities.isBlank( percentage) == false)
        {
            assert percentage!=null;
            String zone = percentage.trim();
            try
            {
                safeZoneLower = Integer.parseInt(zone);
            }
            catch( NumberFormatException nf)
            {
                LOGGER.error( "Setting SAFE_ZONE_LOWER='" + zone + "'", nf);
            }
        }
        else
        {
            safeZoneLower=SAFE_ZONE_LOWER_DEFAULT;
        }

        if( safeZoneLower <= 0)
        {
            LOGGER.error( "Can't set SAFE_ZONE_LOWER to zero or less '" + percentage + "'");
            safeZoneLower=SAFE_ZONE_LOWER_DEFAULT;
        }
        if( safeZoneLower > 90)
        {
            LOGGER.error( "Can't set SAFE_ZONE_LOWER to more than 90 '" + percentage + "'");
            safeZoneLower=SAFE_ZONE_LOWER_DEFAULT;
        }

        LOGGER.info("MemoryManager.SAFE_ZONE_LOWER= " + safeZoneLower + "%");
    }

    /**
     * Used to set the min tenured percentage from system properties.
     * 
     * The tenured percentage is a percent of the max memory.
     *
     * @param percentage The tenured percentage
     */
    public static void setTenuredPercent(final @Nullable String percentage)
    {
        if( StringUtilities.isBlank( percentage) == false)
        {
            assert percentage!=null;
            String temp = percentage.trim();
            try
            {
                int percent = Integer.parseInt(temp);
                if( percent < 5 || percent > 95)
                {
                    LOGGER.error("Tenured percentage out of range 5..95 " + percent );
                    tenuredMinPercent=COLLECTOR.defaultInitiatingOccupancyFaction;
                }
                else
                {
                    tenuredMinPercent=percent;
                }
            }
            catch( NumberFormatException nf)
            {
                LOGGER.error( "Setting " + TENURED_PERCENT + " ='" + temp + "'", nf);
            }

            if( tenuredMinPercent > OVERRIDE_OCCUPANCY_FRACTION && OVERRIDE_OCCUPANCY_FRACTION > 0)
            {
                LOGGER.info("MemoryManager." + TENURED_PERCENT + "= " +
                    tenuredMinPercent + "% is greater than " +
                    COLLECTOR.initiatingHeapOccupancyPercentArgument + " of " +
                    OVERRIDE_OCCUPANCY_FRACTION + "% which will cause GC to run constantly"
                );
            }
        }
        else
        {
            if( OVERRIDE_OCCUPANCY_FRACTION > 5 )
            {
                tenuredMinPercent=OVERRIDE_OCCUPANCY_FRACTION;
            }
            else
            {
                tenuredMinPercent=COLLECTOR.defaultInitiatingOccupancyFaction;
            }
        }

        assert tenuredMinPercent>=5 && tenuredMinPercent<=95: "Tenured percentage out of range 5..95 was: " + tenuredMinPercent;
        LOGGER.info("MemoryManager." + TENURED_PERCENT + "= " + tenuredMinPercent + "%");
    }

    /**
     * What is the safe zone lower currently set to ?
     *
     * @return The safe zone lower amount
     */
    @CheckReturnValue @Nonnegative
    public static int getSafeZoneLower()
    {
        assert safeZoneLower>=0 && safeZoneLower<=90: "Safe Zone should be a percentage was: " + safeZoneLower;
        return safeZoneLower;
    }

    /**
     * Used to set the safe zone parameter from system properties
     *
     * @param percentage The minimum amount to free.
     */
    public static void setMinFreePercent(final @Nullable String percentage)
    {
        if( StringUtilities.isBlank( percentage) == false)
        {
            assert percentage!=null;
            String zone = percentage.trim();
            try
            {
                minFreePercent = Integer.parseInt(zone);
            }
            catch( NumberFormatException nf)
            {
                LOGGER.error( "Setting MIN_FREE_PERCENT='" + zone + "'", nf);
            }
        }
        else
        {
            minFreePercent=MIN_FREE_PERCENT_DEFAULT;
        }

        if( minFreePercent <= 0)
        {
            LOGGER.error( "Can't set MIN_FREE_PERCENT to zero or less '" + percentage + "'");
            minFreePercent=MIN_FREE_PERCENT_DEFAULT;
        }

        LOGGER.info("MemoryManager.MIN_FREE_PERCENT= " + minFreePercent + "%");
    }

    /**
     * what is the minimum percentage to free if we detected that we are below the safe zone limit ?
     *
     * @return The minimum amount to free.
     */
//    @CheckReturnValue @Nonnegative
//    public static int getMinFreePercent()
//    {
//        return minFreePercent;
//    }

    /**
     * Used to set the safe zone parameter from system properties
     *
     * @param percentage The safe zone upper limit
     */
    public static void setSafeZoneUpper(final @Nullable String percentage)
    {
        if( StringUtilities.isBlank( percentage) == false)
        {
            assert percentage!=null;
            String zone = percentage.trim();
            try
            {
                safeZoneUpper = Integer.parseInt(zone);
            }
            catch( NumberFormatException nf)
            {
                LOGGER.error( "Setting SAFE_ZONE_UPPER='" + zone + "'", nf);
            }
        }
        else
        {
            safeZoneUpper=SAFE_ZONE_UPPER_DEFAULT;
        }

        if( safeZoneUpper <= 0)
        {
            LOGGER.error( "Can't set SAFE_ZONE_UPPER to zero or less '" + percentage + "'");
            safeZoneUpper=SAFE_ZONE_UPPER_DEFAULT;
        }
        if( safeZoneUpper >90)
        {
            LOGGER.error( "Can't set SAFE_ZONE_UPPER to 90 or more '" + percentage + "'");
            safeZoneUpper=SAFE_ZONE_UPPER_DEFAULT;
        }
        
        if( safeZoneUpper < safeZoneLower)
        {
            LOGGER.warn("SAFE_ZONE_UPPER (" + safeZoneUpper + "%) < SAFE_ZONE_UPPER (" + safeZoneLower + "%)" );
        }

        LOGGER.info("MemoryManager.SAFE_ZONE_UPPER= " + safeZoneUpper + "%");
    }

    /**
     * At what percentage free will we say that we are full ?
     *
     * @return The safe zone upper limit
     */
    @CheckReturnValue @Nonnegative
    public static int getSafeZoneUpper()
    {
        assert safeZoneUpper>=0&& safeZoneUpper<=90: "safe zone upper should be in range was: " + safeZoneUpper;
        return safeZoneUpper;
    }

    /**
     * Number of times GC has been called.
     *
     * @return The number of times GC has been called.
     */
    @CheckReturnValue @Nonnegative
    public static long getCountGC()
    {
        long count= TENURED_GENERATION_GARBAGE_COLLECTOR.getCollectionCount();
        assert count>=0;
        return count;
    }

    /**
     * Total GC time.
     *
     * @return The total time we have spent in GC
     */
    @CheckReturnValue
    public static long getGCtime()
    {
        return TENURED_GENERATION_GARBAGE_COLLECTOR.getCollectionTime();
    }

    /**
     * The total memory limited to the MAX_MEMORY defined by the command line.
     * if MAX_MEMORY was not set and the total memory is greater than the MAX_MEMORY then
     * set MAX_MEMORY
     *
     * @return The total number of bytes used.
     */
    @CheckReturnValue @Nonnegative
    public static long getTotalMemory()
    {
        MemoryUsage heapMemoryUsage = getHeapMemoryUsage();
        long max= getTotalMemory(heapMemoryUsage);
        assert max>0:"max must be positive: " + max;
        return max;
    }
    
    @CheckReturnValue @Nonnegative
    private static long getTotalMemory(final MemoryUsage heapMemoryUsage)
    {
        if( maxDefined)
        {
            return maxMemory;
        }
        
        long max = heapMemoryUsage.getMax();
        assert max>0:"max must be positive: " + max;
        long committed = heapMemoryUsage.getCommitted();
        assert max>=committed:"committed: " + NumUtil.convertMemoryToHumanReadable(committed) +" must not above max: " + NumUtil.convertMemoryToHumanReadable(max);
        
        if( committed > max) return committed;

        return max;
    }

    @CheckReturnValue @Nonnull
    @SuppressWarnings("CallToThreadYield")
    private static MemoryUsage getHeapMemoryUsage()
    {
        for( int attempts=1;true;attempts++)
        {
            MemoryUsage heapMemoryUsage=null;
            try{
                heapMemoryUsage = MEMORY_MX_BEAN.getHeapMemoryUsage();
                long max=heapMemoryUsage.getMax();
                long committed=heapMemoryUsage.getCommitted();
                if( max < committed)
                {
                    throw new IllegalArgumentException( 
                        "max: " + NumUtil.convertMemoryToHumanReadable(max) + 
                        " committed: " + NumUtil.convertMemoryToHumanReadable(committed)
                    );
                }
                return heapMemoryUsage;
            }
            catch( IllegalArgumentException iae)
            {                    
                releaseRainyDayFund();
                Thread.yield();
                if( attempts > 3)
                {
                    String msg="Can not get heap memory usage. giving up";
                    lastError(msg, iae);
                    LOGGER.error( msg, iae);                    
                    assert false: msg;
                    if( heapMemoryUsage==null)
                    {
                        throw iae;
                    }
                    return heapMemoryUsage;
                }
                else
                {
                    LOGGER.warn( "can not get heap memory usage. retying... " + attempts, iae);       
                    Thread.yield();
                }
            }
        }               
    }
    
    /**
     * The percentage free.
     *
     * @return 0-1 percent free.
     */
    @CheckReturnValue @Nonnegative
    public static float getFreePercent()
    {
        long        max,
                    free;
        MemoryUsage heapMemoryUsage = getHeapMemoryUsage();
        max = getTotalMemory(heapMemoryUsage);
        free  = getFreeMemory(heapMemoryUsage);

        float percent = (float)free / (float)max;
                
        assert percent >=0 && percent <=1: "percentage out of range was: " + PF.format(percent);

        if( percent > 1) percent=1;
        
        return percent;
    }

    /**
     * The tenured free memory in bytes.
     *
     * @return total amount of free tenured memory
     */
    @CheckReturnValue @Nonnegative
    public static long getTenuredFreeMemory()
    {
        MemoryUsage usage = getMemoryUsage(TENURED_GENERATION_POOL);
        
        return getTenuredFreeMemory( usage);
    }

    /**
     * The tenured free memory in bytes.
     *
     * @return total amount of free tenured memory
     */
    @CheckReturnValue @Nonnegative
    private static long getTenuredFreeMemory(final @Nonnull MemoryUsage usage)
    {
        long tenuredSize= getTenuredTotalMemory(usage);
        long tenuredUsed= getUsed(usage);
        long tenuredFree= tenuredSize-tenuredUsed;

        assert tenuredFree>=0;
        if ( tenuredFree < 0) tenuredFree = 0;
        
        return tenuredFree;
    }

    /**
     * The tenured percentage free.
     *
     * @return 0-1 percent free.
     */
    @CheckReturnValue @Nonnegative
    public static float getTenuredFreePercent()
    {
        MemoryUsage usage = getMemoryUsage(TENURED_GENERATION_POOL);
        
        return getTenuredFreePercent( usage);
    }

    @CheckReturnValue @Nonnegative
    private static float getTenuredFreePercent( final @Nonnull MemoryUsage usage)
    {
        assert usage!=null;

        long tenuredSize= getTenuredTotalMemory(usage);

        long tenuredFree= getTenuredFreeMemory(usage);

        float percent = (float)tenuredFree/(float)tenuredSize;

        assert percent >=0 && percent <=1: "percent out of range: " + PF.format(percent);
        
        if( percent>1) percent=1;
                
        return percent;
    }

    /**
     * The upper safety zone as a percentage 0-1
     *
     * @return The percentage
     */
    @CheckReturnValue @Nonnegative
    public static float getSafeUpperPercent()
    {
        float percent= (float)safeZoneUpper/(float)100;
        
        assert percent >=0 && percent <=1: "safe upper percentage out of range was: " + PF.format(percent);

        return percent;
    }

    /**
     * The free Memory, if we have defined the MAX_MEMORY to be more than what is
     * currently reserved then add this to the free memory. If the reserved is more than the
     * amount defined on the command line ignore the extra amount. For sensibility reasons
     * never return a negative amount.
     *
     * @return the free memory
     */
    @CheckReturnValue @Nonnegative
    public static long getFreeMemory()
    {
        MemoryUsage heapMemoryUsage = getHeapMemoryUsage();
        
        return getFreeMemory(heapMemoryUsage);
    }
    
    @CheckReturnValue @Nonnegative
    private static long getFreeMemory(final @Nonnull MemoryUsage heapMemoryUsage)
    {        
        long max = heapMemoryUsage.getMax();
        long used = heapMemoryUsage.getUsed();
        
        long free=max-used;

        assert free<=max:"free (" + free + ") more than max (" + max + ")";
        if( free > max) free = max;
        
        assert free>=0:"free amount must be non negative was " + free;
        if( free <0)
        {
            free=0;
        }
        return free;
    }

    /**
     * the size of the memory padding
     * @return the size in bytes
     */
    @CheckReturnValue @Nonnegative
    public static long getPaddingMemory()
    {
        long padding = 0;
        synchronized( WEAK_PADDING)
        {
            for( int i=0; i < WEAK_PADDING.length;i++)
            {
                WeakReference ref = WEAK_PADDING[i];

                if( ref != null)
                {
                    Object data = ref.get();

                    if( data instanceof byte[])
                    {
                        byte[] array = (byte[])data;
                        padding += array.length;
                    }
                    else
                    {
                        WEAK_PADDING[i] = null;
                    }
                }
            }
        }

        assert padding>=0: "padding must not be negative: " + padding;
        return padding;
    }

    /**
     * Call System.gc() regardless of when it has been called last.
     * @return Was it called ?
     */
    public static boolean gc()//NOPMD
    {
        return gc( 0);
    }

    /**
     * Only call System.gc() if it has NOT been called in the last n seconds.
     * @param intervalSeconds Call system.gc() if not called within the last X seconds.
     * @return Was it called ?
     */
    public static boolean gc( final @Nonnegative int intervalSeconds)//NOPMD
    {
        String logMessage[] = new String[1];
        boolean flag = callGC( intervalSeconds, logMessage);

        if( logMessage[0] != null)
        {
            LOGGER.warn( logMessage[0]);
        }

        return flag;
    }

    /**
     * Only call System.gc() if it has NOT been called in the last n seconds.
     *
     * @param intervalSeconds Call system.gc() if not called within the last X seconds.
     * @param logMsg The message.
     * @return Was it called ?
     */
    @SuppressWarnings({"CallToNativeMethodWhileLocked", "CallToThreadYield"})
    public static boolean callGC( final int intervalSeconds, final String logMsg[])
    {
        GC_LOCK.take();
        try
        {
            @SuppressWarnings({"MismatchedReadAndWriteOfArray", "UnusedAssignment"})
            String messageHandle[] = logMsg;

            long start = System.currentTimeMillis();

            /**
             * -1 for a interval Seconds means DISABLED
             */
            if(
                intervalSeconds >= 0 &&
                (
                    intervalSeconds == 0 ||
                    lastGCtime + (intervalSeconds * 1000) < start
                )
            )
            {
                if( DISABLE_GC)//#NOPMD
                {
                    messageHandle[0] = "MemoryManager: DISABLED System.gc()";
                    lastGCtime = System.currentTimeMillis();

                    // Give the incremental GC sometime.
                    Thread.yield();
                }
                else
                {
                    messageHandle[0] = "MemoryManager: calling System.gc()";

                    long maxWaitTime;
                    long used = getTotalUsed();

                    if( used > 2 * 1024 * 1024 * 1024)
                    {
                        maxWaitTime = 30 * 1000;
                    }
                    else
                    {
                        maxWaitTime = 10 * 1000;
                    }

                    WeakReference wf=null;
                    ReferenceQueue q = new ReferenceQueue();
                    boolean nullFound = false;
                    for( int i = 0; i <= GC_WATCH.length;i++)
                    {
                        int pos = i == GC_WATCH.length ?  0: i;
                        Object obj = GC_WATCH[pos];
                        if( obj == null)
                        {
                            nullFound=true;
                        }
                        else if( nullFound )
                        {
                            wf= new WeakReference( obj, q);
                            GC_WATCH[pos]=null;
                            int previousPos = i-1;

                            GC_WATCH[previousPos]=new Object();
                            break;
                        }
                    }

                    if( wf == null)
                    {
                        throw new RuntimeException("ERROR: Tail ment head");
                    }

                    System.gc();
                    try
                    {
                        q.remove( maxWaitTime);
                    }
                    catch( InterruptedException ie)
                    {
                        LOGGER.info( "interrupted", ie);
                        Thread.currentThread().interrupt();
                    }

                    long end;
                    long period;

                    end = System.currentTimeMillis();
                    period = end-start;

                    String msg;
                    if( wf.get() != null)
                    {
                        msg = "background GC DID NOT complete " + TimeUtil.getDiff(start, end);
                    }
                    else
                    {
                        msg="background GC complete " + TimeUtil.getDiff(start, end);
                    }

                    if( period > maxGCperiod)
                    {
                        maxGCperiod = period;
                    }

                    messageHandle[0] = msg;

                    lastGCtime = end;

                    return true;
                }
            }
            else
            {
                // Give the incremental GC sometime.
                Thread.yield();
            }

            return false;
        }
        finally
        {
            GC_LOCK.release();
        }
    }

    /**
     * The amount of memory used
     *
     * @return The number of bytes used.
     */
    @CheckReturnValue @Nonnegative
    public static long getTotalUsed()
    {
        MemoryUsage heapMemoryUsage = getHeapMemoryUsage();

        return getUsed(heapMemoryUsage);
    }

    /**
     * The amount of tenured memory used
     *
     * @return The number of bytes used.
     */
    @CheckReturnValue @Nonnegative
    public static long getTenuredUsed()
    {
        MemoryUsage usage = getMemoryUsage(TENURED_GENERATION_POOL);

        return getUsed( usage);
    }
    
    /**
     * https://bugs.openjdk.java.net/browse/JDK-8154458
     */
    @CheckReturnValue @Nonnull
    @SuppressWarnings("CallToThreadYield")
    private static MemoryUsage getMemoryUsage( final @Nonnull MemoryPoolMXBean mp)
    {
        MemoryUsage usage;
        try
        {
            usage=mp.getUsage();
        }
        catch( InternalError ie)
        {
            Thread.yield();
            LOGGER.warn( "Could not get the usage", ie);
            usage=mp.getUsage();
        }
        
        return usage;
    }
    
    @CheckReturnValue @Nonnegative
    private static long getUsed(final @Nonnull MemoryUsage usage)
    {
        assert usage!=null;
        long used = usage.getUsed();
        assert used>=0: "Used memory must be non negative: " + NumUtil.convertMemoryToHumanReadable(used);

        if( used <0) return 0;
        
        return used;
    }
    /**
     * Records the last time memory was checked (once a second or so)
     * @return the last tick timestamp.
     */
    @CheckReturnValue @Nonnegative
    public static long lastTick()
    {
        return LAST_TICK.get();
    }
    
    /**
     * Records the last time memory was checked in seconds
     * @return the current timestamp.
     */
    @Nonnegative
    public static long tick()
    {
        long now=System.currentTimeMillis();
        LAST_TICK.set(now);
        
        return now;
    }

    /**
     * The last level cleared
     *
     * @return The last level cleared.
     */
    @CheckReturnValue @Nonnull
    public static Cost getLastClearedLevel()
    {
        assert lastClearedLevel!=null;
        return lastClearedLevel;
    }

    /**
     * How many times have the memory been cleared.
     *
     * @return The number of times.
     */
    @CheckReturnValue @Nonnegative
    public static int getTotalClearedCount()
    {
        assert totalClearedCount>=0: "total cleared count must be non negative: " + totalClearedCount;
        return totalClearedCount;
    }

    /**
     * how much time has been taken clearing memory.
     *
     * @return The number of times.
     */
    @CheckReturnValue @Nonnegative
    public static long getTotalClearedTimeTaken()
    {
        assert totalClearedTimeTaken>=0: "total cleared time taken must be non negative: " + totalClearedTimeTaken;
        return totalClearedTimeTaken;
    }

    /**
     * The maximum level cleared.
     *
     * @return The maximum level cleared.
     */
    @CheckReturnValue @Nonnull
    public static Cost getHighWaterMark()
    {
        assert highWaterMark!=null;
        return highWaterMark;
    }

    /**
     * Last time the maximum level was cleared.
     *
     * @return The maximum level cleared.
     */
    @CheckReturnValue @Nonnull
    public static Date getHighWaterMarkTime()
    {
        return new Date( highWaterMarkTime);       
    }

    /**
     * The last time memory was cleared.
     *
     * @return The system time last cleared.
     */
    @CheckReturnValue @Nonnegative
    public static long getLastClearedTime()
    {
        return LAST_CLEARED[lastClearedLevel.level];
    }
    
    /**
     * Check if there has been an issue. 
     * @throws Exception if there has been an error since the last time this method was called. 
     */
    public static void lastError() throws Exception
    {
        Issue tmpIssue=lastIssue;
        if( tmpIssue!=null)
        {
            lastIssue=null;
            String tmpMessage=tmpIssue.message;
            if( tmpMessage!=null){
                tmpMessage="memory error";
            }
            throw new Exception(tmpMessage, tmpIssue.cause);
        }
    }
    
    /** 
     * Record the an issue which has occurred by an background process. 
     * @param msg the message
     * @param cause the cause.
     */
    public static void lastError( final String msg, final Throwable cause)
    {
        lastIssue=new Issue( msg, cause);
    }
    
    /**
     * The estimated size of the data held at this cost level
     *
     * @param cost The cost level
     * @return The estimated amount held.
     * @throws java.lang.InterruptedException the request was interrupted.
     */
    @SuppressWarnings("null")
    @CheckReturnValue @Nonnegative
    public static long estimateLevel( final @Nonnull Cost cost) throws InterruptedException
    {
        long estimate=0;
        MemoryHandler list[]=listHandlers(cost);

        for( MemoryHandler h: list)
        {
            assert h!=null: "null handler";
            if( h == null) continue;

            /* The freeMemory routine should be fast!!! */
            estimate += h.getEstimatedSize();
        }
        assert estimate >=0: "estimate memory must be NON negative: " + estimate;
        return estimate;
    }

    /************************************************************************************************/
    /*                                          PRIVATES                                            */
    /************************************************************************************************/

    /**
     * Call the listeners
     */
    private static void callListeners( final @Nonnull Cost cost)
    {
        totalClearedCount++;

        if( LISTENERS.isEmpty()) return;

        ListenerRunner r = new ListenerRunner( cost, LISTENERS);

        ThreadPool.schedule( r);
    }
    
    /**
     * Show the memory usage details.
     *
     * @param start
     * @param oTotal
     * @param oUsed
     * @param msg the type
     * @return the value
     */
    private static long showDetails(
        final StopWatch sw,
        final long oUsed,
        final String msg,
        final Cost cost,
        final Log logger
    )
    {
        StringBuilder sb = new StringBuilder( 200);

        long cleared;
        cleared= showDetails(
            sw,
            oUsed,
            msg,
            cost,
            sb
        );

        logger.info( sb.toString());

        return cleared;
    }

    /**
     * the memory details
     *
     * @return the details
     */
    @CheckReturnValue @Nonnull
    public static String getDetails()
    {
        StringBuilder msg = new StringBuilder( 200);
        showDetails(
            null,
            -1,
            null,
            null,
            msg
        );

        return msg.toString();
    }

    /**
     * Show the memory usage details.
     *
     * @param start
     * @param oTotal
     * @param oUsed
     * @param msg the type
     * @return the value
     */
    private static long showDetails(
        final StopWatch sw,
        final long oUsed,
        final @Nullable String clearMsg,
        final @Nullable Cost cost,
        final StringBuilder sb
    )
    {
        long    max,
                free;

        MemoryUsage heapMemoryUsage = getHeapMemoryUsage();
        long paddingSize=getPaddingMemory();
        MemoryUsage tenuredUsage = getMemoryUsage(TENURED_GENERATION_POOL);

        max = getTotalMemory(heapMemoryUsage);
        free  = getFreeMemory(heapMemoryUsage);

        long reserved = Runtime.getRuntime().totalMemory();
        long used = getUsed(heapMemoryUsage);
        final String leftPadding= "                 ";
        sb.append( "Memory Manager\n");
        sb.append( "--------------\n");
        if( clearMsg!=null)
        {
            String tmpMsg=clearMsg.trim();
            int pos = tmpMsg.indexOf(":");
            String left=tmpMsg;
            String right="";
            if( pos!=-1)
            {
                left="    " + tmpMsg.substring(0, pos +1);
                right=tmpMsg.substring(pos +1).trim();
            }
            
            sb.append(left);
            if( left.length()<leftPadding.length())
            {
                sb.append(leftPadding.substring(0, leftPadding.length()-left.length()));
            }
            boolean first=true;
            for( String item:right.split(", "))
            {
                if( first==false)
                {
                    sb.append(leftPadding);
                }
                else
                {
                    first=false;
                }
                sb.append(item);
                sb.append("\n");
            }
        }
        if( cost !=null)
        {
            sb.append( "    Cost:        ");
            sb.append( cost.label);
            sb.append( " (");
            sb.append( cost.level);
            sb.append( ")\n");
        }

        if( max != reserved)
        {
            String label =              "Max:         ";
            if( max < reserved) label = "MAX MEMORY:  ";
            sb.append("    ").append( label);
            sb.append( NumUtil.convertMemoryToHumanReadable(max));
            sb.append( " ( reserved ");
            sb.append( NumUtil.convertMemoryToHumanReadable(reserved));
            sb.append( " )");
        }
        else
        {
            sb.append( "    Max:         ");
            sb.append( NumUtil.convertMemoryToHumanReadable(max));
        }

        sb.append( "\n");
        sb.append( "    Used:        ");
        sb.append( NumUtil.convertMemoryToHumanReadable(used));
        sb.append( "\n");
        sb.append( "    Free:        ");
        sb.append( NumUtil.convertMemoryToHumanReadable(free));
        sb.append( "( ");
        sb.append(PF.format((double)free / (double)max));
        sb.append( " )\n");
        sb.append( "\n");
        
        long tenuredSize=getTenuredTotalMemory(tenuredUsage);

        sb.append( "    Tenured Size:");
        sb.append( NumUtil.convertMemoryToHumanReadable(tenuredSize));
        sb.append( "\n");

        long tenuredUsed=getUsed(tenuredUsage);
        int tenuredUsedPercent =(int) (100.0 - 100.0* getTenuredFreePercent(tenuredUsage));
        sb.append( "    Tenured Used:");
        sb.append( NumUtil.convertMemoryToHumanReadable(tenuredUsed));
        sb.append( " ( ");
        sb.append( tenuredUsedPercent);
        sb.append( "% )");
        sb.append( "\n");
        
        if( paddingSize>0){
            sb.append( "    Padding:     ");
            sb.append( NumUtil.convertMemoryToHumanReadable(paddingSize));
            sb.append( "\n");
        }
        long currentThreadhold = TENURED_GENERATION_POOL.getCollectionUsageThreshold();

        sb.append( "    Threshold:   ");

        sb.append( NumUtil.convertMemoryToHumanReadable(currentThreadhold));
        long cleared = -1;

        if( oUsed > used)
        {
            cleared = oUsed - used;

            sb.append( "\n");
            sb.append( "    Org Used:    ");
            sb.append( NumUtil.convertMemoryToHumanReadable(oUsed));
            sb.append( "\n");

            sb.append( "    Cleared:     ");
            sb.append( NumUtil.convertMemoryToHumanReadable( cleared));
            sb.append( "\n");
        }

        sb.append( "\n");

        if( sw != null)
        {
            sb.append( "    Time:        ");
            sb.append(sw.formatNano(sw.duration()));
            sb.append( "\n");

            sb.append( "    When:        ");
            sb.append(TimeUtil.format("d MMM yyyy H:mm:ss", sw.creationDate(), TimeZone.getDefault()));
            sb.append( "\n");
        }

        int objectPendingFinalizationCount = MEMORY_MX_BEAN.getObjectPendingFinalizationCount();
        if( objectPendingFinalizationCount>0){
            sb.append( "    Pending:     ");
            sb.append( MEMORY_MX_BEAN.getObjectPendingFinalizationCount());
            sb.append( "\n");
        }
        
        return cleared;
    }

    /**
     * Log the Memory Manager details.
     *
     * @param logger The logger to write to.
     */
    public static void logDetails(final @Nonnull Log logger)
    {
        showDetails(
            null,
            -1,
            null,
            null,
            logger
        );
    }

    /**
     * Sets or works out the max memory to use.
     */
    private static void doSetMaxMemory()//NOPMD
    {
        String orgStr;
        orgStr = CProperties.getProperty( "MAX_MEMORY", "");

        if( StringUtilities.isBlank( orgStr) == false)
        {
            try
            {
                setMaxMemory( orgStr);

                LOGGER.info( "MemoryManager:Max MEMORY = " + NumUtil.convertMemoryToHumanReadable( maxMemory));

                return;
            }
            catch( NumberFormatException nf)
            {
                LOGGER.error( "Setting MAX_MEMORY='" + orgStr + "'", nf);
            }
        }

        long max = jvmMaxMemory();

        if( maxDefined == false)
        {
            maxMemory=max;
        }

        LOGGER.info( "MemoryManager:Max MEMORY = " + NumUtil.convertMemoryToHumanReadable( maxMemory) + " calculated");
    }

    /**
     * Manually set the tenured size.
     *
     * @param size The tenured size.
     */
    public static void setTenuredSize( final @Nullable String size)
    {
        if( StringUtilities.isBlank(size)) return;

        long tempSize = MemoryUtil.parseSize( size);
        setTenuredSize( tempSize);
    }

    /**
     * set the tenured size
     * @param size the size ( negative means use the default).
     */
    public static void setTenuredSize( final long size)
    {
        if( size > 0)
        {
            long tempSize=size;

            boolean hasPoolMax=false;
            if( TENURED_GENERATION_POOL != null)
            {
                long poolMax = getMemoryUsage(TENURED_GENERATION_POOL).getMax();
                if( poolMax>0)
                {
                    hasPoolMax=true;
                    if( size > poolMax)
                    {
                        tempSize=poolMax;
                        LOGGER.warn( "Adjusting tenured size " + NumUtil.convertMemoryToHumanReadable(size) + "->" + NumUtil.convertMemoryToHumanReadable(tempSize));
                    }
                }
            }

            if( hasPoolMax == false)
            {
                if( tempSize > getTotalMemory() * 0.8)
                {
                    if( size > getTotalMemory())
                    {
                        setMaxMemory( size);
                    }

                    tempSize=(long)(getTotalMemory() * 0.8);

                    if( tempSize != size)
                    {
                        LOGGER.warn( "Adjusting tenured size " + NumUtil.convertMemoryToHumanReadable(size) + "->" + NumUtil.convertMemoryToHumanReadable(tempSize));
                    }
                }
            }

            if( maxDefined)
            {
                if( tempSize > getTotalMemory())
                {
                    assert tenuredMinPercent>=5 && tenuredMinPercent<=95: "Tenured percentage out of range 5..95 was: " + tenuredMinPercent;

                    setMaxMemory( tempSize/(long)tenuredMinPercent * 100L);
                }
            }

            long tempTenuredThreshold = calculatedTenuredThreshold();
            if( tempTenuredThreshold < tempSize)
            {
                LOGGER.warn("MemoryManager." + TENURED_SIZE + " of " +
                    NumUtil.convertMemoryToHumanReadable(tempSize) + " is greater than " +
                    COLLECTOR.initiatingHeapOccupancyPercentArgument +
                    " threshold of " + NumUtil.convertMemoryToHumanReadable(tempTenuredThreshold) +
                    " which will cause GC to run constantly"
                );
            }

            tenuredMemorySize = tempSize;
        }
        else
        {
            tenuredMemorySize=-1;
        }
    }

    /**
     * the GC threshold
     * @return the threshold in bytes.
     */
    @CheckReturnValue @Nonnegative
    public static long calculatedTenuredThreshold()
    {
        assert tenuredMinPercent>=5 && tenuredMinPercent<=95: "Tenured percentage out of range 5..95 was: " + tenuredMinPercent;

        long percent = tenuredMinPercent;
        
        long tempMax;
        if( COLLECTOR==Collector.G1)
        {
            tempMax= getTotalMemory();
        }
        else
        {
            tempMax= getTenuredTotalMemory();
        }
        long amount = tempMax/100L * percent-(tempMax/100L * RESERVE_PERCENT);
        
        assert amount >0: "required threshold should more than zero " + amount;

        return amount;
    }

    /**
     * get the tenured size
     * @return the size
     */
    @CheckReturnValue @Nonnegative
    public static long getTenuredTotalMemory()
    {
        MemoryUsage usage = getMemoryUsage(TENURED_GENERATION_POOL);

        return getTenuredTotalMemory(usage);
    }
    
    /**
     * get the tenured size
     * @return the size
     */
    @CheckReturnValue @Nonnegative
    private static long getTenuredTotalMemory(final @Nonnull MemoryUsage usage)
    {
        assert usage!=null;
        if( tenuredMemorySize > 0 ) return tenuredMemorySize;

        long        tenuredCommitted;
        tenuredCommitted = usage.getCommitted();
        assert  tenuredCommitted > 0;
        
        long max = getTotalMemory();

        assert tenuredMinPercent>=5 && tenuredMinPercent<=95: "Tenured percentage out of range 5..95 was: " + tenuredMinPercent;

        long tenuredMinSize = max/100L * (long)tenuredMinPercent;
        assert  tenuredMinSize > 0;
        if( tenuredMinSize > tenuredCommitted || tenuredCommitted <= 0)
        {
            return tenuredMinSize;
        }
        else
        {
            return tenuredCommitted;
        }
    }    

    /**
     * Returns the collection usage threshold value of this memory pool in bytes. The default value is zero.
     * @return size in bytes.
     */
    @CheckReturnValue @Nonnegative
    public static long getCollectionUsageThreshold()
    {
        long collectionUsageThreshold=TENURED_GENERATION_POOL.getCollectionUsageThreshold();
        assert collectionUsageThreshold>=0: "collection usage threshold should be non negative: " + collectionUsageThreshold;
        return collectionUsageThreshold;
    }
    
    /**
     * Are we in the safe zone ? if so clean up
     *
     * @return amount of memory released.
     * @throws java.lang.InterruptedException request interrupted.
     */
    @SuppressWarnings( "CallToThreadYield")
    @Nonnegative
    public static long checkZone() throws InterruptedException
    {
        LOCK.take();
        try
        {
            long        totalActual[] = {0L};
            boolean     call = false;
            boolean     nameSet=false;
            StopWatch sw  = new StopWatch(true);

            boolean lowMemoryMode=false;
            if( isFull())
            {
                lowMemoryMode=true;
                releaseRainyDayFund();
            }
            
            long totalMemoryUsed=getTotalUsed();
            long        totalMemory;
            totalMemory = getTotalMemory();
            assert totalMemory >0: "max memory should more than zero " + totalMemory;
            assert totalMemoryUsed<=totalMemory:"Total used memory:" + totalMemoryUsed +" must be less than total memory: " + totalMemory;
            long totalMemoryFree=totalMemory-totalMemoryUsed;
            assert totalMemoryFree>=0: "totalMemoryFree must be non negative: " + totalMemoryFree;
            if( totalMemoryFree<0) totalMemoryFree=0;
            
            long paddingMemory = getPaddingMemory();
            long totalMemoryFreeAdjusted=totalMemoryFree-paddingMemory;
            
            if( totalMemoryFreeAdjusted<0) totalMemoryFreeAdjusted=0;
            double totalMemoryFreePercentAdjusted=(double)totalMemoryFreeAdjusted/(double)totalMemory;
            assert totalMemoryFreePercentAdjusted>=0.0 && totalMemoryFreePercentAdjusted<=1.0: "totalMemoryFreePercentAdjusted must be between 0-1: " + totalMemoryFreePercentAdjusted;
            
            Cost maxLevel = null;

            MemoryUsage tenuredUsage;
            for( int attempts=1;true;attempts++)
            {
                try{
                    tenuredUsage= getMemoryUsage(TENURED_GENERATION_POOL);
                    break;
                }
                catch( IllegalArgumentException iae)
                {                    
                    releaseRainyDayFund();
                    
                    if( attempts > 3)
                    {
                        String msg="Can not get the current usage. giving up";
                        lastError(msg, iae);
                        LOGGER.error( msg, iae);                    
                        return 0;
                    }
                    else
                    {
                        LOGGER.warn( "can not get the current usage. retying... " + attempts, iae);                    
                    }
                }
            }
            
            long currentThreadhold = TENURED_GENERATION_POOL.getCollectionUsageThreshold();
            long requiredThreadhold = calculatedTenuredThreshold();
            long tenuredMax=getTenuredTotalMemory(tenuredUsage);
            
            long safetyThreadhold = tenuredMax - (long)(tenuredMax/100.0 * safeZoneLower);
            assert safetyThreadhold<tenuredMax: "safetyThreadhold{" +safetyThreadhold+"} < tenuredMax{" + tenuredMax +"}";

            if( safetyThreadhold < requiredThreadhold) safetyThreadhold=requiredThreadhold;

            if( currentThreadhold != requiredThreadhold)
            {
                try
                {
                    TENURED_GENERATION_POOL.setCollectionUsageThreshold(requiredThreadhold);
                }
                catch(  IllegalArgumentException iae)
                {
                    LOGGER.error("Could not set the setCollectionUsageThreshold to " + requiredThreadhold + " max " + totalMemory, iae);
                }

                try
                {
                    TENURED_GENERATION_POOL.setUsageThreshold(requiredThreadhold);
                }
                catch(  IllegalArgumentException iae)
                {
                    LOGGER.error("Could not set the setUsageThreshold to " + requiredThreadhold + " max " + totalMemory, iae);
                }
            }
            long tenuredUsed  = tenuredUsage.getUsed();
            long tenuredUsedAdjusted  = tenuredUsed-paddingMemory;
            if( tenuredUsedAdjusted < 0) tenuredUsedAdjusted = 0;
//            long tenuredMax = tenuredUsage.getMax();
//            long tenuredCommitted = tenuredUsage.getCommitted();
//            tenuredMax=(tenuredMax>tenuredCommitted?tenuredMax:tenuredCommitted);
//            long tenuredMax = getTenuredTotalMemory(tenuredUsage);
            long tenuredFreeAdjusted =  tenuredMax- tenuredUsedAdjusted ;
            if( tenuredFreeAdjusted<0) tenuredFreeAdjusted=0;
            
            long cleared = 0;
            String type = "unknow";

            long totalUsed=getTotalUsed();
            
            double tenuredFreePercentAdjusted = (double)tenuredFreeAdjusted/(double)tenuredMax;

            long gcCount=getCountGC();
            boolean collectionUsageThresholdExceeded = TENURED_GENERATION_POOL.isCollectionUsageThresholdExceeded();
            boolean usageThresholdExceeded = TENURED_GENERATION_POOL.isUsageThresholdExceeded();
//            long tenuredFreeRequired=0;
            if( usageThresholdExceeded == false)
            {                
                collectionUsageThresholdExceeded=false;
            }
//            else
//            {
//                long usageThreshold = TENURED_GENERATION_POOL.getUsageThreshold();
//                tenuredFreeRequired=usageThreshold-tenuredUsed;
//            }
            long collectionUsageThresholdCount= TENURED_GENERATION_POOL.getCollectionUsageThresholdCount();

            /**
             * If we have < 10% AND there has been a full GC since the last time we ran then we will clear all cache again. 
             */
            if( totalMemoryFreePercentAdjusted < 0.1 && lastPanicGCCount !=gcCount)
            {
                PANIC_COUNT.incrementAndGet();

                long requiredFree = (long)( totalMemory * (double)safeZoneUpper/100.0) - paddingMemory;

                if( requiredFree<0)
                {
                    if( lowMemoryMode)
                    {
                        LOGGER.warn( "Low memory mode but not memory required to be freed, setting the default free amount" );  
                        requiredFree=totalMemory/2;
                    }
                    else
                    {
                        return 0;
                    }
                }

                lastPanicGCCount=gcCount;
                releaseRainyDayFund();

                long free=getFreeMemory();
                long initialFree=free;
                Cost lastCleared=null;
                /* We are in trouble... panic. */
                for(
                    int level = 0;
                    level <= MemoryHandler.Cost.PANIC.level;
                    level++
                )
                {
                    if( free > requiredFree)
                    {
                        break;
                    }
                    lastCleared=Cost.find(level);
                    
                    long levelClearedAmount = freeLevel( lastCleared,  1);
                    if( levelClearedAmount>0)
                    {
                        cleared += levelClearedAmount;

                        if( maxLevel ==null ||level > maxLevel.level)
                        {
                            maxLevel=lastCleared;
                        }
                        Thread.yield();
                        free=getFreeMemory();
                        
                        long realCleared = free-initialFree;
                        if( realCleared > cleared)
                        {
                            cleared=realCleared;
                        }
                    }
                }

                if( lastCleared== Cost.PANIC)
                {
                    type = "PANIC ONLY " + PF.format(totalMemoryFreePercentAdjusted)+ " (" + NumUtil.convertMemoryToHumanReadable(totalMemoryFreeAdjusted) + " of " + NumUtil.convertMemoryToHumanReadable(totalMemory) + ") free.";
                    if( paddingMemory>0)
                    {
                        type +=" Padding " + NumUtil.convertMemoryToHumanReadable(paddingMemory) +".";
                    }
                    type +=" CLEARED " + NumUtil.convertMemoryToHumanReadable(cleared);
                    LOGGER.warn(type);

                    gc();
                }
                
                sw.stop();
            }
            else
            {
                boolean cleanUpNeeded = false;
                boolean clearMediumLevel = false;
                String reason="unknown";
                if(
                    collectionUsageThresholdExceeded&&
                    collectionUsageThresholdCount != lastTenuredThreadholdCount
                )
                {
                    cleanUpNeeded = true;
                    clearMediumLevel = true;
                    lastTenuredThreadholdCount = collectionUsageThresholdCount;
                    reason="Threshold exceeded";
                }
                else if(
                    tenuredUsedAdjusted >= safetyThreadhold &&
                    (
                        lastMediumGCCount!=gcCount                                                                ||
                        lastMediumClearTime + MIN_INCREMENTAL_CLEAR_TIME < sw.creationDate().getTime()
                    )
                )
                {
                    cleanUpNeeded = true;
                    clearMediumLevel = true;
                    reason="tenured used > safety";
                }
                else if(
                    usageThresholdExceeded &&
                    (
                        lastLowGCCount!=gcCount                                                                   ||
                        lastLowClearTime + MIN_INCREMENTAL_CLEAR_TIME < sw.creationDate().getTime()
                    )
                )
                {
                    cleanUpNeeded = true;
                    reason="Threshold exceeded & gc has run";
                }

                if( cleanUpNeeded)
                {
                    long usageThreshold = TENURED_GENERATION_POOL.getUsageThreshold();
                    assert usageThreshold>0:"must be postive: " + usageThreshold;
                    long tenuredFreeRequired=tenuredUsed-usageThreshold;
                    if( tenuredFreeRequired<0)tenuredFreeRequired=0;
                    long tenuredFreeRequiredAdjusted= tenuredFreeRequired + (long)((double)tenuredMax * 0.05); 
                    assert tenuredFreeRequiredAdjusted>0: "tenuredFreeRequiredAdjustedmust be postive: " + tenuredFreeRequiredAdjusted;
                    
                    /** Give ourselves some room to move */
                    long rainyDaySize=releaseRainyDayFund();
                    
                    type = "Clean up: " + reason + ", required " +
                            NumUtil.convertMemoryToHumanReadable(tenuredFreeRequiredAdjusted) + 
                            (rainyDaySize>0?", rainy day fund " +NumUtil.convertMemoryToHumanReadable(rainyDaySize):"") +
                            ", tenured free " +
                            NumUtil.convertMemoryToHumanReadable(tenuredFreeAdjusted) + " (" + PF.format(tenuredFreePercentAdjusted) + ")";

                    RUNNER.setName( "MemoryManager: " + type);
                    nameSet=true;

                    int level;

                    for(
                        level = MemoryHandler.Cost.LOWEST.level;
                        level <= MemoryHandler.Cost.LOW.level;
                        level++
                    )
                    {
                        /**
                         * If we have cleared enough break out.
                         */
                        if( cleared >= tenuredFreeRequiredAdjusted) break;
                        Cost levelCost=Cost.find(level);
                        long levelClearedAmount = freeLevel( levelCost,  1);

                        cleared += levelClearedAmount;

                        if( levelClearedAmount > 0)
                        {
                            maxLevel=levelCost;
                        }
                    }

                    lastLowClearTime = sw.creationDate().getTime();
                    lastLowGCCount=gcCount;

                    if( cleared < tenuredFreeRequiredAdjusted && clearMediumLevel)
                    {
                        lastMediumClearTime = sw.creationDate().getTime();
                        lastMediumGCCount=gcCount;

                        for( level = MemoryHandler.Cost.MEDIUM_LOW.level; level <= MemoryHandler.Cost.PANIC.level; level++)
                        {
                            /**
                             * If we have cleared enough break out.
                             */
                            if( cleared >= tenuredFreeRequiredAdjusted) break;

                            Cost levelCost=Cost.find(level);
                            double percentage=0.0;
                            if( lowMemoryMode == true)
                            {
                                percentage=1.0;
                            }
                            else {
                                long estimate;

                                estimate = estimateLevel( levelCost);
                                if( estimate > 0)
                                {
                                    if( cleared + estimate >= tenuredFreeRequiredAdjusted)
                                    {
                                        percentage = (double)(tenuredFreeRequiredAdjusted - cleared)/(double)estimate;
                                    }
                                    else
                                    {
                                        percentage=1.0;
                                    }
                                }
                            }
                            
                            if( percentage>0)
                            {
                                long amount = freeLevel( levelCost, percentage);

                                if( amount > 0)
                                {
                                    cleared += amount;
                                    maxLevel = levelCost;
                                }
                            }
                        }
                    }

                    /** Give the system a break. */
                    Thread.yield();
                    sw.stop();
                }
            }

            /**
             * Only show the details if we have done something.
             */
            if( cleared > 0)
            {
                call = true;

                long actual;
                actual = showDetails(sw, totalUsed, type + ", est. cleared " + NumUtil.convertMemoryToHumanReadable( cleared), maxLevel, LOGGER);

                if( actual > cleared)
                {
                    totalActual[0] += actual;
                }
                else
                {
                    totalActual[0] += cleared;
                }
            }

            nameSet |= tidyUp( totalActual);

            if( call)
            {
                assert maxLevel!=null;
                callListeners( maxLevel);

                long diff = sw.durationMS();
                totalClearedTimeTaken += diff;
            }

            populatePadding();

            /*
             * Set the name back if changed
             */
            if( nameSet) RUNNER.setName( THREAD_NAME_IDLE);

            long amount= totalActual[0];
            assert amount>=0: "amount should be non negative: " + amount;
            return amount;
        }
        finally
        {
            LOCK.release();
        }
    }

    /* Clear rainy day fund to give us some space to work */
    private static long releaseRainyDayFund()
    {
        long rainyDayFundSize=0;
        byte[] tmpRainyDayFund=rainyDayFund;
        if( tmpRainyDayFund!=null)
        {
            rainyDayFundSize=tmpRainyDayFund.length;
            rainyDayFund = null;                  
        }
        
        return rainyDayFundSize;
    }
    
    private static void populatePadding()
    {
        long threshold = TENURED_GENERATION_POOL.getUsageThreshold();
        assert threshold > 0: "Usage should be positive was: " + threshold;

        long used = getMemoryUsage(TENURED_GENERATION_POOL).getUsed();

        long paddingSize = threshold - used;
        if( paddingSize > MAX_PADDING_SIZE)
        {
            paddingSize=MAX_PADDING_SIZE;
        }

        int segmentSize = (int)(threshold/100L);
        if( segmentSize<=0)segmentSize=1;
        if( segmentSize > PADDING_MAX_SEGMENT) segmentSize=PADDING_MAX_SEGMENT;
        int paddingPercent = (int)(paddingSize/segmentSize);

        paddingPercent -= PADDING_MARGIN;
        // it's posible for the used amount to be many times our threshold
        if( paddingPercent < -100 ) paddingPercent = -100;

        if( paddingPercent != 0)
        {
            int tempPercent=paddingPercent;
            int clearedPercent = 0;
            int addedPercent = 0;
            synchronized( WEAK_PADDING)
            {
                for( int i = 0; i < WEAK_PADDING.length;i++)
                {
                    WeakReference ref = WEAK_PADDING[i];

                    if( ref != null)
                    {
                        if( HARD_PADDING[i] == null)
                        {
                            Object data = ref.get();

                            if( data instanceof byte[])
                            {
                                // if we are adding memory then put the hard links back in
                                if( paddingPercent > 0)
                                {
                                    HARD_PADDING[i] = (byte[])data;
                                }
                            }
                            else
                            {
                                WEAK_PADDING[i] = null;
                                ref = null;
                            }
                        }
                    }

                    if( tempPercent > 0 && addedPercent < PADDING_MAX_GROWTH_PERCENT) // only add few percent at a time.
                    {
                        if( ref == null)
                        {
                            byte array[] = new byte[segmentSize];
                            WEAK_PADDING[i] = new WeakReference( array);
                            HARD_PADDING[i] = array;
                            addedPercent++;
                            tempPercent--;
                        }
                    }
                    else if( tempPercent < 0)
                    {
                        if( ref != null && HARD_PADDING[i] == null)
                        {
                            // We have already released the reference so we don't need to do again as the GC hasn't clear it yet
                            tempPercent++;
                        }
                    }
                }

                // OK we need to release some more.
                for( int i = 0; tempPercent < 0 && i < WEAK_PADDING.length;i++)
                {
                    if( HARD_PADDING[i] != null)
                    {
                        HARD_PADDING[i] = null;
                        tempPercent++;
                        clearedPercent--;
                    }
                }
            }

            if( LOGGER.isDebugEnabled())
            {
                int changedPercent;

                if( paddingPercent < 0)
                {
                    changedPercent = clearedPercent;
                }
                else
                {
                    changedPercent = addedPercent;
                }

                if( changedPercent != 0)
                {
                    LOGGER.debug("Padding Percent: " + changedPercent + "%, size: " + NumUtil.convertMemoryToHumanReadable(segmentSize * changedPercent));
                }
            }
        }
    }

    @CheckReturnValue @Nonnull
    private static MemoryHandler[] listHandlers( final @Nonnull Cost cost) throws InterruptedException
    {
        //assert cost >=0 && cost < LEVELS.length: "Invalid level: " + cost;
        WeakHashMap<MemoryHandler, String> map = LEVELS[ cost.level];
        assert map != null: "no handlers for level: "  + cost;

        MemoryHandler list[];

        Lock l=REGISTER_LOCK.readLock();
        l.lockInterruptibly();
        try
        {
            int size = map.size();
            list = new MemoryHandler[size];

            map.keySet().toArray(list);
        }
        finally
        {
            l.unlock();
        }

        int counter=0;
        for(MemoryHandler h: list)
        {
            if( h == null) counter++;
        }

        if( counter>0)
        {
            MemoryHandler list2[]=new MemoryHandler[list.length-counter];
            int tc=0;
            for(MemoryHandler h: list)
            {
                if( h != null)
                {
                    list2[tc]=h;
                    tc++;
                }
            }

            list=list2;
        }

        return list;
    }

    @SuppressWarnings("null")
    private static boolean tidyUp( long pTotalActual[]) throws InterruptedException
    {
        @SuppressWarnings({"MismatchedReadAndWriteOfArray", "UnusedAssignment"})
        long totalActual[] = pTotalActual;
        boolean nameSet = false;

        long cTime = System.currentTimeMillis();

        /*
         * Only do this once a minute
         */
        if( cTime > lastCheckedIdle + 60L * 1000L)
        {
            String msg = "MemoryManager: tidy up";
            nameSet=true;

            RUNNER.setName( msg);
            lastCheckedIdle = cTime;

            for( int level = 0; level < MAX_IDLE_TIME.length; level++)
            {
                if( MAX_IDLE_TIME[level] != 0)
                {
                    Cost levelCost=Cost.find(level);
                    MemoryHandler list[]=listHandlers( levelCost);

                    for( MemoryHandler h: list)
                    {
                        assert h!=null: "null handler";
                        if( h == null) continue;

                        if( h.getLastAccessed() + MAX_IDLE_TIME[level] < cTime)
                        {
                            /* The freeMemory routine should be fast!!! */
                            long amount;
                            amount = h.queuedFreeMemory(1);

                            if( amount >= 0)
                            {
                                totalActual[0] += amount;
                            }
                            else
                            {
                                String tmpMSG= h + " cleared negative amount of memory " + amount;
                                LOGGER.info( tmpMSG);
                                assert false: tmpMSG;
                            }
                        }
                    }
                }
            }
        }

        return nameSet;
    }

    /**
     * Free  ALL memory from this level.
     *
     * @param cost
     * @param percentage
     * @return the value
     */
    @SuppressWarnings("null") @Nonnegative
    private static long freeLevel( final @Nonnull Cost cost, final @Nonnegative double percentage) throws InterruptedException
    {
        assert percentage>0 && percentage<=1: "invalid percentage " + percentage;
        long cleared=0;

        MemoryHandler list[]=listHandlers(cost);
        
        for( MemoryHandler h: list)
        {
            assert h!=null: "null handler";
            if( h == null) continue;

            /* The freeMemory routine should be fast!!! */
            long amount;
            amount = h.queuedFreeMemory(percentage);

            if( amount > 0)
            {
                cleared += amount;                    
            }
            else if( amount < 0)
            {
                String msg=h + " cleared negative memory " + amount;
                LOGGER.info( msg);
                assert false: msg;
            }
        }

        lastClearedLevel = cost;
        long now=System.currentTimeMillis();
        LAST_CLEARED[cost.level] = now;
        if( cost.level >= highWaterMark.level)
        {
            highWaterMark = cost;
            highWaterMarkTime = now;
        }
        assert cleared >=0: "cleared memory must be NON negative: " + cleared;
        return cleared;
    }

    /**
     * 1/2 meg to do some work
     */
    @Nullable
    public static void makeRainyDay()
    {
        byte temp[] = rainyDayFund;
        if( temp == null) //NOPMD
        {
            if( isFull()==false){
                long free = getFreeMemory();

                if( free * 10 > RAINY_DAY_SIZE)
                {
                    temp = new byte[RAINY_DAY_SIZE];
                    rainyDayFund=temp;
                }
            }
        }
    }

    /**
     * The panic count
     * @return true if disabled
     */
    @CheckReturnValue @Nonnegative
    public static long getPanicCount()
    {
        long count= PANIC_COUNT.get();
        assert count>=0: "panic count must not be negative: " + count;
        return count;
    }

    /**
     * prevent instances MemoryManager from being created.
     */
    private MemoryManager()
    {
    }

    private static class Issue
    {
        final String message;
        final Throwable cause;
        
        Issue( String message, Throwable cause)
        {
            this.message=message;
            this.cause=cause;
        }
    }

    static
    {
        GC_WATCH=new Object[11];
        for( int i = 1; i < GC_WATCH.length;i++)
        {
            GC_WATCH[i]=new Object();
        }

        /**
         * fixed possible concurrence issues.
         */
        for( int pos=0; pos < LEVELS.length; pos ++)
        {
            LEVELS[ pos] = new WeakHashMap<>();
        }

        int defaultValue=5;
        String temp = System.getProperty(ENV_PADDING_MARGIN, "" + defaultValue);

        try
        {
            defaultValue = Integer.parseInt(temp);
        }
        catch( NumberFormatException nf)
        {
             LOGGER.error(ENV_PADDING_MARGIN, nf);
        }
        PADDING_MARGIN=defaultValue;

        defaultValue=5;
        temp = System.getProperty(ENV_PADDING_MAX_GROWTH_PERCENT, "" + defaultValue);

        try
        {
            defaultValue = Integer.parseInt(temp);
        }
        catch( NumberFormatException nf)
        {
             LOGGER.error(ENV_PADDING_MARGIN, nf);
        }
        PADDING_MAX_GROWTH_PERCENT=defaultValue;

        WEAK_PADDING=new WeakReference[100];
        HARD_PADDING=new byte[WEAK_PADDING.length][];

        MEMORY_MX_BEAN = ManagementFactory.getMemoryMXBean();
        List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();

        GarbageCollectorMXBean foundGarbageCollector= null;
        Collector tmpType=Collector.PS;
        for( GarbageCollectorMXBean garbageCollectorMXBean:garbageCollectorMXBeans)
        {
            String names[] = garbageCollectorMXBean.getMemoryPoolNames();
            for( String name: names)
            {
                for( Collector checkCollector: Collector.values())
                {
                    if( name.equals(checkCollector.oldGenerationPoolName))
                    {
                        foundGarbageCollector = garbageCollectorMXBean;
                        tmpType=checkCollector;
                        break;
                    }
                }

                if( foundGarbageCollector != null) break;
            }

            if( foundGarbageCollector != null) break;
        }
        COLLECTOR=tmpType;
        int occupancyFaction = -1;
        if( StringUtilities.notBlank(COLLECTOR.initiatingHeapOccupancyPercentArgument))
        {
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            List<String> inputArguments = runtimeMXBean.getInputArguments();
            for( String argument: inputArguments)
            {
                if( argument.startsWith("-XX:"))
                {
                    String tmpArgument = argument.substring(4);

                    if( tmpArgument.startsWith(COLLECTOR.initiatingHeapOccupancyPercentArgument))
                    {
                        String value = tmpArgument.substring(COLLECTOR.initiatingHeapOccupancyPercentArgument.length() + 1);

                        occupancyFaction=Integer.parseInt(value);
                        break;
                    }
                }
            }
        }
        
        OVERRIDE_OCCUPANCY_FRACTION=occupancyFaction;
        int reservePercent = COLLECTOR.defaultResercePercent;
        if( StringUtilities.notBlank(COLLECTOR.reservePercentArgument))
        {
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            List<String> inputArguments = runtimeMXBean.getInputArguments();
            for( String argument: inputArguments)
            {
                if( argument.startsWith("-XX:"))
                {
                    String tmpArgument = argument.substring(4);

                    if( tmpArgument.startsWith(COLLECTOR.reservePercentArgument))
                    {
                        String value = tmpArgument.substring(COLLECTOR.reservePercentArgument.length() + 1);

                        reservePercent=Integer.parseInt(value);
                        break;
                    }
                }
            }
        }
        
        RESERVE_PERCENT=reservePercent;
        assert foundGarbageCollector != null: "No Old generation GC found";
        TENURED_GENERATION_GARBAGE_COLLECTOR = foundGarbageCollector;

        List<MemoryPoolMXBean> list = ManagementFactory.getMemoryPoolMXBeans();
        MemoryPoolMXBean foundPool= null;
        for (MemoryPoolMXBean list1 : list)
        {
            MemoryPoolMXBean memoryPool;
            memoryPool = list1;
            String name = memoryPool.getName();

            for( Collector t:Collector.values())
            {
                if( name.equals(t.oldGenerationPoolName))
                {
                    foundPool = memoryPool;
                    break;
                }
            }
            if( foundPool != null) break;
        }

        if( foundPool ==null)
        {
            throw new Error( "MemoryManager: no GC pools found");
        }
        TENURED_GENERATION_POOL = foundPool;

        DISABLE_GC = CProperties.isDisabled( "MEMORY_MANAGER_GC");

        setTenuredPercent( CProperties.getProperty( TENURED_PERCENT));
        setSafeZoneLower( CProperties.getProperty( "SAFE_ZONE_LOWER"));
        setSafeZoneUpper( CProperties.getProperty( "SAFE_ZONE_UPPER"));
        setMinFreePercent( CProperties.getProperty( "MIN_FREE_PERCENT"));
        setTenuredSize( CProperties.getProperty( TENURED_SIZE));

        temp = CProperties.getProperty( ENV_MIN_INCREMENTAL_CLEAR_TIME);

        long tempTime = -1;

        if( StringUtilities.isBlank(temp) == false)
        {
            try
            {
                tempTime = Long.parseLong(temp);
            }
            catch( NumberFormatException nf)
            {
                LOGGER.error(ENV_MIN_INCREMENTAL_CLEAR_TIME, nf);
            }
        }

        if( tempTime < 0)
        {
            tempTime = 60000;
        }

        MIN_INCREMENTAL_CLEAR_TIME=tempTime;

        tick();

        LAST_CLEARED = new long[MemoryHandler.Cost.values().length];
        MAX_IDLE_TIME = new long[MemoryHandler.Cost.values().length];

        long cTime = System.currentTimeMillis();

        for( int i = 0; i < LAST_CLEARED.length; i++)
        {
            LAST_CLEARED[i] = cTime;
        }

        MAX_IDLE_TIME[ MemoryHandler.Cost.LOWEST.level] = 5 * 60 * 1000;
        MAX_IDLE_TIME[ MemoryHandler.Cost.LOWER.level]  = 30 * 60 * 1000;
        MAX_IDLE_TIME[ MemoryHandler.Cost.LOW.level]    = 60 * 60 * 1000;

        doSetMaxMemory();

        RUNNER = MemoryManagerRunner.create();

        long tempTenuredThreshold = calculatedTenuredThreshold();
        LOGGER.info("Memory Manager threshold of " + NumUtil.convertMemoryToHumanReadable(tempTenuredThreshold));
    }
}
