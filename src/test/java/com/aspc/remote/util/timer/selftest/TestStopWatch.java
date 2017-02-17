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
package com.aspc.remote.util.timer.selftest;

import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.timer.StopWatch;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.commons.logging.Log;

/**
 *  test date utilities
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED self test unit</i>
 *
 *  @author         Nigel
 *  @since          2 June 2015
 */
public class TestStopWatch extends TestCase
{
    private static final Log LOGGER = CLogger.getLog( "com.aspecthuntley.apps.UpdateWebUserPass");
    
    /**
     * Creates new VirtualDBTestUnit
     * @param name The name of the test unit
     */
    public TestStopWatch(String name)
    {
        super( name);
    }

    /**
     * @param args  */
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }

    /**
     * @return the value
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestStopWatch.class);
        return suite;
    }
    public void testNone() throws InterruptedException
    {
         StopWatch sw=new StopWatch();
         LOGGER.info( sw.asciiArt());
         LOGGER.info( sw.summary("none"));
         LOGGER.info( sw.asciiArtTimeline());
         
         assertEquals( "average", -1, sw.avg());
    }
    public void testTimeline() throws InterruptedException
    {
        StopWatch sw=new StopWatch();
        for( int loop=0;loop<17;loop++)
        {
            sw.start();
            sw.stop();
        }
        
        Thread.sleep(100);
        for( int loop=0;loop<16;loop++)
        {
            sw.start();
            sw.stop();
        }
        
        String art=sw.asciiArtTimeline();
        LOGGER.info( art);
    }
    public void testArt()
    {
        int loops[]={0,1,49,50,51,100,101,203,1000};
        
        for( int c:loops)
        {
            StopWatch sw=new StopWatch();
            for( int i=0;i<c;i++)
            {
                sw.start();
                sw.stop();
            }
            LOGGER.info( sw.asciiArt());
            LOGGER.info( sw.asciiArtTimeline());
        }
        
    }
    public void testFormat()
    {
        check( 0, "0 ms");
        check( 1, "1 ns");
        check( 500, "500 ns");
        check( 1500, "1,500 ns");
        check( 5500, "6 µs");
        check( 5500000, "6 ms");
        check( 5 * 60 * 1000 * 1000, "300 ms");
        check( 5 * 60 * 1000L * 1000L * 1000L, "5 Min");
               
    }
    private void check( final long nano, final String expected)
    {
        StopWatch sw=new StopWatch();
        
        String actual = sw.formatNano(nano);
        
        assertEquals( nano + " ns", expected, actual);
    }
    
    /**
     *
     * @throws Exception a serious problem
     */
    @SuppressWarnings("SleepWhileInLoop")
    public void testStartStop() throws Exception
    {
        StopWatch sw=new StopWatch();
        long start=System.currentTimeMillis();
        for( int i=0;i< 101;i++)
        {
            sw.start();
            if( i==1)
            {
                Thread.sleep(2000);
            }
            else if( i >50 && i < 59)
            {
                // no time
            }
            else if( i%2 == 0)
            {
                Thread.sleep(100);
            }
            else
            {
                Thread.sleep(5);
            }
            
            sw.stop();
        }
        sw.test();
        long end=System.currentTimeMillis();
        LOGGER.info( sw);
        
        long max=sw.max();
        
        if( max/1000/1000>end-start)
        {
            fail( "max too large " + max/1000);
        }
        
        
        LOGGER.info( sw.asciiArt());
    }


}
