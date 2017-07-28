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
package com.aspc.remote.jdbc;

import com.aspc.remote.database.internal.Column;
import com.aspc.remote.database.internal.Row;
import com.aspc.remote.database.internal.SResultSet;
import com.aspc.remote.jdbc.impl.SoapResultSetMetaData;
import com.aspc.remote.soap.Constants;
import com.aspc.remote.memory.HashMapFactory;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.DocumentUtil;
import com.aspc.remote.util.misc.StringUtilities;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.TimeZone;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *  SResultSet
 *
 * <i>THREAD MODE: SINGLE-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since 29 September 2006
 */
public class SoapResultSet extends SResultSet
{
    /**
     * the result set
     * @param doc the document
     * @param rsCount the result count
     * @param callback the call back
     * @throws Exception a serious problem
     */
    public SoapResultSet( final Document doc, final int rsCount, final Executor callback) throws Exception
    {
        this.doc = doc;
        this.callback = callback;

        this.rsCount = rsCount;

        rsSeq = -1;

        decodeData();
    }

   /**
    * Get the rowAttributes
    *
    * @return HashMap the map of attributes and its value.
    * @exception SQLException if a database-access error occurs.
    */
    @CheckReturnValue @Nullable
    public Object[] getRowAttributeNames() throws SQLException
    {
        validToGet();
        return ((SoapRow)currentData).getRowAttributeNames();
    }


   /**
    * Get the rowAttributes
    *
    * @param key the key
    * @return HashMap the map of attributes and its value.
    * @exception SQLException if a database-access error occurs.
    */
    @CheckReturnValue @Nullable
    public String getRowAttributevValue(final @Nonnull String key) throws SQLException
    {
        validToGet();
        return ((SoapRow)currentData).getRowAttributeValue(key);
    }

     /**
     * Get the value of a column in the current row as a Java String.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the column value; if the value is SQL NULL, the result is null
     * @exception SQLException if a database-access error occurs.
     */
    @CheckReturnValue
     public Object[] getAttributeNames(int columnIndex) throws SQLException
     {
        validToGet();

        return ((SoapRow)currentData).getAttributeNames( findColumnData( columnIndex));
     }

     /**
     * Get the value of a column in the current row as a Java String.
     *
     * @param columnName the column name
     * @return the column value; if the value is SQL NULL, the result is null
     * @exception SQLException if a database-access error occurs.
     */
     @CheckReturnValue
     public Object[] getAttributeNames(String columnName) throws SQLException
     {
        validToGet();

        return ((SoapRow)currentData).getAttributeNames( findColumnData( columnName));
     }

    /**
    * Get the cellAttributes
    *
    * @param columnIndex the first column is 1, the second is 2, ...
    * @param key the key
    * @return HashMap the map of attributes and its value.
    * @exception SQLException if a database-access error occurs.
    */
     @CheckReturnValue
    public String getAttributeValue(int columnIndex, final String key) throws SQLException
    {
        validToGet();

        return ((SoapRow)currentData).getAttributeValue( findColumnData( columnIndex), key);
    }

   /**
    * Get the cellAttributes
    *
    * @param columnName the column name
    * @param key the key
    * @return HashMap the map of attributes and its value.
    * @exception SQLException if a database-access error occurs.
    */
    @CheckReturnValue
    public String getAttributeValue(String columnName, final String key) throws SQLException
    {
        validToGet();

        return ((SoapRow)currentData).getAttributeValue( findColumnData( columnName), key);
    }

    /**
     *
     * @param orginalCmds the command
     * @return the title
     */
    @CheckReturnValue
    public String getTitle( String orginalCmds)
    {
        if( StringUtilities.isBlank( rsTitle) == false)
        {
            return rsTitle;
        }

        String[] list = StringUtilities.splitCommands( orginalCmds);

        if( rsSeq >= 0 && rsSeq < list.length)
        {
            return list[rsSeq];
        }

        return "unknown";
    }

    /**
     *
     * @return the set count
     */
    @CheckReturnValue
    public int getSetCount()
    {
        return rsCount;
    }

    /** {@inheritDoc}
     * @throws java.sql.SQLException */
    @Override
    public void close() throws SQLException
    {
        NodeList sets = doc.getElementsByTagName( Constants.ELM_RESULTSET);

        int len = sets.getLength();

        for( int i = 0; i < len; i++)
        {
            Element rs = (Element)sets.item( i);
            String cursorName = rs.getAttribute( Constants.ATT_RESULTSET_CURSOR);

            if( StringUtilities.isBlank( cursorName) == false)
            {
                try
                {
                    callback.execute( "CURSOR CLOSE " + cursorName);
                }
                catch( Exception e)
                {
                    LOGGER.info( "CURSOR CLOSE " + cursorName, e);
                }
            }
        }
    }

    /**
     *
     * @throws Exception a serious problem
     * @return the next result NULL if no next result set
     */
    @CheckReturnValue
    public @Nullable SoapResultSet nextResultSet() throws Exception
    {
        return getResultSet( rsCount + 1);
    }

    /**
     *
     * @param rsCount the result set count
     * @throws Exception a serious problem
     * @return the result set
     */
    @CheckReturnValue
    public @Nullable SoapResultSet getResultSet( int rsCount) throws Exception
    {
        if( hasResultSet( rsCount))
        {
            return new SoapResultSet( doc, rsCount, callback);
        }
        else
        {
            return null;
        }
    }

    /**
     *
     * @param rsCount the result set count
     * @return true if we have a result set
     */
    @CheckReturnValue
    public boolean hasResultSet( int rsCount)
    {
        NodeList sets = doc.getElementsByTagName( Constants.ELM_RESULTSET);

        return rsCount < sets.getLength();
    }

    /** {@inheritDoc}
     * @return the value
     * @throws java.sql.SQLException */
    @Override @CheckReturnValue
    public int getFetchDirection() throws SQLException
    {
        if(doc != null)
        {
            return this.fetchDirection;
        }
        else
        {
            throw new SQLException("ResultSet closed");
        }
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
        return new SoapResultSetMetaData(columns);
    }

    /** {@inheritDoc}
     * @param fetchDirection the direction
     * @throws java.sql.SQLException if a database-access error occurs.
     */
    @Override
    public void setFetchDirection(int fetchDirection) throws SQLException
    {
        if(doc != null)
        {
            this.fetchDirection = fetchDirection;
        }
        else
        {
            throw new SQLException("ResultSet closed");
        }

    }

    /** {@inheritDoc}
     * @return the value
     * @throws java.sql.SQLException */
    @Override @CheckReturnValue
    public int getFetchSize() throws SQLException
    {
        if(doc == null)
        {
            throw new SQLException("ResultSet Closed");
        }
        else
        {
            return fetchSize;
        }
    }

    /** {@inheritDoc}
     * @throws java.sql.SQLException */
     @Override
    public void setFetchSize(int rows) throws SQLException
    {
        if( doc == null)
        {
            throw new SQLException("ResultSet Closed");
        }
        else if( rows < 0)
        {
            throw new SQLException("Invalid Fetch Size");
        }
        else
        {
            fetchSize = rows;
        }
    }

    /** {@inheritDoc}
     * @return the value
     * @throws java.sql.SQLException */
    @Override @CheckReturnValue @Nullable
    protected Row fetchRow( int row) throws SQLException
    {
        if( row < 1)
        {
            return null;
        }

        if( row >= 1 && row <= rowPage.size())
        {
            return (Row)rowPage.get( row - 1);
        }

        try
        {
            if( StringUtilities.isBlank( nextCursorName) == false)
            {
                Document tmpDoc = callback.execute( "CURSOR FETCH " + nextCursorName + ", " + rowPage.size() + ", 100");
                NodeList sets = tmpDoc.getElementsByTagName( Constants.ELM_RESULTSET);

                Element rs = (Element)sets.item( 0);

                decodeRS(rs);

                return fetchRow( row);
            }
        }
        catch( Exception e)
        {
           throw new SoapSQLException( e.getMessage());
        }

        return null;
    }

    private void decodeData() throws Exception
    {
        NodeList sets = doc.getElementsByTagName( Constants.ELM_RESULTSET);

        Element rs = (Element)sets.item( rsCount);
        if( rs == null)
        {
            throw new Exception( "Not a record set");
        }

        try
        {
            decodeRS(rs);
        }
        catch( Exception e)
        {
            LOGGER.warn( DocumentUtil.docToString(doc));

            throw e;
        }
    }

    private void decodeRS(final Element rs) throws Exception
    {
        if( rs.hasAttribute( Constants.ATT_RESULTSET_TITLE))
        {
            rsTitle = rs.getAttribute( Constants.ATT_RESULTSET_TITLE);
        }

        if( rsSeq == -1)
        {
            String temp = rs.getAttribute( Constants.ATT_RESULTSET_SEQ);

            rsSeq = 0;

            if( StringUtilities.isBlank( temp) == false)
            {
                rsSeq = Integer.parseInt( temp);
            }
        }

        nextCursorName = rs.getAttribute( Constants.ATT_RESULTSET_CURSOR);

        NodeList cols = rs.getElementsByTagName( Constants.ELM_RESULTSET_CD);

        if( rowPage == null)
        {
            rowPage = new ArrayList();

            columns = new ArrayList();
            columnKeys = HashMapFactory.create();

            for(int i = 0; i < cols.getLength(); i++)
            {
                Node node = cols.item( i);
                String name, type;

                name = ((Element)node).getAttribute( Constants.ATT_RESULTSET_CD_PATH);
                type = ((Element)node).getAttribute( Constants.ATT_RESULTSET_CD_TYPE);

                int tn;

                if( type.equalsIgnoreCase( Constants.FIELD_TYPE_DATE))
                {
                    tn = Types.DATE;
                }
                else if( type.equalsIgnoreCase( Constants.FIELD_TYPE_DOUBLE))
                {
                    tn = Types.DOUBLE;
                }
                else if( type.equalsIgnoreCase( Constants.FIELD_TYPE_STRING))
                {
                    tn = Types.CHAR;
                }
                else if( type.equalsIgnoreCase( Constants.FIELD_TYPE_INTEGER))
                {
                    tn = Types.INTEGER;
                }
                else if( type.equalsIgnoreCase( Constants.FIELD_TYPE_BOOLEAN))
                {
                    tn = Types.BOOLEAN;
                }
                else if( type.equalsIgnoreCase( Constants.FIELD_TYPE_LONG))
                {
                    tn = Types.BIGINT;
                }
                else if( type.equalsIgnoreCase( Constants.FIELD_TYPE_TIMESTAMP))
                {
                    tn = Types.TIMESTAMP;
                }
                else
                {
                    throw new Exception( "unknown type '" + type + "' column: " + i);
                }

                Column col;

                col = new Column( this, name, tn, i);

                columns.add( col);

                columnKeys.put( name.toLowerCase(), col);
            }
        }

        NodeList rowList = rs.getElementsByTagName( Constants.ELM_RESULTSET_TR);
        TimeZone tz = callback.getTimeZone();
        for(int i = 0; i < rowList.getLength(); i++)
        {
            Node row = rowList.item( i);

            rowPage.add(
                new SoapRow( tz, columns, row)
            );
        }
    }

    @CheckReturnValue
    public int getUpdateCount() throws SQLException
    {
        Row row = this.fetchRow(1);
        if(getColumnCount()==1 && getColumnName(1).equals("row_count"))
        {
            return row.getInt(findColumnData(1));
        }
        else if(getColumnCount()==1 && getColumnName(1).equals("global_key"))
        {
            return 1;
        }
        return -1;
    }

    @CheckReturnValue
    public boolean isUpdateResult() throws SQLException
    {
        if( getColumnCount()==1 && (getColumnName(1).equals("row_count") || getColumnName(1).equals("global_key")) )
        {
            return true;
        }
        return false;
    }


    private String      nextCursorName;

    private String      rsTitle = "";

    private final int   rsCount;
    private int         rsSeq;
    private int fetchDirection = FETCH_UNKNOWN;
    private int fetchSize = 0;

    private final Document    doc;
    private final Executor    callback;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.jdbc.SoapResultSet");//#LOGGER-NOPMD
}
