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
package com.aspc.remote.application;
import java.awt.*;
import javax.swing.*;

/**
 *  LabelPanel
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED swing</i>
 *  @author      Nigel Leck
 *  @since       30 January 1997
 */
public class LabelPanel extends JPanel
{
    JComponent          component;
    FLabel              label                = null;

    /**
     * 
     * @param labelNm 
     * @param component 
     */
    public LabelPanel( String labelNm, JComponent component)
    {
        //super(true);
        setOpaque(true);
        this.component = component;
        component.setAlignmentX(0);
        add( component);

        setTitle(labelNm);//NOPMD
        validate();
        setAlignmentX(0);

        repaint();
    }

    // Methods
    @Override
    public void requestFocus()
    {
        component.requestFocus();
    }


    /**
     * 
     * @return the value
     */
    public String getTitle( )
    {
        String title = "";
        if( label != null)
        {
            title = label.getText();
        }

        return title;
    }

    /**
     * 
     * @return the value
     */
    @Override
    public Dimension getMinimumSize()
    {
        return getPreferredSize();
    }

    /**
     * 
     * @return the value
     */
    @Override
    public Dimension getMaximumSize()
    {
        return getPreferredSize();
    }

    /**
     * 
     * @param inTitle 
     */
    public void setTitle( String inTitle)
    {
        if( label != null)
        {
            remove( label);
        }

        if( inTitle.equals( "") == false)
        {
            label = new FLabel( inTitle);
            remove(component);
            label.setAlignmentY(0);

            add( label);
            add( component);
        }
    }
}
