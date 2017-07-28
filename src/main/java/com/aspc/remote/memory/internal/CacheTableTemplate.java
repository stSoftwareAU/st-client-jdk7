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
package com.aspc.remote.memory.internal;

import com.aspc.remote.memory.MemoryHandler;
import com.aspc.remote.memory.MemoryManager;
import com.aspc.remote.util.misc.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;

/**
 *  Cache Table template.
 *
 *  Originated from two HashMap, one for weak references and one for hard references.
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED memory management</i>
 *
 *  @author      Nigel Leck
 * @param <V> the class of that is stored.
 *  @since       31 December 1998
 */
public abstract class CacheTableTemplate<V> implements MemoryHandler
{
    protected final ReentrantReadWriteLock rwLock=new ReentrantReadWriteLock(true);

    /**
     * create a new cache table.
     *
     * @param description the description of the cache table.
     * @param theCost the relative cost of this cache table.
     */
    public CacheTableTemplate( final @Nonnull String description, final @Nonnull Cost theCost)
    {
        this.description = description;

        cost = theCost;
        if( cost==Cost.PANIC)
        {
            cost = Cost.HIGHEST;
        }

        hashThreshold = (int)(INITIAL_CAPACITY * LOAD_FACTOR);

        setAverageSize(100);
    }

    /**
     * The default behavior of a cache table is to not release objects that are
     * still referred to elsewhere in the system when release memory is called.
     * This is to prevent duplicates from being loaded and generally taking up more memory. <br>
     * <br>
     * The default behavior can be changed by setting this flag to true. <br>
     * <br>
     * Good examples are DBData we must never load two lots of DBData for one row as only one
     * set of DBObjects would receive the incoming events. In other places we don't care.
     *
     * @param flag may release ?
     */
    public void setMayReleaseReferences( final boolean flag)
    {
        mayReleaseReferences = flag;
    }

    /**
     * sets the threshold to register with the Memory Manager
     *
     * @param count the number of elements before we register with the memory manager.
     */
    public final void setThreshold( final int count)
    {
        memoryMangerThreshold = count;
    }

    /**
     *
     * @param collector the collector
     */
    public void stats( final @Nullable StatsCollector collector)
    {
        Lock l = rwLock.readLock();
        l.lock();
        try
        {
            InterfaceEntry tmpData[]=(InterfaceEntry[]) data;
            if( tmpData == null) return;

            int tmpCount = 0,
                blank = 0,
                multi = 0,
                max = 0;

            for (int i = tmpData.length ; i-- > 0 ;)
            {
                int c = 0;
                for (InterfaceEntry e = tmpData[i] ; e != null ; e = e.next())
                {
                    if( collector != null) collector.examine(e);
                    c++;
                    tmpCount++;
                }

                if( c == 0) blank++;
                if( c > 1) multi++;

                if( c > max) max = c;
            }

            if( LOGGER.isDebugEnabled())
            {
                LOGGER.debug( description + ", count=" + tmpCount + ", blank=" + blank+ ", max=" + max+ ", multi=" + multi + ", len=" + tmpData.length );
            }
        }
        finally
        {
            l.unlock();
        }
    }

    /**
     * gets the description of this cache table.
     *
     * @return the description.
     */
    @CheckReturnValue @Nonnull
    public String getDescription()
    {
        return description;
    }

    /**
     * The cost of this cache table.
     *
     * @return the relative cost
     */
    @Override @CheckReturnValue @Nonnull
    public final Cost getCost( )
    {
        return cost;
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map.
     */
    @CheckReturnValue @Nonnegative
    public final int size()
    {
        Lock l = rwLock.readLock();
        l.lock();

        try
        {
            return count;
        }
        finally
        {
            l.unlock();
        }
    }

    /**
     * The number of entries that have hard links.
     *
     * @return the count
     */
    @CheckReturnValue @Nonnegative
    public final int getHardLinkCount()
    {
        Lock l = rwLock.readLock();
        l.lock();

        try
        {
            return count - unallocated;
        }
        finally
        {
            l.unlock();
        }
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings.
     */
    @CheckReturnValue
    public final boolean isEmpty()
    {
        Lock l = rwLock.readLock();
        l.lock();

        try
        {
            return count == 0;
        }
        finally
        {
            l.unlock();
        }
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value.
     *
     * @param value value whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value.
     */
    @CheckReturnValue
    public final boolean containsValue( final @Nonnull V value)
    {
        Lock l = rwLock.readLock();
        l.lock();

        try
        {
            if( data == null) return false;

            for (int i = data.length ; i-- > 0 ;)
            {
                for (InterfaceEntry e = (InterfaceEntry)data[i] ; e != null ; e = e.next())
                {
                    Object temp = e.get();

                    if( temp == null)
                    {
                        estEmptyCount++;

                        checkRegister();
                    }
                    else if (value.equals( temp))
                    {
                        return true;
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
     * Removes all mappings from this map.
     */
    public final void clear()
    {
        Lock l = rwLock.writeLock();
        l.lock();

        try
        {
            data = createArray( INITIAL_CAPACITY);
            estEmptyCount = 0;
            count = 0;
            unallocated = 0;

            checkRegister();
        }
        finally
        {
            l.unlock();
        }
    }

    /**
     * The last time this cache table was accessed.
     *
     * @return The last time the cache table was access.
     */
    @Override @CheckReturnValue
    public final long getLastAccessed()
    {
        Lock l = rwLock.readLock();
        l.lock();

        try
        {
            return lastAccessed * 1000L;
        }
        finally
        {
            l.unlock();
        }
    }

    /**
     * Sets the estimated average size of the objects held.
     *
     * @param size the estimated average size.
     */
    public final void setAverageSize( final @Nonnegative int size)
    {
        if( size<1)throw new IllegalArgumentException("average size must be positive: " + size);
        Lock l = rwLock.writeLock();
        l.lock();
        try
        {
            averageSize = size;
        }
        finally
        {
            l.unlock();
        }
    }

    /**
     * The estimated average size.
     *
     * @return the size in bytes
     */
    @CheckReturnValue @Nonnegative
    public final int getAverageSize()
    {
        Lock l = rwLock.readLock();
        l.lock();
        try
        {
            return averageSize;
        }
        finally
        {
            l.unlock();
        }
    }

    /**
     * The description of this cache table
     *
     * @return the description
     */
    @Override @CheckReturnValue @Nonnull
    public String toString( )
    {
        return getDescription();
    }

    /**
     * The estimated amount of memory held by this cache table.
     *
     * @return the estimated size in bytes
     */
    @Override @CheckReturnValue @Nonnegative
    public final long getEstimatedSize()
    {
        Lock l = rwLock.readLock();
        l.lock();
        try
        {
            if( data == null) return 0;

            long size = 0;

            long elementSize = count * OVERHEAD_ELEMENT;
            size += elementSize;

            long arraySize = data.length * MemoryUtil.sizeOfPointer();
            size += arraySize;

            int temp = count - unallocated;

            long dataSize = 0;

            if( temp > 0)
            {
                dataSize = (long)temp * (long)averageSize;
            }

            size += dataSize;
            assert size>=0: "negative size: " + size + " ( count=" + count + ")";
            return size;
        }
        finally
        {
            l.unlock();
        }
    }

    /**
     * When called if the items are stored as softRefernces
     * and in tidy up mode then just clear the ones that
     * have being removed. Otherwise clear all memory held
     *
     * @param requiredPercent The required percent to free.
     *
     * @return The amount cleared in bytes.
     */
    @Override @Nonnegative
    public final long freeMemory( final @Nonnegative double requiredPercent)
    {
        return MemoryManager.callFreeMemory( this, requiredPercent);
    }

    /**
     * Clear all memory ASAP.
     *
     * @return The estimated total number of bytes released.
     */
    @Override @Nonnegative
    public long panicFreeMemory()
    {
        return queuedFreeMemory(1);
    }

    /**
     * The queued called to freeMemory which is needed to prevent a deadlock.
     *
     * @param requiredPercent The memory to free
     *
     * @return The estimate amount of memory released.
     */
    @Override @Nonnegative
    public final long queuedFreeMemory( final double requiredPercent)
    {
        assert requiredPercent>0 && requiredPercent<=1: "invalid percentage: " + requiredPercent;
        Lock l = rwLock.writeLock();
        l.lock();
        try
        {
            long orginalSize = getEstimatedSize();

            if( orginalSize == 0) return 0;

            /**
             * If the programmer has specified that we don't care about whether these
             * objects are referenced else where and it is more than 90% to be freed
             * then just clear the whole table.
             */
            if( mayReleaseReferences && requiredPercent > 0.9)
            {
                clear();

                return orginalSize;
            }

            int holder = count;

            int reqCleared = count;
            int remaining = count;
            int previouslyAllocated = count - unallocated;

            unallocated = 0;

            int lowerLimit = lastAccessed;
            int tempFirstAccess = (int)(MemoryManager.lastTick()/1000L);

            int freed = 0;

            /**
             * We found a deadlock when writing out to the log file ( conflict with the
             * print of a validation exception message where the toString() method resulted
             * in a fetch)
             */
            if( requiredPercent != 1)
            {
                int diff = (int)(( lastAccessed - firstAccessed ) * requiredPercent);

                lowerLimit = firstAccessed + diff + 1;

                int objCleared = (int)(previouslyAllocated * requiredPercent);

                /**
                 * Take into account the size of the data array
                 */
                int arrayCleared;

                arrayCleared = (int)Math.round((data.length * MemoryUtil.sizeOfPointer() )/(double)getAverageSize() * requiredPercent);

                /**
                 * Take into account the element overhead
                 */
                int overheadCleared;

                overheadCleared = (int)Math.round((count * OVERHEAD_ELEMENT)/(getAverageSize() * requiredPercent));

                reqCleared = objCleared + arrayCleared + overheadCleared;

                /**
                 * If you have asked me to release some memory
                 * make sure that we release at least one.
                 */
                if( reqCleared == 0 && requiredPercent > 0.0)
                {
                    reqCleared = 1;
                }
            }

            if( mayReleaseReferences)
            {
                for (int i = data.length ; i-- > 0; )
                {
                    InterfaceEntry prev = null;

                    for (InterfaceEntry e = (InterfaceEntry)data[i] ; e != null; e = e.next(), remaining--)
                    {
                        /**
                         * Need to maintain the unallocated to keep and actuate estimate.
                         */
                        if( freed >= reqCleared)
                        {
                            if( e.hasHardReference() == false)
                            {
                                unallocated++;
                            }
                        }
                        else
                        {
                            int tempAccess = e.getData() & 0xfffe;

                            /**
                             * 1) If the the minimum required to be cleared is more then the remaining elements then clear.
                             * 2) If the last access time of this element is less than the lower limit then clear and we haven't cleared
                             *    too many already.
                             */
                            if(
                                reqCleared >= remaining + freed     ||
                                (
                                    tempAccess <= lowerLimit &&
                                    reqCleared > freed
                                )
                            )
                            {
                                removeElement( e, i, prev, false);
                                freed++;

                                continue;
                            }

                            /**
                             * If we are going to keep this record then
                             * record the first access time
                             */
                            if( tempAccess < tempFirstAccess)
                            {
                                tempFirstAccess = tempAccess;
                            }
                        }
                        prev = e;
                    }
                }
            }
            else
            {
                for (int i = data.length ; i-- > 0; )
                {
                    InterfaceEntry prev = null;

                    for (InterfaceEntry e = (InterfaceEntry)data[i] ; e != null; e = e.next(), remaining--)
                    {
                        /**
                         * Need to maintain the unallocated to keep and actuate estimate.
                         */
                        if( freed >= reqCleared)
                        {
                            if( e.hasHardReference() == false)
                            {
                                unallocated++;
                            }
                        }
                        else
                        {
                            boolean flag = false;
                            boolean hasHardLink = e.hasHardReference();
                            boolean mustKeep = false;
                            if( hasHardLink )
                            {
                                /**
                                 * 1) If the the minimum required to be cleared is more then the remaining elements then clear.
                                 * 2) If the last access time of this element is less than the lower limit then clear and we haven't cleared
                                 *    too many already.
                                 */
                                if( e instanceof CacheEntry)
                                {
                                    if(reqCleared >= remaining + freed )
                                    {
                                        flag = true;

                                        ((CacheEntry)e).hardReference=null;
                                    }
                                    else
                                    {
                                        int tempAccess = e.getData() & 0xfffe;

                                        if(
                                            tempAccess <= lowerLimit &&
                                            reqCleared > freed
                                        )
                                        {
                                            flag = true;
                                            ((CacheEntry)e).hardReference=null;
                                        }
                                        else
                                        {
                                            mustKeep = true;
                                            /**
                                             * If we are going to keep this record then
                                             * record the first access time
                                             */
                                            if( tempAccess < tempFirstAccess)
                                            {
                                                tempFirstAccess = tempAccess;
                                            }
                                        }
                                    }
                                }
                            }

                            if( mustKeep == false)
                            {
                                Object temp = e.get();

                                if( temp == null)
                                {
                                    removeElement( e, i, prev, false);

                                    if( hasHardLink) freed++;

                                    continue;
                                }
                                else
                                {
                                    if( flag)
                                    {
                                        freed++;
                                    }

                                    if( flag || hasHardLink == false)
                                    {
                                        unallocated++;
                                    }
                                }
                            }
                        }
                        prev = e;
                    }
                }
            }

            estEmptyCount = 0;
            checkRegister();

            /**
             * We may not clear all of the elements under the limit but make sure first accessed is set to the lower
             * limit so that it is evenly distributed.
             */
            firstAccessed = tempFirstAccess;
            if( firstAccessed < lowerLimit) firstAccessed = lowerLimit;

            long currentSize = getEstimatedSize();
            long releasedSize = orginalSize - currentSize;
            if( releasedSize <= 0)
            {
                releasedSize = freed * averageSize;
            }

            if( releasedSize != 0 && LOGGER.isDebugEnabled())
            {
                String freedStr = NUMBER_FORMAT.format( freed);

                String holderStr = NUMBER_FORMAT.format( holder);

                double percentCleared = (double)releasedSize/(double)orginalSize;

                String percentStr = PERCENT_FORMAT.format( percentCleared);
                String requiredStr = PERCENT_FORMAT.format( requiredPercent);

                String previouslyAllocatedStr = NUMBER_FORMAT.format( previouslyAllocated);

                if( LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(
                        "Cleared " + description + " cost: " + cost + " unlinked: " + freedStr +
                        " of " + previouslyAllocatedStr + "(" + holderStr + ") required: " + requiredStr +
                        " Released: " + NumUtil.convertMemoryToHumanReadable( releasedSize)+ " " + percentStr
                    );
                }
            }

            return releasedSize;
        }
        finally
        {
            l.unlock();
        }
    }

    /**
     * Compact
     */
    public void compact()
    {
        Lock l = rwLock.writeLock();
        l.lock();
        try
        {
            InterfaceEntry oldArray[] = (InterfaceEntry[])data;

            int newCapacity = (int)(count * LOAD_FACTOR) + 1;

            if( newCapacity < INITIAL_CAPACITY) newCapacity = INITIAL_CAPACITY;

            if( newCapacity < oldArray.length)
            {
                copyData( oldArray, newCapacity);
            }
        }
        finally
        {
            l.unlock();
        }
    }

    /**
     *
     * @return the amount cleared.
     */
    @Override @Nonnegative
    public final long tidyUp()
    {
        double percent=0.05;
        Lock l = rwLock.readLock();
        l.lock();
        try
        {
            registeredForTidyUp = false;

//            percent = (double)conservativeCount/(double)count;
//
//            if( conservativeCount > 0 && percent < 0.01) percent = 0.01;
        }
        finally
        {
            l.unlock();
        }
        return queuedFreeMemory( percent);
    }

    /*========================================================================
     * PROTECTED/PRIVATE METHODS EXPECT LOCKS TO ALREADY TO BE TAKEN.
     *========================================================================*/

    /**
     * tidy up
     */
    protected void tidyUpRequired( )
    {
        if( registeredForTidyUp == false)
        {
            registeredForTidyUp = true;
            EuthanasiaManager.register( this);
        }
    }

    /**
     *
     * @param current the interface
     * @param pos the position
     * @param prev the previous entry
     * @param updateUnallocated update the unallocated
     */
    protected final void removeElement(
        final InterfaceEntry current,
        final int pos,
        final InterfaceEntry prev,
        final boolean updateUnallocated
    )
    {
        if( prev != null)
        {
            InterfaceEntry nextPtr = current.next();

            assert prev!=nextPtr: "POINTER LOOPS BACK for " + description;

            prev.setNext( nextPtr);
        }
        else
        {
            data[pos] = current.next();
        }

        current.setNext( null);

        if( updateUnallocated && unallocated > 0) unallocated--;
        if( count > 0) count--;
    }

    /**
     * check registered
     */
    protected final void checkRegister( )
    {
        if( count - unallocated > memoryMangerThreshold)
        {
            if( registered == false)
            {
                MemoryManager.register(this);
                registered = true;
            }
        }
        else
        {
            if( registered)
            {
                MemoryManager.deregister(this);
                registered = false;
            }
        }
    }

    /**
     * @param e The entry to touch.
     */
    protected final void touch( final InterfaceEntry e)
    {
        lastAccessed = (int)(MemoryManager.lastTick()/1000L);
        e.setData( lastAccessed & 0xfffe);

        if( firstAccessed == 0)
        {
            firstAccessed = lastAccessed;
        }
    }

    /**************************************************************************/
    /****                         ABSTRACT                                  ***/
    /**************************************************************************/

    /**
     * Create the underlying entry array
     *
     * @param size the number of entries to create
     *
     * @return the array
     */
    protected abstract InterfaceEntry[] createArray( final int size);

    /**************************************************************************/
    /****                        PROTECTED                                  ***/
    /**************************************************************************/
    /**
     * Rehashes the contents of this map into a new <tt>HashMap</tt> instance
     * with a larger capacity. This method is called automatically when the
     * number of keys in this map exceeds its capacity and load factor.
     *
     * @param indexTemp the index
     * @param hash the hash code
     * @return the new index
     */
    protected final int rehash( final int indexTemp, final int hash)
    {
        if (count < hashThreshold)
        {
            return indexTemp;
        }

        InterfaceEntry oldArray[] = (InterfaceEntry[])data;

        int newCapacity = oldArray.length * 2 + 1;

        copyData( oldArray, newCapacity);

        return (hash & 0x7FFFFFFF) % data.length;
    }

    private void copyData( InterfaceEntry oldArray[], int newCapacity)
    {
        InterfaceEntry tempData[] = createArray(newCapacity);
        hashThreshold = (int)(tempData.length * LOAD_FACTOR);

        int tempCount=0;
        int tempUnallocated =0;

        for (int i = oldArray.length ; i-- > 0 ;)
        {
            for (InterfaceEntry old = oldArray[i] ; old != null ; )
            {
                InterfaceEntry e = old;
                old = old.next();

                if( e.get() != null)
                {
                    int index = (e.hashCode() & 0x7FFFFFFF) % tempData.length;
                    InterfaceEntry nextPtr = tempData[index];
                    
                    assert e!=nextPtr: "POINTER LOOPS BACK for " + description;

                    e.setNext(nextPtr);

                    tempData[index] = e;

                    if( e.hasHardReference() == false) tempUnallocated++;

                    tempCount++;
                }
            }
        }

        data = tempData;
        count = tempCount;
        unallocated=tempUnallocated;
    }

    /** the cost */
    protected Cost                       cost;
    /** The estimated empty count */
    protected int                       estEmptyCount;
    /** the number unallocated */
    protected int                       unallocated;
    /** the threshold */
    protected int                       memoryMangerThreshold;

    /** the description */
    protected String                    description;

    /** the data */
    protected Object[]                  data;
    /** the count */
    protected int                       count;

    /**************************************************************************/
    /****                         PRIVATE                                   ***/
    /**************************************************************************/

    private int                         hashThreshold;

    /** the average size of elements in this cache table */
    private int                         averageSize;

    /** The time an element was accessed. */
    private int                         lastAccessed,
                                        firstAccessed;

    private boolean                     registered,
                                        registeredForTidyUp,
                                        mayReleaseReferences;

    private static final ConcurrentDecimalFormat  NUMBER_FORMAT = new ConcurrentDecimalFormat( "#,##0");
    private static final ConcurrentDecimalFormat  PERCENT_FORMAT = new ConcurrentDecimalFormat( "#,##0.00%");

    /** the capacity */
    protected static final int          INITIAL_CAPACITY    = 1001;
    private static final float          LOAD_FACTOR         = 0.75f;

    private static final int            OVERHEAD_ELEMENT    = 48;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.memory.internal.CacheTableTemplate");//#LOGGER-NOPMD

    /**
     * The property to enable pointer check.
     */
    public static final String PROPERTY_ENABLE_POINTER_CHECK="ENABLE_POINTER_CHECK";

}
