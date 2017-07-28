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
import java.awt.*;
import java.io.*;
import javax.swing.*;
import java.util.concurrent.atomic.AtomicInteger;
/**
 *  DocInternalFrame
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED swing</i>
 *
 *  @author      Nigel Leck
 *  @since       29 September 2006
 */
public class DocInternalFrame extends CInternalFrame
{
    private static final long serialVersionUID = 42L;

    /**
     *
     */
    protected File file;

    boolean hasChanged;
    private static final AtomicInteger untitledCount=new AtomicInteger();

    /**
     * 
     * @param master 
     */
    public DocInternalFrame( CFrame master)
    {
        super( "", master);
        String t = "Untitled";
        setClosable(true);
        setMaximizable(true);
        setIconifiable(true);
        setResizable(true);

        if( master instanceof DocFrame)
        {
             t += untitledCount.incrementAndGet();
        }

        setTitle( t);
    }

    /**
     *
     */
    public DocInternalFrame( )
    {
        this( null);
        setTitle( "*** Doc Under Construction ***");
    }

    /**
     * 
     * @return the value
     */
    public File getFile( )
    {
        return file;
    }

    /**
     * 
     * @param file 
     * @throws Exception a serious problem
     */
    public void openFile(File file ) throws Exception
    {
        this.file = file;

        doTitle();
    }

    /**
     *
     */
    protected void doTitle()
    {
        if( file != null)
        {
            setTitle( file.toString());
        }
    }

    /**
     * 
     * @return the value
     */
    @Override
    public boolean isAllowedToClose()
    {
        if( hasChanged == false) return true;

        CQuestion q = new CQuestion( master, "Save changes ?");

        q.setVisible( true);

        q.waitForUser();

        if( q.isOK())
        {
            return true;
        }

        return false;
    }

    /**
     * 
     * @throws Exception a serious problem
     */
    public void doSave() throws Exception
    {
    }

//    public void doClose()
//    {
//        setVisible( false);
//    }

    /**
     *
     */
    public void doSaveAs()
    {
        Runnable r = new Runnable()
        {
            @SuppressWarnings("deprecation")
            @Override
            public void run()
            {
                try
                {
                    FileDialog theFileDialog = new FileDialog(master, "Save As File...");

                    theFileDialog.show();

                    String selectedFile = theFileDialog.getFile();

                    if(selectedFile != null)
                    {
                        File aFile;

                        aFile = new File(
                            theFileDialog.getDirectory(),
                            selectedFile
                        );

                        if( aFile.exists() == true)
                        {
                            CQuestion q = new CQuestion(
                                master,
                                "'" + selectedFile + "' exists. Do you wish to override ?"
                            );

                            q.waitForUser();

                            if( q.isOK() == false)
                            {
                                return;
                            }
                        }

                        file = aFile;
                        doTitle();

                        doSave();

                        ((DocFrame)master).doRecentMenu(file.toString());
                    }
                }
                catch( Exception e)
                {
                    CMessage message = new CMessage( master, e.toString());
                    message.setVisible(true);
                }
            }
        };

        SwingUtilities.invokeLater( r);
    }
}
