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
package com.aspc.remote.jdbc;

import com.aspc.remote.database.internal.Column;
import com.aspc.remote.database.internal.Row;
import com.aspc.remote.soap.Constants;
import com.aspc.remote.memory.HashMapFactory;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.DateUtil;
import com.aspc.remote.util.misc.StringUtilities;
import com.aspc.remote.util.misc.TimeUtil;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimeZone;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *  SResultSet
 *
 * <i>THREAD MODE: SINGLE-THREADED</i>
 * @author Nigel Leck
 * @since 29 September 2006
 */
public class SoapRow extends Row
{
    /** the cells */
    protected HashMap  cellAttributes;
    /** the rows attributes */
    protected HashMap  rowAttributes;

    /**
    * Get the row attribute value of a column in the current row as a Java String.
    *
    * @param key the attribute name
    * @return String the attribute value
    * @exception SQLException if a database-access error occurs.
    */
    @CheckReturnValue @Nullable
    public String getRowAttributeValue(final @Nonnull String key) throws SQLException
    {
        if( rowAttributes == null)
        {
            return null;
        }

        return (String)rowAttributes.get(key);
    }

   /**
    * Get the cell Attribute names
    *
    * @param column the column
    * @param key the key
    * @return HashMap the map of attributes and its value.
    * @exception SQLException if a database-access error occurs.
    */
    public String getAttributeValue(Column column, final String key) throws SQLException
    {
        if( cellAttributes == null)
        {
            return null;
        }

        HashMap map = (HashMap) cellAttributes.get(column.getName());
        if( map == null)
        {
           return null;
        }

        return (String)map.get(key);
    }

   /**
    * Get the cell Attribute names
    *
    * @param column the column
    * @return HashMap the map of attributes and its value.
    * @exception SQLException if a database-access error occurs.
    */
    public Object[] getAttributeNames(Column column) throws SQLException
    {
        if( cellAttributes == null)
        {
            return null;
        }

        HashMap map = (HashMap) cellAttributes.get(column.getName());
        if( map == null)
        {
           return null;
        }

        return map.keySet().toArray();
    }

   /**
    * Get the row attribute Names
    *
    * @return HashMap the map of attributes and its value.
    * @exception SQLException if a database-access error occurs.
    */
    @CheckReturnValue @Nullable
    public Object[] getRowAttributeNames() throws SQLException
    {
        if( rowAttributes == null)
        {
            return null;
        }

        return rowAttributes.keySet().toArray();
    }

    /**
     * The row for the soap result set
     * @param tz The timezone
     * @param columns the list of columns
     * @param rowNode the xml row
     * @throws Exception a serious problem
     */
    public SoapRow( final TimeZone tz, final ArrayList columns, final Node rowNode) throws Exception
    {
        super(tz);

        int colCount;

        colCount     = columns.size();
        cells        = new Object[ colCount];

        NodeList fieldList = ((Element)rowNode).getChildNodes();

        /*
         * Add Row attributes
         */
        NamedNodeMap mp = rowNode.getAttributes();
        int len = mp.getLength();

        for(int j=0; j<len; j++)
        {
            String attributeName = mp.item(j).getNodeName();

            if(canAddAttribute(attributeName))
            {
                if(rowAttributes == null)
                {
                    rowAttributes = HashMapFactory.create();
                }
                rowAttributes.put(attributeName,mp.item(j).getNodeValue() );
            }
        }

        // populate the NULL values.
        for( int c = 0; c < columns.size(); c++)
        {
            Column column = (Column)columns.get( c);

            int type = column.getType();

            if( type == Types.BOOLEAN) cells[c] = Boolean.FALSE;
        }

        for( int i = 0; i < fieldList.getLength(); i++)
        {
            Element field = (Element)fieldList.item( i);

            /*
             * Add Cell attributes
             */
            mp = field.getAttributes();
            len = mp.getLength();
            HashMap cellMap = HashMapFactory.create();
            for(int j=0; j<len; j++)
            {
                String attributeName = mp.item(j).getNodeName();

                if(canAddAttribute(attributeName))
                {
                    if(cellAttributes == null)
                    {
                        cellAttributes = HashMapFactory.create();
                    }
                    cellMap.put(attributeName,mp.item(j).getNodeValue() );
                }
            }

            String value=null;
            if( field.getFirstChild() != null)
            {
                value = field.getFirstChild().getNodeValue();
            }

            /**
             * If the value is encoded then decode it. This is to handle the
             * invalid XML characters.
             *
             * http://www.w3.org/TR/xml/#dt-character
             */
            if( field.hasAttribute( Constants.ATT_RESULTSET_TR_COL_ENCODING))
            {
                String encoding = field.getAttribute(Constants.ATT_RESULTSET_TR_COL_ENCODING);

                if( encoding.equals( Constants.ENCODING_BASE64))
                {
                    value=StringUtilities.decodeUTF8base64( value);
                }
            }

            String tagName= field.getTagName();
            if( tagName.equals(Constants.ELM_RESULTSET_TR_COL))
            {
                String path = field.getAttribute( Constants.ATT_RESULTSET_TR_COL_PATH);

                for( int c = 0; c < columns.size(); c++)
                {
                    Column column = (Column)columns.get( c);

                    if( column.getName().equals( path))
                    {
                        cells[c]=parseValue( column, value);

                        if(cellAttributes != null)
                        {
                            cellAttributes.put(column.getName(), cellMap);
                        }
                    }
                }
            }
            else
            {
                int c = Integer.parseInt(tagName.substring(1)) - 1;
                Column column = (Column)columns.get( c );

                cells[c]=parseValue( column, value);

                if(cellAttributes != null)
                {
                    cellAttributes.put(column.getName(), cellMap);
                }
            }
        }
    }

    private boolean canAddAttribute(final String attributeName)
    {
        if(
            //attributeName.equalsIgnoreCase(Constants.ATT_RESULTSET_TR_REC_ID) == true ||
            //attributeName.equalsIgnoreCase(Constants.ATT_RESULTSET_TR_REC_KEY) == true ||
            attributeName.equalsIgnoreCase(Constants.ATT_RESULTSET_TR_COL_PATH) == true)
        {
           return false;
        }

        return true;
    }

    private Object parseValue( Column column, String value) throws Exception
    {
        int type = column.getType();

        if( type == Types.CHAR)
        {
            return value == null ? NULL_STR : value;
        }

        if( StringUtilities.isBlank( value) )
        {
            if( type == Types.BOOLEAN) return Boolean.FALSE;

            return null;
        }

        if( type == Types.INTEGER)
        {
            return Integer.valueOf( value);
        }
        else if( type == Types.FLOAT)
        {
            return new Double(value);
        }
        else if( type == Types.BOOLEAN)
        {
            if( value.equals( "Y"))
            {
                return Boolean.TRUE;
            }
            else if( value.equals( "N"))
            {
                return Boolean.FALSE;
            }
            throw new Exception( "invalid boolean '" + value + "'");
        }
        else if( type == Types.BIGINT)
        {
            return new Long( value);
        }
        else if( type == Types.DATE)
        {
            return new java.sql.Date(TimeUtil.parse( "d MMM yyyy", value, DateUtil.GMT_ZONE).getTime());
        }
        else if( type == Types.TIMESTAMP)
        {
            return new java.sql.Timestamp(TimeUtil.parse( "yyyy-MM-dd HH:mm:ss.SSS", value, tz).getTime());
        }
        else
        {
            throw new Exception( "unknown type=" + type);
        }
    }
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.jdbc.SoapRow");//#LOGGER-NOPMD
}
