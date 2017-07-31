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
package com.aspc.remote.database;

import com.aspc.remote.database.internal.*;
import com.aspc.remote.util.links.LinkManager;
import com.aspc.remote.memory.HashMapFactory;
import com.aspc.remote.memory.MemoryHandler;
import com.aspc.remote.memory.MemoryManager;
import com.aspc.remote.util.misc.*;
import com.aspc.remote.util.timer.Lap;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.CheckReturnValue;
import javax.annotation.*;
import javax.annotation.Nullable;
import javax.annotation.Syntax;
import org.apache.commons.logging.Log;

/**
 *  CSQL is a wrapper for JDBC. It simplifies the usage/error
 *  handling of JDBC
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED sql</i>
 *  @author      Nigel Leck
 *  @since       August 19, 1996
 */
public final class CSQL extends SResultSet implements ResultsLoader
{
    /**
    * the SQL logger
    */
    public static final Log LOGGER_TIMINGS_SQL = CLogger.getLog("timings.sql");//NOPMD

    /** show the stack trace */
    public static final String DEBUG_SHOW_STACK="DEBUG_SHOW_STACK";

    /** control the time we will log the stack traces of slow requests */
    public static final String PROPERTY_LOG_SLOW_STACK_TRACE_TIME="LOG_SLOW_STACK_TRACE_TIME";
    
    private static enum Type{
        SELECT,
        UPDATE,
        INSERT,
        CREATE,
        DELETE,
        ADMIN,
        UNKNOWN;
    };
    
    private Type sqlType;
    
    private int                     maxRow,
                                    rowCount;

    private int queryTimeOutSeconds=DEFAULT_QUERY_TIMEOUT_SECONDS;

    private ResultsLoader           loader;
    private boolean                 suppressErrorLogging;
    private String                  theSql;
    private final DataBase          dataBase;//NOPMD
    private CSQL                    nextSql;
    private SQLWarning              sqlWarning;
    private Boolean                 readonly;
    private Connection              lockedConnection;
    private Statement               batchStatement;
    private StringBuilder           batchStatementText;
    private HashMap<String, PreparedStatementHolder> preparedStatementMap;

    private static final long LOG_SLOW_STACK_TRACE_TIME;
    /** control the default query timeout */
    public static final String PROPERTY_DEFAULT_QUERY_TIMEOUT_SECONDS="DEFAULT_QUERY_TIMEOUT_SECONDS";
    /** the default number of seconds before timing out */
    public static final int DEFAULT_QUERY_TIMEOUT_SECONDS;

    private static final ThreadLocal<AtomicLong>THREAD_HIT_COUNTER=new ThreadLocal<>();
    private static final ThreadLocal<AtomicLong>THREAD_TIME_ON_DB=new ThreadLocal<>();
    private static final ThreadLocal<AtomicLong>THREAD_LARGE_HIT_COUNTER=new ThreadLocal<>();

    /** the additional thread IDs */
    private static final ThreadLocal<List<String>> THREAD_IDS = new ThreadLocal<>();

    private static final AtomicLong HIT_COUNT=new AtomicLong();
    private static final AtomicLong HIT_ROW_COUNT=new AtomicLong();

    private static final AtomicLong HIT_LARGE_COUNT=new AtomicLong();

    private static final ThreadLocal CONNECTIONS=new ThreadLocal();

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.database.CSQL");//#LOGGER-NOPMD

    private static final boolean DEBUG_ALLOW_SIMULATE_SLOW=true;//#RELEASE ENABLED
    private static final ThreadLocal<Long> SIMULATE_SLOW=new ThreadLocal<>();
    
    private static final boolean DEBUG_LOG_BATCH_VALUES=false;//#RELEASE

    /**
     *
     * @param db the database
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public CSQL( final @Nonnull DataBase db)
    {
        assert db != null: "database must not be null";
        dataBase = db;
        loader = this;
        clear();
    }

    /** 
     * Slow ALL queries from this thread down by the delay milliseconds. 
     * 
     * @param delayMS 0 or less means no delay. 
     * @return the previous delay value. 
     */
    public static long simulateSlowQuery( final @Nonnegative long delayMS)
    {
        if( DEBUG_ALLOW_SIMULATE_SLOW)
        {
            Long old=SIMULATE_SLOW.get();
            if( delayMS>0)
            {
                SIMULATE_SLOW.set(delayMS);
            }
            else
            {
                SIMULATE_SLOW.remove();
            }
            
            if( old !=null)
            {
                return old;
            }
            else
            {
                return 0;
            }
        }
        
        return -1;
    }
    
    /**
     * Disable error level logging ( warning only).
     * @return this
     */
    @Nonnull
    public CSQL disableErrorLogging()
    {
        suppressErrorLogging=true;
        return this;
    }
    
    /**
     * Enable error level logging 
     * @return this
     */
    @Nonnull
    public CSQL enableErrorLogging()
    {
        suppressErrorLogging=false;
        return this;
    }
    
    /**
     * Force the statement to be read only
     * @param readonly true if this query should be readonly. 
     * @return this.
     */
    @Nonnull
    public CSQL setReadOnly( final boolean readonly)
    {
        if( readonly == false && dataBase.protection==DataBase.Protection.READONLY)
        {
            throw new IllegalArgumentException( "can't set as modifiable on a database that is marked as readonly");
        }
        
        this.readonly=readonly;
        
        return this;
    }
    
    /**
     * add additional thread IDs
     * @param threadID the thread id
     */
    public static void addThreadID(final @Nonnull String threadID)
    {
        List<String> list = THREAD_IDS.get();
        if( list == null)
        {
            list = new ArrayList<>();
            THREAD_IDS.set(list);
        }

        list.add(threadID);
    }

    /**
     * clear the Thread IDs.
     */
    public static void clearThreadIDs()
    {
        THREAD_IDS.remove();
    }

    /**
     * register the hits counter for this thread
     * @param counter the hits counter
     * @return the old counter
     */
    @Nonnull
    public static AtomicLong registerThreadHitsCounter( final @Nonnull AtomicLong counter)
    {
        AtomicLong originalCounter = THREAD_HIT_COUNTER.get();
        THREAD_HIT_COUNTER.set(counter);

        return originalCounter;
    }

    /**
     * register the hits counter for this thread
     * @param counter the hits counter
     * @return the old counter
     */
    @Nonnull
    public static AtomicLong registerThreadLargeHitsCounter( final @Nonnull AtomicLong counter)
    {
        AtomicLong originalCounter = THREAD_LARGE_HIT_COUNTER.get();
        THREAD_LARGE_HIT_COUNTER.set(counter);

        return originalCounter;
    }

    /**
     * register the time on the database for this thread
     * @param timeOnDB the time on the database
     * @return the old time on the database holder
     */
    @Nonnull
    public static AtomicLong registerThreadTimeOnDB( final @Nonnull AtomicLong timeOnDB)
    {
        AtomicLong originalTimeOnDB = THREAD_TIME_ON_DB.get();
        THREAD_TIME_ON_DB.set(timeOnDB);

        return originalTimeOnDB;
    }

    /**
     *
     * @param loader the loader
     * @return this
     */
    @Nonnull
    public CSQL setLoader( final @Nonnull ResultsLoader loader)
    {
        this.loader = loader;
        return this;
    }

    /**
     *
     * @throws SQLException a serious problem
     */
    public void lockConnection() throws SQLException
    {
        if( lockedConnection == null)
        {
            try
            {
                lockedConnection = dataBase.checkOutConnection();
            }
            catch( Exception e)
            {
                throw new SQLException( e.getMessage());
            }
        }
    }

    /**
     *
     * @return the database
     */
    @CheckReturnValue @Nonnull
    public DataBase getDataBase()
    {
        return dataBase;
    }

    /**
     * are we stateless ?
     * @throws Exception a database error occurred
     * @return TRUE if we are stateless
     */
    @CheckReturnValue
    public boolean isStateless() throws Exception
    {
        if( lockedConnection != null)
        {
            return lockedConnection.getAutoCommit();
        }

        HashMap current = (HashMap)CONNECTIONS.get();

        if( current != null )
        {
            Connection  threadConn;//NOPMD
            threadConn = (Connection)current.get( dataBase);

            if( threadConn != null)
            {
                return threadConn.getAutoCommit();
            }
        }

        return true;
    }

    /**
     * Starts a transaction.
     * This wrapper is needed due to multiple threads/connections.
     *
     * Thread Safe: we synchronize on a static table so that we don't "lose"
     *              any connections.
     * @throws SQLException a serious problem
     */
    public void beginTransaction() throws SQLException
    {
        if( Thread.currentThread().isInterrupted())
        {
            throw new DataBaseError( "Thread has been interrupted");
        }

        Lap start=new Lap();

        if( lockedConnection != null)
        {
            lockedConnection.setAutoCommit( false);
            recordTime( "BEGIN", start, lockedConnection, null);
            return;
        }

        /**
         * The thread local variable makes this thread safe.
         */
        HashMap current = (HashMap)CONNECTIONS.get();

        if( current == null )
        {
            current = HashMapFactory.create();
            CONNECTIONS.set( current);
        }

        Connection  threadConn;//NOPMD
        threadConn = (Connection)current.get( dataBase);

        if( threadConn == null)
        {
            for( int loop = 0; true; loop++)
            {
                try
                {
                    threadConn = dataBase.checkOutConnection();
                    threadConn.setAutoCommit( false);                    

                    break;
                }
                catch( Exception e)
                {
                    LinkManager.killClient( threadConn);
                    String msg="Connection is dead: " + threadConn;
                    LOGGER.warn( msg, e);

                    if( loop > 10) throw new SQLException(msg,e);
                }
            }

            current.put(
                dataBase,
                threadConn
            );
        }

        recordTime( "BEGIN", start, threadConn, null);
    }

    /**
     * Ends a transaction.
     * This wrapper is needed due to multiple threads/connections
     * @throws SQLException a serious problem
     */
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch", "UseSpecificCatch"})
    public void commit() throws SQLException
    {
        if( Thread.currentThread().isInterrupted())
        {
            rollback();
            throw new DataBaseError( "aborted transaction ( thread interrupted)");
        }
        assert batchStatement == null: "should not have open batch statements";
        assert preparedStatementMap == null || preparedStatementMap.isEmpty(): "should not have any prepared statements outstanding";

        Lap start=new Lap();

        if( lockedConnection != null)
        {
            try
            {
                lockedConnection.commit();
                recordTime( "COMMIT", start, lockedConnection, null);
            }
            catch( SQLException t)
            {
                iRollback( lockedConnection);
                throw t;
            }
            catch( Throwable t)
            {
                iRollback( lockedConnection);
                throw new SQLException( "could not commit", t);
            }
            return;
        }

        HashMap current = (HashMap)CONNECTIONS.get();

        if( current != null )
        {
            Connection  threadConn;//NOPMD
            threadConn = (Connection)current.get( dataBase);

            if( threadConn != null)
            {
                try
                {
                    threadConn.commit();
                    threadConn.setAutoCommit( true);

                    current.remove( dataBase);
                    dataBase.checkInConnection(threadConn);
                  // LinkManager.checkInClient( threadConn);

                    recordTime( "COMMIT", start, threadConn, null);

                    return;
                }
                catch( Exception e)
                {
                    iRollback( threadConn);

                    throw e;
                }
                catch( Throwable t)
                {
                    iRollback( threadConn);

                    throw new Error( "could not commit", t);
                }
            }
        }

        recordTime( "COMMIT ( no transaction)", start, null, null);
        LOGGER.warn( "Warning:", new SQLWarning("COMMIT with no current transaction") );
    }

    /**
     * Rolls back the current transaction.
     *
     * This wrapper is needed due to multiple threads/connections
     */
    public void rollback()
    {
        Lap start=new Lap();

        if( lockedConnection != null)
        {
            iRollback( lockedConnection);

            return;
        }

        HashMap current = (HashMap)CONNECTIONS.get();

        if( current != null )
        {
            Connection  threadConn;//NOPMD
            threadConn = (Connection)current.get( dataBase);

            if( threadConn != null)
            {
                iRollback( threadConn);

                return;
            }
        }

        recordTime( "ROLLBACK ( no transaction)", start, null, null);
    }

    /**
     * Rolls back a connection.
     *
     * Always kill the connection as some databases ( POSTGRES ) leave the connection in a funny state
     * once an error occurs
     */
    @SuppressWarnings({"empty-statement", "BroadCatchBlock", "TooBroadCatch"})
    private void iRollback( final @Nullable Connection conn)
    {
        boolean isInterrupted = Thread.interrupted();

        try
        {
            Lap start=new Lap();

            if( batchStatement != null)
            {
                try
                {
                    batchStatement.close();
                }
                catch( SQLException e)
                {
                    LOGGER.warn( "close batch statement",e);
                }
                batchStatement = null;
                batchStatementText=null;
            }

            if( preparedStatementMap != null )
            {
                try
                {
                    preparedStatementMap.values().stream().forEach((holder) -> {
                        try
                        {
                            holder.preparedStatement.close();
                        }
                        catch( SQLException sqlE)
                        {
                            LOGGER.warn( "close of prepared statement", sqlE);
                        }
                    });
                }
                finally
                {
                    preparedStatementMap.clear();
                }
            }

            if( conn != null)
            {
                try
                {
                    if( conn.getAutoCommit()==false)
                    {
                        conn.rollback();
                    }
                }
                catch( SQLException e)
                {
                    LOGGER.warn( "Rollback",e);
                }
                finally
                {
                    if( lockedConnection == null)
                    {
                        try
                        {
                            if( conn.isClosed() == false)
                            {
                                conn.close();
                            }
                        }
                        catch( SQLException t)
                        {
                            LOGGER.error( "closing connection",t);
                        }

                        HashMap current = (HashMap)CONNECTIONS.get();

                        if( current != null )
                        {
                            current.remove( dataBase);
                        }

                        LinkManager.killClient( conn);
                    }
                }

                recordTime( "ROLLBACK", start, conn, null);

                return;
            }

            recordTime( "ROLLBACK ( no connection)", start, null, null);
        }
        finally
        {
            if( isInterrupted)
            {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     *
     * @return the row count
     */
    @CheckReturnValue @Nonnegative
    public int getRowCount()
    {
        return rowCount;
    }
//
//    /**
//     *
//     * @return the type
//     */
//    @CheckReturnValue
//    public Type getSQLType()
//    {
//        return sqlType;
//    }

    /**
     *
     * @return the description
     */
    @CheckReturnValue @Nonnull
    public String getSQLTypeDesc()
    {
        switch( sqlType)
        {
            case SELECT:
                return "SELECT";
            case UPDATE:
                return "UPDATE";
            case INSERT:
                return "INSERT";
            case DELETE:
                return "DELETE";
            case CREATE:
                return "CREATE";
            default:
                return "Unknown";
        }
    }

    /**
     *
     * @param c the column
     * @return the type
     */
    @Override @CheckReturnValue
    public int getColumnType( final @Nonnegative int c)
    {
        Column column;
        column = columns.get( c - 1);
        return column.getType();
    }

    /**
     * returns the index of the column Name, -1 if not found
     * @param name the column name
     * @return the column number
     */
    @CheckReturnValue
    public int getColumnNr( final @Nonnull String name)
    {
        Column column;
        try
        {
            column = findColumnData(name);

            return column.getNumber();
        }
        catch( RuntimeException rt)
        {
            return -1;
        }
    }

    /**
     *
     * @param key the column
     * @return true if we have that column
     */
    @CheckReturnValue
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean hasColumn( final @Nonnull String key)
    {
        try
        {
            findColumnData( key);
            return true;
        }
        catch( RuntimeException rt)
        {
            return false;
        }
    }

    /**
     * Sets the number of seconds the driver will wait for a Statement object to execute to the given number of seconds.
     * If the limit is exceeded, an SQLException is thrown. A JDBC driver must apply this limit to the execute,
     * executeQuery and executeUpdate methods. JDBC driver implementations may also apply this limit to ResultSet
     * methods (consult your driver vendor documentation for details).
     *
     * @param seconds the new query timeout limit in seconds; zero means there is no limit
     * @return this
     */
    @Nonnull
    public CSQL setQueryTimeOutSeconds( final int seconds)
    {
        queryTimeOutSeconds=seconds;
        return this;
    }

    /**
     *
     * @param inSql the SQL
     * @throws SQLException a serious problem
     */
    public void execute( final @Syntax("SQL") @Nonnull String inSql) throws SQLException
    {
        clear();
        sqlType = Type.UNKNOWN;
        executeSql( inSql, false);
    }

    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch", "null", "UseSpecificCatch"})
    private void executeSql( final Object inObj, final boolean query) throws SQLException
    {
        if( Thread.currentThread().isInterrupted())
        {
            rollback();
            throw new DataBaseError( "Thread has been interrupted");
        }

        Lap start=new Lap();
        if( DEBUG_ALLOW_SIMULATE_SLOW)
        {
            Long delayMS = SIMULATE_SLOW.get();
            if( delayMS!=null)
            {
                try{
                    Thread.sleep(delayMS);
                }
                catch( InterruptedException ie)
                {
                    throw new SQLException("interrupted during delay of " + delayMS, ie);
                }
            }
        }
        Statement stmt          = null;//NOPMD

        theSql                  = null;

        Connection conn         = null;//NOPMD

        Connection  transactionConn = lockedConnection;//NOPMD

        boolean realError = false;

        if( lockedConnection == null)
        {
            HashMap current = (HashMap)CONNECTIONS.get();

            if( current != null )
            {
                transactionConn = (Connection)current.get( dataBase);
            }
        }
        boolean turnOffAutoCommit=false;//allow cursors which is only allowed if auto commit is off
        boolean turnOffReadOnly=false;
        try
        {
            boolean inTransaction;

            inTransaction = (transactionConn != null);
            
            if( inTransaction)
            {
                conn = transactionConn;
            }
            else
            {
                conn = dataBase.checkOutConnection();

                if( readonly==null)
                {
                    if( dataBase.protection==DataBase.Protection.SELECT_READONLY_BY_DEFAULT)
                    {
                        if( sqlType == Type.SELECT)
                        {
                            if( conn.isReadOnly()==false)
                            {
                                conn.setReadOnly(true);
                                turnOffReadOnly=true;
                            }
                        }
                    }
                }
                else if( readonly )
                {
                    if( conn.isReadOnly()==false)
                    {
                        conn.setReadOnly(true);
                        turnOffReadOnly=true;                    
                    }
                }

                if( turnOffReadOnly == false && query )
                {
                    turnOffAutoCommit=true;                    
                }
            }

            if( conn == null)
            {
                throw new Exception( "Error (CSQL) - No data base connection");
            }

            String dbType = dataBase.getType();
            if( turnOffAutoCommit)
            {
                conn.setAutoCommit(false);

                stmt = conn.createStatement();

                stmt.setFetchSize(1000);
            }
            else
            {
                stmt = conn.createStatement();
            }

            if (!dbType.equalsIgnoreCase(DataBase.TYPE_POSTGRESQL))
            {
                stmt.setQueryTimeout(queryTimeOutSeconds);
            }
            theSql  = (String)inObj;

            String aSql = theSql.trim();

            if( aSql.startsWith("/*"))
            {
                int pos = aSql.indexOf("*/");

                if( pos != -1)
                {
                    aSql = aSql.substring(pos + 2);
                }
            }
            if( dataBase.isCommentsSupported() == false)
            {
                aSql = removeComments( aSql);
            }

            String type = dataBase.getType();

            if( type.equals( DataBase.TYPE_SYBASE) == true)
            {
                executeSybaseSql( stmt, query, aSql);
            }
            else if(
                type.equals( DataBase.TYPE_POSTGRESQL) == true ||
                type.startsWith( DataBase.TYPE_MYSQL) == true
            )
            {
                executePostgresSql( stmt, query, aSql);
            }
            else
            {
                executeGenericSql( stmt,  query, aSql);
            }

            sqlWarning = getFirstWarning( stmt);
            if( sqlWarning != null)
            {
                String sqlState = sqlWarning.getSQLState();

                int errorCode=sqlWarning.getErrorCode();

                if( errorCode == 0)// Sybase rename
                {
                    sqlWarning=null;
                }
                else if( errorCode == -1100 && type.equals(DataBase.TYPE_HSQLDB) )
                {
                    sqlWarning=null;
                }
                else
                {
                    if("01ZZZ".equals(sqlState) == false ) // Sybase Show plan
                    {
                        throw sqlWarning;
                    }

                    LOGGER.warn(
                        theSql +
                        "\nSQL Warning: " + errorCode + "(" + sqlState + ")",
                        sqlWarning
                    );
                }
            }

            recordTime( inObj, start, conn, sqlWarning);
        }
        catch( TooManyRowsException e)
        {
            recordTime( inObj, start, conn, e);
        }
        catch( Throwable e)
        {
            recordTime( inObj, start, conn, e);

            realError = true;

            if( e instanceof SQLWarning)//NOPMD
            {
                String type = dataBase.getType();
                if(type.equalsIgnoreCase(DataBase.TYPE_MSSQL))
                {
                    SQLException sqlException = (SQLException)e;
                    while( sqlException != null)
                    {
                        int errNo;

                        errNo = sqlException.getErrorCode();
                        if(errNo == 15477)
                        {
                            LOGGER.warn( theSql + " " + e.toString());
                            realError = false;
                            return;
                        }
                    }
                }
            }

            if( e instanceof SQLException)//NOPMD
            {
                SQLException sqlException;

                sqlException = (SQLException)e;

                StringBuilder buffer = new StringBuilder();

                while( sqlException != null)
                {
                    int errNo;

                    errNo = sqlException.getErrorCode();

                    buffer.append("\nErrNo = ").append(errNo);
                    buffer.append("\nMsg   = ").append(sqlException);

                    sqlException = sqlException.getNextException();
                }

                String t;
                t = buffer.toString();

                if( t.length() != 0)
                {
                    LOGGER.warn( theSql + t);
                }
            }

            String temp =  theSql + "\nSQL Error(perform) - rowNo " + rowCount;
            /**
             * don't send an email out when we have canceled the request
             */
            if(Thread.currentThread().isInterrupted())
            {
                LOGGER.warn(
                   temp,
                    e
                );
            }
            else
            {
                LOGGER.error(
                    temp,
                    e
                );
            }

            iRollback(conn);

            if( e instanceof DeadLockException)//NOPMD
            {
                throw (DeadLockException)e;
            }
            else if( e instanceof SQLException)//NOPMD
            {
                throw (SQLException)e;
            }
            else if( e instanceof OutOfMemoryError)
            {
                MemoryManager.clearMemory(MemoryHandler.Cost.HIGHEST);
                String msg="Out Of Memory running: " + inObj;
                MemoryManager.lastError(msg, e);
                LOGGER.warn( msg, e);
                throw (Error)e;
            }
            else if( e instanceof Error)//NOPMD
            {
                LOGGER.warn( "non sql error " + inObj, e);
                throw (Error)e;
            }
            else if( e instanceof RuntimeException)//NOPMD
            {
                LOGGER.warn( inObj, e);
                throw (RuntimeException)e;
            }
            else
            {
                LOGGER.warn( "non sql exception", e);
                throw new SQLException( e.getMessage());
            }
        }
        finally
        {
            if( realError == false)
            {
                if( turnOffAutoCommit )
                {
                    conn.setAutoCommit(true);
                }
                if( turnOffReadOnly)
                {
                    conn.setReadOnly(false);
                }
            }
            close(stmt);

            if(transactionConn == null )
            {
                if(realError == false)
                {
                    dataBase.checkInConnection(conn);
//                    LinkManager.checkInClient( conn);
                }
                else
                {
                    try
                    {
                        if( conn != null && conn.isClosed() == false) conn.close();
                    }
                    catch( SQLException sqlE)
                    {
                        LOGGER.warn( "could not close connection after error", sqlE);
                    }
                }
            }
        }
    }

    private void executeSybaseSql( final Statement stmt, final boolean query, final String theSql) throws Exception
    {
        boolean setMax = false;
        try
        {
            if( query == true)
            {
                if( maxRow > 0)
                {
                    stmt.execute( "set rowcount " + (maxRow + 1));
                    setMax = true;
                }
                stmt.executeQuery( theSql );
                rowCount = loader.loadResults( stmt);
            }
            else
            {
                rowCount = stmt.executeUpdate( theSql);
            }
        }
        catch( Exception e)
        {
            Exception e2= convertException( e);

            if( e2 != null ) throw e2;
        }
        finally
        {
            if( setMax)
            {
                stmt.execute( "set rowcount 0");
            }
        }
    }

    @SuppressWarnings({"ThrowableResultIgnored", "ConvertToStringSwitch"})
    private SQLException convertException( final Throwable e)
    {
        String type = dataBase.getType();
        if( type.equals( DataBase.TYPE_ORACLE))
        {
            if( e instanceof SQLException)//NOPMD
            {
                SQLException sqlException = (SQLException)e;
                while( sqlException != null)
                {
                    if( "61000".equals(sqlException.getSQLState()))
                    {
                        return new DeadLockException( e.getMessage());
                    }
                    else if( sqlException.getErrorCode() == 17081)
                    {
                        return new DeadLockException( e.getMessage());
                    }

                    sqlException = sqlException.getNextException();
                }

                return (SQLException)e;
            }

            return new SQLException( e.getMessage());
        }
        else if( type.equals( DataBase.TYPE_POSTGRESQL) || type.equals( DataBase.TYPE_MSSQL))
        {
            if( e instanceof SQLException)//NOPMD
            {
                SQLException sqlException;
                sqlException = (SQLException)e;
                while( sqlException != null)
                {
                    String msg = e.getMessage();
                    if( msg != null && msg.toLowerCase().contains("deadlock"))
                    {
                        return new DeadLockException( msg);
                    }

                    sqlException = sqlException.getNextException();
                }

                return (SQLException)e;
            }
            LOGGER.warn( theSql, e);
            String msg;

            msg = e.getMessage();
            if( StringUtilities.isBlank(msg))
            {
                msg = e.toString();
            }
            return new SQLException( msg);
        }
        else if( type.equals(DataBase.TYPE_SYBASE))
        {
            boolean deadlockError = false;
            boolean realError = true;
            String msg;

            msg = e.getMessage();

            // JZOR2 is sybase's code for NO output which is OK.
            if(
                msg != null                         &&
                msg.startsWith( "JZ0R2") == true
            )
            {
                realError = false;
            }
            else if( e instanceof SQLException)//NOPMD
            {
                realError = false;
                SQLException sqlException;

                sqlException = (SQLException)e;

                while( sqlException != null)
                {
                    int errNo;

                    errNo = sqlException.getErrorCode();

                    if( errNo == 1205)  // Deadlock
                    {
                        deadlockError = true;
                    }
                    else if(
                        errNo != 405801 && // Loaded X Amount
                        errNo != 404101 && // Dumped X Amount
                        errNo != 3137   && // Online
                        errNo != 3015   && // Warning restoring a master DB
                        errNo != 3476   && // Redo Journal Pass
                        errNo != 3479   && // Redo commit
                        errNo != 3216   && // Backup session
                        errNo != 602801 && // Dumpfile name
                        errNo != 304301 && // Dump Complete
                        errNo != 304201    // Load Complete
                    )
                    {
                        realError = true;
                    }

                    sqlException = sqlException.getNextException();
                }
            }

            if( realError == true)
            {
                if( (e instanceof SQLException) == false)
                {
                    return new SQLException( e.getMessage());
                }

                return (SQLException)e;
            }
            else if( deadlockError)
            {
                return new DeadLockException( e.getMessage());
            }
            else
            {
                return null;
            }
        }

        if( e instanceof SQLException)
        {
            return (SQLException)e;
        }

        return new SQLException( e.getMessage());
    }

    private void initBachStatement() throws SQLException
    {
        if (batchStatement == null)
        {
            Connection conn;
            if( lockedConnection == null)
            {
                HashMap current = (HashMap)CONNECTIONS.get();

                if( current != null )
                {
                    conn = (Connection)current.get( dataBase);

                    if( conn == null)
                    {
                        throw new SQLException( "must have began a transaction to call batch statements");
                    }
                }
                else
                {
                    throw new SQLException( "must have began a transaction to call batch statements");
                }
            }
            else
            {
                conn = lockedConnection;
            }
            batchStatement = conn.createStatement();

            batchStatementText = new StringBuilder();
        }
    }

    /**
     * Batch batchStatement
     * @param theSql sql batchStatement
     * @return this
     * @throws SQLException a database problem
     */
    @Nonnull
    public CSQL addBatch( final @Syntax("SQL") @Nonnull String theSql) throws SQLException
    {
        initBachStatement();
        batchStatement.addBatch(theSql);

        if( batchStatementText.length() > 0)
        {
            batchStatementText.append( ";\n");
        }
        batchStatementText.append(theSql.trim());
        return this;
    }

    /**
     * Batch batchStatement
     * @param statement SQL batchStatement
     * @param args the arguments to use.
     * @return this
     * @throws SQLException a database problem
     */
    @Nonnull
    public CSQL addPreparedStatement( final @Syntax("SQL") @Nonnull String statement, final Object... args) throws SQLException
    {
        PreparedStatementHolder holder=null;
        if( preparedStatementMap == null)
        {
            preparedStatementMap=HashMapFactory.create();
        }
        else
        {
            holder=preparedStatementMap.get( statement);
        }

        if( holder == null)
        {
            initBachStatement();
            Connection conn = batchStatement.getConnection();
            PreparedStatement ps=conn.prepareStatement(statement);
            holder = new PreparedStatementHolder(ps, statement);

            preparedStatementMap.put( statement, holder);
            if( DEBUG_LOG_BATCH_VALUES)
            {
                holder.logText=new StringBuilder();
            }
        }

        PreparedStatement pstmt=holder.preparedStatement;
        holder.calls++;
        if( holder.logText != null) holder.logText.append( "\n");

        for( int i=0; i < args.length; i++)
        {
            Object value=args[i];

            if(value instanceof String)
            {
                pstmt.setString(i + 1, (String)value);
            }
            else if(value instanceof Long)
            {
                pstmt.setLong(i + 1, ((Long)value));
            }
            else if(value instanceof Integer)
            {
                pstmt.setInt(i + 1, ((Integer)value));
            }
            else if(value instanceof Double)
            {
                pstmt.setDouble(i + 1, ((Double)value));
            }
            else if(value instanceof Date)
            {
                pstmt.setDate(i + 1, ((Date)value));
            }
            else if(value instanceof Timestamp)
            {
                pstmt.setTimestamp(i+1, (Timestamp)value);
            }
            else if( value == null)
            {
                pstmt.setNull(i + 1, 0);
            //    value="NULL";
            }
            else
            {
                throw new RuntimeException( "unknow type " + value);
            }

            if( holder.logText != null)
            {
                if( i > 0)
                {
                    holder.logText.append(",");
                }
                holder.logText.append(value);
            }
        }

        pstmt.addBatch();
        
        return this;
    }

    /**
     * Execute batch batchStatement
     * @return the rows
     * @throws SQLException a database problem
     */
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch", "UseSpecificCatch"})
    @Nonnull
    public int[] executeBatch() throws SQLException
    {
        if( Thread.currentThread().isInterrupted())
        {
            rollback();
            throw new DataBaseError( "Thread has been interrupted");
        }
        theSql=null;

        Lap start=new Lap();

        initBachStatement();

        String currentStatement="UNKNOW";
        PreparedStatementHolder tmpHolder=null;
        try
        {
            int rows[]={};
            rowCount=0;
            currentStatement=batchStatementText.toString();
            if( StringUtilities.isBlank(currentStatement)==false)
            {

                rows = batchStatement.executeBatch();

                int tCount=0;
                for( int i = 0; i < rows.length; i++)
                {
                    tCount += rows[i];
                }
                rowCount +=tCount;
                recordTime( currentStatement, start, batchStatement.getConnection(), null, tCount, dataBase);
            }

            if( preparedStatementMap != null )
            {
                try
                {
                    for( PreparedStatementHolder holder: preparedStatementMap.values())
                    {
                        tmpHolder=holder;
                        Lap pStart=new Lap();

                        StringBuilder sb = new StringBuilder(holder.statement);
                        sb.append("\n/* CALLS: ");
                        sb.append( holder.calls);
                        if( holder.logText != null) sb.append( holder.logText);
                        sb.append(" */");
                        currentStatement=sb.toString();
                        int pRows[] = holder.preparedStatement.executeBatch();

                        int tCount=0;
                        for( int i = 0; i < pRows.length; i++)
                        {
                            tCount += pRows[i];
                        }
                        rowCount +=tCount;
                        recordTime( currentStatement, pStart, batchStatement.getConnection(), null, tCount, dataBase);

                        holder.preparedStatement.close();
                    }
                }
                finally
                {
                    preparedStatementMap.clear();
                }
            }
            return rows;
        }
        catch( Throwable e)
        {
            StringBuilder sb = new StringBuilder(currentStatement);
            if( tmpHolder != null)
            {
                sb.append("\n/* CALLS: ");
                sb.append( tmpHolder.calls);
                if( tmpHolder.logText != null) sb.append( tmpHolder.logText);
                sb.append(" */");
            }
            
            recordTime( sb.toString(), start, batchStatement.getConnection(), e);

            if( e instanceof SQLException)//NOPMD
            {
                SQLException sqlException;

                sqlException = (SQLException)e;

                StringBuilder buffer = new StringBuilder();

                while( sqlException != null)
                {
                    int errNo;

                    errNo = sqlException.getErrorCode();

                    buffer.append("\nErrNo = ").append(errNo);
                    buffer.append("\nMsg   = ").append(sqlException);

                    sqlException = sqlException.getNextException();
                }

                String t;
                t = buffer.toString();

                if( t.length() != 0 )
                {
                    LOGGER.warn( t);
                }
            }

            String temp =  currentStatement + "\nSQL Error(perform) - rowNo " + rowCount;

            SQLException sqlException = convertException(e);
            /**
             * don't send an email out when we have canceled the request
             */
            if(
                Thread.currentThread().isInterrupted() ||
                suppressErrorLogging ||
                sqlException instanceof DeadLockException
            )
            {
                LOGGER.warn(
                   temp,
                    sqlException
                );
            }
            else
            {
                LOGGER.error(
                    temp,
                    sqlException
                );
            }

            iRollback(batchStatement.getConnection());

            throw sqlException;
        }
        finally
        {
            close(batchStatement);
            batchStatement = null;
            batchStatementText=null;
        }
    }

    private void executeGenericSql( final @Nonnull Statement stmt, final boolean query, final @Nonnull String theSql) throws Exception
    {
        try
        {
            if( query == true)
            {
                if( maxRow > 0)
                {
                    stmt.setMaxRows(maxRow);
                }
                stmt.executeQuery( theSql );
                rowCount = loader.loadResults( stmt);
            }
            else
            {
                rowCount = stmt.executeUpdate( theSql);
            }
        }
        catch( Exception e)
        {
            Exception e2= convertException( e);

            if( e2 != null ) throw e2;
        }        
    }

    private void executePostgresSql( final @Nonnull Statement stmt, final boolean query, final @Nonnull String theSql) throws Exception
    {
        try
        {
            if( query == true)
            {
                if( maxRow > 0)
                {
                    stmt.setMaxRows(maxRow);
                }

                stmt.execute( theSql );
                rowCount = loader.loadResults( stmt);
            }
            else
            {
                rowCount = stmt.executeUpdate( theSql);
            }
        }
        catch( Exception e)
        {
            throw convertException( e);
        }
    }

    @CheckReturnValue @Nonnull 
    private String removeComments( final @Nonnull String sql)
    {
        String temp = sql;

        while( true)
        {
            int pos = temp.indexOf( "/*");
            if( pos == -1) break;

            int pos2 = temp.substring( pos).indexOf( "*/");
            if( pos2 == -1 ) break;

            String t;
            t = temp.substring(0,  pos) + temp.substring( pos + pos2 + 2);
            temp = t;
        }

        return temp;
    }

    /**
     * Record the SQL time
     *
     * @param inObj The SQL
     * @param start When did we start ?
     * @param conn The connection
     * @param e the exception
     */
    private void recordTime(
        final Object inObj,
        final Lap lap,
        final Connection conn,
        final Throwable e
    )
    {
        recordTime( inObj, lap, conn, e, getRowCount(), dataBase);
    }

    /**
     * Record the SQL time
     *
     * @param inObj The SQL
     * @param lap When did we start ?
     * @param conn The connection
     * @param e the exception
     * @param rows the number of rows
     * @param dataBase the current database.
     */
    @SuppressWarnings("ThrowableResultIgnored")
    public static void recordTime(
        final @Nullable Object inObj,
        final @Nonnull Lap lap,
        final @Nullable Connection conn,
        final @Nullable Throwable e,
        final int rows,
        final @Nonnull DataBase dataBase
    )
    {
        if( inObj == null) return;

        if( LOGGER_TIMINGS_SQL.isDebugEnabled() == false) return;
        lap.end();
        String temp = inObj.toString();

        StringBuilder buffer = new StringBuilder( temp.length() + 1000);

        buffer.append( temp);

        buffer.append( "\n");
        boolean logStackTrace = false;

        Throwable tempE = e;
        for( int loop=0; tempE != null;loop++)
        {
            if( tempE instanceof SQLWarning)
            {
                buffer.append( "===  WARNING  ===\n");
            }
            else
            {
                buffer.append( "===  ERROR  ===\n");
            }
            String msg = tempE.getMessage();
            if( StringUtilities.isBlank(msg))
            {
                msg = tempE.toString();
            }
            if( tempE instanceof SQLException)
            {
                SQLException warn = (SQLException)tempE;
                if( warn.getErrorCode() != 0)
                {
                    buffer.append(warn.getErrorCode()).append("(").append(warn.getSQLState()).append( "): ");
                }
            }
            buffer.append( msg);
            buffer.append( "\n");

            StringWriter w = new StringWriter();
            PrintWriter p = new PrintWriter( w);
            tempE.printStackTrace(p);
            buffer.append( w.toString());
            @SuppressWarnings("ThrowableResultIgnored")
            Throwable tempE2 = tempE.getCause();

            if( tempE2 == null)
            {
                if( tempE instanceof SQLException)
                {
                    SQLException sqlException = (SQLException)tempE;

                    tempE = sqlException.getNextException();
                }
                else
                {
                    tempE = null;
                }
            }
            else
            {
                tempE = tempE2;
            }

            if( loop > 5) break;
        }

        buffer.append( "/*------------------------------------------------------------------------------\n| ");
        buffer.append( Thread.currentThread().getName());
        if( conn != null)
        {
            buffer.append( " [");
            buffer.append( conn.hashCode());
            buffer.append( "]");
        }
        java.util.Date now = new java.util.Date();
        buffer.append( " ");
        buffer.append( TimeUtil.format("yyyyMMMdd-HH:mm:ss.SSS", now, TimeZone.getDefault()));
        buffer.append( "\n| ");
        increamentHitCount();
        if( rows >= 0)
        {
            buffer.append( rows);
            if( rows > 1000)
            {
                increamentLargeHitCount();
                buffer.append( " <LARGE> ");
            }
            HIT_ROW_COUNT.addAndGet(rows);
        }
        else
        {
            buffer.append( "?");
        }

        boolean isSlow = false;
        long timeTaken = lap.durationMS();

        if( timeTaken > LOG_SLOW_STACK_TRACE_TIME)
        {
            buffer.append(" <SLOW> ");
            isSlow = true;
        }
        AtomicLong timeOnDB = THREAD_TIME_ON_DB.get();
        if( timeOnDB != null)
        {
            timeOnDB.addAndGet(timeTaken);
        }
        buffer.append( " Rows in ");
        buffer.append( TimeUtil.getDiff(0, timeTaken));
        buffer.append( " ");
        buffer.append( dataBase.getUser());
        buffer.append( "@");
        buffer.append( dataBase.getShortUrl());

        if( e == null)
        {
            if(isSlow )
            {
                logStackTrace = true;
            }
            else if( CProperties.findProperty( DEBUG_SHOW_STACK, "NO").equalsIgnoreCase("YES"))
            {
                logStackTrace = true;
            }
        }

        List<String> threadIds = THREAD_IDS.get();
        if(threadIds != null && threadIds.size() > 0)
        {
            buffer.append( "\n| ");
            boolean first = true;
            for(String entry : threadIds)
            {
                if(!first)
                {
                    buffer.append(",");
                }
                else
                {
                    first = false;
                }
                buffer.append(entry);
            }
        }

        if(logStackTrace)
        {
            StackTraceElement trace[] = Thread.currentThread().getStackTrace();
            boolean foundRoot=false;
            for( StackTraceElement st: trace)
            {
                if( foundRoot == false)
                {
                    if( st.getClassName().contains("CSQL"))
                    {
                        foundRoot=true;
                    }
                    continue;
                }

                if( st.getClassName().contains("CSQL") && st.getMethodName().equals("recordTime")) continue;

                buffer.append("\n|   ");
                buffer.append(st.toString());
            }
        }

        buffer.append( "\n+=============================================================================*/\n");

        // clear the interrupt flag so that we can log the information.
        boolean isInterrupted = Thread.interrupted();

        try
        {
            LOGGER_TIMINGS_SQL.debug( buffer);
        }
        finally
        {
            if( isInterrupted )
            {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     *
     * @return true if there is some warnings.
     */
    @CheckReturnValue
    public boolean hasWarnings()
    {
        return sqlWarning != null;
    }

    /**
     *
     * @param stmt the batchStatement
     * @throws Exception a serious problem
     * @return the first warning
     */
    @CheckReturnValue @Nullable
    public SQLWarning getFirstWarning( final @Nonnull Statement stmt) throws Exception
    {
        SQLWarning warn;

        warn = stmt.getWarnings();
        if( dataBase.getType().startsWith( DataBase.TYPE_MSSQL) == false)
        {
            return warn;
        }

        while( warn != null)
        {
            if( warn.getErrorCode() != 0)
            {
                return warn;
            }
            warn = warn.getNextWarning();

        }
        return null;
    }

    /**
     *
     * @return the warning text
     */
    @SuppressWarnings("ThrowableResultIgnored")
    @CheckReturnValue @Nonnull
    public String getWarningText()
    {

        SQLWarning warn = sqlWarning;

        StringBuilder buffer = new StringBuilder();
        while( warn != null)
        {
            if(
                dataBase.getType().startsWith( DataBase.TYPE_MSSQL) == false ||
                warn.getErrorCode() != 0
            )
            {
                buffer.append(warn.getErrorCode()).append("(").append(warn.getSQLState()).append("): ").append(
                    warn.getMessage());
                buffer.append( "\n");
            }
            warn = warn.getNextWarning();

        }

        return buffer.toString();
    }

    /**
    *
    * @param stmt the batchStatement
    * @return the number of rows
    * @throws Exception a serious problem
    */
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    @Override
    public int loadResults( final @Nonnull Statement stmt) throws Exception
    {
         int    i,
                colCount;

        try
        (ResultSet result = stmt.getResultSet()) {

            if( result != null)
            {
                columns     = new ArrayList();
                columnKeys  = HashMapFactory.create();
                ResultSetMetaData        metaData;

                metaData    = result.getMetaData();
                colCount    = metaData.getColumnCount();
                rowPage = new ArrayList();

                for( i = 1; i <= colCount; i++)
                {
                    Column column;

                    String cName;
                    cName = metaData.getColumnName( i);
                    column = new Column(
                        this,
                        cName,
                        metaData.getColumnType( i),
                        i - 1
                    );

                    columns.add( column);
                    columnKeys.put( cName.trim().toLowerCase(), column);
                }

                TimeZone tz = TimeZone.getDefault();
                for(rowCount = 0; result.next(); rowCount++)
                {
                    Row row;
                    if( maxRow > 0 && rowCount > maxRow )
                    {

                        throw(
                            new TooManyRowsException(
                                "Too Many Rows( > " + maxRow + ") found for :-\n" + theSql
                            )
                        );
                    }

                    row = new Row(tz, columns, result);

                    rowPage.add( row);
                }
            }
            if( stmt.getMoreResults())
            {
                if( dataBase.getType().equals( DataBase.TYPE_MYSQL) == false ||
                    stmt.getUpdateCount() != -1)
                {
                    nextSql = new CSQL(dataBase);

                    nextSql.loader.loadResults( stmt);
                }
            }
        }

        return rowCount;
    }

    /**
     *
     * @return the next results
     */
    @CheckReturnValue @Nullable
    public CSQL nextResults()
    {
        return nextSql;
    }

    /**
     *
     * @return true if more results
     */
    @CheckReturnValue @Nonnull
    public boolean hasMoreResults()
    {
        return nextSql != null;
    }

    /**
     *
     * @return the SQL
     */
    @CheckReturnValue @Nonnull
    public @Syntax("SQL") String getSQL()
    {
        return theSql;
    }

    /**
     * performs the SQL batchStatement passed. We examine the batchStatement to check if it's a SELECT type
     * batchStatement and if so request the record set.
     * @param inSql the SQL
     * @return this
     * @throws java.sql.SQLException a serious problem
     */
    @Nonnull
    public CSQL perform( final @Syntax("SQL") @Nonnull String inSql) throws SQLException
    {
        String                  tSql;
        clear();

        int i=0;

        // Remove leading comments
        do
        {
            for( ; i < inSql.length(); i++)
            {
                if( Character.isLetter(inSql.charAt(i)) || inSql.charAt(i) == '/')
                {
                    break;
                }
            }

            if(inSql.substring(i).startsWith("/*"))
            {
                int pos;
                pos = inSql.substring(i + 2).indexOf("*/");

                if( pos != -1)
                {
                    i += pos + 4;
                }
                else
                {
                    break;
                }

                continue;
            }

            break;
        }
        while( true);

        sqlType = Type.UNKNOWN;

        if( inSql.length() - i > 6)
        {
            tSql = inSql.substring(i);
            int pos=0;
            int len = tSql.length();
            for( ; pos < len; pos++)
            {
                if( Character.isWhitespace(tSql.charAt(pos)))
                {
                    break;
                }
            }

            tSql        = tSql.substring(0, pos).trim();

            if( tSql.equalsIgnoreCase("SELECT") == true)
            {
                sqlType = Type.SELECT;
            }
            else if( tSql.equalsIgnoreCase("UPDATE") == true)
            {
                sqlType = Type.UPDATE;
            }
            else if( tSql.equalsIgnoreCase("INSERT") == true)
            {
                sqlType = Type.INSERT;
            }
            else if( tSql.equalsIgnoreCase("DELETE") == true)
            {
                sqlType = Type.DELETE;
            }
            else if( tSql.equalsIgnoreCase("CREATE") == true)
            {
                sqlType = Type.CREATE;
            }
            else if(
                tSql.equalsIgnoreCase("GRANT")    == true ||
                tSql.equalsIgnoreCase("ALTER")    == true ||
                tSql.equalsIgnoreCase("DROP")     == true ||
                tSql.equalsIgnoreCase("REVOKE")   == true
            )
            {
                sqlType = Type.ADMIN;
            }
        }

        if( sqlType == Type.SELECT || sqlType == Type.UNKNOWN)
        {
            executeSql( inSql, true);
        }
        else
        {
            executeSql( inSql, false);
        }
        
        return this;
    }

    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    private void close( final @Nullable Statement stmt)
    {
        if( stmt == null) return;

        try
        {
            stmt.close();
        }
        catch( SQLException e)
        {
            LOGGER.debug( "Error - closing Statment", e);
        }
        catch( Throwable e)
        {
            LOGGER.warn( "Error - closing Statment", e);
        }
    }

    /**
     *
     * @param theSelect the select
     * @return this
     * @throws java.sql.SQLException a serious problem
     * @throws com.aspc.remote.database.NotFoundException not found
     */
    @Nonnull
    public CSQL findOne( final @Syntax("SQL") @Nonnull String theSelect) throws SQLException, NotFoundException
    {
        perform( theSelect);

        if( getRowCount() < 1)
        {
            throw new NoRowsFoundException( "No Rows Found for :-\n" + theSelect);
        }
        else
        {
            if( sqlType == Type.SELECT || sqlType == Type.UNKNOWN)
            {
                if( next()==false)
                {
                    throw new NoRowsFoundException( "No Rows Found for :-\n" + theSelect);
                }
            }
        }

        if( getRowCount() > 1 )
        {
            throw new TooManyRowsException(
                "Too Many Rows(" + getRowCount() + ") found for :-\n" + theSelect
            );
        }
        
        return this;
    }

    /**
     *
     * @param maxRow the max number of rows.
     * @return this
     */
    @Nonnull
    public CSQL setMaxRow( final int maxRow)
    {
        this.maxRow = maxRow;
        return this;
    }

    /**
     * Returns true if the batchStatement was a select type of batchStatement
     * @return true if there is output
     */
    @CheckReturnValue 
    public boolean hasOutput()
    {
        return columns != null;
    }

    /**
     * Produces a tab separated data string.
     * Data is produced with or with out header info ( encoded not to include tabs in data)
     * @param header_fg include header
     * @throws java.sql.SQLException a serious problem
     * @return the encoded data
     */
    @CheckReturnValue @Nullable
    public String encodeTableData( final boolean header_fg) throws SQLException
    {
        return encodeTableData( header_fg, -1, null);
    }

    /**
     * increment hit count
     * @return The incremented hit count
     */
    @Nonnegative
    public static long increamentHitCount()
    {
        AtomicLong threadHitCounter = THREAD_HIT_COUNTER.get();
        if( threadHitCounter != null)
        {
            threadHitCounter.incrementAndGet();
        }
        return HIT_COUNT.incrementAndGet();
    }

    /**
     * The hit count
     * @return the hit count
     */
    @CheckReturnValue @Nonnegative
    public static long getHitCount()
    {
        return HIT_COUNT.get();
    }

    /**
     * The hit ROW count
     * @return the hit ROW count
     */
    @CheckReturnValue @Nonnegative
    public static long getHitRowCount()
    {
        return HIT_ROW_COUNT.get();
    }

    /**
     * increment hit count
     * @return The incremented hit count
     */
    @Nonnegative
    public static long increamentLargeHitCount()
    {
        AtomicLong threadLargeHitCounter = THREAD_LARGE_HIT_COUNTER.get();
        if( threadLargeHitCounter != null)
        {
            threadLargeHitCounter.incrementAndGet();
        }

        return HIT_LARGE_COUNT.incrementAndGet();
    }

    /**
     * The hit count
     * @return the hit count
     */
    @CheckReturnValue @Nonnegative
    public static long getLargeHitCount()
    {
        return HIT_LARGE_COUNT.get();
    }

    /**
     * With addition option of a row ( -1 for all rows) and only some columns
     * 
     * @param header_fg include header
     * @param whichRow which row
     * @param restrictCols the columns
     * @throws java.sql.SQLException a serious problem
     * @return the encoded data
     */
    @CheckReturnValue @Nullable
    public String encodeTableData(
        boolean     header_fg,
        int         whichRow,
        List        restrictCols
    ) throws SQLException
    {
        if( hasOutput() == false)
        {
            return null;
        }

        int colList[] = null;

        if( restrictCols != null)
        {
            colList = new int[ restrictCols.size()];

            for( int i = 0; i < colList.length; i++)
            {
                colList[i] = findColumn(
                    (String)restrictCols.get(i)
                );
            }
        }

        StringBuilder s;
        int approximateSize;
        approximateSize = whichRow == -1 ? rowCount : 1 * 1000;
        int max = 2000000;

        if( approximateSize > max) approximateSize =  max;

        s = new StringBuilder(approximateSize);

        int cols;
        cols = getColumnCount();

        if( whichRow == -1)
        {
            rewind();
        }

        int outLen = cols;
        int col;

        if( colList != null)
        {
            outLen = colList.length;
        }

        if( header_fg == true)
        {
            for (int j = 0; j < outLen; j++)
            {
                col = j;
                if( colList != null)
                {
                    col = colList[j];
                }

                if( col > 0)
                {
                    s.append("\t");
                }

                s.append( getColumnName( col));
                s.append( ",");
                int t;

                t = getColumnType(col);
                switch (t) {
                    case Types.DATE:
                        s.append( "DATE");
                        break;
                    case Types.FLOAT:
                        s.append( "FLOAT");
                        break;
                    case Types.INTEGER:
                        s.append( "INTEGER");
                        break;
                    case Types.CHAR:
                        s.append( "CHAR");
                        break;
                    default:
                        break;
                }
            }
            s.append("\n");
        }

        do
        {
            if( whichRow == -1)
            {
                if( next() == false)
                {
                    break;
                }
            }

            for (int j = 0; j < outLen; j++)
            {
                col = j;
                if( colList != null)
                {
                    col = colList[j];
                }

                if( col > 0)
                {
                    s.append("\t");
                }

                Object o;
                o = getObject( col);
                if( o != null)
                {
                    s.append(StringUtilities.encode( o.toString()));
                }
            }
            s.append( "\n");
        }
        while( whichRow == -1);

        String result;

        result = s.toString();

        return result;
    }

    private void clear()
    {
        currentRow          = 0;
        rowPage             = null;
        sqlWarning          = null;
        columnKeys          = null;
        columns             = null;
        rowCount            = 0;
        sqlType             = Type.UNKNOWN;

        currentData         = null;
        nextSql             = null;
    }

    private static class PreparedStatementHolder
    {
        final PreparedStatement preparedStatement;
        final String statement;
        long calls;
        StringBuilder logText;

        PreparedStatementHolder(final PreparedStatement ps, final String statement) {
            this.preparedStatement = ps;
            this.statement = statement;
        }
        
    }

    static
    {
        ThreadPool.addPurifier(  new CSQLPurifier( CONNECTIONS));

        long logSlowStackTraceTime=30000;
        try
        {
            String temp = System.getProperty( PROPERTY_LOG_SLOW_STACK_TRACE_TIME, "" + logSlowStackTraceTime);
            logSlowStackTraceTime= Long.parseLong(temp);
        }
        finally
        {
            LOG_SLOW_STACK_TRACE_TIME = logSlowStackTraceTime;
        }

        int defaultTimeoutSecs=15 * 60;// 15 minutes default timeout
        try
        {
            String temp = System.getProperty( PROPERTY_DEFAULT_QUERY_TIMEOUT_SECONDS, "" + defaultTimeoutSecs);
            defaultTimeoutSecs= Integer.parseInt(temp);
        }
        finally
        {
            DEFAULT_QUERY_TIMEOUT_SECONDS = defaultTimeoutSecs;
        }
    }
}
