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

import com.aspc.remote.memory.MemoryHandler.Cost;
import org.apache.commons.logging.Log;
import com.aspc.remote.memory.MemoryListener;
import com.aspc.remote.memory.MemoryManager;
import com.aspc.remote.util.misc.CLogger;
import java.lang.ref.WeakReference;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nonnull;

/**
 *  Run in a background thread to notify a listener
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED clear of memory listeners</i>
 *
 *  @author      Nigel Leck
 *  @since       1 November 2002
 */
public class ListenerRunner implements Runnable
{
    /**
     * Creates a runner to notify the clearing of memory levels
     *
     * @param cost the cost
     * @param listeners the objects to notify
     */
    public ListenerRunner(final @Nonnull Cost cost, final @Nonnull CopyOnWriteArrayList<WeakReference<MemoryListener>> listeners)
    {
        this.cost = cost;
        this.listeners = listeners;
    }

    /**
     * notify the objects
     */
    @Override
    public void run()
    {
        try
        {
            for( WeakReference<MemoryListener> wr: listeners)
            {
                MemoryListener l;

                l = wr.get();

                if( l != null)
                {
                    try
                    {
                        l.clearedMemory(cost);
                    }
                    catch( Exception e)
                    {
                        String msg=l + ": notify memory clear";
                        MemoryManager.lastError(msg, e);
                        LOGGER.error( msg, e);                        
                    }
                }
                else
                {
                    listeners.remove( wr);
                }
            }
        }
        catch( Throwable t)
        {
            String msg="notify memory clear";
            MemoryManager.lastError(msg, t);
            LOGGER.error( msg, t);
        }
    }

    private final Cost cost;
    private final CopyOnWriteArrayList<WeakReference<MemoryListener>> listeners;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.memory.internal.ListenerRunner");//#LOGGER-NOPMD
}
