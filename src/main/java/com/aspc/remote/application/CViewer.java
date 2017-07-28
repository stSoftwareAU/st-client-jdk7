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
import java.net.*;

import com.aspc.remote.util.misc.*;

/**
 *  CViewer a Image Viewer
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED swing</i>
 *  @author      Nigel Leck
 *  @since       5 December 1996
 */
public class CViewer extends Canvas
{
    /**
     *
     */
    protected Image image = null;

    /**
     * 
     * @param theFile 
     */
    public CViewer( String theFile)
    {
        setFileName( theFile);//NOPMD
    }

    /**
     * 
     * @param str 
     */
    public void setFileName(String str)
    {
        try
        {
            image = Toolkit.getDefaultToolkit().getImage(str);
            MediaTracker tracker = new MediaTracker(this);
            tracker.addImage(image, 0);
            tracker.waitForID(0);
        }
        catch(Exception e)
        {
            LOGGER.info( "Error - displaying image", e);
        }
    }

    /**
     * 
     * @param g 
     */
    @Override
    public void paint(Graphics g)
    {
        Dimension dim = getSize();
        g.clipRect(0,0,dim.width,dim.height);
        g.drawImage(image, 0, 0, this);
    }

    /**
     * 
     * @return the value
     */
    @Override
    public Dimension getPreferredSize()
    {
        if (image != null)
        {
            return (new Dimension(image.getWidth(this), image.getHeight(this)));
        }
        else
        {
            return new Dimension(0,0);
        }
    }

    /**
     * 
     * @return the value
     */
    @Override
    public Dimension getMinimumSize()
    {
        return new Dimension(30,30);
    }
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.application.CViewer");//#LOGGER-NOPMD
}
