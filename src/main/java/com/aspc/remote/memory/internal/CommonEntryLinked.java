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
 *  Cache entry
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED memory management</i>
 *
 *  @author      Nigel Leck
 *  @since       3 November 2002
 */
public final class CommonEntryLinked extends CommonEntry
{    
    /** Privates */
    private CommonEntry nextEntry;
 
    /**
     * creates a new Cache Entry
     *
     * @param referent the object
     * @param nextEntry the next element
     */
    protected CommonEntryLinked( final Object referent, final CommonEntry nextEntry)
    {
        super( referent);

        this.nextEntry = nextEntry;
    }

    /**
     * get the next entry
     * @return the next entry
     */
    @Override
    public CommonEntry next()
    {
        return nextEntry;
    }

    /**
     * Set the next entry
     * @param nextEntry the next entry
     */
    @Override
    public void setNext(final CommonEntry nextEntry)
    {
        this.nextEntry = nextEntry;
    }
}
