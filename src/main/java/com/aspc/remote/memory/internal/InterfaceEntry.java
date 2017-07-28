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
 *  Cache entry
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED memory management</i>
 *
 *  @author      Nigel Leck
 *  @since       17 December 2007
 */
public interface InterfaceEntry
{
    /**
     * 
     * @return the next entry
     */
    public InterfaceEntry next();

    /**
     * 
     * @param value the value
     */
    public void setData(int value);
    
    /**
     * set the next entry
     * @param next the entry
     */
    public void setNext(InterfaceEntry next);

    /**
     * 
     * @return get the object
     */
    public Object get();

    /** 
     * The last time this entry was fetched
     * @return the data
     */
    public int getData();

    /**
     * clear hard reference
     */
    public void clearHardReference();
    
    /** 
     * has this entry got a hard reference. 
     * 
     * @return true if there is a hard reference. 
     */
    public boolean hasHardReference();
}
