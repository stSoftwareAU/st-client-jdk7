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
package com.aspc.remote.database.internal.wrapper;

import com.aspc.developer.ThreadCop;
import com.aspc.remote.jdbc.SoapSQLException;
import com.aspc.remote.util.misc.CLogger;
import java.sql.*;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import org.apache.commons.logging.Log;

/**
 *  soap statement
 *
 * <i>THREAD MODE: SINGLE-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       29 September 2006
 */
public class WrapperStatement implements Statement
{
//    private RuntimeException alreadyClosedException;
    private boolean closed;
    private static final StackTraceElement[] DUMMY_STACK=new StackTraceElement[0];
    private final StackTraceElement[] stackTrace=DUMMY_STACK;
    private StringBuilder batchSQL;

    /**
     *
     * @param wrapperConnection the connection
     * @param statement the statement
     */
    public WrapperStatement(final WrapperConnection wrapperConnection, final Statement statement)
    {
        this.wrapperConnection = wrapperConnection;
        this.statement=statement;

        //stackTrace = Thread.currentThread().getStackTrace();
    }

    @Override @CheckReturnValue @Nonnull
    public String toString()
    {
        String temp="";
        if( batchSQL != null)
        {
            temp = ", batch= " + batchSQL;
        }
        return "WrapperStatement{ statement=" + statement + temp + '}';
    }

    /**
     * What stack trace opened this connection
     * @return the stack trace
     */
    public StackTraceElement[] openedByStackTrace()
    {
        return stackTrace.clone();
    }

    private void checkValid()
    {
        if( closed)
        {
            throw new WrapperAssertError("connection already closed");
        }

        assert ThreadCop.access(this);
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet executeQuery(final String sql) throws SQLException
    {
        checkValid();
        return wrap( statement.executeQuery(sql));
    }

    /** {@inheritDoc} */
    @Override
    public int executeUpdate(final String sql) throws SQLException
    {
        checkValid();
        if( lastRS != null)
        {
            lastRS.close();
            lastRS=null;
        }
        return statement.executeUpdate(sql);
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws SQLException
    {
        try
        {
            checkValid();
            closed=true;
            //alreadyClosedException=new RuntimeException( "closed by thread " + Thread.currentThread());

            if( lastRS != null)
            {
                lastRS.close();
                lastRS=null;
            }
            batchSQL=null;
            statement.close();
            wrapperConnection.closeStatement( this);
        }
        catch( SQLException e)
        {
            LOGGER.warn("could not close", e);
            throw e;
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxFieldSize() throws SQLException
    {
        checkValid();
        return statement.getMaxFieldSize();
    }

    /** {@inheritDoc} */
    @Override
    public void setMaxFieldSize(int max) throws SQLException
    {
        checkValid();
        statement.setMaxFieldSize(max);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaxRows() throws SQLException
    {
        checkValid();
        return statement.getMaxRows();
    }

    /** {@inheritDoc} */
    @Override
    public void setMaxRows(int max) throws SQLException
    {
        checkValid();
        statement.setMaxRows(max);
    }

    /** {@inheritDoc} */
    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException
    {
        checkValid();
        statement.setEscapeProcessing(enable);
    }

    /** {@inheritDoc} */
    @Override
    public int getQueryTimeout() throws SQLException
    {
        checkValid();
        return statement.getQueryTimeout();
    }

    /** {@inheritDoc} */
    @Override
    public void setQueryTimeout(int seconds) throws SQLException
    {
        checkValid();
        statement.setQueryTimeout(seconds);
    }

    /** {@inheritDoc} */
    @Override
    public void cancel() throws SQLException
    {
        checkValid();
        throw new SoapSQLException( "Not supported");
    }

    /** {@inheritDoc} */
    @Override
    public SQLWarning getWarnings() throws SQLException
    {
        checkValid();
        return statement.getWarnings();
    }

    /** {@inheritDoc} */
    @Override
    public void clearWarnings() throws SQLException
    {
        checkValid();
        statement.clearWarnings();
    }

    /** {@inheritDoc} */
    @Override
    public void setCursorName(String name) throws SQLException
    {
        checkValid();
        statement.setCursorName(name);
    }

    /** {@inheritDoc} */
    @Override
    public boolean execute(final String sql) throws SQLException
    {
        checkValid();
        return statement.execute(sql);
    }

    /**
     * wrap up a result set
     * @param rs result set.
     * @return the result set.
     */
    public WrapperResultSet wrap( final ResultSet rs)
    {
        WrapperResultSet tmpRS = new WrapperResultSet( rs);

        assert ThreadCop.monitor(tmpRS, ThreadCop.MODE.ACCESS_ONLY_BY_CREATING_THREAD);
        lastRS = tmpRS;

        return tmpRS;
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getResultSet() throws SQLException
    {
        checkValid();
        if( lastRS != null) return lastRS;


        return wrap ( statement.getResultSet());
    }

    /** {@inheritDoc} */
    @Override
    public int getUpdateCount() throws SQLException
    {
        checkValid();
        return statement.getUpdateCount();
    }

    /** {@inheritDoc} */
    @Override
    public boolean getMoreResults() throws SQLException
    {
        checkValid();
        lastRS=null;
        return statement.getMoreResults();
    }

    //--------------------------JDBC 2.0-----------------------------

    /** {@inheritDoc} */
    @Override
    public void setFetchDirection(int direction) throws SQLException
    {
        checkValid();
        statement.setFetchDirection(direction);
    }

    /** {@inheritDoc} */
    @Override
    public int getFetchDirection() throws SQLException
    {
        checkValid();
        return statement.getFetchDirection();
    }

    /** {@inheritDoc} */
    @Override
    public void setFetchSize(int rows) throws SQLException
    {
        checkValid();
        statement.setFetchSize(rows);
    }

    /** {@inheritDoc} */
    @Override
    public int getFetchSize() throws SQLException
    {
        checkValid();
        return statement.getFetchSize();
    }

    /** {@inheritDoc} */
    @Override
    public int getResultSetConcurrency() throws SQLException
    {
        checkValid();
        return statement.getResultSetConcurrency();
    }

    /** {@inheritDoc} */
    @Override
    public int getResultSetType()  throws SQLException
    {
        checkValid();
        return statement.getResultSetType();
    }

    /** {@inheritDoc} */
    @Override
    public void addBatch( final String sql ) throws SQLException
    {
        checkValid();
        if( batchSQL == null)
        {
            batchSQL=new StringBuilder();
        }
        else
        {
            batchSQL.append(";\n");
        }
        batchSQL.append(sql);
        statement.addBatch(sql);
    }

    /** {@inheritDoc} */
    @Override
    public void clearBatch() throws SQLException
    {
        checkValid();
        batchSQL=null;
        statement.clearBatch();
    }

    /** {@inheritDoc} */
    @Override
    public int[] executeBatch() throws SQLException
    {
        checkValid();
        batchSQL=null;
        return statement.executeBatch();
    }

    /** {@inheritDoc} */
    @Override
    public Connection getConnection()  throws SQLException
    {
        checkValid();
        return wrapperConnection;
    }

    /** {@inheritDoc} */
    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException
    {
        checkValid();
        throw new SoapSQLException( "Not supported");
    }

    /** {@inheritDoc} */
    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException
    {
        checkValid();
        throw new SoapSQLException( "Not supported");
    }

    /** {@inheritDoc} */
    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException
    {
        checkValid();
        throw new SoapSQLException( "Not supported");
    }

    /** {@inheritDoc} */
    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException
    {
        checkValid();
        throw new SoapSQLException( "Not supported");
    }

    /** {@inheritDoc} */
    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException
    {
        checkValid();
        throw new SoapSQLException( "Not supported");
    }

    /** {@inheritDoc} */
    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException
    {
        checkValid();
        throw new SoapSQLException( "Not supported");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet getGeneratedKeys() throws SQLException
    {
        checkValid();
        throw new SoapSQLException( "Not supported");
    }

    /** {@inheritDoc} */
    @Override
    public boolean getMoreResults(int current) throws SQLException
    {
        checkValid();
        lastRS=null;
        return statement.getMoreResults(current);
    }

    /** {@inheritDoc} */
    @Override
    public int getResultSetHoldability() throws SQLException
    {
        checkValid();
        throw new SoapSQLException( "Not supported");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isClosed() throws SQLException
    {
        checkValid();
        throw new SoapSQLException( "Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public void setPoolable(boolean poolable) throws SQLException
    {
        checkValid();
        throw new SoapSQLException( "Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPoolable() throws SQLException
    {
        checkValid();
        throw new SoapSQLException( "Not Supported");
    }

    /** {@inheritDoc}
     * @param <T> the type
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
        checkValid();
        throw new SoapSQLException( "Not Supported");
    }

    /** connection */
    private final WrapperConnection wrapperConnection;
    /** the statement */
    protected final Statement statement;
    private ResultSet lastRS;

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.database.internal.wrapper.WrapperStatement");//#LOGGER-NOPMD

    /** {@inheritDoc } */
    @Override
    public void closeOnCompletion() throws SQLException
    {
        checkValid();
        close();
    }

    /** {@inheritDoc } */
    @Override
    public boolean isCloseOnCompletion() throws SQLException
    {
        checkValid();
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
