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
package com.aspc.remote.util.image.selftest;

import com.aspc.remote.util.image.ImageResize;
import com.aspc.remote.util.image.ImageUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.aspc.remote.util.misc.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLConnection;
import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;

/**
 *  Check image resize
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED self test unit</i>
 *
 *  @author         Nigel Leck
 *  @since          1 Feb 2014
 */
public class TestImageResizeLocal extends TestCase
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.image.selftest.TestImageResizeLocal");//#LOGGER-NOPMD

    private static boolean firstRun=true;

    /**
     * Creates new VirtualDBTestUnit
     * @param name The name of the test unit
     */
    public TestImageResizeLocal(String name)
    {
        super( name);
    }

    /**
     * run the tests
     * @param args the args
     */
    public static void main(String[] args)
    {
        Test test=suite();
//        test=TestSuite.createTest(TestImageResize.class, "testXIcon");
        TestRunner.run(test);
    }

    /**
     * create a test suite
     * @return the suite of tests
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestImageResizeLocal.class);
        return suite;
    }

    public void testScaleSVG() throws IOException
    {
        File srcFile=new File( System.getProperty("SRC_DIR"), "/com/aspc/remote/util/image/selftest/play.svg");

        ImageResize ir = new ImageResize(srcFile);
        ir.setMaxWidth(200);
        File scaledImage = ir.process();
        assertTrue("don't scale SVG", scaledImage.exists());
    }

    public void testScale() throws IOException
    {
        File srcFile=new File( System.getProperty("SRC_DIR"), "/com/aspc/remote/util/image/selftest/scale.png");

        ImageResize ir = new ImageResize(srcFile);
        ir.setMaxWidth(200);
        File scaledImage = ir.process();
        BufferedImage targetImage ;

        targetImage = ImageIO.read(scaledImage);

        int h=targetImage.getHeight();

        assertEquals( "rounding up of scaled height", 14, h);
    }

    public void testScaleJPG() throws IOException
    {
        File srcFile=new File( System.getProperty("SRC_DIR"), "/com/aspc/remote/util/image/selftest/scaleJPG.jpg");

        BufferedImage targetImage ;

        targetImage = ImageIO.read(srcFile);

        int h=targetImage.getHeight();

        assertEquals( "rounding up of scaled height", 100, h);
    }

    public void testMime() throws Exception
    {
        File srcFile=new File( System.getProperty("SRC_DIR"), "/com/aspc/remote/util/image/selftest/404.jpg");
//        String src=System.getProperty("SRC_DIR") + "/webapps/st/images/404.jpg"
//        File srcFile = new File( src);
        ImageResize ir=new ImageResize(srcFile);

        ir.setFormat("");
        ir.setHeight( -1);
        ir.setWidth(-1);
        ir.setMaxHeight( -1);
        ir.setMaxWidth(-1);
        ir.setQuality( 100);

        File rawFile=ir.process();
        boolean doesContentMatch = FileUtil.doesContentMatch(srcFile, rawFile);
        assertTrue( "content match", doesContentMatch);

        String mime=ir.getMimeType();

        assertEquals( "Mime should not change", "image/jpg", mime);

        ir.setFormat("png");

        mime=ir.getMimeType();

        assertEquals( "Mime should not change", "image/png", mime);
    }

    /**
     * Checks what happens if not an image.
     *
     * @throws IOException if an IO exception occurs.
     */
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void testNonImage() throws IOException, Exception
    {
        File tmpFile=File.createTempFile("non-image", ".txt",FileUtil.makeQuarantineDirectory());
        try{
            FileWriter fw = null;
            try
            {
                fw = new FileWriter( tmpFile);
                fw.write("Hello World");
            }
            finally
            {
                if(fw != null)
                {
                    fw.close();
                }
            }
            try
            {
                new ImageResize( tmpFile);
                fail( "should have failed");
            }
            catch( IOException io)
            {
                // good
            }
        }
        finally{
            tmpFile.delete();
        }
    }

    /**
     * Checks what happens if non-existing
     *
     * @throws IOException if an IO exception occurs.
     */
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void testNonExisting() throws IOException, Exception
    {
        File tmpFile=File.createTempFile("image", ".png",FileUtil.makeQuarantineDirectory());

        try
        {
            new ImageResize( tmpFile);
            fail( "did not detect missing file");
        }
        catch( IllegalArgumentException iae)
        {
            //good
        }
        finally{
            tmpFile.delete();
        }
    }

    public void testTranslate()
    {
        double pixelMM = ImageUtil.convertDPI2PixelMM(300);

        int dpi = ImageUtil.convertDPI(pixelMM);

        assertEquals("DPI", 300, dpi);
    }

    @Override
    /**
     * Set up the data universe ready for the test. A test unit maybe stopped half way through the processing so
     * we can not rely on the tear down process to set up the data for the next run.
     *
     * @throws java.lang.Exception A serious problem
     */
    public void setUp() throws IOException
    {
        if( firstRun)
        {
            firstRun=false;
            File dir= new File( FileUtil.getCachePath() + "/test/resize");
            if( dir.exists())
            {
                for( File f: dir.listFiles())
                {
                    f.delete();
                }
            }
        }
    }

    private void process(
        final File srcFile,
        final int w,
        final int h,
        final String requiredFormat,
        final int mw,
        final int mh,
        final float quality,
        final int dpi
    ) throws IOException
    {
        ImageResize ir = new ImageResize( srcFile);
        ir.setWidth(w);
        ir.setHeight(h);
        ir.setDPI(dpi);
        if(quality>0 )
        {
            ir.setQuality((int)(quality * 100));
        }
        ir.setMaxWidth(mw);
        ir.setMaxHeight(mh);
        ir.setFormat(requiredFormat);

        File process = ir.process();

        String mimeType = ir.getMimeType();

        String format;
        if(mimeType.endsWith("png"))
        {
            format="png";
        }
        else if(mimeType.endsWith("jpg")||mimeType.endsWith("jpeg"))
        {
            format="jpg";
        }
        else if(mimeType.endsWith("gif"))
        {
            format="gif";
        }
        else if(mimeType.endsWith("bmp"))
        {
            format="bmp";
        }
        else if(mimeType.endsWith("ico"))
        {
            format="ico";
        }
        else
        {
            throw new IOException( "unknown type " + mimeType);
        }
        String name = srcFile.getName();
        int pos = name.lastIndexOf(".");
        String fn = name.substring(0, pos) + "(";

        boolean start=false;
        if( w >=0)
        {
            fn += "width=" + w;
            start=true;
        }

        if( h >= 0)
        {
            if( start) fn += "&";
            start=true;

            fn += "height=" + h;
        }
        if( StringUtilities.notBlank(requiredFormat))
        {
            if( start) fn += "&";
            start=true;

            fn += "format=" + requiredFormat;
        }
        if( mw >= 0)
        {
            if( start) fn += "&";
            start=true;

            fn += "max-width=" + mw;
        }
        if( mh >= 0)
        {
            if( start) fn += "&";
            start=true;

            fn += "max-height=" + mh;
        }
        if( dpi >= 0)
        {
            if( start) fn += "&";
            start=true;

            fn += "dpi=" + dpi;
        }
        if( quality>0)
        {
            if( start) fn += "&";
         //   start=true;

            fn += "quality=" + (int)(quality * 100);
        }

        fn += ")." + format;

        File targetFile=new File( srcFile.getParentFile(), fn);

        FileUtil.copy(process, targetFile);
        BufferedImage orginalImage=null;
        try
        {
            orginalImage = ImageIO.read(srcFile);
        }
        catch( IllegalArgumentException e)
        {
            LOGGER.info( "could not read orginal file", e);
        }
        BufferedImage targetImage ;

        targetImage = ImageIO.read(targetFile);

        assertNotNull(targetFile.toString(), targetImage);

        if( w >0 )
        {
            assertEquals( "check width", w, targetImage.getWidth());
        }

        if( h >0 )
        {
            assertEquals( "check height", h, targetImage.getHeight());
        }

        if( h<=0 && w <= 0 && mh > 0 && mh < targetImage.getHeight())
        {
            fail( targetImage.getHeight() + " greater than max height " + mh);
        }

        if( mw > 0 && mw < targetImage.getWidth())
        {
            fail( targetImage.getWidth() + " greater than max width " + mw);
        }

        if(orginalImage!=null && mh >0 && mw <= 0 && w <=0 && mh < orginalImage.getHeight())
        {
            if( orginalImage.getWidth() <= targetImage.getWidth())
            {
                fail( "should have scaled down image when filtered by max-height");
            }
        }

        if(orginalImage!=null && mw >0 && mh <= 0 && h <=0 && mw < orginalImage.getWidth())
        {
            if( orginalImage.getHeight()<= targetImage.getHeight())
            {
                fail( "should have scaled down image when filtered by max-width");
            }
        }

        int srcDPI = ImageUtil.getDPI(srcFile);
        int checkDPI;
        if( dpi > 0)
        {
            checkDPI=dpi;
        }
        else
        {
            checkDPI=srcDPI;
        }
        int targetDPI = ImageUtil.getDPI(targetFile);
        if( format.equals( "gif") == false &&format.equals( "bmp") == false && checkDPI != targetDPI)
        {
            fail( "Should not have changed the DPI " + checkDPI + "->" + targetDPI);
        }

        if( format.equals( "bmp") == false)
        {
            BufferedInputStream is = null;
            try
            {
                is = new BufferedInputStream(new FileInputStream(targetFile));
                String guessMimeType = URLConnection.guessContentTypeFromStream(is);
                if(guessMimeType == null)
                {
                    fail( "could not read file " + targetFile);
                }
            }
            finally
            {
                if(is != null)
                {
                    is.close();
                }
            }
        }
    }
}
