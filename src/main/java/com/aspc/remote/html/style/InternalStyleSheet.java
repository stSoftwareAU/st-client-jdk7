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
package com.aspc.remote.html.style;

import com.aspc.remote.html.ClientBrowser;
import java.awt.Color;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  HTMLStyleSheet
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       17 August 1998
 */
public abstract class InternalStyleSheet
{
    public static final String POSITION_STATIC      = "static";
    public static final String POSITION_RELATIVE    = "relative";
    public static final String POSITION_FIXED    = "fixed";
    public static final String POSITION_ABSOLUTE    = "absolute";

    public static final String BACKGROUND_COLOUR    = "background-color";

    //public static final String CURSOR_HAND          = "hand";
    public static final String CURSOR_POINTER       = "pointer";

    public static final String STYLE_CURSOR         = "cursor";

    /**
     * http://www.w3schools.com/htmldom/prop_style_borderwidth.asp
     */
    public static final String STYLE_BORDER_WIDTH   = "border-width";
    /**
     * http://www.w3schools.com/CSS/pr_text_white-space.asp
     */
    public static final String STYLE_WHITE_SPACE   = "white-space";
     /** normal:  Default. White-space is ignored by the browser*/
    public static final String WHITE_SPACE_NORMAL   = "normal";
     /** pre:     White-space is preserved by the browser. Acts like the &lt;pre&gt; tag in HTML*/
    public static final String WHITE_SPACE_PRE   = "pre";
     /** nowrap:  The text will never wrap, it continues on the same line until a &lt;br&gt; tag is encountered*/
    public static final String WHITE_SPACE_NOWRAP   = "nowrap";

    /**
     * font size
     */
    public static final String STYLE_FONT_SIZE      ="font-size";
    /**
     * font family
     */
    public static final String STYLE_FONT_FAMILY    ="font-family";

    /**
     * text decoration
     */
    public static final String STYLE_TEXT_DECORATION    ="text-decoration";

    public static final String BORDER_WIDTH_NONE    = "none";
    public static final String BORDER_WIDTH_THIN    = "thin";
    public static final String BORDER_WIDTH_MEDIUM  = "medium";
    public static final String BORDER_WIDTH_THICK   = "thick";

    public InternalStyleSheet()
    {

    }

    public void addElement( final String type, final String value)
    {
        items.put(type, value);
    }

    public void setColour(
        String type,
        Color color
    )
    {
        int c;
        c = color.getRGB() & 0xffffff;

        String  t;

        t = "000000" + Integer.toHexString(c);

        t = "#" + t.substring(t.length() - 6);

        addElement( type, t);
    }

    public int getNumElements()
    {
        return items.size();
    }

    public void setZ( int index)
    {
        addElement("z-index", "" + index);
    }

    public void setPosition( String type)
    {
        addElement( "position", type);
    }

    public void setVisible( boolean visible)
    {
        if( visible == true)
        {
            addElement( "visibility", "visible");
        }
        else
        {
            addElement( "visibility", "hidden");
        }
    }

    /**
     * generate the raw HTML for this component.
     *
     * @param browser The target browser
     * @param buffer The generate HTML
     */
    public abstract void iGenerate( ClientBrowser browser, StringBuilder buffer);

    protected final ConcurrentHashMap<String, String> items = new ConcurrentHashMap();
}
