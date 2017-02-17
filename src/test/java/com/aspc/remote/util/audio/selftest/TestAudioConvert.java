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
package com.aspc.remote.util.audio.selftest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.aspc.remote.util.misc.*;
import com.aspc.remote.util.audio.AudioConversionException;
import com.aspc.remote.util.audio.AudioConverter;
import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;

/**
 *  Check the audio conversion.
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED self test unit</i>
 *
 *  @author         Nigel Leck
 */
public class TestAudioConvert extends TestCase
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.audio.selftest.TestAudioConvert");//#LOGGER-NOPMD

    /**
     * Creates new VirtualDBTestUnit
     * @param name The name of the test unit
     */
    public TestAudioConvert(final String name)
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
//        test=TestSuite.createTest(TestAudioConvert.class, "testBMP");
        TestRunner.run(test);
    }

    /**
     * create a test suite
     * @return the suite of tests
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestAudioConvert.class);
        return suite;
    }

    public void testWave() throws AudioConversionException, IOException
    {
        File waveFile=new File( System.getProperty("SRC_DIR")+"/com/aspc/remote/util/audio/selftest/a2002011001-e02.wav");

        File mp3File=AudioConverter.build(waveFile).process();

        LOGGER.info( mp3File);
        File wavFile2=AudioConverter.build(mp3File).setFormat(AudioConverter.Format.WAV).process();
        LOGGER.info( wavFile2);

        if( wavFile2.length()<= mp3File.length())
        {
            fail( "Wav " + NumUtil.convertMemoryToHumanReadable(wavFile2.length()) +") file should be bigger than mp3" + NumUtil.convertMemoryToHumanReadable(mp3File.length()) +")");
        }
        File oggFile=AudioConverter.build(waveFile).setFormat(AudioConverter.Format.OGG).process();
        
        LOGGER.info( oggFile);        
    }

    public void testBadFile() throws IOException
    {
        File javaFile=new File( System.getProperty("SRC_DIR")+"/com/aspc/remote/util/audio/selftest/TestAudioConvert.java");

        try
        {
            AudioConverter.build(javaFile).process();
            fail( "should not be able to process a java file");
        }
        catch( AudioConversionException ce)
        {
            LOGGER.info( "expected", ce);
        }
    }
}
