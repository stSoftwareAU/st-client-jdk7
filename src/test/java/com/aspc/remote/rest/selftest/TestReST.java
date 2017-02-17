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
import com.aspc.remote.rest.ReST;
import com.aspc.remote.database.InvalidDataException;
import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.CLogger;
import java.net.MalformedURLException;
import java.net.URL;

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
public class TestReST extends TestCase
{
    /**
     * Creates a new unit test
     * @param name the name of the unit
     */
    public TestReST( final String name )
{
        super( name );
    }

    /**
     * Creates the test suite
     * @return Test the test suite
     */
    public static Test suite()
{
        TestSuite suite = new TestSuite( TestReST.class );
        return suite;
    }

    /**
     * Entry point to run this test standalone
     * @param args the arguments to the test
     */
    public static void main( String[] args )
    {
        TestRunner.run( suite() );

        System.exit(0);
    }

    public void testValidURL() throws InvalidDataException, MalformedURLException
    {
        String good[]={
            "http://uat.aa",
            "http://thisisalongdomainnamewhichshouldbefine.com",
            "http://localhost//feed?UUID=6400000008-10012ada6-14a0c8c6bad&FETCH_CONTENT=true",
            "http://foo.com/blah_blah",
            "http://foo.com/blah_blah/",
            "http://foo.com/blah_blah_(wikipedia)",
            "https://foo.com/blah_blah_(wikipedia)_(again)",
//            "http://✪df.ws/123",
            "http://userid:password@example.com:8080",
            "http://userid:password@example.com:8080/",
            "http://userid@example.com",
            "http://userid@example.com/",
            "http://userid@example.com:8080",
            "http://userid@example.com:8080/",
            "http://userid:password@example.com",
            "http://userid:password@example.com/",
            "http://142.42.1.1/",
            "http://142.42.1.1:8080/",
//            "http://➡.ws/䨹",
//            "http://⌘.ws",
//            "http://⌘.ws/",
            "http://foo.com/blah_(wikipedia)#cite-1",
            "http://foo.com/blah_(wikipedia)_blah#cite-1",
            "http://foo.com/unicode_(✪)_in_parens",
            "http://foo.com/(something)?after=parens",
//            "http://☺.damowmow.com/",
            "http://code.google.com/events/#&product=browser",
            "http://j.mp",
            "https://foo.bar/baz",
            "http://foo.bar/?q=Test%20URL-encoded%20stuff",
//            "http://مثال.إختبار",
//            "http://例子.测试",
            "http://223.255.255.254",
            "http://-.~_!$&'()*+,;=:%40:80%2f::::::@example.com",
            "http://a.b-c.de"
        };

        for( String url: good)
        {
            ReST.Builder b=ReST.builder( url);
            LOGGER.info(b);
//            assertTrue( url, valid);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void testInvalidURL() throws MalformedURLException, Exception
    {
        String bad[]={
            "http://.",
            "http://",
            "foo.com",
            "http://..",
            "http://../",
            "http://?",
            "http://??",
            "http://??/",
            "http://#",
            "http://##",
            "http://##/",
            "http://foo.bar?q=Spaces should be encoded",
            "//",
            "//a",
            "///a",
            "///",
            "http:///a",
            "rdar://1234",
            "h://test",
            "http:// shouldfail.com",
            ":// should fail",
            "http://foo.bar/foo(bar)baz quux",
            "ftps://foo.bar/",
            "http://-error-.invalid/",
            "http://a.b--c.de/",
            "http://-a.b.co",
            "http://a.b-.co",
            "http://0.0.0.0",
//            "http://10.1.1.0",
//            "http://10.1.1.255",
//            "http://224.1.1.1",
            "http://1.1.1.1.1",
            "http://123.123.123",
            "http://3628126748",
            "http://.www.foo.bar/",
            "http://www.foo.bar./",
            "http://.www.foo.bar./",
//            "http://10.1.1.1",
//            "http://10.1.1.254"
        };

        for( String url: bad)
        {
            try
            {
                ReST.builder( url).getContentAsString();
                
                fail( "should be bad " + url);
            }
            catch( InvalidDataException ide)
            {
                // good
            }

        }
    }

    /**
     * long urls
     * @throws Exception a test failure.
     */
    public void testLong() throws Exception
    {
        String path="cms-functionality-the-system-should-allow-for-the-creation-of-cms-managed-e-newsletter-campaigns-including-build-templates-editing-"+
                  "tools-ability-to-create-export-and-import-subscriber-lists-includes-subscribe-unsubscribe-confirmation-email-responses-to-users";
        URL url=new URL("http://aspc.jobtrack.com.au/feed?PATH=" + path + "&FETCH_CONTENT=true$$$&p2=" + path + "&p3=" + path);
        String result=ReST.builder(url).setMinCachePeriod( "10 min").getContentAsString().trim();
        LOGGER.info( result);
        if( result.isEmpty())
        {
            fail( "no result");
        }
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.rest.selftest.TestReST");//#LOGGER-NOPMD
}
