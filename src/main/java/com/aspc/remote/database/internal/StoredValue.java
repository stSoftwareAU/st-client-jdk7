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
import com.aspc.remote.database.*;

/**
 *  StoredValue.java
 *
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       December 5, 2000, 11:31 AM
 */
public class StoredValue
{
    /**
     * Stored value for a id
     * @param id The code
     */
    public StoredValue( String id)
    {
        this.id = id;
    }

    /**
     * returns the code of this stored value
     * @return The code
     */
    public String getID()
    {
        return id;
    }

    /**
     * When was this stored value last incremented
     * @return The last incremented time in ms
     */
    public long getLastIncremented()
    {
        return lastIncremented;
    }

    /**
     * Set the next number
     * @param inNr The next number
     * @param count The cache size
     */
    public synchronized void setNumber( long inNr, int count)
    {
        if( inNr < startNr)
        {
            throw new NextNumberException(
                "Trying to set the start number (" + inNr +
                ") to be less than current (" + startNr + ")"
            );
        }

        this.startNr = inNr;
        this.count = count;
        lastIncremented = System.currentTimeMillis();
    }

    /**
     * Do we have more number in the cache ?
     * @return true if we have more numbers.
     */
    public synchronized boolean hasMoreNumbers()
    {
        if( count > 0)
        {
            return true;
        }
        return false;
    }

    /**
     * The next number
     * @return The next number.
     */
    public synchronized long getNextNumber()
    {
        if( hasMoreNumbers() == false)
        {
            throw new DataBaseError( "No more numbers");
        }

        long value;

        value = startNr;

        startNr++;
        count--;

        return value;
    }

    /** When did we last hit the DB ? */
    private long lastIncremented;

    private final String id;
    private long startNr;
    private int  count;
}
