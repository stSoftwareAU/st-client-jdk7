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
package com.aspc.remote.rest.internal.selftest;
import com.aspc.remote.database.InvalidDataException;
import com.aspc.remote.rest.internal.ReSTAuthorization;
import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.CLogger;
import java.net.MalformedURLException;

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
public class TestReSTAuthorization extends TestCase
{
    /**
     * Creates a new unit test
     * @param name the name of the unit
     */
    public TestReSTAuthorization( final String name )
{
        super( name );
    }

    /**
     * Creates the test suite
     * @return Test the test suite
     */
    public static Test suite()
{
        TestSuite suite = new TestSuite( TestReSTAuthorization.class );
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

    public void testToString() throws InvalidDataException, MalformedURLException
    {
        assertEquals( "Don't show the full token", "token: owne******************23d5", new ReSTAuthorization("owner-64152f6f1221d40f223d5").toString());
        assertEquals( "Don't show the full token", "token: QddA***********************UXYH", new ReSTAuthorization("QddAGsDSS0FkXCb1zRCCzzeShZRnUXYH").toString());
        assertEquals( "Don't show the password", "aus\\user:xxxx", new ReSTAuthorization("user","password","aus").toString());
//        LOGGER.info("len: " + StringUtilities.decodeBase64("1GWuHmFyyKQUKWV6sR6EEzSCdLGnhqyZFBqLagHp".getBytes()).length);
    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.rest.internal.selftest.TestReSTAuthorization");//#LOGGER-NOPMD
}
