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

import com.aspc.remote.util.misc.*;

/**
 *  GridPanel
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED swing</i>
 *  @author      Nigel Leck
 *  @since       2 January 1997
 */
public class GridPanel extends JPanel
{
    /**
     *
     */
    public  GridBagConstraints   constraints;
    private GridBagConstraints   defaults;

    GridBagLayout               gridbag;

    private int width,
                height;

    /**
     * 
     * @param w 
     * @param h 
     */
    public GridPanel( int w, int h)
    {
        height        = h;
        width                = w;
        gridbag                = new GridBagLayout();
        constraints        = new GridBagConstraints();
        setLayout(gridbag);

        if( h > 0 && w > 0)
        {
            setSize( w, h);
        }

        constraints.weightx                = 100;//0.5;
        constraints.weighty                = 100;//0.5;
        constraints.anchor                = GridBagConstraints.CENTER;
        constraints.fill                = GridBagConstraints.BOTH;
        constraints.gridwidth                = 1;
        constraints.gridheight                = 1;
        constraints.insets.top                = 5;
        constraints.insets.right        = 5;
        constraints.insets.left                = 5;
        constraints.insets.bottom        = 5;
        setDefaults();//NOPMD
    }

    /**
     *
     */
    public GridPanel()
    {
        this(0, 0);
    }

    /**
     *
     */
    public void setDefaults()
    {
        defaults = (GridBagConstraints)constraints.clone();
    }

    /**
     *
     */
    public void restoreDefaults()
    {
        constraints = (GridBagConstraints)defaults.clone();
    }

    /**
     * 
     * @param inComponent 
     */
    public void addToPanel( Component inComponent )
    {
        gridbag.setConstraints(inComponent, constraints);
        add( inComponent);
    }

    /**
     * 
     * @return the value
     */
    @Override
    public Dimension getMinimumSize()
    {
        if( width != 0)
        {
            return new Dimension(width, height);
        }
        else
        {
            return super.getMinimumSize();
        }
    }

    /**
     * 
     * @return the value
     */
    @Override
    public Dimension getPreferredSize()
    {
        if( width != 0)
        {
            return getMinimumSize();
        }
        else
        {
            return super.getPreferredSize();
        }
    }
}
