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

/**
 *  HTMLListText
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       18 July 1998
 */
public class HTMLListText extends HTMLComponent
{
    /**
     *
     * @param text
     */
    public void addItem( String text)
    {
        addItem(new HTMLText( text));
    }

    /**
     *
     * @param text
     */
    public void addItem( HTMLText text)
    {
        iAddComponent(text);
    }

    /**
     *
     * @param component 
     */
    public void addComponent( HTMLComponent component)
    {
        iAddComponent(component);
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
        if( items == null)
        {
            return;
        }

        buffer.append( "<UL" );
        iGenerateAttributes(browser, buffer);
        buffer.append( ">\n" );

        for( int i = 0; i < iGetComponentCount(); i++)
        {
            HTMLComponent c;

            c = iGetComponent(i);
            assert c!=null;
            buffer.append("<LI>");

            iGenerateComponent(c, browser, i, buffer);
            
            buffer.append("</LI>\n");
        }

        buffer.append( "</UL>\n" );
    }
}
