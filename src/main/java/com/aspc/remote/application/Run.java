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
import com.aspc.remote.util.misc.*;
import javax.swing.SwingUtilities;

/**
 *  Run is a static class provides a method for
 *  running an application simular to Applets
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED swing</i>
 *  @author      Nigel Leck
 *  @since       2 December 1996
 */
public final class Run
{
    /**
     *
     */
    public static final int INITIAL_WIDTH = 200;
    /**
     *
     */
    public static final int INITIAL_HEIGHT = 100;

    /**
     *
     * @param className
     */
    public static void application( String className)
    {
        application( className, null);
    }

    /**
     *
     * @param className
     * @param args The command line arguments
     */
    public static void application( final String className, final String []args)
    {
        try
        {
            call( className, args);
        }
        catch( Throwable t)
        {
            LOGGER.error( "application: " + className, t);
        }
/*        
                                synchronized( theApp)
                        {
                            while( true)
                            {
                                theApp.wait( 10000);

                                if( theApp.isQuiting())
                                {
                                    break;
                                }   
                            }
                        }
*/
    }
    
    private static void call( final String className, final String []args) throws Exception
    {        
        SwingUtilities.invokeAndWait(
            new Runnable() 
            {            
            @SuppressWarnings("empty-statement")
            @Override
                public void run() 
                {
                    Label label = null;
                    Frame frame = null;
                    try
                    {
                        CApp theApp;
                        frame = new Frame( "Loading");

                        frame.setSize(INITIAL_WIDTH, INITIAL_HEIGHT);
                        CUtilities.center( null, frame);

                        frame.setBackground(Color.lightGray);
                        frame.setLayout( new BorderLayout());
                        label = new Label( "Loading classes");
                        label.setAlignment(Label.CENTER);
                        frame.add( "Center", label);
                        frame.setVisible(true);

                        try
                        {
                            theApp = (CApp) (Class.forName( className).newInstance());
                            //Class.forName( "Application.CFrame");
                            //Class.forName( "Application.CDialog");
                        }
                        catch( Exception e)
                        {
                            LOGGER.info("Error running '" + className + "'", e);
                            return;
                        }

                        if( args != null)
                        {
                            label.setText("Parsing args...");
                            theApp.handleArgs( args);
                        }

                        label.setText("Init...");
                        theApp.init();
                        label.setText("Start...");

                        theApp.start();

                        //label.setText("Loading Screen...");

                        frame.setVisible(false);
                    }
                    catch( Throwable t)
                    {
                        LOGGER.error( className, t);
                        if( label != null)
                        {
                            label.setText( t.toString());
                            try
                            {
                                Thread.sleep( 3000);
                            }
                            catch( Exception e)
                            {
                                ;
                            }
                        }

                        if( frame != null)
                        {
                            frame.setVisible(false);
                        }
                        System.exit( 1);
                    }
                }
            }
        );
    }

    private Run()
    {        
    }
    
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.application.Run");//#LOGGER-NOPMD
}
