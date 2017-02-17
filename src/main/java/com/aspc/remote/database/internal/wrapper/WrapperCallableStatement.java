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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

/**
 *  soap statement
 *
 * <i>THREAD MODE: SINGLE-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       29 September 2006
 */
public class WrapperCallableStatement extends WrapperStatement implements CallableStatement
{
    /**
     *
     * @param wrapperConnection the connection
     * @param statement the statement
     */
    public WrapperCallableStatement(final WrapperConnection wrapperConnection, final CallableStatement statement)
    {
        super(wrapperConnection, statement );
    }

    /** {@inheritDoc } */
    @Override
    public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException
    {
        ((CallableStatement)statement).registerOutParameter(  parameterIndex, sqlType);
    }

    /** {@inheritDoc } */
    @Override
    public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException
    {
        ((CallableStatement)statement).registerOutParameter(  parameterIndex, sqlType, scale);
    }

    /** {@inheritDoc } */
    @Override
    public boolean wasNull() throws SQLException
    {
        return ((CallableStatement)statement).wasNull(  );
    }

    /** {@inheritDoc } */
    @Override
    public String getString(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)statement).getString( parameterIndex );
    }

    /** {@inheritDoc } */
    @Override
    public boolean getBoolean(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)statement).getBoolean( parameterIndex );
    }

    /** {@inheritDoc } */
    @Override
    public byte getByte(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)statement).getByte( parameterIndex );
    }

    /** {@inheritDoc } */
    @Override
    public short getShort(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)statement).getShort( parameterIndex );
    }

    /** {@inheritDoc } */
    @Override
    public int getInt(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)statement).getInt( parameterIndex );
    }

    /** {@inheritDoc } */
    @Override
    public long getLong(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)statement).getLong( parameterIndex );
    }

    /** {@inheritDoc } */
    @Override
    public float getFloat(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)statement).getFloat( parameterIndex );
    }

    /** {@inheritDoc } */
    @Override
    public double getDouble(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)statement).getDouble( parameterIndex );
    }

    /** {@inheritDoc } */
    @Override
    @SuppressWarnings({"deprecation", "deprecation"})
    public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException
    {
        return ((CallableStatement)statement).getBigDecimal( parameterIndex, scale );
    }

    /** {@inheritDoc } */
    @Override
    public byte[] getBytes(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)statement).getBytes( parameterIndex );
    }

    /** {@inheritDoc } */
    @Override
    public Date getDate(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)statement).getDate( parameterIndex );
    }

    /** {@inheritDoc } */
    @Override
    public Time getTime(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)statement).getTime( parameterIndex );
    }

    /** {@inheritDoc } */
    @Override
    public Timestamp getTimestamp(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)statement).getTimestamp( parameterIndex );
    }

    /** {@inheritDoc } */
    @Override
    public Object getObject(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)statement).getObject( parameterIndex );
    }

    /** {@inheritDoc } */
    @Override
    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)statement).getBigDecimal( parameterIndex );
    }

    /** {@inheritDoc } */
    @Override
    public Object getObject(int parameterIndex, Map<String, Class<?>> map) throws SQLException
    {
        return ((CallableStatement)statement).getObject( parameterIndex, map );
    }

    /** {@inheritDoc } */
    @Override
    public Ref getRef(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)statement).getRef( parameterIndex );
    }

    /** {@inheritDoc } */
    @Override
    public Blob getBlob(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)statement).getBlob( parameterIndex );
    }

    /** {@inheritDoc } */
    @Override
    public Clob getClob(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)statement).getClob( parameterIndex );
    }

    /** {@inheritDoc } */
    @Override
    public Array getArray(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)statement).getArray( parameterIndex );
    }

    /** {@inheritDoc } */
    @Override
    public Date getDate(int parameterIndex, Calendar cal) throws SQLException
    {
        return ((CallableStatement)statement).getDate( parameterIndex, cal );
    }

    /** {@inheritDoc } */
    @Override
    public Time getTime(int parameterIndex, Calendar cal) throws SQLException
    {
        return ((CallableStatement)statement).getTime( parameterIndex, cal );
    }

    /** {@inheritDoc } */
    @Override
    public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException
    {
        return ((CallableStatement)statement).getTimestamp( parameterIndex, cal );
    }

    /** {@inheritDoc } */
    @Override
    public void registerOutParameter(int parameterIndex, int sqlType, String typeName) throws SQLException
    {
        ((CallableStatement)statement).registerOutParameter( parameterIndex, sqlType, typeName);
    }

    /** {@inheritDoc } */
    @Override
    public void registerOutParameter(String parameterName, int sqlType) throws SQLException
    {
        ((CallableStatement)statement).registerOutParameter( parameterName, sqlType);
    }

    /** {@inheritDoc } */
    @Override
    public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException
    {
        ((CallableStatement)statement).registerOutParameter( parameterName, sqlType, scale);
    }

    /** {@inheritDoc } */
    @Override
    public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException
    {
        ((CallableStatement)statement).registerOutParameter( parameterName, sqlType, typeName);
    }

    /** {@inheritDoc } */
    @Override
    public URL getURL(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)statement).getURL( parameterIndex);
    }

    /** {@inheritDoc } */
    @Override
    public void setURL(String parameterName, URL val) throws SQLException
    {
        ((CallableStatement)statement).setURL( parameterName, val);
    }

    /** {@inheritDoc } */
    @Override
    public void setNull(String parameterName, int sqlType) throws SQLException
    {
        ((CallableStatement)statement).setNull( parameterName, sqlType);
    }

    /** {@inheritDoc } */
    @Override
    public void setBoolean(String parameterName, boolean x) throws SQLException
    {
        ((CallableStatement)statement).setBoolean( parameterName, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setByte(String parameterName, byte x) throws SQLException
    {
        ((CallableStatement)statement).setByte( parameterName, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setShort(String parameterName, short x) throws SQLException
    {
        ((CallableStatement)statement).setShort( parameterName, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setInt(String parameterName, int x) throws SQLException
    {
        ((CallableStatement)statement).setInt( parameterName, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setLong(String parameterName, long x) throws SQLException
    {
        ((CallableStatement)statement).setLong( parameterName, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setFloat(String parameterName, float x) throws SQLException
    {
        ((CallableStatement)statement).setFloat( parameterName, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setDouble(String parameterName, double x) throws SQLException
    {
        ((CallableStatement)statement).setDouble( parameterName, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException
    {
        ((CallableStatement)statement).setBigDecimal( parameterName, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setString(String parameterName, String x) throws SQLException
    {
        ((CallableStatement)statement).setString( parameterName, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setBytes(String parameterName, byte[] x) throws SQLException
    {
        ((CallableStatement)statement).setBytes( parameterName, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setDate(String parameterName, Date x) throws SQLException
    {
        ((CallableStatement)statement).setDate( parameterName, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setTime(String parameterName, Time x) throws SQLException
    {
        ((CallableStatement)statement).setTime( parameterName, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setTimestamp(String parameterName, Timestamp x) throws SQLException
    {
        ((CallableStatement)statement).setTimestamp( parameterName, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException
    {
        ((CallableStatement)statement).setAsciiStream( parameterName, x, length);
    }

    /** {@inheritDoc } */
    @Override
    public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException
    {
        ((CallableStatement)statement).setBinaryStream( parameterName, x, length);
    }

    /** {@inheritDoc } */
    @Override
    public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException
    {
        ((CallableStatement)statement).setObject( parameterName, x, scale);
    }

    /** {@inheritDoc } */
    @Override
    public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException
    {
        ((CallableStatement)statement).setObject( parameterName, x, targetSqlType);
    }

    /** {@inheritDoc } */
    @Override
    public void setObject(String parameterName, Object x) throws SQLException
    {
        ((CallableStatement)statement).setObject( parameterName, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException
    {
        ((CallableStatement)statement).setCharacterStream( parameterName, reader, length);
    }

    /** {@inheritDoc } */
    @Override
    public void setDate(String parameterName, Date x, Calendar cal) throws SQLException
    {
        ((CallableStatement)statement).setDate( parameterName, x, cal);
    }

    /** {@inheritDoc } */
    @Override
    public void setTime(String parameterName, Time x, Calendar cal) throws SQLException
    {
        ((CallableStatement)statement).setTime( parameterName, x, cal);
    }

    /** {@inheritDoc } */
    @Override
    public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException
    {
        ((CallableStatement)statement).setTimestamp( parameterName, x, cal);
    }

    /** {@inheritDoc } */
    @Override
    public void setNull(String parameterName, int sqlType, String typeName) throws SQLException
    {
        ((CallableStatement)statement).setNull( parameterName, sqlType, typeName);
    }

    /** {@inheritDoc } */
    @Override
    public String getString(String parameterName) throws SQLException
    {
        return ((CallableStatement)statement).getString( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public boolean getBoolean(String parameterName) throws SQLException
    {
        return ((CallableStatement)statement).getBoolean( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public byte getByte(String parameterName) throws SQLException
    {
        return ((CallableStatement)statement).getByte( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public short getShort(String parameterName) throws SQLException
    {
        return ((CallableStatement)statement).getShort( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public int getInt(String parameterName) throws SQLException
    {
        return ((CallableStatement)statement).getInt( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public long getLong(String parameterName) throws SQLException
    {
        return ((CallableStatement)statement).getLong( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public float getFloat(String parameterName) throws SQLException
    {
        return ((CallableStatement)statement).getFloat( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public double getDouble(String parameterName) throws SQLException
    {
        return ((CallableStatement)statement).getDouble( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public byte[] getBytes(String parameterName) throws SQLException
    {
        return ((CallableStatement)statement).getBytes( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public Date getDate(String parameterName) throws SQLException
    {
        return ((CallableStatement)statement).getDate( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public Time getTime(String parameterName) throws SQLException
    {
        return ((CallableStatement)statement).getTime( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public Timestamp getTimestamp(String parameterName) throws SQLException
    {
        return ((CallableStatement)statement).getTimestamp( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public Object getObject(String parameterName) throws SQLException
    {
        return ((CallableStatement)statement).getObject( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public BigDecimal getBigDecimal(String parameterName) throws SQLException
    {
        return ((CallableStatement)statement).getBigDecimal( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public Object getObject(String parameterName, Map<String, Class<?>> map) throws SQLException
    {
        return ((CallableStatement)statement).getObject( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public Ref getRef(String parameterName) throws SQLException
    {
        return ((CallableStatement)statement).getRef( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public Blob getBlob(String parameterName) throws SQLException
    {
        return ((CallableStatement)statement).getBlob( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public Clob getClob(String parameterName) throws SQLException
    {
        return ((CallableStatement)statement).getClob( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public Array getArray(String parameterName) throws SQLException
    {
        return ((CallableStatement)statement).getArray( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public Date getDate(String parameterName, Calendar cal) throws SQLException
    {
        return ((CallableStatement)statement).getDate( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public Time getTime(String parameterName, Calendar cal) throws SQLException
    {
        return ((CallableStatement)statement).getTime( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException
    {
        return ((CallableStatement)statement).getTimestamp( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public URL getURL(String parameterName) throws SQLException
    {
        return ((CallableStatement)statement).getURL( parameterName);
    }
    /** {@inheritDoc } */
    
    @Override
    public RowId getRowId(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)statement).getRowId( parameterIndex);
    }

    /** {@inheritDoc } */
    @Override
    public RowId getRowId(String parameterName) throws SQLException
    {
        return ((CallableStatement)statement).getRowId( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public void setRowId(String parameterName, RowId x) throws SQLException
    {
        ((CallableStatement)statement).setRowId( parameterName, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setNString(String parameterName, String value) throws SQLException
    {
        ((CallableStatement)statement).setNString( parameterName, value);
    }

    /** {@inheritDoc } */
    @Override
    public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException
    {
        ((CallableStatement)statement).setNCharacterStream( parameterName, value, length);
    }

    /** {@inheritDoc } */
    
    @Override
    public void setNClob(String parameterName, NClob value) throws SQLException
    {
        ((CallableStatement)statement).setNClob( parameterName, value);
    }

    /** {@inheritDoc } */
    @Override
    public void setClob(String parameterName, Reader reader, long length) throws SQLException
    {
        ((CallableStatement)statement).setClob( parameterName, reader, length);
    }

    /** {@inheritDoc } */
    @Override
    public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException
    {
        ((CallableStatement)statement).setBlob( parameterName, inputStream, length);
    }

    /** {@inheritDoc } */
    @Override
    public void setNClob(String parameterName, Reader reader, long length) throws SQLException
    {
        ((CallableStatement)statement).setNClob( parameterName, reader, length);
    }

    /** {@inheritDoc } */
    @Override
    public NClob getNClob(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)statement).getNClob( parameterIndex);
    }

    /** {@inheritDoc } */
    @Override
    public NClob getNClob(String parameterName) throws SQLException
    {
        return ((CallableStatement)statement).getNClob( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException
    {
        ((CallableStatement)statement).setSQLXML( parameterName, xmlObject);
    }

    /** {@inheritDoc } */
    @Override
    public SQLXML getSQLXML(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)statement).getSQLXML( parameterIndex);
    }

    /** {@inheritDoc } */
    @Override
    public SQLXML getSQLXML(String parameterName) throws SQLException
    {
        return ((CallableStatement)statement).getSQLXML( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public String getNString(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)statement).getNString( parameterIndex);
    }

    /** {@inheritDoc } */
    @Override
    public String getNString(String parameterName) throws SQLException
    {
        return ((CallableStatement)statement).getNString( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public Reader getNCharacterStream(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)statement).getNCharacterStream( parameterIndex);
    }

    /** {@inheritDoc } */
    @Override
    public Reader getNCharacterStream(String parameterName) throws SQLException
    {
        return ((CallableStatement)statement).getNCharacterStream( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public Reader getCharacterStream(int parameterIndex) throws SQLException
    {
        return ((CallableStatement)statement).getCharacterStream( parameterIndex);
    }

    /** {@inheritDoc } */
    @Override
    public Reader getCharacterStream(String parameterName) throws SQLException
    {
        return ((CallableStatement)statement).getCharacterStream( parameterName);
    }

    /** {@inheritDoc } */
    @Override
    public void setBlob(String parameterName, Blob x) throws SQLException
    {
        ((CallableStatement)statement).setBlob( parameterName, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setClob(String parameterName, Clob x) throws SQLException
    {
        ((CallableStatement)statement).setClob( parameterName, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException
    {
        ((CallableStatement)statement).setAsciiStream( parameterName, x, length);
    }

    /** {@inheritDoc } */
    @Override
    public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException
    {
        ((CallableStatement)statement).setBinaryStream( parameterName, x, length);
    }

    /** {@inheritDoc } */
    @Override
    public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException
    {
        ((CallableStatement)statement).setCharacterStream( parameterName, reader, length);
    }

    /** {@inheritDoc } */
    @Override
    public void setAsciiStream(String parameterName, InputStream x) throws SQLException
    {
        ((CallableStatement)statement).setAsciiStream( parameterName, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setBinaryStream(String parameterName, InputStream x) throws SQLException
    {
        ((CallableStatement)statement).setBinaryStream( parameterName, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setCharacterStream(String parameterName, Reader reader) throws SQLException
    {
        ((CallableStatement)statement).setCharacterStream( parameterName, reader);
    }

    /** {@inheritDoc }
     * @param x */
    @Override
    public void setNCharacterStream(String parameterName, Reader x) throws SQLException
    {
        ((CallableStatement)statement).setNCharacterStream( parameterName, x);
    }

    /** {@inheritDoc }
     * @param x */
    @Override
    public void setClob(String parameterName, Reader x) throws SQLException
    {
        ((CallableStatement)statement).setClob( parameterName, x);
    }

    /** {@inheritDoc }
     * @param x */
    @Override
    public void setBlob(String parameterName, InputStream x) throws SQLException
    {
        ((CallableStatement)statement).setBlob( parameterName, x);
    }

    /** {@inheritDoc }
     * @param x */
    @Override
    public void setNClob(String parameterName, Reader x) throws SQLException
    {
        ((CallableStatement)statement).setNClob( parameterName, x);
    }

    /** {@inheritDoc } */
    @Override
    public ResultSet executeQuery() throws SQLException
    {
        return ((CallableStatement)statement).executeQuery();
    }

    /** {@inheritDoc } */
    @Override
    public int executeUpdate() throws SQLException
    {
        return ((CallableStatement)statement).executeUpdate();
    }

    /** {@inheritDoc } */
    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException
    {
        ((CallableStatement)statement).setNull( parameterIndex, sqlType);
    }

    /** {@inheritDoc } */
    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException
    {
        ((CallableStatement)statement).setBoolean( parameterIndex, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException
    {
        ((CallableStatement)statement).setByte( parameterIndex, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setShort(int parameterIndex, short x) throws SQLException
    {
        ((CallableStatement)statement).setShort( parameterIndex, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setInt(int parameterIndex, int x) throws SQLException
    {
        ((CallableStatement)statement).setInt( parameterIndex, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setLong(int parameterIndex, long x) throws SQLException
    {
        ((CallableStatement)statement).setLong( parameterIndex, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException
    {
        ((CallableStatement)statement).setFloat( parameterIndex, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException
    {
        ((CallableStatement)statement).setDouble( parameterIndex, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException
    {
        ((CallableStatement)statement).setBigDecimal( parameterIndex, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setString(int parameterIndex, String x) throws SQLException
    {
        ((CallableStatement)statement).setString( parameterIndex, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException
    {
        ((CallableStatement)statement).setBytes( parameterIndex, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException
    {
        ((CallableStatement)statement).setDate( parameterIndex, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException
    {
        ((CallableStatement)statement).setTime( parameterIndex, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException
    {
        ((CallableStatement)statement).setTimestamp( parameterIndex, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException
    {
        ((CallableStatement)statement).setAsciiStream( parameterIndex, x, length);
    }

    /** {@inheritDoc } */
    @Override
    @SuppressWarnings("deprecation")
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException
    {
        ((CallableStatement)statement).setUnicodeStream( parameterIndex, x, length);
    }

    /** {@inheritDoc } */
    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException
    {
        ((CallableStatement)statement).setBinaryStream( parameterIndex, x, length);
    }

    /** {@inheritDoc } */
    @Override
    public void clearParameters() throws SQLException
    {
        ((CallableStatement)statement).clearParameters();
    }

    /** {@inheritDoc } */
    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException
    {
        ((CallableStatement)statement).setObject( parameterIndex, x, targetSqlType);
    }

    /** {@inheritDoc } */
    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException
    {
        ((CallableStatement)statement).setObject( parameterIndex, x);
    }

    /** {@inheritDoc } */
    @Override
    public boolean execute() throws SQLException
    {
        return ((CallableStatement)statement).execute();
    }

    /** {@inheritDoc } */
    @Override
    public void addBatch() throws SQLException
    {
        ((CallableStatement)statement).addBatch();
    }

    /** {@inheritDoc }
     * @param x */
    @Override
    public void setCharacterStream(int parameterIndex, Reader x, int length) throws SQLException
    {
        ((CallableStatement)statement).setCharacterStream( parameterIndex, x, length);
    }

    /** {@inheritDoc } */
    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException
    {
        ((CallableStatement)statement).setRef( parameterIndex, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException
    {
        ((CallableStatement)statement).setBlob( parameterIndex, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException
    {
        ((CallableStatement)statement).setClob( parameterIndex, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException
    {
        ((CallableStatement)statement).setArray( parameterIndex, x);
    }

    /** {@inheritDoc } */
    @Override
    public ResultSetMetaData getMetaData() throws SQLException
    {
        return ((CallableStatement)statement).getMetaData( );
    }

    /** {@inheritDoc } */
    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException
    {
        ((CallableStatement)statement).setDate( parameterIndex, x, cal);
    }

    /** {@inheritDoc } */
    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException
    {
        ((CallableStatement)statement).setTime( parameterIndex, x, cal);
    }

    /** {@inheritDoc } */
    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException
    {
        ((CallableStatement)statement).setTimestamp( parameterIndex, x, cal);
    }

    /** {@inheritDoc } */
    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException
    {
        ((CallableStatement)statement).setNull( parameterIndex, sqlType, typeName);
    }

    /** {@inheritDoc } */
    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException
    {
        ((CallableStatement)statement).setURL( parameterIndex, x);
    }

    /** {@inheritDoc } */
    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException
    {
        return ((CallableStatement)statement).getParameterMetaData( );
    }

    /** {@inheritDoc } */    
    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException
    {
        ((CallableStatement)statement).setRowId( parameterIndex, x);
    }

    /** {@inheritDoc }
     * @param x */
    @Override
    public void setNString(int parameterIndex, String x) throws SQLException
    {
        ((CallableStatement)statement).setNString( parameterIndex, x);
    }

    /** {@inheritDoc }
     * @param x */
    @Override
    public void setNCharacterStream(int parameterIndex, Reader x, long length) throws SQLException
    {
        ((CallableStatement)statement).setNCharacterStream( parameterIndex, x, length);
    }

    /** {@inheritDoc }
     * @param x */
    @Override
    public void setNClob(int parameterIndex, NClob x) throws SQLException
    {
        ((CallableStatement)statement).setNClob( parameterIndex, x);
    }

    /** {@inheritDoc }
     * @param x */
    @Override
    public void setClob(int parameterIndex, Reader x, long length) throws SQLException
    {
        ((CallableStatement)statement).setClob( parameterIndex, x, length);
    }

    /** {@inheritDoc }
     * @param x */
    @Override
    public void setBlob(int parameterIndex, InputStream x, long length) throws SQLException
    {
        ((CallableStatement)statement).setBlob( parameterIndex, x, length);
    }

    /** {@inheritDoc }
     * @param x */
    @Override
    public void setNClob(int parameterIndex, Reader x, long length) throws SQLException
    {
        ((CallableStatement)statement).setNClob( parameterIndex, x, length);
    }

    /** {@inheritDoc }
     * @param x */
    @Override
    public void setSQLXML(int parameterIndex, SQLXML x) throws SQLException
    {
        ((CallableStatement)statement).setSQLXML( parameterIndex, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException
    {
        ((CallableStatement)statement).setObject( parameterIndex, x,targetSqlType,scaleOrLength);
    }

    /** {@inheritDoc } */
    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException
    {
        ((CallableStatement)statement).setAsciiStream( parameterIndex, x,length);
    }

    /** {@inheritDoc } */
    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException
    {
        ((CallableStatement)statement).setBinaryStream( parameterIndex, x,length);
    }

    /** {@inheritDoc }
     * @param x */
    @Override
    public void setCharacterStream(int parameterIndex, Reader x, long length) throws SQLException
    {
        ((CallableStatement)statement).setCharacterStream( parameterIndex, x,length);
    }

    /** {@inheritDoc } */
    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException
    {
        ((CallableStatement)statement).setAsciiStream( parameterIndex, x);
    }

    /** {@inheritDoc } */
    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException
    {
        ((CallableStatement)statement).setBinaryStream( parameterIndex, x);
    }

    /** {@inheritDoc }
     * @param x */
    @Override
    public void setCharacterStream(int parameterIndex, Reader x) throws SQLException
    {
        ((CallableStatement)statement).setCharacterStream( parameterIndex, x);
    }

    /** {@inheritDoc }
     * @param x */
    @Override
    public void setNCharacterStream(int parameterIndex, Reader x) throws SQLException
    {
        ((CallableStatement)statement).setNCharacterStream( parameterIndex, x);
    }

    /** {@inheritDoc }
     * @param x */
    @Override
    public void setClob(int parameterIndex, Reader x) throws SQLException
    {
        ((CallableStatement)statement).setClob( parameterIndex, x);
    }

    /** {@inheritDoc }
     * @param x */
    @Override
    public void setBlob(int parameterIndex, InputStream x) throws SQLException
    {
        ((CallableStatement)statement).setBlob( parameterIndex, x);
    }

    /** {@inheritDoc }
     * @param x */
    @Override
    public void setNClob(int parameterIndex, Reader x) throws SQLException
    {
        ((CallableStatement)statement).setNClob( parameterIndex, x);
    }

    /** {@inheritDoc } */
    @Override
    public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException
    {
        return null;
        //return ((CallableStatement)statement).getObject( parameterIndex, type);
    }

    /** {@inheritDoc } */
    @Override
    public <T> T getObject(String parameterName, Class<T> type) throws SQLException
    {
        return null;//((CallableStatement)statement).getObject( parameterName, type);
    }

}
