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

/**
 *  Collect statistics
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED memory management</i>
 *
 *  @author      Nigel Leck
 *  @since       23 February 2001, 11:39
 */
public interface StatsCollector
{
    /**
     * 
     * @param entry the entry
     */
    void examine( final InterfaceEntry entry);
}
