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

package com.aspc.remote.util.image;

import com.aspc.remote.util.image.internal.SyncLoadEditorKit;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.StringUtilities;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import org.apache.commons.logging.Log;

/**
 * ImgRendererHTML.java
 * <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 * @author padma
 * @since       October 19, 2006
 */
public class ImgRendererHTML implements ImgRenderer
{

    /**
     * Creates a new instance of imgRendererHTML
     * @param htmlTags to get the image from
     * @param width Width of the screen
     * @param height Height of the screen
     */
    public ImgRendererHTML(String htmlTags, int width, int height)
    {
        this.htmlTags = htmlTags;
        this.width = width;
        this.height =height;
        url = null;
    }

    /**
     * Creates a new instance of imgRendererHTML
     * @param url the url
     * @param width Width of the screen
     * @param height Height of the screen
     */
    public ImgRendererHTML(URL url, int width, int height)
    {
        this.url = url;
        this.width = width;
        this.height =height;
        htmlTags = null;
    }

      /**
     * Returns the dimensions of the image
     * @param g Graphics context
     * @return Dimensions of image
     */
    @Override
    public Dimension getDimensions( Graphics g)
    {
        return null;
    }

    /**
     * Generate the image
     * @return Generated image
     */
    @Override
    public BufferedImage createImage()
    {

        try
        {
            JTextPane editorPane = new JTextPane();

            if( StringUtilities.isBlank( htmlTags) == false)
            {
                editorPane.setContentType("text/html");

                HTMLDocument doc = new HTMLDocument( );
                doc.getDocumentProperties().put("IgnoreCharsetDirective", Boolean.TRUE);
                HTMLEditorKit kit = new HTMLEditorKit( );

                String tags =  htmlTags;
                Reader r = new StringReader( tags);
                kit.read( r, doc, 0);
                editorPane.setDocument(doc);
            }
            else
            {
                editorPane.setEditable(false);

                JEditorPane.registerEditorKitForContentType("text/html", "SyncLoadEditorKit");
                editorPane.setEditorKitForContentType("text/html", new SyncLoadEditorKit());
                editorPane.setPage(url);

            }

            JFrame frm = new JFrame();
            frm.add(new JScrollPane(editorPane));
            frm.setSize( width, height);

            frm.setVisible( true);
            frm.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

            ImgRendererSwing swingImage = new ImgRendererSwing( editorPane);
            BufferedImage img =  swingImage.createImage();
            frm.dispose();
            return img;
        }
        catch( Exception e)
        {
            LOGGER.error( "error", e);
        }

        return null;
    }

    /**
     * Paints the image onto the Graphics context
     * @param g  Graphics context
     */
    @Override
    public void paint( Graphics g)
    {

    }


    private final URL url;
    private final String htmlTags;
    private final int width, height;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.image.ImgRendererHTML");//#LOGGER-NOPMD
}


