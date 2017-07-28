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
package com.aspc.remote.application;
import java.awt.*;
import javax.swing.*;

/**
 *  CPanel
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED swing</i>
 *  @author      Nigel Leck
 *  @since       29 January 1997
 */
public class CPanel extends JPanel
{
    /**
     * 
     * @param axis 
     */
    public CPanel( int axis)
    {
        setLayout( new BoxLayout( this, axis));
        setAlignmentX(0);
    }

    /**
     *
     */
    public CPanel()
    {
        this( BoxLayout.X_AXIS);
    }

    /**
     * 
     * @param chars 
     * @return the value
     */
    public int getCharWidth( int chars)
    {
        int w;

        FontMetrics fm;
        Font f;
        f = getFont();
        fm = Toolkit.getDefaultToolkit().getFontMetrics( f);
        w = fm.charWidth('w') * chars;

        return w;
    }

    /**
     * 
     * @param inRect 
     * @param center 
     * @return the value
     */
    public Rectangle getProportionalRect( Rectangle inRect, boolean center)
    {
        Rectangle   result;

        Dimension   dim;

        double yStretch,
               xStretch;

        result = new Rectangle(0,0,0,0);

        dim = getMinimumSize();

        yStretch = (double)inRect.height/ (double)dim.height;
        xStretch = (double)inRect.width/ (double)dim.width;

        if( yStretch < xStretch)
        {
            result.width = (int)(dim.width * yStretch);
            result.height = inRect.height;
            if( center == true)
            {
                result.x = (inRect.width - result.width) / 2;
            }
        }
        else
        {
            result.height = (int)(dim.height * xStretch);
            result.width = inRect.width;
            if( center == true)
            {
                result.y = (inRect.height - result.height) / 2;
            }
        }

        return result;
    }

    /**
     * 
     * @param center 
     * @return the value
     */
    public Rectangle getProportionalRect( boolean center)
    {
            Rectangle bounds;

        bounds = getBounds();

        return getProportionalRect( bounds, center);
    }

    /**
     * 
     * @param in 
     * @return the value
     */
    public int xInt( int in)
    {
        int result;
        Rectangle r;

        Dimension   dim;

        r = getProportionalRect( true);

        dim = getMinimumSize();

        result = r.x + (int)(r.width * ( (float) in/ (float)dim.width));

        return result;
    }

    /**
     * 
     * @param in 
     * @return the value
     */
    public int yInt( int in)
    {
        int result;
        Rectangle r;

        Dimension   dim;

        r = getProportionalRect( true);

        dim = getMinimumSize();

        result = r.y + (int)(r.height * ( (float) in/ (float)dim.height));

        return result;
    }
}
