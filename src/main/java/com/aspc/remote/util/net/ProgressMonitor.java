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
package com.aspc.remote.util.net;

import com.aspc.remote.util.misc.CLogger;
import java.nio.channels.FileChannel;
import org.apache.commons.logging.Log;

/**
 * Moved from internal class in BaseClientNetUtil
 * @author Nigel Leck
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 * @since       February 1, 2006
 */
public class ProgressMonitor implements Runnable
{
    FileChannel channel         = null;
    IClientListener listener    = null;
    long size                   = 0;
    int checkInterval           = 1000; //1 sec
    private int percent         = -1;//NOPMD

    /**
     * Monitor constructor.
     * @param channel The file channel
     * @param listener The client listener
     * @param size The size of a file to transfer
     */
    public ProgressMonitor(FileChannel channel, IClientListener listener, long size)
    {
        this.channel = channel;
        this.listener = listener;
        this.size = size;

    }
    /**
     * Monitor constructor.
     * @param channel The file channel
     * @param listener The client listener
     * @param size The size of a file to transfer
     * @param checkInterval The interval for checking file transfer status
     */ 
    public ProgressMonitor(FileChannel channel, IClientListener listener, long size, int checkInterval)
    {
        this(channel, listener, size);
        this.checkInterval = checkInterval;

    }

    /**
     * monitor the transfer
     */
    @Override
    public void run()
    {            
        while (true)
        {
            long fetchedBytes = 0;

            try
            {
                //MT WARN: is this thread safe ??? when another thread is writing to it ? 
                fetchedBytes = channel.size();

                Thread.sleep(checkInterval);
            }
            catch(Exception ex)
            {
                LOGGER.info("Progress bar monitor was unable to run");
                break;
            }

            percent= (int) (fetchedBytes*100/size);
            sendUpdate(listener, percent);
            if(fetchedBytes >= size)
            {
                break;
            }

        }

    }
    
    /**
     * Sends an updated download status to the listener.
     * @param listener The listener
     * @param update The percentage of a file being transfered
     */
    private void sendUpdate(IClientListener listener, int update)
    {
        if(listener != null)
        {
            listener.clientStatusActionPerformed(update);
        }
    }
    
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.net.ProgressMonitor");//#LOGGER-NOPMD
}
