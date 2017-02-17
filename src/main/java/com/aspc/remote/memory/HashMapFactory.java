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
package com.aspc.remote.memory;

import com.aspc.developer.ThreadCop;
import com.aspc.developer.wrapper.HashMapTC;
import java.util.HashMap;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 *  Hash Map Factory
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       11 September 2012
 */
@SuppressWarnings({"NestedAssignment", "AssertWithSideEffects"})
public class HashMapFactory
{
    private static final boolean ENABLE_THREAD_COP;
    private static final boolean DEBUG_HASH_MAPS=false;//#RELEASE
    private HashMapFactory()
    {

    }

    /**
     *
     * @return the hash map
     */
    @CheckReturnValue @Nonnull
    public static HashMap create()
    {
        if( ENABLE_THREAD_COP)
        {
            return create( ThreadCop.MODE.ACCESS_ONLY_BY_CREATING_THREAD, 16);
        }
        else
        {
            return new HashMap();
        }
    }

    /**
     *
     * @param mode the mode
     * @return the hash map
     */
    @CheckReturnValue @Nonnull
    public static HashMap create(final ThreadCop.MODE mode)
    {
        if( ENABLE_THREAD_COP)
        {
            return create( mode, 16);
        }
        else
        {
            return new HashMap();
        }
    }

    /**
     *
     * @param mode the mode
     * @param expectedSize how many records will be stored.
     * @return the hash map
     */
    @CheckReturnValue @Nonnull
    public static HashMap create(final @Nonnull ThreadCop.MODE mode, final @Nonnegative int expectedSize)
    {
        assert expectedSize>=0;
        int capacity =(int) (expectedSize / 0.75) + 1;
        if( ENABLE_THREAD_COP)
        {
            HashMapTC h= new HashMapTC( capacity);
            ThreadCop.monitor(h, mode);

            return h;
        }
        else
        {
            return new HashMap(capacity);
        }
    }

    static
    {
        boolean flag=false;
        assert flag=true;
            
        ENABLE_THREAD_COP=flag&&DEBUG_HASH_MAPS;
    }
}
