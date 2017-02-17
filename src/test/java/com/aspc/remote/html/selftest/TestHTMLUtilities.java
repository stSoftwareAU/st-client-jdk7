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
package com.aspc.remote.html.selftest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import com.aspc.remote.html.*;
import com.aspc.remote.util.misc.CLogger;
import java.net.URL;
import org.apache.commons.logging.Log;

/**
 *  check the GWT modules.
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED self test unit</i>
 *
 *  @author         Paul Smout
 *  
 *  @since          June 5 2008
 */
public class TestHTMLUtilities extends TestCase
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.html.selftest.TestHTMLUtilities");//#LOGGER-NOPMD

    /**
     * Creates new VirtualDBTestUnit
     * @param name The name of the test unit
     */
    public TestHTMLUtilities(String name)
    {
        super( name);
    }

    /**
     * @param args The command line arguments
     */
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }

    /**
     * @return the value
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestHTMLUtilities.class);
        return suite;
    }

    /**
     * Upgrade to SSL
     *
     * @throws Exception a serious problem
     */
    public void testHTTPS() throws Exception
    {
        checkHTTPS();
        checkHTTPS();
    }
    
    private void checkHTTPS() throws Exception
    {
        URL url = HTMLUtilities.bestURL("http://www.stsoftware.com.au", true);

        assertEquals("should have upgraded to SSL", "https", url.getProtocol());
        assertEquals("Change to default port https", "https://www.stsoftware.com.au", url.toString());

        url = HTMLUtilities.bestURL("http://www.stsoftware.com.au:8080/siteST", true);
        assertEquals("should have upgraded to SSL", "https", url.getProtocol());
        assertEquals("Change to default port https", "https://www.stsoftware.com.au/siteST", url.toString());
        url = HTMLUtilities.bestURL("http://www.stsoftware.com.au:80/siteST", true);

        assertEquals("should have upgraded to SSL", "https", url.getProtocol());
        url = HTMLUtilities.bestURL("https://www.stsoftware.com.au", true);

        assertEquals("Already SSL no need to change", "https", url.getProtocol());

        url = HTMLUtilities.bestURL("https://www.stsoftware.com.au", false);

        assertEquals("Already SSL no need to change", "https", url.getProtocol());
    }
    
    /**
     * Just Check HTTP
     *
     * @throws Exception a serious problem
     */
    public void testHTTP() throws Exception
    {
        checkHTTP();
        checkHTTP();
    }
    
    private void checkHTTP() throws Exception
    {
        URL url = HTMLUtilities.bestURL("http://www.stsoftware.com.au/site/ST?X=Y", false);

        assertEquals("Don't upgrade if not asked", "http", url.getProtocol());

        assertEquals("Don't change", "http://www.stsoftware.com.au/site/ST?X=Y", url.toString());

        url = HTMLUtilities.bestURL("http://www.stsoftware.com.au:80/site/ST?X=Y", false);

        assertEquals("Don't upgrade if not asked", "http", url.getProtocol());

        assertEquals("Don't change", "http://www.stsoftware.com.au/site/ST?X=Y", url.toString());
        
        url = HTMLUtilities.bestURL("http://60.241.239.222:8080", true);

        assertEquals("devserver has no SSL", "http", url.getProtocol());

  //      url = HTMLUtilities.bestURL("http://60.241.239.222/site", false);
        
   //     assertEquals("devserver has no SSL", "http", url.getProtocol());
       // assertEquals("Change to 8080", "http://60.241.239.222:8080/site", url.toString());
                
        url = HTMLUtilities.bestURL("http://60.241.239.223/site", false);
        
        assertEquals("devserver has no SSL", "http", url.getProtocol());
        assertEquals("Leave as is as we can't connect", "http://60.241.239.223/site", url.toString());
        /*
        try
        {
            URL testURL = new URL("http://localhost");
            URLConnection c = testURL.openConnection();
           //NetUrl.relaxSSLConnection(c);

            c.connect();
        }
        catch(ConnectException e)
        {
            try
            {
                URL testURL = new URL("http://localhost:8080");
                URLConnection c = testURL.openConnection();
               //NetUrl.relaxSSLConnection(c);

                c.connect();
                
                //only test when the port 80 is not available
                url = HTMLUtilities.bestURL("http://localhost/site/ST?X=Y", false);
                assertEquals("1) should use port 8080", 8080, url.getPort());
                url = HTMLUtilities.bestURL("http://localhost/site/ST?X=Y", false);
                assertEquals("2) should use port 8080", 8080, url.getPort());
            }
            catch(ConnectException e2)
            {
                
            }
        }*/
    }
}
