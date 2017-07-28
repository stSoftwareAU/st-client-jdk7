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
package com.aspc.remote.jdbc;

import com.aspc.remote.jdbc.impl.ExecutorConnection;
import com.aspc.remote.soap.Client;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.StringUtilities;
import java.sql.SQLException;
import org.apache.commons.logging.Log;

/**
 * Remote Server database connection.
 * Implements a database connection through SOAP.
 *
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 * @author  Nigel Leck
 * @since 29 September 2006
 */
public class SoapConnection extends ExecutorConnection
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.jdbc.SoapConnection");//#LOGGER-NOPMD
    private Client remoteClient;

    /**
     * JDBC via SOAP
     *
     * @param url the URL
     * @param user the user
     * @param pass the password
     * @throws Exception a serious problem
     */
    public SoapConnection(final String url, final String user, final String pass) throws Exception
    {
        String host;
        int pos = url.indexOf( "/");
        String layer = null;

        if( pos != -1)
        {
            host = url.substring(0, pos);
            layer = url.substring( pos +1);
        }
        else
        {
            host = url;
        }

        remoteClient = new Client( host);

        pos = user.indexOf( "@");
        String login = user;
        if( pos != -1)
        {
            layer = user.substring( pos +1);
            login = user.substring( 0, pos);
        }

        remoteClient.login( login, pass, layer);
        executor= remoteClient;
    }

    /**
     * JDBC via SOAP
     *
     * @param soapClient the soap client
     */
    public SoapConnection(Client soapClient)
    {
        super( soapClient);
        remoteClient=soapClient;
    }


    /** {@inheritDoc} */
    @Override
    public void close() throws SQLException
    {
        try
        {
            remoteClient.logout();
        }
        catch( Exception e)
        {
            String msg = e.getMessage();
            if( StringUtilities.isBlank(msg))
            {
                msg = e.toString();
            }
            throw new SoapSQLException( msg, e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isClosed() throws SQLException
    {
        if( remoteClient == null) return true;
        if( remoteClient.currentTransport() == null) return true;
        if( remoteClient.getUserName() == null) return true;
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void setCatalog(String catalog) throws SQLException
    {
        remoteClient.setLayer(catalog);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getCatalog() throws SQLException
    {
        return remoteClient.getLayer();
    }
}
