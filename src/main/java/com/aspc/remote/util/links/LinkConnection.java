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
package com.aspc.remote.util.links;

import org.apache.commons.logging.Log;
import java.util.*;
import java.sql.Connection;
import com.aspc.remote.util.misc.*;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *  LinkConnection
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       19 June 1998
 */
public class LinkConnection
{
    /**
     *
     */
    protected Object    client;

    private long        checkedOutTime,
                        checkedOutCounter,
                        lastAccess,
                        maxIdle;

    private final String id;

    private boolean markedAsClosed;
    private final LinkType type;
    private final ReadWriteLock rw=new ReentrantReadWriteLock();
    
    private static final AtomicLong COUNTER=new AtomicLong();
    
    /**
     * Create a new connection
     *
     * @param lt the type. 
     * @param client The client object
     * @throws Exception a serious problem
     */
    public LinkConnection( final @Nonnull LinkType lt, final @Nonnull Object client) throws Exception
    {
        if( client == null) 
        {
            throw new IllegalArgumentException( "Must not be null client");
        }
        type = lt;
        lastAccess = System.currentTimeMillis();
        maxIdle = -1;
        this.client = client;
        id = "" + COUNTER.incrementAndGet();
    }

    /**
     * The type of this connection
     *
     * @return the type
     */
    @Nonnull @CheckReturnValue
    public LinkType getType()
    {
        return type;
    }
    
    /**
     * the number of times this connection has been checked out. 
     * @return the count.
     */
    @CheckReturnValue
    public long getCheckOutCount()
    {
        return checkedOutCounter;
    }

    /**
     * Gets the checkedOutTime
     *
     * @return date of check out
     */
    @Nullable @CheckReturnValue
    public Date getCheckedOutDate()
    {
        long tmpTime=checkedOutTime;
        if( tmpTime != 0)
        {
            return new Date( tmpTime);
        }
        
        return null;
    }

    /**
     * The maximum idle time. 
     *
     * @param secs the idle time in seconds. 
     */
    public void setMaxIdle( final long secs)
    {
        maxIdle = secs;
    }

    /**
     * If connection is available set the checked out time and return true.
     *
     * @param checkedOut the connections that where checked out. 
     * @return TRUE if you successfully checked out.
     */
    @CheckReturnValue
    public boolean checkOut(final @Nonnull ConcurrentHashMap<Object, LinkConnection> checkedOut)
    {
        if( checkedOut==null) throw new IllegalArgumentException( "checkout list is mandatory");
        long now=System.currentTimeMillis();
        Lock gate=rw.writeLock();
        gate.lock();
        try
        {
            if( markedAsClosed) return false;

            if( checkedOutTime != 0)
            {
                return false;
            }

            checkedOutTime = now;
            lastAccess = checkedOutTime;
            checkedOutCounter++;
            checkedOut.put( client, this);

            return true;
        }
        finally
        {
            gate.unlock();
        }
    }
    
    /**
     * Is this connection checked out ? 
     *
     * @return true if checked out
     */
    @CheckReturnValue
    public boolean isCheckedOut()
    {
        return checkedOutTime != 0;
    }
    
    /**
    * Notify other people that this is now back in.
    * @param checkedOut
    */
    public void checkIn(final @Nonnull ConcurrentHashMap<Object, LinkConnection> checkedOut)
    {
        long now = System.currentTimeMillis();
        Lock gate = rw.writeLock();
        gate.lock();
        try{
            if( checkedOutTime==0)
            {
                throw new IllegalStateException( this + " not checked out");
            }
            if( checkedOut.remove(client, this)==false)
            {
                throw new IllegalStateException( this + " not checked out");
            }
            lastAccess = now;
            checkedOutTime = 0;
        }
        finally
        {
            gate.unlock();
        }
    }

    /**
     * The connection ID
     *
     * @return the ID
     */
    @Nonnull @CheckReturnValue
    public String getId()
    {
        assert id!=null;
        return id;
    }

    /**
     * Gets the actual client.
     *
     * @return the client object
     */
    @Nonnull @CheckReturnValue
    public Object getClient()
    {
        assert client!=null;
        return client;
    }

    /**
     * A string version of this connection.
     *
     * @return the description
     */
    @Override @Nonnull @CheckReturnValue
    public String toString()
    {
        return "LinkConnection{" + "client=" + client + ", markedAsClosed=" + markedAsClosed + ", type=" + type + '}';
    }

    /**
     * Is this connection closed ? 
     *
     * @return TRUE if closed
     */
    @CheckReturnValue
    public boolean isClosed() {
        return markedAsClosed;
    }

    /**
     * close the connection if not already closed
     */
    public final void close()
    {
        Lock gate=rw.writeLock();
        gate.lock();
        try{
            if( markedAsClosed) return;

            markedAsClosed = true;

            if( client instanceof Connection)
            {
                Connection c=(Connection)client;
                try
                {
                    if( c.isClosed()==false)
                    {
                        try
                        {
                            if( c.getAutoCommit() == false)
                            {
                                c.rollback();
                            }
                        }
                        catch( SQLException e)
                        {
                            LOGGER.warn( "rollback before close", e);
                        }
                        finally
                        {
                            c.close();
                        }
                    }
                }
                catch( SQLException e)
                {
                    LOGGER.warn( "*** could not close connection***:" + client, e);
                }
            }
        }
        finally
        {
            gate.unlock();
        }
    }

    /**
     * Test if this connection is valid
     *
     * @throws Exception a serious problem
     */
    public void testConnection() throws Exception
    {
        Lock gate=rw.writeLock();
        gate.lock();
        try
        {
            iTestConnection();
        }
        catch( TimeoutException to)
        {
            throw to;
        }
        catch( Exception e)
        {
            LOGGER.warn( "Line test of '" + client + "' failed", e);
            throw e;
        }
        finally{
            gate.unlock();
        }
    }

    /**
     * Test if this connection is valid
     *
     * @throws Exception a serious problem
     */
    protected void iTestConnection() throws Exception
    {
        if( markedAsClosed == true)
        {
            throw new Exception( "Marked as closed '" + client + "' ");
        }

        if( maxIdle != -1)
        {
            long now;
            now = System.currentTimeMillis();

            if( now - lastAccess > maxIdle * 1000)//MT WARN: Unsynchronized
            {
                throw new TimeoutException("Max idle");
            }
        }
        
        if( client instanceof Connection)
        {
            Connection connection=(Connection)client;
            if( connection.isValid(30)==false)
            {
                throw new SQLException("connection no longer valid");
            }
        }
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.links.LinkConnection");//#LOGGER-NOPMD
}
