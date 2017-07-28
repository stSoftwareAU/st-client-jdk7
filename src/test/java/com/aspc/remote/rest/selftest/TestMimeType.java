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
package com.aspc.remote.rest.selftest;
import com.aspc.remote.rest.ReST;
import com.aspc.remote.rest.Response;
import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.CLogger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.json.JSONObject;

/**
 * check the email utilities
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED test case</i>
 *
 * @author luke
 * @since July 29, 2013
 */
public class TestMimeType extends TestCase
{
    /**
     * Creates a new unit test
     * @param name the name of the unit
     */
    public TestMimeType( final String name )
    {
        super( name );
    }

    /**
     * Creates the test suite
     * @return Test the test suite
     */
    public static Test suite()
{
        TestSuite suite = new TestSuite( TestMimeType.class );
        return suite;
    }

    /**
     * Entry point to run this test standalone
     * @param args the arguments to the test
     */
    public static void main( String[] args )
    {
        Test test=suite();
//        test = TestSuite.createTest(TestReSTBuilder.class, "testWrongPassword");
        TestRunner.run( test);

        System.exit(0);
    }

    /**
     * check call to Google maps.
     * @throws Exception a test failure.
     */
    public void testJSON() throws Exception
    {
        String url="https://maps.googleapis.com/maps/api/directions/json?origin=Toronto&destination=Montreal&key=" + API_KEY;

        ReST.Builder call = ReST.builder(url);

        Response rr=call.getResponseAndCheck();
        String data = rr.getContentAsString();
        String mimeType = rr.mimeType;
        assertTrue( "check mime: " + mimeType, mimeType.matches("application/json.*"));
        JSONObject json=new JSONObject( data);

        LOGGER.info( json.toString(2));

        String status=json.getString("status");

        assertEquals( "check status", "OK", status);

//        assertTrue("should be fetched", rr.trace.isFetched());

        /**
         * Second quick call should not cache a fetch.
         */
        ReST.Builder call2 = ReST.builder(url).setMinCachePeriod("5 minutes");
        Response rr2=call2.getResponse();
        String data2 = rr2.getContentAsString();
        String mimeType2 = rr2.mimeType;

        assertTrue( "check mime",  mimeType2.matches("application/json.*"));

        JSONObject json2=new JSONObject( data2);

        LOGGER.info( json2.toString(2));

        String status2=json2.getString("status");

        assertEquals( "check status", "OK", status2);

//        assertEquals("should be fetched", Trace.CACHED, rr2.trace);

        assertEquals("data should match", data, data2);
    }

    private static final String API_KEY="AIzaSyCVKU_wSE5sjYkmKiqeItKkIMIA4ptrDoo";
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.rest.selftest.TestMimeType");//#LOGGER-NOPMD
}
