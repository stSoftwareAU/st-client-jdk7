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
import com.aspc.remote.rest.Response;
import com.aspc.remote.rest.Status;
import org.apache.commons.logging.Log;
import com.aspc.remote.util.misc.CLogger;

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
public class TestStatusReference extends TestCase
{
    /**
     * Creates a new unit test
     * @param name the name of the unit
     */
    public TestStatusReference( final String name )
    {
        super( name );
    }

    /**
     * Creates the test suite
     * @return Test the test suite
     */
    public static Test suite()
{
        TestSuite suite = new TestSuite( TestStatusReference.class );
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
     * Check all the references are valid.
     * @throws Exception a test failure.
     */
    public void testCheckReferenceIsValid() throws Exception
    {
        for( Status status:Status.values())
        {
            String url=status.reference;
            if( url!=null)
            {
                Response r = ReST.builder(url).setMinCachePeriod("5 min").getResponse();
                if( r.status.isError())
                {
                    fail( url + " status: " + r.status);
                }

            }
        }

    }

    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.rest.selftest.TestStatusReference");//#LOGGER-NOPMD
}
