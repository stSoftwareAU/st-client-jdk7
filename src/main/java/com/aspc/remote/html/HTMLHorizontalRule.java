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
 *  HTMLHorizontalRule
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 * @since 29 September 2006
 *  @since
 */
public class HTMLHorizontalRule extends HTMLComponent
{
    /**
     * 
     * @param size 
     */
    public void setSize( int size )
    {
        this.size = size;
    }

    /**
     * 
     * @param width 
     */
    public void setWidth( String width )
    {
        this.width = width;
    }

    //public void setWidthPercent( int widthPercent )
    //{
    //    this.widthPercent = widthPercent;
   // }

    /**
     * 
     * @param noShade 
     */
    public void setNoShade( boolean noShade )
    {
        this.noShade = noShade;
    }

    /**
     * generate the raw HTML for this component.
     *
     * @param browser The target browser
     * @param buffer The generate HTML
     */    
    @Override
    protected void iGenerate( ClientBrowser browser, StringBuilder buffer)
    {
        buffer.append( "<HR" );

        if( StringUtilities.isBlank(width) == false)
            buffer.append( " WIDTH=" + width );
   //     else if( widthPercent != -1 )
   //         buffer.append( " WIDTH=" + widthPercent + "%" );

        if( size != -1 )
            buffer.append( " SIZE=" + size );

        if( noShade )
            buffer.append( " NOSHADE" );

        buffer.append( ">" );
    }

    private boolean     noShade = false;
    private int         size = -1;
    private String      width;
//    private int         widthPercent = -1;
}
