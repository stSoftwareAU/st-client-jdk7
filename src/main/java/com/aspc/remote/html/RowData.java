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
import java.awt.*;

/**
 *  RowData
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       26 August 1998
 */
class RowData
{
    public RowData()
    {
        width = "";
        align = "";
        toolTip = "";
        verticalAlign = "";
    }

    public String getWidth()
    {
        return width;
    }

    public void setWidth( String width)
    {
        this.width = width;
    }

    public Color getBackgroundColor()
    {
        return bgcolor;
    }

    public String getAlign()
    {
        return align;
    }

    public void setBackgroundColor( Color bgcolor)
    {
        this.bgcolor = bgcolor;
    }

    public void setAlign( String align)
    {
        this.align = align;
    }

    public String getToolTip()
    {
        return toolTip;
    }

    public void setToolTip( String tip)
    {
        this.toolTip = tip;
    }

    public String getVerticalAlign()
    {
        return verticalAlign;
    }

    public void setVerticalAlign( String verticalAlign)
    {
        this.verticalAlign= verticalAlign;
    }

    private String  width,
                    align,
                    toolTip,
                    verticalAlign;

    private Color bgcolor;
}
