/**
 *  STS Remote library
 *c
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
package com.aspc.remote.database.internal;

import com.aspc.remote.database.NoColumnException;
import com.aspc.remote.jdbc.SoapSQLException;
import com.aspc.remote.memory.HashMapFactory;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.StringUtilities;
import com.aspc.remote.util.misc.TimeUtil;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;

/**
 *  SResultSet
 *
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED sql</i>
 *
 *  @author      Nigel Leck
 *  @since       29 September 2006
 */
public class SResultSet implements ResultSet
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.database.internal.SResultSet");//#LOGGER-NOPMD
    private boolean loggedOutput;

    public static final ThreadLocal<Boolean> ALLOW_OVERFLOW=new ThreadLocal(){
        @Override
        protected Object initialValue() {
            return Boolean.FALSE;
        }
        
    };
    /**
     *
     */
    public SResultSet()
    {
    }

    /**
     *
     * @param data the data
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    public SResultSet(final String data) throws SQLException
    {
        this();

        decodeTableData(data);//NOPMD
    }

    /**
     *
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public List<Column> getColumns()
    {
        return Collections.unmodifiableList(columns);
    }

    /**
     *
     * @throws Exception a serious problem
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public String formatOutput() throws Exception
    {
        int holdRow = currentRow;
        Row holdData = currentData;
        try
        {
            StringBuilder buffer = new StringBuilder();

            buffer.append(dividerLine());
            buffer.append("|");

            for (int i = 1; i <= getColumnCount(); i++)
            {
                int width;
                String text;
                text = getColumnName(i);
                width = getColumnDisplaySize(i);
                if (width < text.length())
                {
                    text = text.substring(0, width);
                }
                buffer.append(text);

                for (int j = text.length();
                        j < width;
                        j++)
                {
                    buffer.append(" ");
                }

                buffer.append("|");
            }
            buffer.append("\n");

            buffer.append(dividerLine());

            rewind();
            while (next())
            {
                String text;

                buffer.append("|");
                for (int i = 1; i <= getColumnCount(); i++)
                {
                    int width;

                    text = getString(i);

                    if (text == null)
                    {
                        text = "";
                    }
                    width = getColumnDisplaySize(i);
                    if (text.length() > width)
                    {
                        buffer.append(text.substring(0, width));
                    }
                    else
                    {
                        Object o;
                        o = getObject(i);
                        boolean left = true;

                        if (o instanceof Number)
                        {
                            left = false;
                        }

                        if (left)
                        {
                            buffer.append(text);
                        }
                        for (int j = text.length(); j < width; j++)
                        {
                            buffer.append(" ");
                        }
                        if (left == false)
                        {
                            buffer.append(text);
                        }
                    }
                    buffer.append("|");
                }
                buffer.append("\n");
            }
            buffer.append(dividerLine());
            return buffer.toString();
        }
        finally
        {
            currentRow = holdRow;
            currentData = holdData;
        }
    }

    /**
     * How many columns ?
     *
     * @return the number
     */
     @CheckReturnValue
    public int getColumnCount()
    {
        if (columns == null)
        {
            return 0;
        }

        return columns.size();
    }

    /**
     * Returns the name of the column at index.
     * @param columnIndex the column
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public String getColumnName(final int columnIndex)
    {
        String name;
        Column column;
        column = columns.get(columnIndex - 1);
        name = column.getName();

        return name;
    }
 
     /**
     * Returns the name of the column at index.
     * @param columnIndex the column
     * @return the value
     */
    @CheckReturnValue
    public int getColumnType(final int columnIndex)
    {
        Column column;
        column = columns.get(columnIndex - 1);
        int type = column.getType();
        return type;
    }
    
    /**
     *
     * @param col the column
     * @throws java.sql.SQLException if a database-access error occurs.
     * @return the value
     */
    @CheckReturnValue
    public int getColumnDisplaySize(final int col) throws SQLException
    {
        int size;
        Column column;
        column = columns.get(col - 1);
        size = column.getDisplaySize();

        return size;
    }

    /**
     * Rewinds the result list back to before the first row.
     */
    public void rewind()
    {
        currentRow = 0;
        isAfterLastFg = false;
        currentData = null;
    }

    @CheckReturnValue @Nonnull
    private String dividerLine() throws SQLException
    {
        StringBuilder result = new StringBuilder("+");

        for (int i = 1; i <= getColumnCount(); i++)
        {
            int width, j;

            width = getColumnDisplaySize(i);

            for (j = 0; j < width; j++)
            {
                result.append("-");
            }

            result.append("+");
        }
        result.append("\n");

        return result.toString();
    }

    /**
     * A ResultSet is initially positioned before its first row; the
     * first call to next makes the first row the current row; the
     * second call makes the second row the current row, etc.
     *
     * <P>If an input stream from the previous row is open, it is
     * implicitly closed. The ResultSet's warning chain is cleared
     * when a new row is read.
     *
     * @return true if the new current row is valid; false if there
     * are no more rows
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue
    public boolean next() throws SQLException
    {
        if( LOGGER.isDebugEnabled() && loggedOutput == false)
        {
            loggedOutput = true;
            try
            {
                LOGGER.debug( formatOutput());
            }
            catch( Exception e)
            {
                LOGGER.warn( "problem logging output", e);
            }
        }
        Row nextRow = fetchRow(currentRow + 1);

        if (nextRow == null)
        {
            isAfterLastFg = true;
            return false;
        }

        currentRow++;
        currentData = nextRow;

        return true;
    }

    /**
     *
     * @param row the row to use
     * @throws java.sql.SQLException if a database-access error occurs.
     * @return the value
     */
    @CheckReturnValue @Nullable
    protected Row fetchRow(int row) throws SQLException
    {
        if (rowPage == null || row > rowPage.size() || row < 1)
        {
            return null;
        }

        return (Row) rowPage.get(row - 1);
    }

    /**
     * Load all the data for a result set.
     *
     * @throws java.sql.SQLException a serious problem
     */
    @SuppressWarnings("empty-statement")
    public void load() throws SQLException
    {
        int row = getRow();

        while (next())
        {
            ;
        }

        setCurrentRow(row);
    }

    /**
     *
     * @param newRow the row
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    public void setCurrentRow(int newRow) throws SQLException
    {
        isAfterLastFg = false;
        if (newRow < 1)
        {
            currentRow = 0;
            currentData = null;
        }
        else
        {
            currentRow = newRow;
            currentData = fetchRow(newRow);
        }
    }

    /**
     * In some cases, it is desirable to immediately release a
     * ResultSet's database and JDBC resources instead of waiting for
     * this to happen when it is automatically closed; the close
     * method provides this immediate release.
     *
     * <P><B>Note:</B> A ResultSet is automatically closed by the
     * Statement that generated it when that Statement is closed,
     * re-executed, or is used to retrieve the next result from a
     * sequence of multiple results. A ResultSet is also automatically
     * closed when it is garbage collected.
     *
     * @exception SQLException if a database-access error occurs.
     */
    @Override
    public void close() throws SQLException
    {
    }

    /**
     * A column may have the value of SQL NULL; wasNull reports whether
     * the last column read had this special value.
     * Note that you must first call getXXX on a column to try to read
     * its value and then call wasNull() to find if the value was
     * the SQL NULL.
     *
     * @return true if last column read was SQL NULL
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue
    public boolean wasNull() throws SQLException
    {
        return isNull(lastCol + 1);
    }

    // Methods for accessing results by column index
    /**
     *
     * @throws java.lang.RuntimeException a problem
     */
    protected void validToGet() throws RuntimeException
    {
        if (currentData == null)
        {
            throw new RuntimeException("SResultSet - No Current row");
        }
    }

    /**
     * Get the value of a column in the current row as a Java String.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL NULL, the result is null
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue @Nullable
    public String getString( final int columnIndex) throws SQLException
    {
        validToGet();

        return currentData.getString(findColumnData(columnIndex));
    }

    /**
     * Get the value of a column in the current row as a Java boolean.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL NULL, the result is false
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue
    public boolean getBoolean(final int columnIndex) throws SQLException
    {
        validToGet();
        Object obj = getObject(columnIndex);

        if (obj == null)
        {
            return false;
        }

        if (obj instanceof Boolean)
        {
            Boolean b = (Boolean) obj;

            return b;
        }
        else if (obj instanceof String)
        {
            String b;
            b = obj.toString();

            if (b.equalsIgnoreCase("Y") ||
                    b.equalsIgnoreCase("YES") ||
                    b.equalsIgnoreCase("TRUE") ||
                    b.equalsIgnoreCase("1"))
            {
                return true;
            }
        }
        else if (obj instanceof Number)
        {
            Number b = (Number) obj;

            if (b.intValue() != 0)
            {
                return true;
            }
        }

        return false;
    }

    /** {@inheritDoc} */
    @Override
    public byte getByte( final int columnIndex) throws SQLException
    {
        //throw new SoapSQLException( "Not supported");
        validToGet();
        Object obj = getObject(columnIndex);
        if (StringUtilities.isBlank(obj))
        {
            return 0;
        }
        else if (obj instanceof Number)
        {
            return ((Number) obj).byteValue();
        }
        else if (obj instanceof String)
        {
            try
            {
                BigDecimal bDec = new BigDecimal((String) obj);
                return bDec.byteValue();
            }
            catch (NumberFormatException nfe)
            {
                throw new SoapSQLException("Format Error " + nfe.getMessage());
            }
        }
        else
        {
            throw new SoapSQLException("Format Error");
        }
    }

    /**
     * Get the value of a column in the current row as a Java short.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL NULL, the result is 0
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue
    public short getShort( final int columnIndex) throws SQLException
    {
        //throw new SoapSQLException( "Not supported");
        validToGet();
        Object obj = getObject(columnIndex);
        if (obj == null)
        {
            return 0;
        }
        else if (obj instanceof Number)
        {
            return ((Number) obj).shortValue();
        }
        else if (obj instanceof String)
        {
            try
            {
                BigDecimal bDec = new BigDecimal(((String) obj).trim());
                return bDec.shortValue();
            }
            catch (NumberFormatException nfe)
            {
                throw new SoapSQLException("Format Error " + nfe.getMessage());
            }
        }
        else
        {
            throw new SoapSQLException("Format Error");
        }
    }

    /**
     * Get the value of a column in the current row as a Java int.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL NULL, the result is 0
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue
    public int getInt( final int columnIndex) throws SQLException
    {
        validToGet();
        return currentData.getInt(findColumnData(columnIndex) );              
    }

    /**
     * Get the value of a column in the current row as a Java long.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL NULL, the result is 0
     * @exception SQLException if a database-access error occurs.
     */
    @Override
    public long getLong( final int columnIndex) throws SQLException
    {
        validToGet();

        return currentData.getLong(findColumnData(columnIndex));
    }

    /**
     * Get the value of a column in the current row as a Java float.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL NULL, the result is 0
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue
    public float getFloat( final int columnIndex) throws SQLException
    {
        validToGet();

        return currentData.getFloat(findColumnData(columnIndex));        
    }

    /**
     * Get the value of a column in the current row as a Java double.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL NULL, the result is 0
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue
    public double getDouble( final int columnIndex) throws SQLException
    {
        validToGet();

        return currentData.getDouble(findColumnData(columnIndex));
    }

    /**
     * Get the value of a column in the current row as a java.lang.BigDecimal object.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param scale the number of digits to the right of the decimal
     * @return the column value; if the value is SQL NULL, the result is null
     * @exception SQLException if a database-access error occurs.
     * @deprecated
     */
    @Override @CheckReturnValue
    public BigDecimal getBigDecimal( final int columnIndex, int scale) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * Get the value of a column in the current row as a Java byte array.
     * The bytes represent the raw values returned by the driver.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL NULL, the result is null
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue @Nullable
    public byte[] getBytes( final int columnIndex) throws SQLException
    {
        //throw new SoapSQLException( "Not Supported");
        validToGet();
        String result = getString(columnIndex);
        if (result == null)
        {
            return null;
        }
        return result.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Get the value of a column in the current row as a java.sql.Date object.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL NULL, the result is null
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue @Nullable
    public java.sql.Date getDate( final int columnIndex) throws SQLException
    {
        validToGet();

        return currentData.getDate(findColumnData(columnIndex));
    }

    /**
     * Get the value of a column in the current row as a java.sql.Time object.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL NULL, the result is null
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue @Nullable
    public java.sql.Time getTime( final int columnIndex) throws SQLException
    {
        //throw new SoapSQLException( "Not Supported");
        validToGet();
        Object obj;
        obj = currentData.getObject(findColumnData(columnIndex));

        if (obj == null)
        {
            return null;
        }
        else if (obj instanceof String)
        {
            return Time.valueOf((String) obj);
        }
        else if (obj instanceof java.util.Date)
        {
            return new Time(((java.util.Date) obj).getTime());
        }
        else
        {
            throw new SQLException("Invalid type");
        }
    }

    /**
     * Get the value of a column in the current row as a java.sql.Timestamp object.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL NULL, the result is null
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue @Nullable
    public java.sql.Timestamp getTimestamp( final int columnIndex) throws SQLException
    {
        validToGet();
        Object obj;
        obj = currentData.getObject(findColumnData(columnIndex));

        if (StringUtilities.isBlank(obj))
        {
            return null;
        }
        else if (obj instanceof String)
        {
            return Timestamp.valueOf((String) obj);
        }
        else if (obj instanceof java.util.Date)
        {
            return new Timestamp(((java.util.Date) obj).getTime());
        }
        else
        {
            throw new SQLException("Invalid type");
        }
    }

    /**
     * A column value can be retrieved as a stream of ASCII characters
     * and then read in chunks from the stream.  This method is particularly
     * suitable for retrieving large LONGVARCHAR values.  The JDBC driver will
     * do any necessary conversion from the database format into ASCII.
     *
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a get method implicitly closes the stream. . Also, a
     * stream may return 0 for available() whether there is data
     * available or not.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return a Java input stream that delivers the database column value
     * as a stream of one byte ASCII characters.  If the value is SQL NULL
     * then the result is null.
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue
    public java.io.InputStream getAsciiStream( final int columnIndex) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * A column value can be retrieved as a stream of Unicode characters
     * and then read in chunks from the stream.  This method is particularly
     * suitable for retrieving large LONGVARCHAR values.  The JDBC driver will
     * do any necessary conversion from the database format into Unicode.
     *
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a get method implicitly closes the stream. . Also, a
     * stream may return 0 for available() whether there is data
     * available or not.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return a Java input stream that delivers the database column value
     * as a stream of two byte Unicode characters.  If the value is SQL NULL
     * then the result is null.
     * @exception SQLException if a database-access error occurs.
     * @deprecated
     */
    @Override @CheckReturnValue @Nullable
    public java.io.InputStream getUnicodeStream( final int columnIndex) throws SQLException
    {
        throw new SoapSQLException("not supported");
    }

    /**
     * A column value can be retrieved as a stream of uninterpreted bytes
     * and then read in chunks from the stream.  This method is particularly
     * suitable for retrieving large LONGVARBINARY values.
     *
     * <P><B>Note:</B> All the data in the returned stream must be
     * read prior to getting the value of any other column. The next
     * call to a get method implicitly closes the stream. Also, a
     * stream may return 0 for available() whether there is data
     * available or not.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return a Java input stream that delivers the database column value
     * as a stream of uninterpreted bytes.  If the value is SQL NULL
     * then the result is null.
     * @exception SQLException if a database-access error occurs.
     */
    @Override  @CheckReturnValue @Nonnull
    public java.io.InputStream getBinaryStream( final int columnIndex) throws SQLException
    {
        throw new SoapSQLException("not supported");
    }

    @CheckReturnValue
    protected int sqlColumnNr( final @Nonnull String name) throws NoColumnException
    {
        Column column;

        column = findColumnData(name);

        return column.getNumber() + 1;
    }

    /**

    // Methods for accessing results by column name
    /**
     * Get the value of a column in the current row as a Java String.
     *
     * @param columnName is the SQL name of the column
     * @return the column value; if the value is SQL NULL, the result is null
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue @Nullable
    public String getString( final @Nonnull String columnName) throws SQLException
    {
        return getString( sqlColumnNr(columnName) );
    }

    /**
     * Get the value of a column in the current row as a Java boolean.
     *
     * @param columnName is the SQL name of the column
     * @return the column value; if the value is SQL NULL, the result is false
     * @exception SQLException if a database-access error occurs.
     */
    @Override  @CheckReturnValue
    public boolean getBoolean(final @Nonnull String columnName) throws SQLException
    {
        return getBoolean(sqlColumnNr(columnName));
    }

    /**
     * Get the value of a column in the current row as a Java byte.
     *
     * @param columnName is the SQL name of the column
     * @return the column value; if the value is SQL NULL, the result is 0
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue
    public byte getByte( final @Nonnull String columnName) throws SQLException
    {
        //throw new SoapSQLException( "Not Supported");
        validToGet();
        Object obj = getObject(columnName);
        if (StringUtilities.isBlank(obj))
        {
            return 0;
        }
        else if (obj instanceof Number)
        {
            return ((Number) obj).byteValue();
        }
        else if (obj instanceof String)
        {
            try
            {
                BigDecimal bDec = new BigDecimal(((String) obj).trim());
                return bDec.byteValue();
            }
            catch (NumberFormatException nfe)
            {
                throw new SoapSQLException("Format Error " + nfe.getMessage());
            }
        }
        else
        {
            throw new SoapSQLException("Format Error");
        }
    }

    /**
     * Get the value of a column in the current row as a Java short.
     *
     * @param columnName is the SQL name of the column
     * @return the column value; if the value is SQL NULL, the result is 0
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue
    public short getShort( final @Nonnull String columnName) throws SQLException
    {
        //throw new SoapSQLException( "Not Supported");
        validToGet();
        Object obj = getObject(columnName);
        if (obj == null)
        {
            return 0;
        }
        else if (obj instanceof Number)
        {
            return ((Number) obj).shortValue();
        }
        else if (obj instanceof String)
        {
            try
            {
                BigDecimal bDec = new BigDecimal(((String) obj).trim());
                return bDec.shortValue();
            }
            catch (NumberFormatException nfe)
            {
                throw new SoapSQLException("Format Error " + nfe.getMessage());
            }
        }
        else
        {
            throw new SoapSQLException("Format Error");
        }
    }

    /**
     *
     * @param col the column
     * @throws java.sql.SQLException if a database-access error occurs.
     * @return the value
     */
    @Override @CheckReturnValue @Nullable
    public Object getObject(final int col) throws SQLException
    {
        validToGet();

        return currentData.getObject(findColumnData(col));
    }

    /**
     *
     * @param columnName
     * @return the value
     */
    @Override @CheckReturnValue @Nullable
    public Object getObject( final @Nonnull String columnName)
    {
        validToGet();

        try{
            return currentData.getObject(findColumnData(columnName));
        }
        catch( NoColumnException nce)
        {
            throw new RuntimeException(columnName,nce);
        }
    }

    /**
     * Get the value of a column in the current row as a Java int.
     *
     * @param columnName is the SQL name of the column
     * @return the column value; if the value is SQL NULL, the result is 0
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue
    public int getInt(final @Nonnull String columnName) throws SQLException
    {
        validToGet();
        return currentData.getInt(findColumnData(columnName) );        
    }

    /**
     * Get  e trd the value of a column in the current row as a Java long.
     *
     * @param columnName is the SQL name of the column
     * @return the column value; if the value is SQL NULL, the result is 0
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue
    public long getLong( final @Nonnull String columnName) throws SQLException
    {
        validToGet();

        return currentData.getLong(findColumnData(columnName) );
    }

    /**
     * Get the value of a column in the current row as a Java float.
     *
     * @param columnName is the SQL name of the column
     * @return the column value; if the value is SQL NULL, the result is 0
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue
    public float getFloat( final @Nonnull String columnName) throws SQLException
    {
        return getFloat(findColumnData(columnName).getNumber());        
    }

    /**
     * Get the value of a column in the current row as a Java double.
     *
     * @param columnName is the SQL name of the column
     * @return the column value; if the value is SQL NULL, the result is 0
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue
    public double getDouble( final @Nonnull String columnName) throws SQLException
    {
        validToGet();

        return currentData.getDouble(findColumnData(columnName));
    }

    /**
     * Get the value of a column in the current row as a java.lang.BigDecimal object.
     *
     * @param columnName is the SQL name of the column
     * @param scale the number of digits to the right of the decimal
     * @return the column value; if the value is SQL NULL, the result is null
     * @exception SQLException if a database-access error occurs.
     * @deprecated
     */
    @Override @CheckReturnValue
    public BigDecimal getBigDecimal( final @Nonnull String columnName, int scale) throws SQLException
    {
        throw new SoapSQLException("Not supported");
    }

    /**
     * Get the value of a column in the current row as a Java byte array.
     * The bytes represent the raw values returned by the driver.
     *
     * @param columnName is the SQL name of the column
     * @return the column value; if the value is SQL NULL, the result is null
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue @Nullable
    public byte[] getBytes( final @Nonnull String columnName) throws SQLException
    {
        validToGet();
        //throw new SoapSQLException( "Not Support");
        String result = currentData.getString(findColumnData(columnName));
        if (result == null)
        {
            return null;
        }
        return result.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Get the value of a column in the current row as a java.sql.Date object.
     *
     * @param columnName is the SQL name of the column
     * @return the column value; if the value is SQL NULL, the result is null
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue @Nullable
    public java.sql.Date getDate( final @Nonnull String columnName) throws SQLException
    {
        validToGet();

        return currentData.getDate(findColumnData(columnName));
    }

    /**
     * Get the value of a column in the current row as a java.sql.Time object.
     *
     * @param columnName is the SQL name of the column
     * @return the column value; if the value is SQL NULL, the result is null
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue @Nullable
    public java.sql.Time getTime( final @Nonnull String columnName) throws SQLException
    {
        //throw new SoapSQLException( "Not Support");
        validToGet();
        Object obj;
        obj = currentData.getObject(findColumnData(columnName));

        if (obj == null)
        {
            return null;
        }
        else if (obj instanceof String)
        {
            return Time.valueOf((String) obj);
        }
        else if (obj instanceof java.util.Date)
        {
            return new Time(((java.util.Date) obj).getTime());
        }
        else
        {
            throw new SQLException("Invalid type");
        }
    }

    /**
     * Get the value of a column in the current row as a java.sql.Timestamp object.
     *
     * @param columnName is the SQL name of the column
     * @return the column value; if the value is SQL NULL, the result is null
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue @Nullable
    public java.sql.Timestamp getTimestamp( final @Nonnull String columnName) throws SQLException
    {
        validToGet();
        Object obj;
        obj = currentData.getObject(findColumnData(columnName));

        if (StringUtilities.isBlank(obj))
        {
            return null;
        }
        else if (obj instanceof String)
        {
            return Timestamp.valueOf((String) obj);
        }
        else if (obj instanceof java.util.Date)
        {
            return new Timestamp(((java.util.Date) obj).getTime());
        }
        else
        {
            throw new SQLException("Invalid type");
        }
    }

    /**
     * A column value can be retrieved as a stream of ASCII characters
     * and then read in chunks from the stream.  This method is particularly
     * suitable for retrieving large LONGVARCHAR values.  The JDBC driver will
     * do any necessary conversion from the database format into ASCII.
     *
     * <P><B>Note:</B> All the data in the returned stream must
     * be read prior to getting the value of any other column. The
     * next call to a get method implicitly closes the stream.
     *
     * @param columnName is the SQL name of the column
     * @return a Java input stream that delivers the database column value
     * as a stream of one byte ASCII characters.  If the value is SQL NULL
     * then the result is null.
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue
    public java.io.InputStream getAsciiStream( final @Nonnull String columnName) throws SQLException
    {
        throw new SoapSQLException("Not Support");
    }

    /**
     * A column value can be retrieved as a stream of Unicode characters
     * and then read in chunks from the stream.  This method is particularly
     * suitable for retrieving large LONGVARCHAR values.  The JDBC driver will
     * do any necessary conversion from the database format into Unicode.
     *
     * <P><B>Note:</B> All the data in the returned stream must
     * be read prior to getting the value of any other column. The
     * next call to a get method implicitly closes the stream.
     *
     * @param columnName is the SQL name of the column
     * @return a Java input stream that delivers the database column value
     * as a stream of two byte Unicode characters.  If the value is SQL NULL
     * then the result is null.
     * @exception SQLException if a database-access error occurs.
     * @deprecated
     */
    @Override @CheckReturnValue @Nullable
    public java.io.InputStream getUnicodeStream( final @Nonnull String columnName) throws SQLException
    {
        throw new SoapSQLException("Not Support");
    }

    /**
     * A column value can be retrieved as a stream of uninterpreted bytes
     * and then read in chunks from the stream.  This method is particularly
     * suitable for retrieving large LONGVARBINARY values.
     *
     * <P><B>Note:</B> All the data in the returned stream must
     * be read prior to getting the value of any other column. The
     * next call to a get method implicitly closes the stream.
     *
     * @param columnName is the SQL name of the column
     * @return a Java input stream that delivers the database column value
     * as a stream of uninterpreted bytes.  If the value is SQL NULL
     * then the result is null.
     * @exception SQLException if a database-access error occurs.
     */
    @Override  @CheckReturnValue @Nonnull
    public java.io.InputStream getBinaryStream( final @Nonnull String columnName) throws SQLException
    {
        throw new SoapSQLException("Not Support");
    }


    // Advanced features:
    /**
     * <p>The first warning reported by calls on this ResultSet is
     * returned. Subsequent ResultSet warnings will be chained to this
     * SQLWarning.
     *
     * <P>The warning chain is automatically cleared each time a new
     * row is read.
     *
     * <P><B>Note:</B> This warning chain only covers warnings caused
     * by ResultSet methods.  Any warning caused by statement methods
     * (such as reading OUT parameters) will be chained on the
     * Statement object.
     *
     * @return the first SQLWarning or null
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue @Nullable
    public SQLWarning getWarnings() throws SQLException
    {
        return null;
    }

    /**
     * After this call getWarnings returns null until a new warning is
     * reported for this ResultSet.
     *
     * @exception SQLException if a database-access error occurs.
     */
    @Override
    public void clearWarnings() throws SQLException
    {
    }

    /**
     * Get the name of the SQL cursor used by this ResultSet.
     *
     * <P>In SQL, a result table is retrieved through a cursor that is
     * named. The current row of a result can be updated or deleted
     * using a positioned update/delete statement that references the
     * cursor name.
     *
     * <P>JDBC supports this SQL feature by providing the name of the
     * SQL cursor used by a ResultSet. The current row of a ResultSet
     * is also the current row of this SQL cursor.
     *
     * <P><B>Note:</B> If positioned update is not supported a
     * SQLException is thrown
     *
     * @return the ResultSet's SQL cursor name
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue @Nullable
    public String getCursorName() throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * The number, types and properties of a ResultSet's columns
     * are provided by the getMetaData method.
     *
     * @return the description of a ResultSet's columns
     * @exception SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue @Nonnull
    public ResultSetMetaData getMetaData() throws SQLException
    {
        return new SResultSetMetaData(columns);
    }

    /**
     *
     * @param name
     * @return the value
     * @throws com.aspc.remote.database.NoColumnException
     */
    @CheckReturnValue
    public boolean isNull(final @Nonnull String name) throws NoColumnException
    {
        validToGet();

        Column column;

        column = findColumnData(name);

//        if (column == null)
//        {
//            throw (new RuntimeException("No Column '" + name + "' FOUND"));
//        }

        return currentData.isNull(column);
    }

    /**
     *
     * @param col the column
     * @throws java.sql.SQLException if a database-access error occurs.
     * @return the value
     */
    @CheckReturnValue
    public boolean isNull(final int col) throws SQLException
    {
        validToGet();

        return currentData.isNull(findColumnData(col));
    }

    /**
     *
     * @param oKey
     * @return the value
     * @throws NoColumnException no column
     */
    @CheckReturnValue
    protected final Column findColumnData(final @Nonnull String oKey) throws NoColumnException
    {
        Column column;

        // try it first without coverting the string as it appears to be the slowest part.
        column = (Column) columnKeys.get(oKey);

        if (column == null)
        {
            // retry but this time convert.
            String key = oKey.trim().toLowerCase();

            column = (Column) columnKeys.get(key);

            if (column == null)
            {
                throw new NoColumnException("No Column '" + key + "' FOUND");
            }

            columnKeys.put(oKey, column);
        }

        lastCol = column.getNumber();

        return column;
    }

    /**
     *
     * @param col the column
     * @throws java.sql.SQLException if a database-access error occurs.
     * @return the value
     */
    @CheckReturnValue
    protected final Column findColumnData(final int col) throws SQLException
    {
        Column column;

        column = columns.get(col - 1);

        if (column == null)
        {
            throw (new SoapSQLException("No Column #" + col + " FOUND"));
        }

        lastCol = column.getNumber();

        return column;
    }

    /**
     *
     * @param name
     * @throws java.sql.SQLException if a database-access error occurs.
     * @return the value
     */
    @Override @CheckReturnValue
    public int findColumn(String name) throws SQLException
    {
        Column column;

        column = findColumnData(name);

        return column.getNumber();
    }

    /**
     * Produces a tab separated data string.
     * Data is produced with or with out header info ( encoded not to include tabs in data)
     * @param data
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    public final synchronized void decodeTableData(final @Nonnull String data) throws SQLException
    {
        StringTokenizer line = new StringTokenizer(data, "\n");

        rowPage = new ArrayList();
        columns = new ArrayList();
        columnKeys = HashMapFactory.create();

        StringTokenizer cols = new StringTokenizer(line.nextToken(), "\t");

        for (int i = 0; cols.hasMoreTokens(); i++)
        {
            String name, type;

            StringTokenizer t = new StringTokenizer(cols.nextToken(), ",");

            name = t.nextToken();
            type = "";
            if (t.hasMoreTokens())
            {
                type = t.nextToken();
            }

            int tn;

            if (type.equalsIgnoreCase("Date"))
            {
                tn = Types.DATE;
            }
            else if (type.equalsIgnoreCase("Float"))
            {
                tn = Types.DOUBLE;
            }
            else if (type.equalsIgnoreCase("Integer"))
            {
                tn = Types.INTEGER;
            }
            else
            {
                tn = Types.CHAR;
            }

            Column col;

            col = new Column(this, name, tn, i);

            columns.add(col);

            columnKeys.put(name.toLowerCase(), col);
        }
        TimeZone tz = TimeZone.getDefault();

        while (line.hasMoreTokens())
        {
            rowPage.add(
                    new Row(tz, columns, line.nextToken()));
        }
    }

    /**
     * JDBC 2.0
     *
     * Give a nullable column a null value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateNull( final int columnIndex) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a boolean value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateBoolean( final int columnIndex, boolean x) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a byte value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateByte( final int columnIndex, byte x) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a short value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateShort( final int columnIndex, short x) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with an integer value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateInt( final int columnIndex, int x) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a long value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateLong( final int columnIndex, long x) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a float value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateFloat( final int columnIndex, float x) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a Double value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateDouble( final int columnIndex, double x) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a BigDecimal value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateBigDecimal( final int columnIndex, BigDecimal x) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a String value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateString( final int columnIndex, String x) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a byte array value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateBytes( final int columnIndex, byte x[]) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a Date value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateDate( final int columnIndex, java.sql.Date x) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a Time value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateTime( final int columnIndex, java.sql.Time x) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a Timestamp value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateTimestamp( final int columnIndex, java.sql.Timestamp x)
            throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with an ascii stream value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @param length the length of the stream
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateAsciiStream( final int columnIndex,
            java.io.InputStream x,
            int length) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a binary stream value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @param length the length of the stream
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateBinaryStream( final int columnIndex,
            java.io.InputStream x,
            int length) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a character stream value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @param length the length of the stream
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateCharacterStream( final int columnIndex,
            java.io.Reader x,
            int length) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with an Object value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @param scale For java.sql.Types.DECIMAL or java.sql.Types.NUMERIC types
     *  this is the number of digits after the decimal.  For all other
     *  types this value will be ignored.
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateObject( final int columnIndex, Object x, int scale)
            throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with an Object value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateObject( final int columnIndex, Object x) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a null value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnName the name of the column
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateNull( final @Nonnull String columnName) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a boolean value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateBoolean( final @Nonnull String columnName, boolean x) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a byte value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateByte( final @Nonnull String columnName, byte x) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a short value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateShort( final @Nonnull String columnName, short x) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with an integer value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateInt( final @Nonnull String columnName, int x) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a long value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateLong( final @Nonnull String columnName, long x) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a float value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateFloat( final @Nonnull String columnName, float x) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a double value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateDouble( final @Nonnull String columnName, double x) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a BigDecimal value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateBigDecimal( final @Nonnull String columnName, BigDecimal x) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a String value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateString( final @Nonnull String columnName, String x) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a byte array value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateBytes( final @Nonnull String columnName, byte x[]) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a Date value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateDate( final @Nonnull String columnName, java.sql.Date x) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a Time value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateTime( final @Nonnull String columnName, java.sql.Time x) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a Timestamp value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
    @exception SQLException if a database access error occurs
     */
    @Override
    public void updateTimestamp( final @Nonnull String columnName, java.sql.Timestamp x)
            throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with an ascii stream value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @param length of the stream
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateAsciiStream( final @Nonnull String columnName,
            java.io.InputStream x,
            int length) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a binary stream value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @param length of the stream
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateBinaryStream( final @Nonnull String columnName,
            java.io.InputStream x,
            int length) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with a character stream value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param reader
     * @param columnName the name of the column
     * @param length of the stream
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateCharacterStream( final @Nonnull String columnName,
            java.io.Reader reader,
            int length) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with an Object value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
     * @param scale For java.sql.Types.DECIMAL or java.sql.Types.NUMERIC types
     *  this is the number of digits after the decimal.  For all other
     *  types this value will be ignored.
     * @exception SQLException if a database access error occurs
     */
    @Override
    public void updateObject( final @Nonnull String columnName, Object x, int scale)
            throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates a column with an Object value.
     *
     * The <code>updateXXX</code> methods are used to update column values in the
     * current row, or the insert row.  The <code>updateXXX</code> methods do not
     * update the underlying database; instead the <code>updateRow</code> or <code>insertRow</code>
     * methods are called to update the database.
     *
     * @param columnName the name of the column
     * @param x the new column value
    @exception SQLException if a database access error occurs
     */
    @Override
    public void updateObject( final @Nonnull String columnName, Object x) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Inserts the contents of the insert row into the result set and
     * the database.  Must be on the insert row when this method is called.
     *
     * @exception SQLException if a database access error occurs,
     * if called when not on the insert row, or if not all of non-nullable columns in
     * the insert row have been given a value
     */
    @Override
    public void insertRow() throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Updates the underlying database with the new contents of the
     * current row.  Cannot be called when on the insert row.
     *
     * @exception SQLException if a database access error occurs or
     * if called when on the insert row
     */
    @Override
    public void updateRow() throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Deletes the current row from the result set and the underlying
     * database.  Cannot be called when on the insert row.
     *
     * @exception SQLException if a database access error occurs or if
     * called when on the insert row.
     */
    @Override
    public void deleteRow() throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Refreshes the current row with its most recent value in
     * the database.  Cannot be called when on the insert row.
     *
     * The <code>refreshRow</code> method provides a way for an application to
     * explicitly tell the JDBC driver to refetch a row(s) from the
     * database.  An application may want to call <code>refreshRow</code> when
     * caching or prefetching is being done by the JDBC driver to
     * fetch the latest value of a row from the database.  The JDBC driver
     * may actually refresh multiple rows at once if the fetch size is
     * greater than one.
     *
     * All values are refetched subject to the transaction isolation
     * level and cursor sensitivity.  If <code>refreshRow</code> is called after
     * calling <code>updateXXX</code>, but before calling <code>updateRow</code>, then the
     * updates made to the row are lost.  Calling the method <code>refreshRow</code> frequently
     * will likely slow performance.
     *
     * @exception SQLException if a database access error occurs or if
     * called when on the insert row
     */
    @Override
    public void refreshRow() throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Cancels the updates made to a row.
     * This method may be called after calling an
     * <code>updateXXX</code> method(s) and before calling <code>updateRow</code> to rollback
     * the updates made to a row.  If no updates have been made or
     * <code>updateRow</code> has already been called, then this method has no
     * effect.
     *
     * @exception SQLException if a database access error occurs or if
     * called when on the insert row
     *
     */
    @Override
    public void cancelRowUpdates() throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Moves the cursor to the insert row.  The current cursor position is
     * remembered while the cursor is positioned on the insert row.
     *
     * The insert row is a special row associated with an updatable
     * result set.  It is essentially a buffer where a new row may
     * be constructed by calling the <code>updateXXX</code> methods prior to
     * inserting the row into the result set.
     *
     * Only the <code>updateXXX</code>, <code>getXXX</code>,
     * and <code>insertRow</code> methods may be
     * called when the cursor is on the insert row.  All of the columns in
     * a result set must be given a value each time this method is
     * called before calling <code>insertRow</code>.
     * The method <code>updateXXX</code> must be called before a
     * <code>getXXX</code> method can be called on a column value.
     *
     * @exception SQLException if a database access error occurs
     * or the result set is not updatable
     */
    @Override
    public void moveToInsertRow() throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Moves the cursor to the remembered cursor position, usually the
     * current row.  This method has no effect if the cursor is not on the insert
     * row.
     *
     * @exception SQLException if a database access error occurs
     * or the result set is not updatable
     */
    @Override
    public void moveToCurrentRow() throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Returns the Statement that produced this <code>ResultSet</code> object.
     * If the result set was generated some other way, such as by a
     * <code>DatabaseMetaData</code> method, this method returns <code>null</code>.
     *
     * @return the Statement that produced the result set or
     * null if the result set was produced some other way
     * @exception SQLException if a database access error occurs
     */
    @Override @CheckReturnValue @Nullable
    public Statement getStatement() throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Returns the value of a column in the current row as a Java object.
     * This method uses the given <code>Map</code> object
     * for the custom mapping of the
     * SQL structured or distinct type that is being retrieved.
     *
     * @return an object representing the SQL value
     * @param i the first column is 1, the second is 2, ...
     * @param map the mapping from SQL type names to Java classes
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue @Nullable
    public Object getObject(int i, java.util.Map map) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Gets a REF(&lt;structured-type&gt;) column value from the current row.
     *
     * @return a <code>Ref</code> object representing an SQL REF value
     * @param i the first column is 1, the second is 2, ...
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue @Nullable
    public Ref getRef(int i) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Gets a BLOB value in the current row of this <code>ResultSet</code> object.
     *
     * @return a <code>Blob</code> object representing the SQL BLOB value in
     *         the specified column
     * @param i the first column is 1, the second is 2, ...
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    @Override  @CheckReturnValue @Nonnull
    public Blob getBlob(int i) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Gets a CLOB value in the current row of this <code>ResultSet</code> object.
     *
     * @return a <code>Clob</code> object representing the SQL CLOB value in
     *         the specified column
     * @param i the first column is 1, the second is 2, ...
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue @Nullable
    public Clob getClob(int i) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Gets an SQL ARRAY value from the current row of this <code>ResultSet</code> object.
     *
     * @return an <code>Array</code> object representing the SQL ARRAY value in
     *         the specified column
     * @param i the first column is 1, the second is 2, ...
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue
    public Array getArray(int i) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Returns the value in the specified column as a Java object.
     * This method uses the specified <code>Map</code> object for
     * custom mapping if appropriate.
     *
     * @return an object representing the SQL value in the specified column
     * @param colName the name of the column from which to retrieve the value
     * @param map the mapping from SQL type names to Java classes
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue @Nullable
    public Object getObject(String colName, java.util.Map map) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Gets a REF(&lt;structured-type&gt;) column value from the current row.
     *
     * @return a <code>Ref</code> object representing the SQL REF value in
     *         the specified column
     * @param colName the column name
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue @Nullable
    public Ref getRef(final @Nonnull String colName) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Gets a BLOB value in the current row of this <code>ResultSet</code> object.
     *
     * @return a <code>Blob</code> object representing the SQL BLOB value in
     *         the specified column
     * @param colName the name of the column from which to retrieve the value
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    @Override  @CheckReturnValue @Nonnull
    public Blob getBlob(String colName) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Gets a CLOB value in the current row of this <code>ResultSet</code> object.
     *
     * @return a <code>Clob</code> object representing the SQL CLOB value in
     *         the specified column
     * @param colName the name of the column from which to retrieve the value
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue @Nullable
    public Clob getClob(final @Nonnull String colName) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Gets an SQL ARRAY value in the current row of this <code>ResultSet</code> object.
     *
     * @return an <code>Array</code> object representing the SQL ARRAY value in
     *         the specified column
     * @param colName the name of the column from which to retrieve the value
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue
    public Array getArray(String colName) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     *
     * @param direction
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue
    public void setFetchDirection(int direction) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Returns the fetch direction for this result set.
     *
     * @return the current fetch direction for this result set
     * @exception SQLException if a database access error occurs
     */
    @Override @CheckReturnValue
    public int getFetchDirection() throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Gives the JDBC driver a hint as to the number of rows that should
     * be fetched from the database when more rows are needed for this result
     * set.  If the fetch size specified is zero, the JDBC driver
     * ignores the value and is free to make its own best guess as to what
     * the fetch size should be.  The default value is set by the statement
     * that created the result set.  The fetch size may be changed at any
     * time.
     *
     * @param rows the number of rows to fetch
     * @exception SQLException if a database access error occurs or the
     * condition 0 <= rows <= this.getMaxRows() is not satisfied.
     */
    @Override @CheckReturnValue
    public void setFetchSize(int rows) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Returns the fetch size for this result set.
     *
     * @return the current fetch size for this result set
     * @exception SQLException if a database access error occurs
     */
    @Override @CheckReturnValue
    public int getFetchSize() throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Indicates whether the current row has been updated.  The value returned
     * depends on whether or not the result set can detect updates.
     *
     * @return true if the row has been visibly updated by the owner or
     * another, and updates are detected
     * @exception SQLException if a database access error occurs
     *
     * @see DatabaseMetaData#updatesAreDetected
     */
    @Override @CheckReturnValue
    public boolean rowUpdated() throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Indicates whether the current row has had an insertion.  The value returned
     * depends on whether or not the result set can detect visible inserts.
     *
     * @return true if a row has had an insertion and insertions are detected
     * @exception SQLException if a database access error occurs
     *
     * @see DatabaseMetaData#insertsAreDetected
     */
    @Override @CheckReturnValue
    public boolean rowInserted() throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Indicates whether a row has been deleted.  A deleted row may leave
     * a visible "hole" in a result set.  This method can be used to
     * detect holes in a result set.  The value returned depends on whether
     * or not the result set can detect deletions.
     *
     * @return true if a row was deleted and deletions are detected
     * @exception SQLException if a database access error occurs
     *
     * @see DatabaseMetaData#deletesAreDetected
     */
    @Override @CheckReturnValue
    public boolean rowDeleted() throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * <p>Moves the cursor to the given row number in the result set.
     *
     * <p>If the row number is positive, the cursor moves to
     * the given row number with respect to the
     * beginning of the result set.  The first row is row 1, the second
     * is row 2, and so on.
     *
     * <p>If the given row number is negative, the cursor moves to
     * an absolute row position with respect to
     * the end of the result set.  For example, calling
     * <code>absolute(-1)</code> positions the
     * cursor on the last row, <code>absolute(-2)</code> indicates the next-to-last
     * row, and so on.
     *
     * <p>An attempt to position the cursor beyond the first/last row in
     * the result set leaves the cursor before/after the first/last
     * row, respectively.
     *
     * <p>Note: Calling <code>absolute(1)</code> is the same
     * as calling <code>first()</code>.
     * Calling <code>absolute(-1)</code> is the same as calling <code>last()</code>.
     *
     * @return true if the cursor is on the result set; false otherwise
     * @param row the row to use
     * @exception SQLException if a database access error occurs or
     * row is 0, or result set type is TYPE_FORWARD_ONLY.
     */
    @Override @CheckReturnValue
    public boolean absolute(int row) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * <p>Moves the cursor a relative number of rows, either positive or negative.
     * Attempting to move beyond the first/last row in the
     * result set positions the cursor before/after the
     * the first/last row. Calling <code>relative(0)</code> is valid, but does
     * not change the cursor position.
     *
     * <p>Note: Calling <code>relative(1)</code>
     * is different from calling <code>next()</code>
     * because is makes sense to call <code>next()</code> when there is no current row,
     * for example, when the cursor is positioned before the first row
     * or after the last row of the result set.
     *
     * @return true if the cursor is on a row; false otherwise
     * @param rows
     * @exception SQLException if a database access error occurs, there
     * is no current row, or the result set type is TYPE_FORWARD_ONLY
     */
    @Override @CheckReturnValue
    public boolean relative(int rows) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     *
     * {@inheritDoc }
     */
    @Override @CheckReturnValue
    public boolean previous() throws SQLException
    {
        Row previousRow = fetchRow(currentRow - 1);
        isAfterLastFg = false;

        if (previousRow == null)
        {
            currentRow = 0;
            currentData = null;
            return false;
        }

        currentRow--;
        currentData = previousRow;

        return true;
    }

    //---------------------------------------------------------------------
    // Traversal/Positioning
    //---------------------------------------------------------------------
    /**
     * JDBC 2.0
     *
     * <p>Indicates whether the cursor is before the first row in the result
     * set.
     *
     * @return true if the cursor is before the first row, false otherwise. Returns
     * false when the result set contains no rows.
     * @exception SQLException if a database access error occurs
     */
    @Override @CheckReturnValue
    public boolean isBeforeFirst() throws SQLException
    {
        if( fetchRow(0) == null) return false;

        return currentRow < 0;
    }

    /**
     * JDBC 2.0
     *
     * <p>Indicates whether the cursor is after the last row in the result
     * set.
     *
     * @return true if the cursor is  after the last row, false otherwise.  Returns
     * false when the result set contains no rows.
     * @exception SQLException if a database access error occurs
     */
    @Override @CheckReturnValue
    public boolean isAfterLast() throws SQLException
    {
        return isAfterLastFg;
    }

    /**
     * JDBC 2.0
     *
     * <p>Indicates whether the cursor is on the first row of the result set.
     *
     * @return true if the cursor is on the first row, false otherwise.
     * @exception SQLException if a database access error occurs
     */
    @Override @CheckReturnValue
    public boolean isFirst() throws SQLException
    {
        return currentRow == 1;
    }

    /**
     * JDBC 2.0
     *
     * <p>Indicates whether the cursor is on the last row of the result set.
     * Note: Calling the method <code>isLast</code> may be expensive
     * because the JDBC driver
     * might need to fetch ahead one row in order to determine
     * whether the current row is the last row in the result set.
     *
     * @return true if the cursor is on the last row, false otherwise.
     * @exception SQLException if a database access error occurs
     */
    @Override @CheckReturnValue
    public boolean isLast() throws SQLException
    {
        boolean last = false;

        if (currentRow == rowPage.size())
        {
            last = true;
        }

        return last;
    }

    /**
     * JDBC 2.0
     *
     * <p>Moves the cursor to the front of the result set, just before the
     * first row. Has no effect if the result set contains no rows.
     *
     * @exception SQLException if a database access error occurs or the
     * result set type is TYPE_FORWARD_ONLY
     */
    @Override @CheckReturnValue
    public void beforeFirst() throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * <p>Moves the cursor to the end of the result set, just after the last
     * row.  Has no effect if the result set contains no rows.
     *
     * @exception SQLException if a database access error occurs or the
     * result set type is TYPE_FORWARD_ONLY
     */
    @Override @CheckReturnValue
    public void afterLast() throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * <p>Moves the cursor to the first row in the result set.
     *
     * @return true if the cursor is on a valid row; false if
     *         there are no rows in the result set
     * @exception SQLException if a database access error occurs or the
     * result set type is TYPE_FORWARD_ONLY
     */
    @Override @CheckReturnValue
    public boolean first() throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * <p>Moves the cursor to the last row in the result set.
     *
     * @return true if the cursor is on a valid row;
     * false if there are no rows in the result set
     * @exception SQLException if a database access error occurs or the
     * result set type is TYPE_FORWARD_ONLY.
     */
    @Override @CheckReturnValue
    public boolean last() throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /** {@inheritDoc} */
    @Override @CheckReturnValue @Nullable
    public int getType() throws SQLException
    {
        return TYPE_SCROLL_INSENSITIVE;
    }

    /**
     * Gets the value of a column in the current row as a java.sql.Timestamp
     * object. This method uses the given calendar to construct an appropriate millisecond
     * value for the Timestamp if the underlying database does not store
     * timezone information.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param cal the calendar to use in constructing the timestamp
     * @return the column value; if the value is SQL NULL, the result is null
     * @exception SQLException if a database access error occurs
     */
    @Override @CheckReturnValue @Nullable
    public java.sql.Timestamp getTimestamp( final int columnIndex, Calendar cal)
            throws SQLException
    {
        validToGet();
        Object obj;
        obj = currentData.getObject(findColumnData(columnIndex));

        if (obj == null)
        {
            return null;
        }
        else if (obj instanceof String)
        {
            String value = (String) obj;
            java.util.Date dateVal = null;
            try
            {
                dateVal = TimeUtil.parse("yyyy-MM-dd HH:mm:ss.SSS", value, cal.getTimeZone());
            }
            catch (java.text.ParseException pe)
            {
                LOGGER.warn( "could not parse " + value, pe);
            }
            if(dateVal == null)
            {
                return null;
            }
            return new Timestamp(dateVal.getTime());
        }
        else if (obj instanceof java.util.Date)
        {
            String value = null;
            java.util.Date dateVal = null;
            try
            {
                value = TimeUtil.format("yyyy-MM-dd HH:mm:ss.SSS", (java.util.Date) obj, TimeZone.getDefault());
                dateVal = TimeUtil.parse("yyyy-MM-dd HH:mm:ss.SSS", value, cal.getTimeZone());
            }
            catch (java.text.ParseException pe)
            {
                LOGGER.warn( "could not parse " + value, pe);
            }
            if(dateVal == null)
            {
                return null;
            }
            return new Timestamp(dateVal.getTime());
        }
        else
        {
            throw new SQLException("Invalid type");
        }
    }

    /**
     * Gets the value of a column in the current row as a java.sql.Timestamp
     * object. This method uses the given calendar to construct an appropriate millisecond
     * value for the Timestamp if the underlying database does not store
     * timezone information.
     *
     * @param columnName the SQL name of the column
     * @param cal the calendar to use in constructing the timestamp
     * @return the column value; if the value is SQL NULL, the result is null
     * @exception SQLException if a database access error occurs
     */
    @Override @CheckReturnValue @Nullable
    public java.sql.Timestamp getTimestamp( final @Nonnull String columnName, Calendar cal)
            throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * Gets the value of a column in the current row as a java.sql.Time
     * object. This method uses the given calendar to construct an appropriate millisecond
     * value for the Time if the underlying database does not store
     * timezone information.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param cal the calendar to use in constructing the time
     * @return the column value; if the value is SQL NULL, the result is null
     * @exception SQLException if a database access error occurs
     */
    @Override @CheckReturnValue @Nullable
    public java.sql.Time getTime( final int columnIndex, Calendar cal) throws SQLException
    {
        validToGet();
        Object obj;
        obj = currentData.getObject(findColumnData(columnIndex));

        if (obj == null)
        {
            return null;
        }
        else if (obj instanceof String)
        {
            String value = (String) obj;
            java.util.Date dateVal = null;
            try
            {
                dateVal = TimeUtil.parse("yyyy-MM-dd HH:mm:ss.SSS", value, cal.getTimeZone());
            }
            catch (java.text.ParseException pe)
            {
                LOGGER.warn( "could not parse " + value, pe);
            }
            if(dateVal == null)
            {
                return null;
            }
            return new Time(dateVal.getTime());
        }
        else if (obj instanceof java.util.Date)
        {
            String value = null;
            java.util.Date dateVal = null;
            try
            {
                value = TimeUtil.format("yyyy-MM-dd HH:mm:ss.SSS", (java.util.Date) obj, TimeZone.getDefault());
                dateVal = TimeUtil.parse("yyyy-MM-dd HH:mm:ss.SSS", value, cal.getTimeZone());
            }
            catch (java.text.ParseException pe)
            {
                LOGGER.warn( "could not parse " + value, pe);
            }
            if(dateVal == null)
            {
                return null;
            }
            return new Time(dateVal.getTime());
        }
        else
        {
            throw new SQLException("Invalid type");
        }
    }

    /**
     * Gets the value of a column in the current row as a java.sql.Time
     * object. This method uses the given calendar to construct an appropriate millisecond
     * value for the Time if the underlying database does not store
     * timezone information.
     *
     * @param columnName the SQL name of the column
     * @param cal the calendar to use in constructing the time
     * @return the column value; if the value is SQL NULL, the result is null
     * @exception SQLException if a database access error occurs
     */
    @Override @CheckReturnValue @Nullable
    public java.sql.Time getTime( final @Nonnull String columnName, Calendar cal) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * <p>Retrieves the current row number.  The first row is number 1, the
     * second number 2, and so on.
     *
     * @return the current row number; 0 if there is no current row
     * @exception SQLException if a database access error occurs
     */
    @Override @CheckReturnValue
    public int getRow() throws SQLException
    {
        if (currentRow < 1)
        {
            return -1;
        }

        return currentRow;
    }

    /**
     *
     * @param columnIndex
     * @param cal
     * @throws java.sql.SQLException if a database-access error occurs.
     * @return the value
     */
    @Override @CheckReturnValue @Nullable
    public java.sql.Date getDate( final int columnIndex, Calendar cal) throws SQLException
    {
        validToGet();
        Object obj;
        obj = currentData.getObject(findColumnData(columnIndex));

        if (obj == null)
        {
            return null;
        }
        else if (obj instanceof String)
        {
            String value = (String) obj;
            java.util.Date dateVal = null;
            try
            {
                dateVal = TimeUtil.parse("yyyy-MM-dd HH:mm:ss.SSS", value, cal.getTimeZone());
            }
            catch (java.text.ParseException pe)
            {
                LOGGER.warn( "could not parse " + value, pe);
            }
            if(dateVal == null)
            {
                return null;
            }
            return new java.sql.Date(dateVal.getTime());
        }
        else if (obj instanceof java.util.Date)
        {
            String value = null;
            java.util.Date dateVal = null;
            try
            {
                value = TimeUtil.format("yyyy-MM-dd HH:mm:ss.SSS", (java.util.Date) obj, TimeZone.getDefault());
                dateVal = TimeUtil.parse("yyyy-MM-dd HH:mm:ss.SSS", value, cal.getTimeZone());
            }
            catch (java.text.ParseException pe)
            {
                LOGGER.warn( "could not parse " + value, pe);
            }
            if(dateVal == null)
            {
                return null;
            }
            return new java.sql.Date(dateVal.getTime());
        }
        else
        {
            throw new SQLException("Invalid type");
        }
    }

    /**
     * Gets the value of a column in the current row as a java.sql.Date
     * object. This method uses the given calendar to construct an appropriate millisecond
     * value for the Date, if the underlying database does not store
     * timezone information.
     *
     * @param columnName the SQL name of the column from which to retrieve the value
     * @param cal the calendar to use in constructing the date
     * @return the column value; if the value is SQL NULL, the result is null
     * @exception SQLException if a database access error occurs
     */
    @Override @CheckReturnValue @Nullable
    public java.sql.Date getDate( final @Nonnull String columnName, Calendar cal) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /** {@inheritDoc} */
    @Override @CheckReturnValue
    public int getConcurrency() throws SQLException
    {
        return CONCUR_READ_ONLY;
    }

    /**
     * JDBC 2.0
     *
     * <p>Gets the value of a column in the current row as a java.io.Reader.
     * @param columnIndex the first column is 1, the second is 2, ...
     * @throws java.sql.SQLException if a database-access error occurs.
     * @return the value
     */
    @Override @CheckReturnValue @Nullable
    public java.io.Reader getCharacterStream( final int columnIndex) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * <p>Gets the value of a column in the current row as a java.io.Reader.
     * @return the value in the specified column as a <code>java.io.Reader</code>
     * @param columnName the name of the column
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue @Nullable
    public java.io.Reader getCharacterStream( final @Nonnull String columnName) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * JDBC 2.0
     *
     * Gets the value of a column in the current row as a java.math.BigDecimal
     * object with full precision.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value (full precision); if the value is SQL NULL,
     * the result is null
     * @exception SQLException if a database access error occurs
     * @deprecated
     */
    @Override @CheckReturnValue @Nonnull
    public BigDecimal getBigDecimal( final int columnIndex) throws SQLException
    {
        //throw new SoapSQLException( "Not Supported");
        validToGet();
        Object obj = getObject(columnIndex);
        if (obj == null)
        {
            return BigDecimal.ZERO;
        }
        else if (obj instanceof BigDecimal)
        {
            return (BigDecimal) obj;
        }
        else if (obj instanceof Byte || obj instanceof Short || obj instanceof Integer || obj instanceof Long)
        {
            return new BigDecimal(((Long) obj));
        }
        else if (obj instanceof Float || obj instanceof Double)
        {
            double d = ((Number)obj).doubleValue();
            return new BigDecimal(d);
        }
        else if (obj instanceof String)
        {
            try
            {
                BigDecimal bDec = new BigDecimal((String) obj);
                return bDec;
            }
            catch (NumberFormatException nfe)
            {
                throw new SoapSQLException("Format Error " + nfe.getMessage());
            }
        }
        else
        {
            throw new SoapSQLException("Format Error");
        }
    }

    /**
     * JDBC 2.0
     *
     * Gets the value of a column in the current row as a java.math.BigDecimal
     * object with full precision.
     * @param columnName the column name
     * @return the column value (full precision); if the value is SQL NULL,
     * the result is null
     * @exception SQLException if a database access error occurs
     *
     */
    @Override  @CheckReturnValue @Nonnull
    public BigDecimal getBigDecimal( final @Nonnull String columnName) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     *
     * @param str
     * @param ref
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    @Override
    public void updateRef(String str, java.sql.Ref ref) throws java.sql.SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     *
     * @param param
     * @throws java.sql.SQLException if a database-access error occurs.
     * @return the value
     */
    @Override @CheckReturnValue @Nullable
    public java.net.URL getURL(int param) throws java.sql.SQLException
    {
        return null;
    }

    /**
     *
     * @param param
     * @param blob
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    @Override
    public void updateBlob(int param, java.sql.Blob blob) throws java.sql.SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     *
     * @param param
     * @param array
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    @Override
    public void updateArray(int param, java.sql.Array array) throws java.sql.SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     *
     * @param str
     * @param clob
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    @Override
    public void updateClob(String str, java.sql.Clob clob) throws java.sql.SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     *
     * @param param
     * @param ref
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    @Override
    public void updateRef(int param, java.sql.Ref ref) throws java.sql.SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     *
     * @param str
     * @throws java.sql.SQLException if a database-access error occurs.
     * @return the value
     */
    @Override @CheckReturnValue @Nullable
    public java.net.URL getURL(String str) throws java.sql.SQLException
    {
        return null;
    }

    /**
     *
     * @param str
     * @param blob
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    @Override
    public void updateBlob(String str, java.sql.Blob blob) throws java.sql.SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     *
     * @param str
     * @param array
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    @Override
    public void updateArray(String str, java.sql.Array array) throws java.sql.SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     *
     * @param param
     * @param clob
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    @Override
    public void updateClob(int param, java.sql.Clob clob) throws java.sql.SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     * Returns a formatted plain text representation of this result set, by calling
     * the method formatOutput. If the formatOutput method throws an Exception, then
     * this method will return the result of calling the toString method of its super
     * class.
     *
     * @return String the string representation of this result set
     */
    @Override @CheckReturnValue @Nonnull
    public String toString()
    {
        String str;
        try
        {
            str = this.formatOutput();
        }
        catch (Exception degrade)
        {
            str = super.toString();
        }
        return str;
    }

    @Override @CheckReturnValue @Nullable
    public RowId getRowId( final int columnIndex) throws SQLException
    {
    throw new SoapSQLException( "Not Supported");
    }
    @Override @CheckReturnValue @Nullable
    public RowId getRowId( final @Nonnull String columnName) throws SQLException
    {
    throw new SoapSQLException( "Not Supported");
    }
    @Override
    public void updateRowId( final int columnIndex, RowId x) throws SQLException
    {
    throw new SoapSQLException( "Not Supported");
    }
    @Override
    public void updateRowId( final @Nonnull String columnName, RowId x) throws SQLException
    {
    throw new SoapSQLException( "Not Supported");
    }
    
    /**
     *
     * @return the value
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue
    public int getHoldability() throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     *
     * @return the value
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    @Override @CheckReturnValue
    public boolean isClosed() throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     *
     * @param columnIndex
     * @param nString
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    @Override
    public void updateNString( final int columnIndex, String nString) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }

    /**
     *
     * @param columnName
     * @param nString
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    @Override
    public void updateNString( final @Nonnull String columnName, String nString) throws SQLException
    {
        throw new SoapSQLException("Not Supported");
    }
    
    @Override
    public void updateNClob( final int columnIndex, NClob nClob) throws SQLException
    {
    throw new SoapSQLException( "Not Supported");
    }
    @Override
    public void updateNClob( final @Nonnull String columnName, NClob nClob) throws SQLException
    {
    throw new SoapSQLException( "Not Supported");
    }
    @Override @CheckReturnValue @Nullable
    public NClob getNClob( final int columnIndex) throws SQLException
    {
    throw new SoapSQLException( "Not Supported");
    }
    @Override @CheckReturnValue @Nullable
    public NClob getNClob( final @Nonnull String columnName) throws SQLException
    {
    throw new SoapSQLException( "Not Supported");
    }
    @Override @CheckReturnValue @Nullable
    public SQLXML getSQLXML( final int columnIndex) throws SQLException
    {
    throw new SoapSQLException( "Not Supported");
    }
    @Override @CheckReturnValue @Nullable
    public SQLXML getSQLXML( final @Nonnull String columnName) throws SQLException
    {
    throw new SoapSQLException( "Not Supported");
    }
    @Override
    public void updateSQLXML( final int columnIndex, SQLXML xmlObject) throws SQLException
    {
    throw new SoapSQLException( "Not Supported");
    }
    @Override
    public void updateSQLXML( final @Nonnull String columnName, SQLXML xmlObject) throws SQLException
    {
    throw new SoapSQLException( "Not Supported");
    }   

    @Override @CheckReturnValue @Nullable
    public String getNString( final int columnIndex) throws SQLException
    {
    throw new SoapSQLException( "Not Supported");
    }
    @Override @CheckReturnValue @Nullable
    public String getNString( final @Nonnull String columnName) throws SQLException
    {
    throw new SoapSQLException( "Not Supported");
    }
    @Override @CheckReturnValue @Nullable
    public Reader getNCharacterStream( final int columnIndex) throws SQLException
    {
    throw new SoapSQLException( "Not Supported");
    }
    @Override @CheckReturnValue @Nullable
    public Reader getNCharacterStream( final @Nonnull String columnName) throws SQLException
    {
    throw new SoapSQLException( "Not Supported");
    }
    public void updateNCharacterStream( final int columnIndex, Reader x, int length) throws SQLException
    {
    throw new SoapSQLException( "Not Supported");
    }
    public void updateNCharacterStream( final @Nonnull String columnName, Reader x, int length) throws SQLException
    {
    throw new SoapSQLException( "Not Supported");
    }
    @Override @CheckReturnValue
    public Object unwrap(Class iface) throws SQLException
    {
    throw new SoapSQLException( "Not Supported");
    }
    @Override @CheckReturnValue
    public boolean isWrapperFor(Class iface) throws SQLException
    {
    throw new SoapSQLException( "Not Supported");
    }

    @Override
    public void updateNCharacterStream( final int columnIndex, Reader x, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateAsciiStream( final int columnIndex, InputStream x, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateBinaryStream( final int columnIndex, InputStream x, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateCharacterStream( final int columnIndex, Reader x, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateBlob( final int columnIndex, InputStream inputStream, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateClob( final int columnIndex, Reader reader, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateNClob( final int columnIndex, Reader reader, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateNCharacterStream( final int columnIndex, Reader x) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateAsciiStream( final int columnIndex, InputStream x) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateBinaryStream( final int columnIndex, InputStream x) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateCharacterStream( final int columnIndex, Reader x) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateBlob( final int columnIndex, InputStream inputStream) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateClob( final int columnIndex, Reader reader) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateNClob( final int columnIndex, Reader reader) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     *
     */
    protected int rowOffset;
    /**
     *
     */
    protected ArrayList rowPage;
    /**
     *
     */
    protected ArrayList<Column> columns;
    /**
     *
     */
    protected HashMap columnKeys;
    /**
     *
     */
    protected int currentRow;
    /**
     *
     */
    protected Row currentData;
    private int lastCol;
    private boolean isAfterLastFg;

    @Override @CheckReturnValue @Nullable
    public <T> T getObject( final int columnIndex, Class<T> type) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override @CheckReturnValue @Nullable
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
