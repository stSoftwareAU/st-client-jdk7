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
import java.util.*;

import com.aspc.remote.util.misc.*;

/**
 *  Printer
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED swing</i>
 *  @author      Nigel Leck
 *  @since       2 February 1997
 */
public class Printer
{
    Frame frame;


    /**
     * 
     * @param inFrame 
     */
    public Printer( Frame inFrame)
    {
            frame = inFrame;
    }

    /**
     * 
     * @return the value
     */
    public PrintJob getJob()
    {
        Properties props;
        PrintJob job;
        props = new Properties();

        job = Toolkit.getDefaultToolkit().getPrintJob(
            frame,
            "Print " + frame.getTitle(),
            props
        );

        return job;
    }

    /**
     * 
     * @param c 
     */
    public void print( Component c)
    {
        PrintJob job;

        try
        {
            job = getJob();

            if( job == null) return;

            Graphics g;

            g = job.getGraphics();

            LOGGER.debug( "Printing " + c);

            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g.drawString("Test Printer", 100, 100);

            ((Container)c).print( g);

            g.dispose();
        }
        catch( Exception e)
        {
            LOGGER.info("printing", e);
        }
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.application.Printer");//#LOGGER-NOPMD
}
