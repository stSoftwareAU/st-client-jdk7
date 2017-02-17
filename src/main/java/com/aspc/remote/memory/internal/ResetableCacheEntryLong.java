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
package com.aspc.remote.memory.internal;

/**
 *  Cache reference
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED memory management</i>
 *
 *  @author      Nigel Leck
 *  @since       3 November 2002
 */
public class ResetableCacheEntryLong extends CacheEntryLong
{
    /**
     * The cache entry for objects with a long for a key.
     *
     * @param key the key
     * @param hashCode the hash number
     * @param referent the object that we want to cache
     * @param next the next entry
     * @param readCheck the read check
     */
    public ResetableCacheEntryLong( 
        final long key, 
        final int hashCode, 
        final Object referent, 
        final InterfaceEntry next,
        final int readCheck
    )
    {
        super( key, hashCode, referent, next);
        
        reset = readCheck;
    }

    /**
     * The reset value
     */
    public int reset;
}
