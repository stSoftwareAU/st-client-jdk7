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

import javax.swing.text.*;
import java.io.Writer;
import java.io.IOException;

/**
 *  CFile
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 *  @author      Nigel Leck
 *  @since       17 September 1998
 */
public class TextWriter extends AbstractWriter
{
    /**
     *
     * @param w
     * @param doc the document
     */
    public TextWriter( Writer w, Document doc)
    {
        super( w, doc);
    }
    
    @Override
    public void write() throws IOException, BadLocationException 
    {
        ElementIterator it =  new ElementIterator(
            getDocument().getDefaultRootElement()
        );

        /*
          This will be a section element for a styled document.
          We represent this element in html as the body tags.
          Therefore we ignore it.
         */
        it.current();

        Element next = null;

        StringBuilder buffer = new StringBuilder();
        
        while((next = it.next()) != null)//NOPMD
        {            
            if (isText(next) == false)
            {
                if( next.getName().equalsIgnoreCase( "br"))
                {
                    buffer.append( "\n" );
                }
                continue;
            }
            String t = getText(next);

            buffer.append( t);
        }
        
        String text = buffer.toString().trim();
        
        getWriter().write( text);
    }

    /**
     *
     * @param elem
     * @return the value
     */
    protected boolean isText(Element elem)
    {
        String name = elem.getName();
        
        return name.equalsIgnoreCase( AbstractDocument.ContentElementName);
    }
}
