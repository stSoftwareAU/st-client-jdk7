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


import java.sql.*;
import java.util.*;

import com.aspc.remote.database.internal.*;


/**
 *  SoapResultSetMetaData
 *
 * <i>THREAD MODE: SINGLE-THREADED</i>
 *
 * @author      Nigel Leck
 * @since 29 September 2006
 */

public class SoapResultSetMetaData extends SResultSetMetaData
{
    /** {@inheritDoc}
     * @param columns
     */
    public SoapResultSetMetaData( ArrayList columns)
    {
        super(columns);
    }

    /** {@inheritDoc}
     * @param column
     * @return the value
     * @throws java.sql.SQLException */
    @Override
    public String getColumnClassName(int column) throws SQLException
    {
        int type = getColumnType( column);

        switch( type)
        {
            case Types.CHAR:
                return "java.lang.String";
            case Types.INTEGER:
                return "java.lang.Integer";
            case Types.FLOAT:
                return "java.lang.Double";
            case Types.DATE:
                return "java.sql.Date";
            case Types.BIGINT:
                return "java.math.BigInteger";
            case Types.TIMESTAMP:
                return "java.sql.Timestamp";
            case Types.BOOLEAN:
                return "java.lang.Boolean";
            default:
                return "java.lang.Object";
        }
    }

}

