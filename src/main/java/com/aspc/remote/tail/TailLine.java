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
package com.aspc.remote.tail;

import com.aspc.remote.util.misc.*;
import java.awt.Color;
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
public class TailLine
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.tail.TailLine");//#LOGGER-NOPMD
    public final String text;
    public final Color color;

    /**
     * create a tail stream of this size
     * @param text the text
     * @param color the color
     */
    public TailLine( final String text, final Color color)
    {
        this.text=text;
        this.color=color;
    }

    public String getText()
    {
        return text;
    }
}
