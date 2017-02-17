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
package com.aspc.remote.memory.selftest.deadlock;

import com.aspc.remote.memory.MemoryHandler;
import com.aspc.remote.memory.MemoryManager;

/**
 *  Test the wrapper classes for Trans header, record and data
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED self test unit</i>
 *
 *  @author      Nigel Leck
 *  @since       27 October 2002
 */
public class ClearMemoryRunner implements Runnable
{
    /**
     * run
     */
    @Override
    public void run()
    {
        MemoryManager.clearMemory( MemoryHandler.Cost.LOW);
    }
}
