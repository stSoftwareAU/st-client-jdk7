/*
 *  Copyright (c) 2002-2006 ASP Converters pty ltd
 *
 *  www.stSoftware.com.au
 *
 *  All Rights Reserved.
 *
 *  This software is the proprietary information of
 *  ASP Converters Pty Ltd.
 *  Use is subject to license terms.
 */
package com.aspc.remote.database.selftest;

import com.aspc.remote.util.misc.*;
import org.apache.commons.logging.Log;

/**
 *  Kill a thread after an amount of time.
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       1 January 2006
 */
public class ThreadKiller implements Runnable
{
    private final Thread target;
    private final long msecs;
    private final long start;//NOPMD
    private boolean cancelled;
    private final String name;
    private static ThreadKiller current;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.database.selftest.ThreadKiller");//#LOGGER-NOPMD

    /** kill a thread after a period of time
     * @param name the name
     * @param target the target
     * @param msecs the milliseconds.
     */
    public ThreadKiller(final String name, final Thread target, final long msecs)
    {
        this.target = target;
        this.name = name;
        this.msecs = msecs;

        this.start = System.currentTimeMillis();

        start();
    }

    private synchronized void start()
    {
        if (current != null)
        {
            current.cancel();
        }

        current = this;

        LOGGER.info("starting: " + name);
        ThreadPool.schedule(this);
    }

    public synchronized void cancel()
    {
        LOGGER.info("cancelled: " + name);
        cancelled = true;
        notifyAll();
    }

    @SuppressWarnings("empty-statement")
    @Override
    public void run()
    {
        synchronized (this)
        {
            try
            {
                wait(msecs);
            }
            catch (InterruptedException ie)
            {
                ;// OK

            }

            if (cancelled == false && current != this)
            {
                LOGGER.info("replaced: " + name);
                return;
            }
        }

        if (cancelled)
        {
            return;
        }
        Thread t=new Thread(() -> {
                LOGGER.warn("killing: " + name + " after " + TimeUtil.getDiff(start));

                LOGGER.info( CLogger.stackDump());
            },
            "Killer logging"
        );

        t.start();
        try
        {
            t.join( 10000);
        }
        catch (InterruptedException ex)
        {
            LOGGER.info( CLogger.stackDump());
        }
        if( ThreadUtil.isAliveOrStarting(target))
        {
            target.interrupt();

            try
            {
                target.join( 600000);
            }
            catch (InterruptedException ex)
            {
                LOGGER.warn("could not join " + target);
            }

            if( ThreadUtil.isAliveOrStarting(target))
            {
                //target.stop(new KillByError("timed out"));
                target.interrupt();
                try
                {
                    target.join( 600000);
                }
                catch (InterruptedException ex)
                {
                    LOGGER.warn("could not join " + target);
                }

                if( ThreadUtil.isAliveOrStarting(target))
                {
                    System.err.println( "Killer giving up... kill -9");
                    System.err.flush();
                    System.exit(9);
                }
            }
        }

    }
}
