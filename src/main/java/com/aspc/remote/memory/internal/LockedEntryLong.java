/*
 *  Copyright (c) 2002-2004 ASP Converters pty ltd
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

/**
 *  Locked "long" entry
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED memory management</i>
 *
 *  @author      Nigel Leck
 *  @since       18 December 2007
 */
public class LockedEntryLong extends LockedEntry implements InterfaceEntryLong
{
    /**
     * The cache entry for objects with a long for a key.
     *
     * @param key the key
     * @param hashCode the hash number
     * @param referent the object that we want to cache
     * @param next the next entry
     */
    public LockedEntryLong( final long key, final int hashCode, final Object referent, final InterfaceEntry next)
    {
        super( hashCode, referent, next);

        this.theKey = key;
    }
    
    /**
     * 
     * @return the key
     */
    @Override
    public long key()
    {
        return theKey;
    }
    
    /**
     * The key for this entry
     */
    public final long theKey;
}
