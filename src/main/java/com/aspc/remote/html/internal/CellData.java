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
package com.aspc.remote.html.internal;
import com.aspc.remote.html.style.HTMLStyleSheet;
import com.aspc.remote.memory.HashMapFactory;
import com.aspc.remote.util.misc.StringUtilities;
import java.awt.Color;
import java.util.HashMap;
import java.util.Vector;

/**
 *  CellData.java
 *
 * <i>THREAD MODE: SINGLE-THREADED html generator component</i>
 *  @author      Nigel Leck
 *  @since       November 16, 2000, 11:28 AM
 */
public class CellData
{
    /**
     * Set the inline style of this component
     * @param type the type
     * @param value the value
     */
    public void setStyleProperty( String type, String value)
    {
        if( styleProperty == null)
        {
            styleProperty = HashMapFactory.create();
        }

        if( StringUtilities.isBlank( value))
        {
            styleProperty.remove(type);
        }
        else
        {
            styleProperty.put(type, value);
        }
    }

    public String   alignment,
            toolTip,
            width,
            id,
            vAlignment,
            height;

    /**
     *
     */
    public Color    bgcolor;

    /**
     *
     */
    public HTMLStyleSheet style;
    /**
     *
     */
    public  HashMap     styleProperty;
    /**
     *
     */
    public String   imageURL;

    /**
     *
     */
    public Integer  span,
            rowSpan;
    /**
     *
     */
    private Boolean  noWrap;

    /**
     *
     */
    public Vector   events;
    /**
     *
     */
    public Object   cellTag;

    /**
     * @return the noWrap
     */
    public Boolean getNoWrap()
    {
        return noWrap;
    }

    /**
     * @param noWrap the noWrap to set
     */
    public void setNoWrap(final Boolean noWrap)
    {
        this.noWrap = noWrap;
    }
}
