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
package com.aspc.remote.util.net.selftest;
import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.CLogger;

import com.aspc.remote.util.net.URLParser;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * check the parsing of the URL
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED test case</i>
 *
 * @author Nigel Leck
 * @since November 13, 2008
 */
public class TestURLParser extends TestCase
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.net.selftest.TestURLParser");//#LOGGER-NOPMD

    /**
     * Check a series of URLs
     * @throws Exception a test failure.
     */
    public void testParser() throws Exception
    {
        String URLS[][]={
            {"ftp://nam\\gadnetftp:password@host.net/xyz/tmp","ftp","nam\\gadnetftp","password", "host.net","","xyz/tmp"},
            {"user@hostname", "UNKNOWN", "user", "", "hostname", "", ""},
            {"user@hostname:123", "UNKNOWN", "user", "", "hostname", "123", ""},
            {"ftp://us%40er:password@hostname/dir", "ftp", "us@er", "password", "hostname", "", "dir"},
            {"user@hostname/dir", "UNKNOWN", "user", "", "hostname", "", "dir"},
            {"user@hostname/a%20number%20of%20names%20", "UNKNOWN", "user", "", "hostname", "", "a number of names "},
            {"ftp://user@hostname/dir", "ftp", "user", "", "hostname", "", "dir"},
            {"ftp://hostname/dir", "ftp", "guest", "", "hostname", "", "dir"},
            {"ftp://hostname", "ftp", "guest", "", "hostname", "", ""},
            {"ftp://user:password@hostname/dir", "ftp", "user", "password", "hostname", "", "dir"},
            {"ftp://user:password@hostname:123/dir", "ftp", "user", "password", "hostname", "123", "dir"},
            {"ftp://user:password@hostname:123", "ftp", "user", "password", "hostname", "123", ""},
            {"ftp://domain%2Fuser:password@hostname:123", "ftp", "domain/user", "password", "hostname", "123", ""},
        };

        for (String[] temp : URLS) {
            String url = temp[0];
            String protocol = temp[1];
            String user = temp[2];
            String password = temp[3];
            String hostname = temp[4];
            String port = temp[5];
            String uri = temp[6];

            URLParser parser = new URLParser(url);

            assertEquals( "protocol from " + url, protocol, parser.getProtocol());
            assertEquals( "user from " + url, user, parser.getUserName());
            assertEquals( "password from " + url, password, parser.getPassword());
            assertEquals( "hostname from " + url, hostname, parser.getHostName());
            assertEquals( "port from " + url, port, parser.getPort());
            assertEquals( "uri from " + url, uri, parser.getURI());

            String tempURL=parser.toString();

            URLParser parser2 = new URLParser(tempURL);

            assertEquals( "protocol from " + tempURL, protocol, parser2.getProtocol());
            assertEquals( "user from " + tempURL, user, parser2.getUserName());
            //assertEquals( "password", password, parser.getPassword());
            assertEquals( "hostname from " + tempURL, hostname, parser2.getHostName());
            assertEquals( "port from " + tempURL, port, parser2.getPort());
            assertEquals( "uri from " + tempURL, uri, parser2.getURI());
        }
    }

    /**
     * Creates a new unit test
     * @param name the name of the unit
     */
    public TestURLParser( final String name )
    {
        super( name );
    }
    
    /**
     * Creates the test suite
     * @return Test the test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite( TestURLParser.class );
        return suite;
    }
    
    /**
     * Entry point to run this test standalone
     * @param args the arguments to the test
     */
    public static void main( String[] args )
    {
        TestRunner.run( suite() );
    }
}
