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
package com.aspc.remote.task.extdbsync;

import org.apache.commons.logging.Log;
import com.aspc.remote.task.SyncManager;
import com.aspc.remote.application.*;
import com.aspc.remote.task.TaskHandler;
import com.aspc.remote.util.misc.*;
import java.util.*;
import org.apache.commons.cli.Options;

/**
 *  Updates an external database with changes made to stSoftware
 *  data in real time
 *
 * <br>
 * <i>THREAD MODE: SINGLETON command line application SINGLE-THREADED</i>
 *
 *  @author      Jason McGrath
 *  @since       23 September 2005
 */
public class ExtDBSyncApp extends AppCmdLine
{
    /**
     * handle the command line args
     * @param p The args
     * @throws Exception A serious problem
     */
    @Override
    @SuppressWarnings("deprecation")
    public void handleArgs( Properties p) throws Exception
    {
        super.handleArgs(p);

        fileName  = p.getProperty( "-f");
        taskCode  = p.getProperty( "-t");
        host  = p.getProperty( "-u");
        handlerClass = p.getProperty( "-h" );

        if( StringUtilities.isBlank( fileName))
        {
            String docDir = CProperties.getDocRoot();
            fileName = docDir + "/WEB-INF/extsync.xml";
        }

        if( StringUtilities.isBlank( taskCode))
        {
            throw new Exception( "-t (task code) parameter is mandatory");
        }

        if( StringUtilities.isBlank( host))
        {
            throw new Exception( "-u (host) parameter is mandatory");
        }

        if( StringUtilities.isBlank( handlerClass ) )
        {
            handlerClass = "com.aspc.remote.task.extdbsync.ExtDBSyncHandler";
        }
    }

    /**
     * add extra command line options
     * @param options the options
     */
    @Override
    protected void addExtraOptions( final Options options)
    {
        super.addExtraOptions( options);
        options.addOption( "t", true, "Task Code" );
        options.addOption( "f", true, "File name" );
        options.addOption( "u", true, "Host url" );
        options.addOption( "h", true, "Handler Class" );
    }

    /**
     * run the job manger
     */
    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void process()
    {
        try
        {
            Object instance = Class.forName( handlerClass ).newInstance();

            if( ! (instance instanceof TaskHandler) )
            {
                throw new IllegalArgumentException( "Handler Class must implement TaskHandler" );
            }

            TaskHandler handler = (TaskHandler)instance;

            if (handler instanceof ExtDBSyncHandler)
            {
                ((ExtDBSyncHandler) handler).configure(fileName);
            }

            SyncManager tm = new SyncManager( taskCode, host, handler );

            tm.listen();
            while(true)
            {
                Thread.sleep( 5 * 1000L );
                tm.wakeUp();
            }


        }
        catch( Exception e)
        {
            LOGGER.error( "oops", e);
        }
    }

    /**
     * main line
     *
     * @param argv The args
     */
    public static void main(String argv[])
    {
        AppCmdLine app = new ExtDBSyncApp();
        app.execute(argv);
    }

    private String fileName;
    private String taskCode;
    private String host;
    private String handlerClass;

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.task.extdbsync.ExtDBSyncApp");//#LOGGER-NOPMD
}
