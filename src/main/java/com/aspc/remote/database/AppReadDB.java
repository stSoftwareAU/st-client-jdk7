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
package com.aspc.remote.database;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import com.aspc.remote.application.DBaseCmdLine;
import com.aspc.remote.util.misc.*;
import java.io.File;
import java.io.FileWriter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 *  Application to read the database.
 *
 * <i>THREAD MODE: SINGLE-THREADED command line application</i>
 *
 *  @author      Nigel Leck
 *  @since       12 May 2009
 */
public class AppReadDB extends DBaseCmdLine
{
    private String outFile;
    private File inFile;

    /**
     * add extra command line options
     * @param options the options
     */
    @Override
    protected void addExtraOptions( final Options options)
    {
        super.addExtraOptions( options);        

        Option outputOption = new Option( "o", true, "output file");
        outputOption.setArgName( "OUT");
        options.addOption(  outputOption);

        Option inOption = new Option( "i", true, "input script");
        inOption.setArgName( "IN");
        options.addOption(  inOption);

    }

    /**
     * handle the command line args
     *
     * @param line the command line
     * @throws Exception a serious problem
     */
    @Override
    public void handleCommandLine( final CommandLine line) throws Exception
    {
        super.handleCommandLine( line);

        outFile = line.getOptionValue("o");
        String tmp = line.getOptionValue("i");

        if( StringUtilities.isBlank(tmp))
        {
            throw new Exception( "input file is mandatory");
        }

        inFile = new File( tmp);

        if( inFile.exists() == false)
        {
            throw new Exception( inFile + " doesn't exist");
        }

        if( inFile.canRead() == false)
        {
            throw new Exception( "can't read " + inFile);
        }
    }

    /**
     * process the program
     */
    @Override
    public void process()
    {
        String script="NOT LOADED";
        try
        {
            script = FileUtil.readFile(inFile);

            CSQL sql = new CSQL( DataBase.getCurrent());

            sql.perform(script);

            if(outFile == null)
            {
                LOGGER.info(sql.formatOutput());
            }
            else
            {
                try
                (FileWriter w = new FileWriter( outFile)) {
                    writeOutFile( sql, w);
                }
            }
        }
        catch( Exception e)
        {
            LOGGER.error(script, e);
        }
    }

    private void writeOutFile( final CSQL sql, final FileWriter w) throws IOException, SQLException
    {
        int columnCount = sql.getColumnCount();

        for( int c = 1; c <= columnCount; c++)
        {
            String name = sql.getColumnName(c);

            name = name.replace( "\"", "\"\"");

            if( c != 1) w.append(",");
            w.append("\"");
            w.append(name);
            w.append("\"");
        }
        w.append( "\n");

        while( sql.next())
        {
            for( int c = 1; c <= columnCount; c++)
            {
                String value = sql.getString(c);

                value = value.replace( "\"", "\"\"");

                if( c != 1) w.append(",");
                w.append("\"");
                w.append(value);
                w.append("\"");
            }
            w.append( "\n");
        }
    }

    /**
     * The main for the program
     * @param argv the command line arguments
     */
    public static void main(final String argv[])
    {
        new AppReadDB().execute(argv);
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.database.AppReadDB");//#LOGGER-NOPMD
}
