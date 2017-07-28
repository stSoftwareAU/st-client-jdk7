/**
 *  STS Remote library
 *
 *  Copyright (C) 2006  stSoftware Pty Ltd
 *
 *  stSoftware.com.au
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *
 *  info AT stsoftware.com.au
 *
 *  or by snail mail to:
 *
 *  stSoftware
 *  building C, level 1,
 *  14 Rodborough Rd
 *  Frenchs Forest 2086
 *  Australia.
 */
package com.aspc.remote.application;

import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.QueueLog;
import com.aspc.remote.util.misc.TimeUtil;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import org.apache.commons.logging.Log;

/**
 *  Shutdown Handle.
 *
 *  <br>
 *  <i>THREAD MODE: SINGLETON SINGLE-THREADED swing</i>
 *  @author      Nigel Leck
 *  @since       16 Nov 2010
 */
public final class Shutdown implements Runnable
{
    /**
     * cause the hook to be added.
     */
    public static void init()
    {

    }

    /**
     * don't show any information
     * @param on turn it on/off
     */
    public static void setSlient( final boolean on)
    {
        SILENT_MODE.set(on);
    }

    /**
     * are we in silent mode ?
     * @return true if silent mode
     */
    @CheckReturnValue
    public static boolean isSlient()
    {
        return SILENT_MODE.get();
    }
    
    /**
     * are we shutting down ?
     * @return true if shutting down
     */
    @CheckReturnValue    
    public static boolean isShutingDown()
    {
        return SHUTTING_DOWN.get();
    }

    /**
     * add a listener
     * @param listener the listener to be called.
     */
    public static void addListener( final @Nonnull ShutdownListener listener)
    {
        synchronized( LIST)
        {
            if( LIST.contains( listener) == false)
            {
                LIST.add( listener);
            }
        }
    }

    /**
     * exit the program 
     * @param exitStatus the status to exit with.
     */
    public static void exit( final int exitStatus)
    {
        System.exit(exitStatus);
    }

    /** run the shutdown */
    @Override
    public void run()
    {
        SHUTTING_DOWN.set(true);
        final ArrayList<ShutdownListener> copy;
        synchronized(LIST)
        {            
            copy = (ArrayList<ShutdownListener>)LIST.clone();
        }

        for(ShutdownListener listener: copy)
        {
            listener.shutdown();
        }
        
        if( SILENT_MODE.get() == false)
        {
            LOGGER.info(
                "done " + TimeUtil.getDiff(START)
            );
        }
        
        QueueLog.flush(60000);
    }

    private Shutdown()
    {
    }

    private static final ArrayList<ShutdownListener> LIST=new ArrayList<>();
    private static final AtomicBoolean SILENT_MODE=new AtomicBoolean();
    private static final long START=System.currentTimeMillis();
    private static final AtomicBoolean SHUTTING_DOWN=new AtomicBoolean();
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.application.Shutdown");//#LOGGER-NOPMD

    static
    {
        Thread shutdownThread=new Thread( new Shutdown(), "shutdown process" );
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }
}
