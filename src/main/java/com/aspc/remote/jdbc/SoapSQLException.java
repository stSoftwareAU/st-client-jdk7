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

import java.sql.SQLException;

/**
 * Soap SQL exception
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *  @author      Nigel Leck
 *  @since       11 February 2008
 */
public class SoapSQLException extends SQLException
{
    /**
     * Constructs an <code>SQLException</code> object with a reason;
     * the <code>SQLState</code> field defaults to <code>null</code>, and
     * the <code>vendorCode</code> field defaults to 0.
     *
     * @param reason a description of the exception
     */
    public SoapSQLException(String reason)
    {
        super(reason);

    }

    /**
     * Constructs an <code>SQLException</code> object with a reason;
     * the <code>SQLState</code> field defaults to <code>null</code>, and
     * the <code>vendorCode</code> field defaults to 0.
     *
     * @param reason a description of the exception
     * @param cause the cause
     */
    public SoapSQLException(final String reason, final Throwable cause)
    {
        super(reason, cause);

    }
}
