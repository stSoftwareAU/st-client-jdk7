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
package com.aspc.remote.util.image;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.DocumentUtil;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.IIOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.logging.Log;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *  Resizes images
 *
 *
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED</i>
 *
 *  @author      Jason McGrath
 *  @since       1 Feb 2014
 */
public class ImageUtil
{
    public static enum FORMAT
    {
        PNG( "png"),
        GIF( "gif"),
        BMP( "bmp"),
        ICO( "ico"),
        JPEG( "jpg"),
        SVG( "svg"),
        UNKNWON( "unknown");

        public final String name;
        public final int imageType;

        @CheckReturnValue @Nonnull
        public static FORMAT find( final @Nullable String format)
        {
            if( format == null)
            {
                return UNKNWON;
            }
            String tmpFormat=format;
            if( tmpFormat.startsWith("image/"))
            {
                tmpFormat=tmpFormat.substring(6);
            }
            if( tmpFormat.equalsIgnoreCase("png"))
            {
                return PNG;
            }
            else if( tmpFormat.equalsIgnoreCase("jpeg") || tmpFormat.equalsIgnoreCase("jpg") || tmpFormat.equalsIgnoreCase("pjeg"))
            {
                return JPEG;
            }
            else if( tmpFormat.equalsIgnoreCase("gif") )
            {
                return GIF;
            }
            else if( tmpFormat.equalsIgnoreCase("bmp") )
            {
                return BMP;
            }
            else if( tmpFormat.equalsIgnoreCase("ico") || tmpFormat.endsWith("icon"))
            {
                return ICO;
            }
            else if( tmpFormat.endsWith("xml") )
            {
                return SVG;
            }
            else
            {
                return UNKNWON;
            }
        }

        private FORMAT( final @Nonnull String name)
        {
            this.name =name;

            if(  name.equalsIgnoreCase("jpg"))
            {
                imageType=BufferedImage.TYPE_INT_RGB;
            }
            else if(  name.equalsIgnoreCase("gif"))
            {
                imageType=BufferedImage.TYPE_INT_ARGB;
            }
            else if(  name.equalsIgnoreCase("bmp"))
            {
                imageType=BufferedImage.TYPE_3BYTE_BGR;
            }
            else
            {
                imageType=BufferedImage.TYPE_INT_ARGB;
            }
        }
    };

    public static final String TREE_JPEG="javax_imageio_jpeg_image_1.0";
    public static final String TREE_IMAGE="javax_imageio_1.0";

    public static void dump( final File imageFile) throws Exception
    {
        ImageInputStream in = ImageIO.createImageInputStream(imageFile);
        IIOMetadata meta;

        try
        {
            Iterator it = ImageIO.getImageReaders(in);
            if (!it.hasNext())
            {
                LOGGER.warn("No reader for this format " + imageFile);
            }
            ImageReader reader = (ImageReader) it.next();
            reader.setInput(in);

            meta = reader.getImageMetadata(0);
        }
        finally
        {
            in.close();
        }

        LOGGER.info( "image: " + imageFile);
        LOGGER.info( "format: " + meta.getNativeMetadataFormatName());
        LOGGER.info( "readOnly: " + meta.isReadOnly());
        LOGGER.info( "pixel Size MM: " + getPixelSizeMM(imageFile));
        LOGGER.info( "DPI: " + getDPI(imageFile));
        
        String[] metadataFormatNames = meta.getMetadataFormatNames();
        for( String formatName: metadataFormatNames)
        {
            LOGGER.info( "format Name: " + formatName);
            Node asTree = meta.getAsTree(formatName);
            StringWriter sw=new StringWriter();
            DocumentUtil.writeNode(asTree, sw, 2);

            LOGGER.info( sw.toString());
        }        
    }
    
    @CheckReturnValue
    public static double getPixelSizeMM(final File imageFile) throws IOException
    {
           // Read the original image from the Server Location
        ImageInputStream in = ImageIO.createImageInputStream(imageFile);
        IIOMetadata meta;

        try
        {
            Iterator it = ImageIO.getImageReaders(in);
            if (!it.hasNext())
            {
                LOGGER.warn("No reader for this format " + imageFile);
                return -1;
            }
            ImageReader reader = (ImageReader) it.next();
            reader.setInput(in);

            try
            {
                meta = reader.getImageMetadata(0);
            }
            catch( IIOException e)
            {
                LOGGER.warn( "could not get source meta data", e);
                return -1;
            }

        }
        finally
        {
            in.close();
        }

        IIOMetadataNode tree = (IIOMetadataNode) meta.getAsTree( TREE_IMAGE);

        double horizontalPixelSizeMM =-1;
        NodeList elementsByTagName = tree.getElementsByTagName("Dimension");
        if( elementsByTagName.getLength()>0)
        {
            IIOMetadataNode dimension = (IIOMetadataNode) tree.getElementsByTagName("Dimension").item(0);
            horizontalPixelSizeMM = getPixelSizeMM(dimension);
        }
        
        return horizontalPixelSizeMM;
    }

    @CheckReturnValue @Nonnull
    public static Dimension getDimension( final @Nonnull File imageFile) throws IOException
    {        
        Dimension result;
        try
        (ImageInputStream in = ImageIO.createImageInputStream(imageFile)) 
        {
            Iterator it = ImageIO.getImageReaders(in);
            if (!it.hasNext())
            {
                throw new IOException("No reader for this format " + imageFile);
            }
            ImageReader reader = (ImageReader) it.next();
            reader.setInput(in);
            int w = reader.getWidth(reader.getMinIndex());
            int h = reader.getHeight(reader.getMinIndex());
            result = new Dimension(w, h);
        }
        
        return result;    
    }
    
    @CheckReturnValue
    public static int getDPI(final File imageFile) throws IOException
    {
        double pixelSizeMM=getPixelSizeMM( imageFile);

        return convertDPI(pixelSizeMM);
    }

    @CheckReturnValue
    public static int convertDPI( double pixelSizeMM)
    {
        int dpi= Math.round((float)(25.4/pixelSizeMM));

        if( dpi <= 0) return -1;

        return dpi;
    }

    @CheckReturnValue
    public static double convertDPI2PixelMM( int dpi)
    {
        if( dpi <= 0) return -1;

        double pixelSizeMM = 25.4/(double)dpi;

        return pixelSizeMM;
    }

    @CheckReturnValue
    public static int getDPI(final IIOMetadataNode dimension) throws IOException
    {
        double pixelSizeMM=getPixelSizeMM( dimension);

        return convertDPI(pixelSizeMM);
    }

    public static final String HORIZONTAL_PIXAL_SIZE="HorizontalPixelSize";
    public static final String JPEG_SIGNATURE="app0JFIF";

    @CheckReturnValue
    public static double getPixelSizeMM(final IIOMetadataNode dimension)
    {
        // NOTE: The standard metadata format has defined dimension to pixels per millimeters, not DPI...
        NodeList pixelSizes = dimension.getElementsByTagName(HORIZONTAL_PIXAL_SIZE);
        IIOMetadataNode pixelSize = pixelSizes.getLength() > 0 ? (IIOMetadataNode) pixelSizes.item(0) : null;

        if( pixelSize != null)
        {
            return Double.parseDouble(pixelSize.getAttribute("value"));
        }
        else
        {
            return -1;
        }
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.image.ImageUtil");//#LOGGER-NOPMD
}
