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
import java.util.*;
import javax.swing.*;

/**
 *  CDialog Dialog standard class.
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED swing</i>
 *  @author      Nigel Leck
 *  @since       2 December 1997
 */
public class CDialog
{
    /**
     *
     */
    /**
     *
     */
    public boolean      pressOK            = false,
                        pressCancel        = false;

    /**
     *
     */
    protected Container master;

    /**
     *
     */
    /**
     *
     */
    protected Vector    message,
                        options;

    /**
     *
     */
    /**
     *
     */
    /**
     *
     */
    protected Object    defaultValue,
                        cancelValue,
                        selectedValue;

    /**
     *
     */
    protected int type;

    /**
     *
     */
    protected String title;
    /**
     *
     */
    protected boolean initialized;

    /**
     *
     */
    public CDialog()
    {
        this( "");
    }

    /**
     * 
     * @param title 
     */
    public CDialog( String title)
    {
        this(null, title);
    }

    /**
     * 
     * @param c 
     * @param title 
     */
    public CDialog(Container c, String title)
    {
        master = c;
        this.title = title;

        message = new Vector();//NOPMD
        options = new Vector();//NOPMD
        cancelValue = "Cancel";
    }


    /**
     *
     */
    protected void setDefaults()
    {
        type = JOptionPane.WARNING_MESSAGE;
        options.addElement( "Cancel");
        options.addElement( "OK");
    }

    /**
     * Return true if the user has pressed OK
     * @return the value
     */
    public boolean isOK()
    {
        return pressOK;
    }

    /**
     * 
     * @param flag 
     */
    public void setVisible( boolean flag)
    {
        if( flag == true)
        {
            Runnable r = new Runnable()
            {
                @Override
                public void run()
                {
                    showDialog();
                }
            };

            SwingUtilities.invokeLater(r);
        }
    }

    /**
     *
     */
    public void waitForUser()
    {
        showDialog();
    }

    private void showDialog()
    {
        if( initialized == false)
        {
            setDefaults();
        }

        initialized = true;

        Object list[] = new Object[ message.size()];

        for( int i = 0; i < message.size(); i++)
        {
            list[i] = message.elementAt( i);
        }

        Object optionsList[] = new Object[ options.size()];

        for( int i = 0; i < options.size(); i++)
        {
           // CButton b;

            Object o;
            o = options.elementAt( i);

            optionsList[i] = o;
        }

        JOptionPane pane = null;
        JDesktopPane desktop;

        if( master instanceof DocFrame)
        {
            desktop = ((DocFrame)master).getDesktop();
            master = desktop;
        }
        else
        {
            desktop = JOptionPane.getDesktopPaneForComponent(master);
        }

        if( desktop == null)
        {
            pane = new JOptionPane(list, type);
            pane.setOptions(optionsList);
            pane.setInitialValue(defaultValue);
        }

        pressOK            = false;
        pressCancel        = false;
        //pane.set.Xxxx(...); // Configure
        while( true)
        {
            if( desktop != null)
            {
                JPanel panel = new JPanel();
                for (Object list1 : list) {
                    JComponent c;
                    if (list1 instanceof JComponent) {
                        c = (JComponent) list1;
                    } else {
                        c = new JTextField(list1.toString());
                    }
                    panel.add( c);
                }

                Dimension d;

                d = desktop.getSize();

                //p = panel.getPreferredSize();
                int h_i = 100;

                d.height = d.height - h_i;
                d.width = d.width - h_i;

                panel.setMaximumSize( d);

                int r;

                r = JOptionPane.showInternalOptionDialog(
                    master,
                    panel,
                    title,
                    JOptionPane.DEFAULT_OPTION,
                    type,
                    null,
                    optionsList,
                    defaultValue
                );

                if( r < 0)
                {
                    selectedValue = cancelValue;
                }
                else
                {
                    selectedValue = optionsList[r];
                }

                //Fixing some bugs.
                CFrame.makeSensible();
            }
            else
            {
                JDialog dialog;

                dialog = pane.createDialog(master, title);
                dialog.pack();

                dialog.show();

                selectedValue = pane.getValue();
            }

            if(selectedValue != null)
            {
                if( validateAction() == false)
                {
                    continue;
                }
            }

            break;
        }
    }

    /**
     * 
     * @return the value
     */
    protected boolean validateAction()
    {
        if(
            selectedValue.equals( "OK") ||
            selectedValue.equals( "YES")
        )
        {
            if( performOK() == true)
            {
                pressOK = true;
            }
            else
            {
                return false;
            }
        }
        else if( selectedValue.equals( "Cancel"))
        {
            pressCancel        = true;
        }

        return true;
    }

    /**
     *  Return's true if the OK is to continue.
     * @return the value
     */
    public boolean performOK()
    {
        return true;
    }
}
