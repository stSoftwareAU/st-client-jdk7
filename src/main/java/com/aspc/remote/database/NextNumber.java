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

import com.aspc.developer.ThreadCop;
import java.util.*;
import com.aspc.remote.database.internal.*;
import com.aspc.remote.memory.HashMapFactory;
import com.aspc.remote.util.misc.*;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import org.apache.commons.logging.Log;

/**
 *  NextNumber.
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED </i>
 *
 *  @author      Nigel Leck
 *  @since       October 2, 1998, 4:54 PM
 */
public final class NextNumber
{
    /**
     * The next number function
     */
    public static final String NEXT_NUMBER_FUNCTION= "NextNumberV11";

    private static final HashMap DB_MAP  = HashMapFactory.create();

    private NextNumber()
    {
    }

    /**
     * The next number of this type.
     * @param id The next number code
     * @return The next number
     */
    @CheckReturnValue
    public static long get( final @Nonnull String id)
    {
        DataBase dBase = DataBase.getCurrent();
        assert dBase !=null;
        return get( id, dBase);
    }

    /**
     * The next number of this type.
     * @param id The next number code
     * @param dBase The database to use.
     * @return The next number
     */
    @CheckReturnValue
    public static long get( final @Nonnull String id, final @Nonnull DataBase dBase)
    {
        return get( id, dBase, 10);
    }

    /**
     * The next number of this type.
     * @param code The next number code
     * @param dBase The database to use.
     * @param cacheSize The cache size must be positive or disabled by zero, default if -1
     * @return The next number
     */
    @SuppressWarnings("SleepWhileInLoop")
    @CheckReturnValue
    public static long get( final @Nonnull String code, final @Nonnull DataBase dBase, final int cacheSize)
    {
        assert cacheSize>=-1: "cacheSize must be positive or disabled by zero, default if -1 was: " + cacheSize;
        HashMap numbers;
        synchronized( DB_MAP)
        {
            numbers = (HashMap)DB_MAP.get( dBase);

            if( numbers == null)
            {
                numbers = HashMapFactory.create();
                assert ThreadCop.monitor(numbers, ThreadCop.MODE.EXTERNAL_SYNCHRONIZED);
                DB_MAP.put( dBase, numbers);
            }
        }

        for( int loop = 0; true; loop++)
        {
            try
            {
                synchronized( numbers)
                {
                    StoredValue sv;
                    sv = (StoredValue)numbers.get( code);

                    if( sv == null)
                    {
                        String key = code.toUpperCase().trim();
                        sv = (StoredValue)numbers.get( key);

                        if( sv == null)
                        {
                            sv = new StoredValue( key);
                            numbers.put( key, sv);
                        }
                    }

                    if( sv.hasMoreNumbers())
                    {
                        return sv.getNextNumber();
                    }

                    NextRunner nr = new NextRunner( dBase, cacheSize, sv);

                    return nr.createMore();
                }
            }
            catch( NextNumberException e)
            {
                String msg = "NextNumber.get(" + code + "," + dBase + "," + cacheSize + ")";
                LOGGER.warn( msg, e);

                if( loop > 10)
                {
                    throw new DataBaseError( msg, e);
                }
                try
                {
                    Thread.sleep( (long)(1000 * Math.random()));
                }
                catch( InterruptedException ie)
                {
                    throw new DataBaseError( "NextNumber.get(" + code + "," + dBase + "," + cacheSize + ") interrupted", ie);
                }
            }
            catch( Exception e)
            {
                throw new DataBaseError( "NextNumber.get(" + code + "," + dBase + "," + cacheSize + ")", e);
            }
        }
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.database.NextNumber");//#LOGGER-NOPMD

    static
    {
        assert ThreadCop.monitor(DB_MAP, ThreadCop.MODE.EXTERNAL_SYNCHRONIZED);
    }
}
