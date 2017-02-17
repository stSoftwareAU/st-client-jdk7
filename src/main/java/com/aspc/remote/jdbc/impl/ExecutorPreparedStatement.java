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

import com.aspc.remote.jdbc.SoapResultSet;
import com.aspc.remote.jdbc.SoapSQLException;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.StringUtilities;
import com.aspc.remote.util.misc.TimeUtil;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.TimeZone;
import org.apache.commons.logging.Log;

/**
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 * @author Nigel Leck
 * @since 16 February 2008
 */
public class ExecutorPreparedStatement extends ExecutorStatement implements PreparedStatement
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.jdbc.impl.ExecutorPreparedStatement");//#LOGGER-NOPMD
    private final String sql;

    private final ArrayList<String> parameters = new ArrayList<>();

    /**
     * a prepared statement
     *
     * @param soapConnection the connection
     * @param sql the SQL
     */
    public ExecutorPreparedStatement( final ExecutorConnection soapConnection, final String sql)
    {
        super( soapConnection);
        this.sql = sql;
    }

    /**
     * a prepared statement
     *
     * @param soapConnection the connection
     * @param sql the SQL
     * @param resultSetType The type of Result Set
     * @param resultSetConcurrency THe concurrency of ResultSet
     */

    public ExecutorPreparedStatement( final ExecutorConnection soapConnection, final String sql, final int resultSetType, final int resultSetConcurrency)
    {
        super(soapConnection,resultSetType,resultSetConcurrency);
        this.sql = sql;
    }

    /** {@inheritDoc} */
    @Override
    public ResultSet executeQuery() throws SQLException
    {
        String tempSQL = null;
        try
        {
            tempSQL = parseSQL();
        }
        catch(IndexOutOfBoundsException e)
        {
            throw new SQLException("Less parameters than expected");
        }

        resultSet = (SoapResultSet)iExecuteQuery(tempSQL);//NOPMD

        return resultSet;
    }

    private String parseSQL()
    {
        StringBuilder buffer = new StringBuilder();

        StringTokenizer st = new StringTokenizer( sql, "?", true);

        int arg=0;
        for( int i =0; st.hasMoreTokens(); i++)
        {
            String token = st.nextToken();

            if( token.equals( "?"))
            {
                String value = parameters.get( arg );
                arg++;
                buffer.append( value);
            }
            else
            {
                buffer.append( token);
            }
        }

        String temp = buffer.toString();

        return temp;
    }

    /** {@inheritDoc} */
    @Override
    public int executeUpdate() throws SQLException
    {
        String tempSQL = null;
        try
        {
            tempSQL=parseSQL();
        }
        catch(Exception e)
        {
            LOGGER.warn( sql + "\n" + parameters, e);
            throw new SQLException("error parsing SQL", e);
        }

        return executeUpdate(tempSQL);
    }

    private void put( int parameterIndex, String value) throws SQLException
    {
        if( parameterIndex <= 0)
        {
            throw new SQLException( parameterIndex + " must be 1 or greater");
        }

        if( parameterIndex - 1 < parameters.size())
        {
            parameters.set( parameterIndex - 1, value);
        }
        else
        {
            while( parameters.size() < parameterIndex -1 )
            {
                parameters.add("NULL");
            }

            parameters.add(value);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException
    {
        put(parameterIndex, "''");
    }

    /** {@inheritDoc} */
    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException
    {
        put(parameterIndex, "" + x);
    }

    /** {@inheritDoc} */
    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException
    {
        put(parameterIndex, "" + x);
    }

    /** {@inheritDoc} */
    @Override
    public void setShort(int parameterIndex, short x) throws SQLException
    {
        put(parameterIndex, "" + x);
    }

    /** {@inheritDoc} */
    @Override
    public void setInt(int parameterIndex, int x) throws SQLException
    {
        put(parameterIndex, "" + x);
    }

    /** {@inheritDoc} */
    @Override
    public void setLong(int parameterIndex, long x) throws SQLException
    {
        put(parameterIndex, "" + x);
    }

    /** {@inheritDoc} */
    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException
    {
        put(parameterIndex, "" + x);
    }

    /** {@inheritDoc} */
    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException
    {
        put(parameterIndex, "" + x);
    }

    /** {@inheritDoc} */
    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException
    {
        put(parameterIndex, "" + x);
    }

    /** {@inheritDoc} */
    @Override
    public void setString(int parameterIndex, String x) throws SQLException
    {
        put(parameterIndex, "'" + StringUtilities.replace(x, "'", "\\'") + "'");
    }

    /** {@inheritDoc} */
    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException
    {
        //throw new SoapSQLException("Not supported yet.");
        if(x == null)
        {
            put(parameterIndex,"''");
        }
        else
        {
            put(parameterIndex,"'"+ StringUtilities.replace(new String(x),"'","\\'")+"'");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException
    {
        put(parameterIndex, "'" + TimeUtil.format("d MMM yyyy", x, TimeZone.getDefault()) + "'");
    }

    /** {@inheritDoc} */
    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException
    {
        //throw new SoapSQLException("Not supported yet.");
        put(parameterIndex, "'" + TimeUtil.format("dd MMM yyyy HH:mm:ss", x, TimeZone.getDefault()) + "'");
    }

    /** {@inheritDoc} */
    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException
    {
        put(parameterIndex, "'" + TimeUtil.format("dd MMM yyyy HH:mm:ss", x, TimeZone.getDefault()) + "'");
    }

    /** {@inheritDoc} */
    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException
    {
        throw new SoapSQLException("Not supported yet.");
    }

    /** {@inheritDoc}
     * @deprecated don't use.
     */
    @Deprecated
    @Override
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException
    {
        throw new SoapSQLException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException
    {
        throw new SoapSQLException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void clearParameters() throws SQLException
    {
        parameters.clear();
    }

    /** {@inheritDoc}
     * @param scale the scale
     */
    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType,
            int scale) throws SQLException
    {
        //throw new SoapSQLException("Not supported yet.");
        switch(targetSqlType)
        {
            case Types.DECIMAL:
            case Types.NUMERIC:
                BigDecimal bigDec = null;
                if(x instanceof BigDecimal)
                {
                    bigDec = (BigDecimal)x;
                }
                else if(x instanceof String )
                {
                    bigDec = new BigDecimal((String)x);
                }
                else if (x instanceof Byte || x instanceof Short || x instanceof Integer ||x instanceof Long)
                {
                    bigDec = new BigDecimal(((Number)x).longValue());
                }
                else if (x instanceof Float || x instanceof Double)
                {
                    bigDec = new BigDecimal(((Number)x).doubleValue());
                }
                else if(x instanceof Boolean )
                {
                    if( ((Boolean)x))
                    {
                        bigDec = BigDecimal.ONE;
                    }
                    else
                    {
                        bigDec = BigDecimal.ZERO;
                    }
                }
                assert bigDec!=null;
                setBigDecimal(parameterIndex,bigDec.setScale(scale,BigDecimal.ROUND_HALF_EVEN));
                break;
            default:
                throw new SQLException("Not yet supported");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException
    {
        //throw new SoapSQLException("Not supported yet.");
        switch(targetSqlType)
        {
            case Types.TINYINT:
                if(x instanceof Number)
                {
                    setByte(parameterIndex,((Number)x).byteValue());
                }
                else if (x instanceof String)
                {
                    setByte(parameterIndex,Byte.valueOf((String)x));
                }
                else if (x instanceof Boolean)
                {
                    int b = (((Boolean)x))?1:0;
                    setByte(parameterIndex,(byte)b );
                }
                break;
            case Types.SMALLINT:
                if( x instanceof Number)
                {
                    setShort(parameterIndex,((Number)x).shortValue());
                }
                else if( x instanceof String)
                {
                    setShort(parameterIndex,Short.valueOf((String)x));
                }
                else if (x instanceof Boolean)
                {
                    int b = (((Boolean)x))?1:0;
                    setShort(parameterIndex,(short)b );
                }
                break;
            case  Types.INTEGER:
                if( x instanceof Number)
                {
                    setInt(parameterIndex,((Number)x).intValue());
                }
                else if(x instanceof String)
                {
                    setInt(parameterIndex,Integer.parseInt((String)x));
                }
                else if (x instanceof Boolean)
                {
                    int b = (((Boolean)x))?1:0;
                    setInt(parameterIndex,b );
                }
                break;
            case Types.BIGINT:
                if(x instanceof Number)
                {
                    setLong(parameterIndex,((Number)x).longValue());
                }
                else if( x instanceof String)
                {
                    setLong(parameterIndex,Long.parseLong((String)x));
                }
                else if (x instanceof Boolean)
                {
                    int b = (((Boolean)x))?1:0;
                    setLong(parameterIndex,b );
                }
                break;
            case Types.REAL:
               if(x instanceof Number)
               {
                setFloat(parameterIndex,((Number)x).floatValue());
               }
               else if (x instanceof String )
               {
                setFloat(parameterIndex,Float.valueOf((String)x));
               }
                else if (x instanceof Boolean)
                {
                    int b = (((Boolean)x))?1:0;
                    setFloat(parameterIndex,b );
                }
                break;
           case Types.FLOAT:
               if(x instanceof Number)
               {
                setDouble(parameterIndex,((Number)x).doubleValue());
               }
               else if (x instanceof String )
               {
                setDouble(parameterIndex,Double.valueOf((String)x));
               }
                else if (x instanceof Boolean)
                {
                    int b = (((Boolean)x))?1:0;
                    setFloat(parameterIndex,b );
                }
                break;
            case Types.DOUBLE:
                    if(x instanceof Number)
                    {
                        setDouble(parameterIndex,((Number)x).doubleValue());
                    }
                    else if( x instanceof String)
                    {
                        setDouble(parameterIndex,Double.valueOf((String)x));
                    }
                else if (x instanceof Boolean)
                {
                    int b = (((Boolean)x))?1:0;
                    setDouble(parameterIndex,b );
                }
                break;
            case Types.BIT:
            case Types.BOOLEAN:
                if(x instanceof Boolean)
                {
                    setBoolean(parameterIndex,(Boolean)x);
                }
                else if(x instanceof String)
                {
                    setBoolean(parameterIndex,Boolean.valueOf((String)x));
                }
                else if(x instanceof Number)
                {
                    setBoolean(parameterIndex,(((Number)x).longValue() == 1));
                }
                break;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                if(x instanceof String)
                {
                    setString(parameterIndex,(String)x);
                }
                else if(x instanceof Number)
                {
                    setString(parameterIndex,((Number)x).toString());
                }
                else if (x instanceof Boolean)
                {
                    String b= (((Boolean)x))?"true":"false";
                    setString(parameterIndex,b );
                }
                else if(x instanceof java.sql.Date)
                {
                    setString(parameterIndex,((java.sql.Date)x).toString());
                }
                else if(x instanceof java.sql.Timestamp)
                {
                    setString(parameterIndex,((java.sql.Timestamp)x).toString());
                }
                else if(x instanceof java.sql.Time)
                {
                    setString(parameterIndex,((java.sql.Time)x).toString());
                }
                break;
            case Types.DATE:
                if( x instanceof java.sql.Date)
                {
                    setDate(parameterIndex,(java.sql.Date)x);
                }
                else
                {
                    setDate(parameterIndex,java.sql.Date.valueOf((String)x));
                }
                break;
            case Types.TIME:
                if( x instanceof Time)
                {
                    setTime(parameterIndex,(Time)x);
                }
                else if ( x instanceof String )
                {
                    setTime(parameterIndex,Time.valueOf((String)x));
                }
                break;
            case Types.TIMESTAMP:
                if(x instanceof Timestamp)
                {
                    setTimestamp(parameterIndex,(Timestamp)x);
                }
                else if(x instanceof String)
                {
                    setTimestamp(parameterIndex,Timestamp.valueOf((String)x));
                }
                else if(x instanceof Date)
                {
                    setTimestamp(parameterIndex,new Timestamp(((java.sql.Date)x).getTime()));
                }
                break;
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                setBytes(parameterIndex,(byte[])x);
                break;
            default:
                throw new SQLException("Not yet supported");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException
    {
        //throw new SoapSQLException("Not supported yet.");
        if(x == null)
        {
            setNull(parameterIndex,Types.CHAR);
        }
        else if( x instanceof Byte)
        {
            setByte(parameterIndex,(Byte)x);
        }
        else if(x instanceof Short)
        {
            setShort(parameterIndex,(Short)x);
        }
        else if (x instanceof Integer)
        {
            setInt(parameterIndex,(Integer)x);
        }
        else if(x instanceof Long)
        {
            setLong(parameterIndex,(Long)x);
        }
        else if( x instanceof Float)
        {
            setFloat(parameterIndex,(Float)x);
        }
        else if(x instanceof Double)
        {
            setDouble(parameterIndex,(Double)x);
        }
        else if(x instanceof Boolean)
        {
            setBoolean(parameterIndex,(Boolean)x);
        }
        else if(x instanceof BigDecimal)
        {
            setBigDecimal(parameterIndex,(BigDecimal)x);
        }
        else if( x instanceof java.sql.Date)
        {
            setDate(parameterIndex,(java.sql.Date)x);
        }
        else if(x instanceof java.sql.Time)
        {
            setTime(parameterIndex,(java.sql.Time)x);
        }
        else if(x instanceof java.sql.Timestamp)
        {
            setTimestamp(parameterIndex,(java.sql.Timestamp)x);
        }
        else if(x instanceof byte[])
        {
            setBytes(parameterIndex,(byte[])x);
        }
        else if (x instanceof String )
        {
            setString(parameterIndex,(String)x);
        }
        else
        {
            throw new SQLException("Not Supported Type");
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean execute() throws SQLException
    {
        String tempSQL = null;
        try
        {
            tempSQL = parseSQL();
        }
        catch(Exception e)
        {
            LOGGER.warn( sql + "\n" + parameters, e);
            throw new SQLException("error parsing SQL", e);
        }

        return execute(tempSQL);
    }

    /** {@inheritDoc} */
    @Override
    public void addBatch() throws SQLException
    {
        String parsedSql = parseSQL();
        if(sqlBatch == null)
        {
            sqlBatch = parsedSql;
        }
        else
        {
            sqlBatch  = sqlBatch + ";" + parsedSql;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException
    {
        throw new SoapSQLException("Not supported yet.");
    }

    /** {@inheritDoc}
     * @param i
     */
    @Override
    public void setRef(int i, Ref x) throws SQLException
    {
        throw new SoapSQLException("Not supported yet.");
    }

    /** {@inheritDoc}
     * @param i
     */
    @Override
    public void setBlob(int i, Blob x) throws SQLException
    {
        throw new SoapSQLException("Not supported yet.");
    }

    /** {@inheritDoc}
     * @param i
     */
    @Override
    public void setClob(int i, Clob x) throws SQLException
    {
        throw new SoapSQLException("Not supported yet.");
    }

    /** {@inheritDoc}
     * @param i
     */
    @Override
    public void setArray(int i, Array x) throws SQLException
    {
        throw new SoapSQLException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public ResultSetMetaData getMetaData() throws SQLException
    {
        if( resultSet != null)
        {
            return resultSet.getMetaData();
        }
        throw new SoapSQLException("no result set");
    }

    /** {@inheritDoc} */
    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException
    {
        //throw new SoapSQLException("Not supported yet.");
        put(parameterIndex, "'" + TimeUtil.format("d MMM yyyy", x, cal.getTimeZone()) + "'");
    }

    /** {@inheritDoc} */
    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException
    {
        //throw new SoapSQLException("Not supported yet.");
        put(parameterIndex, "'" + TimeUtil.format("d MMM yyyy HH:mm:ss", x, cal.getTimeZone()) + "'");
    }

    /** {@inheritDoc} */
    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException
    {
        //throw new SoapSQLException("Not supported yet.");
        put(parameterIndex, "'" + TimeUtil.format("dd MMM yyyy HH:mm:ss", x, cal.getTimeZone()) + "'");
    }

    /** {@inheritDoc}
     * @param paramIndex
     */
    @Override
    public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException
    {
        throw new SoapSQLException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException
    {
        throw new SoapSQLException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException
    {
        throw new SoapSQLException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void setNString(int parameterIndex, String value) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
