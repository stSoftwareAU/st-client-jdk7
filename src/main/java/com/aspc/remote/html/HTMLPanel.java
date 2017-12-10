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
package com.aspc.remote.html;

import com.aspc.developer.ThreadCop;
import com.aspc.remote.util.misc.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.*;
import javax.swing.text.BadLocationException;
import org.apache.commons.logging.Log;

/**
 *  HTMLPanel
 *
 * <i>THREAD MODE: SINGLE-THREADED HTML generator component</i>
 *  @author      Nigel Leck
 *  @since       14 June 1998
 */
public class HTMLPanel extends HTMLContainer
{
    private final ReadWriteLock GENERATE_LOCK = new ReentrantReadWriteLock();

    /**
     *
     * @return the value
     */
    public synchronized String generateRTF()
    {
   final String pars[] = new String[2];

        String html = generate();

        pars[0] = html;
        pars[1] = null;

        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    javax.swing.JTextPane pane;
                    pane = new javax.swing.JTextPane();

                    pane.setEditorKit(new javax.swing.text.html.HTMLEditorKit());

                    pane.read(
                        new java.io.StringReader(pars[0]),
                        "a.html"
                    );

                    javax.swing.text.rtf.RTFEditorKit rtf = new javax.swing.text.rtf.RTFEditorKit();

                    java.io.ByteArrayOutputStream out;
                    out = new java.io.ByteArrayOutputStream();

                    javax.swing.text.Document doc;
                    doc = pane.getDocument();

                    rtf.write(out, doc, 0, doc.getLength());

                    pars[1] = new String( out.toByteArray());
                }
                catch( IOException | BadLocationException e)
                {
                    pars[1] = e.toString();
                }
            }

        };

        com.aspc.remote.application.CApp.swingInvokeAndWait(r);

        return pars[1];
    }

    @Override
    public void setParent( HTMLComponent parent)
    {
        super.setParent(parent);
    }
    
    /**
     *
     * @return the value
     * @throws Exception a serious problem.
     */
    public synchronized String generateText() throws Exception
    {
        int count = getComponentCount();
        if( count == 0 ) return "";

        return StringUtilities.convertHtmlToText(generate());
    }

    /**
     *
     * @return the value
     * @throws IOException a problem writing to the file occurred.
     */
    public synchronized java.io.File generateHTMLFile( ) throws IOException
    {
        String html = generate();

        return writeToFile( html);
    }

    private File writeToFile( final String html ) throws IOException
    {
        String title = "page";
        if( this instanceof HTMLPage)
        {
            title = ((HTMLPage)this).getTitle();
        }

        return writeToFile( title, html, "html");
    }

    public static File writeToFile( final String title, final String data, final String extension ) throws IOException
    {
        Date now = new Date();
        String dirName = CProperties.getProperty("LOG_DIR") + "/pages/" + TimeUtil.format("yyyy/MMM/dd/HH/mm", now, TimeZone.getDefault());
        FileUtil.mkdirs(dirName);
        String tmpTitle = title.replace( " ", "_");
        tmpTitle = tmpTitle.replace( ":", "_");
        tmpTitle = StringUtilities.encode(tmpTitle);
        while( tmpTitle.contains("__"))
        {
            tmpTitle = tmpTitle.replace( "__", "_");
        }

        if( StringUtilities.isBlank(tmpTitle) || tmpTitle.length() < 3)
        {
            tmpTitle="unknown";
        }
        else if( tmpTitle.length() > 80)
        {
            tmpTitle=tmpTitle.substring(0, 80);
        }
        File dir=new File( dirName);
        File tmpFile = File.createTempFile(tmpTitle, ".html", dir);

        try
        (PrintWriter out = new PrintWriter( new FileWriter( tmpFile))) {


            out.println(data);
        }

        File file = null;

        for( int v =1; true;v++)
        {
            String fileName = tmpTitle + ( v > 1 ? "_" + v : "") + "." + extension;

            file=new File( dirName, fileName);

            if( tmpFile.renameTo(file)) break;

            // This should never happen but just in case.
            if( v > 1000 ) return tmpFile;
        }

        return file;
    }


    /**
     *
     * @return the value
     */
    public String generate( )
    {
        return generate( new ClientBrowser());
    }

     /**
      *
      * @param component
      */
     public void registerPostCompileCallBack( HTMLComponent component)
    {
        if( postCompileList == null) postCompileList = new ArrayList();//MT WARN: Inconsistent synchronization

        postCompileList.add( component);
    }

     /**
      *
      * @param browser
      * @return the value
      */
     @SuppressWarnings("AssertWithSideEffects")
    public synchronized @Nonnull String generate( @Nonnull final ClientBrowser browser)
    {
        Lock l=GENERATE_LOCK.readLock();
        l.lock();
        try
        {
            assert ThreadCop.pushMonitor(this, ThreadCop.MODE.ACCESS_ONLY_BY_CREATING_THREAD);

            if( isCompiled() == false)
            {
                compile(browser);

                if( postCompileList != null)
                {
                    for (Object postCompileList1 : postCompileList) {
                        HTMLComponent component = (HTMLComponent) postCompileList1;
                        component.postCompile(browser);
                    }
                }
            }

            StringBuilder buffer = new StringBuilder( 50000);
            try
            {
                assert ThreadCop.pushMonitor(this, ThreadCop.MODE.READONLY);
                iGenerate( browser, buffer);
            }
            finally
            {
                assert ThreadCop.popMonitor(this);
            }

            String html = buffer.toString();

            if( LOGGER.isDebugEnabled())
            {
                try
                {
                    writeToFile( html);
                }
                catch( IOException e)
                {
                    LOGGER.warn( "could not write generated html", e);
                }
            }

            return html;
        }
        finally
        {
            l.unlock();
            assert ThreadCop.popMonitor(this);
        }
    }

    private ArrayList postCompileList;

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.html.HTMLPanel");//#LOGGER-NOPMD
}
