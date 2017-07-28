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
package com.aspc.remote.launcher;

import com.aspc.remote.application.AppCmdLine;
import com.aspc.remote.util.misc.*;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;

/**
 *  Launch the application
 *
 * <br>
 * <i>THREAD MODE: SINGLETON command line application SINGLE-THREADED</i>
 *
 *  @author      Jason McGath
 *  @since       24 May 2009
 */
public class App extends AppCmdLine implements BuildListener
{
    /**
     * add extra command line options
     * @param options the options
     */
    @Override
    protected void addExtraOptions( final Options options)
    {
        super.addExtraOptions( options);

        Option antOption = new Option( "f", true, "The ant script file name that is to be processed.");
        antOption.setArgName( "file");
        antOption.setOptionalArg(true);

        options.addOption(  antOption);
    }

    /**
     * handle the command line
     * @param line the line
     * @throws Exception a serious problem.
     */
    @Override
    public void handleCommandLine(CommandLine line) throws Exception
    {
        super.handleCommandLine(line);

        if( line.hasOption( "f"))
        {
           System.setProperty(Util.PROPERTY_LAUNCHER_FILE, line.getOptionValue("f"));
        }
        String[] args = line.getArgs();

        if( args.length > 0)
        {
            target = args[0];
        }
    }

    /**
     * process the program
     */
    @Override
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public void process()
    {
        if( StringUtilities.isBlank(target))
        {
            try
            {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
            }
            catch( ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
            {
                LOGGER.warn("could not set UI", e);
            }
            try
            {
                Console console = Console.mainWidnow();
                synchronized( console)
                {
                    console.wait();
                }
                return;
            }
            catch( Throwable e)
            {
                LOGGER.info("reverting to command line", e);
            }
        }
        try
        {

            DefaultLogger logger = new DefaultLogger();
            
            logger.setErrorPrintStream(System.err);
            logger.setOutputPrintStream(System.out);
            logger.setMessageOutputLevel(Project.MSG_INFO);

            Util.runTarget(target, logger, null, null, this);
        }
        catch( Exception e)
        {
            LOGGER.error("oops", e);
            exitStatus = 1;
        }

    }

   /**
     * The main for the program
     *
     * @param argv The command line arguments
     */
    public static void main(String argv[])
    {
        new App().execute(argv);
    }

    private String target = "";

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.launcher.App");//#LOGGER-NOPMD

    @Override
    public void buildStarted(BuildEvent be)
    {
    }

    @Override
    public void buildFinished(BuildEvent be)
    {
        if(be != null && be.getException() != null)
        {
            exitStatus = 1;
        }
    }

    @Override
    public void targetStarted(BuildEvent be)
    {
    }

    @Override
    public void targetFinished(BuildEvent be)
    {
    }

    @Override
    public void taskStarted(BuildEvent be)
    {
    }

    @Override
    public void taskFinished(BuildEvent be)
    {
    }

    @Override
    public void messageLogged(BuildEvent be)
    {
    }
}
