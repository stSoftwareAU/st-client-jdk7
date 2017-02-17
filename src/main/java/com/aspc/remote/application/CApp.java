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
import com.aspc.remote.util.misc.*;
import java.awt.Component;
import java.awt.Toolkit;
import java.io.*;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import javax.swing.JApplet;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *  CApp the top level application class
 *
 * <br>
 * <i>THREAD MODE: SINGLETON SINGLE-THREADED command line application</i>
 *
 *  @author      Nigel Leck
 *  @since       1 September 1996
 */
public class CApp extends JApplet
{
    private static final long serialVersionUID = 42L;

    /**
     *
     */
    public static CApp gApp;

    private static Date startTime;
    private boolean quiting;

    /**
     *
     */
    public CApp()
    {
        AppCmdLine.setDefaults();
    }

    /**
     * 
     * @return the value
     */
    public boolean isQuiting()
    {
        return quiting;
    }

    /**
     * 
     * @param r 
     */
    public static void swingInvokeAndWait( Runnable r)
    {
        try
        {
            if( isSwingThreadSafe( null) == false)
            {
                SwingUtilities.invokeAndWait( r);
            }
            else
            {
                r.run();
            }
        }
        catch( Throwable e)
        {
            LOGGER.error( "Thread is '" + Thread.currentThread() + "'", e);
        }
    }

    /**
     * 
     * @param c 
     * @return the value
     */
    public static boolean isSwingThreadSafe( Component c)
    {
        if( !Thread.currentThread().getName( ).contains("AWT-EventQueue"))
        {
            if( c == null || c.isVisible() == true)
            {
                return false;
            }
        }

        return true;
    }

    /**
     * 
     * @param r 
     * @param c 
     */
    public static void invokeIfNeeded( Runnable r, Component c)
    {
        if( CApp.isSwingThreadSafe( c))
        {
            r.run();

            return;
        }

        try
        {
            SwingUtilities.invokeAndWait( r);
        }
        catch( Exception e)
        {
            LOGGER.info( "invoke and wait", e);
        }
    }

    /**
     * 
     * @param c 
     */
    public static void checkSwingValid(Component c)
    {
        if( isSwingThreadSafe( c) == false )
        {
            RuntimeException rt;

            rt = new RuntimeException(
                "Swing component called in a non-thread safe manner"
            );

            LOGGER.info( "error", rt);

            Toolkit.getDefaultToolkit().beep();
        }
    }


    @Override
    public void init()
    {
        gApp = this;

        startTime = new Date();

        loadProperties();

        String  cn,
        uiKey = "DEFAULT.UI";

        cn = CProperties.getProperty( uiKey);

        if( cn != null)
        {
            try
            {
                UIManager.setLookAndFeel(cn);
            }
            catch( Exception e)
            {
                LOGGER.error("Error Loading L&F", e);
                System.getProperties().remove( uiKey);
            }
        }
        /*else
        {
            try
            {
                String uiClassName = UIManager.getSystemLookAndFeelClassName();
                UIManager.setLookAndFeel(uiClassName);
            }
            catch (Exception e)
            {
                LOGGER.error("Error Loading L&F", e);
            }
        }*/
    }

    /**
     * 
     * @return the value
     */
    public static Date getStartTime()
    {
        return startTime;
    }

    @Override
    public void start()
    {
        super.start();
        //try
        //{
        //    SwingUtilities.invokeLater(
        //        new Runnable()
        //        {
        //            public void run()
        //            {
                        showMainScreen();
        //            }
        //        }
       //     );
       // }
       // catch(Exception e)
       // {
       //     LOGGER.info( "starting applet", e);
       // }
    }

    /**
     *
     */
    public void showMainScreen()
    {
    }

    /**
     * 
     * @param args The command line arguments
     */
    public void handleArgs( String []args)
    {
    }

    private File getPrefFile( boolean createDir) throws IOException
    {
        File file;
        String fn;

        fn = StringUtilities.replace(
            this.getClass().getName(),
            ".",
            "_"
        );

        String prefDir;

        if( CProperties.isUnix())
        {
            prefDir = System.getProperties().getProperty( "user.home", "/tmp");
        }
        else
        {
            prefDir = "c:/";
        }

        file = new File(
            prefDir +
            "/pref"
        );

        if( createDir)
        {
            FileUtil.mkdirs(file);
        }
        file = new File( file, fn + ".prf");

        return file;
    }


    private void loadProperties()
    {
        Properties p = new Properties();
        boolean running1_3 = false;

        Class prefClass = null;
        try
        {
            prefClass = Class.forName( "java.util.prefs.Preferences");
        }
        catch( ClassNotFoundException nf)
        {
            // Running 1.3
            running1_3 = true;
        }



        try
        {
            // Handle backward compatability where pref were stored in files
            File fl = getPrefFile( false);
            if( running1_3 || fl.exists())
            {
                // set up new properties object from file "myProperties.txt"
                FileInputStream propStream;
                propStream = new FileInputStream( fl);

                try
                {
                    p.load(propStream);
                }
                finally
                {
                    propStream.close();
                }

            }
            else
            {

                Class parameterTypes[] =
                { getClass().getClass() };

                Method m = null;

                Object args[] =
                { this.getClass()};
                m = prefClass.getMethod( "userNodeForPackage", parameterTypes);

                Object prefs;
                prefs = m.invoke( null, args);

                parameterTypes = new Class[0];
                m = prefClass.getMethod( "keys", parameterTypes);

                args = new Object[0];
                String keys[] = (String[])m.invoke( prefs, args);

                parameterTypes = new Class[2];
                Class strClass = Class.forName( "java.lang.String");
                parameterTypes[0] = strClass;
                parameterTypes[1] = strClass;
                m = prefClass.getMethod( "get", parameterTypes);

                for (String key : keys) {
                    args = new Object[2];
                    args[0] = key;
                    args[1] = "";
                    String val = (String)m.invoke( prefs, args);
                    p.put(key, val);
                }
                /*
                Preferences prefs = Preferences.userNodeForPackage(this.getClass());
                String keys[] = prefs.keys();
                for( int i=0; i<keys.length; i++)
                {
                p.put( keys[i],  prefs.get( keys[i], ""));
                } */
            }

            Properties sp = System.getProperties();
            // load properties that have not already been set
            Enumeration e = p.keys();
            while( e.hasMoreElements())
            {
                String key = (String)e.nextElement();
                if( sp.containsKey( key) == false)
                {
                    System.setProperty( key, (String)p.get(key));
                }
            }

            // Delete old system file
            if( running1_3 == false && fl.exists())
            {
                FileUtil.deleteAll(fl);
            }
        }
        catch (Exception e)
        {
            LOGGER.warn("Can't load defaults ", e);
        }
    }

    private void saveProperties()
    {
        Enumeration e;
        //StringUtilities su = new StringUtilities();

        boolean running1_3 = false;

        Class prefClass = null;
        try
        {
            prefClass = Class.forName( "java.util.prefs.Preferences");
        }
        catch( ClassNotFoundException nf)
        {
            // Running 1.3
            running1_3 = true;
        }

        if( running1_3 == false)
        {
            try
            {
                Class parameterTypes[] =
                { getClass().getClass() };

                Method m = null;

                Object args[] =
                { this.getClass()};
                m = prefClass.getMethod( "userNodeForPackage", parameterTypes);

                Object prefs;
                prefs = m.invoke( null, args);

                parameterTypes = new Class[2];
                Class strClass = Class.forName( "java.lang.String");
                parameterTypes[0] = strClass;
                parameterTypes[1] = strClass;
                m = prefClass.getMethod( "put", parameterTypes);

                Properties p = System.getProperties();
                e = p.propertyNames();
                while( e.hasMoreElements())
                {
                    String key = (String)e.nextElement();

                    // Only Write the UPPER case ones
                    if( key.equals( key.toUpperCase()) == true)
                    {
                        args = new Object[2];
                        args[0] = key;
                        args[1] = p.getProperty(key);
                        m.invoke( prefs, args);
                    }
                }

                /*
                Preferences prefs = Preferences.userNodeForPackage(this.getClass());

                Properties p = System.getProperties();
                enum = p.propertyNames();
                while( enum.hasMoreElements())
                {
                    String key = (String)enum.nextElement();
                    // Only Write the UPPER case ones
                    if( key.equals( key.toUpperCase()) == true)
                    {
                        prefs.put( key , p.getProperty(key));
                    }
                }*/
            }
            catch (Exception ex)
            {
                LOGGER.warn("Couldn't Save save properties", ex);
            }
        }
        else
        {

            try
            {
                File file = getPrefFile( true);
                try (PrintWriter out = new PrintWriter( new FileOutputStream( file))) {
                    Properties p = System.getProperties();
                    e = p.propertyNames();
                    while( e.hasMoreElements())
                    {
                        String key = (String)e.nextElement();
                        // Only Write the UPPER case ones
                        if( key.equals( key.toUpperCase()) == true)
                        {
                            String outStr = key + "=" + p.getProperty(key);
                            out.println(outStr);
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                LOGGER.warn("Couldn't Save save properties", ex);
            }
        }
    }

    /**
     *
     */
    public void doQuit( )
    {
        doQuit( 0);
    }

    @Override
    public void stop()
    {
        quiting = true;
        super.stop();
    }
    /**
     * 
     * @param status 
     */
    public void doQuit( int status)
    {
        saveProperties();
        quiting = true;
        System.exit(status);
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.application.CApp");//#LOGGER-NOPMD
}
