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
package com.aspc.remote.jdbc.impl;

import com.aspc.remote.jdbc.Executor;
import com.aspc.remote.jdbc.SoapResultSet;
import com.aspc.remote.jdbc.SoapSQLException;
import com.aspc.remote.soap.Constants;
import com.aspc.remote.util.misc.CLogger;
import java.sql.*;
import org.apache.commons.logging.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *  soap statement
 *
 * <i>THREAD MODE: SINGLE-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       29 September 2006
 */
public class ExecutorStatement implements Statement
{
    /** connection */
    protected final ExecutorConnection executorConnection;

    /** the last result set */
    protected SoapResultSet resultSet;

    private int fetchDirection = ResultSet.FETCH_FORWARD;
    private int resultSetType = ResultSet.TYPE_SCROLL_INSENSITIVE;
    private int resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;
    private int fetchSize =0;
    private int maxRows = 0;
    private boolean isClosedFg = false;
    /**
     * SQL Batch Statements
     */
    protected String sqlBatch = null;
    /**
     *  Querry Timeout
     */
    protected int queryTimeout = 0;
    private int maxFieldSize = 999;

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.jdbc.impl.ExecutorStatement");//#LOGGER-NOPMD

    /**
     *
     * @param soapConnection the connection
     */
    public ExecutorStatement(final ExecutorConnection soapConnection)
    {
        this.executorConnection = soapConnection;
    }

    /**
     *
     * @param soapConnection the connection
     * @param resultSetType the resultSet type
     * @param  resultSetConcurrency the resultSet Concurrency
     */
    public ExecutorStatement(final ExecutorConnection soapConnection, final int resultSetType, final int resultSetConcurrency)
    {
        this.executorConnection = soapConnection;
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet executeQuery(String sql) throws SQLException
    {
        if ( isClosedFg )
        {
            throw new SQLException("Statement closed");
        }
        Executor rc = executorConnection.getExecutor();

        try
        {
            resultSet = rc.fetch( sql);
            if( executorConnection.getAutoCommit() && rc.isStateless() == false )
            {
                executorConnection.commit();
            }

            if(resultSet.isUpdateResult())
            {
                throw new SoapSQLException("Non-resultset query");
            }
            resultSet.setFetchDirection(fetchDirection);
            resultSet.setFetchSize(fetchSize);
            return resultSet;
        }
        catch( Exception e)
        {
            LOGGER.error("Error",e);
            throw new SoapSQLException( e.toString());//NOPMD
        }
    }

    /**
     * @param sql The SQL query
     * @return ResultSet Returned ResultSet
     * @throws java.sql.SQLException A serious problem
     */
    protected ResultSet iExecuteQuery(String sql) throws SQLException
    {
        if ( isClosedFg )
        {
            throw new SQLException("Statement closed");
        }
        Executor rc = executorConnection.getExecutor();

        try
        {
            resultSet = rc.fetch( sql);
            if( executorConnection.getAutoCommit()&& rc.isStateless() == false )
            {
                executorConnection.commit();
            }
            resultSet.setFetchDirection(fetchDirection);
            resultSet.setFetchSize(fetchSize);
            return resultSet;
        }
        catch( Exception e)
        {
            LOGGER.error("Error",e);
            throw new SoapSQLException( e.toString());//NOPMD
        }
    }

    /** {@inheritDoc} */
    @Override
    public int executeUpdate(String sql) throws SQLException
    {
        SoapResultSet result =(SoapResultSet)iExecuteQuery( sql);
        if( !result.isUpdateResult())
        {
            throw new SoapSQLException("SQL Producing a ResultSet");
        }
        return result.getUpdateCount();
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws SQLException
    {
        if( resultSet != null)
        {
            resultSet.close();
            resultSet = null;
        }
        isClosedFg = true;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxFieldSize() throws SQLException
    {
        return maxFieldSize;
    }

    /** {@inheritDoc} */
    @Override
    public void setMaxFieldSize(int max) throws SQLException
    {
        if( max <= 0)
        {
            throw new SoapSQLException("Invalid Max Field Value");
        }
        maxFieldSize = max;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxRows() throws SQLException
    {
        //throw new SoapSQLException( "Not supported");
        if(isClosedFg)
        {
            throw new SQLException("Statement Closed");
        }
        else
        {
            return maxRows;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setMaxRows(int max) throws SQLException
    {
        //throw new SoapSQLException( "Not supported");
        if(isClosedFg )
        {
            throw new SQLException("Statement Closed");
        }
        else if(max < 0)
        {
            throw new SQLException("Invalid Max Rows");
        }
        else
        {
            maxRows = max;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException
    {
        //if( enable == false)
        //{
        //    throw new SoapSQLException( "setEscapeProcessing(false) - Not supported");
        //}
    }

    /** {@inheritDoc} */
    @Override
    public int getQueryTimeout() throws SQLException
    {
        return queryTimeout;
    }

    /** {@inheritDoc} */
    @Override
    public void setQueryTimeout(int seconds) throws SQLException
    {
        if( seconds < 0)
        {
            throw new SoapSQLException("Invalid Query Timeout Value");
        }
        queryTimeout = seconds;
    }

    /** {@inheritDoc} */
    @Override
    public void cancel() throws SQLException
    {
        throw new SoapSQLException( "Not supported");
    }

    /** {@inheritDoc} */
    @Override
    public SQLWarning getWarnings() throws SQLException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void clearWarnings() throws SQLException
    {
        throw new SoapSQLException( "Not supported");
    }

    /** {@inheritDoc} */
    @Override
    public void setCursorName(String name) throws SQLException
    {
        throw new SoapSQLException( "Not supported");
    }

    /** {@inheritDoc} */
    @Override
    public boolean execute(final String sql) throws SQLException
    {
        resultSet=null;
        Executor rc = executorConnection.getExecutor();

        try
        {
            Document doc = rc.execute(sql);

            if( executorConnection.getAutoCommit() && rc.isStateless() == false )
            {
                executorConnection.commit();
            }

            NodeList list = doc.getElementsByTagName( Constants.ELM_RESULTSET);
            if( list.getLength() > 0)
            {
                Element element = (Element) list.item(0);
                boolean executeOnly = false;
                if( element.hasAttribute( Constants.ATT_QUERY))
                {
                    String value = element.getAttribute(Constants.ATT_QUERY);

                    if( value.equalsIgnoreCase("false"))
                    {
                        executeOnly = true;
                    }
                }

                if( executeOnly == false)
                {
                    resultSet = new SoapResultSet( doc, 0, rc);
                    resultSet.setFetchDirection(fetchDirection);
                    resultSet.setFetchSize(fetchSize);
                }
            }
        }
        catch( Exception e)
        {
            LOGGER.error("Error",e);
            throw new SoapSQLException( e.toString());//NOPMD
        }

        return resultSet != null && (!resultSet.isUpdateResult());
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getResultSet() throws SQLException
    {
        if (null == resultSet)
        {
            return null;
        }
        return (resultSet.isUpdateResult())?null:resultSet;
    }

    /** {@inheritDoc} */
    @Override
    public int getUpdateCount() throws SQLException
    {
        return resultSet.getUpdateCount();
    }

    /** {@inheritDoc} */
    @Override
    public boolean getMoreResults() throws SQLException
    {
        if( resultSet == null) return false;

        int rsCount = resultSet.getSetCount();

        try
        {
            SoapResultSet t = resultSet.getResultSet( rsCount + 1);

            if( t == null) return false;

            resultSet = t;

            return true;
        }
        catch( Exception e)
        {
            throw new SoapSQLException( e.getMessage());//NOPMD
        }
    }

    //--------------------------JDBC 2.0-----------------------------

    /** {@inheritDoc} */
    @Override
    public void setFetchDirection(int direction) throws SQLException
    {
        //throw new SoapSQLException( "Not supported");
        if(isClosedFg)
        {
            throw new SQLException("Statement Closed");
        }
        else if(direction !=ResultSet.FETCH_FORWARD && direction!=ResultSet.FETCH_REVERSE && direction !=ResultSet.FETCH_UNKNOWN)
        {
            throw new SQLException("Invalid Fetch Direction");
        }
        else
        {
            fetchDirection=direction;
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getFetchDirection() throws SQLException
    {
        //throw new SoapSQLException( "Not supported");
        if(!isClosedFg)
        {
            return fetchDirection;
        }
        else
        {
            throw new SQLException("Closed Statement");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setFetchSize(int rows) throws SQLException
    {
        //throw new SoapSQLException( "Not supported");
        if(isClosedFg)
        {
            throw new SQLException("Closed Statement");
        }
        else if(rows < 0)
        {
            throw new SQLException("Invalid Fetch Size");
        }
        else
        {
            fetchSize=rows;
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getFetchSize() throws SQLException
    {
        //throw new SoapSQLException( "Not supported");
        if(isClosedFg)
        {
            throw new SQLException("Closed Statement");
        }
        else
        {
            return fetchSize;
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getResultSetConcurrency() throws SQLException
    {
        return resultSetConcurrency;
    }

    /** {@inheritDoc} */
    @Override
    public int getResultSetType()  throws SQLException
    {
        return resultSetType;
    }

    /** {@inheritDoc} */
    @Override
    public void addBatch( String sql ) throws SQLException
    {
        if(sqlBatch == null)
        {
            sqlBatch = sql;
        }
        else
        {
            sqlBatch  = sqlBatch + ";" + sql;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void clearBatch() throws SQLException
    {
        sqlBatch = null;
    }

    /** {@inheritDoc} */
    @Override
    public int[] executeBatch() throws SQLException
    {
        int[] uCnt = null;
        int[] retUpCnt;

        if( sqlBatch == null)
        {
            uCnt = new int[0];
        }
        else if( isClosedFg )
        {
            throw new SoapSQLException("Statement Closed");
        }
        else
        {
            String[] queries = sqlBatch.split(";");
            uCnt = new int[queries.length];
            for( int i=0;i<queries.length;i++)
            {
                try
                {
                    uCnt[i] = executeUpdate(queries[i]);
                }
                catch(SQLException sqle)
                {
                    retUpCnt = new int[i];
                    System.arraycopy(uCnt, 0, retUpCnt, 0, i);
                    throw new BatchUpdateException(sqle.getMessage(),retUpCnt);
                }
            }
        }
        return uCnt;
    }

    /** {@inheritDoc} */
    @Override
    public Connection getConnection()  throws SQLException
    {
        return executorConnection;
    }

    /** {@inheritDoc} */
    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException
    {
        throw new SoapSQLException( "Not supported");
    }

    /** {@inheritDoc} */
    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException
    {
        throw new SoapSQLException( "Not supported");
    }

    /** {@inheritDoc} */
    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException
    {
        throw new SoapSQLException( "Not supported");
    }

    /** {@inheritDoc} */
    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException
    {
        throw new SoapSQLException( "Not supported");
    }

    /** {@inheritDoc} */
    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException
    {
        throw new SoapSQLException( "Not supported");
    }

    /** {@inheritDoc} */
    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException
    {
        throw new SoapSQLException( "Not supported");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getGeneratedKeys() throws SQLException
    {
        throw new SoapSQLException( "Not supported");
    }

    /** {@inheritDoc} */
    @Override
    public boolean getMoreResults(int current) throws SQLException
    {
        if( resultSet == null) return false;

        try
        {
            SoapResultSet t = resultSet.getResultSet( current);

            if( t == null) return false;

            resultSet = t;

            return true;
        }
        catch( Exception e)
        {
            throw new SoapSQLException( e.getMessage());//NOPMD
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getResultSetHoldability() throws SQLException
    {
        throw new SoapSQLException( "Not supported");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isClosed() throws SQLException
    {
        throw new SoapSQLException( "Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public void setPoolable(boolean poolable) throws SQLException
    {
        throw new SoapSQLException( "Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPoolable() throws SQLException
    {
        throw new SoapSQLException( "Not Supported");
    }

    /** {@inheritDoc}
     * @param <T>
     */
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        throw new SoapSQLException( "Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        throw new SoapSQLException( "Not Supported");
    }

    /** {@inheritDoc } */
    @Override
    public void closeOnCompletion() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc } */
    @Override
    public boolean isCloseOnCompletion() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
