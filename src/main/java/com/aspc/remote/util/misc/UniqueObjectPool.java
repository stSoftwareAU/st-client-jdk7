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
package com.aspc.remote.util.misc;

import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import org.apache.commons.logging.Log;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 *  NetUtil
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       August 9, 1999, 5:21 PM
 */
public class UniqueObjectPool
{
    private final WeakHashMap objectToPool = new WeakHashMap();
    /**
     * the object pool factory
     */
    protected final PoolableObjectFactory factory;
    private final ConcurrentHashMap<Object, ObjectPool> pools= new ConcurrentHashMap();
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.UniqueObjectPool");//#LOGGER-NOPMD

    private static final ThreadLocal CURRENT_KEY=new ThreadLocal();

    private static final ThreadLocal REQUEST_KEY = new ThreadLocal();

    /**
     * separate pool per key. There will be no blocking between pools.
     * @param factory The factory to create the pools with
     */
    public UniqueObjectPool( final PoolableObjectFactory factory)
    {
        this.factory = factory;
    }

    /**
     * What is the current key that we are creating an object for.
     * @return The current key
     */
    public static Object currentKey()
    {
        return CURRENT_KEY.get();
    }

    /**
     * What is the borrow key that request an object for.
     * @return The borrow key
     */
    public static Object requestKey()
    {
        return REQUEST_KEY.get();
    }

    /**
     * pool size
     * @return int size
     */
    public int size()
    {
        return pools.size();
    }

    /**
     * borrow an object
     * @param key the key to borrow.
     * @return the object that was borrowed.
     * @throws Exception a serious problem
     */
    public Object borrowObject( final Object key) throws Exception
    {
        ObjectPool pool = null;

        try
        {
            REQUEST_KEY.set(key);
            pool = getPool( key);
        }
        finally
        {
            REQUEST_KEY.set(null);
        }
        assert pool!=null;
        Object obj;
        try
        {
            CURRENT_KEY.set( key);

            obj = pool.borrowObject();
        }
        finally
        {
            CURRENT_KEY.set(null);
        }

        synchronized( objectToPool)
        {
            objectToPool.put( obj, pool);
        }

        return obj;
    }

    /**
     * return an object back into the pool.
     * @param borrowedObject the object to be returned
     * @throws Exception a serious problem
     */
    public void returnObject( final Object borrowedObject) throws Exception
    {
        if( borrowedObject == null) return;

        ObjectPool pool;

        synchronized( objectToPool)
        {
            pool = (ObjectPool)objectToPool.remove(borrowedObject);
        }

        if( pool == null)
        {
            throw new Exception( borrowedObject + " never borrowed");
        }

        pool.returnObject( borrowedObject);
    }

    /**
     * Invalidate this object
     * @param borrowedObject the object that will be invalidated.
     * @throws Exception a serious problem
     */
    public void invalidateObject( final Object borrowedObject) throws Exception
    {
        if( borrowedObject == null) return;

        ObjectPool pool;

        synchronized( objectToPool)
        {
            pool = (ObjectPool)objectToPool.get( borrowedObject);
        }

        if( pool == null)
        {
            throw new Exception( borrowedObject + " never borrowed");
        }

        pool.invalidateObject( borrowedObject);
    }

    /**
     * make a new pool
     * @throws Exception a serious problem
     * @return the new object pool
     */
    protected ObjectPool makePool( ) throws Exception
    {
        return new GenericObjectPool( factory);
    }

    @Nonnull
    private ObjectPool getPool( final Object key) throws Exception
    {
        ObjectPool pool;

        boolean created = false;
        pool = pools.get( key);

        if( pool == null)
        {
            pool = makePool();
            ObjectPool currentPool = pools.putIfAbsent(key, pool);
            if( currentPool == null)
            {
                created=true;
            }
            else
            {
                pool=currentPool;
            }
        }

        if( created)
        {
            LOGGER.info( "Created pool for " + StringUtilities.stripPasswordFromURL( key.toString()));
        }

        return pool;
    }

    /**
     * The number of objects that are currently checked out.
     * @return the borrowed object count.
     */
    public int borrowedObjectsCount()
    {        
        synchronized( objectToPool)
        {
            return objectToPool.size();
        }
    }
    
    /**
     * Close this pool and free up resource.
     */
    public void close()
    {
        LOGGER.info("closing the pool start");
        //-- close all the Object pool in the pools HashMap
        ArrayList<ObjectPool>listToClose=new ArrayList();
        
        for (Map.Entry e : pools.entrySet()) 
        {
            listToClose.add((ObjectPool)e.getValue());
        }
        
        synchronized( objectToPool)
        {
            //--- clear objectToPool WeakHashMap
            objectToPool.clear();
            pools.clear();
        }
        
        for( ObjectPool op: listToClose)
        {
            try
            {
                op.close();
            }
            catch (Exception e1)
            {
                LOGGER.warn("closing " + op, e1);
            }
        }
        
        LOGGER.info("closing the pool finish");
    }
}
