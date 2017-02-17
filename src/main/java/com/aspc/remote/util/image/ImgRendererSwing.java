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

import com.aspc.remote.util.misc.CLogger;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.apache.commons.logging.Log;

/**
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 * @author padma
 * @since       October 19, 2006
 */
public class ImgRendererSwing implements ImgRenderer
{

    /**
     * Creates a new instance of ImgRendererSwing
     * @param component to get the image form
     */
    public ImgRendererSwing(Component component)
    {
        sComponent = component;

    }
     /**
     * Returns the dimensions of the image
     * @param g Graphics context
     * @return Dimensions of image
     */
    @Override
    public Dimension getDimensions( Graphics g)
    {
        return sComponent.getSize();
    }

    /**
     * Generate the image
     * @return Generated image
     */
    @Override
    public BufferedImage createImage()
    {
        Dimension componentSize = getDimensions(null);
        sComponent.setSize(componentSize );     // Make sure that preferredsize and size is the same
        int h = componentSize.height;
        int w = componentSize.width;

        if( h == 0) h = 600;
        if( w == 0) w = 600;

        BufferedImage img = new BufferedImage( w,h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.fillRect(0,0,img.getWidth(), img.getHeight());

        paint(g2);
        g2.dispose();

        return img;
    }

    /**
     * Paints the image onto the Graphics context
     * @param g  Graphics context
     */
    @Override
    public void paint( Graphics g)
    {
        sComponent.paint(g);
    }

    private final Component sComponent;
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.image.ImgRendererSwing");//#LOGGER-NOPMD
}
