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

import com.aspc.remote.util.misc.CLogger;
import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;

/**
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 * @author Nigel Leck
 * @since 29 September 2006
 */
public class Driver implements java.sql.Driver
{
    /** the signature */
    public static final String URL_PREFIX = "jdbc:aspc:";
    private static final String SIGNATURE       = "SOAP:";

    /**
     * Try to make a database connection to the given URL.
     * The driver should return "null" if it realizes it is the wrong kind
     * of driver to connect to the given URL.  This will be common, as when
     * the JDBC driver manager is asked to connect to a given URL it passes
     * the URL to each loaded driver in turn.
     *
     * <P>The driver should raise a SQLException if it is the right
     * driver to connect to the given URL, but has trouble connecting to
     * the database.
     *
     * <P>The java.util.Properties argument can be used to passed arbitrary
     * string tag/value pairs as connection arguments.
     * Normally at least "user" and "password" properties should be
     * included in the Properties.
     *
     * @param url The URL of the database to connect to
     * @param info a list of arbitrary string tag/value pairs as
     * connection arguments; normally at least a "user" and
     * "password" property should be included
     * @return a Connection to the URL
     * @exception SQLException if a database-access error occurs.
     */
    @Override
    public Connection connect(
        final String url,
        final java.util.Properties info
    )   throws SQLException
    {
        LOGGER.info(
            "Driver: " + url
        );

        String tempURL = url;

        if( tempURL.startsWith(URL_PREFIX))
        {
            tempURL = tempURL.substring(URL_PREFIX.length());
        }
        else
        {
            if( url.startsWith("jdbc:"))
            {
                tempURL = tempURL.substring(5);
            }
            if( tempURL.startsWith( SIGNATURE) == false)
            {
                return null;
            }

            tempURL = tempURL.substring( SIGNATURE.length());
        }

        while( tempURL.startsWith("/"))
        {
            tempURL = tempURL.substring(1);
        }
        String u = null;
        try
        {
            String p;

            u = info.getProperty( "user", "guest");
            p = info.getProperty( "password", "guest");

            return new SoapConnection( tempURL, u, p);
        }
        catch( Throwable e)
        {
            String msg = "Connect error: " + url + " user: " + u + " caused: " + e.getMessage();
            LOGGER.error( msg, e);
            throw new SoapSQLException( msg);
        }
    }

    /**
     * Returns true if the driver thinks that it can open a connection
     * to the given URL.  Typically drivers will return true if they
     * understand the subprotocol specified in the URL and false if
     * they don't.
     *
     * @param url The URL of the database.
     * @return True if this driver can connect to the given URL.
     * @exception SQLException if a database-access error occurs.
     */
    @Override
    public boolean acceptsURL(String url) throws SQLException
    {
        LOGGER.debug( "acceptsURL '" + url + "'");

        if( url.startsWith( SIGNATURE) == true)
        {
            return true;
        }

        if( url.startsWith(URL_PREFIX)) return true;

        return false;
    }

    /*private static final Object[][] knownProperties = {
        { "DBNAME", Boolean.TRUE,"Database name to connect to; may be specified directly in the JDBC URL." },
        { "user", Boolean.TRUE,"Username to connect to the database as.", null },
        //{ "PGHOST", Boolean.FALSE,"Hostname of the PostgreSQL server; may be specified directly in the JDBC URL." },
        //{ "PGPORT", Boolean.FALSE,"Port number to connect to the PostgreSQL server on; may be specified directly in the JDBC URL.", },
        //{ "password", Boolean.FALSE,"Password to use when authenticating.", },
        //{ "protocolVersion", Boolean.FALSE,"Force use of a particular protocol version when connecting; if set, disables protocol version fallback.", },
        { "ssl", Boolean.FALSE,"Control use of SSL; any nonnull value causes SSL to be required." },
        //{ "sslfactory", Boolean.FALSE,"Provide a SSLSocketFactory class when using SSL." },
        //{ "sslfactoryarg", Boolean.FALSE,"Argument forwarded to constructor of SSLSocketFactory class." },
        //{ "loglevel", Boolean.FALSE,"Control the driver's log verbosity: 0 is off, 1 is INFO, 2 is DEBUG.",new String[] { "0", "1", "2" } },
        //{ "allowEncodingChanges", Boolean.FALSE,"Allow the user to change the client_encoding variable." },
        //{ "logUnclosedConnections", Boolean.FALSE,"When connections that are not explicitly closed are garbage collected,
     //log the stacktrace from the opening of the connection to trace the leak source."},
        //{ "prepareThreshold", Boolean.FALSE,"Default statement prepare threshold (numeric)." },
        //{ "charSet", Boolean.FALSE,"When connecting to a pre-7.3 server, the database encoding to assume is in use." },
        //{ "compatible", Boolean.FALSE,"Force compatibility of some features with an older version of the driver.",new String[] { "7.1", "7.2", "7.3", "7.4", "8.0", "8.1", "8.2" } },
        //{ "loginTimeout", Boolean.FALSE,"The login timeout, in seconds; 0 means no timeout beyond the normal TCP connection timout." },
        //{ "stringtype", Boolean.FALSE,"The type to bind String parameters as (usually 'varchar'; 'unspecified' allows implicit casting to other types)",new String[] { "varchar", "unspecified" } },
    };*/

    /** {@inheritDoc} */
    @Override
    public DriverPropertyInfo[] getPropertyInfo(final String url, java.util.Properties info)
                         throws SQLException
    {

        DriverPropertyInfo[] props = new DriverPropertyInfo[0];

        return props;
    }


    /** {@inheritDoc} */
    @Override
    public int getMajorVersion()
    {
        return 1;
    }

    /** {@inheritDoc} */
    @Override
    public int getMinorVersion()
    {
        return 1;
    }


    /** {@inheritDoc} */
    @Override
    public boolean jdbcCompliant()
    {
        LOGGER.info(
            "jdbcCompliant: " + false
        );
        return false;
    }

    /** {@inheritDoc } */
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException
    {
        throw new SQLFeatureNotSupportedException("Not supported yet.");
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.jdbc.Driver");//#LOGGER-NOPMD

    static
    {
        try
        {
            java.sql.DriverManager.registerDriver(new Driver());
        }
        catch( SQLException e)
        {
            LOGGER.warn("could not register driver", e);
        }
    }

}
