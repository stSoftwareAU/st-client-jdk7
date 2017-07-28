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

import com.aspc.remote.application.Shutdown;
import com.aspc.remote.application.ShutdownListener;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.TimeUtil;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;

/**
 *  LinkManager
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       14 May 1998
 */
public final class LinkManager
{
    private static final AtomicBoolean SHUTTING_DOWN=new AtomicBoolean(false);
    private static final long MAX_CHECK_TIME = 10L * 60L * 1000L;

    /**
     * Gets the link type by key
     * @param key The key
     * @return the value
     */
    @CheckReturnValue @Nullable
    public static LinkType getType( final @Nonnull String key)
    {
        return TYPES.get(key);
    }

    /**
     * Does this type exist
     * @param key The key
     * @return the value
     */
    @CheckReturnValue
    public static boolean hasType( final @Nonnull String key)
    {
        return TYPES.containsKey(key);
    }

    /**
     * Removes this type and closes all connections.
     * @param key The key
     */
    public static void killType( final @Nonnull String key)
    {
        LinkType lt;

        lt = TYPES.remove(key);

        if( lt == null)
        {
            LOGGER.debug("killType( " + key + ") not found");
            return;
        }

        LinkConnection list[];
        list = lt.listConnections();

        for (LinkConnection c : list) 
        {
            try {
                c.close();
            }
            catch( Exception e)
            {
                LOGGER.error("LinkManager.removeType(" + key + ")", e);
            }
        }
    }
    
    /**
     * closes all connections of this type
     * @param key The key
     * @throws java.lang.Exception a serious problem.
     */
    public static void close( final @Nonnull String key) throws Exception
    {
        LinkType lt;

        lt = TYPES.get(key);

        if( lt == null)
        {
            throw new IllegalArgumentException("no such type: " + key);
        }

        lt.close( CHECKEDOUT);
    }


    /**
     * Get statistics on each link
     * @throws Exception a serious problem
     * @return the value
     */
    @CheckReturnValue @Nonnull    
    public static String getStats() throws Exception
    {
        checkUp();
        String result = "";

        for (String key : TYPES.keySet()) 
        {
            result += key;
            LinkType lt;
            lt = TYPES.get(key);
            StringTokenizer st = new StringTokenizer(
                lt.getStats(),
                "\n"
            );
            while( st.hasMoreTokens())
            {
                result += "\n    " + st.nextToken();
            }
            result += "\n";
        }

        return result;
    }

    /**
     * Adds a link type group
     * @param lt the type
     * @return 
     */
    @Nonnull
    public static LinkType addType( final @Nonnull LinkType lt)
    {
        checkUp();

        String key = lt.getCode();
        LinkType lt2=TYPES.putIfAbsent(key, lt);
        if( lt2!=null)
        {
            return lt2;
        }
        return lt;
    }

    /**
     * Get a list of connections for a link type group
     * @param type the type
     * @throws Exception a serious problem
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public static LinkConnection[] listConnections( final @Nonnull String type) throws Exception
    {
        if( type == null) throw new IllegalArgumentException( "type is mandatory");
        checkUp();
        LinkType lt;

        lt = findType( type);

        return lt.listConnections();
    }

    /**
     * Count the number of connections in a link type group
     * @param type the type
     * @throws Exception a serious problem
     * @return the value
     */
    @CheckReturnValue
    public static int countAvailableClient( final @Nonnull String type) throws Exception
    {
        checkUp();
        LinkType lt;

        lt = findType( type);

        return lt.countAvailableConnections();
    }

    /**
     * Adds a connection to a link type group
     * @param type the type
     * @param connection the connection
     * @throws Exception a serious problem
     */
    public static void addConnection(
        final @Nonnull String type,
        final @Nonnull LinkConnection connection
    ) throws Exception
    {
        checkUp();
        LinkType lt;

        lt = findType( type);

        lt.addConnection( connection);
    }

    /**
     * Returns the first available connection. If no connects available
     * then an attempt is made to create a new connection.
     * @param type the type
     * @throws Exception a serious problem
     * @return the value
     */
    public static Object checkOutClient(final @Nonnull String type) throws Exception
    {
        checkUp();

        LinkType lt;

        lt = findType( type);

        Object obj;

        obj = lt.getFirstClient( CHECKEDOUT);

        return obj;
    }

    /**
     * Count the number of connections for a connection type.
     * @param type the type
     * @throws Exception a serious problem
     * @return the value
     */
    @CheckReturnValue
    public static int countConnections( final String type) throws Exception
    {
        checkUp();

        LinkType lt;

        lt = findType( type);

        return lt.countConnections();
    }

    /**
     * The maximum number of connections allowed for this connection type.
     * @param type the type
     * @throws Exception a serious problem
     * @return the value
     */
    @CheckReturnValue
    public static int getMaxConnections( final @Nonnull String type) throws Exception
    {
        checkUp();

        LinkType lt;

        lt = findType( type);

        return lt.getMaximumConnections();
    }

    /**
     *
     * @param linkType
     * @param connection
     * @return the value
     */
    public static boolean rawCheckOutConnection( final LinkType linkType, final LinkConnection connection)
    {
        return linkType.checkOutConnection( connection, CHECKEDOUT);
    }

    /**
     * Returns the connection with id.
     * @param type the type
     * @param id
     * @throws Exception a serious problem
     * @return the value
     */
    public static Object checkOutClient(
        final String type,
        final String id
    ) throws Exception
    {
        checkUp();

        LinkType lt;

        lt = findType( type);

        Object obj;

        LinkConnection lc;

        lc = lt.getMatchConnection( id, CHECKEDOUT);

        obj = lc.getClient();

        return obj;
    }

    /**
     * Short hand method of checking out and removing a client.
     * @param type the type
     * @throws Exception a serious problem
     * @return the value
     */
    public static Object takeClient(final @Nonnull String type) throws Exception
    {
        Object client;

        client = checkOutClient(type);

        removeClient( client);

        return client;
    }

    /**
     * Removes & kills a connection. The connection must be check out first.
     * @param client the client to use
     */
    public static void killClient( final @Nullable Object client)
    {
        if( client == null) return;
        LinkConnection connection;
        connection = removeClient( client);

        if( connection != null)
        {
            connection.close();
        }
    }

    /**
     *
     * @param client the client to use
     * @return the value
     */
    @CheckReturnValue
    public static boolean isCheckedOut( final @Nullable Object client)
    {
        if( client == null) return false;

        LinkConnection connection;
        connection = CHECKEDOUT.get( client);

        return connection != null;
    }

    /**
     * Removes a connection. The connection must be check out first.
     * @param client the client to use
     * @return the value
     */
    @Nullable
    public static LinkConnection removeClient( final @Nullable Object client)
    {
        if( client == null) return null;

        LinkConnection connection;

        connection = CHECKEDOUT.get(client);

        if( connection == null)
        {
            throw new RuntimeException( "Client " + client + " never checked out");
        }

        removeConnection( connection);

        return connection;
    }

    /**
     * remove a connection. The connection must be check out first.
     *
     * @param connection the connection to remove
     */
    public static void removeConnection( final @Nonnull LinkConnection connection)
    {
        for( LinkType lt: TYPES.values())
        {
            if( lt.hasConnection(connection))
            {
                if( lt.removeConnection( connection, CHECKEDOUT))
                {
                    return;
                }
            }
        }

        LOGGER.warn( "Could not find owner of " + connection);
    }

    /**
     * Returns a client to the pool of connections
     * @param client the client to use
     */
    public static void checkInClient( final @Nullable Object client)
    {
        if( client == null) return;

        LinkConnection connection;

        connection = CHECKEDOUT.get(client);
        if( connection == null)
        {
            throw new RuntimeException( "Client " + client + " never checked out");
        }

        try
        {
            long maxReuse = connection.getType().getMaximumReuse();
            if( maxReuse > 0 && connection.getCheckOutCount() > maxReuse)
            {
                killClient(client);
            }
            else
            {
                connection.checkIn(CHECKEDOUT);
            }
        }
        catch( Exception e)
        {
            LOGGER.error(
                "LinkManager.checkInClient(" + client + ")",
                e
            );
        }
    }

    /**
     * Returns true if Link Manager thread is running
     * @return the value
     */
    @CheckReturnValue
    public static boolean isRunning()
    {
        return isRunning(LAST_CHECKED.get());
    }

        /**
     * Returns true if Link Manager thread is running
     * @return the value
     */
    @CheckReturnValue
    private static boolean isRunning(final long lastChecked)
    {
        if( SHUTTING_DOWN.get() || thread == null || thread.getState()== Thread.State.TERMINATED)
        {
            return false;
        }

        long now=System.currentTimeMillis();
        
        return now - lastChecked <= MAX_CHECK_TIME;
    }
    
    /**
    * Check if Link Manager is running.
    */
    private static void checkUp()
    {
        if( isRunning() == false)
        {
            startUp();
        }
    }

   /**
    * Check if Link Manager is running.
    * Starts a new thread if not running.
    */
    private static synchronized void startUp()
    {
        if( SHUTTING_DOWN.get()==false)
        {
            long lastChecked = LAST_CHECKED.get();
            // Check again it may have been
            // restarted since our first check.
            if( isRunning(lastChecked) == false)
            {
                if( SHUTTING_DOWN.get()==false) // double check as shutdown isn't sync'd
                {
                    if( thread != null)
                    {
                        LOGGER.fatal(
                            "LinkManager restarted - loop test has taken " +
                            TimeUtil.getDiff(lastChecked)
                        );

                        thread.setName("DEAD - LinkManager");
                        thread.interrupt();

                        StringBuilder buffer = new StringBuilder();

                        CLogger.requestDump( buffer);
                        LOGGER.fatal(buffer);
                    }
                    LinkManager lm=new LinkManager();
                    LinkManagerRunner lmr=new LinkManagerRunner(lm);
                    LinkManagerShutdownListener lmsl=new LinkManagerShutdownListener();
                    Shutdown.addListener(lmsl);
                    Thread tmpThread = new Thread(
                        lmr,
                        "LinkManager"
                    );

                    tmpThread.setDaemon(  true);
                    tmpThread.setPriority(Thread.MIN_PRIORITY);
                    LAST_CHECKED.set(System.currentTimeMillis());

                    tmpThread.start();

                    thread=tmpThread;
                }
            }
        }
    }

    /**
    * Check that each link is connected.
    */
    private void testLinks() throws Exception
    {
        LAST_CHECKED.set( System.currentTimeMillis());

        for (String key : TYPES.keySet()) 
        {
            if( SHUTTING_DOWN.get()) break;
            LinkType lt;
            lt = TYPES.get(key);
            
            if( lt == null) continue;
            
            try
            {
                lt.testLines(CHECKEDOUT);
            }
            catch( Exception e)
            {
                LOGGER.info("Line test for '" + key + "'", e);
            }
        }
    }

    public static void shutdown()
    {
        SHUTTING_DOWN.set(true);
        thread=null;
        
        ForkJoinPool pool=new ForkJoinPool();

        for( String key: TYPES.keySet())
        {
            @SuppressWarnings("Convert2Lambda")
            ForkJoinTask<Void> task = pool.submit(new Callable<Void>(){
                @Override
                public Void call() throws Exception {
                    killType( key);
                    return null;
                }
            });
            
        }
        pool.shutdown();
        try{
            pool.awaitTermination(30, TimeUnit.SECONDS);
        }
        catch( InterruptedException ie)
        {
            LOGGER.warn( "could not shutdown connection", ie);
        }
        
    }

    /**
     * Find a link type.
     * @param type the type
     * @throws Exception a serious problem
     * @return the value
     */
    @Nonnull
    private static LinkType findType( final @Nonnull String type) throws Exception
    {
        checkUp();
        LinkType lt;

        lt = TYPES.get(type);

        if( lt == null)
        {
            throw new NoTypeException( "'" + type + "' server not found and types is "+TYPES.keySet());
        }

        return lt;
    }

    /* Can't be created */
    private LinkManager( )
    {
    }

    private static class LinkManagerShutdownListener implements ShutdownListener
    {
        @Override
        public void shutdown() {
            LinkManager.shutdown();
        }    
    }
    
    private static class LinkManagerRunner implements Runnable
    {       
        private final LinkManager lm;
        LinkManagerRunner( LinkManager lm)
        {
            this.lm=lm;
        }
        /**
        * Periodically test each link
        */
        @SuppressWarnings({"SleepWhileInLoop", "BroadCatchBlock", "TooBroadCatch", "UseSpecificCatch"})
        @Override
        public void run()
        {
            while( true)
            {
                if( thread == null) break;
                
                if( Thread.currentThread() != thread)
                {
                    LOGGER.error("LinkManager - Checking thread changed.");
                    break;
                }
                
                
                try
                {
                    lm.testLinks();
                }
                catch( Throwable t)
                {
                    LOGGER.error( "LinkManager - " + t, t);
                }
                try
                {
                    Thread.sleep( 2 * 60 * 1000);
                }
                catch( InterruptedException e)
                {
                    
                }
            }
        }
    }
    
    private static final ConcurrentHashMap<String, LinkType>    TYPES = new ConcurrentHashMap();
    private static final ConcurrentHashMap<Object, LinkConnection>    CHECKEDOUT = new ConcurrentHashMap();
    
    private static Thread thread;//MT CHECKED

    private static final AtomicLong LAST_CHECKED=new AtomicLong(System.currentTimeMillis());
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.links.LinkManager");//#LOGGER-NOPMD
}
