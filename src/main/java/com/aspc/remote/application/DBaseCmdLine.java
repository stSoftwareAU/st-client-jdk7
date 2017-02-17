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
package com.aspc.remote.application;

import com.aspc.remote.database.DataBase;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.CProperties;
import javax.annotation.Nonnull;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;

/**
 *  DBaseCmdLine a Data Base command line
 *
 * <i>THREAD MODE: SINGLE-THREADED command line application</i>
 *
 *  @author      Nigel Leck
 *  @since       23 June 1997
 */
public abstract class DBaseCmdLine extends AppCmdLine
{
    private static final String DEFAULT_USER="guest";
    private static final String DEFAULT_TYPE="sybase";
    private static final String DEFAULT_PW="";

    /**
     * add extra command line options
     * @param options the options
     */
    @Override
    protected void addExtraOptions( final @Nonnull Options options)
    {
        super.addExtraOptions( options);

        Option urlOption = new Option( "D", true, "The URL to connect");
        urlOption.setArgName( "URL");
        options.addOption(  urlOption);

        Option typeOption = new Option( "T", true, "The Type of the connection (default '" + DEFAULT_TYPE +"')");
        typeOption.setArgName( "SYBASE|HSQLDB|POSTGRES");
        options.addOption(  typeOption);

        Option userOption = new Option( "U", true, "User name for connection (default '" + DEFAULT_USER + "')");
        userOption.setArgName( "user");
        options.addOption(  userOption);

        Option passOption = new Option( "P", true, "User's Password (default '" + DEFAULT_PW + "')");
        passOption.setArgName( "password");
        passOption.setOptionalArg(true);

        options.addOption(  passOption);
    }

    /**
     * handle the command line args
     *
     * @param line the command line
     * @throws Exception a serious problem
     */
    @Override
    public void handleCommandLine( final @Nonnull CommandLine line) throws Exception
    {
        super.handleCommandLine( line);
        // Defaults
        user     = DEFAULT_USER;
        type     = DEFAULT_TYPE;
        password = DEFAULT_PW;

        url = line.getOptionValue("D", CProperties.getProperty( "DBSOURCE"));
        if( url == null)
        {
            throw new Exception( "No Url supplied");
        }

        user        = line.getOptionValue("U", CProperties.getProperty( "DBUSER", user));
        type        = line.getOptionValue("T", CProperties.getProperty( "DBTYPE", type));
        if( line.hasOption("P"))
        {
            password    = line.getOptionValue("P", "");
        }
        else
        {
            password    = CProperties.getProperty( "DBPASSWORD", password);
        }
    }

    /**
     * connect to the database
     * @throws Exception a serious problem
     */
    @Override
    public void init() throws Exception
    {
        super.init();
        DataBase db;

        try
        {
          db = new DataBase(user, password, type, url, DataBase.Protection.NONE);

          db.connect();
          LOGGER.info("Connected ");
        }
        catch( Exception e)
        {
            LOGGER.info( "Connect Error :- Url:" + url + " User:" + user + " Type:" + type + " Password:" + password);
            throw e;
        }
    }
    /**
     * the url
     */
    protected String url;
    /**
     * the user
     */
    protected String user;
    /**
     * the type
     */
    protected String type;

    /**
     * the password
     */
    protected String                                                                            password;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.application.DBaseCmdLine");//#LOGGER-NOPMD
}
