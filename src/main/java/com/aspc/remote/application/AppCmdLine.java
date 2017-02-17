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

import com.aspc.remote.util.misc.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Properties;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *  AppCmdLine is the standard command line.
 *
 * <i>THREAD MODE: SINGLE-THREADED command line application</i>
 *
 *  @author      Nigel Leck
 *  @since       23 June 1997
 */
public abstract class AppCmdLine
{
    /**
     * command line option help
     */
    public static final String OPTION_HELP="help";

    /**
     * command line option home
     */
    public static final String OPTION_HOME="HOME";

    /**
     * command line option to set the remote URL
     */
    public static final String OPTION_REMOTE_URL="remote-url";

    /**
     * command line option to turn on debugging
     */
    public static final String OPTION_DEBUG="debug";

    /**
     * the system property for the remote URL
     */
    public static final String PROPERTY_REMOTE_URL="REMOTE.url";

    /** The value to return to the OS */
    protected int exitStatus = 0;

    private boolean initialized,
                    handledCommandLine;

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.application.AppCmdLine");//#LOGGER-NOPMD

    private boolean exitFlag = true;

    /**
     * execute the program
     * @param args the args
     */
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void execute( final @Nonnull String args[])
    {
        Shutdown.init();
        String appName = getClass().toString().substring(6);
        System.getProperties().put( "application.name", appName );

        setDefaults();

        RelaxedCommandLineParser parser = new RelaxedCommandLineParser();//#FACTORY-NOSYNC

        Options options = makeCommandLineOptions(parser);

        CommandLine line;
        try
        {
            // parse the command line arguments
            line = parser.parse( options, args );
        }
        catch( ParseException exp )
        {
            // oops, something went wrong
            abortShowHelp(exp);
            return;
        }

        try
        {
            handleCommandLine( line);
            if( handledCommandLine == false)
            {
                throw new Exception(
                    "Programming error :-\n" +
                    "'handleCommandLine' overloaded and 'super.handleCommandLine(line)' not called"
                );
            }
            handleLegacyArgs( args);
        }
        catch(Exception e)
        {
            abortShowHelp(e);
        }

        try
        {
            init();
            if( initialized == false)
            {
                throw new Exception( "Programming error :- init overloaded and super.init() not called");
            }
            process();
        }
        catch( Throwable e)
        {
            exitStatus = 1;
            LOGGER.fatal(
                "Failed because ",
                e
            );
        }

        if (exitFlag)
        {
            Shutdown.exit(exitStatus);
        }
    }

    /**
     * set exit or not flag
     * @param flag  the flag
     */
    public void setExitFlg(boolean flag)
    {
        exitFlag = flag;
    }

    private void handleLegacyArgs(final @Nonnull String args[]) throws Exception
    {
        Properties p = RelaxedCommandLineParser.makeLegacyProperties( args);

        handleArgs( p);
    }
    /**
     * abort the program
     * @param message the message to show
     * @param exp the exception
     */
    protected void abort( final @Nonnull String message, final @Nullable Exception exp)
    {
        LOGGER.error( message, exp);
        System.exit(1);
    }

    @Nonnull
    private Options makeCommandLineOptions( final @Nullable RelaxedCommandLineParser parser)
    {
        Options options = new Options();

        addExtraOptions( options);

        Properties p = new Properties();
        getExtraPars( p);

        if( p.size() > 0)
        {
            if( parser != null ) parser.setLegacyArgs( true);

            Object keys[] = p.keySet().toArray();

            for (Object key1 : keys) 
            {
                String key = (String) key1;
                if( key.startsWith( "["))
                {
                    key = key.substring(1);
                }
                if (key.startsWith( "-") && key.length() > 1) {
                    String temp = key.substring(1, 2);
                    if (options.hasOption( temp) == false) {
                        options.addOption(temp, false, p.getProperty((String) key1));
                    }
                }
            }
        }
        return options;
    }

    /**
     * abort showing the help
     * @param e
     */
    protected void abortShowHelp( final @Nullable Exception e)
    {
        HelpFormatter formatter = new HelpFormatter();

        Options options = makeCommandLineOptions( null);
        String header = "";
        String tail = "";
        String footer = "";
        boolean showStack = false;
        if( e != null)
        {
            if( e instanceof MissingOptionException)
            {
                footer = "Missing options: " + e.getMessage();
            }
            else
            {
                footer =e.getMessage();
                if( StringUtilities.isBlank( footer))
                {
                    footer = e.toString();
                }
                showStack=true;
            }
        }

        StringWriter sw= new StringWriter();
        PrintWriter pw = new PrintWriter( sw);
        formatter.printHelp( pw, 132, "java " + getClass().getName(), header, options, 4, 8, tail,true );

        LOGGER.error( sw.toString());

        if( showStack)
        {
            LOGGER.error( footer,e);
        }
        else
        {
            LOGGER.error( footer);
        }
        System.exit(1);
    }

    /**
     * do not use this method
     * @param p the parameters
     * @deprecated NO LONGER USED
     */
    public void getExtraPars(final @Nonnull Properties p)
    {
    }

    /**
     * add extra command line options
     * @param options the options
     */
    @OverridingMethodsMustInvokeSuper
    protected void addExtraOptions( final @Nonnull Options options)
    {
        Option help = new Option( OPTION_HELP, false, "Print this message");

        options.addOption(  help);
        Option remoteOpt = new Option("R", OPTION_REMOTE_URL, true, "Remote client URL" );
        remoteOpt.setArgName( "user:password@server/layer");
        options.addOption( remoteOpt);
        Option debugOpt = new Option( OPTION_DEBUG, false, "Turns on debug mode");
        options.addOption(  debugOpt);

        Option homeOpt = new Option( "H", OPTION_HOME, true, "Home directory" );
        homeOpt.setArgName("dir");
        options.addOption( homeOpt);
    }

    /**
     * init the program
     * @throws Exception a serious problem
     */
    @OverridingMethodsMustInvokeSuper
    public void init() throws Exception
    {
        initialized = true;
    }

    /**
     * process ( extend this method)
     * @throws java.lang.Exception a serious problem
     */
    public abstract void process() throws Exception;

    /**
     * set up the defaults
     */
    public static void setDefaults()
    {
        Properties p = System.getProperties();

        p.put( "user.region", "AU" );

        Locale locale = new Locale(
            p.getProperty( "user.language", "EN" ),
            p.getProperty( "user.region", "" )
        );

        Locale.setDefault( locale );

        System.setProperties( p );
    }

    /**
     * Do not use this method
     *
     * @param p the properties
     * @throws Exception a serious problem
     * @deprecated NO LONGER USED
     */
    public void handleArgs( final @Nonnull Properties p) throws Exception
    {
    }

    /**
     * handle the command line args
     * @param line the command line
     * @throws Exception a serious problem
     */
    @OverridingMethodsMustInvokeSuper
    public void handleCommandLine( final @Nonnull CommandLine line) throws Exception
    {
        handledCommandLine = true;
        if( line.hasOption(OPTION_HELP))
        {
            abortShowHelp( null);
        }
        if( line.hasOption( OPTION_REMOTE_URL))
        {
           System.getProperties().put(
                PROPERTY_REMOTE_URL,
                line.getOptionValue(OPTION_REMOTE_URL)
           );
        }
        if( line.hasOption( 'R'))
        {
           System.getProperties().put(
                PROPERTY_REMOTE_URL,
                line.getOptionValue('R')
           );
        }

        if( line.hasOption( OPTION_DEBUG))
        {
           Logger.getRootLogger().setLevel( Level.DEBUG);
        }
        else
        {
           System.getProperties().put( "debug", "NO");
        }

        if( line.hasOption(OPTION_HOME))
        {
            System.getProperties().put(
                "ST_HOME",
                line.getOptionValue(OPTION_HOME)
           );
        }
    }
}
