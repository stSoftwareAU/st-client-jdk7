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

import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;

/**
 *  command line parser.
 * The class BasicParser provides a very simple implementation of
 * the {@link Parser#flatten(Options,String[],boolean) flatten} method.
 *
 * <i>THREAD MODE: SINGLE-THREADED paser</i>
 *
 *  @author      Nigel Leck
 *  @since       10 April 2006
 */
public class RelaxedCommandLineParser extends GnuParser 
{
    private boolean legacyArgs;
    
    /**
     * <p>A simple implementation of {@link Parser}'s abstract
     * {@link Parser#flatten(Options,String[],boolean) flatten} method.</p>
     *
     * <p><b>Note:</b> <code>options</code> and <code>stopAtNonOption</code>
     * are not used in this <code>flatten</code> method.</p>
     *
     * @param options The command line {@link Options}
     * @param arguments The command line arguments to be parsed
     * @param stopAtNonOption Specifies whether to stop flattening
     * when an non option is found.
     * @return The <code>arguments</code> String array.
     */
    @Override
    protected String[] flatten( final Options options, 
                                final String[] arguments, 
                                final boolean stopAtNonOption )
    {
        String[] kArgs = populateKnownOptions( options, arguments);
        
        String fAgrs[] = super.flatten( options, kArgs, stopAtNonOption);

        String sAgrs[] =stripKnownArgs( options, fAgrs);

        return sAgrs;
    }

    /**
     * make as using legacy args
     * @param flag ture if using legacy args
     */
    public void setLegacyArgs( final boolean flag)
    {
        legacyArgs = flag;
    }
    
    /**
     * add known options
     * @param arguments the original args
     * @param options the option set to append to.
     * @return the new set of args 
     */
    protected String[] populateKnownOptions(final Options options, final String[] arguments)
    {
        if( legacyArgs)
        {
            for (String temp : arguments) {
                if( temp.startsWith( "-") && temp.length() > 1)
                {
                    temp = temp.substring( 1, 2);

                    if( options.hasOption(temp) == false)
                    {
                        options.addOption( temp, false, "legacy option");
                    }
                }
            }
        }
        
        return arguments;
    }

    /**
     * make a old sytle properties
     * @param arguments the command line args
     * @return the properties
     */
    public static Properties makeLegacyProperties(final String[] arguments)
    {
        Properties p = new Properties();

        for (String argument : arguments) {
            String  name    = "",
                    value   = "";
            if (argument.startsWith("-") == true) {
                if (argument.length() > 1) {
                    name = argument.substring(0, 2);
                    if (argument.length() > 2) {
                        value = argument.substring(2, argument.length());
                    }
                }
            }
            p.put( name, value);
        }
        
        return p;
    }
    /**
     * remove known options that will cause an error
     * @param options the options
     * @param fArgs the full list of args 
     * @return the stripted version of the arguments. 
     */
    protected String[] stripKnownArgs(final Options options, final String[] fArgs)
    {
        ArrayList list = new ArrayList();
        
        final String knownList[]={
            "-D",
            "-U",
            "-P",
            "-T"
        };
        
        for (String temp : fArgs) {
            boolean found = false;
            for( int j = 0; j < knownList.length && found == false;j++)
            {
                String knownOption = knownList[j];
                
                if( temp.startsWith( knownOption))
                {
                    if( options.hasOption( knownOption) == false)
                    {
                        found = true;
                    }
                }
            }
            
            if( found == false)
            {
                list.add( temp);
            }                
        }
        
        String stripedArgs[] = new String[ list.size()];
        list.toArray( stripedArgs);
        return stripedArgs;
    }
}
