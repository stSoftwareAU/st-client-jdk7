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

import java.lang.ref.WeakReference;
import javax.annotation.CheckReturnValue;


/**
 *  Cache entry
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED memory management</i>
 *
 *  @author      Nigel Leck
 *  @since       3 November 2002
 */
public abstract class CacheEntry extends WeakReference implements InterfaceEntry
{
    /**
     * creates a new Cache Entry
     *
     * @param hash The hash id
     * @param referent the object
     * @param next the next element
     */
    public CacheEntry(final int hash, final Object referent, final InterfaceEntry next)
    {
        super( referent);

        this.hash = hash;
        this.nextEntry = next;
        hardReference = referent;
    }

    /**
     * the next entry
     */
    public InterfaceEntry nextEntry;

    /**
     * the hash
     */
    public final int hash;

    /** The last time this entry was fetched */
    public int data;
    
    /**
     * set the data
     * @param temp the value
     */
    @Override
    public void setData( final int temp)
    {
        data = temp;
    }
    
    /** Don't allow the object pointed to by this entry to be deallocated */
    public Object hardReference;
    
    /**
     * clear the hard reference
     */
    @Override
    public void clearHardReference()
    {
        hardReference=null;
    }
    
    /** 
     * has this entry got a hard reference. 
     * 
     * @return true if there is a hard reference. 
     */
    @Override
    public boolean hasHardReference()
    {
        return hardReference != null;
    }
    
    /** 
     * set the next entry
     * @param entry the next entry
     */
    @Override
    public void setNext( final InterfaceEntry entry)
    {
        nextEntry = entry;
    }
    
    /**
     * 
     * @return the next one
     */
    @Override
    public InterfaceEntry next() 
    {
        return nextEntry;
    }

    /** 
     * 
     * @return the hash code
     */
    @Override @CheckReturnValue
    public int hashCode() 
    {
        return hash;
    }

    /**
     * 
     * @param obj the object to check
     * @return true if equals
     */
    @Override @CheckReturnValue
    public boolean equals(final Object obj) 
    {
        if (obj == null) 
        {
            return false;
        }
        if (getClass() != obj.getClass()) 
        {
            return false;
        }
        
        final CacheEntry other = (CacheEntry) obj;
        if (this.get() != other.get() ) 
        {
            return false;
        }
        
        if (this.hash != other.hash) 
        {
            return false;
        }

        return true;
    }

    /**
     * 
     * @return the data
     */
    @Override
    public int getData() 
    {
        return data;
    }
}
