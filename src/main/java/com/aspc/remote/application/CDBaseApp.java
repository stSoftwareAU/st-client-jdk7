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
package com.aspc.remote.application;

import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.*;
import com.aspc.remote.database.*;

/**
 *  CDBaseApp Data base Application class which extends CApplication
 *
 * <br>
 * <i>THREAD MODE: SINGLETON command line application SINGLE-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       2 December 1996
 */
public class CDBaseApp extends CApp
{
    DataBase dataBase;

    /**
     * 
     * @param args The command line arguments
     */
    @Override
    @SuppressWarnings("empty-statement")
    public void handleArgs( String []args)
    {
        super.handleArgs( args);
        
        // Defaults
        String user        = "guest";
        String password    = "guest";

        String        type       = "sybase";
        String        url        = null;

        for (String arg : args) {
            if (arg.startsWith("-U")) {
                user = arg.substring(2);
            } else if (arg.startsWith("-T")) {
                type = arg.substring(2);
            } else if (arg.startsWith("-D")) {
                url = arg.substring(2);
            }
            if (arg.startsWith("-P")) {
                password = arg.substring(2);
            }
        }

        if( url != null)
        {
            DataBase db;

            try
            {
                db = new DataBase(user, password, type, url, DataBase.Protection.NONE);

                db.connect();
            }
            catch( Exception e)
            {
                ;//OK
            }
        }
    }

    /**
     * The main for the program
     * 
     * @param argv The command line arguments
     */
    public static void main( String argv[]) 
    {
        Run.application( "com.aspc.remote.application.CDBaseApp");
    }
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.application.CDBaseApp");//#LOGGER-NOPMD
}
