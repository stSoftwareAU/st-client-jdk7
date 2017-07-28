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

import org.apache.commons.logging.Log;
import java.awt.*;
import java.awt.event.*;
import com.aspc.remote.util.misc.*;
import javax.swing.*;

/**
 *  CenterTextPanel
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED swing</i>
 *  @author      Nigel Leck
 *  @since       2 December 1996
 */
public class CenterTextPanel extends JPanel
{
    String theText;
    /**
     *
     */
    public CenterTextPanel( )
    {
        theText = "";
    }

    /**
     *
     * @param inString
     */
    public void setText( String inString)
    {
        theText = inString;
        repaint(1000);
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint( g);
        g.setColor( SystemColor.window);
        Rectangle theRec;

        theRec = getBounds();
        g.draw3DRect(
            2,
            2,
            theRec.width - 4,
            theRec.height - 4,
            false
        );

        g.setColor( Color.black);

        //int down,
        //    fontSize;

        g.drawString( theText, 5, 20);
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.application.CenterTextPanel");//#LOGGER-NOPMD
}
