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

import com.aspc.remote.util.misc.CUtilities;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 *  HelpUtil
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED swing</i>
 *  @author      Nigel Leck
 *  @since       23 June 1997
 */
public final class HelpUtil
{
    private HelpUtil()
    {
    }

    public static void register( final JFrame jFrame)
    {
        JMenuBar menuBar = jFrame.getJMenuBar();


        JMenu helpMenu=null;

        int count = menuBar.getMenuCount();

        for( int i = 0; i < count;i++)
        {
            JMenu menu = menuBar.getMenu(i);

            if( menu.getText().startsWith("Help"))
            {
                helpMenu = menu;
            }
        }

        if( helpMenu == null)
        {
            helpMenu = new JMenu( "Help");
            helpMenu.setMnemonic('H');
            menuBar.add(helpMenu);
        }
        Component[] menuComponents = helpMenu.getMenuComponents();
        
        for( Component c : menuComponents)
        {
            if( c instanceof JMenuItem)
            {
                JMenuItem mi = ( JMenuItem)c;
                
                if( mi.getText().startsWith("About"))
                {
                    helpMenu.remove(mi);
                }
            }
        }

        JMenuItem aboutMItem;
        aboutMItem = new JMenuItem("About...");
        aboutMItem.setMnemonic('T');

        aboutMItem.addActionListener(
            new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    AboutDialog ad = new AboutDialog(jFrame, true);
                    CUtilities.center(jFrame, ad);
                    ad.setVisible(true);
                }
            }
        );
        helpMenu.add( aboutMItem);
    }
}
