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

import org.apache.commons.logging.Log;
import com.aspc.remote.memory.MemoryHandler;
import com.aspc.remote.util.misc.CLogger;
import java.util.ArrayList;

/**
 *  Run in a background thread to notify a listener
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED memory</i>
 *
 *  @author      Nigel Leck
 *  @since       1 November 2002
 */
public final class EuthanasiaManager implements Runnable
{
    /**
     * notify the objects
     */
    @Override
    public void run()
    {
        while( true)
        {
            try
            {
                MemoryHandler h = takeHandler();

                h.tidyUp();
            }
            catch( Throwable t)
            {
                LOGGER.error( "oops", t);
            }
        }
    }

    /**
     *
     * @param handler to register
     */
    public static void register( MemoryHandler handler)
    {
        if( thread == null)//NOPMD
        {
            checkUp();
        }

        synchronized( LIST)
        {
            if( LIST.contains( handler) == false)
            {
                LIST.add( handler);

                LIST.notifyAll();
            }
        }
    }

    private static synchronized void checkUp()
    {
        if( thread == null)
        {
            EuthanasiaManager runner;
            runner = new EuthanasiaManager();

            thread = new Thread( runner, "Euthanasia Manager");

            thread.setDaemon(true);
            thread.start();
        }
    }

    @SuppressWarnings("empty-statement")
    private static MemoryHandler takeHandler()
    {
        synchronized( LIST)
        {
            while( LIST.isEmpty())
            {
                try
                {
                    LIST.wait(2 * 60 * 1000);
                }
                catch( InterruptedException ie)
                {
                    ;
                }
            }

            MemoryHandler h;

            h = (MemoryHandler)LIST.remove( LIST.size() - 1);

            return h;
        }
    }

    /**
     */
    private EuthanasiaManager()
    {
    }

    private static Thread thread;//MT CHECKED
    private static final ArrayList LIST = new ArrayList();
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.memory.internal.EuthanasiaManager");//#LOGGER-NOPMD
}
