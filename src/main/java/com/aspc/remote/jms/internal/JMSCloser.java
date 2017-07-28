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
package com.aspc.remote.jms.internal;

import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.ThreadUtil;
import javax.jms.TopicConnection;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

/**
 *  Close the JMS connection in a back ground thread so it can't be blocked. 
 *
 * @author Nigel Leck
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @since       August 13, 2004, 4:20 PM
 */
public final class JMSCloser extends Thread
{
    /**
     * constructor
     *
     * @param publisher The JMS publisher.
     * @param subscriber The JMS subscriber.
     * @param session The JMS session.
     * @param connection The JMS connection.
     */    
    public JMSCloser(TopicPublisher publisher,  TopicSubscriber subscriber, TopicSession session, TopicConnection connection)
    {
        this.publisher  = publisher;
        this.subscriber = subscriber;
        this.session    = session;
        this.connection = connection;
        
        setName( "JMS close connection");
        setDaemon( true);
        start();
    }

    /**
     * wait for up to 60 seconds for the connection to close.
     */    
    public void check( )
    {
        try
        {
            join( 60 * 1000);
        }
        catch( InterruptedException ie)
        {
            LOGGER.warn( "interrupted during close", ie);
        }
        
        if( ThreadUtil.isAliveOrStarting(this))
        {
            // don't interrupt as it will close the socket
            //interrupt();
            
            LOGGER.fatal( "Could not close JMS connection");
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
        // Close subscriber handle.
        if( subscriber != null)
        {
            try
            {
                subscriber.setMessageListener(null);
                
                subscriber.close();                
            }
            catch( Throwable jmsException)
            {
                LOGGER.warn( "shutdown JMS subscriber", jmsException);
            }
        }

        if( publisher != null)
        {
            try
            {
                publisher.close();
            }
            catch( Throwable jmsException)
            {
                LOGGER.warn( "shutdown JMS publisher", jmsException);
            }
        }

        if( session != null)
        {
            try
            {
                session.close();
            }
            catch( Throwable jmsException)
            {
                LOGGER.warn( "shutdown JMS session", jmsException);
            }
        }

        if( connection != null)
        {
            try
            {
                connection.close();
            }
            catch( Throwable jmsException)
            {
                LOGGER.warn( "shutdown JMS connection", jmsException);
            }
        }
    }

    private final TopicPublisher publisher;
    private final TopicSubscriber subscriber;
    private final TopicSession session;
    private final TopicConnection connection;
    
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.jms.internal.JMSCloser");//#LOGGER-NOPMD
}
