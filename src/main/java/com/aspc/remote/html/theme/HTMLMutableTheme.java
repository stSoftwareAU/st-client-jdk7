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
package com.aspc.remote.html.theme;

import java.awt.Color;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 *  HTMLTheme.java
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       July 23, 2000, 8:01 PM
 */
public class HTMLMutableTheme extends HTMLTheme
{
    /** Creates new HTMLTheme */
    public HTMLMutableTheme()
    {
        iSetDefaults();//NOPMD
    }

    /**
     *
     * @param relativeSize
     */
    public void setDefaultFontRelativeSize( int relativeSize)
    {
        setDefault(
            DKEY_FONT_RELATIVE_SIZE, relativeSize);
    }

    /**
     *
     * @param size
     *
    public void setDefaultFontSize( int size)
    {
        setDefault(
            DKEY_FONT_SIZE,
            Integer.valueOf( size)
        );
    }*/

    /**
     *
     * @param name
     *
    public void setDefaultFontFace( String name)
    {
        setDefault(
            DKEY_FONT_FACE,
            name
        );
    }*/

    /**
     *
     * @param color
     */
    public void setDefaultErrorFontColor( int color)
    {
        setDefault(
            DKEY_ERROR_FONT_COLOR,
            new Color(color)
        );
    }

    /**
     *
     * @param color
     */
    public void setDefaultFontColor( int color)
    {
        setDefault(
            DKEY_FONT_COLOR,
            new Color(color)
        );
    }

    /**
     *
     * @param color
     */
    public void setDefaultFontColor( Color color)
    {
        setDefault(
            DKEY_FONT_COLOR,
            new Color(color.getRGB())
        );
    }

    /**
     *
     * @param bold
     */
    public void setDefaultFontBold( boolean bold)
    {
        setDefault(
            DKEY_FONT_BOLD, bold);
    }

    /**
     *
     * @param relativeSize
     */
    public void setDefaultButtonFontRelativeSize( int relativeSize)
    {
        setDefault(
            DKEY_FONT_RELATIVE_SIZE, relativeSize);
    }

    /**
     *
     * @param color
     */
    public void setDefaultLinkColor( int color)
    {
        setDefault(
            DKEY_LINK_COLOR,
            new Color(color)
        );
    }

    /**
     *
     * @param bold
     */
    public void setDefaultLinkBold( boolean bold)
    {
        setDefault(
            DKEY_LINK_BOLD, bold);
    }

    /**
     *
     * @param color
     */
    public void setDefaultLinkVisitedColor( int color)
    {
        setDefault(
            DKEY_LINK_VISITED_COLOR,
            new Color(color)
        );
    }

    /**
     *
     * @param key The key
     * @param value the value
     */
    public void setDefault( final String key, final Object value)
    {
        if( defaults == null)
        {
            defaults = new Hashtable();
        }

        if( value != null)
        {
            defaults.put( key, value);
        }
        else
        {
            defaults.remove( key);
        }
    }

    /**
     *
     * @param vers
     */
    public void setVersion(int vers)
    {
         setDefault(
            DKEY_SKIN_VERSION, vers);
    }


    /**
     *
     */
    protected void iSetDefaults()
    {
        //setDefault( DKEY_TABLE_HEAD_BG, new Color( 0x003366));
        ///setDefault( DKEY_TABLE_HEAD_FONT_COLOR, Color.white);
        setDefault( DKEY_TABLE_CAPTION_SIZE, 1);
        setDefault( DKEY_TABLE_CAPTION_FONT_COLOR, Color.black);

//        setDefault( DKEY_FONT_SIZE, Integer.valueOf(16));
       // setDefault( DKEY_FONT_FACE,"ARIAL,Helvetica,Times");
        setDefault( DKEY_FONT_RELATIVE_SIZE, 0);
        setDefault( DKEY_FONT_COLOR, Color.black);
        setDefault( DKEY_FONT_BOLD, "FALSE");
        //setDefault( DKEY_TABLE_ODD_ROW_COLOR, new Color( 240, 240, 240));
        setDefault( DKEY_CONTROL_BG_COLOR, new Color(230, 230, 230));
        setDefault( DKEY_CONTROL_TITLE_BG_COLOR, new Color(215, 215, 215));
        setDefault( DKEY_TABLE_HIGHLIGHT_ROW_COLOR, new Color(130, 230, 130));
        setDefault( DKEY_TABLE_HEAD_BG,  new Color(0xD8D6D6));
        setDefault( DKEY_TABLE_HEAD_FONT_COLOR, Color.black);
        setDefault( DKEY_SECTION_BODY_COLOR,  new Color(230, 230, 230));

        setDefault( DKEY_PAGE_BACKGROUND_COLOR, new Color( 0xEEEEDF));
        setDefault( DKEY_PAGE_SUMMARY_BACKGROUND_COLOR, Color.white);
        //setDefault( DKEY_TABLE_HEAD_BG, new Color( 0x003366));
        //setDefault( DKEY_TABLE_HEAD_FONT_COLOR, Color.black);
        //setDefault( DKEY_TABLE_CAPTION_SIZE, Integer.valueOf(1));
        //setDefault( DKEY_TABLE_CAPTION_FONT_COLOR, Color.black);
        //setDefault( DKEY_FONT_SIZE, Integer.valueOf(14));
        //setDefault( DKEY_FONT_FACE,"ARIAL,Helvetica,Times");
        //setDefault( DKEY_FONT_RELATIVE_SIZE, Integer.valueOf(0));
        //setDefault( DKEY_FONT_COLOR, Color.black);
        //setDefault( DKEY_FONT_BOLD, "FALSE");
        setDefault( DKEY_ERROR_FONT_COLOR, Color.red);
        //setDefault( DKEY_TABLE_ODD_ROW_COLOR, new Color( 200, 200, 200));
        //setDefault( DKEY_CONTROL_BG_COLOR, new Color(240, 240, 240));
        //s//etDefault( DKEY_CONTROL_TITLE_BG_COLOR, new Color(220, 220, 220));
        //s//etDefault( DKEY_TABLE_HIGHLIGHT_ROW_COLOR, new Color(130, 230, 130));
        setDefault( DKEY_TABLE_BORDER_COLOR, "default");
        setDefault( DKEY_SECTION_HEAD_COLOR, new Color(0x006699));
        //setDefault( DKEY_SECTION_BODY_COLOR, new Color(0xCCD8DE));
        setDefault( DKEY_SECTION_HEAD_FONT_COLOR, Color.white);
        setDefault( DKEY_SECTION_HEAD_FONT_BOLD, "TRUE");
        setDefault( DKEY_SECTION_HEAD_FONT_ITALIC, "FALSE");
        setDefault( DKEY_SECTION_HEAD_FONT_FACE,"ARIAL");

        setDefault( DKEY_LINK_COLOR, new Color( 0x0099cc));

        setDefault(DKEY_LINK_ACTIVE_COLOR, new Color( 0xcc0033));

        setDefault( DKEY_LINK_VISITED_COLOR, new Color( 0x663399));

        // Skins
        setDefault( DKEY_SKIN_ID, "BASE");

        setDefault( DKEY_SKIN_VERSION, 2);

    }

    /**
     * Returns a list of changed fields.
     * @return the value
     */
    public Enumeration getChangedFields()
    {
        Hashtable table = defaults;

        if( table == null)
        {
            table = new Hashtable();
        }

        return table.keys();
    }

    /**
     * Has any changes been made to this mutable theme
     * @return the value
     */
    public boolean hasChanges()
    {
        return defaults != null;
    }
    
    /*//LEGACY_START    
    public void setDefaultButtonBorderColor( int color)
    {

    }
    *///LEGACY_END
    /*//LEGACY_START    
    public void setDefaultButtonBorderColor( Color color)
    {

    }
    *///LEGACY_END
}
