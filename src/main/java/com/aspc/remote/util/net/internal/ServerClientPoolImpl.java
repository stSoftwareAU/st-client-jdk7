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
package com.aspc.remote.util.net.internal;

import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.CProperties;
import com.aspc.remote.util.misc.StringUtilities;
import com.aspc.remote.util.misc.UniqueObjectPool;
import com.aspc.remote.util.net.NetClientFactory;
import com.aspc.remote.util.net.NetClientPool;
import com.aspc.remote.util.net.NetUtil;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 *  FTP pooling
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author Nigel Leck
 *  @since       August 9, 1999, 5:21 PM
 */
public final class ServerClientPoolImpl extends UniqueObjectPool implements NetClientPool
{    
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.net.internal.ServerClientPoolImpl");//#LOGGER-NOPMD
    
    /**
     * Special handler for applets ( no pooling)
     */
    public ServerClientPoolImpl()
    {
        super( new NetClientFactory());
    }
    
    /**
     * make the connection pool.
     * @throws Exception a serious problem.
     * @return the new connection pool 
     */
    @Override
    protected ObjectPool makePool( ) throws Exception
    {
        GenericObjectPool pool = new GenericObjectPool( factory);
        String temp;
        
        int clientMaxActive = 100;
        try
        {
            temp = CProperties.getProperty(NetUtil.NETCLIENT_MAX_ACTIVE);
            if( StringUtilities.isBlank(temp) == false) 
            {
                clientMaxActive = Integer.parseInt(temp);
            }
        }
        catch( NumberFormatException nf)
        {
            LOGGER.warn( "Couldn't set " + NetUtil.NETCLIENT_MAX_ACTIVE, nf);
        }

        pool.setMaxActive( clientMaxActive);
        
        pool.setMaxWait( NetUtil.getMaxWaitTime()); // Block for 2 minutes to get a new FTP connection.

        long clientTestIdle = 5 * 60 * 1000;
        try
        {
            temp = CProperties.getProperty(NetUtil.NETCLIENT_TEST_IDLE_MS);
            if( StringUtilities.isBlank(temp) == false) 
            {
                clientTestIdle = Long.parseLong(temp);
            }
        }
        catch( NumberFormatException nf)
        {
            LOGGER.warn( "Couldn't set " + NetUtil.NETCLIENT_TEST_IDLE_MS, nf);
        }

        pool.setTimeBetweenEvictionRunsMillis(clientTestIdle);
        
        pool.setTestOnBorrow( true);
        pool.setTestWhileIdle( false);
        pool.setTestOnReturn( false);
        
        return pool;
    }

}
