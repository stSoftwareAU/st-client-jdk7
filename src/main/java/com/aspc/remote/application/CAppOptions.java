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

import org.apache.commons.logging.Log;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.aspc.remote.util.misc.*;
import javax.swing.UIManager.*;

/**
 *  CAppOptions sets the user's options
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED swing</i>
 *  @author      Nigel Leck
 *  @since       13 February 1997
 */
public class CAppOptions extends CDialog implements ActionListener
{
    /**
     *
     */
    protected JTabbedPane tabPane;
    /**
     *
     */
    protected JComboBox   UIList;

    /**
     *
     */
    protected boolean changedUI;

    /**
     *
     */
    public CAppOptions()
    {
        init();
    }

    /**
     * 
     * @param c 
     */
    public CAppOptions(Container c)
    {
        super(c, "Options");
        init();
    }

    private void init()
    {
        tabPane = new JTabbedPane( );
        message.addElement(tabPane);

        CPanel general = new CPanel();

        UIList = new JComboBox();

        UIManager.LookAndFeelInfo[] list;

        list = UIManager.getInstalledLookAndFeels();

        String current;

        current = UIManager.getLookAndFeel().getName();
        int current_i =0;
        for( int i = 0; i < list.length; i++)
        {
            String name;
            name = list[i].getName();
            UIList.addItem( name);
            if( name.equals( current))
            {
                current_i = i;
            }
        }

        UIList.setSelectedIndex(current_i);

        UIList.addActionListener( this);
        LabelPanel lp = new LabelPanel("Look & Feel", UIList);

        general.add( lp );
        tabPane.addTab( "General", general);
    }

    /**
     * 
     * @param event the event
     */
    @Override
    public void actionPerformed(ActionEvent event)
    {
        if( event.getSource() == UIList)
        {
            changedUI = true;
        }
    }

    /**
     * 
     * @return the value
     */
    @Override
    public boolean performOK( )
    {
        if( changedUI == true)
        {
            final PleaseWait pw = new PleaseWait( master, "Updating UI...", true);
            Runnable r = new Runnable()
            {
                @Override
                public void run()
                {
                    String name;

                    name = UIList.getSelectedItem().toString();

                    UIManager.LookAndFeelInfo[] list;

                    list = UIManager.getInstalledLookAndFeels();

                    for (LookAndFeelInfo list1 : list) {
                        if (list1.getName().equals(name)) {
                            try {
                                String cn;
                                cn = list1.getClassName();
                                UIManager.setLookAndFeel(cn);
                                pw.setText( "Updating components...");
                                SwingUtilities.updateComponentTreeUI(master);
                                Window w;
                                w = SwingUtilities.windowForComponent( master);
                                if( w instanceof JFrame)
                                {
                                    JMenuBar bar;
                                    bar = ((JFrame)w).getJMenuBar();
                                    if( bar != null)
                                    {
                                        pw.setText( "Updating menu...");
                                        SwingUtilities.updateComponentTreeUI(bar);
                                    }
                                }
                                pw.setText( "Updating properties...");
                                System.getProperties().put( "DEFAULT.UI", cn);
                            }catch( Exception e)
                            {
                                LOGGER.info( "Exception", e);
                            } finally {
                                pw.setVisible( false);
                            }
                            return;
                        }
                    }
                }
            };

            SwingUtilities.invokeLater( r);
        }

        return super.performOK();
    }
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.application.CAppOptions");//#LOGGER-NOPMD
}
