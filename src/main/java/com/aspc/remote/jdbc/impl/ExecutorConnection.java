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
import com.aspc.remote.jdbc.SoapSQLException;
import com.aspc.remote.util.misc.CLogger;
import java.sql.*;
import java.util.Properties;
import org.apache.commons.logging.Log;

/**
 * Remote Server database connection.
 * Implements a database connection through SOAP.
 *
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 * @author  Nigel Leck
 * @since 29 September 2006
 */
public abstract class ExecutorConnection implements Connection
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.jdbc.impl.ExecutorConnection");//#LOGGER-NOPMD
    protected Executor executor;
    private int transactionIsolationLevel = TRANSACTION_READ_COMMITTED;
    private boolean autoCommit;

    /**
     * JDBC via SOAP
     *
     * @param executor the executor
     *
     */
    public ExecutorConnection(final Executor executor)
    {
        this.executor=executor;
    }

    protected ExecutorConnection()
    {

    }
    /**
     *
     * @return the client
     */
    public Executor getExecutor()
    {
        return executor;
    }

    /** {@inheritDoc} */
    @Override
    public Statement createStatement() throws SQLException
    {
        return new ExecutorStatement( this);
    }

    /** {@inheritDoc} */
    @Override
    public PreparedStatement prepareStatement(final String sql) throws SQLException
    {
        return new ExecutorPreparedStatement( this, sql);
    }

    /**
     * We don't care about result set type as ours is always scrollable.
     *
     * {@inheritDoc}
     */
    @Override
     public PreparedStatement prepareStatement(
        String sql,
        int resultSetType,
    int resultSetConcurrency
    ) throws SQLException
    {
        return prepareStatement( sql);
    }

     /** {@inheritDoc} */
    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException
    {
        throw new SoapSQLException( "prepareStatement(String sql, String[] columnNames) - Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException
    {
        throw new SoapSQLException( "prepareStatement(String sql, int autoGeneratedKeys) - Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException
    {
        throw new SoapSQLException( "prepareStatement(String sql, int[] columnIndexes) - Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
    {
        throw new SoapSQLException( "prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) - Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public CallableStatement prepareCall(String sql) throws SQLException
    {
        throw new SoapSQLException( "prepareCall - Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public String nativeSQL(String sql) throws SQLException
    {
        return sql;
    }

    /** {@inheritDoc} */
    @Override
    public void setAutoCommit(final boolean autoCommit) throws SQLException
    {
        this.autoCommit = autoCommit;
    }

    /** {@inheritDoc} */
    @Override
    public boolean getAutoCommit() throws SQLException
    {
        return autoCommit;
    }

    /** {@inheritDoc} */
    @Override
    public void commit() throws SQLException
    {
        try
        {
            executor.execute( "COMMIT");
        }
        catch( Exception e)
        {
            throw new SoapSQLException( e.getMessage());//NOPMD
        }
    }

    /** {@inheritDoc} */
    @Override
    public void rollback() throws SQLException
    {
        try
        {
            executor.execute( "ROLLBACK");
        }
        catch( Exception e)
        {
            throw new SoapSQLException( e.getMessage());//NOPMD
        }
    }

    /** {@inheritDoc} */
    @Override
    public abstract void close() throws SQLException;

    /** {@inheritDoc} */
    @Override
    public abstract boolean isClosed() throws SQLException;

    /** {@inheritDoc} */
    @Override
    public DatabaseMetaData getMetaData() throws SQLException
    {
        return new ExecutorDatabaseMetaData( this);
    }

    /** {@inheritDoc} */
    @Override
    public void setReadOnly(boolean readOnly) throws SQLException
    {
        if( readOnly)
        {
            throw new SoapSQLException( "setReadOnly( true) - Not Supported");
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isReadOnly() throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public abstract void setCatalog(String catalog) throws SQLException;

    /** {@inheritDoc} */
    @Override
    public String getCatalog() throws SQLException
    {
        throw new SoapSQLException( "getCatalog() - Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public void setTransactionIsolation(int level) throws SQLException
    {
        if( level == TRANSACTION_READ_COMMITTED)
        {
            throw new SoapSQLException( "This Level is not supported by DBMS");
        }
        else
        {
            transactionIsolationLevel = level;
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getTransactionIsolation() throws SQLException
    {
        return transactionIsolationLevel;
    }

    /** {@inheritDoc} */
    @Override
    public SQLWarning getWarnings() throws SQLException
    {
        return null;//throw new SoapSQLException( "getWarnings() - Not Supported");
    }

    /** {@inheritDoc} */
    @SuppressWarnings("NoopMethodInAbstractClass")
    @Override
    public void clearWarnings() throws SQLException
    {

    }

    /** {@inheritDoc} */
    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency)
      throws SQLException
    {
        return new ExecutorStatement(this,resultSetType,resultSetConcurrency);
    }

    /** {@inheritDoc} */
    @Override
    public CallableStatement prepareCall(
        String sql,
        int resultSetType,
    int resultSetConcurrency
    ) throws SQLException
    {
        throw new SoapSQLException( "prepareCall(sql, resultSetType) - Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public java.util.Map getTypeMap() throws SQLException
    {
        throw new SoapSQLException( "getTypeMap() - Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public void setTypeMap(java.util.Map map) throws SQLException
    {
        throw new SoapSQLException( "setTypeMap( map) - Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
    {
        return createStatement();
    }

    /** {@inheritDoc} */
    @Override
    public int getHoldability() throws SQLException
    {
        throw new SoapSQLException( "getHoldability() - Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
    {
        throw new SoapSQLException( "prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) - Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException
    {
        throw new SoapSQLException( "releaseSavepoint(Savepoint savepoint) - Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public void rollback(Savepoint savepoint) throws SQLException
    {
        try
        {
            executor.execute( "ROLLBACK");
        }
        catch( Exception e)
        {
            throw new SoapSQLException( e.getMessage());//NOPMD
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setHoldability(int holdability) throws SQLException
    {
        throw new SoapSQLException( "setHoldability(int holdability) - Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public Savepoint setSavepoint() throws SQLException
    {
        throw new SoapSQLException( "setSavepoint() - Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public Savepoint setSavepoint(String name) throws SQLException
    {
        throw new SoapSQLException( "setSavepoint(String name) - Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public Clob createClob() throws SQLException
    {
        throw new SoapSQLException( "createClob() - Not Supported");
    }


    /** {@inheritDoc} */
    @Override
    public Blob createBlob() throws SQLException
    {
        throw new SoapSQLException( "createBlob() - Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public NClob createNClob() throws SQLException
    {
        throw new SoapSQLException( "Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public SQLXML createSQLXML() throws SQLException
    {
        throw new SoapSQLException( "Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isValid(int timeout) throws SQLException
    {
        throw new SoapSQLException( "Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public String getClientInfo(String name) throws SQLException
    {
        throw new SoapSQLException( "Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public Properties getClientInfo() throws SQLException
    {
        throw new SoapSQLException( "Not Supported");
    }

    /** {@inheritDoc}
     * @param ifc
     * @return the value
     * @throws SQLException if a database-access error occurs.
     */
    public Object createQueryObject(Class ifc) throws SQLException
    {
        throw new SoapSQLException( "Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public Object unwrap(Class iface) throws SQLException
    {
        throw new SoapSQLException( "Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isWrapperFor(Class iface) throws SQLException
    {
        throw new SoapSQLException( "Not Supported");
    }

    /** {@inheritDoc} */
    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc } */
    @Override
    public void setSchema(String schema) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc } */
    @Override
    public String getSchema() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc }
     * @param executor executor
     * @throws SQLException a serious problem
     */
    @Override
    public void abort(java.util.concurrent.Executor executor) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc }
     * @param executor executor
     * @param milliseconds milliseconds
     * @throws SQLException a serious problem
     */
    @Override
    public void setNetworkTimeout(java.util.concurrent.Executor executor, int milliseconds) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc } */
    @Override
    public int getNetworkTimeout() throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
