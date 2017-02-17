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
import com.aspc.remote.html.scripts.HTMLMouseEvent;

/**
 * A HTMLComponent used to generate HTML to represent an area of a image map that will
 * action the supplied href when clicked on by the mouse.
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       January 9, 2001, 1:29 PM
 */
public final class HTMLArea extends HTMLContainer implements AlternativeAttribute
{
    /**
     * Creates new HTMLArea
     * @param x
     * @param y
     * @param width
     * @param height
     * @param href
     */
    public HTMLArea(int x, int y, int width, int height, String href)
    {
        setCoordX(x);//NOPMD
        setCoordY(y);//NOPMD
        setCoordWidth(width);//NOPMD
        setCoordHeight(height);//NOPMD

        setURL(href);//NOPMD
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setAlternative( final String alt)
    {
        this.alt=alt;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public String getAlternative( )
    {
        return alt;
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
     * @param href
     */
    public void setURL( String href)
    {
        this.href = href;
    }

    /**
     *
     * @param x
     */
    public void setCoordX( int x)
    {
        this.coordX = x;
    }

    /**
     *
     * @param y
     */
    public void setCoordY( int y)
    {
        this.coordY = y;
    }

    /**
     *
     * @param width
     */
    public void setCoordWidth( int width)
    {
        this.coordWidth = width;
    }


    /**
     *
     * @param height
     */
    public void setCoordHeight( int height)
    {
        this.coordHeight = height;
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
        buffer.append("<area shape=\"rect\" coords=\"").append(coordX).append(",").append(coordY).append(",").append(coordWidth).append(",").append(coordHeight).append("\" href=\"").append(href).append(
            "\"");

        iGenerateAttributes(browser, buffer);

        buffer.append( ">\n");
    }

    private String  href;
    private int coordX,
        coordY,
        coordWidth,
        coordHeight;

}
