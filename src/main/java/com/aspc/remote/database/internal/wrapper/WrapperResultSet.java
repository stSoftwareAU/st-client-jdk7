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
package com.aspc.remote.database.internal.wrapper;

import com.aspc.developer.ThreadCop;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

/**
 * Remote Server database connection.
 * Implements a database connection through SOAP.
 *
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 * @author  Nigel Leck
 * @since 29 September 2006
 */
public class WrapperResultSet implements ResultSet
{

    private final ResultSet rs;

//    private SQLException resultInvalidExcepton;
    private boolean nextCalledReturnedFalse;
    private boolean closedCalled;

    /**
     * The wrapper result set.
     * @param rs the result set
     */
    public WrapperResultSet(final ResultSet rs)
    {
        this.rs = rs;
    }

    private void checkValid()
    {
        if( closedCalled)
        {
            throw new WrapperAssertError("result set already not valid");
        }

        assert ThreadCop.access(this);
    }
    /**
     * {@inheritDoc}
     * <B>SOME VENDORS THROW AN ERROR IF CALLED TWICE</B>
     */
    @Override
    public boolean next() throws SQLException
    {
        checkValid();

        if( nextCalledReturnedFalse)
        {
            throw new WrapperAssertError( "next called again when already past the last record");
        }
        if( rs.next())
        {
            return true;
        }
        else
        {
            nextCalledReturnedFalse=true;
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws SQLException
    {
        if( closedCalled) return;

        checkValid();

//        resultInvalidExcepton=new SQLException( "result set closed");

        rs.close();

        closedCalled=true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean wasNull() throws SQLException
    {
        checkValid();
        return rs.wasNull();
    }

    /** {@inheritDoc} */
    @Override
    public String getString(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getString(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public boolean getBoolean(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getBoolean(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public byte getByte(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getByte(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public short getShort(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getShort(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public int getInt(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getInt(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public long getLong(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getLong(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public float getFloat(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getFloat(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public double getDouble(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getDouble(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException
    {
        checkValid();
        return rs.getBigDecimal(columnIndex, scale);
    }

    /** {@inheritDoc} */
    @Override
    public byte[] getBytes(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getBytes(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public Date getDate(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getDate(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public Time getTime(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getTime(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getTimestamp(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getAsciiStream(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getUnicodeStream(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getBinaryStream(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public String getString(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getString(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public boolean getBoolean(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getBoolean(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public byte getByte(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getByte(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public short getShort(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getShort(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public int getInt(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getInt(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public long getLong(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getLong(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public float getFloat(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getFloat(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public double getDouble(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getDouble(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException
    {
        checkValid();
        return rs.getBigDecimal(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public byte[] getBytes(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getBytes(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public Date getDate(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getDate(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public Time getTime(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getTime(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getTimestamp(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getAsciiStream(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public InputStream getUnicodeStream(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getUnicodeStream(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getBinaryStream(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public SQLWarning getWarnings() throws SQLException
    {
        checkValid();
        return rs.getWarnings();
    }

    /** {@inheritDoc} */
    @Override
    public void clearWarnings() throws SQLException
    {
        checkValid();
        rs.clearWarnings();
    }

    /** {@inheritDoc} */
    @Override
    public String getCursorName() throws SQLException
    {
        checkValid();
        return rs.getCursorName();
    }

    /** {@inheritDoc} */
    @Override
    public ResultSetMetaData getMetaData() throws SQLException
    {
        checkValid();
        return rs.getMetaData();
    }

    /** {@inheritDoc} */
    @Override
    public Object getObject(final int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getObject(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public Object getObject(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getObject(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public int findColumn(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.findColumn(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getCharacterStream(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getCharacterStream(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getBigDecimal(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getBigDecimal(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isBeforeFirst() throws SQLException
    {
        checkValid();
        return rs.isBeforeFirst();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAfterLast() throws SQLException
    {
        checkValid();
        return rs.isAfterLast();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isFirst() throws SQLException
    {
        checkValid();
        return rs.isFirst();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isLast() throws SQLException
    {
        checkValid();
        return rs.isLast();
    }

    /** {@inheritDoc} */
    @Override
    public void beforeFirst() throws SQLException
    {
        checkValid();
        rs.beforeFirst();
    }

    /** {@inheritDoc} */
    @Override
    public void afterLast() throws SQLException
    {
        checkValid();
        rs.afterLast();
    }

    /** {@inheritDoc} */
    @Override
    public boolean first() throws SQLException
    {
        checkValid();
        return rs.first();
    }

    /** {@inheritDoc} */
    @Override
    public boolean last() throws SQLException
    {
        checkValid();
        return rs.last();
    }

    /** {@inheritDoc} */
    @Override
    public int getRow() throws SQLException
    {
        checkValid();
        return rs.getRow();
    }

    /** {@inheritDoc} */
    @Override
    public boolean absolute(int row) throws SQLException
    {
        checkValid();
        return rs.absolute(row);
    }

    /** {@inheritDoc} */
    @Override
    public boolean relative(int rows) throws SQLException
    {
        checkValid();
        return rs.relative(rows);
    }

    /** {@inheritDoc} */
    @Override
    public boolean previous() throws SQLException
    {
        checkValid();
        return rs.previous();
    }

    /** {@inheritDoc} */
    @Override
    public void setFetchDirection(int direction) throws SQLException
    {
        checkValid();
        rs.setFetchDirection(direction);
    }

    /** {@inheritDoc} */
    @Override
    public int getFetchDirection() throws SQLException
    {
        checkValid();
        return rs.getFetchDirection();
    }

    /** {@inheritDoc} */
    @Override
    public void setFetchSize(int rows) throws SQLException
    {
        checkValid();
        rs.setFetchSize(rows);
    }

    /** {@inheritDoc} */
    @Override
    public int getFetchSize() throws SQLException
    {
        checkValid();
        return rs.getFetchSize();
    }

    /** {@inheritDoc} */
    @Override
    public int getType() throws SQLException
    {
        checkValid();
        return rs.getType();
    }

    /** {@inheritDoc} */
    @Override
    public int getConcurrency() throws SQLException
    {
        checkValid();
        return rs.getConcurrency();
    }

    /** {@inheritDoc} */
    @Override
    public boolean rowUpdated() throws SQLException
    {
        checkValid();
        return rs.rowUpdated();
    }

    /** {@inheritDoc} */
    @Override
    public boolean rowInserted() throws SQLException
    {
        checkValid();
        return rs.rowInserted();
    }

    /** {@inheritDoc} */
    @Override
    public boolean rowDeleted() throws SQLException
    {
        checkValid();
        return rs.rowDeleted();
    }

    /** {@inheritDoc} */
    @Override
    public void updateNull(int columnIndex) throws SQLException
    {
        checkValid();
        rs.updateNull(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException
    {
        checkValid();
        rs.updateBoolean(columnIndex, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException
    {
        checkValid();
        rs.updateByte(columnIndex, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateShort(int columnIndex, short x) throws SQLException
    {
        checkValid();
        rs.updateShort(columnIndex, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateInt(int columnIndex, int x) throws SQLException
    {
        checkValid();
        rs.updateInt(columnIndex, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateLong(int columnIndex, long x) throws SQLException
    {
        checkValid();
        rs.updateLong(columnIndex, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException
    {
        checkValid();
        rs.updateFloat(columnIndex, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException
    {
        checkValid();
        rs.updateDouble(columnIndex, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException
    {
        checkValid();
        rs.updateBigDecimal(columnIndex, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateString(int columnIndex, String x) throws SQLException
    {
        checkValid();
        rs.updateString(columnIndex, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException
    {
        checkValid();
        rs.updateBytes(columnIndex, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException
    {
        checkValid();
        rs.updateDate(columnIndex, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException
    {
        checkValid();
        rs.updateTime(columnIndex, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException
    {
        checkValid();
        rs.updateTimestamp(columnIndex, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException
    {
        checkValid();
        rs.updateAsciiStream(columnIndex, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException
    {
        checkValid();
        rs.updateBinaryStream(columnIndex, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException
    {
        checkValid();
        rs.updateCharacterStream(columnIndex, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException
    {
        checkValid();
        rs.updateObject(columnIndex, x, scaleOrLength);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException
    {
        checkValid();
        rs.updateObject(columnIndex, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateNull(String columnLabel) throws SQLException
    {
        checkValid();
        rs.updateNull(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public void updateBoolean(String columnLabel, boolean x) throws SQLException
    {
        checkValid();
        rs.updateBoolean(columnLabel, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException
    {
        checkValid();
        rs.updateByte(columnLabel, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateShort(String columnLabel, short x) throws SQLException
    {
        checkValid();
        rs.updateShort(columnLabel, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateInt(String columnLabel, int x) throws SQLException
    {
        checkValid();
        rs.updateInt(columnLabel, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateLong(String columnLabel, long x) throws SQLException
    {
        checkValid();
        rs.updateLong(columnLabel, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException
    {
        checkValid();
        rs.updateFloat(columnLabel, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException
    {
        checkValid();
        rs.updateDouble(columnLabel, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException
    {
        checkValid();
        rs.updateBigDecimal(columnLabel, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateString(String columnLabel, String x) throws SQLException
    {
        checkValid();
        rs.updateString(columnLabel, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException
    {
        checkValid();
        rs.updateBytes(columnLabel, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateDate(String columnLabel, Date x) throws SQLException
    {
        checkValid();
        rs.updateDate(columnLabel, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateTime(String columnLabel, Time x) throws SQLException
    {
        checkValid();
        rs.updateTime(columnLabel, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException
    {
        checkValid();
        rs.updateTimestamp(columnLabel, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException
    {
        checkValid();
        rs.updateAsciiStream(columnLabel, x, length);
    }

    /** {@inheritDoc} */
    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException
    {
        checkValid();
        rs.updateBinaryStream(columnLabel, x, length);
    }

    /** {@inheritDoc} */
    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException
    {
        checkValid();
        rs.updateCharacterStream(columnLabel, reader, length);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException
    {
        checkValid();
        rs.updateObject(columnLabel, x, scaleOrLength);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException
    {
        checkValid();
        rs.updateObject(columnLabel, x);
    }

    /** {@inheritDoc} */
    @Override
    public void insertRow() throws SQLException
    {
        checkValid();
        rs.insertRow();
    }

    /** {@inheritDoc} */
    @Override
    public void updateRow() throws SQLException
    {
        checkValid();
        rs.updateRow();
    }

    /** {@inheritDoc} */
    @Override
    public void deleteRow() throws SQLException
    {
        checkValid();
        rs.deleteRow();
    }

    /** {@inheritDoc} */
    @Override
    public void refreshRow() throws SQLException
    {
        checkValid();
        rs.refreshRow();
    }

    /** {@inheritDoc} */
    @Override
    public void cancelRowUpdates() throws SQLException
    {
        checkValid();
        rs.cancelRowUpdates();
    }

    /** {@inheritDoc} */
    @Override
    public void moveToInsertRow() throws SQLException
    {
        checkValid();
        rs.moveToInsertRow();
    }

    /** {@inheritDoc} */
    @Override
    public void moveToCurrentRow() throws SQLException
    {
        checkValid();
        rs.moveToCurrentRow();
    }

    /** {@inheritDoc} */
    @Override
    public Statement getStatement() throws SQLException
    {
        checkValid();
        return rs.getStatement();
    }

    /** {@inheritDoc} */
    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException
    {
        checkValid();
        return rs.getObject(columnIndex, map);
    }

    /** {@inheritDoc} */
    @Override
    public Ref getRef(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getRef(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public Blob getBlob(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getBlob(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public Clob getClob(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getClob(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public Array getArray(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getArray(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException
    {
        checkValid();
        return rs.getObject(columnLabel, map);
    }

    /** {@inheritDoc} */
    @Override
    public Ref getRef(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getRef(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public Blob getBlob(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getBlob(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public Clob getClob(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getClob(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public Array getArray(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getArray(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException
    {
        checkValid();
        return rs.getDate(columnIndex, cal);
    }

    /** {@inheritDoc} */
    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException
    {
        checkValid();
        return rs.getDate(columnLabel, cal);
    }

    /** {@inheritDoc} */
    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException
    {
        checkValid();
        return rs.getTime(columnIndex, cal);
    }

    /** {@inheritDoc} */
    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException
    {
        checkValid();
        return rs.getTime(columnLabel, cal);
    }

    /** {@inheritDoc} */
    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException
    {
        checkValid();
        return rs.getTimestamp(columnIndex, cal);
    }

    /** {@inheritDoc} */
    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException
    {
        checkValid();
        return rs.getTimestamp(columnLabel, cal);
    }

    /** {@inheritDoc} */
    @Override
    public URL getURL(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getURL(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public URL getURL(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getURL(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException
    {
        checkValid();
        rs.updateRef(columnIndex, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateRef(String columnLabel, Ref x) throws SQLException
    {
        checkValid();
        rs.updateRef(columnLabel, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException
    {
        checkValid();
        rs.updateBlob(columnIndex, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateBlob(String columnLabel, Blob x) throws SQLException
    {
        checkValid();
        rs.updateBlob(columnLabel, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException
    {
        checkValid();
        rs.updateClob(columnIndex, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateClob(String columnLabel, Clob x) throws SQLException
    {
        checkValid();
        rs.updateClob(columnLabel, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException
    {
        checkValid();
        rs.updateArray(columnIndex, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateArray(String columnLabel, Array x) throws SQLException
    {
        checkValid();
        rs.updateArray(columnLabel, x);
    }

    /** {@inheritDoc} */
    @Override
    public RowId getRowId(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getRowId(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public RowId getRowId(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getRowId(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException
    {
        checkValid();
        rs.updateRowId(columnIndex, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException
    {
        checkValid();
        rs.updateRowId(columnLabel, x);
    }

    /** {@inheritDoc} */
    @Override
    public int getHoldability() throws SQLException
    {
        checkValid();
        return rs.getHoldability();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isClosed() throws SQLException
    {
        checkValid();
        return rs.isClosed();
    }

    /** {@inheritDoc} */
    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException
    {
        checkValid();
        rs.updateNString(columnIndex, nString);
    }

    /** {@inheritDoc} */
    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException
    {
        checkValid();
        rs.updateNString(columnLabel, nString);
    }

    /** {@inheritDoc} */
    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException
    {
        checkValid();
        rs.updateNClob(columnIndex, nClob);
    }

    /** {@inheritDoc} */
    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException
    {
        checkValid();
        rs.updateNClob(columnLabel, nClob);
    }

    /** {@inheritDoc} */
    @Override
    public NClob getNClob(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getNClob(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public NClob getNClob(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getNClob(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getSQLXML(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getSQLXML(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException
    {
        checkValid();
        rs.updateSQLXML(columnIndex, xmlObject);
    }

    /** {@inheritDoc} */
    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException
    {
        checkValid();
        rs.updateSQLXML(columnLabel, xmlObject);
    }

    /** {@inheritDoc} */
    @Override
    public String getNString(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getNString(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public String getNString(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getNString(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException
    {
        checkValid();
        return rs.getNCharacterStream(columnIndex);
    }

    /** {@inheritDoc} */
    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException
    {
        checkValid();
        return rs.getNCharacterStream(columnLabel);
    }

    /** {@inheritDoc} */
    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException
    {
        checkValid();
        rs.updateNCharacterStream(columnIndex, x, length);
    }

    /** {@inheritDoc} */
    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException
    {
        checkValid();
        rs.updateNCharacterStream(columnLabel, reader, length);
    }

    /** {@inheritDoc} */
    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException
    {
        checkValid();
        rs.updateAsciiStream(columnIndex, x, length);
    }

    /** {@inheritDoc} */
    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException
    {
        checkValid();
        rs.updateBinaryStream(columnIndex, x, length);
    }

    /** {@inheritDoc} */
    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException
    {
        checkValid();
        rs.updateCharacterStream(columnIndex, x, length);
    }

    /** {@inheritDoc} */
    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException
    {
        checkValid();
        rs.updateAsciiStream(columnLabel, x, length);
    }

    /** {@inheritDoc} */
    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException
    {
        checkValid();
        rs.updateBinaryStream(columnLabel, x, length);
    }

    /** {@inheritDoc} */
    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException
    {
        checkValid();
        rs.updateCharacterStream(columnLabel, reader, length);
    }

    /** {@inheritDoc} */
    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException
    {
        checkValid();
        rs.updateBlob(columnIndex, inputStream, length);
    }

    /** {@inheritDoc} */
    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException
    {
        checkValid();
        rs.updateBlob(columnLabel, inputStream, length);
    }

    /** {@inheritDoc} */
    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException
    {
        checkValid();
        rs.updateClob(columnIndex, reader, length);
    }

    /** {@inheritDoc} */
    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException
    {
        checkValid();
        rs.updateClob(columnLabel, reader, length);
    }

    /** {@inheritDoc} */
    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException
    {
        checkValid();
        rs.updateNClob(columnIndex, reader, length);
    }

    /** {@inheritDoc} */
    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException
    {
        checkValid();
        rs.updateNClob(columnLabel, reader, length);
    }

    /** {@inheritDoc} */
    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException
    {
        checkValid();
        rs.updateNCharacterStream(columnIndex, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException
    {
        checkValid();
        rs.updateNCharacterStream(columnLabel, reader);
    }

    /** {@inheritDoc} */
    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException
    {
        checkValid();
        rs.updateAsciiStream(columnIndex, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException
    {
        checkValid();
        rs.updateBinaryStream(columnIndex, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException
    {
        checkValid();
        rs.updateCharacterStream(columnIndex, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException
    {
        checkValid();
        rs.updateAsciiStream(columnLabel, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException
    {
        checkValid();
        rs.updateBinaryStream(columnLabel, x);
    }

    /** {@inheritDoc} */
    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException
    {
        checkValid();
        rs.updateCharacterStream(columnLabel, reader);
    }

    /** {@inheritDoc} */
    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException
    {
        checkValid();
        rs.updateBlob(columnIndex, inputStream);
    }

    /** {@inheritDoc} */
    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException
    {
        checkValid();
        rs.updateBlob(columnLabel, inputStream);
    }

    /** {@inheritDoc} */
    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException
    {
        checkValid();
        rs.updateClob(columnIndex, reader);
    }

    /** {@inheritDoc} */
    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException
    {
        checkValid();
        rs.updateClob(columnLabel, reader);
    }

    /** {@inheritDoc} */
    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException
    {
        checkValid();
        rs.updateNClob(columnIndex, reader);
    }

    /** {@inheritDoc} */
    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException
    {
        checkValid();
        rs.updateNClob(columnLabel, reader);
    }

    /** {@inheritDoc} */
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        checkValid();
        return rs.unwrap(iface);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        checkValid();
        return rs.isWrapperFor(iface);
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
