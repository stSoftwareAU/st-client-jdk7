/**
 *  STS Remote library
 *
 *  Copyright (C) 2006  stSoftware Pty Ltd
 *
 *  www.stsoftware.com.au
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
package com.aspc.remote.util.misc;

import com.aspc.developer.ThreadCop;
import com.aspc.remote.memory.HashMapFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import org.apache.commons.logging.Log;

/**
 *  Thread Pool
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       February 24, 2001, 3:56 PM
 */
public final class ThreadPool extends Thread
{
    /**
     * the names of the threads
     */
    private static final ThreadLocal NAMES = new ThreadLocal();

    // Constructors
    @SuppressWarnings("ValueOfIncrementOrDecrementUsed")
    private ThreadPool()
    {
        super( "Pool:" + COUNTER.incrementAndGet() );
    }

    /**
     * schedule a runner.
     * @param runner the runner to be scheduled
     */
    public static void schedule( final @Nonnull Runnable runner)
    {
        schedule( runner, null);
    }

    /**
     * make sure we have a thread to run our task
     * @return the value
     */
    @CheckReturnValue
    public static boolean threadsAvailable()
    {
        synchronized( AVAILABLE)
        {
            if( AVAILABLE.size() > 0)
            {
                return true;
            }
        }

        try
        {
            ThreadPool w = createRawThread();

            synchronized( AVAILABLE)
            {
                AVAILABLE.add( w);
            }

            return true;
        }
        catch( OutOfMemoryError oome)
        {
            LOGGER.error("Couldn't create a Thread. Sleeping...", oome);
            try
            {
                Thread.sleep((long) (1000 * Math.random()));
            }
            catch (InterruptedException ex)
            {
                LOGGER.info("Interrupted", ex);
            }

            return false;
        }
    }

    /**
     * schedule a runner.
     * @param runner the runner to be scheduled
     * @param name the name of the thread to be scheduled
     */
    public static void schedule( final @Nonnull Runnable runner, final String name)
    {
        ThreadPool w=null;

        synchronized( AVAILABLE)
        {
            int size = AVAILABLE.size();

            if( size > 0)
            {
                w = (ThreadPool)AVAILABLE.remove(size -1);
            }
        }

        if( w == null)
        {
            w = createRawThread();
        }

        w.putRunner( runner, name);
    }

    /**
     * Wait for a scheduled runner.
     *
     * @param runner the runner to wait for.
     *
     */
    public static void waitFor( final @Nonnull Runnable runner)
    {
        waitFor( runner, 0);
    }

    /**
     * Wait for a scheduled runner.
     *
     * @param runner the runner to wait for.
     * @param timeout the maximum time to wait in milliseconds.
     *
     */
    public static void waitFor( final @Nonnull Runnable runner, final long timeout)
    {
        try
        {
            long startTS = 0;

            synchronized( RUNNERS)
            {
                while( RUNNERS.get(runner) != null)
                {
                    long tempTimeout = timeout;

                    if( timeout != 0)
                    {
                        long currentTS = System.currentTimeMillis();
                        if( startTS == 0)
                        {
                            startTS =currentTS;
                        }
                        else
                        {
                            tempTimeout = timeout - ( currentTS - startTS);

                            if( tempTimeout <= 0) break;
                        }
                    }

                    RUNNERS.wait(tempTimeout);
                }
            }
        }
        catch( InterruptedException ie)
        {
            // remember the interrupt
            Thread.currentThread().interrupt();
        }
    }

    /**
     * is the scheduled runner alive ?
     * @param runner the runner to check
     * @return is alive
     */
    @CheckReturnValue
    public static boolean isAlive( final @Nonnull Runnable runner)
    {
        synchronized( RUNNERS)
        {
            Thread thread = (Thread)RUNNERS.get(runner);

            if( thread == null) return false;

            return thread.isAlive();
        }
    }

    /**
     * What is the scheduled runner current name ?
     * @param runner the runner to check
     * @return the name
     */
    @CheckReturnValue
    public static String getName( final @Nonnull Runnable runner)
    {
        synchronized( RUNNERS)
        {
            Thread thread = (Thread)RUNNERS.get(runner);

            if( thread == null) return "completed";

            return thread.getName();
        }
    }

    /**
     * record the state of the thread before we start.
     */
    public static void recordState()
    {
        Thread thread = Thread.currentThread();

        String name = thread.getName();

        NAMES.set( name);
    }

    /**
     * add a thread purifier
     * @param purifier a purifier to be called.
     */
    public static synchronized void addPurifier( final @Nonnull ThreadPurifier purifier)
    {
        PURIFIER_MAP.put( purifier, "");

        ThreadPurifier list[] = new ThreadPurifier[ PURIFIER_MAP.size()];

        PURIFIER_MAP.keySet().toArray( list);

        purifierList = list;
    }

    /**
     * Purify the thread before placing back in the pool
     * @return check that the thread is clean.
     */
    public static boolean purify()
    {
        Version.purifyThreadVersion();
        ThreadPurifier list[] = purifierList;

        if( list != null)
        {
            for (ThreadPurifier tp : list) 
            {
                tp.purifyThread();
            }
        }

        Thread thread = Thread.currentThread();

        String name = (String)NAMES.get();

        if( name != null)
        {
            thread.setName( name);
            NAMES.set( null);
        }

        boolean cleanRun=interrupted()==false;
        
        return cleanRun;            
    }

    /**
     * run the scheduled runner.
     */
    @Override
    public void run()
    {
        while( true)
        {
            Runnable runner = getRunner();

            recordState();
            
            boolean cleanRun=true;
            try
            {
                if( StringUtilities.isBlank( theName) == false)
                {
                    setName( theName);
                }

                // Clear any interrupts from previous runs.
                if( Thread.interrupted())
                {
                    cleanRun=false;
                    LOGGER.warn("interrupted before starting, just clear and ignore");
                }
                runner.run();
            }
            catch( Throwable t)
            {
                cleanRun=false;
                LOGGER.error( "Pool Runner", t);
            }
            finally
            {
                synchronized( RUNNERS)
                {
                    RUNNERS.remove(theRunner);
                    RUNNERS.notifyAll();
                }
            }

            cleanRun &=purify();

            theRunner = null;
            theName = null;

            if( cleanRun)
            {
                synchronized( AVAILABLE)
                {
                    if( AVAILABLE.size() >= MAX_RESERVE)
                    {
                        break;
                    }

                    AVAILABLE.add( this);
                }
            }
            else
            {
                LOGGER.warn("interrupted runner, ending the thread pool thread.");
                break;
            }
        }
    }

    /**
     * get the stats
     * @return the stats
     */
    @CheckReturnValue @Nonnull
    public static String getStats()
    {
        String results = "";

        for( int i=0; true; i++)
        {
            Thread thread = null;
            synchronized( AVAILABLE)
            {
                if( i < AVAILABLE.size())
                {
                    thread = (Thread)AVAILABLE.get( i);
                }
            }

            if( thread == null) break;
            results += thread;
            results += "(" + thread.hashCode() + ")";
            results += "\n";

        }

        return results;
    }

    // Privates
    @SuppressWarnings("empty-statement")
    private synchronized Runnable getRunner()
    {
        while( theRunner == null)
        {
            try
            {
                wait( 60000);
            }
            catch( InterruptedException ie)
            {
                ;
            }
        }

        return theRunner;
    }

    private synchronized void putRunner( final @Nonnull Runnable runner, String name)
    {
        synchronized( RUNNERS)
        {
            RUNNERS.put(runner, this);
            RUNNERS.notifyAll();
        }

        theRunner = runner;
        theName = name;

        notifyAll();
    }

    @CheckReturnValue
    private static ThreadPool createRawThread()
    {
        try
        {
            ThreadPool thread = new ThreadPool();
            thread.setDaemon( true );
            thread.start();

            return thread;
        }
        catch( OutOfMemoryError oome)
        {
            LOGGER.fatal("Could not create a new thread", oome);
            //JDK5-START
            StringBuilder sb = new StringBuilder();
            sb.append("Current Threads:\n");
            sb.append("----------------\n");
            for (Thread thread : Thread.getAllStackTraces().keySet())
            {
                sb.append(thread);
                sb.append("\n");
                StackTraceElement[] stackTrace = thread.getStackTrace();
                for (int i=0; stackTrace != null && i < stackTrace.length; i++)
                {
                    StackTraceElement ste = stackTrace[i];
                    sb.append("\t").append(ste).append("\n");
                }
            }
            LOGGER.error(sb.toString());
            //JDK5-END
            throw oome;
        }
    }

    private Runnable theRunner;
    private String theName;

    //Globals
    private static final HashMap PURIFIER_MAP = HashMapFactory.create();
    private static final WeakHashMap RUNNERS = new WeakHashMap();
    private static ThreadPurifier purifierList[];//MT CHECKED
    private static final ArrayList<Thread> AVAILABLE = new ArrayList();
    private static final AtomicLong COUNTER=new AtomicLong();
    private static final int MAX_RESERVE=10;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.ThreadPool");//#LOGGER-NOPMD
    static
    {
        assert ThreadCop.monitor(PURIFIER_MAP, ThreadCop.MODE.EXTERNAL_SYNCHRONIZED);
    }
}
