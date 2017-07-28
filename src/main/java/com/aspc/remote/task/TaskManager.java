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

import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.StringUtilities;
import com.aspc.remote.jdbc.Executor;
import com.aspc.remote.jdbc.ExecutorPool;
import com.aspc.remote.jdbc.SoapResultSet;
import com.aspc.remote.util.misc.TimeUtil;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import org.apache.commons.pool.PoolableObjectFactory;

/**
 * TaskManager will manage a session for a Task, by using the TASK commands
 * START, NEXT and BEAT.
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 * @author      luke
 * @since       28 September 2005
 */
public class TaskManager
{
    private static final ThreadLocal<TaskManager>CURRENT=new ThreadLocal();

    /**
     * Creates a new TaskManager for the given task code, using the factory to create
     * an ExecutorPool
     * @param taskCode the task code for the app
     * @param factory an Executor object factory for the pool
     */
    public TaskManager( final String taskCode, final PoolableObjectFactory factory )
    {
        this.pool = new ExecutorPool( factory );
        this.task = taskCode;
        this.session = "";
        this.mode = "";
        this.transid = "";
        this.livemode = false;
    }

    /**
     * Calls TASK START for the task given in the constructor, stores the resulting
     * session and mode for use in TASK NEXT and TASK BEAT
     * @return boolean true if we got a session number and mode is live
     * @throws Exception a serious problem
     */
    public boolean taskStart() throws Exception
    {
        boolean gotSession = false;

        Executor executor = null;

        SoapResultSet srs = null;

        try
        {
            executor = pool.borrow();
            srs = executor.fetch( "TASK START "+task );
            if( srs.next() )
            {
                session = srs.getString( "session_id" );
                mode = srs.getString( "mode" );
                parameters = srs.getString( "parameters");
                if( StringUtilities.isBlank( session ) == false && mode.equals( TaskMode.LIVE_MODE ) )
                {
                    gotSession = true;

                    String afterMessage = "";

                    if( standByTime != 0)
                    {
                        afterMessage = " after " + TimeUtil.getDiff( standByTime);
                        standByTime = 0;
                    }
                    LOGGER.info( "starting " + task + afterMessage);
                }
                else
                {
                    LOGGER.debug( "Task '" + task + "' cannot be started. Mode=" + mode);
                }
            }
            else
            {
                throw new Exception( "No results returned from TASK START "+task );
            }
        }
        catch( Exception e )
        {
            // failure here is indicated by the false return value
            LOGGER.error( "Failed to start task '" + task + "'", e);
        }
        finally
        {
            if( srs != null )
            {
                try
                {
                    srs.close();
                }
                catch( Exception e )
                {
                    // We can continue from this error.
                    LOGGER.warn( "could not close result set", e);
                }
            }

            pool.release( executor );
        }

        livemode = gotSession;

        if( livemode == false)
        {
            if( standByTime == 0)
            {
                standByTime = System.currentTimeMillis();
            }

            LOGGER.info( "standing by " + task + " for " + TimeUtil.getDiff( standByTime));
        }

        if( livemode)
        {
            CURRENT.set(this);
        }

        return gotSession;
    }

    /**
     * Calls TASK NEXT using the session from taskStart for this task.
     * @return boolean true if we get a transaction id and mode is live
     * @throws Exception a serious problem
     */
    @SuppressWarnings("empty-statement")
    public boolean taskNext() throws Exception
    {
        boolean gotTask = false;

        Executor executor = null;

        SoapResultSet srs = null;

        try
        {
            executor = pool.borrow();
            srs = executor.fetch( "TASK SESSION "+session+" NEXT "+task );

            if( srs.next() )
            {
                transid = srs.getString( "trans_id" );
                mode = srs.getString( "mode" );
                trans_ms = srs.getLong("trans_ms");

                if( StringUtilities.isBlank( transid ) == false && mode.equals( TaskMode.LIVE_MODE ) )
                {
                    gotTask = true;
                }
            }
        }
        catch( Exception e )
        {
            // failure here is indicated by the false return value
            LOGGER.info( "Failed to get next task", e);
        }
        finally
        {
            if( srs != null )
            {
                try
                {
                    srs.close();
                }
                catch( Exception e )
                {
                    ; // must be already closed
                }
            }
            pool.release( executor );
        }

        // we might not have tasks yet still have a session
        if( mode.equals( TaskMode.LIVE_MODE ) )
        {
            livemode = true;
            CURRENT.set(this);
        }
        else
        {
            CURRENT.remove();
            livemode = false;
        }

        return gotTask;
    }

    /**
     * do a incremental beat on the current task manager.
     * @return true if we are still alive.
     */
    @SuppressWarnings("empty-statement")
    public static boolean incrementalBeat()
    {
        TaskManager tm = CURRENT.get();
        if( tm == null)
        {
            LOGGER.warn( "NO current task manager can not BEAT");

            return false;
        }
        else
        {
            Executor executor = null;

            SoapResultSet srs = null;
            boolean alive=false;
            @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
            String cmd= "TASK SESSION "+tm.session + " BEAT " + tm.task ;
            try
            {
                executor = tm.pool.borrow();

                srs = executor.fetch( cmd);

                if( srs.next() )
                {

                    String mode = srs.getString( "mode" );
                    assert mode!=null;
                    if( mode.equals( TaskMode.LIVE_MODE ) )
                    {
                        alive = true;
                    }
                }
            }
            catch( Exception ignore )
            {
                LOGGER.error( cmd, ignore);
                 // failure here is indicated by the false return value
            }
            finally
            {
                if( srs != null )
                {
                    try
                    {
                        srs.close();
                    }
                    catch( Exception e )
                    {
                        ; // must be already closed
                    }
                }

                tm.pool.release( executor );
            }

            if(alive ==false)
            {
                CURRENT.remove();
            }

            return alive;
        }
    }

    /**
     * Calls TASK BEAT using the session id and the transaction id from TASK NEXT
     * @return boolean true if mode is live
     */
    @SuppressWarnings("empty-statement")
    public boolean taskBeat()
    {
        boolean alive = false;

        Executor executor = null;

        SoapResultSet srs = null;

        try
        {
            executor = pool.borrow();
            String temp = "";
            if( StringUtilities.isBlank( transid) == false)
            {
                temp = "," + transid;
            }
            srs = executor.fetch( "TASK SESSION "+session+" BEAT " + task + temp );

            if( srs.next() )
            {
                mode = srs.getString( "mode" );

                if( mode.equals( TaskMode.LIVE_MODE ) )
                {
                    alive = true;
                }
            }
        }
        catch( Exception ignore )
        {
            LOGGER.error( "ERROR:TASK SESSION "+session+" BEAT "+task+","+transid, ignore);
             // failure here is indicated by the false return value
        }
        finally
        {
            if( srs != null )
            {
                try
                {
                    srs.close();
                }
                catch( Exception e )
                {
                    ; // must be already closed
                }
            }

            pool.release( executor );
        }

        livemode = alive;
        if( alive)
        {
            CURRENT.set(this);
        }
        else
        {
            CURRENT.remove();
        }
        return alive;
    }
    /**
     * Calls TASK SESSION using the session id and the transaction id from TASK NEXT
     * @return String session if mode is live
     */
    @SuppressWarnings("empty-statement")
    public String taskSession()
    {
        boolean alive = false;

        Executor executor = null;

        SoapResultSet srs = null;

        try
        {
            executor = pool.borrow();
            String temp = "";
            if( StringUtilities.isBlank( transid) == false)
            {
                temp = "," + transid;
            }
            srs = executor.fetch( "TASK SESSION "+session+" BEAT " + task + temp );

            if( srs.next() )
            {
                mode = srs.getString( "mode" );

                if( mode.equals( TaskMode.LIVE_MODE ) )
                {
                    alive = true;
                }
            }
        }
        catch( Exception ignore )
        {
            LOGGER.error( "ERROR:TASK SESSION "+session+" BEAT "+task+","+transid, ignore);
             // failure here is indicated by the false return value
        }
        finally
        {
            if( srs != null )
            {
                try
                {
                    srs.close();
                }
                catch( Exception e )
                {
                    ; // must be already closed
                }
            }

            pool.release( executor );
        }

        livemode = alive;

        if (alive)
        {
            CURRENT.set(this);
            return session;
        }
        else
        {
            CURRENT.remove();
            return null;
        }
    }

    /**
     * Calls TASK END for this session, end the session and allow another session to start
     * @throws Exception a serious problem
     */
    public void taskEnd() throws Exception
    {
        Executor executor = null;

        try
        {
            executor = pool.borrow();
            executor.execute( "TASK SESSION "+session+" END "+task );
        }
        catch( Exception e )
        {
            LOGGER.warn(
                "this is the end of the task lifecycle, there is no reason to recover from failure here." +
                " also, the session will time out without this call",
                e
            );
        }
        finally
        {
            pool.release( executor );
        }

        session = "";
        mode = "";
        livemode = false;
        CURRENT.remove();
    }

    /**
     * Returns true if this task manager is currently live
     * @return boolean true if the task manager is alive
     */
    public boolean alive()
    {
        return livemode;
    }

    /**
     * Calls TASK ERROR using the session id and the transaction id from TASK NEXT
     * @return boolean true if mode is live
     * @param msg the message
     * @throws Exception a serious problem
     */
    @SuppressWarnings("empty-statement")
    public boolean taskError( String msg) throws Exception
    {
        boolean alive = false;

        Executor executor = null;

        SoapResultSet srs = null;

        String encMsg = msg.replace( "\\", "\\\\");
        encMsg = encMsg.replace( "'", "\\'");

        String cmd = "TASK SESSION "+session+" ERROR '"+task+"','"+encMsg+"'";

        try
        {
            executor = pool.borrow();

            srs = executor.fetch( cmd );

            if( srs.next() )
            {
                mode = srs.getString( "mode" );

                if( mode.equals( TaskMode.LIVE_MODE ) )
                {
                    alive = true;
                }
            }
        }
        catch( Exception ignore )
        {
            LOGGER.error( "ERROR:" + cmd, ignore);
             // failure here is indicated by the false return value
        }
        finally
        {
            if( srs != null )
            {
                try
                {
                    srs.close();
                }
                catch( Exception e )
                {
                    ; // must be already closed
                }
            }

            pool.release( executor );
        }

        livemode = alive;
        if( alive)
        {
            CURRENT.set(this);
        }
        else
        {
            CURRENT.remove();
        }
        return alive;
    }

    /**
     * get the parameters
     * @return the parameters
     */
    @CheckReturnValue @Nonnull
    public String getParameters()
    {
        return parameters;
    }

    private String parameters;
    private String session;
    private String mode;
    private boolean livemode;

    private long standByTime;

    /**
     * The Task Code for this task
     */
    protected String task;

    /**
     * Executor object pool for this task
     */
    protected ExecutorPool pool;

    /**
     * The current Transaction ID for this task
     */
    protected String transid;
    protected Long trans_ms;

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.task.TaskManager");//#LOGGER-NOPMD
}
