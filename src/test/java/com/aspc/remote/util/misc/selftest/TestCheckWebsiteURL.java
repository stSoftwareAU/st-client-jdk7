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
package com.aspc.remote.util.misc.selftest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.aspc.remote.util.misc.*;

import org.apache.commons.logging.Log;

/**
 *  Check DNS Black list
 *
 * <br>
 * <i>THREAD MODE: SINGLE-THREADED self test unit</i>
 *
 *  @author         Nigel Leck
 *  @since          3 August 2014
 */
public class TestCheckWebsiteURL extends TestCase
{
    private static final Log LOGGER = CLogger.getLog( "com.aspc.remote.util.misc.selftest.TestCheckWebsiteURL");//#LOGGER-NOPMD

    /**
     * Creates new VirtualDBTestUnit
     * @param name The name of the test unit
     */
    public TestCheckWebsiteURL(String name)
    {
        super( name);
    }

    /**
     * run the tests
     * @param args the args
     */
    public static void main(String[] args)
    {
        Test test = suite();
//        test= TestSuite.createTest(TestCheckWebsiteURL.class, "testInvalidSites");
        TestRunner.run(test);
        System.exit(0);
    }

    /**
     * create a test suite
     * @return the suite of tests
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(TestCheckWebsiteURL.class);
        return suite;
    }

    public void testValidSites() throws Exception
    {
        String sites[]={
            "https://www.apple.com",
            "https://demo1.jobtrack.com.au",
            "https://demo2.jobtrack.com.au",
            "https://www.jobtrack.com.au",
            "https://www.stsoftware.com.au",
        };

        for( String site: sites)
        {
            if( CUtilities.checkWebsiteURL(site) ==false)
            {
                fail( "should be valid: " + site);
            }
        }
    }

    public void testInvalidSites() throws Exception
    {
        String sites[]={
            "https://www.apple.com.zzzz"
        };

        for( String site: sites)
        {
            if( CUtilities.checkWebsiteURL(site))
            {
                fail( "should be invalid: " + site);
            }
        }
    }

}
