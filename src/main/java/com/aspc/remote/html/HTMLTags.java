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
package com.aspc.remote.html;
import com.aspc.remote.util.misc.*;

/**
 *  HTMLTags
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       21 April 1998
 */
public class HTMLTags extends HTMLComponent
{
    /**
     * 
     * @param tags 
     */
    public HTMLTags(String tags)
    {
        this.tags = tags;
    }

    /**
     * 
     * @param tags 
     * @param string_if_blank 
     */
    public HTMLTags(String tags, String string_if_blank)
    {
        if (StringUtilities.isBlank(tags))
        {
          this.tags = string_if_blank;
        }
        else
        {
          this.tags = tags;
        }
    }

    /**
     * generate the raw HTML for this component.
     *
     * @param browser The target browser
     * @param buffer The generate HTML
     */    
    @Override
    protected void iGenerate( ClientBrowser browser, StringBuilder buffer )
    {
        String text = tags;

        // Remove illegal characters
        for( int i = 0; i < text.length(); i++ )
        {
            char c = text.charAt( i );

            if( c >= 128 )
            {
                char array[] = { c };
                String from = new String( array );
                String to = "&#" + (int)c + ";";
                text = text.replace( from, to );
            }
        }

        buffer.append( text);
    }

    /**
     * 
     * @return the value
     */
    public String getTags()
    {
        return tags;
    }

    private String tags;//NOPMD
}
