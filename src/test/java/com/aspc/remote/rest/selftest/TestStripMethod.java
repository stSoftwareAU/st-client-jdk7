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
package com.aspc.remote.rest.selftest;
import com.aspc.remote.database.InvalidDataException;
import com.aspc.remote.database.selftest.DBTestUnit;
import com.aspc.remote.rest.Method;
import com.aspc.remote.rest.ReST;
import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.StringUtilities;
import com.aspc.remote.util.timer.StopWatch;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * check the email utilities
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED test case</i>
 *
 * @author luke
 * @since July 29, 2013
 */
public class TestStripMethod extends TestCase
{
    /**
     * Creates a new unit test
     * @param name the name of the unit
     */
    public TestStripMethod( final String name )
    {
        super( name );
    }

    /**
     * Creates the test suite
     * @return Test the test suite
     */
    public static Test suite()
{
        TestSuite suite = new TestSuite( TestStripMethod.class );
        return suite;
    }

    /**
     * Entry point to run this test standalone
     * @param args the arguments to the test
     */
    public static void main( String[] args )
    {
        Test test=suite();
//        test = TestSuite.createTest(TestReSTBuilder.class, "testGZip");
        TestRunner.run( test);

        System.exit(0);
    }

    /**
     * Check we can get the parameter value
     *
     * @throws MalformedURLException
     * @throws com.aspc.remote.database.InvalidDataException
     */
    public void testParameterValues() throws MalformedURLException, InvalidDataException
    {
        URL url=new URL("http://localhost:8080/ReST/v6/class/Person?XYZ=ABC&_method=PUT");

        ReST.Builder b=ReST.builder(url);
        b.setMethod(Method.POST);

        String[] values = b.getParameterValues("XYZ");

        if( values==null || values.length!=1)
        {
            fail( "XYZ=" + Arrays.toString(values));
        }

        if( values[0].equals("ABC")==false)
        {
            fail( "XYZ=" + Arrays.toString(values));
        }
    }

    /**
     * Check we can get the parameter value
     *
     * @throws MalformedURLException
     * @throws com.aspc.remote.database.InvalidDataException
     */
    public void testParameterValues2() throws MalformedURLException, InvalidDataException
    {
        URL url=new URL("http://localhost:8080/ReST/v6/class/Person?XYZ=ABC&_method=PUT");

        ReST.Builder b=ReST.builder(url);
        b.setMethod(Method.POST);
        b.setParameter("XYZ", "2");
        String[] values = b.getParameterValues("XYZ");

        if( values==null || values.length!=1)
        {
            fail( "XYZ=" + Arrays.toString(values));
        }

        if( values[0].equals("2")==false)
        {
            fail( "XYZ=" + Arrays.toString(values));
        }
    }

    /**
     * Check we can get the parameter value
     *
     * @throws MalformedURLException
     * @throws com.aspc.remote.database.InvalidDataException
     */
    public void testParameterValues3() throws MalformedURLException, InvalidDataException
    {
        URL url=new URL("http://localhost:8080/ReST/v6/class/Person?XYZ=ABC&_method=PUT");

        ReST.Builder b=ReST.builder(url);
        b.setMethod(Method.POST);
        b.addParameter("XYZ", "2");
        String[] values = b.getParameterValues("XYZ");

        if( values==null || values.length!=2)
        {
            fail( "XYZ=" + Arrays.toString(values));
        }

        if( values[0].equals("ABC")==false)
        {
            fail( "XYZ=" + Arrays.toString(values));
        }

        if( values[1].equals("2")==false)
        {
            fail( "XYZ=" + Arrays.toString(values));
        }
    }

    /**
     * Check we can get the parameter value
     *
     * @throws MalformedURLException
     * @throws com.aspc.remote.database.InvalidDataException
     */
    public void testGetMethod() throws MalformedURLException, InvalidDataException
    {
        URL url=new URL("http://localhost:8080/ReST/v6/class/Person?XYZ=ABC&_method=GET");

        ReST.Builder b=ReST.builder(url);
        b.setMethod(Method.POST);

        Method method = b.getMethod();

        assertEquals("call method", Method.POST, method);

        String[] values = b.getParameterValues("_method");

        if( values==null || values.length!=1)
        {
            fail( "_method=" + Arrays.toString(values));
        }

        if( values[0].equals("GET")==false)
        {
            fail( "_method=" + Arrays.toString(values));
        }

    }

    /**
     * Stript of method from very long URL
     *
     * @throws MalformedURLException
     * @throws com.aspc.remote.database.InvalidDataException
     */
    public void testVeryLongMethod1() throws MalformedURLException, InvalidDataException
    {
        StringBuilder sb=new StringBuilder("http://localhost:8080/ReST/v6/class/Person");

        for( int loop=0;loop<10000;loop++)
        {
            sb.append("&").append("n").append(loop).append("=").append(loop);
        }
        sb.append("&_method=PUT&fe=y");
        String url=sb.toString();

        LOGGER.info( url.length());
        StopWatch sw=new StopWatch();
        for( int sample=0;sample<30;sample++)
        {
            sw.start();
            ReST.Builder b=ReST.builder(url);
            b.setMethod(Method.POST);
            sw.stop();
        }

//        LOGGER.info( sw.summary("parse time"));

        long min = sw.min();
        if( sw.min()> 100000000)
        {
            fail( sw.summary("parsing too slow was: " + min));
        }
    }

    /**
     * Stript of method from very long URL
     *
     * @throws MalformedURLException
     * @throws com.aspc.remote.database.InvalidDataException
     */
    public void testVeryLongMethod2() throws MalformedURLException, InvalidDataException
    {
        StringBuilder sb = new StringBuilder("http://localhost:8080/ReST/v6/class/Person?XYZ=abc");
        for(int i = 0;i < 500;i++)
        {
            sb.append("&abcdef").append(i).append("=1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
        }
        sb.append("&_method=PUT&fe=y");
        String url = sb.toString();
        LOGGER.info( url.length());
        StopWatch sw=new StopWatch();
        for( int sample=0;sample<30;sample++)
        {
            sw.start();
            ReST.Builder b=ReST.builder(url);
            b.setMethod(Method.POST);
            sw.stop();

        }

        ReST.Builder b=ReST.builder(url);
        b.setMethod(Method.POST);

        for(int i = 0;i < 500;i++)
        {
            String name="abcdef" + i;
            String[] values = b.getParameterValues(name);

            if( values==null || values.length!=1)
            {
                fail( name + "=" + Arrays.toString(values));
            }

            if( values[0].equals("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890")==false)
            {
                fail( name + "=" + Arrays.toString(values));
            }
        }
//        LOGGER.info( sw.summary("parse time"));

        long min = sw.min();
        if( sw.min()> 100000000)
        {
            fail( sw.summary("parsing too slow was: " + min));
        }
    }

    /**
     * Stript of method from very long URL
     *
     * @throws MalformedURLException
     * @throws com.aspc.remote.database.InvalidDataException
     */
    public void testVeryLongMethod3() throws MalformedURLException, InvalidDataException
    {
        StringBuilder sb=new StringBuilder("http://localhost:8080/ReST/v6/class/Person");
        String inject[]=DBTestUnit.getSQLInjectionStrings();
        for( int loop=0;loop<inject.length;loop++)
        {
            sb.append("&").append("n").append(loop).append("=").append(StringUtilities.encode(inject[loop]));
        }
        sb.append("&_method=PUT&fe=y");
        String url=sb.toString();

        LOGGER.info( url.length());
        StopWatch sw=new StopWatch();
        for( int sample=0;sample<30;sample++)
        {
            sw.start();
            ReST.Builder b=ReST.builder(url);
            b.setMethod(Method.POST);
            sw.stop();
        }

//        LOGGER.info( sw.summary("parse time"));

        long min = sw.min();
        if( sw.min()> 100000000)
        {
            fail( sw.summary("parsing too slow was: " + min));
        }
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.rest.selftest.TestStripMethod");//#LOGGER-NOPMD
}
