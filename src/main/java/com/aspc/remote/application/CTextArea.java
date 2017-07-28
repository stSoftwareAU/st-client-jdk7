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
 *  CTextArea
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED swing</i>
 *  @author      Nigel Leck
 *  @since       12 December 1996
 */
public class CTextArea extends JPanel
{
    JTextArea text;
    JScrollPane scrollPane;

    /**
     *
     */
    public CTextArea()
    {
        text = new JTextArea();
        scrollPane = new JScrollPane(text);
        scrollPane.setDoubleBuffered(true);
        setLayout( new BorderLayout());
        add("Center", scrollPane);
    }

    /**
     * 
     * @return the value
     */
    public String getText()
    {
        return text.getText();
    }

    /**
     * 
     * @param font 
     */
    @Override
    public void setFont( Font font)
    {
        if( text != null)
        {
            text.setFont( font);
        }
    }

    /**
     * 
     * @param s 
     */
    public void setText( String s)
    {
        text.setText( s);
    }

    /**
     * 
     * @param flag 
     */
    public void setEditable( boolean flag)
    {
        text.setEditable( flag);
    }

    /**
     * 
     * @param flag 
     */
    @Override
    public void setEnabled( boolean flag)
    {
        text.setEnabled( flag);
    }

    /**
     * 
     * @return the value
     */
    public JTextArea getJTextArea()
    {
        return text;
    }

    /**
     * 
     * @return the value
     */
    public JScrollPane getJScrollPane()
    {
        return scrollPane;
    }
}
