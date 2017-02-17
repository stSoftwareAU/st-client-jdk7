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
package com.aspc.remote.memory.internal;//NOPMD

import javax.annotation.CheckReturnValue;

/**
 *  locked entry
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED memory management</i>
 *
 *  @author      Nigel Leck
 *  @since       18 December 2007
 */
public abstract class LockedEntry implements InterfaceEntry
{
    private InterfaceEntry nextEntry;

    private final int hash;

    /** The last time this entry was fetched */
    private int data;
    private final Object hardReference;

    /**
     * 
     * @param temp the data
     */
    @Override
    public void setData( final int temp)
    {
        data = temp;
    }
    
    /**
     * creates a new Cache Entry
     *
     * @param hash The hash id
     * @param referent the object
     * @param next the next element
     */
    public LockedEntry(final int hash, final Object referent, final InterfaceEntry next)
    {
        this.hash = hash;
        this.nextEntry = next;
        hardReference = referent;
    }
   
    /**
     * 
     * @return the object
     */
    @Override
    public Object get()
    {
        return hardReference;
    }
    
    /**
     * clear the reference
     */
    @SuppressWarnings("empty-statement")
    @Override
    public void clearHardReference()
    {
        ;// don't clear on a locked entry
    }
    
    /** 
     * has this entry got a hard reference. 
     * 
     * @return true if there is a hard reference. 
     */
    @Override
    public boolean hasHardReference()
    {
        return true;
    }
    
    /** 
     * the next entry
     * @param entry the entry
     */
    @Override
    public void setNext( final InterfaceEntry entry)
    {
        nextEntry = entry;
    }
    
    /** 
     * 
     * @return the next entry
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
     * @return true if equal
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
        
        final LockedEntry other = (LockedEntry) obj;
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
