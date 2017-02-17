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
package com.aspc.remote.database;

import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.CLogger;
import java.sql.ResultSetMetaData;


/**
 *  Column Information
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       10 December 2009
 */
public final class ColumnInfo
{
    /** the name */
    public final String name;
    /** the type name */
    public final String typeName;
    /** the name */
    public final int type;

    /** the size */
    public final int size;
    /** the  precision */
    public final int precision;
    /** is nullable */
    public final boolean nullable;
    /** the scale */
    public final int scale;

    /**
     * The index info
     * @param name the name
     * @param typeName the type name
     * @param size the size
     * @param precision the precision
     * @param nullable is nullable
     * @param scale the scale
     * @param type the type
     */
    public ColumnInfo( final String name, final String typeName, final int type, final int size, final int precision, final int nullable, final int scale)
    {
        this.name=name;
        this.type=type;
        this.typeName=typeName;
        this.size=size;
        this.precision=precision;
        this.nullable=nullable != ResultSetMetaData.columnNoNulls;
        this.scale=scale;
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.database.ColumnInfo");//#LOGGER-NOPMD
}
