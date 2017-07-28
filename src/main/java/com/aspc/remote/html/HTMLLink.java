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

import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.*;

/**
 *  This element defines a link.
 *  Unlike Anchor elements, it may only appear in the HEAD section of a document,
 *  although it may appear any number of times. Although LINK has no content, it
 *  conveys relationship information that may be rendered by user agents in a variety
 *  of ways (e.g., a tool-bar with a drop-down menu of links).
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Jason McGrath
 *  @since       August 28, 2006 4:29 PM
 */
public class HTMLLink extends HTMLContainer
{
    /**
     * the title
     * @param title
     */
    public void setTitle( String title)
    {
        this.title = title;
    }
    /**
     * the content type of the content available at the link target address
     * @param type - contect type
     */
    public void setContentType( String type)
    {
        this.type = type;
    }

    /**
     * the location of a Web resource
     * @param href - href of web resource
     */
    public final void setURL( String href)
    {
        this.href = href;
    }

    
    /**
     * describes the relationship from the current document to the anchor specified by the href attribute
     * @param rel - name of forward link
     */

    public void setForwardLinkType( String rel)
    {
        this.rel = rel;
    }
    
    /**
     * describe a reverse link from the anchor specified by the href attribute to the current document.
     * @param rev - name of reverse link
     */

    public void setReverseLinkType( String rev)
    {
        this.rev = rev;
    }

    /**
     * specifies the intended destination medium for style information. It may be a single media descriptor or a comma-separated list.
     * @param media - media type
     */
    public void setMedia( String media)
    {
        this.media = media;
    }

    /**
     * the base language of the resource designated by href
     * @param hreflang - base language
     */
    public void setLanguage( String hreflang)
    {
        this.hreflang = hreflang;
    }
    
   /**
     * the character encoding of the resource designated by the link
     * @param charset - character set
     */
    public void setCharSet( String charset)
    {
        this.charset = charset;
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
        buffer.append( "<LINK ");

        if( StringUtilities.isBlank(title) == false)
        {
            buffer.append( " title=\"");
            buffer.append( StringUtilities.encodeHTML(title));
            buffer.append( "\"");
        }
        
        if( StringUtilities.isBlank(rel) == false)
        {
            buffer.append( " rel=\"");
            buffer.append( rel);
            buffer.append( "\"");
        }
        
        if( StringUtilities.isBlank(rev) == false)
        {
            buffer.append( " rev=\"");
            buffer.append( rev);
            buffer.append( "\"");
        }
        
        if( StringUtilities.isBlank(href) == false)
        {
            buffer.append( " href=\"");
            buffer.append( href);
            buffer.append( "\"");
            
            if( StringUtilities.isBlank(hreflang) == false)
            {
                buffer.append( " hreflang=\"");
                buffer.append( hreflang);
                buffer.append( "\"");
            }
        }
        
        if( StringUtilities.isBlank(type) == false)
        {
            buffer.append( " type=\"");
            buffer.append( type);
            buffer.append( "\"");
        }
        
                
        if( StringUtilities.isBlank(media) == false)
        {
            buffer.append( " media=\"");
            buffer.append( media);
            buffer.append( "\"");
        }                
                
        if( StringUtilities.isBlank(charset) == false)
        {
            buffer.append( " charset=\"");
            buffer.append( charset);
            buffer.append( "\"");
        }

        buffer.append( ">");
    }

 
    private String      title,
                        href,
                        type,
                        charset,
                        rel,
                        rev,
                        media,
                        hreflang;

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.html.HTMLLink");//#LOGGER-NOPMD
}
