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

import com.aspc.remote.util.misc.CLogger;
import javax.annotation.Nullable;
import org.apache.commons.logging.Log;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;

/**
 * ExecutorPool provides pooling and life cycle management via a factory for
 * Executor objects.
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED pool</i>
 *
 * @author      luke
 * @since       27 September 2005
 */
public class ExecutorPool
{
    private final GenericObjectPool pool;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.jdbc.ExecutorPool");//#LOGGER-NOPMD

    /**
     * Creates a new ExecutorPool using the given factory
     * @param factory the factory for managing Executor objects
     */
    public ExecutorPool( final PoolableObjectFactory factory )
    {
        pool = new GenericObjectPool( factory );
    }

    /**
     * Borrows an Executor object from the pool
     * @return Executor the borrowed Executor
     * @throws Exception a serious problem
     */
    public Executor borrow() throws Exception
    {
        return (Executor)pool.borrowObject();
    }

    /**
     * Invalidates the borrowed executor
     * @param executor the borrowed executor
     * @throws Exception a serious problem
     */
    public void invalidate( final @Nullable Executor executor ) throws Exception
    {
        pool.invalidateObject( executor );
    }

    /**
     * Returns the borrowed executor to the pool.
     * @param executor the borrowed Executor to return
     */
    public void release( final @Nullable Executor executor )
    {
        if( executor == null) return;
        try
        {
            pool.returnObject( executor );
        }
        catch( Exception ignore )
        {
            LOGGER.warn( "could not return " + executor, ignore);
        }
    }
}
