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
public class PleaseWait implements ActionListener
{
    CenterTextPanel text;
    Component master;
    JInternalFrame dialog;
    CButton cancelButton;

    /**
     * 
     * @param master 
     * @param message the message
     */
    public PleaseWait(Component master, String message)
    {
        this( master, message, true);
    }

    /**
     * 
     * @param master 
     * @param message the message
     * @param visible 
     */
    public PleaseWait(Component master, String message, boolean visible)
    {
        this.master = master;

        text = new CenterTextPanel();
        cancelButton = new CButton( "Cancel");
        cancelButton.setVisible( false);
        cancelButton.addActionListener( this);

        setVisible(visible);//NOPMD

        text.setText( message);
    }

    /**
     * 
     * @param event the event
     */
    @Override
    public void actionPerformed(ActionEvent event)
    {
        if( event.getSource() == cancelButton)
        {
            cancelButton.setEnabled( false);
            return;
        }
    }

    /**
     * 
     * @param listener 
     */
    public void addCancelListener( ActionListener listener)
    {
        final ActionListener aListener = listener;

        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {

                cancelButton.addActionListener( aListener);
                cancelButton.setVisible( true);
                if( dialog != null)
                {
                    dialog.pack();
                }
            }
        };

        CApp.invokeIfNeeded( r, null);
    }

    /**
     * 
     * @param inText 
     */
    public void setText( String inText)
    {
        text.setText( inText);
        //if( master instanceof CFrame)
      //  {
     //       ((CFrame)master).status.setText( inText);
      //  }
    }

    /**
     * 
     * @param flag 
     */
    public void setVisible(boolean flag)
    {
        try
        {
            if(flag == false)
            {
                if( dialog != null)
                {
                //    if( master instanceof CFrame)
                 //   {
                //        ((CFrame)master).status.setText( "");
               //     }

                    if( master instanceof JFrame)
                    {
                        ((JFrame)master).getGlassPane().setCursor(Cursor.getDefaultCursor());
                    }
                    dialog.setClosed( true);
                    dialog = null;
                    //Fixing some bugs.
                    CFrame.makeSensible();
                }
            }
            else
            {
                Container parent;

                if( master instanceof DocFrame)
                {
                    parent = ((DocFrame)master).getDesktop();
                }
                else if(
                    master instanceof JInternalFrame &&
                    cancelButton.isVisible() == false
                )
                {
                    parent = ((JInternalFrame) master).getLayeredPane();
                }
                //else
                //{
                //    parent = JOptionPane.getDesktopPaneForComponent(master);
               // }
                else
                {
                    parent = (Container)master;
                }

                //if( parent == null)
                //{
                //    LOGGER.info( "Please Wait not displaied");
                //    return;
                //}
                dialog = new JInternalFrame("Please Wait", false, false,false, false);
                text.setPreferredSize( new Dimension(150, 40));
                dialog.getContentPane().add(text, BorderLayout.CENTER);
                dialog.getContentPane().add(cancelButton, BorderLayout.SOUTH);
                dialog.pack();
                cancelButton.setEnabled( true);

                Dimension            iFrameSize = dialog.getPreferredSize();
                Dimension            rootSize = parent.getSize();

                dialog.setBounds((rootSize.width - iFrameSize.width) / 2,
                                 (rootSize.height - iFrameSize.height) / 2,
                                 iFrameSize.width, iFrameSize.height);

                if(parent instanceof JLayeredPane)
                {
                    parent.add(dialog, JLayeredPane.MODAL_LAYER);
                }
                else
                {
                    parent.add(dialog, BorderLayout.CENTER);
                }

                parent.validate();

                dialog.setVisible(true);
                
                try
                {
                    dialog.setSelected(true);
                }
                catch (java.beans.PropertyVetoException e)
                {
                    ;
                }

                if( master instanceof JFrame)
                {
                    ((JFrame)master).getGlassPane().setCursor(
                        Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR )
                    );
                }
            }
        }
        catch( Exception e)
        {
            LOGGER.info( "Error in 'Please wait'", e);
        }
    }
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.application.PleaseWait");//#LOGGER-NOPMD
}
