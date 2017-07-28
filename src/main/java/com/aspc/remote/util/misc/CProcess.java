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
package com.aspc.remote.util.misc;

import org.apache.commons.logging.Log;
import java.io.*;
import java.util.*;

/**
 *  CProcess
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       19 September 1998
 */
public class CProcess
{
    private final String cmds[];//NOPMD
    private final int timeout;//NOPMD
    /**
     * 
     * @param cmds 
     */
    public CProcess(String cmds[])
    {
        this.cmds = cmds;
        timeout = 3600;
    }

    /**
     * 
     * @throws Exception a serious problem
     */
    public void execute() throws Exception
    {
        Date start = new Date();
        Process process = Runtime.getRuntime().exec( cmds );
        ProcessRunner processRunner = null;

        String  processCmds = null;

        StringBuilder outBuffer = new StringBuilder(),
                     errBuffer = new StringBuilder();

        try
        {
            InputStream processOut = process.getInputStream();
            InputStream processErr = process.getErrorStream();

            processRunner = new ProcessRunner(process);

            processRunner.join(
                timeout * 1000L
            );

            getText( outBuffer, processOut);
            getText( errBuffer, processErr);

            if( processRunner.exception != null )
            {
                throw processRunner.exception;
            }

            String errText = errBuffer.toString();

            if( errText.equals( "") == false)
            {
                LOGGER.info( processCmds);
                throw new Exception(
                    errText
                );
            }

            LOGGER.debug( "Debug: DONE in " + TimeUtil.getDiff(start) );

            int v;
            v = process.exitValue();

            if( v != 0 )
            {
                for (String cmd : cmds) {
                    LOGGER.info(cmd);
                }
                LOGGER.info( "");
                throw new Exception( "returned ( " + v + " )" );
            }

        }
        finally
        {
            if( process != null)
            {
                // release all the memory and resources.
                process.destroy();
            }

            for( int loop = 0; loop < 2; loop++)
            {
                if( ThreadUtil.isAliveOrStarting(processRunner))
                {
//                    if( loop == 0)
//                    {
                    assert processRunner!=null;
                    processRunner.interrupt();
//                    }
//                    else
//                    {
//                        processRunner.stop( new Error( "killed"));
//                    }

                    processRunner.join( 5 * 1000L);
                }
            }
//            processRunner = null;
//            process = null;
        }
    }

    private boolean getText(
        StringBuilder buffer,
        InputStream  inputStream
    ) throws Exception
    {
        byte data[];
        int l;

        l = inputStream.available();
        if( l != 0)
        {
            data = new byte[l];

            inputStream.read( data );
            String text;

            text = new String( data );
            buffer.append( text);
            return true;
        }

        return false;
    }
    
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.CProcess");//#LOGGER-NOPMD
}

class ProcessRunner extends Thread
{
    Process process;
    Exception exception;

    public ProcessRunner( Process process)
    {
        setName( "Process Runner");
        setDaemon( true);
        this.process = process;

        start();
    }

    @Override
    public void run()
    {
        try
        {
            process.waitFor( );
        }
        catch( Exception e)
        {
            exception = e;
        }
        process = null;
    }
}
