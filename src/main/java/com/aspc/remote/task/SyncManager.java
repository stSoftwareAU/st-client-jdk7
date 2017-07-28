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
package com.aspc.remote.task;

import com.aspc.remote.jdbc.Executor;
import com.aspc.remote.jdbc.SoapResultSet;
import com.aspc.remote.soap.internal.ClientFactory;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.ThreadUtil;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import org.apache.commons.pool.PoolableObjectFactory;

/**
 * SyncManager looks after the session life cycle for a task, and will call the
 * <code>handleTask</code> method of the <code>TaskHandler</code> when it gets
 * a transaction record from TASK NEXT.
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 * @author      luke
 * @since       28 September 2005
 */
public class SyncManager extends TaskManager implements Runnable
{
    /**
     * Creates a new SyncManager. It uses the hosts String to create a <code>ClientFactory</code>
     * @param taskCode the code for this task
     * @param hosts the hosts string used to make new Client objects by ClientFactory
     * @param handler the task handler that will be called when a task is ready to be processed
     */
    public SyncManager( final String taskCode, final String hosts, final TaskHandler handler )
    {
        this( taskCode, new ClientFactory( hosts ), handler );
    }

    /**
     * Creates a new SyncManager.
     * @param taskCode the code for this task
     * @param factory the factory used by the internal ExecutorPool for Executor objects
     * @param handler the task handler that will be called when a task is ready to be processed
     */
    public SyncManager( final String taskCode, final PoolableObjectFactory factory, final TaskHandler handler )
    {
        super( taskCode, factory );
        this.handler = handler;
        this.timeout = 0L;
        this.killed = false;
    }

    /**
     * The main task processing loop for the sync manager
     */
    @Override
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public synchronized void run()
    {
        try
        {
            while( true )
            {
                if( killed) break;

                while( taskStart() == false )
                {
                    if( killed) break;

                    wait( 5000L ); // 5 seconds
                }

                Executor client = null;

                try
                {
                    client = pool.borrow();

                    handler.beginTask( this, client, getParameters() );
                }
                catch( Exception e )
                {
                    LOGGER.error("an error occur." , e);
                    taskEnd(); // go back to sleep.
                    wait( 5000L ); // 5 seconds
                    continue;
                }
                finally
                {
                    pool.release( client );
                    client=null;
                }

                if( killed) break;

                boolean firstLoop = true;
                while(  alive() )
                {
                    if( firstLoop == false)
                    {
                        wait( timeout );
                        if( killed) break;
                    }
                    firstLoop = false;

                    while( taskNext() )
                    {
                        if( killed) break;

                        if(handler instanceof TaskHandlerV2)
                        {
                            boolean doNotStop = ((TaskHandlerV2) handler).preHandleTask(transid, trans_ms);
                            if (doNotStop == false)
                            {
                                break;
                            }
                        }

                        SoapResultSet srs = null;

                        try
                        {
                            client = pool.borrow();

                            srs = client.fetch( "TRANSACTION LIST "+transid );

                            if(handler instanceof TaskHandlerV2)
                            {
                                ((TaskHandlerV2)handler).handleTaskV2( client, transid, srs, trans_ms);
                            }
                            else
                            {
                                handler.handleTask( client, transid, srs );
                            }

                            if( taskBeat() == false )
                            {
                                break; // go back to sleep
                            }
                        }
                        catch( Exception e )
                        {
                            LOGGER.error("an error occur." , e);
                            try
                            {
                                String msg = "An error occurred processing transaction '" + transid + "'. Error:" + e;
                                if( taskError( msg) == false )
                                {
                                    break; // go back to sleep
                                }

                            }
                            catch( Exception le)
                            {
                                LOGGER.error("Could not update task with details of error." , le);
                            }
                        }
                        finally
                        {
                            if( srs != null )
                            {
                                try
                                {
                                    srs.close();
                                }
                                catch( SQLException sqle )
                                {
                                    LOGGER.error( "could not close the result set", sqle );
                                        //if we can't close, probably already closed
                                        //just ignore and continue
                                }
                            }
                            pool.release( client );
                        }
                    }
                }
            }
        }
        catch( Throwable death )
        {
            LOGGER.fatal( "Thread death for RemoteApp: ", death );
        }

        try
        {
            taskEnd();
        }
        catch( Exception e )
        {
            LOGGER.warn(
                "we are at the end for this task, if an error is thrown here," +
                 "then we will have to wait for the task time out to kill it",
                e
            );
        }

        notifyAll();
    }

    /**
     * Starts the main processing loop for the sync manager, created in a new thread.
     * Only one main loop thread will be alive at a time. If a main loop thread already exists
     * and is alive, this call does nothing.
     */
    public synchronized void listen()
    {
        if( ThreadUtil.isAliveOrStarting(t) == false )
        {
            t = new Thread( this, "SyncManager task code: "+task );
            t.setDaemon( true );
            t.start();
        }
    }

    /**
     * Calls notify on this object, to wake up the main loop if it is in <code>wait</code>
     */
    public synchronized void wakeUp()
    {
        this.notifyAll();
    }

    /**
     * Gets the timeout that is used when the main loop goes to sleep
     * @return long the current timeout value
     */
    public long getTimeout()
    {
        return timeout;
    }

    /**
     * Sets the timeout value that is used when the main loop goes to sleep
     * negative values will be silently ignored, and the timeout will not change
     * @param milliseconds the new timeout in milliseconds
     */
    public void setTimeout( final long milliseconds )
    {
        if( milliseconds >= 0L )
        {
            timeout = milliseconds;
        }
    }

    /**
     * Checks to see if this sync manager is listening
     * @return boolean true if this task manager is listening
     */
    public boolean listening()
    {
        boolean isListening = false;

        if( t != null && t.isAlive() == true )//MT WARN: Inconsistent synchronization
        {
            isListening = true;
        }

        return isListening;
    }

    /**
     * Sets a flag indicating that the sync manager should stop.
     */
    @SuppressWarnings("empty-statement")
    public void kill()
    {
        killed = true;//MT WARN: Inconsistent synchronization

        synchronized( this)
        {
            notifyAll();

            long end = System.currentTimeMillis() + timeout * 2;

            while( alive())
            {
                if( end < System.currentTimeMillis())
                {
                    try
                    {
                        wait( 10000);
                    }
                    catch( InterruptedException ie)
                    {
                        ;// Don't care
                    }
                }
                else
                {
                    LOGGER.error( "Failed to kill SyncManager");
                    break;
                }
            }
        }
    }

    /**
     * TaskHandler instance that will be called to process a task
     */
    protected TaskHandler handler;

    private Thread t;
    private long timeout;
    private boolean killed;

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.task.SyncManager");//#LOGGER-NOPMD
}
