/*
 *  Copyright (c) 2001-2004 ASP Converters pty ltd
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

import org.apache.commons.logging.Log;
import com.aspc.remote.memory.SizeOf;
import com.aspc.remote.util.misc.*;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 *  Common table allows us to save memory by reusing common data.
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED memory management</i>
 *
 *  @author      Nigel Leck
 *  @since       April 21, 2001, 10:58 AM
 */
public class CommonTable implements SizeOf
{
    private final String description;

    private CommonEntry[] data = new CommonEntry[2 << 7];
    private int currentCount;

    private int threshold = (int)(data.length * 0.75f);
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.memory.internal.CommonTable");//#LOGGER-NOPMD

    /**
     * dump the content
     */
    public void dump()
    {
        int unallocated = 0,
            allocated = 0;

        for( int index = 0; index < data.length; index++)//MT WARN: Inconsistent synchronization
        {
            for (CommonEntry e = data[index] ; e != null ; e = e.next())
            {
                Object obj = e.get();

                if( obj == null)
                {
                    LOGGER.info( index + ": unallocated" );
                    unallocated++;
                }
                else
                {
                    LOGGER.info( index + ": " + obj.hashCode() + "=" + obj );
                    allocated++;
                }
            }
        }

        LOGGER.info( description + " allocated: " + allocated + " unallocated: " + unallocated);
    }

    /**
     * compact the table
     */
    public synchronized void compact()
    {
       forceRehash(0, data.length, false);
    }

    /**
     * Get the current count
     * @return the current count
     */
    public int getCount()
    {
        return currentCount;
    }
    /**
     * make sure there is only one instance of this object
     *
     * @param obj the object to intern
     * @return the unique object
     */
    public synchronized Object intern( final Object obj)
    {
        /* Makes sure the key is not already in the HashMap. */
        int hash;

        hash = obj.hashCode();

        int index = (hash & 0x7FFFFFFF) % data.length;

        CommonEntry prev = null;

        for (CommonEntry nextEntry = data[index] ; nextEntry != null ;)
        {
            CommonEntry entry = nextEntry;
            nextEntry = entry.next();

            Object target = entry.get();

            if( target == null)
            {
                removeElement( entry, index, prev);
            }
            else
            {
                if (target.hashCode() == hash)
                {
                    if( obj.equals( target))
                    {
                        return target;
                    }
                }

                prev = entry;
            }
        }

        index = rehash( index, hash);

        // Creates the new entry.
        CommonEntry e = CommonEntry.create( obj, data[index]);
        data[index] = e;
        currentCount++;

        return obj;
    }

    private void removeElement( final CommonEntry current, final int pos, final CommonEntry prev)
    {
        if( prev != null)
        {
            prev.setNext( current.next());
        }
        else
        {
            data[pos] = current.next();
        }

        current.setNext( null);

        if( currentCount > 0) currentCount--;
    }

    /**
     *
     * @param description the description
     */
    public CommonTable( final String description)
    {
        this.description = description;
    }

    /**
     *
     * @return the string
     */
    @Override @CheckReturnValue @Nonnull
    public String toString( )
    {
        return description;
    }

    private int forceRehash( final int hash, final int newCapacity, final boolean ignoreCounts)
    {
        int oldCapacity = data.length;

        CommonEntry oldArray[] = data;

        CommonEntry tempArray[] = new CommonEntry[newCapacity];
        int oldCount = currentCount;
        int newCount=0;

        for (int oldIndex = oldCapacity ; oldIndex-- > 0 ;)
        {
            CommonEntry oldPrev = null;

            for (CommonEntry oldNextEntry = oldArray[oldIndex] ; oldNextEntry != null ; )
            {
                CommonEntry oldEntry = oldNextEntry;
                oldNextEntry = oldEntry.next();

                Object target = oldEntry.get();

                if( target != null)
                {
                    int index = (target.hashCode() & 0x7FFFFFFF) % newCapacity;

                    CommonEntry nextEntry = tempArray[index];
                    CommonEntry entry = oldEntry;

                    if( nextEntry != null)
                    {
                        if( entry instanceof CommonEntryLinked)
                        {
                            if( entry.next() != nextEntry)
                            {
                                entry = CommonEntry.create(target, nextEntry);
                            }
                        }
                        else
                        {
                            entry = CommonEntry.create(target, nextEntry);
                        }
                    }
                    else
                    {
                        if( entry.next() != null)
                        {
                            entry = CommonEntry.create(target, null);
                        }
                    }

                    tempArray[index] = entry;

                    newCount++;
                    oldPrev = oldEntry;
                }
                else
                {
                    removeElement( oldEntry, oldIndex, oldPrev);
                }
            }
        }

        // if we have drop backwards then don't change the common table.
        if( ignoreCounts == false && newCount < oldCapacity * 0.20f && oldCapacity > 1000)
        {
            int calculateCapacity = (int)(( newCount + 1000) * 2.5) + 1;
            int pc = ( 100 - (newCount * 100/ oldCapacity));
            if( LOGGER.isDebugEnabled())
            {
                LOGGER.debug(
                    description + " COMPACTING as counts dropped from " +
                    oldCount + "->" + newCount + " " + pc +
                    "% remaining RESIZING TO " +calculateCapacity
                );
            }
            return forceRehash( hash, calculateCapacity, true);
        }
        else if( ignoreCounts == false && newCount < oldCapacity * 0.5f)
        {
            if( LOGGER.isDebugEnabled())
            {
                LOGGER.debug(
                    description + " did not grow as counts dropped from " + oldCount + "->" + newCount + " " + ( 100 - (newCount * 100/ oldCapacity)) + "% remaining"
                );
            }
        }
        else
        {
            int pc = ( 100 - (newCount * 100/ newCapacity));
            if( pc < 0)
            {
                LOGGER.warn( description + " count " + newCount + " is more than capacity " + newCapacity + " " + pc + "% remaining" );
            }
            else
            {
                if( LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(
                        description + " count " + newCount + " capacity grown from " + oldCapacity + "->" + newCapacity + " " + pc + "% remaining"
                    );
                }
            }
            data = tempArray;
            threshold = (int)( newCapacity * 0.75f);
        }

        currentCount = newCount;
        return (hash & 0x7FFFFFFF) % data.length;
    }

    private int rehash( final int indexTemp, final int hash)
    {
        if( currentCount < threshold) return indexTemp;

        return forceRehash( hash, data.length * 2 + 1, false);
    }

    /**
     *
     * @return the estimated size in bytes
     */
    @Override
    public synchronized long estimatedMemoryInBytes()
    {
        return data.length * 8 + currentCount * 48; // a pointer is 8 bytes each CommonEntry is 48 bytes
    }
}
