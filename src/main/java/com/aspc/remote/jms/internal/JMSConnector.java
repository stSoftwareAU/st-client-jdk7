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
package com.aspc.remote.jms.internal;

import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.jms.*;
import java.util.*;

/**
 *  re-try openning the JMS connecion if we failed to connect previously. 
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       August 13, 2004, 4:20 PM
 */
public final class JMSConnector extends Thread
{
    private static  JMSConnector current = null;//MT CHECKED
    private static final WeakHashMap clients = new WeakHashMap();
    
    /**
     * constructor
     * @param client The client to reconect.
     */    
    private JMSConnector()
    {
        
        setName( "JMS connector");
        setDaemon( true);
        
        start();
    }

    /**
     * remove the client
     * @param client the client
     */
    public static void remove( final Client client)
    {
        synchronized( clients)
        {
            clients.remove( client);
        }        
    }
    
    /**
     * retry
     * @param client the client to retry
     */
    public static synchronized void retry( Client client)
    {
        if( current == null )
        {
            current = new JMSConnector();
        }
        
        synchronized( clients)
        {
            clients.put( client, "");
            clients.notifyAll();
        }
    }
    
    /**
     * actually do the closing of the connection.
     *
     * @see java.lang.Runnable#run()
     */    
    @Override
    public void run()
    {
        while( true)
        {
            try
            {
                Object keys[] = null;
                while( keys == null)
                {
                    synchronized( clients)
                    {
                        if( clients.isEmpty()) clients.wait();
                        keys = clients.keySet().toArray();
                    }
                }
                
                for (Object key : keys) {
                    Client client = (Client) key;
                    if( client != null)
                    {
                        try
                        {
                            if( client.isConnected() == false && client.isEnabled())
                            {
                                client.connect();
                            }
                            
                            if( client.isConnected()  || client.isEnabled() == false)
                            {
                                synchronized( clients)
                                {
                                    clients.remove( client);
                                }
                            }
                        }
                        catch( Exception e)
                        {
                            LOGGER.fatal( "Could not connect to " + client, e);
                        }
                    }
                }
            }
            catch( Exception e)
            {
                LOGGER.info( "failed to connect", e);
            }
        }
    }
        
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.jms.internal.JMSConnector");//#LOGGER-NOPMD
}
