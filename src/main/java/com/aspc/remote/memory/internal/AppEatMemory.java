/*
 *  Copyright (c) 2000-2004 ASP Converters pty ltd
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

import com.aspc.remote.memory.MemoryManager;
import org.apache.commons.logging.Log;
import com.aspc.remote.application.AppCmdLine;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.NumUtil;
import com.aspc.remote.util.misc.TimeUtil;

/**
 *  Test the wrapper classes for Trans header, record and data
 *
 * <br>
 * <i>THREAD MODE: SINGLETON command line application SINGLE-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       27 October 2002
 */
public class AppEatMemory extends AppCmdLine
{
    private Link first = new Link();

    /** {@inheritDoc }
     * @throws Exception a serious problem
     */
    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void process() throws Exception
    {
        //new Inflater().run();
        Link last = first;
        int lastFree = 100;
        long start = System.currentTimeMillis();
        for( int loop=0; true;loop++)
        {
            Link link = new Link();
            last.next = link;
            last = link;

            if( loop % 1000 == 0)
            {
                int free = (int)( MemoryManager.getFreePercent() * 100.0);

                if( lastFree != free)
                {
                    LOGGER.info( "free " + free + "% after " + TimeUtil.getDiff(start) + " used " + NumUtil.convertMemoryToHumanReadable(MemoryManager.getTotalUsed()));
                    lastFree = free;
                }

                if( free < 40)
                {
                    break;
                }
            }
        }

        LOGGER.info( "count=" +count());

        Inflater list[] = new Inflater[16];
        for( int i = 0;i < list.length;i++)
        {
            list[i] = new Inflater();
        }
        for (Inflater list1 : list)
        {
            list1.start();
        }

        Thread.sleep(10000);
        first.next = null;
        first = null;
        Thread.sleep(10000);

        while( true)
        {
            Thread.sleep(1000);
            int free = (int)( MemoryManager.getFreePercent() * 100.0);

            if( free > 50)
            {
                LOGGER.info( "free " + free + "% after " + TimeUtil.getDiff(start) + " used " + NumUtil.convertMemoryToHumanReadable(MemoryManager.getTotalUsed()));
                break;
            }
            LOGGER.info( "sleeping after " + TimeUtil.getDiff(start) + " used " + NumUtil.convertMemoryToHumanReadable(MemoryManager.getTotalUsed()));
        }
    }
    private int count()
    {
        int c=0;
        Link last=first;
        while( last !=null)
        {
            c++;
            last = last.next;
        }

        return c;
    }

    /**
     * The main for the program
     * @param argv The command line arguments
     */
    public static void main(String argv[])
    {
        System.setProperty("DISABLE", "MEMORY_MANAGER_GC");

        new AppEatMemory().execute(argv);
    }

    class Link
    {
        Link next;
    }

    class Inflater extends Thread
    {
        Object data[];
        @Override

        @SuppressWarnings("SleepWhileInLoop")
        public void run()
        {
            while( true)
            {
                while( true)
                {
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException ex)
                    {
                        LOGGER.info( "ignore", ex);
                    }
                    int free = (int)( MemoryManager.getFreePercent() * 100.0);

                    if( free > 15)
                    {
                        break;
                    }
                }
                try
                {
                    populate();
                }
                catch( Throwable t)
                {
                    LOGGER.error( "could not populate", t);
                }

                //long size = data.length * MemoryUtil.sizeOfPointer() + data.length * MemoryUtil.sizeOf(data[0]);
                long size = MemoryUtil.sizeOf(data);
                LOGGER.info("allocated: " + NumUtil.convertMemoryToHumanReadable(size));
            }
        }

        private void populate()
        {
            data = new Object[ 4 * 1024 * 1024];
            for( int i = 0; i < data.length; i++)
            {
                data[i] = new Object();
            }
        }

    }
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.memory.internal.AppEatMemory");//#LOGGER-NOPMD
}
