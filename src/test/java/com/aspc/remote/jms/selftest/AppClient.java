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
package com.aspc.remote.jms.selftest;

import org.apache.commons.logging.Log;
import com.aspc.remote.jms.*;
import com.aspc.remote.util.misc.*;

/**
 *  Marvin's meaning of life is to find and eradicate bad data.
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED command line application</i>
 *
 *  @author      Jason McGath
 *  @since       8 July 2002
 */
public class AppClient extends Client
{

    /**
     * the provider URL
     * @return the URL
     */
    @Override
    public String getProviderUrl() 
    {
        return "tcp://eqrap7u:1099";
        //return "tcp://eqrap4:61616";
    }
    
    /**
     * the factory
     * @return the factory
     */
    @Override
    public String getContextFactory() 
    {
        return "org.activemq.jndi.ActiveMQInitialContextFactory";
    }
    
    /**
     * The main for the program
     * @param argv the args
     */
    public static void main(String argv[])
    {
        Client client = new AppClient();
     
        try
        {
            client.connect();
            //client.send( "hello");
        }
        catch( Exception e)
        {
            LOGGER.error( "JMS", e);
        }
        for( int i = 0; i < 2000; i++)
        {
            LOGGER.info( "connect: " + i);
            try
            {
                Thread.sleep( 1000);
                //client = new AppClient();
                //client.shutdownJMS();
                //Thread.sleep( 10000);
                //client.connect();
                //client.send( "hello");
                //client.shutdownJMS();
            }
            catch( Exception e)
            {
                LOGGER.error( "JMS", e);
                //break;
            }
        }
        
        LOGGER.info( "completed");
        System.exit(0);
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.jms.selftest.AppClient");//#LOGGER-NOPMD
}
