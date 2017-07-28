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
package com.aspc.remote.util.net;

import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.StringUtilities;
import com.aspc.remote.util.net.internal.ServerClientPoolImpl;
import com.jcraft.jsch.JSchException;
import java.util.WeakHashMap;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import org.apache.commons.logging.Log;
import org.apache.commons.pool.PoolableObjectFactory;

/**
 * Manages NetClient objects for multiple protocols
 *
 * @author Nigel Leck
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 * @since       February 17, 2006
 */
@SuppressWarnings("AssertWithSideEffects")
public class NetClientFactory implements PoolableObjectFactory
{
    private final WeakHashMap clientPWD = new WeakHashMap();
//    private static final boolean ASSERT;
    
    /**
     * Activates the given client
     * @param client the client to activate
     */
    @Override
    public void activateObject( final @Nonnull Object client )
    {
        NetClient nc = (NetClient)client;
        
        nc.activate();
        
        try
        {
            String dir = (String)clientPWD.get( nc );

            if( dir == null )
            {
                throw new Exception( "could not find a directory for the client" );
            }
            
            if( ! nc.changeDirectory( dir, false ) )
            {
                throw new Exception( "client could not change directory to "+dir );
            }
        }
        catch( Exception e )
        {
            LOGGER.warn(
                "failure to activate a NetClient for key "+
                StringUtilities.stripPasswordFromURL(nc.toString()) +
                "\nthe connection might have timed out",
                e
            );
            
            nc.destroy();
        }
    }
    
    /**
     * Destroys a client
     * @param client the client to destroy
     */
    @Override
    public void destroyObject( final @Nonnull Object client )
    {
        NetClient nc = (NetClient) client;
        
        nc.destroy();
    }
    
    /**
     * Makes a client
     * @return Object the client
     * @throws Exception failure to make a client
     */
    @Override @CheckReturnValue @Nonnull
    public Object makeObject( ) throws Exception
    {
        Object key = ServerClientPoolImpl.currentKey();
        
        NetClient nc = null;
        String url = key.toString();
        
        if( url.startsWith( NetClient.PREFIX_FTP ) )
        {
            nc = new NetClientFtp();
            nc.make( url );
        }
        else if( url.startsWith( NetClient.PREFIX_SFTP ) )
        {
            try{
                nc = new NetClientSftp();
                nc.make( url );
            }
            catch( JSchException e)
            {
                if( nc!=null)
                {
                    nc.destroy();
                }
                long pauseMS=(long) (1000* Math.random());
                LOGGER.warn( "Could not connect ( retrying in " + pauseMS + ") " + StringUtilities.stripPasswordFromURL(url), e);
                Thread.sleep(pauseMS);
                nc = new NetClientSftp();
                try{
                    nc.make( url );
                }
                catch( JSchException e2)
                {
                    String msg;
//                    if( ASSERT)
//                    {
//                        msg="Could not connect to " + url;
//                    }
//                    else
//                    {
                        msg="Could not connect to " + StringUtilities.stripPasswordFromURL(url);
//                    }
                    throw new Exception( msg, e2);
                }
            }
        }
        else if( url.startsWith( NetClient.PREFIX_FILE ) )
        {
            nc = new NetClientFile();
            nc.make( url );
        }
        else
        {
            throw new Exception( "unknown protocol, cannot make NetClient: "+ StringUtilities.stripPasswordFromURL(url) );
        }
        
        String dir = nc.getDirectory();
        clientPWD.put( nc, dir );
        
        return nc;
    }
    
    /**
     * Validates a client
     * @param client the client to validate
     * @return boolean true if the client is valid
     */
    @Override @CheckReturnValue
    public boolean validateObject( final @Nonnull Object client )
    {
        NetClient nc = (NetClient)client;
        
        return nc.validate();
    }
    
    /**
     * Passivates a client, the implementation does nothing, handled through activate
     * @param client the client to passivate
     */
    @SuppressWarnings("empty-statement")
    @Override
    public void passivateObject( final @Nonnull Object client )
    {
        // handled through activate.
    }
//    
//    static
//    {
//        boolean flag=false;
//        assert flag=true;
//        ASSERT=flag;
//    }
    
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.net.NetClientFactory");//#LOGGER-NOPMD
}
