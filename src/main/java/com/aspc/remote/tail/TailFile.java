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
package com.aspc.remote.tail;

import com.aspc.remote.util.misc.*;
import java.io.File;
import java.io.FileInputStream;
import org.apache.commons.logging.Log;

/**
 *  Tail output Stream
 *
 * <br>
 * <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel
 *  @since       2 July 2009
 */
public class TailFile implements Runnable
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.tail.TailFile");//#LOGGER-NOPMD
    private final File file;

    private final byte[]buffer=new byte[20480];
    private final TailStream tailStream;
    private final TailQueue queue;

    private long lastPos;

    /**
     * create a tail stream of this size
     * @param text the text
     * @param color the color
     */
    public TailFile( final File file, final TailQueue queue )
    {
        if( file == null) throw new IllegalArgumentException( "file must be passed");
        if( queue == null) throw new IllegalArgumentException( "queue must be passed");

        this.file=file;
        this.queue=queue;
        tailStream = new TailStream( queue, null);
    }

    public void schedule()
    {
        ThreadPool.schedule(this, "read: " + file);

    }
    @SuppressWarnings("SleepWhileInLoop")
    @Override
    public void run()
    {
        while( queue.isClosed() == false)
        {
            try
            {
                Thread.sleep(200);
                readFile();
            }
            catch( Exception e)
            {
                LOGGER.warn( "could not read file " + file, e);
                try
                {
                    Thread.sleep((long) (10000 ));
                }
                catch (InterruptedException ex)
                {

                }
            }
        }
    }

    private void readFile( ) throws Exception
    {
        if( file.canRead())
        {
            FileInputStream in=null;
            try
            {
                long len = file.length();
                if( len < lastPos)
                {
                    // We've restarted the file.
                    lastPos=0;
                }
                int readLen =(int)(len - lastPos);
                if( readLen < 1)
                {
                    return;
                }
                in=new FileInputStream( file);
                if( readLen > buffer.length)
                {
                    lastPos=len - buffer.length;
                }
                in.skip(lastPos);
                int howManyRead = in.read( buffer, 0, buffer.length);
                if( howManyRead>0)
                {
                    lastPos+=howManyRead;
                    for( int i =0; i < howManyRead; i++)
                    {
                        tailStream.write(buffer[i]);
                    }
                }
            }
            finally
            {
                if( in != null) in.close();
            }
        }
    }
}
