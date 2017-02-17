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

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import org.apache.commons.logging.Log;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 *  Detect Java dead locks. 
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       6th August 2008
 */
public final class ThreadDeadlockDetector
{
    private static final AtomicLong detectionCounter=new AtomicLong();
    private static ThreadDeadlockDetector detector;//MT CHECKED
    private static Timer threadCheck;//MT CHECKED
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.ThreadDeadlockDetector");//#LOGGER-NOPMD
    private static final ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
    
    /**
     * The number of milliseconds between checking for deadlocks.
     * It may be expensive to check for deadlocks, and it is not
     * critical to know so quickly.
     */
    private static final int DEFAULT_DEADLOCK_CHECK_PERIOD = 300000;

    private ThreadDeadlockDetector()
    {
        this(DEFAULT_DEADLOCK_CHECK_PERIOD);
    }

    private ThreadDeadlockDetector(int deadlockCheckPeriod)
    {
        threadCheck.schedule(
            new TimerTask()
            {
                @Override
                public void run()
                {
                    checkForDeadlocks();
                }
            },
            10,
            deadlockCheckPeriod
        );
    }

    /**
     * how many times have we detected a deadlock ?
     * @return the count
     */
    public static long detectionCount()
    {
        return detectionCounter.get();
    }

    /**
     * start the checker
     * @return the value
     */
    public static synchronized ThreadDeadlockDetector start()
    {
        if( threadCheck == null)
        {
             threadCheck = new Timer("ThreadDeadlockDetector", true);
             
             detector= new ThreadDeadlockDetector();
        }
        return detector;
    }

    /**
     *
     * @return the deadlocks
     */
    public static synchronized String checkForDeadlocks()
    {
        long[] ids = findDeadlockedThreads();
        if (ids != null && ids.length > 0)
        {
            Thread[] threads = new Thread[ids.length];
            for (int i = 0; i < threads.length; i++)
            {
                threads[i] = findMatchingThread(
                        mbean.getThreadInfo(ids[i]));
            }
            return deadlockDetected(threads);
        }

        return "";
    }

    private static long[] findDeadlockedThreads()
    {
        // JDK 1.5 only supports the findMonitorDeadlockedThreads()
//        if (mbean.isSynchronizerUsageSupported())
//        {
//            return mbean.findDeadlockedThreads();
//        }
//        else
//        {
            return mbean.findMonitorDeadlockedThreads();
//        }
    }

    private static String deadlockDetected(Thread[] threads)
    {
        detectionCounter.incrementAndGet();
        StringBuilder sb = new StringBuilder();
        sb.append("Deadlocked Threads:\n");
        sb.append("-------------------\n");
        for (Thread thread : threads)
        {
            if( thread == null) continue;
            sb.append(thread);
            sb.append("\n");
            for (StackTraceElement ste : thread.getStackTrace())
            {
                sb.append("\t").append(ste).append("\n");
            }
        }
        String str = sb.toString();
        LOGGER.fatal(str);

        return str;

    }

    private static Thread findMatchingThread(ThreadInfo inf)
    {
        for (Thread thread : Thread.getAllStackTraces().keySet())
        {
            if (thread.getId() == inf.getThreadId())
            {
                return thread;
            }
        }
        throw new IllegalStateException("Deadlocked Thread not found");
    }
}
