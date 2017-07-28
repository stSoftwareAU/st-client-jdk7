/*
 *  Copyright (c) 1999-2004 ASP Converters pty ltd
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
import com.aspc.remote.memory.MemoryManager;
import com.aspc.remote.util.misc.*;
import static java.lang.Thread.sleep;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import static java.lang.Thread.sleep;
import static java.lang.Thread.sleep;
import static java.lang.Thread.sleep;
import static java.lang.Thread.sleep;
import static java.lang.Thread.sleep;
import static java.lang.Thread.sleep;
import static java.lang.Thread.sleep;

/**
 *  MemoryManager runner.
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED memory management</i>
 *
 *  @author      Nigel Leck
 *  @since       21 October 2005
 */
public final class MemoryManagerRunner
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.memory.internal.MemoryManagerRunner");//#LOGGER-NOPMD
    private static MemoryManagerRunner runner;    
    
    private final MemoryManagerRunnerThread nonStopThread;
    private MemoryManagerRunner( )
    {
        nonStopThread=new MemoryManagerRunnerThread();
    }

    /**
     * set the name of the runner.
     * @param name the name
     */
    public void setName( final @Nonnull String name)
    {
        nonStopThread.setName(name);
    }    
    
    private long check(final long nextTS)
    {        
        long tmpNextTS=nextTS+1000L;
        try
        {
            MemoryManager.checkZone();
            
            MemoryManager.makeRainyDay( );

            long now = MemoryManager.tick();
            if( now<nextTS)
            {
                long sleepMS=nextTS-now;
                sleep(sleepMS);
            }
            else if( now > tmpNextTS)
            {
                tmpNextTS=now;
            }
        }
        catch( OutOfMemoryError out)
        {
            String msg="Memory Manager: Out of memory";
            MemoryManager.lastError(msg, out);
            LOGGER.fatal(
                msg,
                out
            );
        }
        catch( Throwable t)
        {
            try
            {
                String msg="Memory Manager - Error";
                MemoryManager.lastError(msg, t);
                LOGGER.fatal(
                    msg,
                    t
                );
                Thread.sleep(5 * 60 * 1000);
            }
            catch( Throwable t2)
            {
            }
        }

        return tmpNextTS;
    }
    
    /**
     * create a runner
     * @return a new runner
     */
    @Nonnull @CheckReturnValue
    public static synchronized MemoryManagerRunner create()
    {
        if( runner == null)
        {
            runner = new MemoryManagerRunner();

            runner.nonStopThread.start();
        }

        return runner;
    }    
    
    private static class MemoryManagerRunnerThread extends Thread
    {
        MemoryManagerRunnerThread() {
            super("Memory Manager: idle" );
            setDaemon(true);
        }
        
        /**
         * The main memory checker loop.
         */
        @Override
        public void run()
        {
            long nextStepTS=System.currentTimeMillis() + 1000L;
            while( true)
            {
                try
                {
                    nextStepTS=runner.check(nextStepTS);
                }
                catch( Throwable t3)
                {
                    /** NEVER EXIT LOOP NO MATTER WHAT... */
                }
            }
        }
    }
}
