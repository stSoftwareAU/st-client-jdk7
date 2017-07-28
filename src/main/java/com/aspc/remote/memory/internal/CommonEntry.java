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

import java.lang.ref.WeakReference;


/**
 *  Cache entry
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED memory management</i>
 *
 *  @author      Nigel Leck
 *  @since       3 November 2002
 */
public class CommonEntry extends WeakReference
{
    /**
     * creates a new Cache Entry
     *
     * @param referent the object
     */
    protected CommonEntry(final Object referent)
    {
        super( referent);
    }

    /**
     * Create a new entry
     * @param referent the base object
     * @param nextEntry the next entry
     * @return the new common entry
     */
    public static CommonEntry create( final Object referent, final CommonEntry nextEntry)
    {
        if( nextEntry != null)
        {
            return new CommonEntryLinked( referent, nextEntry);
        }
        else
        {
            return new CommonEntry( referent);            
        }
    }

    /**
     * Set the next entry ( must be null)
     * @param nextEntry the next entry
     */
    public void setNext(final CommonEntry nextEntry)
    {
        if( nextEntry != null)
        {
            throw new RuntimeException( "must be null");
        }
    }

    /**
     * get the next entry ( always null)
     * @return null
     */
    public CommonEntry next()
    {
        return null;
    }    
}
