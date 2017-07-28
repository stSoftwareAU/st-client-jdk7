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

import java.io.IOException;
import org.apache.commons.logging.Log;

import com.aspc.remote.util.misc.*;
import java.awt.Color;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 *  Tail output Stream
 *
 * <br>
 * <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel
 *  @since       2 July 2009
 */
public class TailStream extends OutputStream
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.tail.TailStream");//#LOGGER-NOPMD
    private final byte buffer[];
    private int top;
    private boolean looped;

    private final TailQueue queue;
    private final Color color;

    /**
     * create a tail stream of this size
     * @param queue the queue
     * @param color
     */
    public TailStream( final TailQueue queue, final Color color)
    {
        buffer=new byte[2048];
        this.queue=queue;
        this.color=color;
    }

    /**
     * write the byte
     * @param b the byte
     * @throws IOException a serious problem
     */
    @Override
    public synchronized void write(final int b) throws IOException
    {
        if( b == 0xD)
        {
            return;
        }

        int pos = top;

        top++;

        if( top >= buffer.length)
        {
            looped=true;
            top = 0;
        }

        buffer[pos]=(byte)b;
        if( b == 0xA)
        {
            makeLine();
        }

    }

    private void makeLine()
    {
        String data;
        if( looped)
        {
            String s1 = new String(buffer, top, buffer.length - top, StandardCharsets.UTF_8);
            String s2 = new String(buffer, 0, top, StandardCharsets.UTF_8);

            data = s1 + s2;
        }
        else
        {
            data = new String(buffer, 0, top, StandardCharsets.UTF_8);
        }
        top = 0;
        looped=false;
        queue.addLine( new TailLine( data, color));
    }
}

