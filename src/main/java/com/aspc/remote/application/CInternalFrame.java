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
import javax.swing.*;
import javax.swing.event.*;
import com.aspc.remote.util.misc.*;
import java.awt.*;
import java.util.*;

/**
 *  CInternalFrame
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED swing</i>
 *  @author      Nigel Leck
 *  @since       June 18, 1998, 1:42 PM
 */
public class CInternalFrame extends JInternalFrame implements InternalFrameListener
{
    /**
     *
     */
    protected CFrame master;
    /**
     *
     */
    public CInternalFrame thisFrame;

    /**
     *
     */
    public CInternalFrame( )
    {
        this( "**** Under Construction ****", null);
    }

    /**
     *
     * @param title
     * @param master
     */
    public CInternalFrame(String title, CFrame master)
    {
        this.master = master;
        setTitle(title);
        thisFrame = this;

        addInternalFrameListener( this);
        setDefaultLocation();//NOPMD
    }

    /**
     *
     * @param flag
     */
    public void invokeBusy( boolean flag)
    {
        Runnable r;

        if( flag == true)
        {
            r = new Runnable( )
            {
                @Override
                public void run()
                {
                    setBusy( true);
                }
            };
        }
        else
        {
            r = new Runnable( )
            {
                @Override
                public void run()
                {
                    setBusy( false);
                }
            };
        }

        try
        {
            SwingUtilities.invokeLater( r);
        }
        catch( Exception e)
        {
            LOGGER.info( "invoke and wait", e);
        }
    }

    /**
     *
     * @param flag
     */
    public void setBusy( boolean flag)
    {
        CApp.checkSwingValid( this);
        if( flag == true)
        {
            getGlassPane().setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR));
        }
        else
        {
            getGlassPane().setCursor( Cursor.getDefaultCursor());
        }
    }

    @Override
    public void internalFrameActivated(InternalFrameEvent e)
    {
    }

    @Override
    public void internalFrameClosed(InternalFrameEvent e)
    {
    }

    @Override
    public void internalFrameClosing(InternalFrameEvent e)
    {
        doClose();
    }

    private boolean doCloseCalled;
    /**
     *
     */
    public void doClose()
    {
        // bug fix - JDK 1.3 stack overflow
        if( doCloseCalled == true)
        {
           return;
        }
        else
        {
           doCloseCalled = true;
        }
        Properties p;

        String      name,
                    value;

        Rectangle r;

        p = System.getProperties();

        setVisible(false);         // hide the Frame

        r = getBounds();
        value = r.x + "," + r.y + "," + r.width + "," + r.height;

        name = getKey();
        name = "FRAME_RECTANGLE." + name;

        p.put( name, value);

        System.setProperties (p);
        try
        {
            setClosed(true);
        }
        catch( Exception e)
        {
            LOGGER.info( "closing", e);
        }
    }

    /**
     *
     * @return the value
     */
    protected boolean isAllowedToClose()
    {
        return true;
    }

    @Override
    public void internalFrameDeactivated(InternalFrameEvent e)
    {
    }

    @Override
    public void internalFrameDeiconified(InternalFrameEvent e)
    {
    }

    @Override
    public void internalFrameIconified(InternalFrameEvent e)
    {
    }

    @Override
    public void internalFrameOpened(InternalFrameEvent e)
    {
    }

    /**
     *
     * @return the value
     */
    public String getDefaultLocationPos()
    {
        return "10,10,600,400";
    }

    /**
     *
     */
    public void setDefaultLocation()
    {
        String temp;
        temp = getKey();
        temp = System.getProperty(
            "FRAME_RECTANGLE." + temp
        );
    //LOGGER.info( "Shown " + temp);

        if( temp == null)
        {
            temp = getDefaultLocationPos();
        }

        try
        {
            StringTokenizer st = new StringTokenizer(temp,",");
            int x = 0, y = 0, w = 0, h = 0;
            for(int i = 0; st.hasMoreTokens(); i++)
            {
                 temp = st.nextToken();
                 int j = Integer.parseInt( temp);
                 switch( i)
                 {
                     case 0:
                        x = j;
                        break;
                     case 1:
                        y = j;
                        break;
                     case 2:
                        w = j;
                        break;
                     case 3:
                        h = j;
                        break;
                     default:
                }
            }
            setBounds( x, y, w, h);
        }
        catch( Exception ex)
        {
            LOGGER.info( "Error - restoring internal frame bounds", ex);
        }
    }
    /**
     *
     * @return the value
     */
    public String getKey()
    {
        String temp = "";

        if( master != null)
        {
            String t;
            t = (master).getTitle();

            int i;
            i = t.indexOf( "-");
            if( i != -1 )
            {
                t = t.substring( 0, t.length() - i + 1);
            }
            t = t.trim();
            temp =  t + ".";
        }

        temp += getTitle().trim();
        temp = temp.replace( ' ', '_');

        return temp.toUpperCase();
    }
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.application.CInternalFrame");//#LOGGER-NOPMD
}
