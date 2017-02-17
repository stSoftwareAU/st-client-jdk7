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
 *  StatusBar
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED swing</i>
 *  @author      Nigel Leck
 *  @since       21 November 1996
 */
public class StatusBar extends JPanel
{
    Container        theContainer;
    
    String        theString;
    
    @Override
    public Dimension getMinimumSize()
    {
        return new Dimension(theContainer.getBounds().width, 24);
    }
    
    @Override
    public Dimension getPreferredSize()
    {
        return getMinimumSize();
    }
    
    /**
     *
     * @param inContainer
     */
    public StatusBar( Container inContainer)
    {
        theContainer = inContainer;
        
        theString = "";
        // Place status bar at the bottom of the frame
    }
    
    /**
     *
     * @param inString
     */
    public void setText( String inString)
    {
        theString = inString;
        repaint();
    }
    
    @Override
    public void paint(Graphics g)
    {
        Color color;
        color = UIManager.getColor( "Menu.background");
        if( color == null) color = Color.pink;
        
        g.setColor( color);
        Rectangle theRec;
        
        theRec = getBounds();
        g.fill3DRect(
                0,
                0,
                theRec.width - 1,
                theRec.height - 1 ,
                true
                );
        
        g.draw3DRect(
                3,
                3,
                theRec.width/3,
                theRec.height - 8,
                false
                );
        
        g.setColor( Color.black);
        
        int down,
                fontSize;
        
        down = (theRec.height / 2);
        fontSize = (g.getFontMetrics().getHeight() / 2);
        
        down += fontSize;
        
        g.setClip( 3,3,theRec.width/3,theRec.height - 8);
        
        g.drawString( theString, 5, down - 3);
    }
}
