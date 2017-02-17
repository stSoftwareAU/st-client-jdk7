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
package com.aspc.remote.soap.internal;

import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.soap.Client;
import org.apache.commons.pool.PoolableObjectFactory;

/**
 *  ClientFactory provides a poolable object factory that can be used with ExecutorPool.
 *  Use this class when creating a remote TaskManager application
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Luke
 *  @since       26 September 2005
 */
public class ClientFactory implements PoolableObjectFactory
{
    private final String hosts;
    /**
     * Creates a new ClientFactory
     * @param hosts the hosts string used to construct a Client
     */
    public ClientFactory( final String hosts )
    {
        this.hosts = hosts;
    }
    
    /**
     * Makes a new Client
     * @return Object the new Client
     * @throws Exception A serious problem
     */
    @Override
    public Object makeObject( ) throws Exception
    {
        Client c = new Client( hosts );
        c.login();
        return c;
    }
    
    /**
     * Activates a client.
     * @param obj the client to activate
     * @throws Exception a serious problem
     */
    @SuppressWarnings("empty-statement")
    @Override
    public void activateObject( Object obj ) throws Exception
    {
        ; /* nothing to do here */
    }
    
    /**
     * Passivate a client when it is returned to the pool. nothing to be done.
     * @param obj the Client to passivate
     * @throws Exception a serious problem 
     */
    @SuppressWarnings("empty-statement")
    @Override
    public void passivateObject( Object obj ) throws Exception
    {
        ; /* nothing to do here */
    }
    
    /**
     * Validates that a Client is in a useable state
     * @param obj the Client to validate
     * @return boolean true if the Client is in a useable state
     */
    @Override
    public boolean validateObject( Object obj )
    {
        Client client = (Client)obj; /* the client is valid if it has transports */
        return ( client.getTransportCount() > 0 );
    }
    
    /**
     * Destroy a Client object. Attempt to logout
     * @param obj the Client to destroy
     */
    @Override
    public void destroyObject( Object obj )
    {
        Client c = (Client)obj;
        try
        {
            c.logout();
        }
        catch( Exception ignore )
        {
            /*
             * We are only trying to logout for completeness, we do not really
             * care weather it fails or not.
             */
            LOGGER.debug( "Logging out Client", ignore);
        }
    }
    
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.soap.internal.ClientFactory");//#LOGGER-NOPMD
}
