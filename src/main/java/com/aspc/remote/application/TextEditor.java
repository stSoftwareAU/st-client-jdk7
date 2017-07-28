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
import javax.swing.*;
import java.io.*;
import com.aspc.remote.util.misc.*;

/**
 *  TextEditor
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED swing</i>
 *  @author      Nigel Leck
 *  @since       1 June 1997
 */
public class TextEditor extends DocInternalFrame
{
    CTextArea field;

    /**
     *
     * @param master
     */
    public TextEditor(CFrame master)
    {
        super( master);

        field = new CTextArea();
        field.setEditable( true);

        getContentPane().setLayout( new BorderLayout());
        getContentPane().add( "Center", field);
    }

    @Override
    public void openFile(File file ) throws Exception
    {
        super.openFile( file);

        BufferedReader reader = new BufferedReader( new FileReader( file));

        StringBuilder buffer = new StringBuilder();

        String line;

        try
        {
            while( ( line = reader.readLine()) != null)//NOPMD
            {
                buffer.append( line);
                buffer.append( "\n");
            }
        }
        finally
        {
            reader.close();
        }

        field.setText( buffer.toString());
    }

    @Override
    public void doSave() throws Exception
    {
        String t;

        t = field.getText();

        LOGGER.info( t);
        try
        (BufferedWriter writer = new BufferedWriter( new FileWriter( file))) {
            writer.write( t);
        }
    }

    /**
     *
     * @param text
     */
    public void setText( String text)
    {
        field.setText( text);
    }
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.application.TextEditor");//#LOGGER-NOPMD
}
