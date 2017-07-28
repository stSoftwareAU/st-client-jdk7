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

import com.aspc.remote.html.scripts.HTMLMouseEvent;
import com.aspc.remote.util.misc.StringUtilities;

/**
 *  HTMLImage
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       16 May 1998
 */
public final class HTMLImage extends HTMLContainer implements AlternativeAttribute
{
    /**
     *
     * @param url
     */
    public HTMLImage( final String url)
    {
        this.url = url;
        width = "";
        height = "-1";
        horizontalSpacing = -1;
        verticalSpacing = 0;
    }

    /**
     * add a mouse event to this component
     *
     * @param me The mouse event
     */
    public void addMouseEvent(HTMLMouseEvent me)
    {
        iAddEvent(me, "");
    }

    /**
     *
     * @param pixels
     */
    public void setBorder( int pixels)
    {
        border = pixels;
    }

    /**
     * Sets the name of the image so that it can be used in mouse overs etc
     * @param inName
     */
    public void setName( String inName)
    {
        iSetName( inName);
    }

    /**
     * get the ID of this component
     *
     * @return the ID
     */
    @Override
    public String getId()
    {
        return id;
    }

    /**
     * set the ID of this component.
     *
     * @param id The id of the component
     */
    public void setId( String id)
    {
        iSetId(id);
    }

    /**
     *
     * @param cursor
     */
    public void setMouseOverCursor( String cursor)
    {
        rollOverCursor = cursor;
    }

    /**
     *
     * @param width
     */
    public void setWidth( int width)
    {
        setWidth( "" + width);
    }

    /**
     *
     * @param width
     */
    public void setWidth( String width)
    {
        this.width = width;
    }

    /**
     *
     * @param height
     */
    public void setHeight( int height)
    {
        this.height = "" + height;
    }

    /**
     *
     * @param height
     */
    public void setHeight( String height)
    {
        this.height = height;
    }

    /**
     *
     * @param align
     */
    @Override
    public void setAlignment( String align)
    {
        this.alignment = align;
    }

    /**
     *
     * @param pixels
     */
    public void setHorizontalSpacing( int pixels)
    {
        horizontalSpacing = pixels;
    }

    /**
     *
     * @param pixels
     */
    public void setVerticalSpacing( int pixels)
    {
        verticalSpacing = pixels;
    }

    public String getToolTip()
    {
        return toolTip;
    }

    public void setToolTip(String toolTip)
    {
        this.toolTip = toolTip;
    }

    /**
     *
     * @param htmlArea
     */
    @Override
    public void addComponent( HTMLComponent htmlArea)
    {
        if( htmlArea instanceof HTMLArea)
        {
            super.addComponent(htmlArea);
        }
        else
        {
            throw new RuntimeException( "Can only add areas to images");
        }
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
        int areas;
        String mapName = "";

        areas = getComponentCount();

        if( areas > 0 )
        {
            mapName = "IMGMAP_" + getParentPage().doGenerateId();
            buffer.append("<map name='");
            buffer.append( mapName);
            buffer.append( "'>\n");
            super.iGenerate( browser, buffer);
            buffer.append("</map>\n");
            buffer.append("<a>");
        }

        // Netscape cannot handle hand cursor
        if( StringUtilities.isBlank( rollOverCursor) == false)
        {
            if( rollOverCursor.equalsIgnoreCase( "hand") &&
                browser.isBrowserNETSCAPE())
            {
                setStyleProperty("cursor", "pointer");
            }
            else
            {
                setStyleProperty("cursor", rollOverCursor);
            }
        }

        buffer.append("<img src='");
        buffer.append( HTMLAnchor.htmlEncodeHREF(url));
        buffer.append( "' border=");
        buffer.append( border);

        if( areas>0 )
        {
            buffer.append(" usemap='#");
            buffer.append( mapName);
            buffer.append( "'");
        }

        if( verticalSpacing != -1)
        {
            buffer.append( " vspace=");
            buffer.append( verticalSpacing);
        }

        if( horizontalSpacing != -1)
        {
            buffer.append( " hspace=");
            buffer.append( horizontalSpacing);
        }

        if( width.equals("") == false)
        {
            buffer.append( " width=");
            buffer.append( width);
        }

        if( height.equals("-1") == false)
        {
            buffer.append( " height=");
            buffer.append( height);
        }

        iGenerateAttributes(browser, buffer);

        buffer.append( ">");
        if( areas > 0 )
        {
            buffer.append("</a>");
        }
    }

    private final String  url;//NOPMD

    private int     border,
                    verticalSpacing,
                    horizontalSpacing;

    private String  height,
                    width,
                    rollOverCursor;

    /** {@inheritDoc } */
    @Override
    public void setAlternative(String alt)
    {
        this.alt=alt;
    }

    /** {@inheritDoc } */
    @Override
    public String getAlternative()
    {
        return alt;
    }
}
