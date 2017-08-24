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
import com.aspc.remote.util.net.NetClientFactory;
import com.aspc.remote.util.net.NetUtil;

import com.aspc.remote.util.net.URLParser;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Check that we can connect to an FTP server.
 *  <br>
 *  <i>THREAD MODE: SINGLE-THREADED test case</i>
 *
 * @author Nigel Leck
 */
public class TestFTP extends TestCase
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.net.selftest.TestFTP");//#LOGGER-NOPMD

    /**
     * Check a sample FTP server.
     * @throws Exception a test failure.
     */
    public void testConnect() throws Exception
    {
        String[] fileList = NetUtil.retrieveFileList("ftp://anonymous:@speedtest.tele2.net/");
        boolean found10GB=false;
        for( String fn: fileList)
        {
            if( fn.contains("10GB.zip"))
            {
                found10GB=true;
            }
            LOGGER.info( fn);
        }

        assertTrue("should have found file", found10GB);
    }

    /**
     * Creates a new unit test
     * @param name the name of the unit
     */
    public TestFTP( final String name )
    {
        super( name );
    }

    /**
     * Creates the test suite
     * @return Test the test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite( TestFTP.class );
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
