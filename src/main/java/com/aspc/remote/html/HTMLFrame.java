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
package com.aspc.remote.html;

import com.aspc.remote.util.net.NetUrl;

/**
 *  HTMLFrame
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       27 February 1998
 */
public class HTMLFrame extends HTMLComponent
{
    /**
     *
     * @param name
     * @param src
     */
    public HTMLFrame( final String name, final String src)
    {
        iSetName(name);
        this.src = src;
        if( NetUrl.asserValidURL(src)==false)
        {
            assert false: "invalid URL: " + src;
            this.src=NetUrl.correctURL( src);
        }
        
        setBorder( true);//NOPMD
    }

    /**
     *
     * @return the value
     */
    public String getName()
    {
        return name;
    }

    /**
     *
     * @return the value
     */
    public String getSrc()
    {
        return src;
    }

    /**
     *
     * @param on
     */
    public final void setBorder( boolean on)
    {
        border = on;
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
        String href=HTMLAnchor.htmlEncodeHREF(HTMLAnchor.makeBondaryCheckURL(src, true));
        buffer.append("<frame name=\"").append(name).append("\" src=\"").append(href).append(
            "\"");

        if( scrolling != null)
        {
            buffer.append(" scrolling=").append( scrolling);
        }

        if( border == false)
        {
            // We need BORDER=0 for netscape.
            buffer.append( " frameborder=0 noresize");
        }

        if( marginHeight != null)
        {
            buffer.append(" marginheight=\"").append(marginHeight).append( "\"");
        }

        if( marginWidth != null)
        {            
            buffer.append(" marginwidth=\"").append(marginWidth).append( "\"");
        }


        buffer.append( ">\n");
    }

    /**
     *
     * @param scrolling
     */
    public void setScrolling( String scrolling)
    {
        this.scrolling = scrolling;
    }

    /**
     *
     * @param parent
     */
    @Override
    public void setParent( HTMLComponent parent)
    {
        HTMLComponent component;

        component = parent;
        boolean found = false;

        while( component != null)
        {
            if( component instanceof HTMLFrameSet)
            {
                found = true;
                //theFrameSet = (HTMLFrameSet)component;
                break;
            }
            component = component.getParent();
        }

        if( found == false)
        {
            throw new RuntimeException(
                "Frame must be added to a HTMLFrameSet"
            );
        }

        super.setParent( parent);
    }

    /**
     *
     * @param marginHeight
     */
    public void setMarginHeight( String marginHeight)
    {
        this.marginHeight = marginHeight;
    }

    /**
     *
     * @param marginWidth
     */
    public void setMarginWidth( String marginWidth)
    {
        this.marginWidth = marginWidth;
    }

    private String  scrolling,
                    marginHeight,
                    marginWidth,
                    src;//NOPMD

    private boolean border;
}
