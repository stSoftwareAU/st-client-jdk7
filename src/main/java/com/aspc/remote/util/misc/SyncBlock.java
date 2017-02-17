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

import com.aspc.remote.database.DataBaseError;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import org.apache.commons.logging.Log;

/**
 * A synchronized block replacement to prevent deadlocks
 * 
 * When you call the method take() on a SyncBlock object you'll block up until the specified maximum number of 
 * seconds and then an error will be thrown if you are unable to obtain the lock on this object. You must ALWAYS 
 * call release() on any sync SyncBlock that you have obtained the lock on.
 * 
 * The SyncBlock differs from the keyword synchronized in that it is interruptible and that it will timeout 
 * if it blocks for too long.
 * 
 * The SyncBlock enhances a normal java.lang.concurrent.Lock in that it will interrupt the blocking thread 
 * if it holds the lock for too long and if the thread holding the lock is not alive a new lock object will 
 * be created and a fatal email will be generated with the details. If a lock fails to be obtained ( which 
 * would have been a deadlock) a fatal email will be generated with the details of the two threads and the 
 * blocking thread will be interrupted and the calling thread will have an error thrown.
 *
 *  <br>
 * <a href="doc-files/sync_block"><H1>SyncBlock a replacement for the <B>synchronised</B> keyword </H1></a>
 * <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 * @author Nigel Leck 
 * @since 29 September 2008
 */
public class SyncBlock
{    
    /**
     * The property to set the synchronized block time. 
     */
    private static final String PROPERTY_SYNC_BLOCK_SECONDS="DIRTY_SYNC_BLOCK_SECONDS";
    
    /**
     * The block time for a synchronized block of one call.
     */
    private static final long SYNC_BLOCK_SECONDS;
    private static final AtomicLong ERROR_COUNT=new AtomicLong();
    private SyncLock syncLock = new SyncLock();
    private final String name;
    private final long blockSeconds;
    /**
     * The logger for the dirty rows
     */
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.SyncBlock");//#LOGGER-NOPMD

    /**
     * A synchronized block
     * @param name the name of the block
     */
    public SyncBlock( final String name)
    {
        this( name, 0);
    }
    
    /**
     * A synchronized block
     * @param name the name of the block
     * @param seconds the seconds
     */
    public SyncBlock( final String name, final int seconds)
    {
        this.name = name;
        if( seconds >0)
        {
            blockSeconds=seconds;
        }
        else
        {
            blockSeconds=SYNC_BLOCK_SECONDS;
        }
    }
    
    /**
     * The error count
     * @return the count
     */
    public static long errorCount()
    {
        return ERROR_COUNT.get();
    }

    /**
     * The name of this SyncBlock
     * @return the name of the block.
     */
    @Override @CheckReturnValue @Nonnull
    public String toString()
    {
        return "Block: " + name;
    }
    
    /** 
     * release the lock
     */
    public void release()
    {
        syncLock.unlock();
    }
    
    /**
     * try to get the lock
     * @return true if we got the lock
     */
    public boolean tryLock( )
    {
        return syncLock.tryLock();
    }
    
    /** 
     * Try for a few seconds to get the lock
     * @param seconds the number of seconds
     * @return true if we got the lock
     * @throws InterruptedException interrupted. 
     */
    public boolean tryLock( final int seconds) throws InterruptedException
    {
        return syncLock.tryLock(seconds, TimeUnit.SECONDS);
    }
    
    /**
     * take the lock and throw an error if you can't get it. 
     */
    public void take()
    {
        try 
        {
            SyncLock tempLock = syncLock;
            if (syncLock.tryLock(blockSeconds, TimeUnit.SECONDS) == false) 
            {
                ERROR_COUNT.incrementAndGet();
                Thread ownerThread = syncLock.getOwner();
                
                if( ownerThread == null || ownerThread.isAlive() == false)
                {
                    synchronized( this)
                    {
                        if( tempLock == syncLock)
                        {
                            syncLock = new SyncLock();
                        }
                    }

                    CLogger.fatal(LOGGER, this + " never released by " + ownerThread);
                    take();
                    return;
                }
                
                StringBuilder sb = new StringBuilder( toString());
                sb.append("\n");
                Thread currentThread = Thread.currentThread();
                
                sb.append("Failed to get lock for thread: ").append(currentThread).append("\n");

                for (StackTraceElement ste : currentThread.getStackTrace())
                {
                    sb.append("\t").append(ste).append("\n");
                }
                
                if( ownerThread != null)
                {                    
                    sb.append("\nLock held by thread: ").append(ownerThread).append("\n");

                    for (StackTraceElement ste : ownerThread.getStackTrace())
                    {
                        sb.append("\t").append(ste).append("\n");
                    }
                    sb.append("Interrupting holding thread");
                    ownerThread.interrupt();
                }
                else
                {
                    sb.append("NO OWNER THREAD found");
                }
                
                CLogger.fatal(LOGGER, sb.toString());

                throw new DataBaseError("could not get the lock on: " + name);
            }
        } 
        catch (InterruptedException ex) 
        {
            ERROR_COUNT.incrementAndGet();
            Thread.interrupted();
            CLogger.warn(LOGGER, "could not take lock on " + name, ex);
            Thread.currentThread().interrupt();
            throw new DataBaseError("could not get the lock on: " + name, ex);
        }
    }
    
    class SyncLock extends ReentrantLock
    {
        private static final long serialVersionUID = 42L;

        public SyncLock( )
        {
            super( true);
        }
        /**
         * get the owner thread
         * @return the owner thread
         */
        @Override
        public Thread getOwner()//NOPMD
        {
            return super.getOwner();
        }
    }
    
    static 
    {
        long blockTime=180;
        try
        {
            String temp = System.getProperty( PROPERTY_SYNC_BLOCK_SECONDS, "" + blockTime);
            long value = Long.parseLong(temp);
            if( value > 0) blockTime=value;
        }
        finally
        {
            SYNC_BLOCK_SECONDS = blockTime;
        }        
    }
}
