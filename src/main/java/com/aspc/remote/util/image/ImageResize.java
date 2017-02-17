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
import com.aspc.remote.util.image.ImageUtil.FORMAT;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.FileUtil;
import com.aspc.remote.util.misc.NumUtil;
import com.aspc.remote.util.misc.QueueLog;
import com.aspc.remote.util.misc.StringUtilities;
import com.aspc.remote.util.misc.TimeUtil;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ImagingOpException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.IIOException;
import javax.imageio.IIOImage;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import org.apache.commons.logging.Log;
import org.w3c.dom.Element;
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
public class ImageResize
{
    private final File srcFile;
    private int maxWidth=-1;
    private int quality=100;
    private int maxHeight=-1;
    private int width=-1;
    private int height=-1;
    private int dpi=-1;
    private FORMAT requiredFormat=null;
    private final long maxMemory;
    private final static int VERSION=5;

    private final FORMAT srcFormat;
    private static final long DEFAULT_MAX_MEMORY=10*1024*1024;
    
    /**
     * Create a new Image resize. 
     * @param srcImage the source image
     * @throws FileNotFoundException we couldn't find the source image file.
     * @throws IOException we couldn't read/write the image.
     */
    public ImageResize(final @Nonnull File srcImage) throws FileNotFoundException, IOException
    {
        this( srcImage, null);
    }
    
    /**
     * Create a new Image resize. 
     * @param srcImage the source image
     * @param knownMimeType the known mime type
     * @throws FileNotFoundException we couldn't find the source image file.
     * @throws IOException we couldn't read/write the image.
     */
    public ImageResize(final @Nonnull File srcImage, final @Nullable String knownMimeType) throws FileNotFoundException, IOException
    {
        this( srcImage, knownMimeType, DEFAULT_MAX_MEMORY);
    }
    
    /**
     * Create a new Image resize. 
     * @param srcImage the source image
     * @param knownMimeType the known mime type
     * @param maxMemory the max memory to use.
     * @throws FileNotFoundException we couldn't find the source image file.
     * @throws IOException we couldn't read/write the image.
     */
    public ImageResize(final @Nonnull File srcImage, final @Nullable String knownMimeType, final @Nonnegative long maxMemory) throws FileNotFoundException, IOException
    {
        if( srcImage.canRead() ==false ) 
        {
            throw new IllegalArgumentException("image can't read " + srcImage);
        }
        else if( srcImage.length()< 10 ) 
        {
            throw new IllegalArgumentException("invalid image only " + srcImage.length() + " bytes long");
        }
        
        this.srcFile=srcImage;
        
        if( maxMemory <= 0)
        {
            throw new IllegalArgumentException("memory must be positive, was " + maxMemory);
        }
        
        this.maxMemory=maxMemory;
        FORMAT format=FORMAT.find( knownMimeType);
        String mimeType=knownMimeType;
        if( format == FORMAT.UNKNWON)
        {
            try
            (BufferedInputStream is = new BufferedInputStream(new FileInputStream(srcImage))) {
                mimeType = URLConnection.guessContentTypeFromStream(is);
            }
            format=FORMAT.find( mimeType);
            
            if( format == FORMAT.UNKNWON)
            {
                mimeType = FileUtil.adjustMimeType(mimeType, srcFile, null);
                format=FORMAT.find( mimeType);               
            }
            else if(StringUtilities.notBlank(knownMimeType) && FileUtil.MIME_APPLICATION_OCTET_STREAM.equalsIgnoreCase(knownMimeType)==false)
            {
                LOGGER.warn("warning " + knownMimeType + " was converted to " + mimeType + " by reading the file");
            }
        }
        
        if( format == FORMAT.UNKNWON)
        {
            throw new IOException("Unsupported mime type " + mimeType);
        }
        srcFormat=format;
    }
    
    /**
     * Copy the attributes from another.
     * @param src
     * @return this
     */
    @Nonnull
    public ImageResize copyAttributes( final @Nonnull ImageResize src)
    {
        requiredFormat=src.requiredFormat;
        dpi=src.dpi;
        setHeight(src.height);
        setWidth(src.width);
        setMaxHeight(src.maxHeight);
        setMaxWidth(src.maxWidth);
        setQuality(src.quality);
        
        return this;
    }
    
    /**
     * Set the format
     * @param format the format
     * @return this
     */
    @Nonnull
    public ImageResize setFormat( final @Nullable String format)
    {
        if( format == null || StringUtilities.isBlank(format))
        {
            requiredFormat=null;
        }
        else if( format.equalsIgnoreCase("jpg"))
        {
            this.requiredFormat=FORMAT.JPEG;
        }
        else
        {
            this.requiredFormat=FORMAT.find(format);
        }        
        
        return this;
    }

    /**
     * Set the MAX width
     * @param w the width
     * @return this
     */
    @Nonnull
    public ImageResize setMaxWidth( final @Nonnegative int w)
    {
        if( w < -1 || w > maxMemory)
        {
            throw new IllegalArgumentException("max width out of range ");
        }
        maxWidth=w;
        
        return this;
    }

    /**
     * Set the height
     * @param h the height
     * @return this
     */
    @Nonnull
    public ImageResize setMaxHeight( final @Nonnegative int h)
    {
        if( h < -1 || h > maxMemory)
        {
            throw new IllegalArgumentException("max height out of range ");
        }
        maxHeight=h;
        
        return this;
    }

    /**
     * Set the width
     * @param w the width
     * @return this
     */
    @Nonnull
    public ImageResize setWidth( final @Nonnegative int w)
    {
        if( w < -1 || w > maxMemory)
        {
            throw new IllegalArgumentException("width out of range ");
        }

        width=w;
        
        return this;
    }

    /**
     * Set the height
     * @param h the height
     * @return this
     */
    @Nonnull
    public ImageResize setHeight( final @Nonnegative int h)
    {
        if( h < -1 || h > maxMemory)
        {
            throw new IllegalArgumentException("height out of range ");
        }

        height=h;

        return this;
    }

    /**
     * set the required DPI
     * @param dpi the dot per inch
     * @return this
     */
    @Nonnull
    public ImageResize setDPI( final @Nonnegative int dpi)
    {
        if( dpi < -1 )
        {
            throw new IllegalArgumentException("DPI out of range " + dpi);
        }

        this.dpi=dpi;
        
        return this;
    }
    
    /**
     * Sets the quality
     * @param q the quality
     * @return this
     */
    @Nonnull
    public ImageResize setQuality( final @Nonnegative int q)
    {
        if( q < -1 || q > 100)
        {
            throw new IllegalArgumentException("quality (" + q + ") out of range 1...100");
        }

        quality=q;

        return this;
    }

    private String calculateFormat()
    {
        String format=srcFormat.name;
        if( requiredFormat != null)
        {
            format=requiredFormat.name;
        }
        else if( quality < 100)
        {
            format="jpg";
        }            
        else if( format == null)
        {
            return null;
        }
        return format.toLowerCase();
    }
    
    public String getMimeType()
    {
        String format=calculateFormat();

        return "image/" + format;
    }
        
    public File process() throws IOException
    {
        if( maxHeight <=0 && StringUtilities.isBlank(requiredFormat) && maxWidth <=0  && dpi <=0 && height <=0 && width<=0 && quality == 100)
        {
            return srcFile;
        }
        long lastModified = srcFile.lastModified();
        String baseDir = TimeUtil.format("yyyy/MMM/dd/HH/mm/", new Date( lastModified), TimeZone.getDefault());
        String fn=FileUtil.getCachePath() + "/resize/v" + VERSION + "/" + baseDir + "/";
        
        String sn=srcFile.getName();
        int lastIndexOf = sn.lastIndexOf(".");
        if( lastIndexOf != -1)
        {
            sn=sn.substring(0, lastIndexOf);
        }
        if( sn.length()>100)
        {
            sn = sn.substring(0, 100);
        }
        
        fn+= sn.replace(".", "_");
        
        if( maxHeight> 0)
        {
            fn+="_mh"+maxHeight;
        }
        if( maxWidth> 0)
        {
            fn+="_mw"+maxWidth;
        }
        if( height> 0)
        {
            fn+="_h"+height;
        }        
        
        if( dpi> 0)
        {
            fn+="_dpi"+dpi;
        }
        if( width> 0)
        {
            fn+="_w"+width;
        }
        if( quality> 0 && quality != 100)
        {
            fn+="_q"+quality;
        }

        String format=calculateFormat();
       
        fn += "#" + StringUtilities.checkSumAdler32("len"+srcFile.length() +",ts"+srcFile.lastModified()+ ":" + srcFile.toString());
        
        fn += "." + format.toLowerCase();
        
        File cacheFile=new File( fn);
        if( cacheFile.exists() == false)
        {
            // Read the original image from the Server Location
            ImageInputStream in = ImageIO.createImageInputStream(srcFile);
            IIOMetadata srcMeta=null;
            BufferedImage srcImage=null;
            
            try
            {
                Iterator it = ImageIO.getImageReaders(in);
               
                while( it.hasNext())
                {
                    ImageReader reader = null;
                    try
                    {
                        reader = (ImageReader) it.next();
                        reader.setInput(in);

                        if( srcMeta == null)
                        {
                            try
                            {
                                srcMeta = reader.getImageMetadata(0);
                            }
                            catch( IIOException e)
                            {
                                LOGGER.warn( "could not get source meta data", e);
                            }
                            catch( NullPointerException npe)
                            {
                                /**
                                 * JDK-8058973 NullPointerException from ICC_Profile.getInstance() in multi-thread application 
                                 * 
                                 * Affects Version/s: 7u65, 7u80, 8u20
                                 * 
                                 * https://bugs.openjdk.java.net/browse/JDK-8058973
                                 */
                                LOGGER.warn( "Known bug JDK-8058973", npe);
                                QueueLog.flush((long) (100 * Math.random()));
                                srcMeta = reader.getImageMetadata(0);
                            }
                        }
                        try
                        {
                            srcImage=reader.read(0);
                         
                            if (null != srcImage) break;
                        }
                        catch( IllegalArgumentException e)
                        {
                            LOGGER.warn( "could read image", e);
                        }
                    }
                    finally
                    {
                        if (null != reader) reader.dispose(); 
                    }
                }
                
                if( srcImage == null)
                {
                    ImageInputStream in2 = ImageIO.createImageInputStream(srcFile);
                    Iterator it2 = ImageIO.getImageReaders(in2);
                    try
                    {
                        while( it2.hasNext())
                        {
                            ImageReader reader = null;
                            try
                            {
                                reader = (ImageReader) it2.next();
                                ImageReadParam param = reader.getDefaultReadParam();
                                reader.setInput(in2, true, true);
                                Iterator<ImageTypeSpecifier> imageTypes = reader.getImageTypes(0);
                                ImageTypeSpecifier bestImageType=null;
                                ImageTypeSpecifier secondImageType=null;
                                while (imageTypes.hasNext()) 
                                {
                                    ImageTypeSpecifier imageTypeSpecifier = imageTypes.next();
                                    int bufferedImageType = imageTypeSpecifier.getBufferedImageType();
                                    if (bufferedImageType == BufferedImage.TYPE_3BYTE_BGR) 
                                    {
                                        bestImageType=imageTypeSpecifier;
                                    }
                                    else if (bufferedImageType == BufferedImage.TYPE_BYTE_GRAY) 
                                    {
                                        secondImageType=imageTypeSpecifier;
                                    }
                                }
                                if(bestImageType != null)
                                {
                                    param.setDestinationType(bestImageType);
                                    try
                                    {
                                        srcImage = reader.read(0, param);
                                        if (null != srcImage) break;                                   
                                    }
                                    catch( IllegalArgumentException e)
                                    {
                                        LOGGER.warn( "could read image", e);
                                    }
                                }
                                if(secondImageType != null)
                                {
                                    param.setDestinationType(secondImageType);
                                    try
                                    {
                                        srcImage = reader.read(0, param);
                                        if (null != srcImage) break;                                   
                                    }
                                    catch( IllegalArgumentException e)
                                    {
                                        LOGGER.warn( "could read image", e);
                                    }
                                }
                            }

                            finally
                            {
                                if (null != reader) reader.dispose(); 
                            }
                        }
                    }
                    finally
                    {
                        in2.close();
                    }
                }
            }
            finally
            {
                in.close();
            }
            
            if( srcImage == null)
            {                
                LOGGER.warn("can not resize " + srcFile);
                return srcFile;
            }

            int limitWidth=width;
            if( limitWidth <= 0 && maxWidth>0)
            {        
                int calWidth;
                if( height > 0)
                {
                    calWidth= (height * srcImage.getWidth() / srcImage.getHeight());
                }
                else
                {
                    calWidth=srcImage.getWidth();
                }
                if( maxWidth < calWidth)
                {
                    limitWidth=maxWidth;
                }
            }
                        
            int limitHeight=height;
            if( limitHeight <= 0 && maxHeight>0)
            {        
                int cal;
                if( width > 0)
                {
                    cal= (width * srcImage.getHeight() / srcImage.getWidth());
                }
                else
                {
                    cal=srcImage.getHeight();
                }
                if( maxHeight < cal)
                {
                    limitHeight=maxHeight;
                }
            }
            
            int w=width;
            if( w <=0)
            {                
                if( limitHeight>=0)
                {
                    w=Math.round((float)limitHeight * (float)srcImage.getWidth() / (float)srcImage.getHeight());
                }
                else
                {
                    w=srcImage.getWidth();
                }
            }

            if( w > limitWidth && limitWidth > 0)
            {
                w=limitWidth;
            }

            int h=height;
            if( h <=0)
            {
                if( limitWidth >=0)
                {
                    h=Math.round((float)limitWidth * (float)srcImage.getHeight() / (float)srcImage.getWidth());
                }
                else
                {
                    h=srcImage.getHeight();
                }
            }
            
            if( h > limitHeight && limitHeight > 0)
            {
                h=limitHeight;
            }

            if( h * w > maxMemory)
            {
                throw new IOException( NumUtil.convertMemoryToHumanReadable(h * w) + " > max memory of " + NumUtil.convertMemoryToHumanReadable(maxMemory));
            }
            
            if( 
                dpi <=0 &&
                quality == 100 &&
                srcImage.getWidth() == w && 
                srcImage.getHeight() == h &&
                srcFormat.name.equalsIgnoreCase(format)
            )
            {
                FileUtil.copy(srcFile, cacheFile);
                
                return cacheFile;
            }
            
            LOGGER.info( srcImage.getWidth() + "x" + srcImage.getHeight() + " -> " + w + "x" + h);

            File dir=cacheFile.getParentFile();
            dir.mkdirs();
            File tempFile = File.createTempFile("resize", "." + format, dir);
            
            BufferedImage scaledImage;

            scaledImage =createResizedCopy(
                format, 
                srcImage,
                w,
                h
            );                
            
            Iterator iter = ImageIO.getImageWritersByFormatName(format);
                                    
            ImageWriter bestWriter=null;
            
            while( iter.hasNext())
            {
                ImageWriter writer = (ImageWriter)iter.next();
                if( iter.hasNext() == false)
                {
                    bestWriter=writer;
                }
                else 
                {
                    ImageWriteParam iwp = writer.getDefaultWriteParam();
                    if( quality < 100 )
                    {
                        if( iwp.canWriteCompressed() == false)
                        {
                            continue;
                        }
                    }

                    bestWriter=writer;
                }                
            }

            if( bestWriter == null)
            {
                throw new IOException( "no writer found for " + format);
            }
            
            String className=bestWriter.getClass().getName();
            
            assert format.equalsIgnoreCase("jpg") == false || className.equals( "com.sun.imageio.plugins.jpeg.JPEGImageWriter"): "wrong writer " + className;
         
            double pixelSizeMM=-1;

            if( dpi > 0)
            {
                pixelSizeMM=ImageUtil.convertDPI2PixelMM(dpi);
            }
            else
            {
                if( srcMeta != null)
                {
                    IIOMetadataNode standardTree = (IIOMetadataNode) srcMeta.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName);
                    NodeList elementsByTagName = standardTree.getElementsByTagName("Dimension");
                    if( elementsByTagName.getLength()>0)
                    {
                        IIOMetadataNode dimension = (IIOMetadataNode) elementsByTagName.item(0);
                        pixelSizeMM= ImageUtil.getPixelSizeMM(dimension);
                    }
                }
            }
            
            try
            (ImageOutputStream stream = ImageIO.createImageOutputStream(tempFile)) {
                bestWriter.setOutput(stream);
                   
                ImageWriteParam writeParam = bestWriter.getDefaultWriteParam();

                if( writeParam.canWriteProgressive())
                {    
                    writeParam.setProgressiveMode(ImageWriteParam.MODE_DEFAULT);
                }

                if( writeParam.canWriteCompressed())
                {
                    writeParam.setCompressionMode(ImageWriteParam.MODE_DEFAULT);
                }
                
                IIOMetadata writeMeta = bestWriter.getDefaultImageMetadata(new ImageTypeSpecifier(scaledImage), writeParam);
                if( writeMeta.isReadOnly()==false)
                {
                    if( pixelSizeMM > 0)
                    {
                        /**
                         * http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6359243
                         * http://bugs.java.com/bugdatabase/view_bug.do?bug_id=5106305
                         */
                        if( className.equals("com.sun.imageio.plugins.png.PNGImageWriter"))
                        {
                            double size=(double)ImageUtil.convertDPI(pixelSizeMM)/10/2.54;
                            setPixelSizeMM( writeMeta, size );
                        }
                        else if( className.equals( "com.sun.imageio.plugins.jpeg.JPEGImageWriter"))
                        {
                            int tmpDPI =ImageUtil.convertDPI(pixelSizeMM);

                            Element tree = (Element)writeMeta.getAsTree(ImageUtil.TREE_JPEG);

                            NodeList app0JFIFList = tree.getElementsByTagName(ImageUtil.JPEG_SIGNATURE);
                            if( app0JFIFList.getLength()>0)
                            {
                                Element jfif;
                                jfif= (Element)app0JFIFList.item(0);

                                jfif.setAttribute("Xdensity", Integer.toString(tmpDPI));
                                jfif.setAttribute("Ydensity", Integer.toString(tmpDPI));
                                jfif.setAttribute("resUnits", "1"); // density is dots per inch  

                                writeMeta.mergeTree(ImageUtil.TREE_JPEG,tree);
                            }
                        }
                        else
                        {
                            setPixelSizeMM( writeMeta, pixelSizeMM );
                        }
                    }
                }
                else
                {
                    LOGGER.info( "writer class: " + bestWriter.getClass().toString());                    
                }

                if( quality < 100)
                {
                    if( writeParam.canWriteCompressed())
                    {
                        String[] compressionTypes = writeParam.getCompressionTypes();
                        if( compressionTypes.length > 0)
                        {
                            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                            writeParam.setCompressionType(compressionTypes[0]);
                            writeParam.setCompressionQuality(((float)quality)/100f);
                        }
                    }
                }
                
                IIOImage image = new IIOImage(scaledImage, null, writeMeta);
                
                bestWriter.write(writeMeta , image, writeParam);
            }
            finally
            {
                bestWriter.reset();
            }

            FileUtil.replaceTargetWithTempFile(tempFile, cacheFile);
        }

        return cacheFile;
    }

    private ImageResize setPixelSizeMM(IIOMetadata metadata, double dotsPerMilli) throws IIOInvalidTreeException 
    {        
       // for PMG, it's dots per millimeter
       //  double dotsPerMilli = 1.0 * DPI / 10 / INCH_2_CM;
       IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
       horiz.setAttribute("value", Double.toString(dotsPerMilli));

       IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
       vert.setAttribute("value", Double.toString(dotsPerMilli));

       IIOMetadataNode dim = new IIOMetadataNode("Dimension");
       dim.appendChild(horiz);
       dim.appendChild(vert);

       IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
       root.appendChild(dim);

       metadata.mergeTree("javax_imageio_1.0", root);
       
       return this;
    }
    
    private BufferedImage createResizedCopy(final String format, final BufferedImage originalImage, final int scaledWidth, final int scaledHeight)
    {
        BufferedImage scaledBI;

        int imageType=ImageUtil.FORMAT.find(format).imageType;

        scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
        scaledBI.setAccelerationPriority(0);
        HashMap map=new HashMap();
        map.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        map.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        map.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        map.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      //  map.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        try
        {
            AffineTransform at = new AffineTransform();
            double sx=(double)scaledWidth/(double)originalImage.getWidth();
            double sy=(double)scaledHeight/(double)originalImage.getHeight();

            at.setToScale(sx, sy);

            RenderingHints hints=new RenderingHints(map);

            AffineTransformOp scaleOp = new AffineTransformOp(at, hints);

            scaleOp.filter(originalImage, scaledBI);
        }
        catch( ImagingOpException ioe)
        {        
            Graphics2D g = scaledBI.createGraphics();
            g.setRenderingHints(map);
     
            try
            {
                g.setComposite(AlphaComposite.Src);
                g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
            }
            finally
            {
                g.dispose();
            }
        }
        return scaledBI;
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.image.ImageResize");//#LOGGER-NOPMD
}
