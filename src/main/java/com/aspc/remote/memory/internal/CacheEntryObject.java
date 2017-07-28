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
 *  Cache reference
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED memory management</i>
 *
 *  @author      Nigel Leck
 *  @since       3 November 2002
 */
public final class CacheEntryObject extends CacheEntry implements InterfaceEntryObject
{
    /**
     * The cache entry with an object key
     *
     * @param key the object key
     * @param hashCode the hash code
     * @param referent the object to be stored.
     * @param next the next entry
     */
    public CacheEntryObject(Object key, int hashCode, Object referent, InterfaceEntry next)
    {
        super( hashCode, referent, next);

        this.theKey = key;
    }

    /** 
     * the key
     * @return the key
     */    
    @Override
    public Object key()
    {
        return theKey;
    }
    
    /**
     * Get the key
     */
    public final Object theKey;
}
