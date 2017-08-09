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
import com.aspc.remote.database.InvalidDataException;
import com.aspc.remote.rest.Method;
import com.aspc.remote.rest.ReST;
import com.aspc.remote.rest.Status;
import com.aspc.remote.rest.Response;
import com.aspc.remote.rest.errors.NotAuthorizedException;
import com.aspc.remote.rest.internal.Trace;
import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.CLogger;
import com.aspc.remote.util.misc.DocumentUtil;
import com.aspc.remote.util.misc.FileUtil;
import com.aspc.remote.util.misc.StringUtilities;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;

/**
 * check the email utilities
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED test case</i>
 *
 * @author luke
 * @since July 29, 2013
 */
public class TestReSTBuilder extends TestCase
{
    /**
     * Creates a new unit test
     * @param name the name of the unit
     */
    public TestReSTBuilder( final String name )
    {
        super( name );
    }

    /**
     * Creates the test suite
     * @return Test the test suite
     */
    public static Test suite()
{
        TestSuite suite = new TestSuite( TestReSTBuilder.class );
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
     * Check the toString method works.
     * @throws java.net.MalformedURLException
     * @throws com.aspc.remote.database.InvalidDataException
     */
    public void testToString() throws MalformedURLException, InvalidDataException
    {
        
        ReST.Builder b=ReST.builder("http://demo.jobtrack.com.au/ReST/v2/class/company");
        LOGGER.info( b.toString());
     
        b.addParameter("X", "Y");
        
        LOGGER.info( b.toString());
    }
    
    /**
     * http://www.456bereastreet.com/archive/201008/what_characters_are_allowed_unencoded_in_query_strings/
     * 
     * @throws MalformedURLException 
     * @throws com.aspc.remote.database.InvalidDataException 
     */
    public void testQueryInvalid() throws MalformedURLException, InvalidDataException
    {
        String checks[]={
            "abc=xyz&x=y ",
            "a%2=1",
            "abcS=123^456"
        };
        
        ReST.Builder b=ReST.builder("http://demo.jobtrack.com.au/ReST/v2/class/company");
        for( String line: checks)
        {            
            try
            {
                b.addQuery(line);
                fail( line);
            }
            catch( IllegalArgumentException iae)
            {
                //Good
            }
        }
                    
    }

    /**
     * http://www.456bereastreet.com/archive/201008/what_characters_are_allowed_unencoded_in_query_strings/
     * 
     * @throws MalformedURLException 
     * @throws com.aspc.remote.database.InvalidDataException 
     */
    public void testQueryValid() throws MalformedURLException, InvalidDataException
    {
        String checks[]={
            "e0a72cb2a2c7",
            "abc=xyz&x=y+",
            "a%20=1",
            "abcS=123%56456",
            "name=a-z,A-Z,0-9,-,.,_,and~",
            "first=this+is+a+field&second=was+it+clear+%28already%29%3F",
            "encode=+Letters+A-Z+and+a-z+numbers+0-9+*-._~"
        };
        
        ReST.Builder b=ReST.builder("http://demo.jobtrack.com.au/ReST/v2/class/company");
        for( String line: checks)
        {            
//            try
//            {
                b.addQuery(line);
//            }
//            catch( IllegalArgumentException iae)
//            {
//                fail( line);
//            }
        }
                    
    }
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void testCacheError() throws Exception
    {
        String url="https://demo2.jobtrack.com.au/ReST/v8/class/DBFolder/123456789@1";

        Response rr=ReST.builder( url).getResponse();
        assertEquals( "should be not found", Status.C404_ERROR_NOT_FOUND, rr.status);

        ReST.Builder call = ReST.builder( url);
        call.setErrorCachePeriod("30 minutes");
//        try
//        {
        Response rr2=call.getResponse();
//            fail( "should not be able to connect");
//        }
//        catch( ReSTException ce)
//        {
//            LOGGER.info( "expected");
//        }
        
        Trace lastCallResult = rr2.trace;
//        if( DBTestUnit.hideKnownErrors() == false)
//        {
            assertEquals( "Should have been cached", Trace.CACHED, lastCallResult);
//        }
    }


    public void testNoKey() throws Exception
    {
        String url="https://maps.googleapis.com/maps/api/directions/json?origin=Toronto&destination=Montreal&key=" + API_KEY;

        String fn = ReST.builder( url).makeFileName();

        assertFalse( "file name must not contain the key", fn.contains(API_KEY));
    }
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void testNotFound() throws Exception
    {
        String goodKey="10@1";
        String badKey="123456789@1";
        String url="https://demo2.jobtrack.com.au/ReST/v8/class/DBFolder/";

        ReST.builder( url + goodKey).getContentAsString();

        Response response=ReST.builder( url + badKey).setMinCachePeriod("5 min").getResponse();
        if( response.status != Status.C404_ERROR_NOT_FOUND)
        {
             fail( "should not find bad key was: " + response.status);
        }

        Response cachedResponse=ReST.builder( url + badKey).setMinCachePeriod("5 min").getResponse();
        if( cachedResponse.status != Status.C404_ERROR_NOT_FOUND)
        {
           fail( "should not find bad key (cached)");
        }
    }
    
    public void testDemo2() throws Exception
    {
        String address="https://demo2.jobtrack.com.au/ReST/v8/class/DBFolder?fields=name&q=" + StringUtilities.encode("name='web'");
        
        URL url =new URL(address);
        
        URLConnection c = url.openConnection();
        
        byte[] array;
        int len;
        try (InputStream in = c.getInputStream()) {
            array = new byte[10240];
            len = in.read(array);
        }
        
        LOGGER.info( new String( array,0, len, "utf-8"));

    }
    public void testLogin() throws Exception
    {
        String url="https://demo1.jobtrack.com.au/ReST/v8/class/DBFolder?fields=name&q=" + StringUtilities.encode("name='modules'");

        ReST.Builder call = ReST.builder( url).setAuthorization("admin", "admin");
        
        Response r=call.getResponse();
        r.checkStatus();
        JSONObject json=r.getContentAsJSON();

        LOGGER.info( json.toString(2));

        JSONArray results = json.getJSONArray("results");
        assertEquals("should have one record", 1, results.length());

        JSONObject folder = results.getJSONObject(0);

        assertEquals( "find a match", "modules", folder.getString("name"));

        ReST.Builder call2 = ReST.builder( url).setMinCachePeriod("10 minutes");

        String data2 = call2.getContentAsString();

        JSONObject json2=new JSONObject( data2);

        LOGGER.info( json2.toString(2));

        JSONArray results2 = json2.getJSONArray("results");
        assertEquals("should have no records", 0, results2.length());

    }
    
    @SuppressWarnings("UseSpecificCatch")
    public void testMixedMethod() throws Exception
    {
        String url = "https://demo1.jobtrack.com.au/ReST/v8/class/DBFolder?_method=GET";
        ReST.Builder call = ReST.builder( url).setAuthorization("admin", "admin");
        try
        {
            call.setMethod(Method.DELETE);
            fail( "should have rejected this");
        }
        catch( Exception e)
        {
            //expected
        }
    }
    
    public void testGZip() throws Exception 
    {
        String url = "https://demo1.jobtrack.com.au/ReST/v8/class/Country";
        ReST.Builder b = ReST.builder( url);
        b.setMinCachePeriod("30 min");
        String text=b.getContentAsString();
        LOGGER.info( text);
        JSONObject json=new JSONObject(text);
        JSONObject json2 = b.getResponse().getContentAsJSON();
        assertEquals( "match JSON ", json.toString(2), json2.toString(2));
        
        
    }
    public void testGZipXML() throws Exception 
    {
        String url = "https://demo1.jobtrack.com.au/ReST/v8/class/Country";
        ReST.Builder b = ReST.builder( url);
        b.addParameter("_accept", "xml");
        String text=b.getContentAsString();
        LOGGER.info( text);
        Document doc=DocumentUtil.makeDocument(text);

        LOGGER.info( DocumentUtil.docToString(doc));
        
        
    }

    @SuppressWarnings("UseSpecificCatch")
    public void testGETBody() throws Exception
    {
        File file = File.createTempFile("test", "test",FileUtil.makeQuarantineDirectory());
        String url = "https://demo1.jobtrack.com.au/ReST/v8/class/DBFolder?_method=GET";
        ReST.Builder call = ReST.builder( url).setAuthorization("admin", "admin");
        try
        {
            call.setBody(file);
            fail( "should have rejected this");
        }
        catch( Exception e)
        {
            //expected
        }
        
        url = "https://demo1.jobtrack.com.au/ReST/v8/class/DBFolder?_method=POST";
        call = ReST.builder( url).setAuthorization("admin", "admin");
        try
        {
            call.setBody(file);
        }
        catch(Exception e)
        {
            fail("should pass here");
        }
        
        url = "https://demo1.jobtrack.com.au/ReST/v8/class/DBFolder";
        call = ReST.builder( url).setAuthorization("admin", "admin");
        call.setMethod(Method.GET);
        try
        {
            call.setBody(file);
            fail( "should have rejected this");
        }
        catch( Exception e)
        {
            //expected
        }

        url = "https://demo1.jobtrack.com.au/ReST/v8/class/DBFolder";
        call = ReST.builder( url).setAuthorization("admin", "admin");
        call.setMethod(Method.POST);
        try
        {
            call.setBody(file);
        }
        catch(Exception e)
        {
            fail("should pass here");
        }
        
        url = "https://demo1.jobtrack.com.au/ReST/v8/class/DBFolder";
        call = ReST.builder( url).setAuthorization("admin", "admin");
        call.setBody(file);
        try
        {
            call.setMethod(Method.GET);
            fail( "should have rejected this");
        }
        catch( Exception e)
        {
            //expected
        }

        url = "https://demo1.jobtrack.com.au/ReST/v8/class/DBFolder";
        call = ReST.builder( url).setAuthorization("admin", "admin");
        call.setBody(file);
        try
        {
            call.setMethod(Method.POST);
        }
        catch(Exception e)
        {
            fail("should pass here");
        }
    }

    public void testWrongPassword() throws Exception
    {
        String url="https://demo1.jobtrack.com.au/ReST/v8/class/DBFolder?fields=name&q=" + StringUtilities.encode("name='modules'");

        ReST.Builder call = ReST.builder( url).setAuthorization("admin", "admin");

        String data = call.getContentAsString();

        JSONObject json=new JSONObject( data);

        LOGGER.info( json.toString(2));

        JSONArray results = json.getJSONArray("results");
        assertEquals("should have one record", 1, results.length());

        JSONObject folder = results.getJSONObject(0);

        assertEquals( "find a match", "modules", folder.getString("name"));

        ReST.Builder call2 = ReST.builder( url).setAuthorization("admin", "zzzbzbzb").setMinCachePeriod("10 minutes");

        try{
            JSONObject json2 = call2.getResponseAndCheck().getContentAsJSON();

            fail( json2.toString(2));
        }
        catch( NotAuthorizedException nae)
        {
            // Correct
        }
    }

    /**
     * check call to Google maps.
     * @throws Exception a test failure.
     */
    public void testGoogleMap() throws Exception
    {
        String url="https://maps.googleapis.com/maps/api/directions/json?origin=Toronto&destination=Montreal&key=" + API_KEY;

        ReST.Builder call = ReST.builder(url);
        Response rr=call.getResponseAndCheck();
        String data = rr.getContentAsString();

        JSONObject json=new JSONObject( data);

        LOGGER.info( json.toString(2));

        String status=json.getString("status");

        assertEquals( "check status", "OK", status);

//        assertTrue("should be fetched", rr.trace.isFetched());


        /**
         * Second quick call should not cache a fetch.
         */
        ReST.Builder call2 = ReST.builder(url).setMinCachePeriod("5 minutes");

//        String data2 = call2.getContentAsString();
        Response rr2=call2.getResponse();
        String data2 = rr2.getContentAsString();
        JSONObject json2=new JSONObject( data2);

        LOGGER.info( json2.toString(2));

        String status2=json2.getString("status");

        assertEquals( "check status", "OK", status2);

        if( rr2.trace!=Trace.CACHED && rr2.trace != Trace.PREFETCH)
        {
            fail("should NOT be fetched: " + rr2.trace);
        }
        
        assertEquals("data should match", data, data2);
    }

    private static final String API_KEY="AIzaSyCVKU_wSE5sjYkmKiqeItKkIMIA4ptrDoo";
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.rest.selftest.TestReSTBuilder");//#LOGGER-NOPMD
}
