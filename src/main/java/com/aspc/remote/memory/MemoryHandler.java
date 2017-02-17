/*
 *  Copyright (c) 1998-2004 ASP Converters pty ltd
 *
 *  www.aspconverters.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.memory;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 *  MemoryHandler
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED </i>
 *
 *  @author      Nigel Leck
 *  @since       15 November 1998
 */
public interface MemoryHandler
{
    public enum Cost{
        LOWEST(0,       "Lowest Cost",  "Memory only clone of an java object for an single user."),
        LOWER(1,        "Lower Cost",   "Memory only clone of an java object for multiple users"),
        LOW(2,          "Low Cost",     "Memory only clone of an java object for multiple users"),
        MEDIUM_LOW(3,   "Med-low Cost", "Medium Cost is for a single record loaded from the database"),
        MEDIUM(4,       "Medium Cost","Medium Cost is for a single record loaded from the database"),
        MEDIUM_HIGH(5,  "Med-high Cost","Medium Cost is for a single record loaded from the database"),
        HIGH(6,         "High Cost","High cost is an aggregation of data loaded from the database"),
        HIGHER(7,       "Higher Cost","High cost is an aggregation of data loaded from the database"),
        HIGHEST(8,      "Highest Cost","High cost is an aggregation of data loaded from the database"),
        PANIC(9,        "** PANIC **","Last dig effort to clear memory");
        
        /** The level */
        public final int level;
        /** the description */
        public final String description;
        
        @CheckReturnValue @Nonnull
        public static Cost find( final int level)
        {
            switch( level)
            {
                case 0:
                    return LOWEST;
                case 1:
                    return LOWER;
                case 2:
                    return LOW;
                case 3:
                    return MEDIUM_LOW;
                case 4:
                    return MEDIUM;
                case 5:
                    return MEDIUM_HIGH;
                case 6:
                    return HIGH;
                case 7:
                    return HIGHER;
                case 8:
                    return HIGHEST;
                case 9:
                    return PANIC;
                default:
                    throw new IllegalArgumentException("invalid cost " + level);
            }                        
        }
        
        /** label */
        public final String label;
        private Cost( final int level, final String label, final String description)
        {
            this.level=level;
            this.label=label;
            this.description=description;
        }
    };

    /**
     * The cost level for this MemoryHandler.
     * @return The cost
     */
    @Nonnull @CheckReturnValue
    Cost getCost();

    /**
     * Free memory at this level and below. <br>
     * To prevent a possible deadlock this method should not be synchronized. It should call
     * MemoryManager.callFreeMemory which in turn calls queuedFreeMemory()
     *
     * @param percentage to be freed
     *
     * @return the approximate amount freed
     */
    @Nonnegative 
    long freeMemory( @Nonnegative double percentage);

    /**
     * Tidy up the memory handler
     * @return the total bytes cleared.
     */
    @Nonnegative 
    long tidyUp();

    /**
     * This method should implement the freeing of the method and maybe synchronized.
     *
     * @param percentage The percentage to be cleared.
     * @return the estimated amount cleared.
     */
    long queuedFreeMemory( @Nonnegative double percentage);
    
    /**
     * Clear all memory.
     *
     * @return the estimated amount cleared.
     */
    @Nonnegative 
    long panicFreeMemory();
    
    /**
     * The estimated size of data held by this memory handler
     *
     * @return The estimated number of bytes held.
     */
    @CheckReturnValue @Nonnegative 
    long getEstimatedSize();

    /**
     * The last time this memory handle was accessed
     *
     * @return The last time in seconds this memory handler was accessed.
     */
    @CheckReturnValue @Nonnegative 
    long getLastAccessed();
}
