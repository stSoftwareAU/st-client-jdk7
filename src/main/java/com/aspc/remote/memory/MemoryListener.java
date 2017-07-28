/*
 *  Copyright (c) 1998-2004 ASP Converters pty ltd
 *
 *  www.stSoftware.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.memory;

import com.aspc.remote.memory.MemoryHandler.Cost;
import javax.annotation.Nonnull;

/**
 *  Will be notified after the memory has been cleared
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED </i>
 *
 *  @author      Nigel Leck
 *  @since       27 October 2002
 */
public interface MemoryListener
{

    /**
     * Memory for this level ( maximum level) has been cleared. The available free memory is now within
     * tolerance.
     *
     * @param cost The level that was cleared
     */
    void clearedMemory( final @Nonnull Cost cost);
}
