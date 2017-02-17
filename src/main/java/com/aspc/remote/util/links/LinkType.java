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
package com.aspc.remote.util.links;

import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.*;
import com.aspc.remote.database.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *  LinkType
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       18 June 1998
 */
public class LinkType
{

    /**
     * The code for this type
     */
    protected final String    code;

    /**
     * The minimum connections
     */
    protected int       minConnections,
                        maxConnections,
                        maxReserve;

    private long maxReuse;

    private LinkGroup linkGroup;
    
    /**
     * The list of all connection ids.
     */
    private final ConcurrentHashMap<String,LinkConnection> linkIds=new ConcurrentHashMap();

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.links.LinkType");//#LOGGER-NOPMD
    
    /**
     * Create a new connection group type
     * @param code
     */
    public LinkType( final @Nonnull String code)
    {
        if( StringUtilities.isBlank(code)) throw new IllegalArgumentException("code is mandatory");
        this.code = code;

        maxReserve = -1;
        maxConnections= 50;
    }

    @Override @CheckReturnValue @Nonnull
    public String toString() 
    {
        return "LinkType{code=" + code + ", minConnections=" + minConnections + ", maxConnections=" + maxConnections + ", maxReserve=" + maxReserve + '}';
    }
    
    /**
     * Count the number of connections in this group.
     * @return the value
     */
    @CheckReturnValue
    public int countAvailableConnections()
    {
        int count = 0;
        List<LinkConnection> listValuesList=Collections.list(linkIds.elements());
        for( LinkConnection lc:listValuesList )
        {
            if( lc.isCheckedOut() == false)
            {
                count++;
            }
        }

        return count;
    }
    
    /**
     * Close all non checked out connections.
     * @param checkedOut the checked out connections.
     * @throws java.lang.Exception a serious problem.
     */
    @CheckReturnValue @Nonnull
    public void close(final @Nonnull ConcurrentHashMap<Object, LinkConnection> checkedOut) throws Exception
    {
        for( LinkConnection lc:linkIds.values() )
        {
            if( lc.checkOut(checkedOut))
            {
                closeConnection(lc, checkedOut);                
            }
        }
    }
    
    /**
     * Get a list of this group's connection.
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public LinkConnection[] listConnections()
    {
        LinkConnection list[];
        ArrayList<LinkConnection> tmp=new ArrayList<>();
        List<LinkConnection> listValuesList=Collections.list(linkIds.elements());
        for( LinkConnection lc:listValuesList )
        {
            tmp.add(lc);
        }
        list = new LinkConnection[ tmp.size()];

        tmp.toArray( list);

        return list;
    }

    /**
     * Get first client connection.
     * @param checkedOut
     * @throws Exception a serious problem
     * @return the value
     */
    @Nonnull @CheckReturnValue
    public Object getFirstClient( final ConcurrentHashMap<Object, LinkConnection> checkedOut) throws Exception
    {
        LinkConnection lc;

        lc = getFirstConnection( checkedOut);
        assert lc!=null;
        assert lc.client!=null;
        
        return lc.client;
    }

    /**
     * Check out the connection
     * @param connection The connection to check out
     * @param checkedOut the map of clients to connections that have been checked out
     * @return TRUE if check out was successful.
     */
    @CheckReturnValue
    public boolean checkOutConnection( final @Nonnull LinkConnection connection, final @Nonnull ConcurrentHashMap<Object, LinkConnection> checkedOut)
    {
        return connection.checkOut(checkedOut);
    }

    /**
     * Finds a matching connection. If that connection is
     * already checked out then wait a bit to see if it's
     * just been tested.
     *
     * @param id
     * @param checkedOut
     * @throws Exception a serious problem
     * @return the value
     */
    @Nonnull @CheckReturnValue
    @SuppressWarnings("SleepWhileInLoop")
    public LinkConnection getMatchConnection(
        final @Nonnull String id,
        final @Nonnull ConcurrentHashMap<Object, LinkConnection> checkedOut
    ) throws Exception
    {
        LinkConnection connection;
        
        connection = linkIds.get( id);        

        if( connection == null)
        {
            throw new NoMatchException( "No such connection " + id);
        }

        long start=0;
        for( int attempts = 0; attempts < 30; attempts++)
        {
            if( connection.checkOut(checkedOut))
            {
                break;
            }
            if( start == 0)
            {
                start=System.currentTimeMillis();
            }
            if( attempts>30)
            {
                throw new LogicException(
                    "Someone has already got it (" + id + ")"
                );                
            }
            Thread.sleep((long)(1000 * Math.random()) + 50);
            
            LOGGER.warn( "re-trying checkout " + attempts + " of 30 delay: " + TimeUtil.getDiff(start));
        }

        return connection;
    }

    /**
     * Get the first available connection, otherwise create a new one.
     * @param checkedOut
     * @throws Exception a serious problem
     * @return the value
     */
    @SuppressWarnings({"SleepWhileInLoop", "UnusedAssignment", "InfiniteRecursion"})
    @Nonnull @CheckReturnValue
    public LinkConnection getFirstConnection( final @Nonnull ConcurrentHashMap<Object, LinkConnection> checkedOut) throws Exception
    {
        List<LinkConnection> listValuesList=Collections.list(linkIds.elements());
        for( LinkConnection lc:listValuesList )
        {
            if( lc.checkOut(checkedOut))
            {
                return lc;
            }
        }

        LinkConnection connection;
        connection = makeConnection();

        addConnection( connection);

        return getFirstConnection(checkedOut);
    }

    /**
     * The number of connections
     * @return the value
     */
    @CheckReturnValue @Nonnegative
    public int countConnections()
    {
        return linkIds.size();
    }

    /**
     * Generates the statistics on this link type.
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public String getStats()
    {
        String results = "";

        List<LinkConnection> listValuesList=Collections.list(linkIds.elements());
        for( LinkConnection lc:listValuesList )
        {
            String t;

            t = lc.toString();

            results += t;

            for( int j = 0; j + t.length() < 39; j++)
            {
                results += " ";
            }
            results += " ";

            if( lc.isClosed())
            {
                results += "*** CLOSED ***";
            }
            else 
            {
                Date checkedOutDate = lc.getCheckedOutDate();
                if( checkedOutDate==null)
                {
                    results += "AVAILABLE";
                }
                else
                {
                    results += TimeUtil.getDiff(
                        lc.getCheckedOutDate()
                    );
                }
            }

            results += "\n";
        }

        return results;
    }

    /**
     * This group's type code
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public String getCode()
    {
        assert code!=null;
        return code;
    }

    /**
     * The minimum number of connections the Link manager will try to maintain.
     * @param min
     * @return this
     */
    @Nonnull
    public LinkType setMinimumConnections( final @Nonnegative int min)
    {
        if( min < 0) throw new IllegalArgumentException("minimum must not be negative was: " + min);
        minConnections = min;
        
        return this;
    }

    /**
     * The group
     * @param lg group
     * @return this
     */
    @Nonnull
    public LinkType setGroup( final @Nullable LinkGroup lg)
    {
        this.linkGroup=lg;

        if( lg != null)
        {
            lg.registerLink( this);
        }
        return this;
    }

    /**
     * The maximum number of connections the Link manager will open.
     * @param max
     * @return this
     */
    @Nonnull
    public LinkType setMaximumConnections( final @Nonnegative int max)
    {
        if( max < 0) throw new IllegalArgumentException("max connections should be non-negative was: " + max);
        if( max < getMinimumConnections())
        {
            maxConnections = getMinimumConnections();
        }
        else
        {
            maxConnections = max;
        }
        
        return this;
    }

    /**
     * The maximum number of connections the Link manager will open.
     * @return the value
     */
    @CheckReturnValue @Nonnegative
    public int getMaximumConnections( )
    {
        return maxConnections;
    }

    /**
     * Returns the minimum number of connections the Link manager will try to maintain.
     * @return the value
     */
    @CheckReturnValue @Nonnegative
    public int getMinimumConnections()
    {
        return minConnections;
    }

    /**
     * Set the maximum Reserve limit. The link Manager try to maintain at least the
     * minimum Connections and no more Available connections than the maximum reserve.
     * @param max
     * @return this
     */
    @Nonnull
    public LinkType setMaximumReserve( final int max)
    {
        if( max < 0) throw new IllegalArgumentException("maximum reserve must not be negative was: " + max);
        maxReserve = max;
        
        return this;
    }

    /**
     * set the maximum reuse
     * @param max the max
     * @return this
     */
    @Nonnull
    public LinkType setMaximumReuse(final @Nonnegative long max)
    {
        if( max < 0) throw new IllegalArgumentException("Maximum reuse must not be negative was: " + max);
        maxReuse=max;
        
        return this;
    }

    /**
     *
     * @return the max reuse.
     */
    @CheckReturnValue @Nonnegative 
    public long getMaximumReuse()
    {
        return maxReuse;
    }

    /**
     * Get the maximum Reserve limit. @see setMaximumReserve
     * @return the value
     */
    @CheckReturnValue @Nonnegative 
    public int getMaximumReserve()
    {
        if( maxReserve > maxConnections && maxConnections > 0)
        {
            return maxConnections;
        }

        return maxReserve;
    }

    private int calMaximumReserve()
    {
        int tmpMaxReserve = getMaximumReserve();

        LinkGroup tmpGroup = linkGroup;

        if( tmpGroup != null)
        {
            int remainingReserve = tmpGroup.calRemainingReserve( );

            if (tmpMaxReserve < 0 || tmpMaxReserve > remainingReserve && remainingReserve >=0)
            {
                tmpMaxReserve=remainingReserve;
            }
        }

        return tmpMaxReserve;
    }

    /**
     * Tests all current connections and removes any that are not OK.
     * Once all the connections are tested create more connections if needed or
     * remove excess connections
     * @param checkout
     * @return this
     * @throws Exception a serious problem
     */
    @SuppressWarnings({"AssignmentToForLoopParameter", "BroadCatchBlock", "TooBroadCatch"})
    @Nonnull
    public LinkType testLines(final @Nonnull ConcurrentHashMap<Object, LinkConnection>    checkout ) throws Exception//NOPMD
    {
        List<LinkConnection> listValuesList=Collections.list(linkIds.elements());
        for( LinkConnection lc:listValuesList )
        {
            if( lc.checkOut(checkout))
            {
                try
                {
                    lc.testConnection();
                    lc.checkIn(checkout);
                }
                catch( TimeoutException to)
                {
                    closeConnection(lc, checkout);
                }
                catch( Throwable t)
                {
                    LOGGER.error(
                        "Connection failed " + lc,
                        t
                    );

                    closeConnection(lc, checkout);
                }
            }
        }

        /**
         * Make extra connections as needed.
         * We may have other threads creating connections so test each loop how many connections
         */
        for(
            int createCount = minConnections - countConnections();
            createCount > 0 && countConnections() < minConnections;
            createCount--
        )
        {
            LinkConnection connection = makeConnection();

            addConnection( connection);
        }

        // Remove excess connections
        int tmpMaxReserve = calMaximumReserve();
        for(
            int loop = 0;
            tmpMaxReserve               >= 0 &&                 // not -1
            countConnections()          > minConnections &&
            countAvailableConnections() > tmpMaxReserve;
            loop++
        )
        {
            boolean found = false;
            
            for( LinkConnection lc:Collections.list(linkIds.elements()) )
            {
                if( LinkManager.rawCheckOutConnection(this, lc ))
                {
                    LOGGER.info(
                        "Culling connection " + lc
                    );

                    closeConnection(lc, checkout);
                    found = true;
                    break;
                }
            }

            if( loop > 10 || found == false)
            {
                break;
            }
        }
        
        return this;
    }

    /**
     * Add a connection to this type group.
     * @param connection
     * @return this
     */
    @Nonnull
    public LinkType addConnection( final @Nonnull LinkConnection connection)
    {
        LinkType lt = connection.getType();

        if( lt.code.equals(code) == false)
        {
            throw new RuntimeException( "mixed link connections for " + connection + " should be " + this + " but was " + lt);
        }

        String key;

        key = connection.getId();

        if( linkIds.get( key) != null)
        {
            String msg="Duplicate connection added '" + key + "'";
            assert false: msg;
            LOGGER.error(msg);
            return this;
        }

        linkIds.put(
            key,
            connection
        );
        
        return this;
    }

    /**
     * Remove and close the connection
     * @param connection
     * @param checkout currently checked out.
     * @return this
     * @throws Exception a serious problem
     */
    @Nonnull
    public LinkType closeConnection( 
        final @Nonnull LinkConnection connection,        
        final @Nonnull ConcurrentHashMap<Object, LinkConnection> checkout
    ) throws Exception
    {
        // Don't close the connection if you can't remove it
        // I think it's a bug that I can't remove it but don't compound the
        // problem
        if( removeConnection( connection, checkout))
        {
            connection.close();
        }
        else
        {
            String msg="LinkType.closeConnection(" + connection + ") failed to removeConnection";
            assert false: msg;
            LOGGER.error(
                msg
            );
        }
        
        return this;
    }

    /**
     * Remove a connection from this type group.
     * @param connection
     * @param checkout currently checked out.
     * @return the value
     */
    @SuppressWarnings("AssignmentToForLoopParameter")
    public boolean removeConnection( 
        final @Nonnull LinkConnection connection, 
        final @Nonnull ConcurrentHashMap<Object, LinkConnection> checkout
    )
    {
        String key = connection.getId();

        if( linkIds.remove(key,connection))
        {
            for( Object tmpKey: checkout.keySet())//Collections.list(checkout.keys()))
            {
                LinkConnection tmpLC = checkout.get(tmpKey);
                if( tmpLC==connection)
                {
                    checkout.remove(tmpKey, connection);
                }
            }
            return true;
        }
        else
        {
            String msg="Could not remove connection '" + connection + "'";
            assert false: msg;
            LOGGER.error(
                msg,
                new Exception()
            );

            return false;
        }
    }

    /**
     * Check it a connection is contained in this type group.
     * @param connection
     * @return the value
     */
    public boolean hasConnection( final @Nonnull LinkConnection connection)
    {
        if( connection==null) throw new IllegalArgumentException("connection is mandatory");
        LinkConnection aConnection;
        String key = connection.getId();
        aConnection = linkIds.get( key);

        if(connection.equals( aConnection))
        {
            return true;
        }

        return false;
    }

    /**
     * Make a new connection
     * @throws Exception a serious problem
     * @return the value
     */
    @CheckReturnValue @Nonnull
    protected final LinkConnection makeConnection() throws Exception
    {
        throw new NoLinksException( "Don't know now to make connection '" + code + "'");
    }
}
