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
 *  CQuestion asks a question
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED swing</i>
 *  @author      Jason McGrath
 *  @since       25 March 2002
 */
public class CCopyWrite extends CDialog
{
    String notice;
    CPanel info;

    /**
     * 
     * @param inFrame 
     * @param notice 
     */
    public CCopyWrite (Container inFrame, String notice)
    {
        super(inFrame, "Copyright Notice");

        this.notice = notice;

        setUp();//NOPMD
    }

    /**
     * 
     * @return the value
     */
    @Override
    public boolean isOK()
    {
        if( selectedValue.equals( "Accept"))
        {
           return true;
        }
        return false;
    }

    /**
     *
     */
    protected void setUp()
    {
        info = new CPanel();
        info.setPreferredSize( new Dimension(400,400));
        info.setLayout( new BorderLayout());
        message.addElement( info);

        JLabel cl = new JLabel( notice);
        JScrollPane pane = new JScrollPane( cl);
        info.add( "Center", pane);
    }

    /**
     *
     */
    @Override
    protected void setDefaults()
    {
        type = JOptionPane.PLAIN_MESSAGE;
        options.addElement( "Accept");
        options.addElement( "Cancel");

        defaultValue = "Accept";
        cancelValue = "Cancel";
    }
}
