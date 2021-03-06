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
 *  FLabel a Fixed length label
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED swing</i>
 *  @author      Nigel Leck
 *  @since       15 February 1997
 */
public class FLabel extends CPanel
{
    JLabel label;

    int width;

    /**
     * 
     * @param labelTx the label
     */
    public FLabel( String labelTx)
    {
        super( BoxLayout.Y_AXIS);
        label = new JLabel(labelTx);
        label.setAlignmentY( 0);
        CPanel aPanel = new CPanel();
        aPanel.setAlignmentX(1);
        aPanel.add( label);
        aPanel.add( Box.createHorizontalGlue() );
        add( aPanel);
        width = getCharWidth( labelTx.length());
    }

    /**
     * 
     * @return the value
     */
    @Override
    public Dimension getMinimumSize()
    {
        Dimension d;

        d = super.getMinimumSize();

        d = new Dimension( width, d.height);
        return d;
    }

    /**
     * 
     * @return the value
     */
    @Override
    public Dimension getMaximumSize()
    {
        return getMinimumSize();
    }

    /**
     * 
     * @return the value
     */
    @Override
    public Dimension getPreferredSize()
    {
        return getMinimumSize();
    }

    /**
     * 
     * @return the value
     */
    public String getText()
    {
        return label.getText();
    }

    /**
     * 
     * @param t 
     */
    public void setText( String t)
    {
        label.setText( t);
    }
}
