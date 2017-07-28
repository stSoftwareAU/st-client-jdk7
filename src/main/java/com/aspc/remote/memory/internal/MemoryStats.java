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

import com.aspc.remote.memory.MemoryHandler.Cost;
import java.text.DecimalFormat;
import com.aspc.remote.util.misc.*;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 *  MemoryStats
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED memory management</i>
 *
 *  @author      Nigel Leck
 *  @since       23 February 2001, 11:39
 */
public class MemoryStats
{
    /**
     * Creates new MemoryStats
     * 
     * @param totalCount the total count
     * @param hardCount the hard count
     * @param cost the cost
     * @param lastAccessed the last accessed
     * @param estimateSize the size
     * @param name the name
     */
    public MemoryStats(final @Nonnull String name, final Cost cost, final long estimateSize, final int totalCount, final int hardCount, final long lastAccessed)
    {
        this.name = name;
        this.estimateSize = estimateSize;
        this.totalCount = totalCount;
        this.hardCount = hardCount;
        this.lastAccessed = lastAccessed;
        this.cost = cost;
    }

    /**
     * the name
     *
     * @return the name
     */
    @CheckReturnValue @Nonnull
    public String getName()
    {
        return name;
    }

    /**
     * the estimate size
     *
     * @return the size
     */
    @CheckReturnValue
    public long getEstimateSize()
    {
        return estimateSize;
    }

    /**
     * The last accessed
     *
     * @return the time in seconds
     */
    @CheckReturnValue
    public long getLastAccessed()
    {
        return lastAccessed;
    }

    /**
     * The last accessed
     *
     * @return the last accessed
     */
    @CheckReturnValue @Nonnull
    public String getLastAccessedStr()
    {
        if( lastAccessed == 0) return "";

        return TimeUtil.getDiff( lastAccessed) + " ago";
    }

    /**
     * The average size
     *
     * @return the size
     */
    @CheckReturnValue @Nonnull
    public String getEstimateAvgStr()
    {
        if( totalCount > 0)
        {
            return NumUtil.convertMemoryToHumanReadable(estimateSize/totalCount);
        }

        return "";
    }

    /**
     * The estimate size
     *
     * @return the size
     */
    @CheckReturnValue @Nonnull
    public String getEstimateSizeStr()
    {
        return NumUtil.convertMemoryToHumanReadable(estimateSize);
    }

    /**
     * the item count
     *
     * @return the count
     */
    @CheckReturnValue @Nonnull
    public String getItemCountStr()
    {
        if( totalCount >= 0)
        {
            DecimalFormat df = new DecimalFormat( "#,##0");

            String temp = df.format( totalCount);

            if( hardCount != totalCount)
            {
                temp += " { " + df.format( hardCount) + " }";//NOPMD
            }

            return temp;
        }

        return "";
    }

    /**
     * the cost
     *
     * @return the cost
     */
    @CheckReturnValue @Nonnull
    public String getCostStr()
    {
        return cost.label;
    }

    /**
     * the cost
     *
     * @return the cost
     */
    @CheckReturnValue @Nonnull
    public Cost getCost()
    {
        return cost;
    }

    /**
     * the count
     *
     * @return  count
     */
    @CheckReturnValue
    public int getTotalCount()
    {
        return totalCount;
    }

    /**
     * the description
     *
     * @return the description
     */
    @Override @CheckReturnValue @Nonnull
    public String toString()
    {
        String msg = name +
            " items=" + totalCount + ( totalCount != hardCount ? " {" + hardCount + "}" : "") +
            " estimateSize=" + NumUtil.convertMemoryToHumanReadable(estimateSize);

        return msg;
    }

    private final String name;

    private final int hardCount,
                totalCount;
    private final Cost cost;

    private final long estimateSize,
                 lastAccessed;
}
