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

package com.aspc.remote.util.misc;

import org.apache.log4j.helpers.Loader;
import java.io.File;
import java.net.URL;
import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

/**
 * This is the static configuration class for logger
 *
 *  <br>
 *  <i>THREAD MODE: MULTI-THREADED</i>
 *
 * @author alex
 * @since 29 September 2006
 */
public final class CLoggerConfig
{
    private CLoggerConfig()
    {
        
    }
        
    /**
     *
     */
    @SuppressWarnings("empty-statement")
    public static void configure()
    {
        Layout layout = new PatternLayout("%m%n");
        Appender console = new ConsoleAppender(layout);

        BasicConfigurator.configure();

        Logger rootLogger;//NOPMD

        rootLogger = Logger.getRootLogger();
        rootLogger.removeAllAppenders();
        rootLogger.addAppender( console);
        rootLogger.setLevel( Level.INFO);

        String logDir = CProperties.getProperty("LOG_DIR");
        if( StringUtilities.isBlank( logDir))
        {
            logDir = CProperties.getHomeDir() +  "/logs";
            System.setProperty("LOG_DIR", logDir);
        }

        String logConfig = CProperties.getProperty(CLogger.LOG_PROPERTIES, "");

        /**
         * THIS IS ALWAYS WRONG for dev we want x y and z and in UAT we want a b and c
         *
         * I change it so that the default logging is good and you need to specify a
         * file to change it.
         *
         * if("".equals(logConfig))
         * {
         *     String root = CProperties.getProperty("DOC_ROOT");
         *
         *     if( StringUtilities.isBlank( root) == false)
         *     {
         *         logConfig = root + "/WEB-INF/logging.properties";
         *     }
         * }
         */
        if(StringUtilities.isBlank( logConfig) == false)
        {
            File fl = new File( logConfig);
            if( fl.exists())
            {
                PropertyConfigurator.configureAndWatch(logConfig);
            }
            else
            {
                try
                {
                    URL url = Loader.getResource( logConfig);
                    if( url != null)
                    {
                        PropertyConfigurator.configure(url);
                    }
                }
                catch( NoClassDefFoundError nf)
                {
                    ;
                }
            }
        }
    }
    
}
