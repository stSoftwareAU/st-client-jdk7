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

import com.aspc.remote.memory.HashMapFactory;
import java.util.HashMap;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Builds fixed width record strings.
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 * @author      luke
 * @since       May 22, 2007
 */
public class FixedWidthRecord
{
    private final String spec;
    private final HashMap data;

    /**
     * Formats data into fixed width strings
     * name begin end filler align, ...
     * "id 0 4 0 right, name 4 24 space left"
     * @param spec the field spec
     */
    public FixedWidthRecord( final String spec )
    {
        this.spec = spec;
        this.data = HashMapFactory.create();
    }

    /**
     * Sets the named value, 'name' should be the same as a field name in the spec
     * @param name the field name
     * @param value the field value
     */
    public void set( String name, String value )
    {
        data.put( name, value );
    }

    /**
     * Gets the value for the given field name, if no value is set returns empty string
     * @param name the field name
     * @return String the value
     */
    public String get( String name )
    {
        String value = (String)data.get(name);

        if( value == null )
        {
            value = "";
        }

        return value;
    }

    /**
     * Formats the data into a fixed width string using the spec.
     * if a value is larger than the size of the field, it is truncated
     * @return String the fixed width record
     */
    @Override @CheckReturnValue @Nonnull
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        if( ! StringUtilities.isBlank( spec ) )
        {
            String[] fields = spec.split( "," );
            for (String field : fields) {
                String[] fld = field.trim().split(" ");
                if (fld.length != 5) {
                    throw new RuntimeException("bad specifcation string, (name begin end fill align): " + field);
                }
                String value = get( fld[0] );
                int bgn = Integer.parseInt( fld[1] );
                int end = Integer.parseInt( fld[2] );
                char fill = (fld[3].equalsIgnoreCase("space"))?' ':fld[3].charAt(0);
                boolean left = fld[4].equalsIgnoreCase("left");
                if( value.length() > (end - bgn) )
                {
                    value = value.substring( 0, (end - bgn) );
                }
                if( left )
                {
                    buf.append( value );
                    bgn += value.length();
                    while( bgn < end )
                    {
                        buf.append( fill );
                        bgn++;
                    }
                }
                else
                {
                    end -= value.length();
                    while( bgn < end )
                    {
                        buf.append( fill );
                        bgn++;
                    }
                    buf.append( value );
                }
            }
        }

        return buf.toString();
    }
}
