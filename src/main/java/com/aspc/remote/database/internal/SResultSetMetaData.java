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
package com.aspc.remote.database.internal;

import com.aspc.remote.jdbc.SoapSQLException;
import java.sql.*;
import java.util.*;

/**
 *  SResultSetMetaData
 *
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED sql</i>
 *
 *  @author      Nigel Leck
 *  @since       29 September 2006
 */
public class SResultSetMetaData implements ResultSetMetaData
{
    private final ArrayList columns;

    /**
     * 
     * @param columns the columns
     */
    public SResultSetMetaData( ArrayList columns)
    {
        this.columns = columns;
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnCount() throws SQLException
    {
        return columns.size();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAutoIncrement(int column) throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCaseSensitive(int column) throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSearchable(int column) throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCurrency(int column) throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int isNullable(int column) throws SQLException
    {
        return ResultSetMetaData.columnNullableUnknown;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSigned(int column) throws SQLException
    {
        int type = getColumnType( column);
        
        return type        == Types.SMALLINT                   ||
                type        == Types.INTEGER                    ||
                type        == Types.BIT                        ||
                type        == Types.TINYINT                    ||
                type        == Types.REAL                       ||
                type        == Types.DOUBLE                     ||
                type        == Types.FLOAT                      ||
                type        == Types.NUMERIC                    ||
                type        == Types.DECIMAL                    ||
                type        == Types.BIGINT;        
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnDisplaySize(int column) throws SQLException
    {
        return -1;
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnLabel(int column) throws SQLException
    {
        return getColumnName( column);
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnName(int column) throws SQLException
    {
        Column col;

        col = (Column)columns.get(column -1);

        return col.getName();
    }

    /** {@inheritDoc} */
    @Override
    public String getSchemaName(int column) throws SQLException
    {
        return "";
    }


    /** {@inheritDoc} */
    @Override
    public int getPrecision(int column) throws SQLException
    {
        int type = getColumnType( column);
        
        switch( type)
        {
            case Types.CHAR: 
                return 0;
            case Types.INTEGER: 
                return 10;
            case Types.FLOAT: 
                return 17;
            case Types.DATE: 
                return 10;
            case Types.BIGINT: 
                return 19;
            case Types.TIMESTAMP: 
                return 12;
            case Types.BOOLEAN: 
                return 1;
            default:
                return 0;
        }        
    }


    /** {@inheritDoc} */
    @Override
    public int getScale(int column) throws SQLException
    {
        int type = getColumnType( column);
        
        switch( type)
        {
            case Types.CHAR: 
                return 0;
            case Types.INTEGER: 
                return 0;
            case Types.FLOAT: 
                return 17;
            case Types.DATE: 
                return 0;
            case Types.BIGINT: 
                return 0;
            case Types.TIMESTAMP: 
                return 0;
            case Types.BOOLEAN: 
                return 0;
            default:
                return 0;
        }        
    }

    /** {@inheritDoc} */
    @Override
    public String getTableName(int column) throws SQLException
    {
        return "";
    }


    /** {@inheritDoc} */
    @Override
    public String getCatalogName(int column) throws SQLException
    {
        return "";
    }


    /** {@inheritDoc} */
    @Override
    public int getColumnType(int column) throws SQLException
    {
        Column col;

        col = (Column)columns.get(column -1);

        return col.getType();
    }


    /** {@inheritDoc} */
    @Override
    public String getColumnTypeName(int column) throws SQLException
    {
        int type = getColumnType( column);
        
        switch( type)
        {
            case Types.CHAR: 
                return "TEXT";
            case Types.INTEGER: 
                return "INTEGER";
            case Types.FLOAT: 
                return "DOUBLE";
            case Types.DATE: 
                return "DATE";
            case Types.BIGINT: 
                return "BIGINT";
            case Types.TIMESTAMP: 
                return "TIMESTAMP";
            case Types.BOOLEAN: 
                return "BOOLEAN";
            default:
                return "UNKNOWN";
        }
    }


    /** {@inheritDoc} */
    @Override
    public boolean isReadOnly(int column) throws SQLException
    {
        return true;
    }


    /** {@inheritDoc} */
    @Override
    public boolean isWritable(int column) throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public  boolean isDefinitelyWritable(int column) throws SQLException
    {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnClassName(int column) throws SQLException
    {
        throw new SoapSQLException( "Not supported");
    }

    /** {@inheritDoc}
     * @param <T> */
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException
    {
        throw new SoapSQLException( "Not supported");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException
    {
        throw new SoapSQLException( "Not supported");
    }
}
