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
package com.aspc.remote.util.misc;

import java.text.DecimalFormat;
import java.text.ParseException;

/**
 *  A wraper for Decimal Format to make sure it's thread safe.
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 * @author  Nigel Leck
 * @since   5 April 2009
 */
public final class ConcurrentDecimalFormat
{
    private static final long serialVersionUID = 42L;
    private final String pattern;
    private final ThreadLocal< DecimalFormat > df = new ThreadLocal< DecimalFormat >()
    {
        @Override
        protected DecimalFormat initialValue()
        {
             return new DecimalFormat(pattern);
        }
    };

    /**
     * create a new Concurrent decimal format
     * @param pattern the pattern
     */
    public ConcurrentDecimalFormat( final String pattern)
    {
        this.pattern=pattern;
    }

    /**
     * format the number
     * @param no the number to format
     * @return the formated string
     */
    public String format(Object no)
    {
        return df.get().format(no);
    }

    /**
     * parse a number
     * @param text the text to parse
     * @return the number
     * @throws java.text.ParseException failed to parse the text
     */
    public Number parse( String text) throws ParseException
    {
        return df.get().parse(text);
    }
}
