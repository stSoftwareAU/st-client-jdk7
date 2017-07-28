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

import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.CProperties;
import com.aspc.remote.util.misc.FileUtil;
import com.aspc.remote.util.misc.StringUtilities;
import java.awt.Color;
import java.io.IOException;
import java.util.Hashtable;
import org.apache.commons.logging.Log;

/**
 *  HTMLTheme.java
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       July 23, 2000, 8:01 PM
 */
public class HTMLTheme extends Object
{
    /**
     * @deprecated 
     */
    public static final Color DEFAULT_MOUSE_OVER_COLOR          =new Color( 0xFFFFD9);

    /*//LEGACY_START    
    public static final String DKEY_FONT_SIZE                   = "FONT_SIZE";
    *///LEGACY_END
    
    /**
     *
     */
    public static final String DKEY_FONT_RELATIVE_SIZE          = "FONT_RELATIVE_SIZE";
    /**
     *
     */
    public static final String DKEY_FONT_COLOR                  = "FONT_COLOR";
    /**
     *
     */
    public static final String DKEY_FONT_BOLD                   = "FONT_BOLD";
    /**
     *
     */
    public static final String DKEY_FONT_ITALIC                 = "FONT_ITALIC";

    /*//LEGACY_START    
    public static final String DKEY_FONT_FACE                   = "FONT_FACE";
    *///LEGACY_END

    /**
     *
     */
    public static final String DKEY_ERROR_FONT_COLOR            = "ERROR_FONT_COLOR";
    /**
     *
     */
    public static final String DKEY_LINK_COLOR                  = "LINK_COLOR";
    /**
     *
     */
    public static final String DKEY_LINK_BOLD                   = "LINK_BOLD";
    /**
     *
     */
    public static final String DKEY_LINK_ACTIVE_COLOR           = "LINK_ACTIVE_COLOR";
    /**
     *
     */
    public static final String DKEY_LINK_VISITED_COLOR          = "LINK_VISITED_COLOR";

    /**
     * Page attributes
     */
    public static final String DKEY_PAGE_BACKGROUND_COLOR         = "PAGE_BACKGROUND_COLOR";
    /**
     *
     */
    public static final String DKEY_PAGE_SUMMARY_BACKGROUND_COLOR = "PAGE_SUMMARY_BACKGROUND_COLOR";


    /**
    * This is the control background color ie dialog backgrounds etc.
    */
    public static final String DKEY_CONTROL_BG_COLOR            = "CONTROL_BG_COLOR";
    /**
     *
     */
    public static final String DKEY_CONTROL_TITLE_BG_COLOR      = "CONTROL_TITLE_BG_COLOR";

    /*//LEGACY_START    
    public static final String DKEY_TABLE_ODD_ROW_COLOR         = "TABLE_ODD_ROW_COLOR";
    *///LEGACY_END

    /**
     *
     */
    public static final String DKEY_TABLE_HIGHLIGHT_ROW_COLOR   = "TABLE_HIGHLIGHT_ROW_COLOR";
    /**
     *
     */
    public static final String DKEY_TABLE_BORDER_COLOR          = "TABLE_BORDER_COLOR";
    /**
     *
     */
    public static final String DKEY_TABLE_HEAD_BG               = "TABLE_HEAD_BG";
    /**
     *
     */
    public static final String DKEY_TABLE_HEAD_FONT_COLOR       = "TABLE_HEAD_FONT_COLOR";
    /**
     *
     */
    public static final String DKEY_TABLE_CAPTION_SIZE          = "TABLE_CAPTION_SIZE";
    /**
     *
     */
    public static final String DKEY_TABLE_CAPTION_FONT_COLOR    = "TABLE_CAPTION_FONT_COLOR";
    /**
     *
     */
    public static final String DKEY_SECTION_HEAD_COLOR          = "TABLE_SECTION_HEAD_COLOR";
    /**
     *
     */
    public static final String DKEY_SECTION_BODY_COLOR          = "TABLE_SECTION_BODY_COLOR";
    /**
     *
     */
    public static final String DKEY_SECTION_HEAD_FONT_COLOR     = "TABLE_SECTION_HEAD_FONT_COLOR";
    /**
     *
     */
    public static final String DKEY_SECTION_HEAD_FONT_BOLD      = "TABLE_SECTION_HEAD_FONT_BOLD";
    /**
     *
     */
    public static final String DKEY_SECTION_HEAD_FONT_ITALIC    = "TABLE_SECTION_HEAD_FONT_ITALIC";
    /**
     *
     */
    public static final String DKEY_SECTION_HEAD_FONT_FACE      = "TABLE_SECTION_HEAD_FONT_FACE";

    /**
     * Skins
     */
    public static final String DKEY_SKIN_ID                    = "SKIN_ID";
    /**
     *
     */
    public static final String DKEY_SKIN_VERSION               = "SKIN_VERSION";

    /**
     *
     */
    public static final String ICON_HELP_BOOK_CLOSE            = "ICON_HELP_BOOK_CLOSE";
    /**
     *
     */
    public static final String ICON_HELP_BOOK_OPEN             = "ICON_HELP_BOOK_OPEN";
    /**
     *
     */
    public static final String ICON_HELP_DOC_CLOSE             = "ICON_HELP_DOC_CLOSE";
    /**
     *
     */
    public static final String ICON_HELP_DOC_OPEN              = "ICON_HELP_DOC_OPEN";
    /**
     *
     */
    public static final String ICON_HELP_DOC                   = "ICON_HELP_DOC";

    /**
     *
     * @return the value
     */
    public boolean showFieldHistoryLink()
    {
        return false;
    }

    /**
     *
     * @param key The key
     * @return the value
     */
    public String getIcon( String key)
    {
        if( key.equals( ICON_HELP_BOOK_CLOSE))
        {
            return "/icons/book16.gif";
        }
        else if( key.equals( ICON_HELP_BOOK_OPEN))
        {
            return "/icons/book_open16.gif";
        }
        else if( key.equals( ICON_HELP_DOC_CLOSE))
        {
            return "/icons/help_doc_closed.gif";
        }
        else if( key.equals( ICON_HELP_DOC_OPEN))
        {
            return "/icons/help_doc_open.gif";
        }
        else if( key.equals( ICON_HELP_DOC))
        {
            //toDo reverse back
            //return "/icons/help_doc16.gif";
            return null;
        }

        return null;
    }

    /**
     *
     * @param key The key
     * @param defaultValue
     * @return the value
     */
    public int getDefaultInt( String key, int defaultValue)
    {
        Object o;

        o = getDefault( key);

        if( o == null || ( o instanceof Number) == false)
        {
            return defaultValue;
        }

        return ((Number)o).intValue();
    }

    /**
     *
     * @param key The key
     * @return the value
     */
    public int getDefaultInt( String key)
    {
        Object o;

        o = getDefault( key);

        if( o == null)
        {
            LOGGER.info(
                "WARNING - HTMLComponent.getDefaultInt('" + key + "') key not found"
            );
            return 0;
        }

        if(( o instanceof Number) == false)
        {
            LOGGER.info(
                "WARNING - HTMLComponent.getDefaultInt('" +
                    key +
                    "') key not a number {" + o + "}"
            );
            return 0;
        }

        return ((Number)o).intValue();
    }

    /**
     *
     * @param key The key
     * @return the value
     */
    public Color getDefaultColor( String key)
    {
        Object o;

        o = getDefault( key);

        if( o == null || ( o instanceof Color) == false)
        {
            if( o != null && o.toString().equals( "default"))
            {
                return null;
            }

            return null;//Color.black;
        }

        return (Color)o;
    }

    /**
     *
     * @param key The key
     * @return the value
     */
    public boolean getDefaultBoolean( String key)
    {
        Object o;

        o = getDefault( key);

        if( o == null)
        {
            return false;
        }

        if( o instanceof Boolean)
        {
            return ((Boolean)o);
        }

        if( o.toString().toUpperCase().contains("TRUE"))
        {
            return true;
        }

        return false;
    }

    /**
     *
     * @param key The key
     * @return the value
     */
    public Object getDefault( String key)
    {
        Object o = null;

        if( defaults != null)
        {
            o = defaults.get( key);
        }

        return o;
    }

    /**
     * Current version of this theme
     * @return the value
     */
    public int getVersion()
    {
        return getDefaultInt( DKEY_SKIN_VERSION);
    }

    /**
     *
     * @return the value
     */
    public int getDefaultFontRelativeSize()
    {
        return getDefaultInt( DKEY_FONT_RELATIVE_SIZE);
    }

    /**
     *
     * @return the value
     *
    public int getDefaultFontSize()
    {
        return getDefaultInt( DKEY_FONT_SIZE);
    }

    /**
     *
     * @return the value
     *
    public String getDefaultFontFace()
    {
        return new String((String)getDefault( DKEY_FONT_FACE ));
    }*/

    /**
     *
     * @return the value
     */
    public Color getDefaultFontColor()
    {
        return getDefaultColor( DKEY_FONT_COLOR);
    }

    /**
     *
     * @return the value
     */
    public boolean getDefaultFontBold()
    {
        return getDefaultBoolean( DKEY_FONT_BOLD);
    }

    /**
     *
     * @return the value
     */
    public int getDefaultButtonFontRelativeSize()
    {
        return getDefaultInt( DKEY_FONT_RELATIVE_SIZE);
    }

    /**
     *
     * @return the value
     */
    public Color getDefaultErrorFontColor()
    {
        return getDefaultColor( DKEY_ERROR_FONT_COLOR);

    }
    /**
     * Returns path of current document directory
     * @return the value
     */
    public String getDocumentDir()
    {
        return CProperties.getDocRoot();
    }

    /**
     *
     * @return the value
     */
    public static String getSkinsRootDir()
    {
        String temp = FileUtil.getCachePath();

        temp += "/skins/";

        return temp;
    }

    /**
     * Directory path which contains the skin files
     * @param addPath
     * @return the value
     * @throws java.io.IOException if an IO exception occurs.
     */
    public String makeSkinsDir(String addPath) throws IOException
    {
        String skinDir = getDefault(DKEY_SKIN_ID) + "/";

        if(StringUtilities.isBlank( addPath) == false) skinDir += addPath;

        // Create path if it does not already exist
        String fullPath = getSkinsRootDir() + skinDir;

        FileUtil.mkdirs(fullPath);

        return skinDir;
    }

    /**
     *
     */
    protected Hashtable defaults;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.html.theme.HTMLTheme");//#LOGGER-NOPMD
}
