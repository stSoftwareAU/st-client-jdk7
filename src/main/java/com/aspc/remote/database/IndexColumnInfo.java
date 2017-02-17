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

/**
 *  Column Information
 *
 *  <i>THREAD MODE: READONLY</i>
 *
 *  @author      Nigel Leck
 *  @since       10 December 2009
 */
public final class IndexColumnInfo
{
    /** the orders */
    public enum ORDER
    {
        /** assending */
        ASSENDING,
        /** descending */
        DESCENDING
    };

    /** the name */
    public final String name;

    /** the sequence number */
    public final int seq;

    /** the order */
    public final ORDER order;

    /**
     * The index column info
     * @param name the name
     * @param seq the sequence number
     * @param order the order
     */
    public IndexColumnInfo( final String name, final int seq, final ORDER order)
    {
        this.name=name.toLowerCase();
        this.seq=seq;
        this.order=order;
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.database.IndexColumnInfo");//#LOGGER-NOPMD
}
