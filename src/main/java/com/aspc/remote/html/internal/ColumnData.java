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
package com.aspc.remote.html.internal;

import java.awt.*;

/**
 *  ColumnData
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 * @since 29 September 2006
 *  @since
 */
public class ColumnData
{
    /**
     *
     */
    public ColumnData( )
    {
        width = "";
        alignment = "";
        toolTip = "";
        verticalAlignment = "";
    }

    /**
     * 
     * @return the value
     */
    public String getWidth()
    {
        return width;
    }

    /**
     * 
     * @param flag 
     */
    public void setHidden( boolean flag)
    {
        hidden = flag;
    }
    
    /**
     * 
     * @return the value
     */
    public boolean isHidden()
    {
        return hidden;
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
     * @return the value
     */
    public Color getBackGroundColor()
    {
        return bgcolor;
    }

    /**
     * 
     * @return the value
     */
    public String getAlignment()
    {
        return alignment;
    }

    /**
     * 
     * @param bgcolor 
     */
    public void setBackGroundColor( Color bgcolor)
    {
        this.bgcolor = bgcolor;
    }

    /**
     * 
     * @param alignment 
     */
    public void setAlignment( String alignment)
    {
        this.alignment = alignment;
    }

    /**
     * 
     * @return the value
     */
    public String getToolTip()
    {
        return toolTip;
    }

    /**
     * 
     * @param tip 
     */
    public void setToolTip( String tip)
    {
        this.toolTip = tip;
    }

    /**
     * 
     * @return the value
     */
    public String getVerticalAlignment()
    {
        return verticalAlignment;
    }

    /**
     * 
     * @param verticalAlignment 
     */
    public void setVerticalAlignment( String verticalAlignment)
    {
        this.verticalAlignment = verticalAlignment;
    }

    private boolean hidden;
    
    private String  width,
                    alignment,
                    toolTip,
                    verticalAlignment;

    private Color bgcolor;
}
