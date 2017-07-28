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
package com.aspc.remote.database.internal;

import org.apache.commons.logging.Log;
import java.sql.*;
import com.aspc.remote.util.misc.*;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 *  Column
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED sql</i>
 *  @author      Nigel Leck
 *  @since       19 November 1998
 */
public final class Column
{
    /**
     * A column
     * 
     * @param rs the result set.
     * @param inName the name
     * @param inType the type
     * @param inNumber the number
     */
    public Column(
        final SResultSet rs,
        final String inName,
        final int inType,
        final int inNumber
    )
    {
        this.rs = rs;

        name            = inName;
        type            = inType;
        number          = inNumber;
        displaySize     = -1;
        
        if(
            type        == Types.CHAR                       ||
            type        == 11                               ||
            type        == 2005                             ||
            type        == Types.VARCHAR                    ||
            type        == Types.OTHER                      ||
            type        == Types.LONGVARCHAR
        )
        {
            type = Types.CHAR;
        }
        else if(
            type        == Types.SMALLINT                   ||
            type        == Types.INTEGER                    ||
            type        == Types.BIT                        ||
            type        == Types.TINYINT
        )
        {
            type = Types.INTEGER;
        }
        else if(
            type        == Types.REAL                       ||
            type        == Types.DOUBLE                     ||
            type        == Types.FLOAT                      ||
            type        == Types.NUMERIC                    ||
            type        == Types.DECIMAL
        )
        {
            type = Types.FLOAT;
        }
        else if(
            type        == Types.DATE                       ||
            type        == Types.TIME
        )
        {
            type = Types.DATE;
        }
        else if(
            type   == Types.BIGINT                          ||
            type   == Types.TIMESTAMP                       ||
            type   == Types.BOOLEAN
        )
        {
            // Done.
        }
        else
        {
            LOGGER.info( "WARNING: Unknown column type " + type + " for " + inName);
        }
    }

    /**
     * The column's name
     * @return the value
     */
    @CheckReturnValue @Nonnull
    public String getName()
    {
        return name;
    }
    
    /**
     * 
     * @return the value
     */
    @CheckReturnValue
    public int getType()
    {
        return type;
    }

    /**
     * 
     * @return the value
     */
    @CheckReturnValue
    public int getNumber()
    {
        return number;
    }

    /**
     * 
     * @throws java.sql.SQLException if a database-access error occurs.
     * @return the value
     */
    @CheckReturnValue
    public int getDisplaySize() throws SQLException
    {
        if( displaySize < 0)
        {
            displaySize = name.length();

            int currentRow = rs.getRow();
            rs.rewind();

            while( rs.next())
            {
                String temp = rs.getString( name);

                int len = temp == null ? 0 : temp.length();

                if( len > displaySize)
                {
                    displaySize = len;
                }
            }

            rs.setCurrentRow( currentRow);
        }

        return displaySize;
    }

    private final String      name;

    private final int         number;

    private int         type,//NOPMD

                        displaySize;

    private final SResultSet  rs;//NOPMD
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.database.internal.Column");//#LOGGER-NOPMD
}
